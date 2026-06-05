console.log("Public Groups Loaded");

window.openPublicGroupCreate = function(){
  show("publicGroupCreate");
};

window.pickGroupPhoto = function(){
  document.getElementById("groupPhotoInput").click();
};

window.selectedGroupPhoto = "";

document.addEventListener("change", function(e){
  if(e.target.id !== "groupPhotoInput") return;
  let file = e.target.files[0];
  if(!file) return;
  let reader = new FileReader();
  reader.onload = function(){
    window.selectedGroupPhoto = reader.result;
    document.getElementById("groupPhotoPreview").innerHTML =
      `<img src="${reader.result}">`;
  };
  reader.readAsDataURL(file);
});

window.publicGroups = [];

window.createPublicGroup = function(){
  let tag = document.getElementById("publicGroupTag").value.trim();
  let display = document.getElementById("publicGroupDisplay").value.trim();

  if(!tag || !display){
    showPopup("Fill everything in");
    return;
  }
  if(!tag.startsWith("#")){
    showPopup("Group name must start with #");
    return;
  }
  let valid = /^#[a-zA-Z0-9_-]+$/;
  if(!valid.test(tag)){
    showPopup("Only letters, numbers, - and _ allowed");
    return;
  }

  db.collection("publicGroups")
  .where("tagLower","==",tag.toLowerCase())
  .get()
  .then(snap=>{
    if(!snap.empty){
      showPopup("This group name is already taken");
      return;
    }

    let group = {
      tag: tag,
      tagLower: tag.toLowerCase(),
      displayName: display,
      photo: window.selectedGroupPhoto || "",
      owner: window.currentUser?.uid || "unknown",
      admins: [],
      members: [window.currentUser?.uid || "unknown"],
      banned: [],
      lastMessage: "",
      lastTime: Date.now()
    };

    db.collection("publicGroups").add(group)
    .then(ref=>{
      group.id = ref.id;
      showPopup("Public group created!");
      document.getElementById("publicGroupTag").value = "";
      document.getElementById("publicGroupDisplay").value = "";
      window.selectedGroupPhoto = "";
      document.getElementById("groupPhotoPreview").innerHTML = "+";
      show("home");
    })
    .catch(err=>{
      showPopup("Failed to create group: " + err.message);
    });
  });
};

window.renderPublicGroups = function(){
  db.collection("publicGroups")
  .where("members","array-contains", window.currentUser?.uid || "")
  .onSnapshot(snap=>{
    document.querySelectorAll(".publicGroupItem").forEach(el=>el.remove());

    let groups = [];
    snap.forEach(doc=>{
      groups.push({ id: doc.id, ...doc.data() });
    });
    groups.sort((a,b)=>(b.lastTime||0)-(a.lastTime||0));

    groups.forEach(group=>{
      let div = document.createElement("div");
      div.className = "publicGroupItem";
      div.innerHTML = `
        <div class="chatAvatar">
          ${group.photo ? `<img src="${group.photo}">` : `👥`}
        </div>
        <div class="chatNameWrap">
          <div>${group.displayName}</div>
          <div style="font-size:12px;opacity:0.6;margin-top:2px;">${group.tag}</div>
          ${group.lastMessage ? `<div style="font-size:12px;opacity:0.5;margin-top:2px;">${group.lastMessage.substring(0,40)}${group.lastMessage.length>40?"...":""}</div>` : ""}
        </div>
      `;
      div.onclick = ()=>{ openPublicGroup(group); };
      chatList.prepend(div);
    });
  }, err=>{ alert(err.message); });
};

/* ===== PUBLIC GROUP MESSAGING — FIRESTORE BACKED ===== */

let unsubscribePublicGroupMessages = null;
let unsubscribePublicGroupTyping = null;
let publicGroupTypingTimeout = null;

