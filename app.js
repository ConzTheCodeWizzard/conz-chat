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
textEl.style.color=hsl(${hue},100%,60%); textEl.style.textShadow=0 0 12px hsl(${hue},100%,60%);
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
// Show dev Suggestions inbox button in FAB if this is the dev account
updateSuggestionsDevBtn();
// Start session guard — kicks this device if another login happens
startSessionGuard(user.uid);

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
// Conz AI row is injected automatically by the MutationObserver in conzai.js
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

/* ===== SESSION GUARD ===== */
let mySessionId=null;
let sessionUnsubscribe=null;

function startSessionGuard(uid){
// Generate a unique ID for this session
mySessionId = Date.now().toString(36) + Math.random().toString(36).slice(2);
// Unsubscribe any previous listener first
if(sessionUnsubscribe){ sessionUnsubscribe(); sessionUnsubscribe=null; }
// Write our sessionId, then wait 2 seconds before subscribing so the
// initial onSnapshot fire (which always fires immediately) is safely past
// the write and won't be mistaken for a foreign login.
db.collection("sessions").doc(uid).set({ sessionId: mySessionId, ts: Date.now() }).then(()=>{
setTimeout(()=>{
// Double-check we haven't already been unsubscribed (e.g. user logged out)
if(!auth.currentUser || auth.currentUser.uid !== uid) return;
sessionUnsubscribe = db.collection("sessions").doc(uid).onSnapshot(snap=>{
if(!snap.exists) return;
let data = snap.data();
// If the sessionId in Firestore no longer matches ours, someone else logged in
if(data.sessionId && data.sessionId !== mySessionId){
if(sessionUnsubscribe){ sessionUnsubscribe(); sessionUnsubscribe=null; }
showSessionKickedPopup();
}
});
}, 2000);
});
}

function showSessionKickedPopup(){
// Create a full-screen overlay that can't be dismissed
let overlay = document.createElement('div');
overlay.id = 'sessionKickedOverlay';
overlay.innerHTML = &lt;div class="sessionKickedBox"&gt; &lt;div class="sessionKickedIcon"&gt;⚠️&lt;/div&gt; &lt;div class="sessionKickedTitle"&gt;Account Logged In Elsewhere&lt;/div&gt; &lt;div class="sessionKickedText"&gt;Your account has been logged into on another device. You have been signed out for security.&lt;/div&gt; &lt;button class="sessionKickedBtn" onclick="document.getElementById('sessionKickedOverlay').remove(); auth.signOut();"&gt;OK&lt;/button&gt; &lt;/div&gt;;
document.body.appendChild(overlay);
// Auto sign out after 4 seconds even if they don't tap OK
setTimeout(()=>{ auth.signOut(); }, 4000);
}

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
email:email,
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
profileContent.innerHTML=&lt;div class="profileCover" id="profileCoverArea" style="${coverStyle}">
${isMe?<button class="coverEditBtn" onclick="pickCoverPhoto()">📷 Cover</button>:''} &lt;/div&gt; &lt;div class="profileAvatarWrap"&gt; &lt;div class="avatar" ${isMe?'onclick="pickImage()"':''}>
${u.photo?:<div style="font-size:40px;">👤</div>} &lt;/div&gt; &lt;/div&gt; &lt;div class="displayName"&gt;${u.displayName||u.username}</div>
 {u.premium?<div class="premiumBadge"&gt;💎 Premium User&lt;/div>:""}
<div class="username">@ {u.status?<div class="profileStatus"&gt;${u.status}</div>:isMe?<div class="profileStatus profileStatusEmpty" onclick="editStatus()">+ Add a status</div>:''}${isMe&&u.status?<div class="profileStatusEdit" onclick="editStatus()"&gt;✏️ Edit status&lt;/div>:''}
 {!isMe?&lt;div class="profileActions"&gt; &lt;button class="profileActionBtn" onclick="openChat('${uid}','${u.username}','${u.photo||''}')">💬 Message</button>
<button class="profileActionBtn blockBtn" onclick="blockUser('${uid}','${u.username}')">🚫 Block</button>
</div>
:""};
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


/* ===== STATUS ===== */
window.editStatus=function(){
let current=(window.myData&&window.myData.status)||'';
let inp=document.getElementById('statusInput');
if(inp) inp.value=current;
document.getElementById('editStatusPopup').style.display='flex';
if(inp) setTimeout(()=>inp.focus(),100);
};

