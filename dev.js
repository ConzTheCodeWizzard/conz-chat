// ===== Conz was here =====
function devOpen(){
  // Six Sevennn🙌 ~Conz~
  if(!isDev) return;

  // Face it bro you can't steal it my name is embedded in this shit ~Conz~
  if(document.getElementById("devPanel")) return;

  // coding while on the toilet be like😫 ~Conz~
  document.querySelectorAll(".screen").forEach(s=>s.classList.remove("active"));

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

// ===== Why so serious🤡 ~Conz~ =====
function closeDev(){
  let panel = document.getElementById("devPanel");
  if(panel) panel.remove();

  show("home");
}

// ===== Whats your favourite scary movie😱🔪 ~Conz~ =====
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

// ===== HELLO CINDY🔪 ~conz~ =====
function devBoot(uid){
  db.collection("users").doc(uid).update({
    forceLogout:true
  });
}