window.openPublicGroup = function(group){
  // Unsubscribe previous listeners
  if(unsubscribePublicGroupMessages){
    unsubscribePublicGroupMessages();
    unsubscribePublicGroupMessages = null;
  }
  if(unsubscribePublicGroupTyping){
    unsubscribePublicGroupTyping();
    unsubscribePublicGroupTyping = null;
  }
  if(window.unsubscribeGroupMessages){
    window.unsubscribeGroupMessages();
    window.unsubscribeGroupMessages = null;
  }
  if(window.unsubscribeMessages){
    window.unsubscribeMessages();
    window.unsubscribeMessages = null;
  }

  // Refresh group data from Firestore before opening
  db.collection("publicGroups").doc(group.id).get().then(doc=>{
    let freshGroup = { id: doc.id, ...doc.data() };
    window.currentGroup = freshGroup;

    document.getElementById("chatName").onclick = function(){
      openGroupInfo(freshGroup);
    };
    document.getElementById("chatName").innerHTML = `👥 ${freshGroup.displayName}`;

    // Show call buttons in chat topbar for groups
    let callBar = document.getElementById("chatCallBar");
    if(callBar) callBar.style.display = "flex";

    document.getElementById("messages").innerHTML = "";
    show("chat");

    // Listen to Firestore messages for this public group
    unsubscribePublicGroupMessages = db.collection("publicGroupMessages")
    .where("groupId","==", freshGroup.id)
    .orderBy("time","asc")
    .onSnapshot(async snap=>{
      let messagesEl = document.getElementById("messages");
      messagesEl.innerHTML = "";

      let msgList = [];
      snap.forEach(doc=>{ msgList.push({ id: doc.id, ...doc.data() }); });

      // Cache user data to avoid repeated fetches
      let userCache = {};
      for(const m of msgList){
        if(!userCache[m.from]){
          let uDoc = await db.collection("users").doc(m.from).get();
          userCache[m.from] = uDoc.data() || {};
        }
      }

      msgList.forEach(m=>{
        let u = userCache[m.from] || {};
        let isMine = m.from === window.currentUser.uid;
        let wrap = document.createElement("div");
        wrap.className = "msgWrap " + (isMine ? "me" : "them");

        let avatar = document.createElement("div");
        avatar.className = "msgAvatar";
        if(u.photo){ avatar.innerHTML = `<img src="${u.photo}">`; }

        let bubble = document.createElement("div");
        bubble.className = "msg";

        // Role badge inline
        let roleBadge = "";
        if(m.from === freshGroup.owner){
          roleBadge = `<span class="inlineBadge ownerInline">Owner</span>`;
        } else if((freshGroup.admins||[]).includes(m.from)){
          roleBadge = `<span class="inlineBadge adminInline">Admin</span>`;
        }

        bubble.innerHTML = `
          ${!isMine ? `<div class="msgSenderName">${u.displayName||u.username||"Unknown"} ${roleBadge}</div>` : ""}
          <div class="msgText">${m.text}</div>
          <div class="msgMeta">${formatKikTime(m.time)}</div>
        `;

        if(isMine){
          wrap.appendChild(bubble);
          wrap.appendChild(avatar);
        } else {
          wrap.appendChild(avatar);
          wrap.appendChild(bubble);
        }
        messagesEl.appendChild(wrap);
      });

      messagesEl.scrollTop = messagesEl.scrollHeight;
    }, err=>{ console.error(err); });

    // Typing indicator listener
    unsubscribePublicGroupTyping = db.collection("publicGroupTyping")
    .where("groupId","==", freshGroup.id)
    .onSnapshot(snap=>{
      let typers = [];
      snap.forEach(doc=>{
        let d = doc.data();
        if(d.uid !== window.currentUser.uid && d.typing && (Date.now()-d.ts) < 5000){
          typers.push(d.name || "Someone");
        }
      });
      let typingEl = document.getElementById("typingIndicator");
      if(typingEl){
        if(typers.length > 0){
          typingEl.innerHTML = `<span class="typingDots">${typers[0]} is typing<span class="dot1">.</span><span class="dot2">.</span><span class="dot3">.</span></span>`;
          typingEl.style.display = "block";
        } else {
          typingEl.style.display = "none";
        }
      }
    });

  }).catch(err=>{ showPopup("Error opening group: " + err.message); });
};

window.sendPublicGroupMessage = function(){
  if(!msgInput.value.trim()) return;
  if(!window.currentGroup) return;

  let text = msgInput.value.trim();
  msgInput.value = "";
  if(window.sendBtn) sendBtn.classList.remove("active");

  // Clear typing
  clearPublicGroupTyping();

  db.collection("publicGroupMessages").add({
    groupId: window.currentGroup.id,
    from: window.currentUser.uid,
    text: text,
    time: Date.now()
  }).then(()=>{
    db.collection("publicGroups").doc(window.currentGroup.id).update({
      lastMessage: text,
      lastTime: Date.now()
    });
  }).catch(err=>{ showPopup("Send failed: " + err.message); });
};

