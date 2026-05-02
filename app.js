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
  fabOpen = false;
}

// AUTH
auth.onAuthStateChanged(user=>{
  if(user){
    currentUser = user;

    db.collection("users").doc(user.uid).get().then(doc=>{
      myData = doc.data() || {};
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
      username: signupUsername.value,
      displayName: signupUsername.value,
      photo: "", // 🔥 base64 image
      created: Date.now(),
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
  applyTheme(myData.mainColor || "#000", myData.secondaryColor || "#f00");
}

function applyTheme(m,s){
  document.documentElement.style.setProperty('--main',m);
  document.documentElement.style.setProperty('--secondary',s);
}

function saveTheme(){
  db.collection("users").doc(currentUser.uid).update({
    mainColor: mainColorPicker.value,
    secondaryColor: secondaryColorPicker.value
  });
}

function resetTheme(){
  applyTheme("#000","#f00");
}

// PROFILE
function openProfile(uid=currentUser.uid){
  db.collection("users").doc(uid).get().then(doc=>{
    let u = doc.data() || {};

    let display = u.displayName || u.username || "User";

    profileContent.innerHTML = `
      <div class="avatar" onclick="${uid===currentUser.uid?'pickImage()':''}">
        ${u.photo ? `<img src="${u.photo}">` : ""}
      </div>
      <div class="displayName">${display}</div>
      <div class="username">@${u.username || "unknown"}</div>
    `;

    daysOnApp.innerText =
      Math.floor((Date.now()-(u.created||Date.now()))/86400000)
      + " days on ConzChat";

    show("profile");
  });
}

// 🔥 BASE64 IMAGE UPLOAD (NO STORAGE)
function pickImage(){
  let input=document.createElement("input");
  input.type="file";
  input.accept="image/*";

  input.onchange=e=>{
    let file=e.target.files[0];
    if(!file) return;

    let reader=new FileReader();

    reader.onload=function(e){
      let base64=e.target.result;

      db.collection("users").doc(currentUser.uid).update({
        photo: base64
      }).then(()=>{
        myData.photo = base64;
        loadAvatar();
        openProfile();
      });
    };

    reader.readAsDataURL(file);
  };

  input.click();
}

// TOP RIGHT AVATAR
function loadAvatar(){
  if(myData.photo){
    profileBtn.innerHTML =
      `<img src="${myData.photo}" style="width:30px;height:30px;border-radius:50%">`;
  } else {
    profileBtn.innerHTML = "👤";
  }
}

// NAV HELPERS
function openSearch(){ show("search"); }
function openSettings(){ show("settings"); }

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

  if(unsubscribeMessages) unsubscribeMessages();

  unsubscribeMessages = db.collection("messages")
  .where("participants","array-contains",currentUser.uid)
  .orderBy("time")
  .onSnapshot(snap=>{

    messages.innerHTML="";

    snap.forEach(doc=>{
      let m=doc.data();

      if(!m.participants.includes(uid)) return;

      let wrap=document.createElement("div");
      wrap.className="msgWrap "+(m.from===currentUser.uid?"me":"them");

      let avatar=document.createElement("div");
      avatar.className="msgAvatar";

      let bubble=document.createElement("div");
      bubble.className="msg";
      bubble.innerText=m.text;

      if(m.from===currentUser.uid){
        if(myData.photo){
          avatar.innerHTML=`<img src="${myData.photo}">`;
        }
        wrap.appendChild(bubble);
        wrap.appendChild(avatar);
      } else {
        db.collection("users").doc(uid).get().then(u=>{
          if(u.data()?.photo){
            avatar.innerHTML=`<img src="${u.data().photo}">`;
          }
        });

        wrap.appendChild(avatar);
        wrap.appendChild(bubble);
      }

      messages.appendChild(wrap);
    });

    messages.scrollTop=messages.scrollHeight;
  });
}

// SEND
function sendMessage(){
  if(!msgInput.value) return;

  db.collection("messages").add({
    text:msgInput.value,
    from:currentUser.uid,
    to:currentChatUser,
    participants:[currentUser.uid,currentChatUser],
    time:Date.now()
  });

  msgInput.value="";
}

// CHAT LIST (ONLY REAL CHATS)
function loadChats(){
  db.collection("messages")
  .where("participants","array-contains",currentUser.uid)
  .orderBy("time","desc")
  .onSnapshot(snap=>{

    chatList.innerHTML="";
    let seen={};

    snap.forEach(doc=>{
      let m=doc.data();

      let other=m.from===currentUser.uid?m.to:m.from;

      if(seen[other]) return;
      seen[other]=true;

      db.collection("users").doc(other).get().then(u=>{
        let data=u.data()||{};

        let div=document.createElement("div");

        div.innerHTML=`
          <div class="chatAvatar">
            ${data.photo ? `<img src="${data.photo}">` : ""}
          </div>
          <div>${data.username || "User"}</div>
        `;

        div.onclick=()=>openChat(other,data.username);

        chatList.appendChild(div);
      });
    });

  });

  show("home");
}
