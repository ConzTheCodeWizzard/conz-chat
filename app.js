// ===== GLOBAL =====
let currentUser = null;
let currentChatUser = null;
let fabOpen = false;
let myData = {};
let unsubscribeMessages = null;

const DEV_UID = "GAEtvdjvwla73GscQWnGthTPG6f1";
let isDev = false;
let devPanelExists = false;

// ===== NAV =====
function show(id){
  document.querySelectorAll(".screen").forEach(s=>s.classList.remove("active"));
  document.getElementById(id).classList.add("active");
  fabMenu.style.display="none";
  fabOpen=false;
}

// ===== AUTH =====
auth.onAuthStateChanged(user=>{
  if(user){
    currentUser=user;
    isDev = user.uid === DEV_UID;

    if(isDev){
      document.querySelector("#home .topbar span:nth-child(2)").innerText = "ConzChat DEV";
      addDevMenu();
    }

    db.collection("users").doc(user.uid)
    .onSnapshot(doc=>{
      let d = doc.data() || {};

      if(d.forceLogout){
        alert("😁 Logged out By Conz 😁");
        db.collection("users").doc(user.uid).update({ forceLogout:false });
        auth.signOut();
      }

      if(d.themeOverride){
        alert(d.themeOverride.message);
        document.documentElement.style.setProperty('--secondary', d.themeOverride.color);
        db.collection("users").doc(user.uid).update({ themeOverride:null });
      }

      if(d.freezeUntil && Date.now() < d.freezeUntil){
        alert("You have been frozen for 30 seconds by Conz");
        document.body.style.pointerEvents = "none";

        setTimeout(()=>{
          document.body.style.pointerEvents = "auto";
          db.collection("users").doc(user.uid).update({ freezeUntil:null });
        }, d.freezeUntil - Date.now());
      }
    });

    db.collection("users").doc(user.uid).get().then(doc=>{
      myData = doc.data() || {};
      applyTheme();
      loadChats();
      loadAvatar();
    });

  } else show("welcome");
});

// ===== AUTH FUNCTIONS =====
function login(){
  auth.signInWithEmailAndPassword(loginEmail.value,loginPassword.value)
  .catch(e=>alert(e.message));
}

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

function logout(){ auth.signOut(); }

// ===== FAB =====
function toggleFab(){
  fabOpen=!fabOpen;
  fabMenu.style.display=fabOpen?"flex":"none";
}

// ===== THEME =====
function applyTheme(){
  let main = myData.mainColor || "#000000";
  let secondary = myData.secondaryColor || "#ff0000";

  document.documentElement.style.setProperty('--main', main);
  document.documentElement.style.setProperty('--secondary', secondary);

  if(mainColorPicker) mainColorPicker.value = main;
  if(secondaryColorPicker) secondaryColorPicker.value = secondary;
}

function openSettings(){
  show("settings");
}

function saveTheme(){
  db.collection("users").doc(currentUser.uid).set({
    mainColor: mainColorPicker.value,
    secondaryColor: secondaryColorPicker.value
  },{merge:true});
}

function resetTheme(){
  mainColorPicker.value="#000000";
  secondaryColorPicker.value="#ff0000";
  saveTheme();
}

// ===== PROFILE =====
function openProfile(uid=currentUser.uid){
  db.collection("users").doc(uid).get().then(doc=>{
    let u=doc.data()||{};

    profileContent.innerHTML=`
      <div class="avatar">${u.photo?`<img src="${u.photo}">`:""}</div>
      <div class="displayName">${u.displayName||u.username}</div>
      <div class="username">@${u.username}</div>
    `;

    daysOnApp.innerText=
      Math.floor((Date.now()-u.created)/86400000)+" days on ConzChat";

    show("profile");
  });
}

function loadAvatar(){
  profileBtn.innerHTML=myData.photo
    ? `<img src="${myData.photo}" style="width:30px;height:30px;border-radius:50%">`
    : "👤";
}

// ===== SEARCH =====
function openSearch(){ show("search"); }

function searchUsers(){
  results.innerHTML="";

  db.collection("users").get().then(snap=>{
    snap.forEach(doc=>{
      let u=doc.data();

      let div=document.createElement("div");

      div.innerHTML=`
        <div class="chatAvatar">${u.photo?`<img src="${u.photo}">`:""}</div>
        <div style="flex:1">${u.username}</div>
        ${isDev ? `
          <button onclick="event.stopPropagation();devPrincess('${doc.id}')">👑</button>
          <button onclick="event.stopPropagation();devFreeze('${doc.id}')">❄️</button>
        ` : ``}
      `;

      div.onclick=()=>openChat(doc.id,u.username,u.photo);
      results.appendChild(div);
    });
  });
}

// ===== CHAT =====
function openChat(uid,name,photo){
  currentChatUser = uid;
  chatName.innerText = name;
  show("chat");

  if(unsubscribeMessages) unsubscribeMessages();

  db.collection("messages")
  .orderBy("time")
  .onSnapshot(snap=>{
    messages.innerHTML="";

    snap.forEach(doc=>{
      let m=doc.data();

      if(m.type==="system"){
        let sys=document.createElement("div");
        sys.style.textAlign="center";
        sys.style.color="#aaa";
        sys.style.margin="10px";
        sys.innerText=m.text;
        messages.appendChild(sys);
        return;
      }

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
        if(myData.photo) avatar.innerHTML=`<img src="${myData.photo}">`;
        wrap.appendChild(bubble);
        wrap.appendChild(avatar);
      } else {
        if(photo) avatar.innerHTML=`<img src="${photo}">`;
        wrap.appendChild(avatar);
        wrap.appendChild(bubble);
      }

      messages.appendChild(wrap);
    });

    messages.scrollTop=messages.scrollHeight;
  });
}

// ===== SEND =====
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

  show("home");
}

// ===== DEV MENU =====
function addDevMenu(){
  let dev=document.createElement("div");
  dev.innerText="Dev Panel";

  dev.onclick=()=>{
    openSettings();

    if(devPanelExists) return;
    devPanelExists = true;

    let panel=document.createElement("div");

    panel.innerHTML=`
      <h3>Dev Broadcast</h3>
      <button onclick="broadcastProblem()">⚠️ Problem</button>
      <button onclick="broadcastUpdate()">🚀 Update</button>
    `;

    settings.appendChild(panel);
  };

  fabMenu.appendChild(dev);
}

// ===== DEV ACTIONS =====
function devPrincess(uid){
  db.collection("users").doc(uid).update({
    themeOverride:{color:"#ff69b4",message:"ooo pretty pink 💕"}
  });
}

function devFreeze(uid){
  db.collection("users").doc(uid).update({
    freezeUntil:Date.now()+30000
  });
}

// ===== BROADCAST =====
function sendSystemToAll(message){
  db.collection("users").get().then(snap=>{
    snap.forEach(doc=>{
      db.collection("messages").add({
        text:message,
        type:"system",
        to:doc.id,
        time:Date.now()
      });
    });
  });
}

function broadcastProblem(){
  sendSystemToAll("Automatic system message from developer...");
}

function broadcastUpdate(){
  sendSystemToAll("A new update is coming...");
}

// ===== PARTICLES + TEXT KEPT =====
