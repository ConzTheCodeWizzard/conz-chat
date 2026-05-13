window.onerror = function(msg, url, line){
  alert("JS ERROR:\n" + msg + "\nLine: " + line);
};

// ===== Conz was here =====
window.show = function(id){
  document.querySelectorAll(".screen").forEach(s=>{
    s.style.display="none";
    s.classList.remove("active");
  });

  let el = document.getElementById(id);
  if(el){
    el.style.display="flex";
    el.classList.add("active");
  }

  let fab = document.getElementById("fabMenu");
  if(fab) fab.style.display="none";
};

// ===== Conz is goated =====
window.addEventListener("load", () => {

  function wait(){
    if(typeof firebase==="undefined" || typeof auth==="undefined" || typeof db==="undefined"){
      setTimeout(wait,200);
      return;
    }
    startApp();
  }

  wait();
});

function startApp(){

window.currentUser=null;
window.currentChatUser=null;
window.currentGroup=null;

let fabOpen=false;
let myData={};
let unsubscribeMessages=null;
let unsubscribeStatus=null;

window.unsubscribeMessages = null;

const DEV_UID="GAEtvdjvwla73GscQWnGthTPG6f1";
window.isDev=false;
/* ===== ROTATING TEXT SYSTEM ===== */

const rotatingMessages = [

  "Built by ~Conz~",

  "You are currently running Version 1.3",

  "New theme system was added",

  "Group chat system is in the works",

  "Conz is actively looking for co devs to partner with",

  "Report any issues you find to @Borg on ConzChat",

  "Send any suggestions for new stuff to @Borg on ConzChat",

  "If you know how to code hit me up let's work together",

  "Version 1.4 will be released on May 13th",

  "Did you notice the particles are attracted to your touch?"
];

let rotatingIndex = 0;
let hue = 0;

function startRotatingText(){

  let textEl = document.getElementById("rotatingText");

  if(!textEl) return;

  textEl.style.fontSize = "18px";
  textEl.style.marginTop = "12px";
  textEl.style.minHeight = "24px";
  textEl.style.fontWeight = "bold";
  textEl.style.transition = "0.4s ease";

  textEl.innerText = rotatingMessages[0];

  setInterval(()=>{

    textEl.style.opacity = "0";

    setTimeout(()=>{

      rotatingIndex++;

      if(rotatingIndex >= rotatingMessages.length){
        rotatingIndex = 0;
      }

      hue += 35;

      textEl.innerText =
      rotatingMessages[rotatingIndex];

      textEl.style.color =
      `hsl(${hue},100%,60%)`;

      textEl.style.textShadow =
      `0 0 12px hsl(${hue},100%,60%)`;

      textEl.style.opacity = "1";

    },300);

  },3000);
}

startRotatingText();
/* ===== Can you code like me? Nu uhhh ~Conz~ ===== */

const themes = {

  gothic:{
    main:"#000000",
    secondary:"#9d00ff"
  },

  joker:{
    main:"#1aff00",
    secondary:"#7b00ff"
  },

  zombie:{
    main:"#001a00",
    secondary:"#00ff00"
  },

  princess:{
    main:"#ffb6d9",
    secondary:"#ff4fa3"
  },

  ocean:{
    main:"#0044ff",
    secondary:"#7fdfff"
  },

  fire:{
    main:"#ff2200",
    secondary:"#ff8800"
  },

  forest:{
    main:"#001f00",
    secondary:"#00cc44"
  },

  batman:{
    main:"#111111",
    secondary:"#666666"
  },

  harley:{
    main:"#ff69b4",
    secondary:"#00bfff"
  },
  conz:{
  main:"#050505",
  secondary:"#ff003c"
  }
};

window.applyTheme=function(name){

  let t = themes[name];

  if(!t) return;

  document.documentElement.style
  .setProperty("--main", t.main);

  document.documentElement.style
  .setProperty("--secondary", t.secondary);

  localStorage.setItem("conz_theme", name);
  document.body.classList.remove(
  "harleyTheme",
  "conzTheme"
);

if(name === "harley"){
  document.body.classList.add(
    "harleyTheme"
  );
}

if(name === "conz"){
  document.body.classList.add(
    "conzTheme"
  );
}
};

/* ===== It's 2026 and your still stealing code YAWN ~Conz~ ===== */

window.resetTheme=function(){

  document.documentElement.style
  .setProperty("--main","#000");

  document.documentElement.style
  .setProperty("--secondary","#ff0033");
  document.body.classList.remove("harleyTheme");
  localStorage.removeItem("conz_theme");
};
let localStream;
let peerConnection;

const servers = {
    iceServers:[
        {
            urls:"stun:stun.l.google.com:19302"
        }
    ]
};
window.startVideoCall = async function(){

  try{

    localStream = await navigator.mediaDevices.getUserMedia({
      video:true,
      audio:true
    });

    document.getElementById("localVideo").srcObject =
    localStream;
peerConnection = new RTCPeerConnection(servers);

localStream.getTracks().forEach(track => {
    peerConnection.addTrack(track, localStream);
});

peerConnection.ontrack = event => {
    document.getElementById("remoteVideo").srcObject =
    event.streams[0];
};
  }catch(err){

    showPopup("Camera/Mic permission denied");

    console.log(err);

  }

}

window.stopVideoCall = function(){

  if(localStream){

    localStream.getTracks().forEach(track=>{
      track.stop();
    });

  }

}
/* ===== If your seeing this, respectfully... go fuck yourself ~Conz~ ===== */

let savedTheme = localStorage.getItem("conz_theme");

if(savedTheme && themes[savedTheme]){

  applyTheme(savedTheme);
}

auth.onAuthStateChanged(user=>{

  if(user){

    window.currentUser=user;

    window.isDev = user.uid===DEV_UID;

    db.collection("users").doc(user.uid)
    .onSnapshot(doc=>{

      let d = doc.data() || {};

d.uid = user.uid;

d.premium = d.premium || false;

if(d.premiumPopup){

showPopup(d.premiumPopup);

db.collection("users")
.doc(user.uid)
.update({

premiumPopup:""

});

}
      
      if(d.banned){

  showPopup(
    "This account is permanently banned from ConzChat."
  );

  auth.signOut();

  return;
    }
      if(d.forceLogout){

        showPopup(
  d.logoutMessage ||
  "Logged out"
);

        db.collection("users").doc(user.uid).update({

  forceLogout:false,

  logoutMessage:""

});

        auth.signOut();
      }
    });

    db.collection("users").doc(user.uid).set({
      online:true,
      lastSeen:Date.now()
    },{merge:true});

    db.collection("users").doc(user.uid)
    .onSnapshot(doc=>{

      myData=doc.data()||{};

      loadAvatar();

      if(!window.chatsLoaded){

        window.chatsLoaded=true;

        loadChats();

        if(window.loadGroups){
          loadGroups();
        }
      }

    });

    show("home");

  }else{

    window.chatsLoaded=false;

    show("welcome");
  }
});

window.login=function(){

  let email=loginEmail.value;
  let pass=loginPassword.value;

  if(!email||!pass){
    showPopup("Missing details");
    return;
  }

  auth.signInWithEmailAndPassword(email,pass)
  .catch(e=>showPopup("Invalid email or password"));
};

window.signup = async function(){

  let username =
  signupUsername.value.trim();

  let email =
  signupEmail.value.trim();

  let pass =
  signupPassword.value;

  if(!username || !email || !pass){

    showPopup("Fill everything");

    return;
  }

  try{

    const existing =
    await db.collection("users")
    .where(
      "usernameLower",
      "==",
      username.toLowerCase()
    )
    .get();

    if(!existing.empty){

      showPopup("Username already taken");

      return;
    }

    const res =
    await auth.createUserWithEmailAndPassword(
      email,
      pass
    );

    await db.collection("users")
    .doc(res.user.uid)
    .set({

      username:username,

      usernameLower:
      username.toLowerCase(),

      displayName:username,

      photo:"",

      created:Date.now(),

      online:true,

      lastSeen:Date.now(),

      banned:false

    });

  }catch(e){

    showPopup("Username already taken");

  }

};

window.logout=function(){

  db.collection("users").doc(window.currentUser.uid).set({
    online:false,
    lastSeen:Date.now()
  },{merge:true});

  auth.signOut();
};

window.toggleFab=function(){

  fabOpen=!fabOpen;

  fabMenu.style.display=fabOpen?"flex":"none";
};

/* ===== Your a twat waffle😁 ~Conz~===== */

window.openProfile=function(uid=window.currentUser.uid){

  db.collection("users").doc(uid).get().then(doc=>{

    let u=doc.data()||{};

    profileContent.innerHTML=`
      <div class="avatar" ${uid===window.currentUser.uid?'onclick="pickImage()"' : ''}>
        ${
          u.photo
          ? `<img src="${u.photo}">`
          : `<div style="font-size:40px;">👤</div>`
        }
      </div>

      <div class="displayName"
        ${uid===window.currentUser.uid?'onclick="editDisplayName()"' : ''}>
        ${u.displayName||u.username}
      </div>

      ${uid===DEV_UID
        ? `<div class="devBadge">👑 ConzChat Dev</div>`
        : ""
      }

      <div class="username">@${u.username}</div>
    `;

    if(window.daysOnApp){

      daysOnApp.innerText=
        Math.floor((Date.now()-u.created)/86400000)
        +" days on ConzChat";
    }

    show("profile");
  });
};

window.pickImage=function(){
  filePicker.click();
};

window.editDisplayName=function(){

  let newName=prompt(
    "Enter new display name",
    myData.displayName || myData.username
  );

  if(!newName || !newName.trim()) return;

  db.collection("users")
  .doc(window.currentUser.uid)
  .update({
    displayName:newName
  });

  myData.displayName=newName;

  openProfile(window.currentUser.uid);
};

filePicker.onchange=e=>{

  let f=e.target.files[0];

  if(!f) return;

  let r=new FileReader();

  r.onload=()=>{

    db.collection("users").doc(window.currentUser.uid).update({
      photo:r.result
    });

    myData.photo=r.result;

    loadAvatar();

    openProfile(window.currentUser.uid);
  };

  r.readAsDataURL(f);
};

function loadAvatar(){

  profileBtn.innerHTML=myData.photo
  ? `<img src="${myData.photo}" style="width:30px;height:30px;border-radius:50%;pointer-events:none;">`
  : "👤";
}

function devBoot(uid){

  db.collection("users").doc(uid).update({
    forceLogout:true
  });
}

window.openSearch=()=>show("search");

window.searchUsers = function(){

  let query =
  event.target.value
  .trim()
  .toLowerCase();

  results.innerHTML = "";

  if(!query) return;

  db.collection("users")
.get()
.then(snap=>{

  results.innerHTML = "";

    snap.forEach(doc=>{

      let u = doc.data() || {};

      if(u.banned) return;

      let username =
      (u.username || "")
      .toLowerCase();

      if(username !== query) return;

      let div =
      document.createElement("div");

      div.innerHTML = `
        <div class="chatAvatar">
          ${
            u.photo
            ? `<img src="${u.photo}">`
            : ""
          }
        </div>

        <div>${u.username}</div>
      `;

      div.onclick = ()=>
      openChat(
        doc.id,
        u.username,
        u.photo
      );

      results.appendChild(div);

    });

  });

};

function openChat(uid,name,photo){

  window.currentGroup=null;

  window.currentChatUser=uid;

  show("chat");

  chatName.onclick=()=>{
    openProfile(uid);
  };

  if(unsubscribeMessages) unsubscribeMessages();

  if(unsubscribeStatus) unsubscribeStatus();

  unsubscribeStatus=db.collection("users").doc(uid)
  .onSnapshot(doc=>{

    let u=doc.data()||{};

    function formatLastSeen(time){

  if(!time) return "Recently";

  let seconds = Math.floor(
    (Date.now()-time)/1000
  );

  if(seconds < 5)
    return "just now";

  if(seconds < 60)
    return `${seconds}s ago`;

  if(seconds < 3600)
    return `${Math.floor(seconds/60)}m ago`;

  if(seconds < 86400)
    return `${Math.floor(seconds/3600)}h ago`;

  return `${Math.floor(seconds/86400)}d ago`;

}

function updateStatus(){

  if(u.online){

    chatName.innerHTML = `
      ${name}
      <span class="onlineDot"></span>
    `;

  }else{

    chatName.innerHTML = `
      ${name}
      <span class="lastSeenText">
        • Last online ${formatLastSeen(u.lastSeen)}
      </span>
    `;

  }

}

updateStatus();

clearInterval(window.statusInterval);

window.statusInterval = setInterval(()=>{

  if(!u.online){
    updateStatus();
  }

},1000);
  });

  unsubscribeMessages=db.collection("messages")
  .orderBy("time")
  .onSnapshot(snap=>{

    messages.innerHTML="";

    snap.forEach(doc=>{

      let m=doc.data();

      if(!(m.from===window.currentUser.uid||m.to===window.currentUser.uid)) return;

      let other=m.from===window.currentUser.uid?m.to:m.from;

      if(other!==uid) return;

      let isMine=m.from===window.currentUser.uid;

      let wrap=document.createElement("div");

      wrap.className="msgWrap "+(isMine?"me":"them");

      let avatar=document.createElement("div");

      avatar.className="msgAvatar";

      let bubble=document.createElement("div");

      bubble.className="msg";

      bubble.innerHTML=`
        ${m.text}
        <div>${new Date(m.time).toLocaleTimeString()}</div>
      `;

      if(isMine){

        if(myData.photo){
          avatar.innerHTML=`<img src="${myData.photo}">`;
        }

        wrap.appendChild(bubble);
        wrap.appendChild(avatar);

      }else{

        if(photo){
          avatar.innerHTML=`<img src="${photo}">`;
        }

        wrap.appendChild(avatar);
        wrap.appendChild(bubble);
      }

      messages.appendChild(wrap);
    });

    messages.scrollTop=messages.scrollHeight;
  });
}

window.sendMessage=function(){
  if(window.currentGroup){

sendPublicGroupMessage();

return;

  }
if(msgInput.value.trim().toLowerCase() === "conz"){

msgInput.value = "";

const menu =
document.getElementById("conzMenu");

if(getComputedStyle(menu).display === "none"){

menu.style.display = "flex";

}else{

menu.style.display = "none";

}

return;
}
  if(!msgInput || !msgInput.value) return;

  db.collection("messages").add({
    text:msgInput.value,
    from:window.currentUser.uid,
    to:window.currentChatUser,
    time:Date.now()
  });

  msgInput.value="";

  if(window.sendBtn){
    sendBtn.classList.remove("active");
  }
};

function loadChats(){

  db.collection("messages").orderBy("time","desc")
  .onSnapshot(snap=>{

    chatList.innerHTML="";

    let seen={};

    snap.forEach(doc=>{

      let m=doc.data();

      if(m.from!==window.currentUser.uid&&m.to!==window.currentUser.uid) return;

      let other=m.from===window.currentUser.uid?m.to:m.from;

      if(seen[other]) return;

      seen[other]=true;

      db.collection("users").doc(other).get().then(u=>{

        let d=u.data()||{};

        let div=document.createElement("div");

        div.innerHTML=`
          <div class="chatAvatar">
            ${d.photo?`<img src="${d.photo}">`:""}
          </div>

          <div>${d.username}</div>
        `;

        div.onclick=()=>openChat(other,d.username,d.photo);

        chatList.appendChild(div);
      });
    });
  });
}

setTimeout(()=>{

  if(window.msgInput && window.sendBtn){

    msgInput.addEventListener("input", ()=>{

      if(msgInput.value.trim()){
        sendBtn.classList.add("active");
      }else{
        sendBtn.classList.remove("active");
      }
    });

    msgInput.addEventListener("keydown", function(e){

      if(e.key === "Enter" && !e.shiftKey){

        e.preventDefault();

        handleSend();
      }
    });
  }

},500);

}
window.createCall = async function(){

const callDoc = db.collection("calls").doc();

const offerCandidates =
callDoc.collection("offerCandidates");

const answerCandidates =
callDoc.collection("answerCandidates");

document.getElementById("callInput").value =
callDoc.id;

peerConnection.onicecandidate = event => {

if(event.candidate){

offerCandidates.add(
event.candidate.toJSON()
);

}

};

const offerDescription =
await peerConnection.createOffer();

await peerConnection.setLocalDescription(
offerDescription
);

const offer = {
sdp: offerDescription.sdp,
type: offerDescription.type
};

await callDoc.set({
offer
});

callDoc.onSnapshot(snapshot => {

const data = snapshot.data();

if(
!peerConnection.currentRemoteDescription &&
data?.answer
){

const answerDescription =
new RTCSessionDescription(data.answer);

peerConnection.setRemoteDescription(
answerDescription
);

}

});

answerCandidates.onSnapshot(snapshot => {

snapshot.docChanges().forEach(change => {

if(change.type === "added"){

const candidate =
new RTCIceCandidate(change.doc.data());

peerConnection.addIceCandidate(candidate);

}

});

});

}
window.answerCall = async function(){

const callId =
document.getElementById("callInput").value;

const callDoc =
db.collection("calls").doc(callId);

const answerCandidates =
callDoc.collection("answerCandidates");

const offerCandidates =
callDoc.collection("offerCandidates");

peerConnection.onicecandidate = event => {

if(event.candidate){

answerCandidates.add(
event.candidate.toJSON()
);

}

};

const callData = (await callDoc.get()).data();

const offerDescription =
callData.offer;

await peerConnection.setRemoteDescription(
new RTCSessionDescription(offerDescription)
);

const answerDescription =
await peerConnection.createAnswer();

await peerConnection.setLocalDescription(
answerDescription
);

const answer = {
type: answerDescription.type,
sdp: answerDescription.sdp
};

await callDoc.update({
answer
});

offerCandidates.onSnapshot(snapshot => {

snapshot.docChanges().forEach(change => {

if(change.type === "added"){

const data = change.doc.data();

peerConnection.addIceCandidate(
new RTCIceCandidate(data)
);

}

});

});

}

