/* =========================================
   CONZCHAT GROUP SYSTEM
   group.js
   ========================================= */

window.currentGroup = null;
window.unsubscribeGroupMessages = null;

/* ===== CREATE GROUP ===== */

window.createGroup = async function(){

  let name = prompt("Enter group name");

  if(!name || !name.trim()) return;

  try{

    let ref = await db.collection("groups").add({

      name:name,

      owner:window.currentUser.uid,

      members:[window.currentUser.uid],

      created:Date.now(),

      photo:"",

      lastMessage:"",

      lastTime:Date.now()
    });

    alert("Group created!");

    loadGroups();

    openGroup(ref.id);

  }catch(err){

    alert(err.message);
  }
};

/* ===== LOAD GROUPS ===== */

window.loadGroups = function(){

  if(!window.currentUser) return;

  db.collection("groups")
  .where("members","array-contains",window.currentUser.uid)
  .orderBy("lastTime","desc")
  .onSnapshot(snap=>{

    let oldGroups = document.querySelectorAll(".groupItem");

    oldGroups.forEach(el=>el.remove());

    snap.forEach(doc=>{

      let g = doc.data();

      let div = document.createElement("div");

      div.className = "groupItem";

      div.innerHTML = `
        <div class="chatAvatar">
          ${
            g.photo
            ? `<img src="${g.photo}">`
            : `👥`
          }
        </div>

        <div>
          <div>${g.name}</div>

          <div style="
            font-size:12px;
            opacity:0.6;
            margin-top:2px;
          ">
            ${g.lastMessage || "No messages yet"}
          </div>
        </div>
      `;

      div.onclick = ()=>{

        openGroup(doc.id);
      };

      chatList.prepend(div);
    });
  });
};

/* ===== OPEN GROUP ===== */

window.openGroup = function(groupId){

  if(!window.currentUser) return;

  window.currentGroup = groupId;

  window.currentChatUser = null;

  show("chat");

  if(window.unsubscribeGroupMessages){
    window.unsubscribeGroupMessages();
  }

  if(window.unsubscribeMessages){
    window.unsubscribeMessages();
  }

  db.collection("groups")
  .doc(groupId)
  .get()
  .then(doc=>{

    let g = doc.data() || {};

    chatName.innerHTML = `
      👥 ${g.name}
    `;
  });

  window.unsubscribeGroupMessages = db.collection("groupMessages")
  .where("groupId","==",groupId)
  .orderBy("time")
  .onSnapshot(async snap=>{

    messages.innerHTML = "";

    for(const doc of snap.docs){

      let m = doc.data();

      let userDoc = await db.collection("users")
      .doc(m.from)
      .get();

      let u = userDoc.data() || {};

      let isMine = m.from===window.currentUser.uid;

      let wrap = document.createElement("div");

      wrap.className =
        "msgWrap " + (isMine ? "me" : "them");

      let avatar = document.createElement("div");

      avatar.className = "msgAvatar";

      if(u.photo){

        avatar.innerHTML = `
          <img src="${u.photo}">
        `;
      }

      let bubble = document.createElement("div");

      bubble.className = "msg";

      bubble.innerHTML = `
        ${
          !isMine
          ? `
          <div style="
            font-size:12px;
            opacity:0.7;
            margin-bottom:4px;
          ">
            ${u.displayName || u.username}
          </div>
          `
          : ""
        }

        ${m.text}

        <div style="
          font-size:11px;
          opacity:0.5;
          margin-top:4px;
        ">
          ${new Date(m.time).toLocaleTimeString()}
        </div>
      `;

      if(isMine){

        wrap.appendChild(bubble);
        wrap.appendChild(avatar);

      }else{

        wrap.appendChild(avatar);
        wrap.appendChild(bubble);
      }

      messages.appendChild(wrap);
    }

    messages.scrollTop = messages.scrollHeight;
  });
};

/* ===== SEND GROUP MESSAGE ===== */

window.sendGroupMessage = async function(){

  if(!window.currentGroup) return;

  if(!msgInput.value.trim()) return;

  let text = msgInput.value;

  msgInput.value = "";

  if(window.sendBtn){
    sendBtn.classList.remove("active");
  }

  try{

    await db.collection("groupMessages").add({

      groupId:window.currentGroup,

      from:window.currentUser.uid,

      text:text,

      time:Date.now()
    });

    await db.collection("groups")
    .doc(window.currentGroup)
    .update({

      lastMessage:text,

      lastTime:Date.now()
    });

  }catch(err){

    alert(err.message);
  }
};

/* ===== GROUP / DM SEND SWITCH ===== */

window.handleSend = function(){

  if(window.currentGroup){

    sendGroupMessage();

  }else{

    sendMessage();
  }
};

/* ===== ADD MEMBER ===== */

window.addToGroup = async function(groupId,uid){

  try{

    let ref = db.collection("groups").doc(groupId);

    let doc = await ref.get();

    let data = doc.data() || {};

    let members = data.members || [];

    if(members.includes(uid)){

      alert("Already in group");

      return;
    }

    members.push(uid);

    await ref.update({
      members:members
    });

    alert("User added!");

  }catch(err){

    alert(err.message);
  }
};

/* ===== AUTO LOAD GROUPS ===== */

setTimeout(()=>{

  if(window.currentUser){

    loadGroups();
  }

},1500);
