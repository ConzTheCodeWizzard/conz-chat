let currentUser = "";
let otherUser = "";

/* NAV */
function show(id){
  document.querySelectorAll(".screen").forEach(s=>s.classList.remove("active"));
  document.getElementById(id).classList.add("active");
}

/* AUTH */
async function signup(){
  let u = su_user.value;
  let p = su_pass.value;

  let res = await auth.createUserWithEmailAndPassword(u+"@app.com", p);

  await db.collection("users").doc(res.user.uid).set({
    username:u
  });

  currentUser = res.user.uid;
  loadChats();
  show("home");
}

async function login(){
  let u = li_user.value;
  let p = li_pass.value;

  let res = await auth.signInWithEmailAndPassword(u+"@app.com", p);

  currentUser = res.user.uid;
  loadChats();
  show("home");
}

/* SEARCH */
function openSearch(){
  show("search");
}

function searchUsers(){
  let input = searchInput.value.toLowerCase();

  db.collection("users").get().then(snap=>{
    results.innerHTML="";

    snap.forEach(doc=>{
      let u = doc.data();

      if(u.username.toLowerCase().includes(input)){
        results.innerHTML += `
          <div class="item" onclick="openChat('${doc.id}','${u.username}')">
            ${u.username}
          </div>
        `;
      }
    });
  });
}

/* PROFILE */
function openProfile(uid=currentUser){
  db.collection("users").doc(uid).get().then(doc=>{
    let u = doc.data();

    profileContent.innerHTML = `
      <h2>${u.username}</h2>
    `;
  });

  show("profile");
}

/* CHAT */
function chatId(a,b){ return [a,b].sort().join("_"); }

function openChat(uid,name){
  otherUser = uid;
  chatName.innerText = name;
  show("chat");

  let id = chatId(currentUser,uid);

  db.collection("messages").doc(id).collection("msgs")
  .orderBy("time")
  .onSnapshot(snap=>{
    messages.innerHTML="";

    snap.forEach(doc=>{
      let m = doc.data();
      let cls = m.from===currentUser ? "me":"other";

      messages.innerHTML += `
        <div class="${cls}">
          <div>${m.text}</div>
        </div>
      `;
    });
  });
}

/* SEND */
async function send(){
  if(!msg.value) return;

  let id = chatId(currentUser,otherUser);

  await db.collection("messages").doc(id).collection("msgs").add({
    from:currentUser,
    text:msg.value,
    time:Date.now()
  });

  msg.value="";
}

/* CHAT LIST */
function loadChats(){
  db.collection("messages").onSnapshot(snap=>{
    let chats = {};

    snap.forEach(doc=>{
      let ids = doc.id.split("_");
      if(ids.includes(currentUser)){
        let other = ids[0]===currentUser ? ids[1] : ids[0];
        chats[other]=true;
      }
    });

    chatList.innerHTML="";

    Object.keys(chats).forEach(uid=>{
      db.collection("users").doc(uid).get().then(doc=>{
        chatList.innerHTML += `
          <div class="item" onclick="openChat('${uid}','${doc.data().username}')">
            ${doc.data().username}
          </div>
        `;
      });
    });
  });
}
