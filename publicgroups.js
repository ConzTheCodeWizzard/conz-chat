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

  if(!tag || !display){ showPopup("Fill everything in"); return; }
  if(!tag.startsWith("#")){ showPopup("Group name must start with #"); return; }
  let valid = /^#[a-zA-Z0-9_-]+$/;
  if(!valid.test(tag)){ showPopup("Only letters, numbers, - and _ allowed"); return; }

  db.collection("publicGroups")
  .where("tagLower","==",tag.toLowerCase())
  .get()
  .then(snap=>{
    if(!snap.empty){ showPopup("This group name is already taken"); return; }

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
    .catch(err=>{ showPopup("Failed to create group: " + err.message); });
  });
};

window.renderPublicGroups = function(){
  db.collection("publicGroups")
  .where("members","array-contains", window.currentUser?.uid || "")
  .onSnapshot(snap=>{
    document.querySelectorAll(".publicGroupItem").forEach(el=>el.remove());

    let groups = [];
    snap.forEach(doc=>{ groups.push({ id: doc.id, ...doc.data() }); });
    groups.sort((a,b)=>(b.lastTime||0)-(a.lastTime||0));

    groups.forEach(group=>{
      let div = document.createElement("div");
      div.className = "publicGroupItem";
      div.innerHTML = `
        <div class="chatAvatar">
          ${group.photo ? `<img src="${group.photo}">` : `<div style="font-size:22px;">👥</div>`}
        </div>
        <div class="chatNameWrap">
          <div class="chatItemName">${group.displayName}</div>
          <div style="font-size:12px;opacity:0.6;margin-top:2px;">${group.tag}</div>
          ${group.lastMessage ? `<div style="font-size:12px;opacity:0.5;margin-top:2px;">${group.lastMessage.substring(0,40)}${group.lastMessage.length>40?"...":""}</div>` : ""}
        </div>
      `;
      div.onclick = ()=>{ openPublicGroup(group); };
      chatList.prepend(div);
    });
  }, err=>{ console.error(err.message); });
};

/* ===== PUBLIC GROUP MESSAGING ===== */

let unsubscribePublicGroupMessages = null;
let unsubscribePublicGroupTyping = null;
let publicGroupTypingTimeout = null;

window.openPublicGroup = function(group){
  if(unsubscribePublicGroupMessages){ unsubscribePublicGroupMessages(); unsubscribePublicGroupMessages = null; }
  if(unsubscribePublicGroupTyping){ unsubscribePublicGroupTyping(); unsubscribePublicGroupTyping = null; }
  if(window.unsubscribeGroupMessages){ window.unsubscribeGroupMessages(); window.unsubscribeGroupMessages = null; }
  if(window.unsubscribeMessages){ window.unsubscribeMessages(); window.unsubscribeMessages = null; }

  window.currentChatUser = null;

  db.collection("publicGroups").doc(group.id).get().then(doc=>{
    let freshGroup = { id: doc.id, ...doc.data() };
    window.currentGroup = freshGroup;

    document.getElementById("chatName").onclick = function(){ openGroupInfo(freshGroup); };
    document.getElementById("chatName").innerHTML = `👥 ${freshGroup.displayName}`;

    let callBar = document.getElementById("chatCallBar");
    if(callBar) callBar.style.display = "flex";

    document.getElementById("messages").innerHTML = "";
    show("chat");

    // Firestore messages listener
    unsubscribePublicGroupMessages = db.collection("publicGroupMessages")
    .where("groupId","==", freshGroup.id)
    .orderBy("time","asc")
    .onSnapshot(async snap=>{
      let messagesEl = document.getElementById("messages");
      messagesEl.innerHTML = "";

      let msgList = [];
      snap.forEach(doc=>{ msgList.push({ id: doc.id, ...doc.data() }); });

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

        let roleBadge = "";
        if(m.from === freshGroup.owner){
          roleBadge = `<span class="inlineBadge ownerInline">Owner</span>`;
        } else if((freshGroup.admins||[]).includes(m.from)){
          roleBadge = `<span class="inlineBadge adminInline">Admin</span>`;
        }

        // Handle media types
        let contentHtml = "";
        if(m.type === "image"){
          contentHtml = `<img src="${m.url}" class="msgImage" onclick="viewFullImage('${m.url}')">`;
          if(m.isCamera) contentHtml += `<div class="msgCameraLabel">📷 Camera</div>`;
        } else if(m.type === "video"){
          contentHtml = `<video src="${m.url}" class="msgVideo" controls playsinline></video>`;
        } else if(m.type === "voice"){
          contentHtml = `<div class="voiceNoteWrap"><audio src="${m.url}" controls class="voiceAudio"></audio><div class="voiceLabel">🎙️ Voice Note</div></div>`;
        } else {
          contentHtml = `<div class="msgText">${m.text||""}</div>`;
        }

        bubble.innerHTML = `
          ${!isMine ? `<div class="msgSenderName">${u.displayName||u.username||"Unknown"} ${roleBadge}</div>` : ""}
          ${contentHtml}
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

    // Typing indicator
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
      if(typeof window.showTypingIndicator === "function"){
        window.showTypingIndicator(typers.length > 0, typers[0] || "");
      }
    });

  }).catch(err=>{ showPopup("Error opening group: " + err.message); });
};

/* ===== SEND PUBLIC GROUP MESSAGE ===== */
window.sendPublicGroupMessage = function(){
  let text = (window.msgInput || document.getElementById("msgInput"))?.value?.trim();
  if(!text) return;
  if(!window.currentGroup || !window.currentGroup.id){
    showPopup("No group selected");
    return;
  }

  let inp = document.getElementById("msgInput");
  if(inp){ inp.value = ""; inp.focus(); }
  let sendBtn = document.getElementById("sendBtn");
  if(sendBtn) sendBtn.classList.remove("active");

  clearPublicGroupTyping();

  db.collection("publicGroupMessages").add({
    groupId: window.currentGroup.id,
    from: window.currentUser.uid,
    text: text,
    type: "text",
    time: Date.now()
  }).then(()=>{
    db.collection("publicGroups").doc(window.currentGroup.id).update({
      lastMessage: text,
      lastTime: Date.now()
    });
  }).catch(err=>{ showPopup("Send failed: " + err.message); });
};

/* ===== TYPING ===== */
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

document.addEventListener("DOMContentLoaded", function(){
  let inp = document.getElementById("msgInput");
  if(!inp) return;
  inp.addEventListener("input", function(){
    if(window.currentGroup && window.currentGroup.id && window.currentGroup.tag){
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
  }catch(err){ showPopup(err.message); }
};

/* ===== KIK TIME ===== */
window.formatKikTime = function(ts){
  if(!ts) return "";
  let now = new Date();
  let d = new Date(ts);
  let diffDays = Math.floor((now - d) / 86400000);
  let hours = d.getHours();
  let mins = d.getMinutes().toString().padStart(2,"0");
  let ampm = hours >= 12 ? "PM" : "AM";
  let h = hours % 12 || 12;
  let timeStr = `${h}:${mins} ${ampm}`;
  if(diffDays === 0) return timeStr;
  if(diffDays === 1) return `Yesterday ${timeStr}`;
  if(diffDays < 7){ let days = ["Sun","Mon","Tue","Wed","Thu","Fri","Sat"]; return `${days[d.getDay()]} ${timeStr}`; }
  let months = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];
  return `${months[d.getMonth()]} ${d.getDate()} ${timeStr}`;
};
