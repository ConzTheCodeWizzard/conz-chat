function devOpen(){
  if(!isDev) return;

  let panel=document.createElement("div");
  panel.className="screen active";

  panel.innerHTML=`
    <div class="topbar">
      <button onclick="this.parentElement.parentElement.remove()">Back</button>
      <span>Dev Panel</span>
    </div>

    <input oninput="devSearch(this.value)">
    <div id="devResults"></div>
  `;

  document.body.appendChild(panel);
}

function devSearch(q){
  db.collection("users").get().then(snap=>{
    devResults.innerHTML="";
    snap.forEach(doc=>{
      let u=doc.data();
      if(!u.username.toLowerCase().includes(q.toLowerCase())) return;

      let div=document.createElement("div");
      div.innerHTML=`${u.username} <button onclick="bootUser('${doc.id}')">Boot</button>`;
      devResults.appendChild(div);
    });
  });
}
