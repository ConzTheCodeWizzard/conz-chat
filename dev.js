// ===== DEV PANEL =====
function devOpen(){
  // 🔒 ONLY DEV CAN OPEN
  if(!isDev) return;

  // prevent duplicates
  if(document.getElementById("devPanel")) return;

  let panel = document.createElement("div");
  panel.id = "devPanel";
  panel.className = "screen active";

  panel.innerHTML = `
    <div class="topbar center">
      <button class="plainBtn" onclick="closeDev()">Back</button>
      <span>Dev Panel</span>
    </div>

    <input 
      id="devSearchInput" 
      placeholder="Search user..." 
      oninput="devSearch(this.value)"
    >

    <div id="devResults"></div>
  `;

  document.body.appendChild(panel);
}

// ===== CLOSE DEV =====
function closeDev(){
  let panel = document.getElementById("devPanel");
  if(panel) panel.remove();

  show("home");
}

// ===== SEARCH USERS =====
function devSearch(q){
  let resultsBox = document.getElementById("devResults");
  if(!resultsBox) return;

  resultsBox.innerHTML = "";

  db.collection("users").get().then(snap=>{
    snap.forEach(doc=>{
      let u = doc.data();

      if(!u.username.toLowerCase().includes(q.toLowerCase())) return;

      let div = document.createElement("div");

      div.innerHTML = `
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <span>${u.username}</span>
          <button onclick="devBoot('${doc.id}')">🚪 Boot</button>
        </div>
      `;

      resultsBox.appendChild(div);
    });
  });
}

// ===== BOOT USER =====
function devBoot(uid){
  db.collection("users").doc(uid).update({
    forceLogout:true
  });
}
