let currentUser=null;
let currentChatUser=null;
let fabOpen=false;
let myData={};

// NAV
function show(id){
  document.querySelectorAll(".screen").forEach(s=>s.classList.remove("active"));
  document.getElementById(id).classList.add("active");
  fabMenu.style.display="none";
}

// AUTH
auth.onAuthStateChanged(user=>{
  if(user){
    currentUser=user;

    db.collection("users").doc(user.uid).get().then(doc=>{
      myData=doc.data();
      loadTheme();
      loadChats();
      loadAvatar();
    });

  }else show("welcome");
});

// LOGIN
function login(){
  auth.signInWithEmailAndPassword(loginEmail.value,loginPassword.value)
  .catch(e=>alert(e.message));
}

// SIGNUP
function signup(){
  auth.createUserWithEmailAndPassword(signupEmail.value,signupPassword.value)
  .then(res=>{
    return db.collection("users").doc(res.user.uid).set({
      username:signupUsername.value,
      displayName:signupUsername.value,
      photoURL:"",
      created:Date.now(),
      mainColor:"#000000",
      secondaryColor:"#ff0000"
    });
  });
}

// LOGOUT
function logout(){ auth.signOut(); }

// FAB
function toggleFab(){
  fabOpen=!fabOpen;
  fabMenu.style.display=fabOpen?"flex":"none";
}

// THEME
function loadTheme(){
  applyTheme(myData.mainColor,myData.secondaryColor);
}

function applyTheme(m,s){
  document.documentElement.style.setProperty('--main',m);
  document.documentElement.style.setProperty('--secondary',s);
}

function saveTheme(){
  db.collection("users").doc(currentUser.uid).update({
    mainColor:mainColorPicker.value,
    secondaryColor:secondaryColorPicker.value
  });
}

function resetTheme(){
  applyTheme("#000","#f00");
}

// PROFILE
function openProfile(uid=currentUser.uid){
  db.collection("users").doc(uid).get().then(doc=>{
    let u=doc.data();

    profileContent.innerHTML=`
      <div class="avatar" onclick="${uid===currentUser.uid?'pickImage()':''}">
        ${u.photoURL?`<img src="${u.photoURL}">`:""}
      </div>
      <div class="displayName">${u.displayName}</div>
      <div class="username">@${u.username}</div>
    `;

    daysOnApp.innerText=
      Math.floor((Date.now()-u.created)/86400000)+" days on ConzChat";

    show("profile");
  });
}

// PROFILE PIC
function pickImage(){
  let i=document.createElement("input");
  i.type="file";
  i.accept="image/*";

  i.onchange=e=>{
    let file=e.target.files[0];
    let ref=storage.ref("pfp/"+currentUser.uid);

    ref.put(file).then(()=>ref.getDownloadURL()).then(url=>{
      db.collection("users").doc(currentUser.uid).update({photoURL:url});
      myData.photoURL=url;
      loadAvatar();
      openProfile();
    });
  };

  i.click();
}

function loadAvatar(){
  if(myData.photoURL){
    profileBtn.innerHTML=`<img src="${myData.photoURL}" style="width:30px;height:30px;border-radius:50%">`;
  }
}

// SEARCH
function searchUsers(){
  results.innerHTML="";
  db.collection("users").get().then(snap=>{
    snap.forEach(doc=>{
      let u=doc.data();
      let d=document.createElement("div");
      d.innerText=u.username;
      d.onclick=()=>openChat(doc.id,u.username);
      results.appendChild(d);
    });
  });
}

// CHAT
function openChat(uid,name){
  currentChatUser=uid;
  chatName.innerText=name;
  show("chat");

  db.collection("messages").orderBy("time")
  .onSnapshot(snap=>{
    messages.innerHTML="";

    snap.forEach(doc=>{
      let m=doc.data();

      if((m.from===currentUser.uid&&m.to===uid)||(m.to===currentUser.uid&&m.from===uid)){

        let wrap=document.createElement("div");
        wrap.className="msgWrap "+(m.from===currentUser.uid?"me":"them");

        let avatar=document.createElement("div");
        avatar.className="msgAvatar";

        if(m.from===currentUser.uid && myData.photoURL){
          avatar.innerHTML=`<img src="${myData.photoURL}">`;
        }

        let bubble=document.createElement("div");
        bubble.className="msg";
        bubble.innerHTML=m.text;

        if(m.from===currentUser.uid){
          wrap.appendChild(bubble);
          wrap.appendChild(avatar);
        }else{
          wrap.appendChild(avatar);
          wrap.appendChild(bubble);
        }

        messages.appendChild(wrap);
      }
    });

    messages.scrollTop=messages.scrollHeight;
  });
}

function sendMessage(){
  if(!msgInput.value)return;

  db.collection("messages").add({
    text:msgInput.value,
    from:currentUser.uid,
    to:currentChatUser,
    time:Date.now()
  });

  msgInput.value="";
}

// CHAT LIST
function loadChats(){
  db.collection("messages").orderBy("time","desc")
  .onSnapshot(snap=>{
    chatList.innerHTML="";
    let done={};

    snap.forEach(doc=>{
      let m=doc.data();
      let other=m.from===currentUser.uid?m.to:m.from;

      if(done[other])return;
      done[other]=true;

      db.collection("users").doc(other).get().then(u=>{
        let d=document.createElement("div");

        let avatarHTML=u.data().photoURL
          ? `<div class="chatAvatar"><img src="${u.data().photoURL}"></div>`
          : `<div class="chatAvatar"></div>`;

        d.innerHTML=avatarHTML+u.data().username;
        d.onclick=()=>openChat(other,u.data().username);

        chatList.appendChild(d);
      });
    });
  });

  show("home");
}
