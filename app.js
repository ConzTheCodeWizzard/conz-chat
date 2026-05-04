let currentUser=null;
let currentChatUser=null;
let myData={};

const DEV_UID="GAEtvdjvwla73GscQWnGthTPG6f1";
let isDev=false;

auth.onAuthStateChanged(user=>{
  if(user){
    currentUser=user;
    isDev=user.uid===DEV_UID;

    if(!isDev){
      let btn=document.getElementById("devBtn");
      if(btn) btn.style.display="none";
    }

    db.collection("users").doc(user.uid).onSnapshot(doc=>{
      let d=doc.data()||{};

      if(d.forceLogout){
        alert("😁YOU GOT BOOTED BY CONZ BRUH😁 ~Conz~");
        db.collection("users").doc(user.uid).update({forceLogout:false});
        auth.signOut();
      }
    });

    db.collection("users").doc(user.uid).get().then(doc=>{
      myData=doc.data()||{};
      loadChats();
    });

    show("home");
  }else{
    show("welcome");
  }
});

function login(){
  auth.signInWithEmailAndPassword(loginEmail.value,loginPassword.value);
}

function signup(){
  auth.createUserWithEmailAndPassword(signupEmail.value,signupPassword.value)
  .then(res=>{
    db.collection("users").doc(res.user.uid).set({
      username:signupUsername.value,
      created:Date.now()
    });
  });
}

function logout(){auth.signOut();}

function show(id){
  document.querySelectorAll(".screen").forEach(s=>s.classList.remove("active"));
  document.getElementById(id).classList.add("active");
}

function openProfile(uid=currentUser.uid){
  db.collection("users").doc(uid).get().then(doc=>{
    let u=doc.data()||{};
    let dev=uid===DEV_UID;

    profileContent.innerHTML=`
      <div class="avatar" onclick="pickImage()">
        ${u.photo?`<img src="${u.photo}">`:"Tap to add"}
      </div>

      <div class="displayName">${u.displayName||u.username}</div>

      ${dev?`<div class="devBadge">👑 ConzChat Dev</div>`:""}

      <div class="username">@${u.username}</div>
      <div class="days">${Math.floor((Date.now()-u.created)/86400000)} days on ConzChat</div>

      ${isDev && uid!==currentUser.uid?`<button onclick="bootUser('${uid}')">Boot User</button>`:""}
    `;

    show("profile");
  });
}

function pickImage(){
  filePicker.click();
}

filePicker.onchange=e=>{
  let file=e.target.files[0];
  let reader=new FileReader();

  reader.onload=()=>{
    db.collection("users").doc(currentUser.uid).update({
      photo:reader.result
    });
  };

  reader.readAsDataURL(file);
};

function bootUser(uid){
  db.collection("users").doc(uid).update({forceLogout:true});
}

function loadChats(){
  db.collection("users").get().then(snap=>{
    chatList.innerHTML="";
    snap.forEach(doc=>{
      if(doc.id===currentUser.uid) return;
      let u=doc.data();

      let div=document.createElement("div");
      div.innerText=u.username;
      div.onclick=()=>openChat(doc.id,u.username);

      chatList.appendChild(div);
    });
  });
}

function openChat(uid,name){
  currentChatUser=uid;
  chatName.innerText=name;
  show("chat");

  db.collection("messages").orderBy("time")
  .onSnapshot(snap=>{
    messages.innerHTML="";
    snap.forEach(doc=>{
      let m=doc.data();

      if(!(m.from===currentUser.uid||m.to===currentUser.uid)) return;
      let other=m.from===currentUser.uid?m.to:m.from;
      if(other!==uid) return;

      let div=document.createElement("div");
      div.innerText=m.text;
      messages.appendChild(div);
    });
  });
}

function sendMessage(){
  if(!msgInput.value) return;

  db.collection("messages").add({
    text:msgInput.value,
    from:currentUser.uid,
    to:currentChatUser,
    time:Date.now()
  });

  msgInput.value="";
}
