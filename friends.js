/* ===== CONZCHAT FRIENDS & MESSAGE SEARCH ===== */

/* ---- Send friend request ---- */
window.sendFriendRequest=async function(toUid, toUsername){
  let myUid=window.currentUser.uid;
  if(myUid===toUid){ showPopup("That's you!"); return; }

  // Check if already friends or request pending
  let myDoc=await db.collection("users").doc(myUid).get();
  let myData=myDoc.data()||{};
  let friends=myData.friends||[];
  if(friends.includes(toUid)){ showPopup("You're already friends!"); return; }

  // Check for existing request
  let existing=await db.collection("friendRequests")
    .where("from","==",myUid)
    .where("to","==",toUid)
    .where("status","==","pending")
    .get();
  if(!existing.empty){ showPopup("Request already sent!"); return; }

  await db.collection("friendRequests").add({
    from: myUid,
    fromName: myData.displayName||myData.username,
    fromPhoto: myData.photo||"",
    to: toUid,
    toName: toUsername,
    status: "pending",
    time: Date.now()
  });
  showPopup(`Friend request sent to @${toUsername}!`);
  if(navigator.vibrate) navigator.vibrate(20);
};

/* ---- Open friend requests inbox ---- */
window.openFriendRequests=function(){
  let myUid=window.currentUser.uid;
  db.collection("friendRequests")
    .where("to","==",myUid)
    .where("status","==","pending")
    .get()
    .then(snap=>{
      let old=document.getElementById("friendRequestsModal");
      if(old) old.remove();

      let modal=document.createElement("div");
      modal.id="friendRequestsModal";
      modal.className="friendRequestsModal";

      if(snap.empty){
        modal.innerHTML=`<div class="frModalHeader"><h3>Friend Requests</h3><button onclick="document.getElementById('friendRequestsModal').remove()">✕</button></div><div class="frEmpty">No pending requests</div>`;
      } else {
        let rows="";
        snap.forEach(doc=>{
          let r=doc.data();
          rows+=`<div class="frRow">
            <div class="frAvatar">${r.fromPhoto?`<img src="${r.fromPhoto}">`:"👤"}</div>
            <div class="frInfo"><div class="frName">${r.fromName}</div></div>
            <div class="frActions">
              <button class="frAccept" onclick="acceptFriendRequest('${doc.id}','${r.from}')">✓</button>
              <button class="frDecline" onclick="declineFriendRequest('${doc.id}')">✕</button>
            </div>
          </div>`;
        });
        modal.innerHTML=`<div class="frModalHeader"><h3>Friend Requests</h3><button onclick="document.getElementById('friendRequestsModal').remove()">✕</button></div>${rows}`;
      }
      document.body.appendChild(modal);
    });
};

window.acceptFriendRequest=async function(reqId, fromUid){
  let myUid=window.currentUser.uid;
  // Add each other as friends
  await db.collection("users").doc(myUid).update({ friends: firebase.firestore.FieldValue.arrayUnion(fromUid) });
  await db.collection("users").doc(fromUid).update({ friends: firebase.firestore.FieldValue.arrayUnion(myUid) });
  await db.collection("friendRequests").doc(reqId).update({ status:"accepted" });
  let modal=document.getElementById("friendRequestsModal");
  if(modal) modal.remove();
  showPopup("Friend request accepted!");
  if(navigator.vibrate) navigator.vibrate([20,10,20]);
};

window.declineFriendRequest=async function(reqId){
  await db.collection("friendRequests").doc(reqId).update({ status:"declined" });
  let modal=document.getElementById("friendRequestsModal");
  if(modal) modal.remove();
};

/* ---- Friends count badge on home ---- */
window.loadFriendRequestBadge=function(){
  let myUid=window.currentUser?.uid;
  if(!myUid) return;
  db.collection("friendRequests")
    .where("to","==",myUid)
    .where("status","==","pending")
    .onSnapshot(snap=>{
      let badge=document.getElementById("friendReqBadge");
      if(!badge) return;
      if(snap.size>0){
        badge.textContent=snap.size>9?"9+":snap.size;
        badge.style.display="flex";
      } else {
        badge.style.display="none";
      }
    });
};

/* ---- In-chat message search ---- */
window.openChatSearch=function(){
  let old=document.getElementById("chatSearchBar");
  if(old){ old.remove(); return; }

  let bar=document.createElement("div");
  bar.id="chatSearchBar";
  bar.className="chatSearchBar";
  bar.innerHTML=`<input type="text" id="chatSearchInput" placeholder="Search messages..." oninput="runChatSearch(this.value)"><button onclick="document.getElementById('chatSearchBar').remove();clearChatSearchHighlights()">✕</button>`;

  let chatScreen=document.getElementById("chat");
  let topbar=chatScreen?chatScreen.querySelector(".topbar"):null;
  if(topbar) topbar.parentNode.insertBefore(bar, topbar.nextSibling);
  else document.body.appendChild(bar);

  setTimeout(()=>{ let inp=document.getElementById("chatSearchInput"); if(inp) inp.focus(); },100);
};

window.runChatSearch=function(query){
  clearChatSearchHighlights();
  if(!query||!query.trim()) return;
  let q=query.toLowerCase();
  let bubbles=document.querySelectorAll("#messages .msgText");
  let first=null;
  bubbles.forEach(b=>{
    if(b.textContent.toLowerCase().includes(q)){
      b.classList.add("searchHighlight");
      if(!first) first=b;
    }
  });
  if(first) first.scrollIntoView({behavior:"smooth",block:"center"});
};

window.clearChatSearchHighlights=function(){
  document.querySelectorAll(".searchHighlight").forEach(el=>el.classList.remove("searchHighlight"));
};

/* ---- Disappearing messages toggle ---- */
window.toggleDisappearing=function(durationMs){
  if(!window.currentChatUser && !window.currentGroup){ showPopup("Open a chat first"); return; }
  let chatId = window.currentChatUser
    ? [window.currentUser.uid, window.currentChatUser].sort().join("_")
    : (window.currentGroup?.id || window.currentGroup);

  if(!durationMs){
    db.collection("chatSettings").doc(chatId).set({ disappearing:false, duration:0 },{merge:true});
    showPopup("Disappearing messages off");
    return;
  }
  db.collection("chatSettings").doc(chatId).set({ disappearing:true, duration:durationMs },{merge:true});
  let label = durationMs===3600000?"1 hour" : durationMs===86400000?"24 hours" : "7 days";
  showPopup(`Disappearing messages: ${label}`);
};

/* ---- Auto-delete expired disappearing messages ---- */
window.cleanupDisappearingMessages=async function(){
  let now=Date.now();
  // DMs
  let dmSnap=await db.collection("messages").where("disappearsAt","<",now).get();
  dmSnap.forEach(doc=>doc.ref.delete());
  // Group messages
  let grpSnap=await db.collection("groupMessages").where("disappearsAt","<",now).get();
  grpSnap.forEach(doc=>doc.ref.delete());
};
// Run cleanup on load and every 5 minutes
window.cleanupDisappearingMessages();
setInterval(window.cleanupDisappearingMessages, 5*60*1000);
