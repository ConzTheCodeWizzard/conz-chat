function devOpen(){
  let panel = document.createElement("div");
  panel.className = "screen active";

  panel.innerHTML = `
    <div class="topbar">
      <button onclick="closeDev()">Back</button>
      <span>Dev Panel</span>
    </div>

    <input id="devSearchInput" placeholder="Search user..." oninput="devSearch(this.value)">
    <div id="devResults"></div>
  `;

  document.body.appendChild(panel);
}

function closeDev(){
  document.querySelectorAll(".screen").forEach(s=>s.classList.remove("active"));
  show("home");

  let panel = document.querySelector(".screen.active");
  if(panel && !panel.id){
    panel.remove();
  }
}

function devSearch(q){
  db.collection("users").get().then(snap=>{
    devResults.innerHTML = "";

    snap.forEach(doc=>{
      let u = doc.data();

      if(!u.username.toLowerCase().includes(q.toLowerCase())) return;

      let div = document.createElement("div");

      div.innerHTML = `
        <div style="display:flex;justify-content:space-between">
          <span>${u.username}</span>
          <button onclick="devBoot('${doc.id}')">🚪 Boot</button>
        </div>
      `;

      devResults.appendChild(div);
    });
  });
}

function devBoot(uid){
  db.collection("users").doc(uid).update({
    forceLogout:true
  });
}
