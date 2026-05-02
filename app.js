let currentUser = null;
let currentChatUser = null;
let fabOpen = false;

// NAV
function show(id){
  document.querySelectorAll(".screen").forEach(s=>s.classList.remove("active"));
  document.getElementById(id).classList.add("active");

  if (fabMenu){
    fabMenu.style.display = "none";
    fabOpen = false;
  }
}

// AUTH
auth.onAuthStateChanged(user=>{
  if(user){
    currentUser = user;
    loadTheme();
    loadChats();
  } else {
    show("welcome");
  }
});

// LOGIN
function login(){
  auth.signInWithEmailAndPassword(loginEmail.value, loginPassword.value)
  .catch(e=>alert(e.message));
}

// SIGNUP
function signup(){
  auth.createUserWithEmailAndPassword(signupEmail.value, signupPassword.value)
  .then(res=>{
    return db.collection("users").doc(res.user.uid).set({
      username: signupUsername.value,
      created: Date.now(),
      mainColor:"#000000",
      secondaryColor:"#ff0000"
    });
  }).catch(e=>alert(e.message));
}

// FAB
function toggleFab(){
  fabOpen = !fabOpen;
  fabMenu.style.display = fabOpen ? "flex" : "none";
}

function openSearch(){ show("search"); }
function openSettings(){ show("settings"); loadTheme(); }

// THEME LIVE
function loadTheme(){
  db.collection("users").doc(currentUser.uid).get().then(doc=>{
    let d = doc.data() || {};
    let main = d.mainColor || "#000";
    let sec = d.secondaryColor || "#f00";

    applyTheme(main, sec);

    if(mainColorPicker){
      mainColorPicker.value = main;
      secondaryColorPicker.value = sec;

      mainColorPicker.oninput = ()=>applyTheme(mainColorPicker.value, secondaryColorPicker.value);
      secondaryColorPicker.oninput = ()=>applyTheme(mainColorPicker.value, secondaryColorPicker.value);
    }
  });
}

function applyTheme(m,s){
  document.documentElement.style.setProperty('--main', m);
  document.documentElement.style.setProperty('--secondary', s);
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

// SEARCH
function searchUsers(){
  let term = searchInput.value.toLowerCase();
  results.innerHTML = "";

  db.collection("users").get().then(snap=>{
    snap.forEach(doc=>{
      let u = doc.data();
      if(u.username.toLowerCase().includes(term)){
        let d = document.createElement("div");
        d.innerText = u.username;
        d.onclick = ()=>openChat(doc.id,u.username);
        results.appendChild(d);
      }
    });
  });
}

// CHAT
function openChat(uid,name){
  currentChatUser = uid;
  chatName.innerText = name;
  show("chat");

  db.collection("messages").orderBy("time")
  .onSnapshot(snap=>{
    messages.innerHTML="";
    snap.forEach(doc=>{
      let m=doc.data();

      if((m.from===currentUser.uid && m.to===uid)||(m.from===uid && m.to===currentUser.uid)){
        let d=document.createElement("div");

        let time=new Date(m.time).toLocaleTimeString();

        d.innerHTML=`${m.text}<br><small>${time}</small>`;
        d.className="msg "+(m.from===currentUser.uid?"me":"them");

        messages.appendChild(d);
      }
    });
    messages.scrollTop=messages.scrollHeight;
  });
}

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
    let done={};

    snap.forEach(doc=>{
      let m=doc.data();
      if(m.from===currentUser.uid||m.to===currentUser.uid){
        let other=m.from===currentUser.uid?m.to:m.from;
        if(done[other])return;
        done[other]=true;

        db.collection("users").doc(other).get().then(u=>{
          let d=document.createElement("div");
          d.innerHTML=`<b>${u.data().username}</b>`;
          d.onclick=()=>openChat(other,u.data().username);
          chatList.appendChild(d);
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
    daysOnApp.innerText=Math.floor((Date.now()-u.created)/86400000)+" days on ConzChat";
    show("profile");
  });
}