window.submitStatus=function(){
let val=(document.getElementById('statusInput').value||'').trim().slice(0,60);
document.getElementById('editStatusPopup').style.display='none';
db.collection("users").doc(window.currentUser.uid).update({status:val}).then(()=>{
if(window.myData) window.myData.status=val;
openProfile(window.currentUser.uid);
});
};

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
let sw=img.widthscale, sh=img.heightscale;
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
let popup=document.getElementById('changeNamePopup');
if(!popup) return;
let inp=document.getElementById('newDisplayNameInput');
if(inp) inp.value=window.myData.displayName||window.myData.username||'';
popup.style.display='flex';
if(inp) setTimeout(()=>inp.focus(),100);
};

window.submitChangeDisplayName=function(){
let inp=document.getElementById('newDisplayNameInput');
let newName=inp?inp.value.trim():'';
if(!newName){ showPopup("Please enter a name"); return; }
document.getElementById('changeNamePopup').style.display='none';
db.collection("users").doc(window.currentUser.uid).update({displayName:newName});
window.myData.displayName=newName;
openProfile(window.currentUser.uid);
};

function loadAvatar(){
profileBtn.innerHTML=window.myData.photo
?<img src="${window.myData.photo}" style="width:30px;height:30px;border-radius:50%;pointer-events:none;">`
:"👤";
}

/* ===== BLOCK USER ===== */
let _pendingBlockUid=null, _pendingBlockUsername=null;
window.blockUser=function(uid,username){
_pendingBlockUid=uid; _pendingBlockUsername=username;
let title=document.getElementById('blockConfirmTitle');
let text=document.getElementById('blockConfirmText');
if(title) title.textContent=Block @${username}?`;
if(text) text.textContent="They won't be able to message you.";
let popup=document.getElementById('blockConfirmPopup');
if(popup) popup.style.display='flex';
};

