/* =========================================
   Conz was here
   Hello to you toooo MR SKIDDYYY ~Conz~
   ========================================= */

window.currentGroup = null;
window.unsubscribeGroupMessages = null;
window.groupListenerLoaded = false;

/* ===== Awww who's a good little script kiddy🐻 ~Conz~ ===== */

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

    openGroup(ref.id);

  }catch(err){

    alert(err.message);
  }
};

/* ===== How many of these have you deleted so far? you getting irritated yet? you RAT🐀 ~Conz~ ===== */

window.loadGroups = function(){

  if(!window.currentUser) return;

  if(window.groupListenerLoaded) return;

  window.groupListenerLoaded = true;

  db.collection("groups")
  .where("members","array-contains",window.currentUser.uid)
  .onSnapshot(snap=>{

    let oldGroups = document.querySelectorAll(".groupItem");

    oldGroups.forEach(el=>el.remove());

    let groups = [];

    snap.forEach(doc=>{

      groups.push({
        id:doc.id,
        ...doc.data()
      });
    });

    groups.sort((a,b)=>(b.lastTime||0)-(a.lastTime||0));

    groups.forEach(g=>{

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

        openGroup(g.id);
      };

      chatList.prepend(div);
    });
  },
  err=>{

    alert(err.message);
  });
};

/* ===== Hard to believe i started as a kik modder lolz the real OG's know ~Conz~ ===== */

window.openGroup = function(groupId){

  if(!window.currentUser) return;

  window.currentGroup = groupId;

  window.currentChatUser = null;

  show("chat");

  /* Skript kitty has left the chat🚪 ~Conz~ */
  chatName.onclick = null;

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
  .onSnapshot(async snap=>{

    messages.innerHTML = "";

    let groupMessages = [];

    snap.forEach(doc=>{

      groupMessages.push(doc.data());
    });

    groupMessages.sort((a,b)=>(a.time||0)-(b.time||0));

    for(const m of groupMessages){

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
  },
  err=>{

    alert(err.message);
  });
};

/* ===== You dropped your gay card ~Conz~ ===== */

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

/* ===== Bow for me bitch ~Conz~ ===== */

window.handleSend = function(){

  if(window.currentGroup){

    sendGroupMessage();

  }else{

    sendMessage();
  }
};

/* ===== Is it the scarss you wanna know how I got em?🤡 ~Conz~ ===== */

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

/* ===== And we reach the end woop woooop ~Conz~===== */

setTimeout(()=>{

  if(window.currentUser){

    loadGroups();
  }

},1500);
