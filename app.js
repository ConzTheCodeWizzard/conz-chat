window.onerror = function(msg, url, line){
  alert("JS ERROR:\n" + msg + "\nLine: " + line);
};

// ===== GLOBAL NAV (ALWAYS AVAILABLE) =====
window.show = function(id){
  document.querySelectorAll(".screen").forEach(s=>{
    s.style.display="none";
    s.classList.remove("active");
  });

  let el = document.getElementById(id);
  if(el){
    el.style.display="flex";
    el.classList.add("active");
  }

  let fab = document.getElementById("fabMenu");
  if(fab) fab.style.display="none";
};

// ===== WAIT FOR FIREBASE =====
window.addEventListener("load", () => {

  function wait(){
    if(typeof firebase==="undefined" || typeof auth==="undefined" || typeof db==="undefined"){
      setTimeout(wait,200);
      return;
    }
    startApp();
  }

  wait();
});

function startApp(){

let currentUser=null;

/* 🔥 FIXED */
window.currentChatUser=null;

let fabOpen=false;
let myData={};
let unsubscribeMessages=null;
let unsubscribeStatus=null;

const DEV_UID="GAEtvdjvwla73GscQWnGthTPG6f1";
window.isDev=false;

auth.onAuthStateChanged(user=>{
  if(user){

    currentUser=user;

    window.isDev = user.uid===DEV_UID;

    db.collection("users").doc(user.uid)
    .onSnapshot(doc=>{

      let d = doc.data() || {};

      if(d.forceLogout){
        alert("😁Logged out By Conz ~Six Sevennn🙌~");

        db.collection("users").doc(user.uid).update({
          forceLogout:false
        });

        auth.signOut();
      }
    });

    db.collection("users").doc(user.uid).set({
      online:true,
      lastSeen:Date.now()
    },{merge:true});

    /* 🔥 FIXED LIVE USER LISTENER */
    db.collection("users").doc(user.uid)
    .onSnapshot(doc=>{

      myData=doc.data()||{};

      loadChats();

      loadAvatar();

    });

    show("home");

  }else{
    show("welcome");
  }
});

window.login=function(){

  let email=loginEmail.value;
  let pass=loginPassword.value;

  if(!email||!pass){
    alert("Missing details");
    return;
  }

  auth.signInWithEmailAndPassword(email,pass)
  .catch(e=>alert(e.message));
};

window.signup=function(){

  let username=signupUsername.value;
  let email=signupEmail.value;
  let pass=signupPassword.value;

  if(!username || !email || !pass){
    alert("Fill everything");
    return;
  }

  auth.createUserWithEmailAndPassword(email,pass)
  .then(res=>{

    return db.collection("users").doc(res.user.uid).set({
      username,
      displayName:username,
      photo:"",
      created:Date.now(),
      online:true,
      lastSeen:Date.now()
    });

  })
  .catch(e=>alert(e.message));
};

window.logout=function(){

  db.collection("users").doc(currentUser.uid).set({
    online:false,
    lastSeen:Date.now()
  },{merge:true});

  auth.signOut();
};

window.toggleFab=function(){

  fabOpen=!fabOpen;

  fabMenu.style.display=fabOpen?"flex":"none";
};

/* ===== PROFILE ===== */

window.openProfile=function(uid=currentUser.uid){

  db.collection("users").doc(uid).get().then(doc=>{

    let u=doc.data()||{};

    profileContent.innerHTML=`
      <div class="avatar" ${uid===currentUser.uid?'onclick="pickImage()"' : ''}>
        ${
          u.photo
          ? `<img src="${u.photo}">`
          : `<div style="font-size:40px;">👤</div>`
        }
      </div>

      <div class="displayName"
        ${uid===currentUser.uid?'onclick="editDisplayName()"' : ''}>
        ${u.displayName||u.username}
      </div>

      ${uid===DEV_UID
        ? `<div class="devBadge">👑 ConzChat Dev</div>`
        : ""
      }

      <div class="username">@${u.username}</div>

      ${isDev && uid!==currentUser.uid
        ? `<button onclick="devBoot('${uid}')">BOOT THIS BITCH</button>`
        : ""
      }
    `;

    if(window.daysOnApp){

      daysOnApp.innerText=
        Math.floor((Date.now()-u.created)/86400000)
        +" days on ConzChat";
    }

    show("profile");
  });
};

window.pickImage=function(){
  filePicker.click();
};

window.editDisplayName=function(){

  let newName=prompt(
    "Enter new display name",
    myData.displayName || myData.username
  );

  if(!newName || !newName.trim()) return;

  db.collection("users")
  .doc(currentUser.uid)
  .update({
    displayName:newName
  });

  myData.displayName=newName;

  openProfile(currentUser.uid);
};

filePicker.onchange=e=>{

  let f=e.target.files[0];

  if(!f) return;

  let r=new FileReader();

  r.onload=()=>{

    db.collection("users").doc(currentUser.uid).update({
      photo:r.result
    });

    myData.photo=r.result;

    loadAvatar();

    openProfile(currentUser.uid);
  };

  r.readAsDataURL(f);
};

function loadAvatar(){

  profileBtn.innerHTML=myData.photo
  ? `<img src="${myData.photo}" style="width:30px;height:30px;border-radius:50%;pointer-events:none;">`
  : "👤";
}

function devBoot(uid){

  db.collection("users").doc(uid).update({
    forceLogout:true
  });
}

window.openSearch=()=>show("search");

window.searchUsers=function(){

  results.innerHTML="";

  db.collection("users").get().then(snap=>{

    snap.forEach(doc=>{

      let u=doc.data();

      let div=document.createElement("div");

      div.innerHTML=`
        <div class="chatAvatar">
          ${u.photo?`<img src="${u.photo}">`:""}
        </div>

        <div>${u.username}</div>
      `;

      div.onclick=()=>openChat(doc.id,u.username,u.photo);

      results.appendChild(div);
    });
  });
};

function openChat(uid,name,photo){

  /* 🔥 FIXED */
  window.currentChatUser=uid;

  show("chat");

  if(unsubscribeMessages) unsubscribeMessages();

  if(unsubscribeStatus) unsubscribeStatus();

  unsubscribeStatus=db.collection("users").doc(uid)
  .onSnapshot(doc=>{

    let u=doc.data()||{};

    if(u.online){

      chatName.innerText=name+" 🟢 Online";

    }else{

      let seconds=Math.floor((Date.now()-(u.lastSeen||0))/1000);

      let text=seconds<60
        ? `${seconds}s ago`
        : seconds<3600
        ? `${Math.floor(seconds/60)}m ago`
        : `${Math.floor(seconds/3600)}h ago`;

      chatName.innerText=name+" • Last seen "+text;
    }
  });

  unsubscribeMessages=db.collection("messages")
  .orderBy("time")
  .onSnapshot(snap=>{

    messages.innerHTML="";

    snap.forEach(doc=>{

      let m=doc.data();

      if(!(m.from===currentUser.uid||m.to===currentUser.uid)) return;

      let other=m.from===currentUser.uid?m.to:m.from;

      if(other!==uid) return;

      let isMine=m.from===currentUser.uid;

      let wrap=document.createElement("div");

      wrap.className="msgWrap "+(isMine?"me":"them");

      let avatar=document.createElement("div");

      avatar.className="msgAvatar";

      let bubble=document.createElement("div");

      bubble.className="msg";

      bubble.innerHTML=`
        ${m.text}
        <div>${new Date(m.time).toLocaleTimeString()}</div>
      `;

      if(isMine){

        if(myData.photo){
          avatar.innerHTML=`<img src="${myData.photo}">`;
        }

        wrap.appendChild(bubble);
        wrap.appendChild(avatar);

      }else{

        if(photo){
          avatar.innerHTML=`<img src="${photo}">`;
        }

        wrap.appendChild(avatar);
        wrap.appendChild(bubble);
      }

      messages.appendChild(wrap);
    });

    messages.scrollTop=messages.scrollHeight;
  });
}

window.sendMessage=function(){

  if(!msgInput || !msgInput.value) return;

  db.collection("messages").add({
    text:msgInput.value,
    from:currentUser.uid,
    to:window.currentChatUser,
    time:Date.now()
  });

  msgInput.value="";

  if(window.sendBtn){
    sendBtn.classList.remove("active");
  }
};

function loadChats(){

  db.collection("messages").orderBy("time","desc")
  .onSnapshot(snap=>{

    chatList.innerHTML="";

    let seen={};

    snap.forEach(doc=>{

      let m=doc.data();

      if(m.from!==currentUser.uid&&m.to!==currentUser.uid) return;

      let other=m.from===currentUser.uid?m.to:m.from;

      if(seen[other]) return;

      seen[other]=true;

      db.collection("users").doc(other).get().then(u=>{

        let d=u.data()||{};

        let div=document.createElement("div");

        div.innerHTML=`
          <div class="chatAvatar">
            ${d.photo?`<img src="${d.photo}">`:""}
          </div>

          <div>${d.username}</div>
        `;

        div.onclick=()=>openChat(other,d.username,d.photo);

        chatList.appendChild(div);
      });
    });
  });
}

setTimeout(()=>{

  if(window.msgInput && window.sendBtn){

    msgInput.addEventListener("input", ()=>{

      if(msgInput.value.trim()){
        sendBtn.classList.add("active");
      }else{
        sendBtn.classList.remove("active");
      }
    });

    msgInput.addEventListener("keydown", function(e){

      if(e.key === "Enter" && !e.shiftKey){

        e.preventDefault();

        sendMessage();
      }
    });
  }

},500);

}
