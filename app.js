let currentUser = null;
let currentChat = null;

/* 🔁 SCREEN SWITCH */
function show(id){
  document.querySelectorAll(".screen").forEach(s => s.classList.remove("active"));
  document.getElementById(id).classList.add("active");
}

/* 🔐 AUTH (NO AUTO REDIRECTS ANYMORE) */
auth.onAuthStateChanged(user => {
  if(user){
    currentUser = user;
  } else {
    currentUser = null;
  }
});

/* 🚪 LOGOUT */
function logout(){
  auth.signOut().then(()=>{
    currentUser = null;
    show("welcome");
  });
}

/* 🆕 SIGNUP */
async function signup(){
  try {
    const u = document.getElementById("su_user").value.trim();
    const p = document.getElementById("su_pass").value.trim();

    if(!u || !p){
      alert("Fill everything");
      return;
    }

    const res = await auth.createUserWithEmailAndPassword(u + "@app.com", p);

    // save user profile
    await db.collection("users").doc(res.user.uid).set({
      username: u,
      created: Date.now(),
      online: true
    });

    currentUser = res.user;

    alert("Account created ✅");

    loadChats();
    show("home");   // 🔥 FORCE NAV

  } catch(e){
    alert(e.message);
    console.error(e);
  }
}

/* 🔑 LOGIN */
async function login(){
  try {
    const u = document.getElementById("li_user").value.trim();
    const p = document.getElementById("li_pass").value.trim();

    if(!u || !p){
      alert("Fill everything");
      return;
    }

    const res = await auth.signInWithEmailAndPassword(u + "@app.com", p);

    currentUser = res.user;

    loadChats();
    show("home");   // 🔥 FORCE NAV

  } catch(e){
    alert(e.message);
    console.error(e);
  }
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
            ${data.username || "User"}
            <span style="float:right;color:${data.online ? 'lime' : 'gray'};">●</span>
          `;

          div.onclick = () => openChat(uid, data.username || "User");

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

          if(m.to === currentUser.uid && !m.read){
            doc.ref.update({ read:true, delivered:true });
          }
        }
      });

      messages.scrollTop = messages.scrollHeight;
    });
}

/* 📤 SEND */
function send(){
  let text = document.getElementById("msg").value;

  if(!text || !currentChat) return;

  db.collection("messages").add({
    from: currentUser.uid,
    to: currentChat,
    text,
    time: Date.now(),
    delivered:false,
    read:false
  });

  document.getElementById("msg").value = "";
}

/* 🔍 SEARCH */
function openSearch(){
  show("search");
}

function searchUsers(){
  let q = document.getElementById("searchInput").value;

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
