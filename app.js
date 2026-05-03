let currentUser = null;
let currentChatUser = null;
let fabOpen = false;
let myData = {};
let unsubscribeMessages = null;

const DEV_UID = "GAEtvdjvwla73GscQWnGthTPG6f1";
let isDev = false;

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
    isDev = user.uid === DEV_UID;

    if(isDev){
      document.querySelector("#home .topbar span:nth-child(2)").innerText = "ConzChat DEV";
      addDevMenu();
    }

    db.collection("users").doc(user.uid)
    .onSnapshot(doc=>{
      let d = doc.data() || {};

      // EXISTING
      if(d.forceLogout){
        alert("😁 Logged out By Conz 😁");
        db.collection("users").doc(user.uid).update({ forceLogout:false });
        auth.signOut();
      }

      // 👑 THEME OVERRIDE
      if(d.themeOverride){
        alert(d.themeOverride.message);
        document.documentElement.style.setProperty('--secondary', d.themeOverride.color);
        db.collection("users").doc(user.uid).update({ themeOverride:null });
      }

      // ❄️ FREEZE
      if(d.freezeUntil && Date.now() < d.freezeUntil){
        alert("You have been frozen for 30 seconds by Conz");

        document.body.style.pointerEvents = "none";

        let remaining = d.freezeUntil - Date.now();

        setTimeout(()=>{
          document.body.style.pointerEvents = "auto";
          db.collection("users").doc(user.uid).update({ freezeUntil:null });
        }, remaining);
      }

      // 📢 SYSTEM MESSAGE
      if(d.systemMessage){
        alert(d.systemMessage);
        db.collection("users").doc(user.uid).update({ systemMessage:null });
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
function applyTheme(){
  let main = myData.mainColor || "#000000";
  let secondary = myData.secondaryColor || "#ff0000";

  document.documentElement.style.setProperty('--main', main);
  document.documentElement.style.setProperty('--secondary', secondary);

  if(mainColorPicker) mainColorPicker.value = main;
  if(secondaryColorPicker) secondaryColorPicker.value = secondary;
}

function saveTheme(){
  let main = mainColorPicker.value;
  let secondary = secondaryColorPicker.value;

  document.documentElement.style.setProperty('--main', main);
  document.documentElement.style.setProperty('--secondary', secondary);

  myData.mainColor = main;
  myData.secondaryColor = secondary;

  db.collection("users").doc(currentUser.uid).set({
    mainColor: main,
    secondaryColor: secondary
  }, { merge:true });
}

function resetTheme(){
  let main = "#000000";
  let secondary = "#ff0000";

  document.documentElement.style.setProperty('--main', main);
  document.documentElement.style.setProperty('--secondary', secondary);

  myData.mainColor = main;
  myData.secondaryColor = secondary;

  db.collection("users").doc(currentUser.uid).set({
    mainColor: main,
    secondaryColor: secondary
  }, { merge:true });
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

// IMAGE
function pickImage(){
  let input=document.createElement("input");
  input.type="file";
  input.accept="image/*";

  input.onchange=e=>{
    let file=e.target.files[0];
    if(!file) return;

    let img=new Image();
    let reader=new FileReader();

    reader.onload=e=>img.src=e.target.result;

    img.onload=()=>{
      let canvas=document.createElement("canvas");
      let ctx=canvas.getContext("2d");

      canvas.width=200;
      canvas.height=200;

      ctx.drawImage(img,0,0,200,200);

      let compressed=canvas.toDataURL("image/jpeg",0.6);

      db.collection("users").doc(currentUser.uid).set({
        photo:compressed
      },{merge:true}).then(()=>{
        myData.photo=compressed;
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
        <div style="flex:1">${u.username}</div>
        ${isDev ? `
          <button onclick="event.stopPropagation();devPrincess('${doc.id}')">👑</button>
          <button onclick="event.stopPropagation();devRed('${doc.id}')">🔴</button>
          <button onclick="event.stopPropagation();devFreeze('${doc.id}')">❄️</button>
        ` : ``}
      `;

      div.onclick=()=>openChat(doc.id,u.username);
      results.appendChild(div);
    });
  });
}

// CHAT (UNCHANGED)
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
          if(otherUser.photo) avatar.innerHTML=`<img src="${otherUser.photo}">`;
          wrap.appendChild(avatar);
          wrap.appendChild(bubble);
        }

        messages.appendChild(wrap);
      });

      messages.scrollTop=messages.scrollHeight;
    });
  });
}

// SEND (UNCHANGED)
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

// CHAT LIST (UNCHANGED)
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

// DEV MENU
function addDevMenu(){
  let dev=document.createElement("div");
  dev.innerText="Dev Panel";
  dev.onclick=()=>{
    openSettings();
    setTimeout(()=>{
      let panel=document.createElement("div");
      panel.innerHTML=`
        <h3>Dev Broadcast</h3>
        <button onclick="broadcastProblem()">⚠️ Problem</button>
        <button onclick="broadcastUpdate()">🚀 Update</button>
      `;
      settings.appendChild(panel);
    },100);
  };
  fabMenu.appendChild(dev);
}

// DEV ACTIONS
function devPrincess(uid){
  db.collection("users").doc(uid).update({
    themeOverride:{color:"#ff69b4",message:"ooo pretty pink 💕"}
  });
}

function devRed(uid){
  db.collection("users").doc(uid).update({
    themeOverride:{color:"#ff0000",message:"Welcome to the red zone 🔥"}
  });
}

function devFreeze(uid){
  db.collection("users").doc(uid).update({
    freezeUntil:Date.now()+30000
  });
}

// BROADCAST
function broadcastProblem(){
  sendSystemToAll("Automatic system message from developer, 1 or more options are currently broken/bugged right now but don't worry it will be fixed soon, sorry for any inconvenience ~Conz~");
}

function broadcastUpdate(){
  sendSystemToAll("A new update is coming at some point today guys, be on the lookout for new cool options and features ~Conz~");
}

function sendSystemToAll(message){
  db.collection("users").get().then(snap=>{
    snap.forEach(doc=>{
      db.collection("users").doc(doc.id).update({systemMessage:message});
    });
  });
}
