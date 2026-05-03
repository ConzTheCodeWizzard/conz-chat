let currentUser = null;
let currentChatUser = null;
let fabOpen = false;
let myData = {};
let unsubscribeMessages = null;

// NAV
function show(id){
  document.querySelectorAll(".screen").forEach(s=>s.classList.remove("active"));
  document.getElementById(id).classList.add("active");
  fabMenu.style.display="none";
  fabOpen=false;
}

// AUTH
auth.onAuthStateChanged(user=>{
  if(user){
    currentUser=user;

    db.collection("users").doc(user.uid).get().then(doc=>{
      myData=doc.data()||{};
      loadTheme();
      loadChats();
      loadAvatar();
    });

  } else show("welcome");
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
      photo:"",
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
  let m=myData.mainColor||"#000";
  let s=myData.secondaryColor||"#ff0000";

  document.documentElement.style.setProperty('--main',m);
  document.documentElement.style.setProperty('--secondary',s);

  if(mainColorPicker) mainColorPicker.value=m;
  if(secondaryColorPicker) secondaryColorPicker.value=s;
}

function saveTheme(){
  db.collection("users").doc(currentUser.uid).update({
    mainColor:mainColorPicker.value,
    secondaryColor:secondaryColorPicker.value
  }).then(loadTheme);
}

function resetTheme(){
  document.documentElement.style.setProperty('--main',"#000");
  document.documentElement.style.setProperty('--secondary',"#ff0000");
}

// PROFILE
function openProfile(uid=currentUser.uid){
  db.collection("users").doc(uid).get().then(doc=>{
    let u=doc.data()||{};

    profileContent.innerHTML=`
      <div class="avatar" ${uid===currentUser.uid?'onclick="pickImage()"':''}>
        ${u.photo?`<img src="${u.photo}">`:""}
      </div>
      <div class="displayName">${u.displayName||u.username}</div>
      <div class="username">@${u.username}</div>
    `;

    daysOnApp.innerText=
      Math.floor((Date.now()-u.created)/86400000)+" days on ConzChat";

    show("profile");
  });
}

function saveDisplayName(){
  db.collection("users").doc(currentUser.uid).update({
    displayName:editName.value
  }).then(()=>openProfile());
}

// IMAGE
function pickImage(){
  let input=document.createElement("input");
  input.type="file";
  input.accept="image/*";

  input.onchange=e=>{
    let file=e.target.files[0];
    let reader=new FileReader();

    reader.onload=ev=>{
      db.collection("users").doc(currentUser.uid).update({
        photo:ev.target.result
      }).then(()=>{
        myData.photo=ev.target.result;
        loadAvatar();
        openProfile();
      });
    };

    reader.readAsDataURL(file);
  };

  input.click();
}

function loadAvatar(){
  profileBtn.innerHTML=myData.photo
    ? `<img src="${myData.photo}" style="width:30px;height:30px;border-radius:50%">`
    : "👤";
}

// SEARCH
function openSearch(){ show("search"); }
function openSettings(){ show("settings"); }

function searchUsers(){
  results.innerHTML="";

  db.collection("users").get().then(snap=>{
    snap.forEach(doc=>{
      let u=doc.data();

      let div=document.createElement("div");

      div.innerHTML=`
        <div class="chatAvatar">${u.photo?`<img src="${u.photo}">`:""}</div>
        <div>${u.username}</div>
      `;

      div.onclick=()=>openChat(doc.id,u.username);

      results.appendChild(div);
    });
  });
}

// CHAT (FIXED)
function openChat(uid,name){
  currentChatUser = uid;
  chatName.innerText = name;
  show("chat");

  if(unsubscribeMessages) unsubscribeMessages();

  db.collection("users").doc(uid).get().then(userDoc=>{
    let otherUser = userDoc.data() || {};

    unsubscribeMessages = db.collection("messages")
    .orderBy("time")
    .onSnapshot(snap=>{

      let fragment = document.createDocumentFragment();

      snap.forEach(doc=>{
        let m = doc.data();

        if(!(m.from===currentUser.uid || m.to===currentUser.uid)) return;

        let other = m.from===currentUser.uid ? m.to : m.from;
        if(other !== uid) return;

        let isMine = m.from === currentUser.uid;

        let wrap=document.createElement("div");
        wrap.className="msgWrap "+(isMine?"me":"them");

        let avatar=document.createElement("div");
        avatar.className="msgAvatar";

        let bubble=document.createElement("div");
        bubble.className="msg";

        bubble.innerHTML=`
          ${m.text}
          <div style="font-size:10px;opacity:0.6">
            ${new Date(m.time).toLocaleTimeString()}
          </div>
        `;

        if(isMine){
          if(myData.photo){
            avatar.innerHTML=`<img src="${myData.photo}">`;
          }
          avatar.onclick=()=>openProfile(currentUser.uid);

          wrap.appendChild(bubble);
          wrap.appendChild(avatar);
        } else {
          if(otherUser.photo){
            avatar.innerHTML=`<img src="${otherUser.photo}">`;
          }
          avatar.onclick=()=>openProfile(uid);

          wrap.appendChild(avatar);
          wrap.appendChild(bubble);
        }

        fragment.appendChild(wrap);
      });

      messages.innerHTML = "";
      messages.appendChild(fragment);
      messages.scrollTop = messages.scrollHeight;
    });
  });
}

// SEND
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

// CHAT LIST
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

        div.onclick=()=>openChat(other,d.username);

        chatList.appendChild(div);
      });
    });

  });

  show("home");
}
