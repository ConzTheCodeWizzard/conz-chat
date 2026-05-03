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

    // 🔥 DEV TITLE
    if(isDev){
      document.querySelector("#home .topbar span:nth-child(2)").innerText = "ConzChat DEV";
      addDevMenu();
    }

    // 🔥 FORCE LOGOUT LISTENER
    db.collection("users").doc(user.uid)
    .onSnapshot(doc=>{
      let d = doc.data() || {};
      if(d.forceLogout){
        alert("😁 Logged out By Conz 😁");
        db.collection("users").doc(user.uid).update({ forceLogout:false });
        auth.signOut();
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

// 🎨 THEME
function applyTheme(){
  let main = myData.mainColor || "#000000";
  let secondary = myData.secondaryColor || "#ff0000";

  document.documentElement.style.setProperty('--main', main);
  document.documentElement.style.setProperty('--secondary', secondary);

  mainColorPicker.value = main;
  secondaryColorPicker.value = secondary;
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
  saveTheme("#000000","#ff0000");
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
        <div>${u.username}</div>
      `;

      div.onclick=()=>openChat(doc.id,u.username);
      results.appendChild(div);
    });
  });
}

// 💬 CHAT
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

// 🔥 DEV MENU
function addDevMenu(){
  let dev=document.createElement("div");
  dev.innerText="Dev Panel";
  dev.onclick=()=>openDevPanel();
  fabMenu.appendChild(dev);
}

function openDevPanel(){
  let panel=prompt("Enter username to boot:");
  if(!panel) return;

  db.collection("users").get().then(snap=>{
    snap.forEach(doc=>{
      let u=doc.data();
      if(u.username===panel){
        db.collection("users").doc(doc.id).update({
          forceLogout:true
        });
      }
    });
  });
}
