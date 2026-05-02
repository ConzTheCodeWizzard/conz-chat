// NAV
function show(id){
  document.querySelectorAll(".screen").forEach(s=>s.classList.remove("active"));
  document.getElementById(id).classList.add("active");
}

// LOGIN
function login(){
  const email = document.getElementById("loginEmail").value;
  const password = document.getElementById("loginPassword").value;

  if(!email || !password){
    alert("Enter email and password");
    return;
  }

  auth.signInWithEmailAndPassword(email, password)
  .then(()=>{
    show("home");
  })
  .catch(e=>{
    alert(e.message);
  });
}

// SIGNUP (FIXED USERNAME)
function signup(){
  const username = document.getElementById("signupUsername").value;
  const email = document.getElementById("signupEmail").value;
  const password = document.getElementById("signupPassword").value;

  if(!username || !email || !password){
    alert("Fill all fields");
    return;
  }

  auth.createUserWithEmailAndPassword(email, password)
  .then(userCred=>{
    return db.collection("users").doc(userCred.user.uid).set({
      username: username,
      created: Date.now()
    });
  })
  .then(()=>{
    show("home");
  })
  .catch(e=>{
    alert(e.message);
  });
}

// LOGOUT
function logout(){
  auth.signOut();
  show("welcome");
}

// SEARCH USERS
function searchUsers(){
  const q = document.getElementById("searchInput").value.toLowerCase();
  const results = document.getElementById("results");

  results.innerHTML = "";

  db.collection("users").get().then(snapshot=>{
    snapshot.forEach(doc=>{
      const user = doc.data();

      if(user.username.toLowerCase().includes(q)){
        const div = document.createElement("div");
        div.style.padding = "10px";
        div.innerText = user.username;
        results.appendChild(div);
      }
    });
  });
}

// PROFILE
function openProfile(){
  alert("Profile next step");
}
