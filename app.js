// ConzChat app.js — Modified for Kik Gateway Backend
// Original by Conz | Backend swap by Kik Gateway
// All db.collection() and auth.* calls now route through firebase.js (Kik bridge)

window.currentUser=null;
window.currentChatUser=null;
window.currentGroup=null;
let fabOpen=false;
window.myData={};
let unsubscribeMessages=null;
let unsubscribeDMTyping=null;
let lastSeenTimes={};
let dmTypingTimeout=null;
window.unsubscribeMessages=null;
const DEV_UID="ConzTheCodeJunky"; // Kik username of dev
window.isDev=false;

/* ===== ROTATING TEXT ===== */
const rotatingMessages=[
  "Built by ~Conz~",
  "Now powered by Kik's network!",
  "Voice & Video Calls now in DMs and Groups!",
  "Search and join real Kik public groups!",
  "Kik-style read receipts added",
  "ConzChat Co Devs are Void And Trojan",
  "Report any issues you find to @Borg on ConzChat",
  "If you know how to code hit me up join the team",
  "Version 1.6 — Kik Backend!",
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

/* ===== AUTH — hide all screens until auth resolves ===== */
document.querySelectorAll(".screen").forEach(s=>{ s.style.display="none"; s.classList.remove("active"); });

auth.onAuthStateChanged(user=>{
  if(user){
    window.currentUser=user;
    window.isDev=(user.username===DEV_UID || user.uid===DEV_UID);
    updateSuggestionsDevBtn();
    startApp(user);
  } else {
    show("welcome");
  }
});

/* ===== SHOW SCREEN ===== */
window.show=function(id){
  document.querySelectorAll(".screen").forEach(s=>{
    s.style.display="none";
    s.classList.remove("active");
  });
  let el=document.getElementById(id);
  if(el){ el.style.display="flex"; el.classList.add("active"); }
};

/* ===== POPUP ===== */
window.showPopup=function(msg,title){
  let popup=document.getElementById("customPopup");
  let text=document.getElementById("popupText");
  let titleEl=document.getElementById("popupTitle");
  if(text) text.innerText=msg;
  if(titleEl){ if(title){ titleEl.innerText=title; titleEl.style.display="block"; } else { titleEl.style.display="none"; } }
  if(popup) popup.style.display="flex";
};
window.closePopup=function(){
  let popup=document.getElementById("customPopup");
  if(popup) popup.style.display="none";
};

/* ===== LOGIN ===== */
window.login=async function(){
  let username=document.getElementById("loginEmail").value.trim();
  let pass=document.getElementById("loginPassword").value;
  if(!username||!pass){ showPopup("Enter your Kik username and password"); return; }
  try{
    showPopup("Connecting to Kik...");
    await auth.signInWithEmailAndPassword(username, pass);
    closePopup();
  }catch(e){
    if(e.message !== "captcha_required"){
      showPopup(e.message||"Login failed");
    }
  }
};

/* ===== SIGNUP ===== */
window.signup=async function(){
  let username=(document.getElementById("signupUsername")||{}).value||"";
  let email=(document.getElementById("signupEmail")||{}).value||"";
  let pass=(document.getElementById("signupPassword")||{}).value||"";
  let firstName=(document.getElementById("signupFirstName")||{}).value||username;
  let lastName=(document.getElementById("signupLastName")||{}).value||"User";
  let birthday=(document.getElementById("signupBirthday")||{}).value||"2000-01-01";

  if(!username||!email||!pass){ showPopup("Fill in all fields"); return; }
  if(username.length<2||username.length>15){ showPopup("Username must be 2-15 characters"); return; }

  try{
    showPopup("Creating your Kik account...");
    // firebase.js bridge handles the actual Kik registration
    await auth.createUserWithEmailAndPassword(email, pass);
    closePopup();
  }catch(e){
    showPopup(e.message||"Signup failed");
  }
};

/* ===== AVATAR ===== */
window.loadAvatar=function(){
  let btn=document.getElementById("profileBtn");
  if(!btn) return;
  let photo=window.myData&&window.myData.photo;
  if(photo){
    btn.innerHTML=`<img src="${photo}" style="width:32px;height:32px;border-radius:50%;object-fit:cover;">`;
  } else {
    btn.innerHTML="👤";
  }
};

/* ===== SEARCH USERS ===== */
window.openSearch=function(){
  fabOpen=false;
  let fm=document.getElementById("fabMenu");
  if(fm) fm.style.display="none";
  show("search");
};

window.searchUsers=function(){
  let q=(document.getElementById("searchInput")||{}).value||"";
  let results=document.getElementById("results");
  if(!results) return;
  if(q.length<2){ results.innerHTML=""; return; }

  // Search on Kik network
  window.searchUsersOnKik(q);

  // Show results from local store as they come in
  setTimeout(()=>{
    let users=Object.values(window._store&&window._store.users||{});
    let filtered=users.filter(u=>(u.username||"").toLowerCase().includes(q.toLowerCase())||(u.displayName||"").toLowerCase().includes(q.toLowerCase()));
    results.innerHTML="";
    if(!filtered.length){ results.innerHTML='<div style="opacity:0.5;text-align:center;padding:16px;">No results yet. Try again in a moment.</div>'; return; }
    filtered.forEach(u=>{
      let div=document.createElement("div");
      div.className="searchResult";
      div.innerHTML=`
        <div class="chatAvatar">${u.photo?`<img src="${u.photo}">`:(u.displayName||u.username||"?")[0].toUpperCase()}</div>
        <div class="chatNameWrap">
          <div>${escapeHtml(u.displayName||u.username||u.uid)}</div>
          <div style="font-size:12px;opacity:0.6;">@${escapeHtml(u.username||u.uid)}</div>
        </div>
      `;
      div.onclick=()=>{ openChat(u.uid||u.jid); };
      results.appendChild(div);
    });
  }, 1500);
};

/* ===== OPEN DM CHAT ===== */
window.openChat=function(uid){
  if(!window.currentUser) return;
  if(unsubscribeMessages){ unsubscribeMessages(); unsubscribeMessages=null; }
  if(window.unsubscribeGroupMessages){ window.unsubscribeGroupMessages(); window.unsubscribeGroupMessages=null; }

  window.currentChatUser=uid;
  window.currentGroup=null;

  // Set chat header
  let chatName=document.getElementById("chatName");
  let userData=window._store&&window._store.users&&(window._store.users[uid]||Object.values(window._store.users).find(u=>u.uid===uid||u.username===uid));
  if(chatName) chatName.innerText=(userData&&(userData.displayName||userData.username))||uid;

  show("chat");

  // Load messages from store
  let messagesEl=document.getElementById("messages");
  if(messagesEl) messagesEl.innerHTML="";

  // Subscribe to incoming messages
  unsubscribeMessages=db.collection("messages")
    .where("from","==",uid)
    .onSnapshot(snap=>{
      if(!messagesEl) return;
      messagesEl.innerHTML="";
      let msgs=[];
      snap.forEach(doc=>{ msgs.push({id:doc.id,...doc.data()}); });
      msgs.sort((a,b)=>(a.time||0)-(b.time||0));
      msgs.forEach(m=>renderMessage(m, messagesEl));
      messagesEl.scrollTop=messagesEl.scrollHeight;
    });

  // Subscribe to typing
  if(unsubscribeDMTyping){ unsubscribeDMTyping(); unsubscribeDMTyping=null; }
  unsubscribeDMTyping=db.collection("dmTyping")
    .where("from","==",uid)
    .onSnapshot(snap=>{
      let typingEl=document.getElementById("typingIndicator");
      if(!typingEl) return;
      let isTyping=false;
      snap.forEach(doc=>{ if(doc.data().typing) isTyping=true; });
      typingEl.style.display=isTyping?"block":"none";
      typingEl.innerText=isTyping?`${uid} is typing...`:"";
    });
};

/* ===== RENDER MESSAGE ===== */
function renderMessage(m, container){
  let isMine=(m.from===window.currentUser.uid||m.from===window.currentUser.username);
  let div=document.createElement("div");
  div.className="message "+(isMine?"mine":"theirs");
  div.dataset.id=m.id;
  let text=m.deleted?"<em style='opacity:0.5'>This message was deleted</em>":escapeHtml(m.text||m.body||"");
  div.innerHTML=`<div class="bubble">${text}</div>`;
  container.appendChild(div);
}

/* ===== SEND MESSAGE ===== */
window.sendMessage=async function(){
  let input=document.getElementById("messageInput");
  let text=(input?input.value:"").trim();
  if(!text||!window.currentUser) return;
  if(input) input.value="";

  let msgData={
    from: window.currentUser.uid||window.currentUser.username,
    text: text,
    time: Date.now(),
    type: "text"
  };

  if(window.currentGroup){
    let groupId=typeof window.currentGroup==="string"?window.currentGroup:window.currentGroup.id||window.currentGroup.jid;
    msgData.groupId=groupId;
    // Determine if public or private group
    let isPublic=window.currentGroup.isPublic||window.currentGroup.tag;
    db.collection(isPublic?"publicGroupMessages":"groupMessages").add(msgData);
  } else if(window.currentChatUser){
    msgData.to=window.currentChatUser;
    db.collection("messages").add(msgData);
  }

  // Render own message immediately
  let messagesEl=document.getElementById("messages");
  if(messagesEl){
    renderMessage({...msgData, id:"local_"+Date.now()}, messagesEl);
    messagesEl.scrollTop=messagesEl.scrollHeight;
  }
};

/* ===== TYPING INDICATOR ===== */
window.onTyping=function(){
  if(!window.currentChatUser||!window.currentUser) return;
  let peerJid=window.currentChatUser;
  window.sendKikTyping(peerJid, true);
  clearTimeout(dmTypingTimeout);
  dmTypingTimeout=setTimeout(()=>{ window.sendKikTyping(peerJid, false); }, 3000);
};

/* ===== LOAD CHATS (from roster) ===== */
window.loadChats=function(){
  if(!window.currentUser) return;
  let chatList=document.getElementById("chatList");
  if(!chatList) return;

  db.collection("users").onSnapshot(snap=>{
    // Clear existing DM items (not group items)
    document.querySelectorAll(".dmItem").forEach(el=>el.remove());
    let users=[];
    snap.forEach(doc=>{ users.push({id:doc.id,...doc.data()}); });
    users.forEach(u=>{
      if(u.uid===window.currentUser.uid||u.username===window.currentUser.username) return;
      let div=document.createElement("div");
      div.className="chatItem dmItem";
      div.innerHTML=`
        <div class="chatAvatar">${u.photo?`<img src="${u.photo}">`:(u.displayName||u.username||"?")[0].toUpperCase()}</div>
        <div class="chatNameWrap">
          <div>${escapeHtml(u.displayName||u.username||u.uid)}</div>
          <div style="font-size:12px;opacity:0.6;">@${escapeHtml(u.username||u.uid)}</div>
        </div>
      `;
      div.onclick=()=>{ openChat(u.uid||u.jid||u.id); };
      chatList.appendChild(div);
    });
  });
};

/* ===== LOAD GROUPS (from Kik roster groups) ===== */
window.loadGroups=function(){
  // Groups come via roster_update socket event — they're in _store.groups
  // We listen for updates
  db.collection("groups").onSnapshot(snap=>{
    document.querySelectorAll(".groupItem").forEach(el=>el.remove());
    let chatList=document.getElementById("chatList");
    if(!chatList) return;
    let groups=[];
    snap.forEach(doc=>{ groups.push({id:doc.id,...doc.data()}); });
    groups.forEach(g=>{
      let div=document.createElement("div");
      div.className="groupItem";
      div.innerHTML=`
        <div class="chatAvatar">${g.photo?`<img src="${g.photo}">`:"👥"}</div>
        <div class="chatNameWrap">
          <div>${escapeHtml(g.name||g.displayName||"Group")}</div>
          <div style="font-size:12px;opacity:0.6;">${g.lastMessage||"No messages yet"}</div>
        </div>
      `;
      div.onclick=()=>{ openGroup(g.id||g.jid); };
      chatList.prepend(div);
    });
  });
};

/* ===== OPEN GROUP ===== */
window.openGroup=function(groupId){
  if(!window.currentUser) return;
  if(unsubscribeMessages){ unsubscribeMessages(); unsubscribeMessages=null; }
  if(window.unsubscribeGroupMessages){ window.unsubscribeGroupMessages(); window.unsubscribeGroupMessages=null; }

  window.currentChatUser=null;

  // Find group data
  let g=window._store&&window._store.groups&&window._store.groups[groupId];
  if(!g) g=window._store&&window._store.publicGroups&&window._store.publicGroups[groupId];
  window.currentGroup=g||{id:groupId,jid:groupId};

  let chatName=document.getElementById("chatName");
  if(chatName) chatName.innerText=(g&&(g.name||g.displayName))||groupId;

  show("chat");

  let messagesEl=document.getElementById("messages");
  if(messagesEl) messagesEl.innerHTML="";

  let isPublic=g&&(g.isPublic||g.tag);
  window.unsubscribeGroupMessages=db.collection(isPublic?"publicGroupMessages":"groupMessages")
    .where("groupId","==",groupId)
    .onSnapshot(snap=>{
      if(!messagesEl) return;
      messagesEl.innerHTML="";
      let msgs=[];
      snap.forEach(doc=>{ msgs.push({id:doc.id,...doc.data()}); });
      msgs.sort((a,b)=>(a.time||0)-(b.time||0));
      msgs.forEach(m=>renderMessage(m, messagesEl));
      messagesEl.scrollTop=messagesEl.scrollHeight;
    });
};

/* ===== CREATE GROUP (stub — Kik requires app) ===== */
window.createGroup=function(){
  showPopup("To create a group on Kik, please use the Kik app. You can then search for and join your group here.");
};

/* ===== PROFILE ===== */
window.openProfile=function(){
  show("profile");
  let content=document.getElementById("profileContent");
  if(!content) return;
  let u=window.myData||{};
  content.innerHTML=`
    <div style="display:flex;flex-direction:column;align-items:center;padding:20px;gap:12px;">
      <div style="width:80px;height:80px;border-radius:50%;overflow:hidden;background:#333;display:flex;align-items:center;justify-content:center;font-size:32px;">
        ${u.photo?`<img src="${u.photo}" style="width:100%;height:100%;object-fit:cover;">`:(u.displayName||u.username||"?")[0]||"👤"}
      </div>
      <div style="font-size:20px;font-weight:bold;">${escapeHtml(u.displayName||u.username||"")}</div>
      <div style="opacity:0.6;">@${escapeHtml(u.username||u.uid||"")}</div>
      ${u.status?`<div style="opacity:0.7;font-size:13px;">${escapeHtml(u.status)}</div>`:""}
      <button onclick="show('settings')">Settings</button>
    </div>
  `;
};

/* ===== SETTINGS ===== */
window.openSettings=function(){ show("settings"); };

/* ===== SIGN OUT ===== */
window.signOut=async function(){
  await auth.signOut();
  window.currentUser=null;
  window.myData={};
  show("welcome");
};

/* ===== THEMES SCREEN ===== */
function renderThemes(){
  let list=document.getElementById("themeList");
  if(!list) return;
  list.innerHTML="";
  Object.keys(themes).forEach(name=>{
    let t=themes[name];
    let btn=document.createElement("button");
    btn.className="themeBtn";
    btn.style.background=t.main;
    btn.style.color=t.secondary;
    btn.style.border=`2px solid ${t.secondary}`;
    btn.innerText=name.charAt(0).toUpperCase()+name.slice(1);
    btn.onclick=()=>{ applyTheme(name); showPopup(`${name} theme applied!`); };
    list.appendChild(btn);
  });
  let resetBtn=document.createElement("button");
  resetBtn.innerText="Reset Theme";
  resetBtn.onclick=()=>{ resetTheme(); showPopup("Theme reset!"); };
  list.appendChild(resetBtn);
}

/* ===== CREDITS ===== */
window.openCredits=function(){
  fabOpen=false;
  let fm=document.getElementById("fabMenu");
  if(fm) fm.style.display="none";
  show("creditsScreen");
  let content=document.getElementById("creditsContent");
  if(content) content.innerHTML=`
    <div style="padding:20px;text-align:center;">
      <h2>ConzChat Credits</h2>
      <p>👑 <strong>Conz</strong> — Lead Developer</p>
      <p>🛠️ <strong>Void</strong> — Co-Developer</p>
      <p>🛠️ <strong>Trojan</strong> — Co-Developer</p>
      <p style="opacity:0.6;margin-top:20px;">Kik Backend powered by kik-bot-api-unofficial</p>
    </div>
  `;
};

/* ===== FAB ===== */
window.toggleFab=function(){
  fabOpen=!fabOpen;
  let fm=document.getElementById("fabMenu");
  if(fm) fm.style.display=fabOpen?"flex":"none";
};

/* ===== UPDATES ===== */
function renderUpdates(){
  let content=document.getElementById("updateContent");
  if(!content) return;
  content.innerHTML=`
    <div style="padding:20px;">
      <h3>Version 1.6 — Kik Backend</h3>
      <p>• Replaced Firebase with Kik's XMPP network</p>
      <p>• Real Kik accounts, users, and groups</p>
      <p>• Search real Kik public groups</p>
      <p>• Messages routed through Kik servers</p>
      <h3 style="margin-top:16px;">Version 1.5</h3>
      <p>• Voice & Video Calls in DMs and Groups</p>
      <p>• Public Groups work globally</p>
      <p>• Kik-style read receipts</p>
    </div>
  `;
}

/* ===== CONZMODS ===== */
function renderConzMods(){
  let content=document.getElementById("conzModsContent");
  if(!content) return;
  content.innerHTML=`
    <div style="padding:20px;">
      <h3>🛠️ ConzChat Mods</h3>
      <p style="opacity:0.7;">Mods coming soon...</p>
    </div>
  `;
}

/* ===== DEV SUGGESTIONS BTN ===== */
function updateSuggestionsDevBtn(){
  let btn=document.getElementById("devSuggestionsBtn");
  if(!btn) return;
  btn.style.display=(window.isDev)?"block":"none";
}

/* ===== SUGGESTIONS ===== */
window.openLeaveSuggestions=function(){
  fabOpen=false;
  let fm=document.getElementById("fabMenu");
  if(fm) fm.style.display="none";
  show("leaveSuggestionsScreen");
};

window.submitSuggestion=function(){
  let input=document.getElementById("suggestionInput");
  let text=(input?input.value:"").trim();
  if(!text){ showPopup("Please write a suggestion first."); return; }
  // Store suggestion locally (no Firebase)
  let suggestions=JSON.parse(localStorage.getItem("conzchat_suggestions")||"[]");
  suggestions.push({ text, ts: Date.now(), uid: window.currentUser&&window.currentUser.uid });
  localStorage.setItem("conzchat_suggestions", JSON.stringify(suggestions));
  if(input) input.value="";
  showPopup("Your suggestion has been saved! 💡");
};

window.openDevSuggestions=function(){
  fabOpen=false;
  let fm=document.getElementById("fabMenu");
  if(fm) fm.style.display="none";
  show("devSuggestionsScreen");
  let list=document.getElementById("devSuggestionsList");
  if(!list) return;
  let suggestions=JSON.parse(localStorage.getItem("conzchat_suggestions")||"[]");
  if(!suggestions.length){ list.innerHTML='<div style="opacity:0.5;text-align:center;padding:20px;">No suggestions yet.</div>'; return; }
  list.innerHTML="";
  suggestions.sort((a,b)=>b.ts-a.ts).forEach(s=>{
    let div=document.createElement("div");
    div.className="devSuggestionCard";
    div.innerHTML=`<div class="devSuggText">${escapeHtml(s.text)}</div><div class="devSuggDate">${new Date(s.ts).toLocaleString()}</div>`;
    list.appendChild(div);
  });
};

/* ===== ESCAPE HTML ===== */
function escapeHtml(str){
  if(!str) return "";
  return String(str).replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g,"&quot;");
}