/* ===== TYPING INDICATOR FOR PUBLIC GROUPS ===== */

function setPublicGroupTyping(){
  if(!window.currentGroup || !window.currentGroup.id) return;
  let myName = window.myData?.displayName || window.myData?.username || "Someone";
  db.collection("publicGroupTyping").doc(window.currentUser.uid + "_" + window.currentGroup.id).set({
    groupId: window.currentGroup.id,
    uid: window.currentUser.uid,
    name: myName,
    typing: true,
    ts: Date.now()
  });
}

function clearPublicGroupTyping(){
  if(!window.currentGroup || !window.currentGroup.id) return;
  db.collection("publicGroupTyping").doc(window.currentUser.uid + "_" + window.currentGroup.id).set({
    groupId: window.currentGroup.id,
    uid: window.currentUser.uid,
    typing: false,
    ts: Date.now()
  });
}

// Hook typing events onto msgInput (will also be used for DM typing)
document.addEventListener("DOMContentLoaded", function(){
  let inp = document.getElementById("msgInput");
  if(!inp) return;
  inp.addEventListener("input", function(){
    if(window.currentGroup && window.currentGroup.id){
      setPublicGroupTyping();
      clearTimeout(publicGroupTypingTimeout);
      publicGroupTypingTimeout = setTimeout(clearPublicGroupTyping, 3000);
    }
  });
});

/* ===== SEARCH PUBLIC GROUPS ===== */

window.searchPublicGroups = function(){
  let query = document.getElementById("publicGroupSearchInput").value.trim().toLowerCase();
  let results = document.getElementById("publicGroupResults");
  results.innerHTML = "";

  db.collection("publicGroups").get().then(snap=>{
    snap.forEach(doc=>{
      let group = { id: doc.id, ...doc.data() };
      let tag = (group.tag||"").toLowerCase();
      let display = (group.displayName||"").toLowerCase();
      if(query && !tag.includes(query) && !display.includes(query)) return;

      let div = document.createElement("div");
      div.className = "publicGroupItem";
      div.innerHTML = `
        <div class="chatAvatar">
          ${group.photo ? `<img src="${group.photo}">` : "👥"}
        </div>
        <div style="flex:1;">
          <div>${group.displayName}</div>
          <div style="font-size:12px;opacity:0.6;">${group.tag}</div>
          <div style="font-size:11px;opacity:0.5;">${(group.members||[]).length} members</div>
        </div>
      `;
      div.onclick = function(){ joinPublicGroup(group); };
      results.appendChild(div);
    });
  });
};

window.joinPublicGroup = async function(group){
  try{
    let ref = db.collection("publicGroups").doc(group.id);
    let doc = await ref.get();
    let data = doc.data() || {};
    let members = data.members || [];

    if(members.includes(window.currentUser.uid)){
      openPublicGroup({ id: doc.id, ...data });
      return;
    }

    members.push(window.currentUser.uid);
    await ref.update({ members: members });
    showPopup("Joined public group!");
    openPublicGroup({ id: doc.id, ...data, members: members });
  }catch(err){
    showPopup(err.message);
  }
};

/* ===== KIK-STYLE TIME FORMATTER ===== */
window.formatKikTime = function(ts){
  if(!ts) return "";
  let now = new Date();
  let d = new Date(ts);
  let diffMs = now - d;
  let diffDays = Math.floor(diffMs / 86400000);

  let hours = d.getHours();
  let mins = d.getMinutes().toString().padStart(2,"0");
  let ampm = hours >= 12 ? "PM" : "AM";
  let h = hours % 12 || 12;
  let timeStr = `${h}:${mins} ${ampm}`;

  if(diffDays === 0){
    return timeStr;
  } else if(diffDays === 1){
    return `Yesterday ${timeStr}`;
  } else if(diffDays < 7){
    let days = ["Sun","Mon","Tue","Wed","Thu","Fri","Sat"];
    return `${days[d.getDay()]} ${timeStr}`;
  } else {
    let months = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];
    return `${months[d.getMonth()]} ${d.getDate()} ${timeStr}`;
  }
};