window.confirmBlockUser=async function(){
document.getElementById('blockConfirmPopup').style.display='none';
let uid=_pendingBlockUid, username=_pendingBlockUsername;
if(!uid) return;
try{
let myRef=db.collection("users").doc(window.currentUser.uid);
let myDoc=await myRef.get();
let blocked=(myDoc.data().blockedUsers||[]);
if(!blocked.includes(uid)){ blocked.push(uid); await myRef.update({blockedUsers:blocked}); }
showPopup(@${username} has been blocked.`);
show("home");
}catch(err){ showPopup(err.message); }
};

window.unblockUser=async function(uid,username){
try{
let myRef=db.collection("users").doc(window.currentUser.uid);
let myDoc=await myRef.get();
let blocked=(myDoc.data().blockedUsers||[]).filter(b=>b!==uid);
await myRef.update({blockedUsers:blocked});
showPopup(@${username} has been unblocked.`);
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
div.innerHTML=&lt;div class="chatAvatar"&gt;${u.photo?<img src="${u.photo}">:""}&lt;/div&gt; &lt;div&gt;${u.username}</div>
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
window._conzAIMode=false;
// Hide bot badge when opening a normal chat
let topbarBadge=document.getElementById('chatTopbarBadge');
if(topbarBadge){ topbarBadge.style.display='none'; topbarBadge.textContent=''; }

if(unsubscribeMessages) unsubscribeMessages();
if(unsubscribeDMTyping) unsubscribeDMTyping();
if(window.unsubscribeGroupMessages){ window.unsubscribeGroupMessages(); window.unsubscribeGroupMessages=null; }
if(window.unsubscribePublicGroupMessages){ window.unsubscribePublicGroupMessages(); window.unsubscribePublicGroupMessages=null; }

// Mark this conversation as seen now
lastSeenTimes[uid]=Date.now();
saveLastSeen();

show("chat");

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
// Only update receipts if Disable Read Receipts mod is OFF
if(!(window.conzMods && window.conzMods.disableReceipts)){
if(m.to===window.currentUser.uid && m.from===uid && m.receipt==="S")
db.collection("messages").doc(doc.id).update({receipt:"D"});
if(m.to===window.currentUser.uid && m.from===uid && m.receipt!=="R")
db.collection("messages").doc(doc.id).update({receipt:"R"});
}

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
if(m.receipt==="R") receiptIcon=<span class="receiptRead" title="Read"&gt;✓✓&lt;/span>;
else if(m.receipt==="D") receiptIcon=<span class="receiptDelivered" title="Delivered"&gt;✓✓&lt;/span>;
else receiptIcon=<span class="receiptSent" title="Sent"&gt;✓&lt;/span>;
}

// Reply-to preview
let replyHtml="";
if(m.replyTo){
replyHtml=<div class="replyPreview"&gt;&lt;span class="replyBar"&gt;&lt;/span&gt;&lt;span class="replyText"&gt;${m.replyTo.text||"📎 Media"}</span></div>`;
}

// Reactions display
let reactionsHtml="";
if(m.reactions && Object.keys(m.reactions).length>0){
let counts={};
Object.values(m.reactions).forEach(e=>{ counts[e]=(counts[e]||0)+1; });
reactionsHtml=<div class="msgReactions">+Object.entries(counts).map(([e,c])=><span class="reactionBubble"&gt;${e}${c&gt;1?" "+c:""}&lt;/span>).join("")+</div>;
}

// Handle image/video/voice messages
let contentHtml="";
if(m.type==="image"){
if(m.viewOnce){
if(m.viewed && !isMine){
contentHtml=<div class="viewOnceOpened"&gt;🔥 Photo opened&lt;/div>;
} else if(!isMine){
contentHtml=<div class="viewOnceThumb" onclick="openViewOnce('${msgId}','image','latex
{m.url}')"&gt;&lt;span class="viewOnceIcon"&gt;🔥&lt;/span&gt;&lt;span class="viewOnceLabel"&gt;Tap to open · Disappears after viewing&lt;/span&gt;&lt;/div>`; } else { contentHtml=`<div class="viewOnceSent"&gt;🔥 Disappearing photo

{m.viewed?' · <span style="color:#ff6b6b">Opened</span>':' · Unopened'}</div>; } } else { contentHtml=; if(m.isCamera) contentHtml+=<div class="msgCameraLabel">📷 Camera</div>; } } else if(m.type==="video"){ if(m.viewOnce){ if(m.viewed && !isMine){ contentHtml=<div class="viewOnceOpened">🔥 Video opened</div>; } else if(!isMine){ contentHtml=<div class="viewOnceThumb" onclick="openViewOnce(' {m.url}')"><span class="viewOnceIcon">🔥</span><span class="viewOnceLabel">Tap to open · Disappears after viewing</span></div>; } else { contentHtml=<div class="viewOnceSent">🔥 Disappearing videolatex
{m.viewed?' · &lt;span style="color:#ff6b6b"&gt;Opened&lt;/span&gt;':' · Unopened'}&lt;/div>`; } } else { contentHtml=`<video src="

{m.url}" class="msgVideo" controls playsinline></video>; if(m.isCamera) contentHtml+=<div class="msgCameraLabel">📷 Camera</div>; } } else if(m.type==="voice"){ let transcriptHtml = m.transcript ?<div class="voiceTranscript">" {m.url}" controls class="voiceAudio"></audio><div class="voiceLabel">🎤️ Voice Note</div>latex
{transcriptHtml}&lt;/div>`; } else if(m.type==="gif"){ contentHtml=`<img src="

{m.url}" class="msgImage msgGif" style="border-radius:10px;max-width:220px;" onclick="viewFullImage('latex
{m.url}')">`; } else { contentHtml=`<div class="msgText"&gt;

{m.text||""}</div>`;
}

bubble.innerHTML=${replyHtml}
${contentHtml} ${reactionsHtml}
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
if(window.myData.photo) avatar.innerHTML=<img src="${window.myData.photo}">; wrap.appendChild(bubble); wrap.appendChild(avatar); } else { if(photo) avatar.innerHTML=`;
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
let emojiRow=emojis.map(e=><button class="reactionEmojiBtn" onclick="addReaction('${msgId}',' {e}</button>`).join("");