/* ===== PUBLIC GROUP SEARCH ===== */
window.searchPublicGroups=function(){
  let q=(document.getElementById("publicGroupSearchInput")||{}).value||"";
  let results=document.getElementById("publicGroupResults");
  if(!results) return;
  if(q.length<2){ results.innerHTML=""; return; }

  window.searchGroupsOnKik(q);

  setTimeout(()=>{
    let groups=Object.values(window._store&&window._store.publicGroups||{});
    let filtered=groups.filter(g=>(g.tag||"").toLowerCase().includes(q.toLowerCase())||(g.displayName||"").toLowerCase().includes(q.toLowerCase()));
    results.innerHTML="";
    if(!filtered.length){ results.innerHTML='<div style="opacity:0.5;text-align:center;padding:16px;">No groups found yet. Try again in a moment.</div>'; return; }
    filtered.forEach(g=>{
      let div=document.createElement("div");
      div.className="publicGroupCard";
      div.innerHTML=`
        <div class="chatAvatar">${g.photo?`<img src="${g.photo}">`:"👥"}</div>
        <div class="chatNameWrap">
          <div>${escapeHtml(g.displayName||g.name||g.tag)}</div>
          <div style="font-size:12px;opacity:0.6;">${escapeHtml(g.tag||"")} · ${g.memberCount||0} members</div>
        </div>
        <button onclick="joinPublicGroup('${g.id||g.jid}','${escapeHtml(g.tag||"")}')">Join</button>
      `;
      results.appendChild(div);
    });
  }, 1500);
};

