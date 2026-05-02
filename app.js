let currentUser = null;
let currentChat = null;
let typingTimeout = null;

function show(id){
  document.querySelectorAll(".screen").forEach(s => s.classList.remove("active"));
  document.getElementById(id).classList.add("active");
}

auth.onAuthStateChanged(async user => {
  if(user){
    currentUser = user;

    await db.collection("users").doc(user.uid).set({
      online: true,
      lastSeen: Date.now()
    }, { merge:true });

    loadChats();
    show("home");
  } else {
    show("welcome");
  }
});

window.addEventListener("beforeunload", () => {
  if(currentUser){
    db.collection("users").doc(currentUser.uid).update({
      online:false,
      lastSeen:Date.now()
    });
  }
});

async function signup(){
  const u = su_user.value;
  const p = su_pass.value;

  const res = await auth.createUserWithEmailAndPassword(u+"@app.com", p);

  await db.collection("users").doc(res.user.uid).set({
    username: u,
    online: true,
    created: Date.now()
  });
}

async function login(){
  const u = li_user.value;
  const p = li_pass.value;

  await auth.signInWithEmailAndPassword(u+"@app.com", p);
}

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

          div.innerHTML = `${m.text}<br><small>${status}</small>`;
          messages.appendChild(div);

          if(m.to === currentUser.uid && !m.read){
            doc.ref.update({ read:true, delivered:true });
          }
        }
      });

      messages.scrollTop = messages.scrollHeight;
    });
}

function send(){
  let text = msg.value;
  if(!text) return;

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

msg.addEventListener("input", () => {
  if(!currentChat) return;

  db.collection("typing").doc(currentUser.uid+"_"+currentChat).set({
    from: currentUser.uid,
    to: currentChat,
    typing:true
  });

  clearTimeout(typingTimeout);
  typingTimeout = setTimeout(() => {
    db.collection("typing").doc(currentUser.uid+"_"+currentChat).delete();
  }, 1500);
});

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
