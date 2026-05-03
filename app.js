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
      myData = doc.data() || {};
      applyTheme();   // 🔥 FIX: apply immediately
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

// =====================
// 🎨 THEME (FULL FIX)
// =====================

function applyTheme(){
  let main = myData.mainColor || "#000000";
  let secondary = myData.secondaryColor || "#ff0000";

  document.documentElement.style.setProperty('--main', main);
  document.documentElement.style.setProperty('--secondary', secondary);

  if(mainColorPicker) mainColorPicker.value = main;
  if(secondaryColorPicker) secondaryColorPicker.value = secondary;
}

function saveTheme(){
  let main = mainColorPicker.value || "#000000";
  let secondary = secondaryColorPicker.value || "#ff0000";

  // 🔥 instant apply
  document.documentElement.style.setProperty('--main', main);
  document.documentElement.style.setProperty('--secondary', secondary);

  // 🔥 update local data (THIS WAS YOUR BUG)
  myData.mainColor = main;
  myData.secondaryColor = secondary;

  // 🔥 save to firestore
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

  mainColorPicker.value = main;
  secondaryColorPicker.value = secondary;

  db.collection("users").doc(currentUser.uid).set({
    mainColor: main,
    secondaryColor: secondary
  }, { merge:true });
}

// =====================
// PROFILE
// =====================

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

// =====================
// IMAGE
// =====================

function pickImage(){
  let input=document.createElement("input");
  input.type="file";
  input.accept="image/*";

  input.onchange=e=>{
    let file=e.target.files[0];
    if(!file) return;

    let img = new Image();
    let reader = new FileReader();

    reader.onload = ev=>{
      img.src = ev.target.result;
    };

    img.onload = ()=>{
      let canvas = document.createElement("canvas");
      let ctx = canvas.getContext("2d");

      let size = 200;
      canvas.width = size;
      canvas.height = size;

      ctx.drawImage(img,0,0,size,size);

      let compressed = canvas.toDataURL("image/jpeg",0.6);

      db.collection("users").doc(currentUser.uid).set({
        photo: compressed
      }, { merge:true }).then(()=>{
        myData.photo = compressed;
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

// =====================
// SEARCH
// =====================

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

// =====================
// SAFE IMAGE
// =====================

function addImg(el, src){
  if(!src) return;
  if(typeof src !== "string") return;
  if(src.length > 120000) return;

  let img = document.createElement("img");
  img.src = src;
  img.onerror = ()=>img.remove();

  el.appendChild(img);
}

// =====================
// CHAT (SAFE)
// =====================

function openChat(uid,name){
  currentChatUser = uid;
  chatName.innerText = name;
  show("chat");

  if(unsubscribeMessages) unsubscribeMessages();

  messages.innerHTML = "";

  db.collection("users").doc(uid).get().then(userDoc=>{
    let otherUser = userDoc.data() || {};

    unsubscribeMessages = db.collection("messages")
    .orderBy("time")
    .onSnapshot(snap=>{

      try{

        messages.innerHTML="";

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
            <div>${new Date(m.time).toLocaleTimeString()}</div>
          `;

          if(isMine){
            addImg(avatar,myData.photo);
            wrap.appendChild(bubble);
            wrap.appendChild(avatar);
          } else {
            addImg(avatar,otherUser.photo);
            wrap.appendChild(avatar);
            wrap.appendChild(bubble);
          }

          messages.appendChild(wrap);
        });

        messages.scrollTop = messages.scrollHeight;

      }catch(e){
        console.log("Prevented crash:",e);
      }

    });
  });
}

// =====================
// SEND
// =====================

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

// =====================
// CHAT LIST
// =====================

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