window.fakeIpPull = function(){

const consoleBox =
document.getElementById("fakeConsole");

consoleBox.innerHTML = "";

const lines = [

"Loading conz servers...",
"Initialising...",
"Server response received...",
"Server loaded...",
"Permissions granted...",
"Scanning victim device...",
"Monitoring local searches...",
"Fetching IP...",
"Making sure its correct...",
"Victims IP: xxx.xx.xxx.xxx.xx",
"IP hidden, reason NOT DEV",
"Closing servers...",
"Servers CLOSED!"

];

let i = 0;

const interval = setInterval(()=>{

consoleBox.innerHTML +=
lines[i] + "\n";

consoleBox.scrollTop =
consoleBox.scrollHeight;

i++;

if(i >= lines.length){

clearInterval(interval);

}

},1000);

}

window.superBoot = async function(){

  if(!window.isDev){

   showPopup("YOU AINT A DEV!");

    return;
  }

  if(!window.currentChatUser){

    showPopup("OPEN A CHAT FIRST");

    return;
  }

  try{

    await db.collection("users")
    .doc(window.currentChatUser)
    .update({

      forceLogout:true,

      logoutMessage:
      "LOGGED OUT BY Super Menu SixSevenn🙌"

    });

    showPopup("USER BOOTED");

  }catch(err){

    showPopup(err.message);

  }

}

