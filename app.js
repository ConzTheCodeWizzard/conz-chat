// FIREBASE INIT (already done in firebase.js)
const auth = firebase.auth();
const db = firebase.firestore();

let currentUser;
let currentChatUser;

// LOGIN STATE
auth.onAuthStateChanged(user => {
  if(user){
    currentUser = user;
    loadChats();
  } else {
    show('login');
  }
});

// SCREEN SWITCH
function show(id){
  document.querySelectorAll('.screen').forEach(s=>s.classList.remove('active'));
  document.getElementById(id).classList.add('active');
}

// =========================
// 🔥 CHAT LIST (MAIN FEATURE)
// =========================
function loadChats(){
  db.collection("messages")
  .orderBy("time", "desc")
  .onSnapshot(snapshot => {

    let chats = {};
    
    snapshot.forEach(doc=>{
      let m = doc.data();

      if(m.from === currentUser.uid || m.to === currentUser.uid){
        let other = m.from === currentUser.uid ? m.to : m.from;

        if(!chats[other]){
          chats[other] = m;
        }
      }
    });

    renderChatList(chats);
  });
}

// RENDER CHAT LIST
function renderChatList(chats){
  chatList.innerHTML = "";

  Object.keys(chats).forEach(uid => {

    db.collection("users").doc(uid).get().then(userDoc=>{
      let user = userDoc.data();
      let msg = chats[uid];

      let div = document.createElement("div");
      div.className = "chatItem";

      div.innerHTML = `
        <div class="avatar">${user.photo ? `<img src="${user.photo}">` : "👤"}</div>
        <div class="chatInfo">
          <div class="name">${user.username}</div>
          <div class="lastMsg">${msg.text || "📷 Media"}</div>
        </div>
      `;

      div.onclick = ()=> openChat(uid, user.username);

      chatList.appendChild(div);
    });
  });
}

// =========================
// 💬 CHAT
// =========================
function openChat(uid, name){
  currentChatUser = uid;
  chatName.innerText = name;
  show("chat");

  db.collection("messages")
  .orderBy("time")
  .onSnapshot(snapshot=>{
    messages.innerHTML = "";

    snapshot.forEach(doc=>{
      let m = doc.data();

      if(
        (m.from === currentUser.uid && m.to === uid) ||
        (m.from === uid && m.to === currentUser.uid)
      ){
        let div = document.createElement("div");
        div.className = m.from === currentUser.uid ? "me" : "them";
        div.innerText = m.text;

        messages.appendChild(div);
      }
    });
  });
}

// SEND MESSAGE
function send(){
  let text = msg.value;
  if(!text) return;

  db.collection("messages").add({
    text,
    from: currentUser.uid,
    to: currentChatUser,
    time: Date.now()
  });

  msg.value = "";
}

// =========================
// 🔍 SEARCH USERS (FIXED)
// =========================
function openSearch(){
  show("search");
}

function searchUsers(){
  let q = searchInput.value.toLowerCase();
  results.innerHTML = "";

  db.collection("users").get().then(snapshot=>{
    snapshot.forEach(doc=>{
      let user = doc.data();

      if(user.username.toLowerCase().includes(q)){
        let div = document.createElement("div");
        div.className = "chatItem";
        div.innerHTML = `
          <div class="avatar">👤</div>
          <div>${user.username}</div>
        `;

        div.onclick = ()=> openChat(doc.id, user.username);

        results.appendChild(div);
      }
    });
  });
}

// =========================
// 👤 PROFILE
// =========================
function openMyProfile(){
  show("profile");

  db.collection("users").doc(currentUser.uid).get().then(doc=>{
    let user = doc.data();
    profileName.innerText = user.username;
  });
}
