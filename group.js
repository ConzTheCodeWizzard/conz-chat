/* =========================================
   Conz was here
   Hello to you toooo MR SKIDDYYY ~Conz~
   ========================================= */

window.currentGroup = null;
window.unsubscribeGroupMessages = null;
window.groupListenerLoaded = false;

let groupTypingTimeout = null;
let unsubscribePrivateGroupTyping = null;

/* ===== CREATE PRIVATE GROUP ===== */

window.createGroup = async function(){
  let name = prompt("Enter group name");
  if(!name || !name.trim()) return;

  try{
    let ref = await db.collection("groups").add({
      name: name,
      owner: window.currentUser.uid,
      members: [window.currentUser.uid],
      admins: [],
      created: Date.now(),
      photo: "",
      lastMessage: "",
      lastTime: Date.now()
    });

    showPopup("Group created!");
    openGroup(ref.id);
  }catch(err){
    showPopup(err.message);
  }
};

/* ===== LOAD PRIVATE GROUPS LIST ===== */

window.loadGroups = function(){
  if(!window.currentUser) return;
  if(window.groupListenerLoaded) return;
  window.groupListenerLoaded = true;

  db.collection("groups")
  .where("members","array-contains", window.currentUser.uid)
  .onSnapshot(snap=>{
    document.querySelectorAll(".groupItem").forEach(el=>el.remove());

    let groups = [];
    snap.forEach(doc=>{ groups.push({ id: doc.id, ...doc.data() }); });
    groups.sort((a,b)=>(b.lastTime||0)-(a.lastTime||0));

    groups.forEach(g=>{
      let div = document.createElement("div");
      div.className = "groupItem";
      div.innerHTML = `
        <div class="chatAvatar">
          ${g.photo ? `<img src="${g.photo}">` : `👥`}
        </div>
        <div class="chatNameWrap">
          <div>${g.name}</div>
          <div style="font-size:12px;opacity:0.6;margin-top:2px;">
            ${g.lastMessage || "No messages yet"}
          </div>
        </div>
      `;
      div.onclick = ()=>{ openGroup(g.id); };
      chatList.prepend(div);
    });
  }, err=>{ showPopup(err.message); });
};

/* ===== OPEN PRIVATE GROUP ===== */