sheet.innerHTML=&lt;div class="actionSheetReactions"&gt;${emojiRow}</div>
<div class="actionSheetDivider"></div>
<button class="actionSheetBtn" onclick="startReply('${msgId}','${(m.text||'📎 Media').replace(/'/g,"'")}','${senderName}')"&gt;↩️ Reply&lt;/button&gt; ${isMine?<button class="actionSheetBtn actionSheetDelete" onclick="deleteMsg('${msgId}')">🗑️ Delete</button>:""} &lt;button class="actionSheetBtn" onclick="document.getElementById('msgActionSheet').remove()"&gt;Cancel&lt;/button&gt;;

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
[reactions.${uid}`]: emoji
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
bar.innerHTML=<span class="replyBarLine"&gt;&lt;/span&gt;&lt;div class="replyBarContent"&gt;&lt;span class="replyBarName"&gt;${senderName}</span><span class="replyBarText"> {text.length>60?"...":""}</span></div><button class="replyBarClose" onclick="cancelReply()">✕</button>`;
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
typingEl.innerHTML=<span class="typingDots"&gt;${name} is typing<span class="dot1">.</span><span class="dot2">.</span><span class="dot3">.</span></span>`;
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

// Route to Conz AI if in bot chat mode
if(window._conzAIMode && window.sendConzAIMessage){
let text=val;
msgInput.value="";
sendBtn.classList.remove("active");
msgInput.focus();
window.sendConzAIMessage(text);
return;
}

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
// Skip if Disable Typing mod is ON
if(window.conzMods && window.conzMods.disableTyping) return;
db.collection("dmTyping").doc(window.currentUser.uid+""+window.currentChatUser).set({
from:window.currentUser.uid,to:window.currentChatUser,typing:true,ts:Date.now()
});
}
function clearDMTyping(){
if(!window.currentChatUser) return;
db.collection("dmTyping").doc(window.currentUser.uid+""+window.currentChatUser).set({
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
div.innerHTML=&lt;div class="chatAvatar"&gt;${d.photo?<img src="${d.photo}">:""} &lt;/div&gt; &lt;div class="chatNameWrap"&gt; &lt;div class="chatItemName"&gt;${d.username}</div>
latex
{unread&gt;0?`<div class="unreadBadge"&gt;

{unread>99?"99+":unread}</div>:""} &lt;/div&gt;;
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
overlay.innerHTML=<img src="${url}" style="max-width:95%;max-height:90%;border-radius:8px;">`;
overlay.onclick=()=>overlay.remove();
document.body.appendChild(overlay);
};

/* ===== VIEW ONCE (DISAPPEARING MEDIA) ===== */
window.openViewOnce=function(msgId, type, url){
// Show the media fullscreen
let overlay=document.createElement("div");
overlay.style.cssText="position:fixed;top:0;left:0;width:100%;height:100%;background:#000;z-index:99999;display:flex;flex-direction:column;justify-content:center;align-items:center;";
let closeBtn=<div style="position:absolute;top:16px;right:16px;color:#fff;font-size:28px;cursor:pointer;z-index:2;" id="viewOnceClose"&gt;✕&lt;/div>;
let label=<div style="position:absolute;top:16px;left:50%;transform:translateX(-50%);color:rgba(255,255,255,0.7);font-size:13px;background:rgba(0,0,0,0.6);padding:4px 12px;border-radius:20px;"&gt;🔥 Disappears when you close&lt;/div>;
if(type==="image"){
overlay.innerHTML=closeBtn+label+<img src="${url}" style="max-width:100%;max-height:90%;object-fit:contain;">; } else { overlay.innerHTML=closeBtn+label+<video src="${url}" autoplay controls playsinline style="max-width:100%;max-height:90%;"&gt;&lt;/video>;
}
document.body.appendChild(overlay);

// Mark as viewed in Firestore and wipe the URL
let collection = window.currentGroup && window.currentGroup.tag ? "publicGroupMessages"
: window.currentGroup ? "groupMessages" : "messages";
db.collection(collection).doc(msgId).update({ viewed:true, url:"" }).catch(()=>{});

// Close on X or tap outside media
overlay.addEventListener("click", function(e){
if(e.target===overlay || e.target.id==="viewOnceClose") overlay.remove();
});
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

/* ===== GIF PICKER ===== */
const GIPHY_KEY = 'gvnL7xPoArGXv6249XCJl87Hto1qv9wa'; // Giphy API key
let _gifSearchTimeout = null;

window.openGifPicker = function(){
let picker = document.getElementById('gifPicker');
if(!picker) return;
picker.style.display = picker.style.display === 'none' ? 'block' : 'none';
if(picker.style.display === 'block'){
loadTrendingGifs();
let inp = document.getElementById('gifSearchInput');
if(inp){ inp.value=''; setTimeout(()=>inp.focus(),100); }
}
};

window.closeGifPicker = function(){
let picker = document.getElementById('gifPicker');
if(picker) picker.style.display = 'none';
};

function loadTrendingGifs(){
fetch(https://api.giphy.com/v1/gifs/trending?api_key=${GIPHY_KEY}&limit=18&rating=pg-13`)
.then(r=>r.json()).then(data=>renderGifResults(data.data||[]))
.catch(()=>{ document.getElementById('gifResults').innerHTML='<div style="color:rgba(255,255,255,0.4);font-size:13px;grid-column:span 3;text-align:center;padding:20px;">Could not load GIFs</div>'; });
}