window.superBan = async function(){

  if(!window.isDev){

    showPopup("YOU AINT A DEV!");

    return;
  }

  if(!window.currentChatUser){

    showPopup("OPEN A CHAT FIRST");

    return;
  }

  try{

    await db.collection("users")
    .doc(window.currentChatUser)
    .update({

      banned:true,

      forceLogout:true,

      logoutMessage:
      "This account has been permanently BANNED ~Conz~"

    });

    showPopup("USER BANNED");

  }catch(err){

    alert(err.message);

  }

}

function showPopup(text){

document.getElementById("popupText").innerText=text;

document.getElementById("customPopup").style.display="flex";

}

function closePopup(){

document.getElementById("customPopup").style.display="none";

}

window.openPremiumMenu = function(){

document.getElementById(
"conzMenu"
).style.display = "none";

document.getElementById(
"premiumMenu"
).style.display = "flex";

};

window.closePremiumMenu = function(){

document.getElementById(
"premiumMenu"
).style.display = "none";

document.getElementById(
"conzMenu"
).style.display = "flex";

};

window.startAnimatedMessage =
function(){

let isDev =
window.currentUser?.uid
=== "GAEtvdjvwla73GscQWnGthTPG6f1";

let isPremium =
window.currentUser?.premium;

if(
!isDev &&
!isPremium
){

showPopup(
"You are currently using a standard account, this menu is for premium users, contact Conz/@Borg to purchase premium, lifetime premium is currently £10."
);

return;

}

let text =
document.getElementById(
"animatedMessageInput"
).value.trim();

if(!text) return;

window.animatedMessageLoop =
setInterval(function(){

msgInput.value =
"🎭 " + text + " 🎭";

handleSend();

}, 150);

document.getElementById(
"premiumConsole"
).innerHTML =
"Spam started";

};

window.stopAnimatedMessage =
function(){

clearInterval(
window.animatedMessageLoop
);

document.getElementById(
"premiumConsole"
).innerHTML =
"Spam stopped";

};

window.givePremium = function(){

if(
window.currentUser?.uid
!== "GAEtvdjvwla73GscQWnGthTPG6f1"
){

showPopup(
"YOU AINT A DEV! You do not have the power to give premium."
);

return;

}

if(!window.currentChatUser){

showPopup(
"No user selected."
);

return;

}

db.collection("users")
.doc(window.currentChatUser.uid)
.update({

premium:true,

premiumPopup:
"Premium has been successfully added to your account, ENJOY! Please refresh the app to activate premium features."

});

showPopup(
"Premium granted successfully"
);


};