window.openGroup = function(groupId){
  if(!window.currentUser) return;

  // Unsubscribe previous listeners
  if(window.unsubscribeGroupMessages){ window.unsubscribeGroupMessages(); window.unsubscribeGroupMessages = null; }
  if(window.unsubscribeMessages){ window.unsubscribeMessages(); window.unsubscribeMessages = null; }
  if(unsubscribePrivateGroupTyping){ unsubscribePrivateGroupTyping(); unsubscribePrivateGroupTyping = null; }

  window.currentChatUser = null;

  db.collection("groups").doc(groupId).get().then(doc=>{
    let g = { id: doc.id, ...doc.data() };
    window.currentGroup = g;

    show("chat");

    // Show call buttons
    let callBar = document.getElementById("chatCallBar");
    if(callBar) callBar.style.display = "flex";

    chatName.onclick = function(){ openGroupInfo(g); };
    chatName.innerHTML = `👥 ${g.name}`;

    // Messages listener
    window.unsubscribeGroupMessages = db.collection("groupMessages")
    .where("groupId","==", groupId)
    .orderBy("time","asc")
    .onSnapshot(async snap=>{
      messages.innerHTML = "";

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
        let msgId = m.id;
        let wrap = document.createElement("div");
        wrap.className = "msgWrap " + (isMine ? "me" : "them") + " msgAnim";

        let avatar = document.createElement("div");
        avatar.className = "msgAvatar";
        if(u.photo){ avatar.innerHTML = `<img src="${u.photo}">`; }

        let bubble = document.createElement("div");
        bubble.className = "msg";
        bubble.dataset.id = msgId;

        let roleBadge = "";
        if(m.from === g.owner){
          roleBadge = `<span class="inlineBadge ownerInline">Owner</span>`;
        } else if((g.admins||[]).includes(m.from)){
          roleBadge = `<span class="inlineBadge adminInline">Admin</span>`;
        }

        // Reply preview
        let replyHtml = "";
        if(m.replyTo){
          replyHtml = `<div class="replyPreview"><span class="replyBar"></span><span class="replyText">${m.replyTo.text||"\uD83D\uDCCE Media"}</span></div>`;
        }

        // Reactions
        let reactionsHtml = "";
        if(m.reactions && Object.keys(m.reactions).length>0){
          let counts={};
          Object.values(m.reactions).forEach(e=>{ counts[e]=(counts[e]||0)+1; });
          reactionsHtml = `<div class="msgReactions">`+Object.entries(counts).map(([e,c])=>`<span class="reactionBubble">${e}${c>1?" "+c:""}</span>`).join("")+`</div>`;
        }

        // Content
        let contentHtml = "";
        if(m.type==="image"){
          contentHtml = `<img src="${m.url}" class="msgImage" onclick="viewFullImage('${m.url}')">`;
        } else if(m.type==="video"){
          contentHtml = `<video src="${m.url}" class="msgVideo" controls playsinline></video>`;
        } else if(m.type==="voice"){
          contentHtml = `<div class="voiceNoteWrap"><audio src="${m.url}" controls class="voiceAudio"></audio><div class="voiceLabel">🎙️ Voice Note</div></div>`;
        } else {
          contentHtml = `<div class="msgText">${m.deleted?"<em>This message was deleted</em>":m.text}</div>`;
        }

        bubble.innerHTML = `
          ${!isMine ? `<div class="msgSenderName">${u.displayName||u.username||"Unknown"} ${roleBadge}</div>` : ""}
          ${replyHtml}
          ${contentHtml}
          ${reactionsHtml}
          <div class="msgMeta">${formatKikTime(m.time)}</div>
        `;

        // Long-press action sheet
        let pressTimer;
        bubble.addEventListener("touchstart",()=>{ pressTimer=setTimeout(()=>{ if(window.showMsgActions) showMsgActions(msgId,m,isMine,u.displayName||u.username,u.photo); },500); });
        bubble.addEventListener("touchend",()=>clearTimeout(pressTimer));
        bubble.addEventListener("touchmove",()=>clearTimeout(pressTimer));
        bubble.addEventListener("contextmenu",(e)=>{ e.preventDefault(); if(window.showMsgActions) showMsgActions(msgId,m,isMine,u.displayName||u.username,u.photo); });

        if(isMine){
          wrap.appendChild(bubble);
          wrap.appendChild(avatar);
        } else {
          wrap.appendChild(avatar);
          wrap.appendChild(bubble);
        }
        messages.appendChild(wrap);
      });

      messages.scrollTop = messages.scrollHeight;
    }, err=>{ showPopup(err.message); });

    // Typing indicator
    unsubscribePrivateGroupTyping = db.collection("groupTyping")
    .where("groupId","==", groupId)
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

  }).catch(err=>{ showPopup(err.message); });
};

/* ===== SEND GROUP MESSAGE ===== */

window.sendGroupMessage = async function(){
  if(!window.currentGroup) return;
  if(!msgInput.value.trim()) return;

  let text = msgInput.value.trim();
  msgInput.value = "";
  if(window.sendBtn) sendBtn.classList.remove("active");

  // Clear typing
  clearGroupTyping();

  // Haptic
  if(navigator.vibrate) navigator.vibrate(20);

  let msgData = {
    groupId: window.currentGroup.id || window.currentGroup,
    from: window.currentUser.uid,
    text: text,
    time: Date.now()
  };
  if(window._replyTo) msgData.replyTo = window._replyTo;
  if(window.cancelReply) window.cancelReply();

  try{
    await db.collection("groupMessages").add(msgData);

    let gId = window.currentGroup.id || window.currentGroup;
    await db.collection("groups").doc(gId).update({
      lastMessage: text,
      lastTime: Date.now()
    });
  }catch(err){
    showPopup(err.message);
  }
};

/* ===== HANDLE SEND — ROUTES TO CORRECT SEND FUNCTION ===== */

window.handleSend = function(){
  if(!window.currentGroup){
    sendMessage();
    return;
  }
  // Public group has an id property (object), private group is a string ID
  if(typeof window.currentGroup === "object" && window.currentGroup.tag){
    sendPublicGroupMessage();
  } else {
    sendGroupMessage();
  }
};

/* ===== GROUP TYPING INDICATOR ===== */