window.searchGifs = function(query){
clearTimeout(_gifSearchTimeout);
if(!query.trim()){ loadTrendingGifs(); return; }
_gifSearchTimeout = setTimeout(()=>{
fetch(https://api.giphy.com/v1/gifs/search?api_key=${GIPHY_KEY}&q=${encodeURIComponent(query)}&limit=18&rating=pg-13)
.then(r=>r.json()).then(data=>renderGifResults(data.data||[]))
.catch(()=>{});
}, 400);
};

function renderGifResults(results){
let container = document.getElementById('gifResults');
if(!container) return;
container.innerHTML = '';
if(!results.length){
container.innerHTML='<div style="color:rgba(255,255,255,0.4);font-size:13px;grid-column:span 3;text-align:center;padding:20px;">No GIFs found</div>';
return;
}
results.forEach(item=>{
let url = item.images && item.images.original ? item.images.original.url : '';
let preview = item.images && item.images.fixed_width_small ? item.images.fixed_width_small.url : url;
if(!url) return;
let img = document.createElement('img');
img.src = preview;
img.style.cssText = 'width:100%;border-radius:8px;cursor:pointer;object-fit:cover;aspect-ratio:1;';
img.onclick = ()=>sendGif(url);
container.appendChild(img);
});
}

window.sendGif = function(url){
closeGifPicker();
// Close media bar too
let mb = document.getElementById('mediaBar');
if(mb) mb.style.display='none';
// Send as a gif type message
let msgData = {
from: window.currentUser.uid,
time: Date.now(),
type: 'gif',
url: url,
text: '',
isCamera: false,
viewOnce: false,
viewed: false
};
if(window.currentGroup && typeof window.currentGroup === 'object' && window.currentGroup.tag){
db.collection('publicGroupMessages').add({...msgData, groupId: window.currentGroup.id});
} else if(window.currentGroup){
db.collection('groupMessages').add({...msgData, groupId: typeof window.currentGroup==='string'?window.currentGroup:window.currentGroup.id});
} else if(window.currentChatUser){
db.collection('messages').add({...msgData, to: window.currentChatUser, receipt:'S'});
}
};

/* ===== POPUP ===== */
window.showPopup=function(text){
document.getElementById("popupText").innerText=text;
document.getElementById("customPopup").style.display="flex";
};
window.closePopup=function(){
document.getElementById("customPopup").style.display="none";
};
window.showTitledPopup=function(title,text){
let titleEl=document.getElementById('popupTitle');
let textEl=document.getElementById('popupText');
if(titleEl){ titleEl.innerText=title; titleEl.style.display='block'; }
if(textEl) textEl.innerText=text;
document.getElementById('customPopup').style.display='flex';
};
const _origClosePopup=window.closePopup;
window.closePopup=function(){
let titleEl=document.getElementById('popupTitle');
if(titleEl){ titleEl.innerText=''; titleEl.style.display='none'; }
_origClosePopup();
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
// Settings now opens a full screen — toggleSettings kept as no-op for safety
window.toggleSettings=function(){ show('settings'); };

window.openChangeDisplayName=function(){
let popup=document.getElementById('changeNamePopup');
let inp=document.getElementById('newDisplayNameInput');
if(inp) inp.value=window.myData.displayName||window.myData.username||'';
if(popup) popup.style.display='flex';
setTimeout(()=>{ if(inp) inp.focus(); },100);
};

window.submitChangeDisplayName=async function(){
let inp=document.getElementById('newDisplayNameInput');
let name=(inp?inp.value:'').trim();
if(!name){ showPopup('Please enter a display name.'); return; }
try{
await db.collection('users').doc(window.currentUser.uid).update({displayName:name});
window.myData.displayName=name;
document.getElementById('changeNamePopup').style.display='none';
showPopup('Display name updated!');
// Refresh profile if open
if(document.getElementById('profile')?.classList.contains('active')){
openProfile(window.currentUser.uid);
}
}catch(e){ showPopup('Failed to update: '+e.message); }
};

window.openChangePassword=function(){
let popup=document.getElementById('changePasswordPopup');
let cur=document.getElementById('currentPasswordInput');
let nw=docum
