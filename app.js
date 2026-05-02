let currentUser = null;
let currentChat = null;

/* NAVIGATION */
function show(id){
  document.querySelectorAll(".screen").forEach(s=>s.classList.remove("active"));
  document.getElementById(id).classList.add("active");
}

function goHome(){
  show("home");
}

/* AUTH */
auth.onAuthStateChanged(u=>{
  currentUser = u;
  if(u){
    loadChats();
    show("home");
  } else {
    show("welcome");
  }
});

function logout(){
  auth.signOut();
}

/* SIGNUP */
async function signup(){
  let u = su_user.value;
  let p = su_pass.value;

  let res = await auth.createUserWithEmailAndPassword(u+"@app.com", p);

  await db.collection("users").doc(res.user.uid).set({
    username: u,
    created: Date.now(),
    bio: "",
    photo: ""
  });
}

/* LOGIN */
async function login(){
  let u = li_user.value;
  let p = li_pass.value;

  await auth.signInWithEmailAndPassword(u+"@app.com", p);
}

/* CHAT LIST */
function loadChats(){
  db.collection("messages").onSnapshot(snap=>{
    chatList.innerHTML = "";

    let users = new Set();

    snap.forEach(doc=>{
      let m = doc.data();

      if(m.from === currentUser.uid) users.add(m.to);
      if(m.to === currentUser.uid) users.add(m.from);
    });

    users.forEach(uid=>{
      db.collection("users").doc(uid).get().then(doc=>{
        let u = doc.data();

        let div = document.createElement("div");
        div.className = "chatItem";
        div.innerText = u.username;

        div.onclick = ()=>openChat(uid, u.username);

        chatList.appendChild(div);
      });
    });
  });
}

/* OPEN CHAT */
function openChat(uid, name){
  currentChat = uid;
  chatName.innerText = name;

  show("chat");

  db.collection("messages").onSnapshot(snap=>{
    messages.innerHTML = "";

    snap.forEach(doc=>{
      let m = doc.data();

      if(
        (m.from === currentUser.uid && m.to === uid) ||
        (m.from === uid && m.to === currentUser.uid)
      ){
        let div = document.createElement("div");
        div.className = m.from === currentUser.uid ? "msgMe" : "msgOther";
        div.innerText = m.text;

        messages.appendChild(div);
      }
    });
  });
}

/* SEND */
function send(){
  if(!msg.value) return;

  db.collection("messages").add({
    from: currentUser.uid,
    to: currentChat,
    text: msg.value,
    time: Date.now()
  });

  msg.value = "";
}

/* SEARCH */
function openSearch(){
  show("search");
}

function searchUsers(){
  let q = searchInput.value.toLowerCase();

  db.collection("users").onSnapshot(snap=>{
    results.innerHTML = "";

    snap.forEach(doc=>{
      let u = doc.data();

      if(u.username.toLowerCase().includes(q)){
        let div = document.createElement("div");
        div.className = "chatItem";
        div.innerText = u.username;

        div.onclick = ()=>openChat(doc.id, u.username);

        results.appendChild(div);
      }
    });
  });
}

/* PROFILE */
function openMyProfile(){
  loadProfile(currentUser.uid, true);
}

function openUserProfile(uid){
  loadProfile(uid, false);
}

function loadProfile(uid, editable){
  db.collection("users").doc(uid).get().then(doc=>{
    let u = doc.data();

    profileName.innerText = u.username;
    profileTag.innerText = "@"+u.username;

    let days = Math.floor((Date.now()-u.created)/86400000);
    profileSince.innerText = days + " days on ConzChat";

    bioInput.value = u.bio || "";
    bioInput.disabled = !editable;

    if(u.photo){
      setProfilePic(u.photo);
    } else {
      profilePic.innerHTML = "👤";
      profilePic.style.background = "#222";
    }

    show("profile");
  });
}

/* SAVE BIO */
function saveProfile(){
  db.collection("users").doc(currentUser.uid).update({
    bio: bioInput.value
  });
}

/* PROFILE PIC CLICK */
profilePic.onclick = () => {

  let hasPic = profilePic.style.backgroundImage;

  if(!hasPic || hasPic === "none"){
    uploadPic.click();
    return;
  }

  let choice = prompt("1 View\n2 Change");

  if(choice === "1"){
    let url = profilePic.style.backgroundImage
      .replace('url("','')
      .replace('")','');

    window.open(url);
  }

  if(choice === "2"){
    uploadPic.click();
  }
};

/* UPLOAD IMAGE */
uploadPic.addEventListener("change", e=>{
  let file = e.target.files[0];
  if(!file) return;

  let reader = new FileReader();

  reader.onload = ()=>{
    let img = reader.result;

    setProfilePic(img);

    db.collection("users").doc(currentUser.uid).update({
      photo: img
    });
  };

  reader.readAsDataURL(file);
});

/* APPLY IMAGE */
function setProfilePic(img){
  profilePic.style.backgroundImage = `url(${img})`;
  profilePic.style.backgroundSize = "cover";
  profilePic.style.backgroundPosition = "center";
  profilePic.innerHTML = "";
}