function setGroupTyping(){
  let gId = typeof window.currentGroup === "object"
    ? window.currentGroup.id
    : window.currentGroup;
  if(!gId) return;
  let myName = window.myData?.displayName || window.myData?.username || "Someone";
  let col = (typeof window.currentGroup === "object" && window.currentGroup.tag)
    ? "publicGroupTyping"
    : "groupTyping";
  db.collection(col).doc(window.currentUser.uid + "_" + gId).set({
    groupId: gId,
    uid: window.currentUser.uid,
    name: myName,
    typing: true,
    ts: Date.now()
  });
}

function clearGroupTyping(){
  let gId = typeof window.currentGroup === "object"
    ? window.currentGroup.id
    : window.currentGroup;
  if(!gId) return;
  let col = (typeof window.currentGroup === "object" && window.currentGroup.tag)
    ? "publicGroupTyping"
    : "groupTyping";
  db.collection(col).doc(window.currentUser.uid + "_" + gId).set({
    groupId: gId,
    uid: window.currentUser.uid,
    typing: false,
    ts: Date.now()
  });
}

/* ===== ADD TO GROUP ===== */

window.addToGroup = async function(groupId, uid){
  try{
    let ref = db.collection("groups").doc(groupId);
    let doc = await ref.get();
    let data = doc.data() || {};
    let members = data.members || [];

    if(members.includes(uid)){
      showPopup("Already in group");
      return;
    }

    members.push(uid);
    await ref.update({ members: members });
    showPopup("User added!");
  }catch(err){
    showPopup(err.message);
  }
};

/* ===== LEAVE GROUP ===== */

window.leaveCurrentGroup = async function(){
  if(!window.currentGroup) return;

  let gId = typeof window.currentGroup === "object"
    ? window.currentGroup.id
    : window.currentGroup;

  let col = (typeof window.currentGroup === "object" && window.currentGroup.tag)
    ? "publicGroups"
    : "groups";

  try{
    let ref = db.collection(col).doc(gId);
    let doc = await ref.get();
    let data = doc.data() || {};
    let members = (data.members || []).filter(m => m !== window.currentUser.uid);

    await ref.update({ members: members });
    window.currentGroup = null;
    showPopup("You left the group.");
    show("home");
  }catch(err){
    showPopup(err.message);
  }
};

/* ===== OPEN ADD PEOPLE MODAL ===== */

window.openAddPeople = async function(){
  if(!window.currentGroup) return;

  let gId = typeof window.currentGroup === "object"
    ? window.currentGroup.id
    : window.currentGroup;

  let col = (typeof window.currentGroup === "object" && window.currentGroup.tag)
    ? "publicGroups"
    : "groups";

  // Get current group members
  let groupDoc = await db.collection(col).doc(gId).get();
  let groupData = groupDoc.data() || {};
  let currentMembers = groupData.members || [];

  // Get all DM contacts (people the user has chatted with)
  let messagesSnap = await db.collection("messages")
  .where("from","==", window.currentUser.uid)
  .get();

  let contactUids = new Set();
  messagesSnap.forEach(doc=>{
    let d = doc.data();
    if(d.to && d.to !== window.currentUser.uid) contactUids.add(d.to);
  });

  let messagesSnap2 = await db.collection("messages")
  .where("to","==", window.currentUser.uid)
  .get();
  messagesSnap2.forEach(doc=>{
    let d = doc.data();
    if(d.from && d.from !== window.currentUser.uid) contactUids.add(d.from);
  });

  // Filter out already-in-group
  let toAdd = [...contactUids].filter(uid => !currentMembers.includes(uid));

  if(toAdd.length === 0){
    showPopup("No contacts to add. Start a DM with someone first.");
    return;
  }

  // Fetch user data for contacts
  let contactData = [];
  for(const uid of toAdd){
    let uDoc = await db.collection("users").doc(uid).get();
    let u = uDoc.data() || {};
    contactData.push({ uid, ...u });
  }

  // Build modal
  let modal = document.getElementById("addPeopleModal");
  if(!modal){
    modal = document.createElement("div");
    modal.id = "addPeopleModal";
    modal.className = "addPeopleModal";
    document.body.appendChild(modal);
  }

  modal.innerHTML = `
    <div class="addPeopleBox">
      <div class="addPeopleTitle">Add People</div>
      <div class="addPeopleList" id="addPeopleList"></div>
      <button class="addPeopleClose" onclick="document.getElementById('addPeopleModal').style.display='none'">Close</button>
    </div>
  `;

  let list = modal.querySelector("#addPeopleList");
  contactData.forEach(u=>{
    let row = document.createElement("div");
    row.className = "addPeopleRow";
    row.innerHTML = `
      <div class="chatAvatar" style="width:36px;height:36px;">
        ${u.photo ? `<img src="${u.photo}">` : "👤"}
      </div>
      <div style="flex:1;">${u.displayName||u.username||"Unknown"}</div>
      <button class="addPeopleBtn" onclick="addPersonToCurrentGroup('${u.uid}', this)">Add</button>
    `;
    list.appendChild(row);
  });

  modal.style.display = "flex";
};

