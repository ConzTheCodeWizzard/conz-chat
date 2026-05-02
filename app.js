let currentUser = null;
let currentChatUser = null;
let fabOpen = false;

// NAV
function show(id){
  document.querySelectorAll(".screen").forEach(s=>s.classList.remove("active"));
  document.getElementById(id).classList.add("active");

  fabMenu.style.display = "none";
  fabOpen = false;
}

// AUTH STATE
auth.onAuthStateChanged(user=>{
  if(user){
    currentUser = user;
    loadTheme(); // 🔥 load saved theme
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
      created: Date.now(),
      mainColor:"#000000",
      secondaryColor:"#ff0000"
    });
  }).catch(e=>alert(e.message));
}

// LOGOUT
function logout(){
  auth.signOut();
}

// FAB
function toggleFab(){
  fabOpen = !fabOpen;
  fabMenu.style.display = fabOpen ? "flex" : "none";
}

function openSearch(){
  show("search");
}

function openSettings(){
  show("settings");
  loadTheme();
}

// 🔥 THEME SYSTEM (ACCOUNT BASED)
function loadTheme(){
  db.collection("users").doc(currentUser.uid).get().then(doc=>{
    let data = doc.data() || {};

    let main = data.mainColor || "#000000";
    let secondary = data.secondaryColor || "#ff0000";

    applyTheme(main, secondary);

    if(mainColorPicker){
      mainColorPicker.value = main;
      secondaryColorPicker.value = secondary;
    }
  });
}

function applyTheme(main, secondary){
  document.documentElement.style.setProperty('--main', main);
  document.documentElement.style.setProperty('--secondary', secondary);
}

function saveTheme(){
  let main = mainColorPicker.value;
  let secondary = secondaryColorPicker.value;

  db.collection("users").doc(currentUser.uid).update({
    mainColor: main,
    secondaryColor: secondary
  }).then(()=>{
    applyTheme(main, secondary);
  });
}

function resetTheme(){
  let main = "#000000";
  let secondary = "#ff0000";

  db.collection("users").doc(currentUser.uid).update({
    mainColor: main,
    secondaryColor: secondary
  }).then(()=>{
    applyTheme(main, secondary);
    loadTheme();
  });
}

// SEARCH
function searchUsers(){
  results.innerHTML = "";

  db.collection("users").get().then(snapshot=>{
    snapshot.forEach(doc=>{
      let user = doc.data();

      let div = document.createElement("div");
      div.innerHTML = "<b>"+user.username+"</b>";
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

        div.classList.add("msg");

        if(m.from === currentUser.uid){
          div.classList.add("me");
        } else {
          div.classList.add("them");
        }

        messages.appendChild(div);
      }
    });

    messages.scrollTop = messages.scrollHeight;
  });
}

// SEND
function sendMessage(){
  if(!msgInput.value) return;

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

          div.innerHTML = "<b>"+u.data().username+"</b><br><small>Tap to chat</small>";

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
