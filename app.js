window.onerror = function(msg, url, line){
  alert("JS ERROR:\n" + msg + "\nLine: " + line);
};

// ===== WAIT FOR FIREBASE SAFELY =====
window.addEventListener("load", () => {

  function waitForFirebase(){
    if (typeof firebase === "undefined" || typeof auth === "undefined" || typeof db === "undefined") {
      setTimeout(waitForFirebase, 300);
      return;
    }

    startApp();
  }

  waitForFirebase();
});

function startApp(){

// ===== GLOBAL =====
let currentUser = null;
let currentChatUser = null;
let fabOpen = false;
let myData = {};
let unsubscribeMessages = null;
let unsubscribeStatus = null;

const DEV_UID = "GAEtvdjvwla73GscQWnGthTPG6f1";
let isDev = false;

// ===== NAV =====
window.show = function(id){

  const screens = document.querySelectorAll(".screen");

  for(let i = 0; i < screens.length; i++){
    screens[i].style.display = "none";
    screens[i].classList.remove("active");
  }

  const target = document.getElementById(id);

  if(target){
    target.style.display = "flex";
    target.classList.add("active");
  }

  const fabMenu = document.getElementById("fabMenu");
  if(fabMenu){
    fabMenu.style.display="none";
  }

  fabOpen=false;
};

// ===== AUTH =====
auth.onAuthStateChanged(user=>{
  if(user){
    currentUser=user;
    isDev = user.uid === DEV_UID;

    const topTitle = document.getElementById("topTitle");
    if(topTitle){
      topTitle.innerText = isDev ? "ConzChat DEV" : "ConzChat";
    }

    const devBtn = document.getElementById("devBtn");
    if(devBtn){
      devBtn.style.display = isDev ? "block" : "none";
    }

    db.collection("users").doc(user.uid).set({
      online:true,
      lastSeen:Date.now()
    },{merge:true});

    window.addEventListener("beforeunload", ()=>{
      db.collection("users").doc(user.uid).set({
        online:false,
        lastSeen:Date.now()
      },{merge:true});
    });

    db.collection("users").doc(user.uid)
    .onSnapshot(doc=>{
      let d = doc.data() || {};
      if(d.forceLogout){
        alert("😁YOU GOT BOOTED BY CONZ😁");
        db.collection("users").doc(user.uid).update({ forceLogout:false });
        auth.signOut();
      }
    });

    db.collection("users").doc(user.uid).get().then(doc=>{
      myData = doc.data() || {};
      applyTheme();
      loadChats();
      loadAvatar();
    });

  } else {
    show("welcome");
  }
});

// ===== AUTH FUNCTIONS (DEBUG VERSION) =====
window.login = function(){
  alert("LOGIN CLICKED");

  const email = document.getElementById("loginEmail").value;
  const password = document.getElementById("loginPassword").value;

  if(!email || !password){
    alert("Missing email or password");
    return;
  }

  auth.signInWithEmailAndPassword(email, password)
  .then(()=>{
  alert("LOGIN SUCCESS");
  show("home");
})
  .catch(e=>{
    alert("ERROR: " + e.message);
  });
};

window.signup = function(){
  const username = document.getElementById("signupUsername").value;
  const email = document.getElementById("signupEmail").value;
  const password = document.getElementById("signupPassword").value;

  auth.createUserWithEmailAndPassword(email, password)
  .then(res=>{
    return db.collection("users").doc(res.user.uid).set({
      username:username,
      displayName:username,
      photo:"",
      created:Date.now(),
      mainColor:"#000000",
      secondaryColor:"#ff0000",
      online:true,
      lastSeen:Date.now()
    });
  });
};

window.logout = function(){
  db.collection("users").doc(currentUser.uid).set({
    online:false,
    lastSeen:Date.now()
  },{merge:true});

  auth.signOut();
};

// ===== FAB =====
window.toggleFab = function(){
  fabOpen=!fabOpen;
  const fabMenu = document.getElementById("fabMenu");
  if(fabMenu){
    fabMenu.style.display=fabOpen?"flex":"none";
  }
};

// ===== THEME =====
function applyTheme(){
  let main = myData.mainColor || "#000000";
  let secondary = myData.secondaryColor || "#ff0000";

  document.documentElement.style.setProperty('--main', main);
  document.documentElement.style.setProperty('--secondary', secondary);

  const mainPicker = document.getElementById("mainColorPicker");
  const secPicker = document.getElementById("secondaryColorPicker");

  if(mainPicker) mainPicker.value = main;
  if(secPicker) secPicker.value = secondary;
}

window.openSettings = function(){ show("settings"); };

window.saveTheme = function(){
  const main = document.getElementById("mainColorPicker").value;
  const secondary = document.getElementById("secondaryColorPicker").value;

  myData.mainColor = main;
  myData.secondaryColor = secondary;

  db.collection("users").doc(currentUser.uid).set({
    mainColor: main,
    secondaryColor: secondary
  },{merge:true}).then(applyTheme);
};

window.resetTheme = function(){
  myData.mainColor = "#000000";
  myData.secondaryColor = "#ff0000";

  db.collection("users").doc(currentUser.uid).set({
    mainColor:"#000000",
    secondaryColor:"#ff0000"
  },{merge:true}).then(applyTheme);
};

// ===== ROTATING TEXT =====
const rotatingTexts = [
  "Built by Conz",
  "Next-gen chat",
  "Fast. Clean. Powerful.",
  "Welcome to the future",
  "Real-time messaging"
];

const rotatingColors = [
  "#00bfff",
  "#ff00ff",
  "#00ff99",
  "#ff0033",
  "#ffff00"
];

let rotateIndex = 0;

function startRotatingText(){
  const el = document.getElementById("rotatingText");
  if(!el) return;

  function update(){
    el.style.opacity = 0;

    setTimeout(()=>{
      el.innerText = rotatingTexts[rotateIndex];
      el.style.color = rotatingColors[rotateIndex];
      el.style.opacity = 1;

      rotateIndex = (rotateIndex + 1) % rotatingTexts.length;
    }, 300);
  }

  update();
  setInterval(update, 2500);
}

startRotatingText();

}