window.addPersonToCurrentGroup = async function(uid, btn){
  if(!window.currentGroup) return;

  let gId = typeof window.currentGroup === "object"
    ? window.currentGroup.id
    : window.currentGroup;

  let col = (typeof window.currentGroup === "object" && window.currentGroup.tag)
    ? "publicGroups"
    : "groups";

  try{
    let ref = db.collection(col).doc(gId);
    let doc = await ref.get();
    let data = doc.data() || {};
    let members = data.members || [];

    if(members.includes(uid)){
      btn.textContent = "Already in";
      return;
    }

    members.push(uid);
    await ref.update({ members: members });
    btn.textContent = "Added ✓";
    btn.disabled = true;
  }catch(err){
    showPopup(err.message);
  }
};

/* ===== PROMOTE TO ADMIN ===== */

window.promoteToAdmin = async function(uid){
  if(!window.currentGroup) return;

  let gId = typeof window.currentGroup === "object"
    ? window.currentGroup.id
    : window.currentGroup;

  let col = (typeof window.currentGroup === "object" && window.currentGroup.tag)
    ? "publicGroups"
    : "groups";

  let isOwner = (typeof window.currentGroup === "object")
    ? window.currentGroup.owner === window.currentUser.uid
    : false;

  if(!isOwner){
    showPopup("Only the owner can promote admins.");
    return;
  }

  try{
    let ref = db.collection(col).doc(gId);
    let doc = await ref.get();
    let data = doc.data() || {};
    let admins = data.admins || [];

    if(admins.includes(uid)){
      showPopup("Already an admin.");
      return;
    }

    admins.push(uid);
    await ref.update({ admins: admins });
    showPopup("User promoted to admin!");
    // Refresh group info
    let freshDoc = await ref.get();
    window.currentGroup = { id: gId, ...freshDoc.data() };
    renderGroupMembers();
  }catch(err){
    showPopup(err.message);
  }
};

/* ===== REMOVE ADMIN ===== */

window.removeAdmin = async function(uid){
  if(!window.currentGroup) return;

  let gId = typeof window.currentGroup === "object"
    ? window.currentGroup.id
    : window.currentGroup;

  let col = (typeof window.currentGroup === "object" && window.currentGroup.tag)
    ? "publicGroups"
    : "groups";

  let isOwner = (typeof window.currentGroup === "object")
    ? window.currentGroup.owner === window.currentUser.uid
    : false;

  if(!isOwner){
    showPopup("Only the owner can remove admins.");
    return;
  }

  try{
    let ref = db.collection(col).doc(gId);
    let doc = await ref.get();
    let data = doc.data() || {};
    let admins = (data.admins || []).filter(a => a !== uid);
    await ref.update({ admins: admins });
    showPopup("Admin removed.");
    let freshDoc = await ref.get();
    window.currentGroup = { id: gId, ...freshDoc.data() };
    renderGroupMembers();
  }catch(err){
    showPopup(err.message);
  }
};

/* ===== INIT TYPING HOOK ===== */

setTimeout(()=>{
  let inp = document.getElementById("msgInput");
  if(inp){
    inp.addEventListener("input", function(){
      if(window.currentGroup){
        setGroupTyping();
        clearTimeout(groupTypingTimeout);
        groupTypingTimeout = setTimeout(clearGroupTyping, 3000);
      }
    });
  }

  if(window.currentUser){
    loadGroups();
  }
}, 1500);
