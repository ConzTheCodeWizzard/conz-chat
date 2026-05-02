let currentUser = null;
let currentChatUser = null;
let fabOpen = false;
let isAdmin = false;

// NAV
function show(id){
  document.querySelectorAll(".screen").forEach(s=>s.classList.remove("active"));
  document.getElementById(id).classList.add("active");

  if (fabMenu){
    fabMenu.style.display = "none";
    fabOpen = false;
  }
}

// AUTH
auth.onAuthStateChanged(user=>{
  if(user){
    currentUser = user;

    // 🔥 SHOW UID ON SCREEN
    alert("Your UID: " + user.uid);

    db.collection("users").doc(user.uid).get().then(doc=>{
      let data = doc.data() || {};

      // BAN
      if(data.banned === true){
        alert("You are banned.");
        auth.signOut();
        return;
      }

      // 🔥 FORCE ADMIN IF USERNAME = Borg
      if(data.username === "Borg"){
        isAdmin = true;

        let devBtn = document.getElementById("devBtn");
        let title = document.getElementById("appTitle");

        if(devBtn) devBtn.style.display = "block";
        if(title) title.innerText = "ConzChat DEV";

        alert("ADMIN MODE ACTIVE");
      }

      loadTheme();
      loadChats();
    });

  } else {
    show("welcome");
  }
});
