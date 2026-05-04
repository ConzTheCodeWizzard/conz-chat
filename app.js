// ===== GLOBAL =====
let currentUser = null;
let currentChatUser = null;
let fabOpen = false;
let myData = {};
let unsubscribeMessages = null;
let unsubscribeStatus = null;

const DEV_UID = "GAEtvdjvwla73GscQWnGthTPG6f1";
let isDev = false;

// ===== NAV =====
function show(id){
  document.querySelectorAll(".screen").forEach(s=>s.classList.remove("active"));
  document.getElementById(id).classList.add("active");

  if(typeof fabMenu !== "undefined"){
    fabMenu.style.display="none";
  }

  fabOpen=false;
}

// ===== AUTH =====
auth.onAuthStateChanged(user=>{
  if(user){
    currentUser=user;
    isDev = user.uid === DEV_UID;

    const topTitle = document.getElementById("topTitle");

    // ✅ FIX: force correct title BOTH ways
    if(topTitle){
      topTitle.innerText = isDev ? "ConzChat DEV" : "ConzChat";
    }

    // ✅ FIX: dev button visibility
    const devBtn = document.getElementById("devBtn");
    if(devBtn){
      devBtn.style.display = isDev ? "block" : "none";
    }

    // ONLINE STATUS
    db.collection("users").doc(user.uid).set({
      online:true,
      lastSeen:Date.now()
    },{merge:true});

    window.addEventListener("beforeunload", ()=>{
      db.collection("users").doc(user.uid).set({
        online:false,
        lastSeen:Date.now()
      },{merge:true});
    });

    // LISTENER (BOOT)
    db.collection("users").doc(user.uid)
    .onSnapshot(doc=>{
      let d = doc.data() || {};

      if(d.forceLogout){
        alert("😁YOU GOT BOOTED BY CONZ BRUH😁 ~Conz~");
        db.collection("users").doc(user.uid).update({ forceLogout:false });
        auth.signOut();
      }
    });

    // LOAD USER DATA
    db.collection("users").doc(user.uid).get().then(doc=>{
      myData = doc.data() || {};

      // ✅ FIX: always apply theme AFTER loading DB
      applyTheme();

      loadChats();
      loadAvatar();
    });

  } else {
    show("welcome");
  }
});

// ===== AUTH =====
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
      secondaryColor:"#ff0000",
      online:true,
      lastSeen:Date.now()
    });
  });
}

function logout(){
  db.collection("users").doc(currentUser.uid).set({
    online:false,
    lastSeen:Date.now()
  },{merge:true});

  auth.signOut();
}

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

  if(typeof mainColorPicker !== "undefined" && mainColorPicker){
    mainColorPicker.value = main;
  }

  if(typeof secondaryColorPicker !== "undefined" && secondaryColorPicker){
    secondaryColorPicker.value = secondary;
  }
}

function openSettings(){ show("settings"); }

function saveTheme(){
  myData.mainColor = mainColorPicker.value;
  myData.secondaryColor = secondaryColorPicker.value;

  db.collection("users").doc(currentUser.uid).set({
    mainColor: myData.mainColor,
    secondaryColor: myData.secondaryColor
  },{merge:true}).then(()=>{
    applyTheme(); // ✅ ensure applied AFTER save
  });
}

function resetTheme(){
  myData.mainColor = "#000000";
  myData.secondaryColor = "#ff0000";

  db.collection("users").doc(currentUser.uid).set({
    mainColor:"#000000",
    secondaryColor:"#ff0000"
  },{merge:true}).then(()=>{
    applyTheme();
  });
}

// ===== PROFILE =====
function openProfile(uid=currentUser.uid){
  db.collection("users").doc(uid).get().then(doc=>{
    let u=doc.data()||{};
    let isDevProfile = uid === DEV_UID;

    profileContent.innerHTML = `
      <div class="avatar" ${uid===currentUser.uid ? 'onclick="pickImage()"' : ''}>
        ${u.photo?`<img src="${u.photo}">`:""}
      </div>

      <div class="displayName">${u.displayName||u.username}</div>

      ${isDevProfile ? `
        <div class="devBadge">👑 ConzChat Dev</div>
      ` : ""}

      <div class="username">@${u.username}</div>

      ${isDev && uid!==currentUser.uid ? `
        <button onclick="bootUser('${uid}')">Boot User</button>
      ` : ""}
    `;

    daysOnApp.innerText =
      Math.floor((Date.now()-u.created)/86400000)+" days on ConzChat";

    show("profile");
  });
}

// ===== PROFILE IMAGE PICKER =====
function pickImage(){
  filePicker.click();
}

filePicker.onchange = e=>{
  let file = e.target.files[0];
  if(!file) return;

  let reader = new FileReader();
  reader.onload = ()=>{
    db.collection("users").doc(currentUser.uid).update({
      photo:reader.result
    }).then(()=>{
      myData.photo = reader.result;
      loadAvatar();
      openProfile();
    });
  };
  reader.readAsDataURL(file);
};

function loadAvatar(){
  profileBtn.innerHTML = myData.photo
    ? `<img src="${myData.photo}" style="width:30px;height:30px;border-radius:50%">`
    : "👤";
}

// ===== DEV BOOT =====
function bootUser(uid){
  db.collection("users").doc(uid).update({
    forceLogout:true
  });
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
      `;

      div.onclick=()=>openChat(doc.id,u.username,u.photo);
      results.appendChild(div);
    });
  });
}

// ===== CHAT =====
function openChat(uid,name,photo){
  currentChatUser = uid;
  show("chat");

  if(unsubscribeMessages) unsubscribeMessages();
  if(unsubscribeStatus) unsubscribeStatus();

  unsubscribeStatus = db.collection("users").doc(uid)
  .onSnapshot(doc=>{
    let u = doc.data() || {};

    if(u.online){
      chatName.innerText = name + " 🟢 Online";
    } else {
      let seconds = Math.floor((Date.now() - (u.lastSeen || 0)) / 1000);

      let text = seconds < 60
        ? `${seconds}s ago`
        : seconds < 3600
        ? `${Math.floor(seconds/60)}m ago`
        : `${Math.floor(seconds/3600)}h ago`;

      chatName.innerText = name + " • Last seen " + text;
    }
  });

  unsubscribeMessages = db.collection("messages")
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
