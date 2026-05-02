let currentUser = null;
let currentChat = null;
let navStack = [];

function show(id){
  const current = document.querySelector(".screen.active");
  if(current) navStack.push(current.id);

  document.querySelectorAll(".screen").forEach(s => s.classList.remove("active"));
  document.getElementById(id).classList.add("active");
}

function goBack(){
  let last = navStack.pop();
  if(last) show(last);
}

auth.onAuthStateChanged(user => {
  currentUser = user;
});

function logout(){
  auth.signOut().then(()=> show("welcome"));
}

async function signup(){
  const u = su_user.value;
  const p = su_pass.value;

  const res = await auth.createUserWithEmailAndPassword(u+"@app.com", p);

  await db.collection("users").doc(res.user.uid).set({
    username:u,
    created:Date.now()
  });

  currentUser = res.user;
  loadChats();
  show("home");
}

async function login(){
  const u = li_user.value;
  const p = li_pass.value;

  const res = await auth.signInWithEmailAndPassword(u+"@app.com", p);

  currentUser = res.user;
  loadChats();
  show("home");
}

function loadChats(){
  db.collection("messages").onSnapshot(snap=>{
    chatList.innerHTML="";
    let users=new Set();

    snap.forEach(doc=>{
      let m=doc.data();
      if(m.from===currentUser.uid) users.add(m.to);
      if(m.to===currentUser.uid) users.add(m.from);
    });

    users.forEach(uid=>{
      db.collection("users").doc(uid).get().then(u=>{
        let d=u.data();
        let div=document.createElement("div");
        div.className="chatItem";
        div.innerText=d.username;
        div.onclick=()=>openChat(uid,d.username);
        chatList.appendChild(div);
      });
    });
  });
}

function openChat(uid,name){
  currentChat=uid;
  chatName.innerText=name;
  show("chat");

  db.collection("messages").onSnapshot(snap=>{
    messages.innerHTML="";
    snap.forEach(doc=>{
      let m=doc.data();
      if((m.from===currentUser.uid&&m.to===uid)||(m.from===uid&&m.to===currentUser.uid)){
        let div=document.createElement("div");
        div.className=m.from===currentUser.uid?"msgMe":"msgOther";
        div.innerText=m.text;
        messages.appendChild(div);
      }
    });
  });
}

function send(){
  db.collection("messages").add({
    from:currentUser.uid,
    to:currentChat,
    text:msg.value,
    time:Date.now()
  });
  msg.value="";
}

function openSearch(){ show("search"); }

function searchUsers(){
  db.collection("users").get().then(snap=>{
    results.innerHTML="";
    snap.forEach(doc=>{
      let u=doc.data();
      if(u.username.includes(searchInput.value)){
        let div=document.createElement("div");
        div.className="chatItem";
        div.innerText=u.username;
        div.onclick=()=>openChat(doc.id,u.username);
        results.appendChild(div);
      }
    });
  });
}

/* PROFILE */
function openMyProfile(){
  db.collection("users").doc(currentUser.uid).get().then(doc=>{
    let u=doc.data();
    profileName.innerText=u.username;
    profileTag.innerText="@"+u.username;
    profileSince.innerText=Math.floor((Date.now()-u.created)/86400000)+" days on ConzChat";
    bioInput.value=u.bio||"";
    show("profile");
  });
}

function openUserProfile(uid){
  db.collection("users").doc(uid).get().then(doc=>{
    let u=doc.data();
    profileName.innerText=u.username;
    profileTag.innerText="@"+u.username;
    profileSince.innerText=Math.floor((Date.now()-u.created)/86400000)+" days on ConzChat";
    show("profile");
  });
}

function saveProfile(){
  db.collection("users").doc(currentUser.uid).update({
    bio:bioInput.value
  });
}
