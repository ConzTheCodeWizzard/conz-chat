      });
    })let currentUser=null;
let currentChat=null;
let nav=[];

function show(id){
  document.querySelectorAll(".screen").forEach(s=>s.classList.remove("active"));
  document.getElementById(id).classList.add("active");
}

function goBack(){
  show("home");
}

auth.onAuthStateChanged(u=>{
  currentUser=u;
});

function logout(){
  auth.signOut();
  show("welcome");
}

async function signup(){
  let u=su_user.value;
  let p=su_pass.value;

  let res=await auth.createUserWithEmailAndPassword(u+"@app.com",p);

  await db.collection("users").doc(res.user.uid).set({
    username:u,
    created:Date.now(),
    bio:"",
    photo:""
  });

  currentUser=res.user;
  loadChats();
  show("home");
}

async function login(){
  let u=li_user.value;
  let p=li_pass.value;

  let res=await auth.signInWithEmailAndPassword(u+"@app.com",p);

  currentUser=res.user;
  loadChats();
  show("home");
}

/* CHAT LIST */
function loadChats(){
  db.collection("messages").onSnapshot(snap=>{
    chatList.innerHTML="";
    let users=new Set();

    snap.forEach(doc=>{
      let m=doc.data();
      if(m.from===currentUser.uid) users.add(m.to);
      if(m.to===currentUser.uid) users.add(m.from);
    });

    users.forEach(uid=>{
      db.collection("users").doc(uid).get().then(u=>{
        let d=u.data();
        let div=document.createElement("div");
        div.className="chatItem";
        div.innerText=d.username;
        div.onclick=()=>openChat(uid,d.username);
        chatList.appendChild(div);
      });
    });
  });
}

/* OPEN CHAT */
function openChat(uid,name){
  currentChat=uid;
  chatName.innerText=name;
  show("chat");

  db.collection("messages").onSnapshot(snap=>{
    messages.innerHTML="";
    snap.forEach(doc=>{
      let m=doc.data();

      if((m.from===currentUser.uid&&m.to===uid)||(m.from===uid&&m.to===currentUser.uid)){
        let div=document.createElement("div");
        div.className=m.from===currentUser.uid?"msgMe":"msgOther";
        div.innerText=m.text;
        messages.appendChild(div);
      }
    });
  });
}

/* SEND */
function send(){
  if(!msg.value) return;

  db.collection("messages").add({
    from:currentUser.uid,
    to:currentChat,
    text:msg.value,
    time:Date.now()
  });

  msg.value="";
}

/* SEARCH */
function openSearch(){
  show("search");
}

function searchUsers(){
  let q=searchInput.value.toLowerCase();

  db.collection("users").onSnapshot(snap=>{
    results.innerHTML="";
    snap.forEach(doc=>{
      let u=doc.data();

      if(u.username.toLowerCase().includes(q)){
        let div=document.createElement("div");
        div.className="chatItem";
        div.innerText=u.username;
        div.onclick=()=>openChat(doc.id,u.username);
        results.appendChild(div);
      }
    });
  });
}

/* PROFILE */
function openMyProfile(){
  db.collection("users").doc(currentUser.uid).get().then(doc=>{
    let u=doc.data();

    profileName.innerText=u.username;
    profileTag.innerText="@"+u.username;
    profileSince.innerText=Math.floor((Date.now()-u.created)/86400000)+" days";

    bioInput.value=u.bio||"";

    if(u.photo){
      profilePic.style.backgroundImage=`url(${u.photo})`;
      profilePic.style.backgroundSize="cover";
      profilePic.style.backgroundPosition="center";
      profilePic.innerHTML="";
    }

    show("profile");
  });
}

function openUserProfile(uid){
  db.collection("users").doc(uid).get().then(doc=>{
    let u=doc.data();

    profileName.innerText=u.username;
    profileTag.innerText="@"+u.username;
    profileSince.innerText=Math.floor((Date.now()-u.created)/86400000)+" days";

    show("profile");
  });
}

function saveProfile(){
  db.collection("users").doc(currentUser.uid).update({
    bio:bioInput.value
  });
}

/* PROFILE PIC */
uploadPic.addEventListener("change", e=>{
  let file=e.target.files[0];
  if(!file) return;

  let reader=new FileReader();

  reader.onload=()=>{
    profilePic.style.backgroundImage=`url(${reader.result})`;
    profilePic.style.backgroundSize="cover";
    profilePic.style.backgroundPosition="center";

    db.collection("users").doc(currentUser.uid).update({
      photo:reader.result
    });
  };

  reader.readAsDataURL(file);
});
