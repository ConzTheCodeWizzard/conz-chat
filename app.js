window.onerror = function(msg, url, line){
  console.error("JS ERROR:", msg, "Line:", line);
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

// ===== ANDROID BACK =====
window.screenHistory = [];
const oldShow = window.show;

window.show = function(id){
  const current = document.querySelector(".screen.active");
  if(current && current.id !== id){
    screenHistory.push(current.id);
  }
  oldShow(id);
  history.pushState({ screen:id }, "");
};

window.addEventListener("popstate", function(){
  if(screenHistory.length > 0){
    oldShow(screenHistory.pop());
  } else {
    if(document.getElementById("home")?.classList.contains("active")){
      history.back();
    }
  }
});

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
window.myData={};
let unsubscribeMessages=null;
// unsubscribeStatus removed — online status feature removed
let unsubscribeDMTyping=null;

// Accurate unread: track per-conversation last-seen timestamp
let lastSeenTimes={};
let dmTypingTimeout=null;

window.unsubscribeMessages=null;

const DEV_UID="GAEtvdjvwla73GscQWnGthTPG6f1";
window.isDev=false;

/* ===== ROTATING TEXT ===== */
const rotatingMessages=[
  "Built by ~Conz~",
  "You are currently running Version 1.5",
  "Voice & Video Calls now in DMs and Groups!",
  "Public Groups now work globally!",
  "Kik-style read receipts added",
  "ConzChat Co Devs are Void And Trojan",
  "Report any issues you find to @Borg on ConzChat",
  "If you know how to code hit me up join the team",
  "Version 1.5 — Major Update!",
  "Thank you for trying ConzChat ~Conz~"
];
let rotatingIndex=0;
let hue=0;

function startRotatingText(){
  let textEl=document.getElementById("rotatingText");
  if(!textEl) return;
  textEl.style.fontSize="18px";
  textEl.style.marginTop="12px";
  textEl.style.minHeight="24px";
  textEl.style.fontWeight="bold";
  textEl.style.transition="0.4s ease";
  textEl.innerText=rotatingMessages[0];
  setInterval(()=>{
    textEl.style.opacity="0";
    setTimeout(()=>{
      rotatingIndex++;
      if(rotatingIndex>=rotatingMessages.length) rotatingIndex=0;
      hue+=35;
      textEl.innerText=rotatingMessages[rotatingIndex];
      textEl.style.color=`hsl(${hue},100%,60%)`;
      textEl.style.textShadow=`0 0 12px hsl(${hue},100%,60%)`;
      textEl.style.opacity="1";
    },300);
  },3000);
}
startRotatingText();

/* ===== THEMES ===== */
const themes={
  gothic:{main:"#000000",secondary:"#9d00ff"},
  joker:{main:"#1aff00",secondary:"#7b00ff"},
  zombie:{main:"#001a00",secondary:"#00ff00"},
  princess:{main:"#ffb6d9",secondary:"#ff4fa3"},
  ocean:{main:"#0044ff",secondary:"#7fdfff"},
  fire:{main:"#ff2200",secondary:"#ff8800"},
  forest:{main:"#001f00",secondary:"#00cc44"},
  batman:{main:"#111111",secondary:"#666666"},
  harley:{main:"#ff69b4",secondary:"#00bfff"},
  conz:{main:"#050505",secondary:"#ff003c"},
  sinister:{main:"#050505",secondary:"#ff0000"}
};

window.applyTheme=function(name){
  let t=themes[name];
  if(!t) return;
  document.documentElement.style.setProperty("--main",t.main);
  document.documentElement.style.setProperty("--secondary",t.secondary);
  localStorage.setItem("conz_theme",name);
  document.body.classList.remove("harleyTheme","conzTheme","sinisterTheme");
  if(name==="harley") document.body.classList.add("harleyTheme");
  if(name==="conz") document.body.classList.add("conzTheme");
  if(name==="sinister") document.body.classList.add("sinisterTheme");
};

window.resetTheme=function(){
  document.documentElement.style.setProperty("--main","#000");
  document.documentElement.style.setProperty("--secondary","#ff0033");
  document.body.classList.remove("harleyTheme","conzTheme","sinisterTheme");
  localStorage.removeItem("conz_theme");
};

let savedTheme=localStorage.getItem("conz_theme");
if(savedTheme && themes[savedTheme]) applyTheme(savedTheme);

/* ===== AUTH — hide all screens until auth resolves (no login flash) ===== */
// Hide everything immediately so nothing flashes before auth resolves
document.querySelectorAll(".screen").forEach(s=>{ s.style.display="none"; s.classList.remove("active"); });

auth.onAuthStateChanged(user=>{
  if(user){
    window.currentUser=user;
    window.isDev=user.uid===DEV_UID;

    db.collection("users").doc(user.uid).onSnapshot(doc=>{
      let d=doc.data()||{};
      d.uid=user.uid;
      d.premium=d.premium||false;
      window.currentUser.premium=d.premium;
      if(d.premiumPopup){
        showPopup(d.premiumPopup);
        db.collection("users").doc(user.uid).update({premiumPopup:""});
      }
      if(d.banned){
        showPopup("This account is permanently banned from ConzChat.");
        auth.signOut();
        return;
      }
      if(d.forceLogout){
        showPopup(d.logoutMessage||"Logged out");
        db.collection("users").doc(user.uid).update({forceLogout:false,logoutMessage:""});
        auth.signOut();
      }
    });

    // Online status feature removed by user request

    db.collection("users").doc(user.uid).onSnapshot(doc=>{
      window.myData=doc.data()||{};
      loadAvatar();
      if(!window.chatsLoaded){
        window.chatsLoaded=true;
        // Load stories rail
        if(typeof window.loadStories==="function") setTimeout(window.loadStories,500);
        loadChats();
        if(window.loadGroups) loadGroups();
        if(window.renderPublicGroups) renderPublicGroups();
      }
    });

    show("home");
  } else {
    window.chatsLoaded=false;
    show("welcome");
  }
});

window.login=function(){
  let email=loginEmail.value;
  let pass=loginPassword.value;
  if(!email||!pass){ showPopup("Missing details"); return; }
  auth.signInWithEmailAndPassword(email,pass).catch(e=>showPopup("Invalid email or password"));
};

window.signup=async function(){
  let username=signupUsername.value.trim();
  let email=signupEmail.value.trim();
  let pass=signupPassword.value;
  if(!username||!email||!pass){ showPopup("Fill everything"); return; }
  try{
    const existing=await db.collection("users").where("usernameLower","==",username.toLowerCase()).get();
    if(!existing.empty){ showPopup("Username already taken"); return; }
    const res=await auth.createUserWithEmailAndPassword(email,pass);
    await db.collection("users").doc(res.user.uid).set({
      username,usernameLower:username.toLowerCase(),displayName:username,
      photo:"",coverPhoto:"",created:Date.now(),
      banned:false,blockedUsers:[]
    });
  }catch(e){ showPopup("Signup failed: "+e.message); }
};

window.logout=function(){
  auth.signOut();
};

window.toggleFab=function(){
  fabOpen=!fabOpen;
  fabMenu.style.display=fabOpen?"flex":"none";
};

/* ===== PROFILE ===== */
window.openProfile=function(uid=window.currentUser.uid){
  db.collection("users").doc(uid).get().then(doc=>{
    let u=doc.data()||{};
    let isMe=uid===window.currentUser.uid;

    // Build full profile HTML first
    let coverStyle = u.coverPhoto
      ? 'background-image:url("'+u.coverPhoto+'");background-size:cover;background-position:center top;'
      : '';
    profileContent.innerHTML=`
      <div class="profileCover" id="profileCoverArea" style="${coverStyle}">
        ${isMe?`<button class="coverEditBtn" onclick="pickCoverPhoto()">📷 Cover</button>`:''}
      </div>
      <div class="profileAvatarWrap">
        <div class="avatar" ${isMe?'onclick="pickImage()"':''}>
          ${u.photo?`<img src="${u.photo}">`:`<div style="font-size:40px;">👤</div>`}
        </div>
      </div>
      <div class="displayName" ${isMe?'onclick="editDisplayName()"':''}>${u.displayName||u.username}</div>
      ${uid===DEV_UID?`<div class="devBadge">👑 ConzChat Dev</div>`:""}
      ${u.premium?`<div class="premiumBadge">💎 Premium User</div>`:""}
      <div class="username">@${u.username}</div>
      ${isMe?`
        <div class="profileActions">
          <button class="profileActionBtn" onclick="logout()">🚪 Logout</button>
          <button class="profileActionBtn" onclick="toggleSettings()">⚙ Settings</button>
        </div>
        <div id="settingsPanel" style="display:none;width:90%;margin-top:10px;">
          <div class="settingTitle">Brightness</div>
          <input type="range" id="brightnessSlider" min="0" max="100" value="50" oninput="updateBrightness(this.value)" style="width:100%;">
        </div>
      `:''}
      ${!isMe?`
        <div class="profileActions">
          <button class="profileActionBtn" onclick="openChat('${uid}','${u.username}','${u.photo||''}')">💬 Message</button>
          <button class="profileActionBtn blockBtn" onclick="blockUser('${uid}','${u.username}')">🚫 Block</button>
        </div>
      `:""}
    `;
    // Also apply via JS as a belt-and-braces fallback
    if(u.coverPhoto){
      let coverEl = document.getElementById("profileCoverArea");
      if(coverEl){
        coverEl.style.backgroundImage = 'url("' + u.coverPhoto + '")';
        coverEl.style.backgroundSize = "cover";
        coverEl.style.backgroundPosition = "center top";
      }
    }

    // Restore brightness slider value if on own profile
    if(isMe){
      let saved=localStorage.getItem("conz_brightness");
      let slider=document.getElementById("brightnessSlider");
      if(saved && slider) slider.value=saved;
    }

    if(window.daysOnApp) daysOnApp.innerText=Math.floor((Date.now()-u.created)/86400000)+" days on ConzChat";
    show("profile");
  });
};

/* ===== PROFILE PICTURE ===== */
window.pickImage=function(){ filePicker.click(); };

filePicker.onchange=e=>{
  let f=e.target.files[0];
  if(!f) return;
  let img=new Image();
  let reader=new FileReader();
  reader.onload=()=>{
    img.onload=()=>{
      let canvas=document.createElement("canvas");
      let maxSize=400;
      let w=img.width,h=img.height;
      if(w>h){if(w>maxSize){h=h*(maxSize/w);w=maxSize;}}
      else{if(h>maxSize){w=w*(maxSize/h);h=maxSize;}}
      canvas.width=w;canvas.height=h;
      canvas.getContext("2d").drawImage(img,0,0,w,h);
      let compressed=canvas.toDataURL("image/jpeg",0.75);
      db.collection("users").doc(window.currentUser.uid).update({photo:compressed});
      window.myData.photo=compressed;
      loadAvatar();
      openProfile(window.currentUser.uid);
    };
    img.src=reader.result;
  };
  reader.readAsDataURL(f);
};

/* ===== COVER PHOTO ===== */
window.pickCoverPhoto=function(){
  let inp=document.getElementById("coverPhotoPicker");
  if(inp) inp.click();
};

document.addEventListener("change",function(e){
  if(e.target.id!=="coverPhotoPicker") return;
  let f=e.target.files[0];
  if(!f) return;
  let img=new Image();
  let reader=new FileReader();
  reader.onload=()=>{
    img.onload=()=>{
      let canvas=document.createElement("canvas");
      // Keep well under Firestore 1MB limit: 600x220 @ 65% quality
      canvas.width=600; canvas.height=220;
      let ctx=canvas.getContext("2d");
      // Cover-fill: scale to fill canvas, centre-crop
      let scale=Math.max(canvas.width/img.width, canvas.height/img.height);
      let sw=img.width*scale, sh=img.height*scale;
      let ox=(canvas.width-sw)/2, oy=(canvas.height-sh)/2;
      ctx.drawImage(img, ox, oy, sw, sh);
      let compressed=canvas.toDataURL("image/jpeg",0.65);
      db.collection("users").doc(window.currentUser.uid).update({coverPhoto:compressed})
        .then(()=>{
          window.myData.coverPhoto=compressed;
          openProfile(window.currentUser.uid);
        })
        .catch(err=>{ showPopup("Cover photo too large, try a smaller image."); });
    };
    img.src=reader.result;
  };
  reader.readAsDataURL(f);
});

window.editDisplayName=function(){
  let newName=prompt("Enter new display name",window.myData.displayName||window.myData.username);
  if(!newName||!newName.trim()) return;
  db.collection("users").doc(window.currentUser.uid).update({displayName:newName});
  window.myData.displayName=newName;
  openProfile(window.currentUser.uid);
};

function loadAvatar(){
  profileBtn.innerHTML=window.myData.photo
    ?`<img src="${window.myData.photo}" style="width:30px;height:30px;border-radius:50%;pointer-events:none;">`
    :"👤";
}

/* ===== BLOCK USER ===== */
window.blockUser=async function(uid,username){
  if(!confirm(`Block @${username}? They won't be able to message you.`)) return;
  try{
    let myRef=db.collection("users").doc(window.currentUser.uid);
    let myDoc=await myRef.get();
    let blocked=(myDoc.data().blockedUsers||[]);
    if(!blocked.includes(uid)){ blocked.push(uid); await myRef.update({blockedUsers:blocked}); }
    showPopup(`@${username} has been blocked.`);
    show("home");
  }catch(err){ showPopup(err.message); }
};

window.unblockUser=async function(uid,username){
  try{
    let myRef=db.collection("users").doc(window.currentUser.uid);
    let myDoc=await myRef.get();
    let blocked=(myDoc.data().blockedUsers||[]).filter(b=>b!==uid);
    await myRef.update({blockedUsers:blocked});
    showPopup(`@${username} has been unblocked.`);
  }catch(err){ showPopup(err.message); }
};

/* ===== SEARCH ===== */
window.openSearch=()=>show("search");

window.searchUsers=function(){
  let query=event.target.value.trim().toLowerCase();
  results.innerHTML="";
  if(!query) return;
  db.collection("users").get().then(snap=>{
    results.innerHTML="";
    snap.forEach(doc=>{
      let u=doc.data()||{};
      if(u.banned) return;
      if((u.username||"").toLowerCase()!==query) return;
      let div=document.createElement("div");
      div.className="privateChatItem";
      div.innerHTML=`
        <div class="chatAvatar">${u.photo?`<img src="${u.photo}">`:""}</div>
        <div>${u.username}</div>
      `;
      div.onclick=()=>openChat(doc.id,u.username,u.photo);
      results.appendChild(div);
    });
  });
};

/* ===== OPEN DM CHAT ===== */
window.openChat=function(uid,name,photo){
  window.currentGroup=null;
  window.currentChatUser=uid;

  if(unsubscribeMessages) unsubscribeMessages();
  if(unsubscribeDMTyping) unsubscribeDMTyping();
  if(window.unsubscribeGroupMessages){ window.unsubscribeGroupMessages(); window.unsubscribeGroupMessages=null; }
  if(window.unsubscribePublicGroupMessages){ window.unsubscribePublicGroupMessages(); window.unsubscribePublicGroupMessages=null; }

  // Mark this conversation as seen now
  lastSeenTimes[uid]=Date.now();
  saveLastSeen();

  show("chat");

  let callBar=document.getElementById("chatCallBar");
  if(callBar) callBar.style.display="flex";

  // Plain centered name — no status indicator
  chatName.innerHTML = name;
  chatName.onclick=()=>{ openProfile(uid); };

  // DM typing listener
  unsubscribeDMTyping=db.collection("dmTyping")
  .where("to","==",window.currentUser.uid)
  .where("from","==",uid)
  .onSnapshot(snap=>{
    let isTyping=false;
    snap.forEach(doc=>{
      let d=doc.data();
      if(d.typing && (Date.now()-d.ts)<5000) isTyping=true;
    });
    showTypingIndicator(isTyping, name);
  });

  // Messages listener
  unsubscribeMessages=db.collection("messages")
  .orderBy("time")
  .onSnapshot(snap=>{
    messages.innerHTML="";
    snap.forEach(doc=>{
      let m=doc.data();
      if(m.to===window.currentUser.uid && m.from===uid && m.receipt==="S")
        db.collection("messages").doc(doc.id).update({receipt:"D"});
      if(m.to===window.currentUser.uid && m.from===uid && m.receipt!=="R")
        db.collection("messages").doc(doc.id).update({receipt:"R"});

      if(!(m.from===window.currentUser.uid||m.to===window.currentUser.uid)) return;
      let other=m.from===window.currentUser.uid?m.to:m.from;
      if(other!==uid) return;

      let isMine=m.from===window.currentUser.uid;
      let msgId=doc.id;
      let wrap=document.createElement("div");
      wrap.className="msgWrap "+(isMine?"me":"them")+" msgAnim";

      let avatar=document.createElement("div");
      avatar.className="msgAvatar";

      let bubble=document.createElement("div");
      bubble.className="msg";
      bubble.dataset.id=msgId;

      let receiptIcon="";
      if(isMine){
        if(m.receipt==="R") receiptIcon=`<span class="receiptRead" title="Read">✓✓</span>`;
        else if(m.receipt==="D") receiptIcon=`<span class="receiptDelivered" title="Delivered">✓✓</span>`;
        else receiptIcon=`<span class="receiptSent" title="Sent">✓</span>`;
      }

      // Reply-to preview
      let replyHtml="";
      if(m.replyTo){
        replyHtml=`<div class="replyPreview"><span class="replyBar"></span><span class="replyText">${m.replyTo.text||"📎 Media"}</span></div>`;
      }

      // Reactions display
      let reactionsHtml="";
      if(m.reactions && Object.keys(m.reactions).length>0){
        let counts={};
        Object.values(m.reactions).forEach(e=>{ counts[e]=(counts[e]||0)+1; });
        reactionsHtml=`<div class="msgReactions">`+Object.entries(counts).map(([e,c])=>`<span class="reactionBubble">${e}${c>1?" "+c:""}</span>`).join("")+`</div>`;
      }

      // Handle image/video/voice messages
      let contentHtml="";
      if(m.type==="image"){
        contentHtml=`<img src="${m.url}" class="msgImage" onclick="viewFullImage('${m.url}')">`;
        if(m.isCamera) contentHtml+=`<div class="msgCameraLabel">📷 Camera</div>`;
      } else if(m.type==="video"){
        contentHtml=`<video src="${m.url}" class="msgVideo" controls playsinline></video>`;
        if(m.isCamera) contentHtml+=`<div class="msgCameraLabel">📷 Camera</div>`;
      } else if(m.type==="voice"){
        let transcriptHtml = m.transcript ? `<div class="voiceTranscript">“${m.transcript}”</div>` : "";
        contentHtml=`<div class="voiceNoteWrap"><audio src="${m.url}" controls class="voiceAudio"></audio><div class="voiceLabel">🎙️ Voice Note</div>${transcriptHtml}</div>`;
      } else {
        contentHtml=`<div class="msgText">${m.text||""}</div>`;
      }

      bubble.innerHTML=`
        ${replyHtml}
        ${contentHtml}
        ${reactionsHtml}
        <div class="msgMeta">${formatKikTime(m.time)} ${receiptIcon}</div>
      `;

      // Long-press for action sheet (reactions, reply, delete)
      let pressTimer;
      bubble.addEventListener("touchstart",()=>{
        pressTimer=setTimeout(()=>{ showMsgActions(msgId, m, isMine, name, photo); },500);
      });
      bubble.addEventListener("touchend",()=>clearTimeout(pressTimer));
      bubble.addEventListener("touchmove",()=>clearTimeout(pressTimer));
      // Desktop right-click
      bubble.addEventListener("contextmenu",(e)=>{ e.preventDefault(); showMsgActions(msgId, m, isMine, name, photo); });

      if(isMine){
        if(window.myData.photo) avatar.innerHTML=`<img src="${window.myData.photo}">`;
        wrap.appendChild(bubble);
        wrap.appendChild(avatar);
      } else {
        if(photo) avatar.innerHTML=`<img src="${photo}">`;
        wrap.appendChild(avatar);
        wrap.appendChild(bubble);
      }
      messages.appendChild(wrap);
    });
    messages.scrollTop=messages.scrollHeight;
  });
};

/* ===== MESSAGE ACTION SHEET (long-press) ===== */
window._replyTo = null;

function showMsgActions(msgId, m, isMine, senderName, senderPhoto){
  // Remove any existing sheet
  let old=document.getElementById("msgActionSheet");
  if(old) old.remove();

  let sheet=document.createElement("div");
  sheet.id="msgActionSheet";
  sheet.className="msgActionSheet";

  const emojis=["❤️","😂","😮","😢","👍","🔥"];
  let emojiRow=emojis.map(e=>`<button class="reactionEmojiBtn" onclick="addReaction('${msgId}','${e}')">${e}</button>`).join("");

  sheet.innerHTML=`
    <div class="actionSheetReactions">${emojiRow}</div>
    <div class="actionSheetDivider"></div>
    <button class="actionSheetBtn" onclick="startReply('${msgId}','${(m.text||'📎 Media').replace(/'/g,"\'")}','${senderName}')">↩️ Reply</button>
    ${isMine?`<button class="actionSheetBtn actionSheetDelete" onclick="deleteMsg('${msgId}')">🗑️ Delete</button>`:""}
    <button class="actionSheetBtn" onclick="document.getElementById('msgActionSheet').remove()">Cancel</button>
  `;

  document.body.appendChild(sheet);
  // Dismiss on backdrop tap
  setTimeout(()=>{
    document.addEventListener("touchstart", function dismissSheet(e){
      if(!sheet.contains(e.target)){ sheet.remove(); document.removeEventListener("touchstart",dismissSheet); }
    });
  },100);
}
window.showMsgActions=showMsgActions;

window.addReaction=function(msgId, emoji){
  let old=document.getElementById("msgActionSheet");
  if(old) old.remove();
  let uid=window.currentUser.uid;
  // Determine collection
  let col = window.currentGroup ? (window.currentGroup.tag ? "publicGroupMessages" : "groupMessages") : "messages";
  db.collection(col).doc(msgId).update({
    [`reactions.${uid}`]: emoji
  }).catch(e=>console.warn("reaction err",e));
  if(navigator.vibrate) navigator.vibrate(30);
};

window.startReply=function(msgId, text, senderName){
  let old=document.getElementById("msgActionSheet");
  if(old) old.remove();
  window._replyTo={ id:msgId, text:text, sender:senderName };
  let bar=document.getElementById("replyBar");
  if(!bar){
    bar=document.createElement("div");
    bar.id="replyBar";
    bar.className="replyBar";
    let inputWrap=document.querySelector(".chatInputBar");
    if(inputWrap) inputWrap.parentNode.insertBefore(bar, inputWrap);
  }
  bar.innerHTML=`<span class="replyBarLine"></span><div class="replyBarContent"><span class="replyBarName">${senderName}</span><span class="replyBarText">${text.substring(0,60)}${text.length>60?"...":""}</span></div><button class="replyBarClose" onclick="cancelReply()">✕</button>`;
  bar.style.display="flex";
  if(window.msgInput) window.msgInput.focus();
};

window.cancelReply=function(){
  window._replyTo=null;
  let bar=document.getElementById("replyBar");
  if(bar) bar.style.display="none";
};

window.deleteMsg=function(msgId){
  let old=document.getElementById("msgActionSheet");
  if(old) old.remove();
  let col = window.currentGroup ? (window.currentGroup.tag ? "publicGroupMessages" : "groupMessages") : "messages";
  db.collection(col).doc(msgId).update({ deleted:true, text:"This message was deleted", type:"text" })
    .catch(e=>console.warn("delete err",e));
  if(navigator.vibrate) navigator.vibrate([30,20,30]);
};

/* ===== TYPING INDICATOR — bottom above input ===== */
function showTypingIndicator(isTyping, name){
  let typingEl=document.getElementById("typingIndicator");
  if(!typingEl) return;
  if(isTyping){
    typingEl.innerHTML=`<span class="typingDots">${name} is typing<span class="dot1">.</span><span class="dot2">.</span><span class="dot3">.</span></span>`;
    typingEl.style.display="flex";
  } else {
    typingEl.style.display="none";
  }
}
window.showTypingIndicator=showTypingIndicator;

/* ===== SEND HANDLER ===== */
window.handleSend=function(){
  let val=msgInput.value.trim();
  if(!val) return;

  // Conz super menu trigger
  if(val.toLowerCase()==="conz" && !window.currentGroup){
    msgInput.value="";
    const menu=document.getElementById("conzMenu");
    if(getComputedStyle(menu).display==="none") menu.style.display="flex";
    else menu.style.display="none";
    return;
  }

  if(window.currentGroup){
    // Public group (has .tag) vs private group (has .groupId or is a string)
    if(typeof window.currentGroup==="object" && window.currentGroup.tag){
      if(typeof window.sendPublicGroupMessage==="function") sendPublicGroupMessage();
    } else {
      if(typeof window.sendGroupMessage==="function") sendGroupMessage();
    }
    return;
  }

  if(!window.currentChatUser) return;

  clearDMTyping();

  let msgData={
    text:val,
    from:window.currentUser.uid,
    to:window.currentChatUser,
    time:Date.now(),
    receipt:"S",
    type:"text"
  };
  if(window._replyTo) msgData.replyTo = window._replyTo;

  db.collection("messages").add(msgData);

  // Haptic feedback
  if(navigator.vibrate) navigator.vibrate(20);

  // Clear reply
  window.cancelReply();

  msgInput.value="";
  sendBtn.classList.remove("active");
  // Keep keyboard open — do NOT blur
  msgInput.focus();
};

// Legacy alias
window.sendMessage=window.handleSend;

/* ===== DM TYPING ===== */
function setDMTyping(){
  if(!window.currentChatUser) return;
  db.collection("dmTyping").doc(window.currentUser.uid+"_"+window.currentChatUser).set({
    from:window.currentUser.uid,to:window.currentChatUser,typing:true,ts:Date.now()
  });
}
function clearDMTyping(){
  if(!window.currentChatUser) return;
  db.collection("dmTyping").doc(window.currentUser.uid+"_"+window.currentChatUser).set({
    from:window.currentUser.uid,to:window.currentChatUser,typing:false,ts:Date.now()
  });
}

/* ===== LAST SEEN PERSISTENCE (for accurate unread counts) ===== */
function saveLastSeen(){
  try{ localStorage.setItem("conz_lastSeen",JSON.stringify(lastSeenTimes)); }catch(e){}
}
function loadLastSeen(){
  try{
    let s=localStorage.getItem("conz_lastSeen");
    if(s) lastSeenTimes=JSON.parse(s);
  }catch(e){}
}
loadLastSeen();

/* ===== LOAD CHAT LIST — no duplicates, accurate unread ===== */
function loadChats(){
  db.collection("messages").orderBy("time","desc").onSnapshot(snap=>{
    // Remove all existing DM rows
    document.querySelectorAll(".privateChatItem").forEach(x=>x.remove());

    let seen={};
    // Collect latest message per conversation
    let convMap={};

    snap.forEach(doc=>{
      let m=doc.data();
      if(m.from!==window.currentUser.uid && m.to!==window.currentUser.uid) return;
      let other=m.from===window.currentUser.uid?m.to:m.from;
      if(!convMap[other]){
        convMap[other]={ latest:m, unread:0 };
      }
      // Count unread: messages TO me, FROM other, after last seen
      if(m.to===window.currentUser.uid && m.from===other){
        let lastSeen=lastSeenTimes[other]||0;
        if(m.time > lastSeen) convMap[other].unread++;
      }
    });

    // Render one row per unique conversation
    Object.keys(convMap).forEach(other=>{
      if(seen[other]) return;
      seen[other]=true;
      let { unread }=convMap[other];
      db.collection("users").doc(other).get().then(u=>{
        let d=u.data()||{};
        let div=document.createElement("div");
        div.className="privateChatItem";
        div.innerHTML=`
          <div class="chatAvatar">${d.photo?`<img src="${d.photo}">`:""}</div>
          <div class="chatNameWrap">
            <div class="chatItemName">${d.username}</div>
            ${unread>0?`<div class="unreadBadge">${unread>99?"99+":unread}</div>`:""}
          </div>
        `;
        div.onclick=()=>{
          lastSeenTimes[other]=Date.now();
          saveLastSeen();
          // Remove badge immediately on tap
          let badge=div.querySelector(".unreadBadge");
          if(badge) badge.remove();
          openChat(other,d.username,d.photo||"");
        };
        chatList.appendChild(div);
      });
    });
  });
}

/* ===== INPUT EVENTS ===== */
setTimeout(()=>{
  if(window.msgInput && window.sendBtn){
    msgInput.addEventListener("input",()=>{
      if(msgInput.value.trim()) sendBtn.classList.add("active");
      else sendBtn.classList.remove("active");

      if(window.currentChatUser && !window.currentGroup){
        setDMTyping();
        clearTimeout(dmTypingTimeout);
        dmTypingTimeout=setTimeout(clearDMTyping,3000);
      }
    });

    msgInput.addEventListener("keydown",function(e){
      if(e.key==="Enter" && !e.shiftKey){
        e.preventDefault();
        handleSend();
      }
    });
  }
},500);

/* ===== VIEW FULL IMAGE ===== */
window.viewFullImage=function(url){
  let overlay=document.createElement("div");
  overlay.style.cssText="position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.95);z-index:99999;display:flex;justify-content:center;align-items:center;";
  overlay.innerHTML=`<img src="${url}" style="max-width:95%;max-height:90%;border-radius:8px;">`;
  overlay.onclick=()=>overlay.remove();
  document.body.appendChild(overlay);
};

/* ===== DEV FUNCTIONS ===== */
window.superBoot=async function(){
  if(!window.isDev){ showPopup("YOU AINT A DEV!"); return; }
  if(!window.currentChatUser){ showPopup("OPEN A CHAT FIRST"); return; }
  try{
    await db.collection("users").doc(window.currentChatUser).update({forceLogout:true,logoutMessage:"LOGGED OUT BY Super Menu SixSevenn🙌"});
    showPopup("USER BOOTED");
  }catch(err){ showPopup(err.message); }
};

window.superBan=async function(){
  if(!window.isDev){ showPopup("YOU AINT A DEV!"); return; }
  if(!window.currentChatUser){ showPopup("OPEN A CHAT FIRST"); return; }
  try{
    await db.collection("users").doc(window.currentChatUser).update({banned:true,forceLogout:true,logoutMessage:"This account has been permanently BANNED ~Conz~"});
    showPopup("USER BANNED");
  }catch(err){ showPopup(err.message); }
};

window.fakeIpPull=function(){
  const consoleBox=document.getElementById("fakeConsole");
  consoleBox.innerHTML="";
  const lines=["Loading conz servers...","Initialising...","Server response received...","Server loaded...","Permissions granted...","Scanning victim device...","Monitoring local searches...","Fetching IP...","Making sure its correct...","Victims IP: xxx.xx.xxx.xxx.xx","IP hidden, reason NOT DEV","Closing servers...","Servers CLOSED!"];
  let i=0;
  const interval=setInterval(()=>{
    consoleBox.innerHTML+=lines[i]+"\n";
    consoleBox.scrollTop=consoleBox.scrollHeight;
    i++;
    if(i>=lines.length) clearInterval(interval);
  },1000);
};

/* ===== POPUP ===== */
window.showPopup=function(text){
  document.getElementById("popupText").innerText=text;
  document.getElementById("customPopup").style.display="flex";
};
window.closePopup=function(){
  document.getElementById("customPopup").style.display="none";
};

/* ===== PREMIUM MENU ===== */
window.openPremiumMenu=function(){
  document.getElementById("conzMenu").style.display="none";
  document.getElementById("premiumMenu").style.display="flex";
};
window.closePremiumMenu=function(){
  document.getElementById("premiumMenu").style.display="none";
  document.getElementById("conzMenu").style.display="flex";
};
window.openCredits=function(){ show("creditsScreen"); };

window.startAnimatedMessage=function(){
  let isDev=window.currentUser?.uid===DEV_UID;
  let isPremium=window.currentUser?.premium;
  if(!isDev&&!isPremium){ showPopup("You are currently using a standard account, this menu is for premium users, contact Conz/@Borg to purchase premium, lifetime premium is currently £10."); return; }
  let text=document.getElementById("animatedMessageInput").value.trim();
  if(!text) return;
  window.animatedMessageLoop=setInterval(function(){
    msgInput.value="🎭 "+text+" 🎭";
    handleSend();
  },150);
  document.getElementById("premiumConsole").innerHTML="Spam started";
};

window.stopAnimatedMessage=function(){
  clearInterval(window.animatedMessageLoop);
  document.getElementById("premiumConsole").innerHTML="Spam stopped";
};

window.givePremium=function(){
  if(window.currentUser?.uid!==DEV_UID){ showPopup("YOU AINT A DEV!"); return; }
  if(!window.currentChatUser){ showPopup("No user selected."); return; }
  db.collection("users").doc(window.currentChatUser).update({premium:true,premiumPopup:"Premium has been successfully added to your account, ENJOY! Please refresh the app to activate premium features."});
  showPopup("Premium granted successfully");
};

/* ===== SETTINGS ===== */
window.toggleSettings=function(){
  const panel=document.getElementById("settingsPanel");
  panel.style.display=panel.style.display==="block"?"none":"block";
};
window.updateBrightness=function(value){
  document.body.style.filter=`brightness(${0.25+(value/100)*0.75})`;
  localStorage.setItem("conz_brightness",value);
};
window.addEventListener("load",function(){
  let saved=localStorage.getItem("conz_brightness");
  if(saved){
    document.body.style.filter=`brightness(${0.25+(saved/100)*0.75})`;
    const slider=document.getElementById("brightnessSlider");
    if(slider) slider.value=saved;
  }
});

/* ===== KIK-STYLE TIMESTAMP ===== */
window.formatKikTime=function(ts){
  if(!ts) return "";
  let now=new Date();
  let d=new Date(ts);
  let diffDays=Math.floor((now-d)/86400000);
  let hours=d.getHours();
  let mins=d.getMinutes().toString().padStart(2,"0");
  let ampm=hours>=12?"PM":"AM";
  let h=hours%12||12;
  let timeStr=`${h}:${mins} ${ampm}`;
  if(diffDays===0) return timeStr;
  if(diffDays===1) return `Yesterday ${timeStr}`;
  if(diffDays<7){ let days=["Sun","Mon","Tue","Wed","Thu","Fri","Sat"]; return `${days[d.getDay()]} ${timeStr}`; }
  let months=["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];
  return `${months[d.getMonth()]} ${d.getDate()} ${timeStr}`;
};

} // end startApp
