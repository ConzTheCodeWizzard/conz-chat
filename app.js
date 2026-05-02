const auth = firebase.auth();
const db = firebase.firestore();

// ===================
// SCREEN SWITCH
// ===================
function show(id){
  document.querySelectorAll('.screen').forEach(s=>s.classList.remove('active'));
  document.getElementById(id).classList.add('active');
}

// ===================
// LOGIN
// ===================
function login(){
  const email = document.getElementById("loginEmail").value;
  const password = document.getElementById("loginPassword").value;

  auth.signInWithEmailAndPassword(email, password)
  .then(()=>{
    show("home");
  })
  .catch(e=>{
    alert(e.message);
  });
}

// ===================
// SIGNUP
// ===================
function signup(){
  const email = document.getElementById("signupEmail").value;
  const password = document.getElementById("signupPassword").value;

  auth.createUserWithEmailAndPassword(email, password)
  .then(userCred=>{
    const uid = userCred.user.uid;

    // create user profile in firestore
    db.collection("users").doc(uid).set({
      username: email.split("@")[0],
      created: Date.now()
    });

    show("home");
  })
  .catch(e=>{
    alert(e.message);
  });
}

// ===================
// LOGOUT
// ===================
function logout(){
  auth.signOut().then(()=>{
    show("welcome");
  });
}

// ===================
// SEARCH USERS (FIXED)
// ===================
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

// ===================
// PROFILE (TEMP)
// ===================
function openProfile(){
  alert("Profile coming next step");
}
