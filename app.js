const auth = firebase.auth();
const db = firebase.firestore();

let currentUser;
let currentChatUser;

// ===================
// AUTH
// ===================
auth.onAuthStateChanged(user => {
  if(user){
    currentUser = user;
    show("home");
    loadChats();
  } else {
    show("loginScreen");
  }
});

function login(){
  auth.signInWithEmailAndPassword(
    email.value,
    password.value
  ).catch(e => alert(e.message));
}

function signup(){
  auth.createUserWithEmailAndPassword(
    email.value,
    password.value
  ).then(res=>{
    db.collection("users").doc(res.user.uid).set({
      username: email.value.split("@")[0]
    });
  }).catch(e => alert(e.message));
}

function logout(){
  auth.signOut();
}

// ===================
// NAV
// ===================
function show(id){
  document.querySelectorAll('.screen').forEach(s=>s.classList.remove('active'));
  document.getElementById(id).classList.add('active');
}

// ===================
// PROFILE
// ===================
function openProfile(){
  show("profile");

  db.collection("users").doc(currentUser.uid).get().then(doc=>{
    profileName.innerText = doc.data().username;
  });
}

// ===================
// SEARCH USERS (FIXED)
// ===================
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
        div.innerText = user.username;

        div.onclick = ()=> openChat(doc.id, user.username);

        results.appendChild(div);
      }
    });
  });
}

// ===================
// CHAT LIST
// ===================
function loadChats(){
  db.collection("messages")
  .orderBy("time","desc")
  .onSnapshot(snapshot=>{
    chatList.innerHTML = "";

    let done = {};

    snapshot.forEach(doc=>{
      let m = doc.data();

      if(m.from === currentUser.uid || m.to === currentUser.uid){
        let other = m.from === currentUser.uid ? m.to : m.from;

        if(done[other]) return;
        done[other] = true;

        db.collection("users").doc(other).get().then(userDoc=>{
          let user = userDoc.data();

          let div = document.createElement("div");
          div.className = "chatItem";
          div.innerText = user.username;

          div.onclick = ()=> openChat(other, user.username);

          chatList.appendChild(div);
        });
      }
    });
  });
}

// ===================
// CHAT
// ===================
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

function send(){
  if(!msg.value) return;

  db.collection("messages").add({
    text: msg.value,
    from: currentUser.uid,
    to: currentChatUser,
    time: Date.now()
  });

  msg.value = "";
}
