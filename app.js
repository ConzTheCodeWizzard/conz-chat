let currentUser;
let currentChat;

/* NAV */
function show(id){
  document.querySelectorAll(".screen").forEach(s=>s.style.display="none");
  document.getElementById(id).style.display="block";
}

/* AUTH */
auth.onAuthStateChanged(u=>{
  currentUser = u;
  if(u){ show("home"); loadChats(); }
  else show("welcome");
});

function signup(){
  auth.createUserWithEmailAndPassword(su_user.value+"@app.com", su_pass.value)
  .then(res=>{
    db.collection("users").doc(res.user.uid).set({
      username: su_user.value,
      bio:"",
      photo:"",
      created: Date.now()
    });
  });
}

function login(){
  auth.signInWithEmailAndPassword(li_user.value+"@app.com", li_pass.value);
}

function logout(){ auth.signOut(); }

/* PROFILE */
function openMyProfile(){
  db.collection("users").doc(currentUser.uid).get().then(doc=>{
    let u = doc.data();
    profileName.innerText = u.username;
    bioInput.value = u.bio;

    if(u.photo) setPic(u.photo);

    show("profile");
  });
}

profilePic.onclick = ()=>{
  if(!profilePic.style.backgroundImage){
    uploadPic.click();
  } else {
    menu.style.display="block";
  }
};

function closeMenu(){ menu.style.display="none"; }

function changePic(){
  closeMenu();
  uploadPic.click();
}

function viewPic(){
  closeMenu();
  let url = profilePic.style.backgroundImage.replace(/url\("|"\)/g,"");
  window.open(url);
}

uploadPic.onchange = e=>{
  let file = e.target.files[0];
  let ref = storage.ref("pfp/"+currentUser.uid);

  ref.put(file).then(()=>{
    ref.getDownloadURL().then(url=>{
      setPic(url);
      db.collection("users").doc(currentUser.uid).update({photo:url});
    });
  });
};

function setPic(url){
  profilePic.style.backgroundImage = `url(${url})`;
  profilePic.style.backgroundSize="cover";
  profilePic.innerHTML="";
}

function saveProfile(){
  db.collection("users").doc(currentUser.uid).update({
    bio: bioInput.value
  });
}

/* SEARCH */
function openSearch(){
  show("search");
  db.collection("users").get().then(snap=>renderUsers(snap.docs));
}

function searchUsers(){
  let q = searchInput.value.toLowerCase();

  db.collection("users").get().then(snap=>{
    let list = snap.docs.filter(d=>{
      return d.data().username.toLowerCase().includes(q);
    });

    renderUsers(list);
  });
}

function renderUsers(list){
  results.innerHTML="";
  list.forEach(doc=>{
    if(doc.id===currentUser.uid) return;

    let u = doc.data();
    let div = document.createElement("div");
    div.innerText = u.username;
    div.onclick = ()=>openChat(doc.id,u.username);
    results.appendChild(div);
  });
}

/* CHAT */
function openChat(uid,name){
  currentChat=uid;
  show("chat");

  db.collection("messages").onSnapshot(snap=>{
    messages.innerHTML="";
    snap.forEach(doc=>{
      let m=doc.data();

      if(
        (m.from===currentUser.uid && m.to===uid) ||
        (m.from===uid && m.to===currentUser.uid)
      ){
        let div=document.createElement("div");

        if(m.text) div.innerText=m.text;

        if(m.image){
          let img=document.createElement("img");
          img.src=m.image;
          img.style.width="150px";
          div.appendChild(img);
        }

        if(m.audio){
          let audio=document.createElement("audio");
          audio.src=m.audio;
          audio.controls=true;
          div.appendChild(audio);
        }

        messages.appendChild(div);
      }
    });
  });
}

/* TEXT */
function send(){
  db.collection("messages").add({
    from:currentUser.uid,
    to:currentChat,
    text:msg.value,
    time:Date.now()
  });
  msg.value="";
}

/* IMAGE */
function sendImage(){
  let file = sendImg.files[0];
  let ref = storage.ref("chat/"+Date.now());

  ref.put(file).then(()=>{
    ref.getDownloadURL().then(url=>{
      db.collection("messages").add({
        from:currentUser.uid,
        to:currentChat,
        image:url
      });
    });
  });
}

/* VOICE */
let recorder, chunks=[];

function startRecording(){
  navigator.mediaDevices.getUserMedia({audio:true}).then(stream=>{
    recorder = new MediaRecorder(stream);

    recorder.ondataavailable=e=>chunks.push(e.data);

    recorder.onstop=()=>{
      let blob = new Blob(chunks);
      chunks=[];

      let ref = storage.ref("audio/"+Date.now());

      ref.put(blob).then(()=>{
        ref.getDownloadURL().then(url=>{
          db.collection("messages").add({
            from:currentUser.uid,
            to:currentChat,
            audio:url
          });
        });
      });
    };

    recorder.start();

    setTimeout(()=>recorder.stop(),3000);
  });
}