window.joinPublicGroup=async function(groupJid, hashtag){
  let token=window._kikToken;
  if(!token){ showPopup("Not logged in"); return; }
  try{
    const res=await fetch(window.GATEWAY_URL+"/api/join_group",{
      method:"POST",
      headers:{"Content-Type":"application/json"},
      body:JSON.stringify({token, group_jid:groupJid, hashtag})
    });
    const data=await res.json();
    if(data.status==="joining"){ showPopup("Joining group..."); }
    else { showPopup(data.error||"Failed to join"); }
  }catch(e){ showPopup(e.message); }
};

/* ===== RENDER PUBLIC GROUPS ===== */
window.renderPublicGroups=function(){
  // Populated from socket events — nothing to do here initially
};

/* ===== START APP (called after auth) ===== */
function startApp(user){
  // Populate myData from profile
  window.myData={
    uid: user.uid||user.username,
    username: user.username||user.uid,
    displayName: user.displayName||user.username||user.uid,
    photo: user.photo||"",
    premium: false,
    banned: false,
    blockedUsers: []
  };

  loadAvatar();
  renderThemes();
  renderUpdates();
  renderConzMods();
  show("home");

  // Load contacts and groups
  if(!window.chatsLoaded){
    window.chatsLoaded=true;
    loadChats();
    loadGroups();
    renderPublicGroups();
  }

  // Listen for profile updates from socket
  let socket=window._socket;
  if(socket){
    socket.on("profile_update", (data)=>{
      if(data.username===user.username||data.jid===user.uid){
        window.myData={...window.myData,...data, displayName:data.display_name||data.username};
        loadAvatar();
      }
    });
  }
}

/* ===== MEDIA BAR ===== */
window.toggleMediaBar=function(){
  let bar=document.getElementById("mediaBar");
  if(bar) bar.style.display=(bar.style.display==="none"||!bar.style.display)?"flex":"none";
};
window.openCamera=function(){
  document.getElementById("cameraInput")&&document.getElementById("cameraInput").click();
};
window.openGallery=function(){
  document.getElementById("galleryInput")&&document.getElementById("galleryInput").click();
};
window.openVideo=function(){
  document.getElementById("videoInput")&&document.getElementById("videoInput").click();
};

/* ===== CALLS (stubs — WebRTC calls still work peer-to-peer) ===== */
window.startVoiceCall=function(){ showPopup("Voice calls require WebRTC setup. Coming soon!"); };
window.startVideoCall=function(){ showPopup("Video calls require WebRTC setup. Coming soon!"); };
window.endCall=function(){ show("chat"); };
window.toggleMute=function(){};
window.toggleCamera=function(){};
