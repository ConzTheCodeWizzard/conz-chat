let currentUser = null;
let currentChatUser = null;

// NAV
function show(id){
  document.querySelectorAll(".screen").forEach(s=>s.classList.remove("active"));
  document.getElementById(id).classList.add("active");
}

// AUTH STATE
auth.onAuthStateChanged(user=>{
  if(user){
    currentUser = user;
    loadChats();
  } else {
    show("welcome");
  }
});

// LOGIN
function login(){
  auth.signInWithEmailAndPassword(
    loginEmail.value,
    loginPassword.value
  ).catch(e=>alert(e.message));
}

// SIGNUP
function signup(){
  auth.createUserWithEmailAndPassword(
    signupEmail.value,
    signupPassword.value
  ).then(res=>{
    return db.collection("users").doc(res.user.uid).set({
      username: signupUsername.value,
      created: Date.now()
    });
  }).catch(e=>alert(e.message));
}

// LOGOUT
function logout(){
  auth.signOut();
}

// SEARCH
function searchUsers(){
  results.innerHTML = "";

  db.collection("users").get().then(snapshot=>{
    snapshot.forEach(doc=>{
      let user = doc.data();

      let div = document.createElement("div");
      div.innerText = user.username;
      div.onclick = ()=>openChat(doc.id, user.username);

      results.appendChild(div);
    });
  });
}

// OPEN CHAT
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
        (m.from===currentUser.uid && m.to===uid) ||
        (m.from===uid && m.to===currentUser.uid)
      ){
        let div = document.createElement("div");
        div.innerText = m.text;
        messages.appendChild(div);
      }
    });
  });
}

// SEND
function sendMessage(){
  db.collection("messages").add({
    text: msgInput.value,
    from: currentUser.uid,
    to: currentChatUser,
    time: Date.now()
  });

  msgInput.value="";
}

// CHAT LIST
function loadChats(){
  db.collection("messages")
  .orderBy("time","desc")
  .onSnapshot(snapshot=>{
    chatList.innerHTML = "";
    let done = {};

    snapshot.forEach(doc=>{
      let m = doc.data();

      if(m.from===currentUser.uid || m.to===currentUser.uid){
        let other = m.from===currentUser.uid ? m.to : m.from;

        if(done[other]) return;
        done[other]=true;

        db.collection("users").doc(other).get().then(u=>{
          let div=document.createElement("div");
          div.innerText=u.data().username;
          div.onclick=()=>openChat(other,u.data().username);
          chatList.appendChild(div);
        });
      }
    });
  });

  show("home");
}

// PROFILE
function openProfile(){
  db.collection("users").doc(currentUser.uid).get().then(doc=>{
    let u=doc.data();

    profileName.innerText=u.username;

    let days = Math.floor((Date.now()-u.created)/(1000*60*60*24));
    daysOnApp.innerText = days + " days on ConzChat";

    show("profile");
  });
}
