function devOpen(){
  show("home");

  let panel = document.createElement("div");
  panel.className = "screen active";

  panel.innerHTML = `
    <div class="topbar">
      <button onclick="show('home');this.parentElement.parentElement.remove()">Back</button>
      <span>Dev Panel</span>
    </div>

    <input placeholder="Search user..." oninput="devSearch(this.value)">
    <div id="devResults"></div>

    <h3>Broadcast</h3>
    <button onclick="broadcast('problem')">⚠️ Problem</button>
    <button onclick="broadcast('update')">🚀 Update</button>
  `;

  document.body.appendChild(panel);
}

function devSearch(q){
  db.collection("users").get().then(snap=>{
    devResults.innerHTML = "";

    snap.forEach(doc=>{
      let u = doc.data();
      if(!u.username.toLowerCase().includes(q.toLowerCase())) return;

      let div = document.createElement("div");

      div.innerHTML = `
        ${u.username}
        <button onclick="devPrincess('${doc.id}')">👑</button>
        <button onclick="devFreeze('${doc.id}')">❄️</button>
        <button onclick="devKick('${doc.id}')">🚪</button>
      `;

      devResults.appendChild(div);
    });
  });
}

function devPrincess(uid){
  db.collection("users").doc(uid).update({
    themeOverride:{color:"#ff69b4",message:"ooo pretty pink 💕"}
  });
}

function devFreeze(uid){
  db.collection("users").doc(uid).update({
    freezeUntil:Date.now()+30000
  });
}

function devKick(uid){
  db.collection("users").doc(uid).update({
    forceLogout:true
  });
}

function broadcast(type){
  let msg = type==="problem"
    ? "System: some features are currently bugged, fix coming soon ~Conz~"
    : "System: new update coming soon ~Conz~";

  db.collection("messages").add({
    text: msg,
    type: "system",
    time: Date.now()
  });
}
