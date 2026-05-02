let currentUser = null;
let currentChat = null;
let typingTimeout = null;

/* 🔁 SCREEN SWITCH */
function show(id){
  document.querySelectorAll(".screen").forEach(s => s.classList.remove("active"));
  document.getElementById(id).classList.add("active");
}

/* 🔐 AUTH STATE */
auth.onAuthStateChanged(async user => {
  if(user){
    currentUser = user;

    await db.collection("users").doc(user.uid).set({
      online: true,
      lastSeen: Date.now()
    }, { merge:true });

    loadChats();

    // 🔥 FIX: don't override login/signup screen
    const currentScreen = document.querySelector(".screen.active")?.id;

    if(currentScreen !== "login" && currentScreen !== "signup"){
      show("home");
    }

  } else {
    show("welcome");
  }
});

/* 🚪 LOGOUT */
function logout(){
  auth.signOut();
}

/* 🆕 SIGNUP */
async function signup(){
  const u = su_user.value;
  const p = su_pass.value;

  if(!u || !p) return alert("Fill everything");

  const res = await auth.createUserWithEmailAndPassword(u+"@app.com", p);

  await db.collection("users").doc(res.user.uid).set({
    username: u,
    online: true,
    created: Date.now()
  });
}

/* 🔑 LOGIN */
async function login(){
  const u = li_user.value;
  const p = li_pass.value;

  if(!u || !p) return alert("Fill everything");

  await auth.signInWithEmailAndPassword(u+"@app.com", p);
}

/* 💬 LOAD CHAT LIST */
function loadChats(){
  db.collection("messages")
    .orderBy("time")
    .onSnapshot(snap => {

      chatList.innerHTML = "";

      let users = new Set();

      snap.forEach(doc => {
        let m = doc.data();
        if(m.from === currentUser.uid) users.add(m.to);
        if(m.to === currentUser.uid) users.add(m.from);
      });

      users.forEach(uid => {
        db.collection("users").doc(uid).get().then(u => {
          let data = u.data();
          if(!data) return;

          let div = document.createElement("div");
          div.className = "chatItem";
          div.innerHTML = `
            ${data.username}
            <span style="float:right;color:${data.online?'lime':'gray'};">●</span>
          `;
          div.onclick = () => openChat(uid, data.username);

          chatList.appendChild(div);
        });
      });

    });
}

/* 📩 OPEN CHAT */
function openChat(uid, name){
  currentChat = uid;
  chatName.innerText = name;
  show("chat");

  db.collection("messages")
    .orderBy("time")
    .onSnapshot(snap => {

      messages.innerHTML = "";

      snap.forEach(doc => {
        let m = doc.data();

        if(
          (m.from === currentUser.uid && m.to === uid) ||
          (m.from === uid && m.to === currentUser.uid)
        ){

          let div = document.createElement("div");
          div.className = m.from === currentUser.uid ? "msgMe" : "msgOther";

          let status = m.from === currentUser.uid
            ? (m.read ? "R" : m.delivered ? "D" : "S")
            : "";

          div.innerHTML = `
            ${m.text}
            <br><small>${status}</small>
          `;

          messages.appendChild(div);

          // mark read
          if(m.to === currentUser.uid && !m.read){
            doc.ref.update({ read:true, delivered:true });
          }
        }
      });

      messages.scrollTop = messages.scrollHeight;
    });
}

/* 📤 SEND MESSAGE */
function send(){
  let text = msg.value;
  if(!text || !currentChat) return;

  db.collection("messages").add({
    from: currentUser.uid,
    to: currentChat,
    text,
    time: Date.now(),
    delivered:false,
    read:false
  });

  msg.value = "";
}

/* 🔍 SEARCH */
function openSearch(){
  show("search");
}

function searchUsers(){
  let q = searchInput.value;

  db.collection("users")
    .where("username", ">=", q)
    .get()
    .then(snap => {
      results.innerHTML = "";

      snap.forEach(doc => {
        let u = doc.data();

        let div = document.createElement("div");
        div.className = "chatItem";
        div.innerText = u.username;
        div.onclick = () => openChat(doc.id, u.username);

        results.appendChild(div);
      });
    });
}

/* 👤 PROFILE */
function openProfile(){
  if(!currentUser) return;

  db.collection("users").doc(currentUser.uid).get().then(doc => {
    let u = doc.data();
    profileName.innerText = u.username;
    show("profile");
  });
}

function saveProfile(){
  let pic = profilePic.value;

  db.collection("users").doc(currentUser.uid).update({
    photo: pic
  });

  alert("Saved");
}
