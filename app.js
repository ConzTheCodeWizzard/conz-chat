let currentUser = null;
let currentChat = null;

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

    // Only go home if currently on welcome
    if(document.getElementById("welcome").classList.contains("active")){
      show("home");
    }

  } else {
    show("welcome");
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

    await auth.createUserWithEmailAndPassword(u + "@app.com", p);

    alert("Account created ✅");

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

    await auth.signInWithEmailAndPassword(u + "@app.com", p);

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
