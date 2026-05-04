window.onerror = function(msg, url, line){
  alert("JS ERROR:\n" + msg + "\nLine: " + line);
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

// ===== GLOBAL =====
let currentUser=null;
let currentChatUser=null;
let fabOpen=false;
let myData={};
let unsubscribeMessages=null;
let unsubscribeStatus=null;

const DEV_UID="GAEtvdjvwla73GscQWnGthTPG6f1";
let isDev=false;

// ===== NAV =====
window.show=function(id){
  document.querySelectorAll(".screen").forEach(s=>{
    s.style.display="none";
    s.classList.remove("active");
  });

  let el=document.getElementById(id);
  if(el){
    el.style.display="flex";
    el.classList.add("active");
  }

  let fab=document.getElementById("fabMenu");
  if(fab) fab.style.display="none";

  fabOpen=false;
};

// ===== AUTH =====
auth.onAuthStateChanged(user=>{
  if(user){
    currentUser=user;
    isDev = user.uid===DEV_UID;

    db.collection("users").doc(user.uid).set({
      online:true,
      lastSeen:Date.now()
    },{merge:true});

    db.collection("users").doc(user.uid).get().then(doc=>{
      myData=doc.data()||{};
      applyTheme();
      loadChats();
      loadAvatar();
    });

    document.getElementById("devBtn").style.display=isDev?"block":"none";

    show("home");

  }else{
    show("welcome");
  }
});

// ===== LOGIN =====
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

// ===== SIGNUP =====
window.signup=function(){
  let username=signupUsername.value;
  let email=signupEmail.value;
  let pass=signupPassword.value;

  auth.createUserWithEmailAndPassword(email,pass)
  .then(res=>{
    return db.collection("users").doc(res.user.uid).set({
      username,
      displayName:username,
      photo:"",
      created:Date.now(),
      mainColor:"#000",
      secondaryColor:"#ff0033",
      online:true,
      lastSeen:Date.now()
    });
  });
};

window.logout=function(){
  db.collection("users").doc(currentUser.uid).set({
    online:false,
    lastSeen:Date.now()
  },{merge:true});

  auth.signOut();
};

// ===== FAB =====
window.toggleFab=function(){
  fabOpen=!fabOpen;
  fabMenu.style.display=fabOpen?"flex":"none";
};

// ===== THEME =====
function applyTheme(){
  document.documentElement.style.setProperty('--main',myData.mainColor||"#000");
  document.documentElement.style.setProperty('--secondary',myData.secondaryColor||"#ff0033");
}

// ===== PROFILE =====
window.openProfile=function(uid=currentUser.uid){
  db.collection("users").doc(uid).get().then(doc=>{
    let u=doc.data()||{};

    profileContent.innerHTML=`
      <div class="avatar" ${uid===currentUser.uid?'onclick="pickImage()"':''}>
        ${u.photo?`<img src="${u.photo}">`:""}
      </div>
      <div class="displayName">${u.displayName||u.username}</div>
      <div>@${u.username}</div>
    `;

    daysOnApp.innerText=Math.floor((Date.now()-u.created)/86400000)+" days";

    show("profile");
  });
};

window.pickImage=function(){
  filePicker.click();
};

filePicker.onchange=e=>{
  let f=e.target.files[0];
  if(!f) return;

  let r=new FileReader();
  r.onload=()=>{
    db.collection("users").doc(currentUser.uid).update({photo:r.result});
    myData.photo=r.result;
    loadAvatar();
  };
  r.readAsDataURL(f);
};

function loadAvatar(){
  profileBtn.innerHTML=myData.photo
  ? `<img src="${myData.photo}" style="width:30px;height:30px;border-radius:50%">`
  : "👤";
}

// ===== SEARCH =====
window.openSearch=()=>show("search");

window.searchUsers=function(){
  results.innerHTML="";
  db.collection("users").get().then(snap=>{
    snap.forEach(doc=>{
      let u=doc.data();

      let div=document.createElement("div");
      div.innerHTML=`
        <div class="chatAvatar">${u.photo?`<img src="${u.photo}">`:""}</div>
        <div>${u.username}</div>
      `;

      div.onclick=()=>openChat(doc.id,u.username,u.photo);
      results.appendChild(div);
    });
  });
};

// ===== CHAT =====
function openChat(uid,name,photo){
  currentChatUser=uid;
  show("chat");

  if(unsubscribeMessages) unsubscribeMessages();
  if(unsubscribeStatus) unsubscribeStatus();

  unsubscribeStatus=db.collection("users").doc(uid)
  .onSnapshot(doc=>{
    let u=doc.data()||{};
    chatName.innerText=u.online
    ? name+" 🟢"
    : name+" • last seen";
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

      let bubble=document.createElement("div");
      bubble.className="msg";
      bubble.innerHTML=`
        ${m.text}
        <div>${new Date(m.time).toLocaleTimeString()}</div>
      `;

      wrap.appendChild(bubble);
      messages.appendChild(wrap);
    });

    messages.scrollTop=messages.scrollHeight;
  });
}

// ===== SEND =====
window.sendMessage=function(){
  if(!msgInput.value) return;

  db.collection("messages").add({
    text:msgInput.value,
    from:currentUser.uid,
    to:currentChatUser,
    time:Date.now()
  });

  msgInput.value="";
};

// ===== CHAT LIST =====
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
          <div class="chatAvatar">${d.photo?`<img src="${d.photo}">`:""}</div>
          <div>${d.username}</div>
        `;

        div.onclick=()=>openChat(other,d.username,d.photo);
        chatList.appendChild(div);
      });
    });
  });
}

// ===== DEV =====
window.devOpen=function(){
  if(!isDev) return;
  alert("Dev panel coming back");
};

           }
