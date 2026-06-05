/* =============================================
   ConzChat Calls v1.5 — Voice & Video
   Simple ring/answer flow via Firestore signaling
   No manual ID needed — just tap call, it rings them
   ============================================= */

let localStream = null;
let peerConnection = null;
let currentCallDoc = null;
let currentCallType = "video";
let callUnsubscribe = null;
let callCandidateUnsub = null;
let incomingCallUnsubscribe = null;
let activeCallId = null;

const ICE_SERVERS = {
  iceServers: [
    { urls: "stun:stun.l.google.com:19302" },
    { urls: "stun:stun1.l.google.com:19302" },
    { urls: "stun:stun2.l.google.com:19302" }
  ]
};

/* ===== START CALL ===== */
window.startCall = async function(type){
  currentCallType = type || "video";

  if(!window.currentChatUser && !window.currentGroup){
    showPopup("Open a chat first to start a call.");
    return;
  }

  try{
    localStream = await navigator.mediaDevices.getUserMedia({
      video: currentCallType === "video",
      audio: true
    });

    document.getElementById("localVideo").srcObject = localStream;
    document.getElementById("remoteVideo").srcObject = null;

    let callTarget = "";
    if(window.currentChatUser){
      callTarget = document.getElementById("chatName").textContent.replace(/[•⚫]/g,"").trim();
    } else if(window.currentGroup){
      callTarget = typeof window.currentGroup === "object"
        ? (window.currentGroup.displayName || window.currentGroup.name || "Group")
        : "Group";
    }

    document.getElementById("callScreenTitle").textContent =
      (currentCallType === "video" ? "📹 " : "🎙️ ") + callTarget;
    document.getElementById("callStatus").textContent = "Ringing...";

    show("videoCall");

    // Create Firestore call doc
    let callDocRef = db.collection("calls").doc();
    currentCallDoc = callDocRef;
    activeCallId = callDocRef.id;

    peerConnection = new RTCPeerConnection(ICE_SERVERS);

    localStream.getTracks().forEach(track => {
      peerConnection.addTrack(track, localStream);
    });

    peerConnection.ontrack = event => {
      document.getElementById("remoteVideo").srcObject = event.streams[0];
      document.getElementById("callStatus").textContent = "Connected";
    };

    peerConnection.onicecandidate = event => {
      if(event.candidate){
        callDocRef.collection("offerCandidates").add(event.candidate.toJSON());
      }
    };

    peerConnection.onconnectionstatechange = () => {
      let state = peerConnection.connectionState;
      if(state === "disconnected" || state === "failed"){
        document.getElementById("callStatus").textContent = state === "failed" ? "Call Failed" : "Disconnected";
      }
    };

    const offerDesc = await peerConnection.createOffer();
    await peerConnection.setLocalDescription(offerDesc);

    let callData = {
      offer: { sdp: offerDesc.sdp, type: offerDesc.type },
      callerId: window.currentUser.uid,
      callerName: window.myData?.displayName || window.myData?.username || "Someone",
      callerPhoto: window.myData?.photo || "",
      callType: currentCallType,
      status: "ringing",
      created: Date.now()
    };

    if(window.currentChatUser){
      callData.callTo = window.currentChatUser;
    } else if(window.currentGroup){
      callData.callGroup = typeof window.currentGroup === "object"
        ? window.currentGroup.id
        : window.currentGroup;
      // For group calls, notify all members
      let members = (window.currentGroup.members || []).filter(m => m !== window.currentUser.uid);
      callData.callToGroup = members;
    }

    await callDocRef.set(callData);

    // Listen for answer
    callUnsubscribe = callDocRef.onSnapshot(snapshot => {
      const data = snapshot.data();
      if(!data) return;
      if(data.status === "declined"){
        document.getElementById("callStatus").textContent = "Call Declined";
        setTimeout(endCall, 2000);
        return;
      }
      if(!peerConnection.currentRemoteDescription && data.answer){
        peerConnection.setRemoteDescription(new RTCSessionDescription(data.answer));
        document.getElementById("callStatus").textContent = "Connecting...";
      }
    });

    // Listen for answer ICE candidates
    callCandidateUnsub = callDocRef.collection("answerCandidates").onSnapshot(snapshot => {
      snapshot.docChanges().forEach(change => {
        if(change.type === "added"){
          peerConnection.addIceCandidate(new RTCIceCandidate(change.doc.data())).catch(()=>{});
        }
      });
    });

  }catch(err){
    showPopup("Could not start call: " + err.message);
    endCall();
  }
};

/* ===== ANSWER CALL ===== */
window.answerIncomingCall = async function(callId){
  if(!callId) return;

  try{
    let callDocRef = db.collection("calls").doc(callId);
    let callData = (await callDocRef.get()).data();
    if(!callData){ showPopup("Call not found."); return; }

    currentCallType = callData.callType || "video";
    currentCallDoc = callDocRef;
    activeCallId = callId;

    localStream = await navigator.mediaDevices.getUserMedia({
      video: currentCallType === "video",
      audio: true
    });

    document.getElementById("localVideo").srcObject = localStream;
    document.getElementById("remoteVideo").srcObject = null;
    document.getElementById("callScreenTitle").textContent =
      (currentCallType === "video" ? "📹 " : "🎙️ ") + (callData.callerName || "Incoming Call");
    document.getElementById("callStatus").textContent = "Connecting...";

    // Hide incoming banner
    let banner = document.getElementById("incomingCallBanner");
    if(banner) banner.style.display = "none";

    show("videoCall");

    peerConnection = new RTCPeerConnection(ICE_SERVERS);

    localStream.getTracks().forEach(track => {
      peerConnection.addTrack(track, localStream);
    });

    peerConnection.ontrack = event => {
      document.getElementById("remoteVideo").srcObject = event.streams[0];
      document.getElementById("callStatus").textContent = "Connected";
    };

    peerConnection.onicecandidate = event => {
      if(event.candidate){
        callDocRef.collection("answerCandidates").add(event.candidate.toJSON());
      }
    };

    await peerConnection.setRemoteDescription(new RTCSessionDescription(callData.offer));
    const answerDesc = await peerConnection.createAnswer();
    await peerConnection.setLocalDescription(answerDesc);

    await callDocRef.update({
      answer: { type: answerDesc.type, sdp: answerDesc.sdp },
      status: "answered"
    });

    // Listen for offer ICE candidates
    callDocRef.collection("offerCandidates").onSnapshot(snapshot => {
      snapshot.docChanges().forEach(change => {
        if(change.type === "added"){
          peerConnection.addIceCandidate(new RTCIceCandidate(change.doc.data())).catch(()=>{});
        }
      });
    });

  }catch(err){
    showPopup("Could not answer call: " + err.message);
    endCall();
  }
};

/* ===== END CALL ===== */
window.endCall = function(){
  if(localStream){
    localStream.getTracks().forEach(track => track.stop());
    localStream = null;
  }
  if(peerConnection){ peerConnection.close(); peerConnection = null; }
  if(callUnsubscribe){ callUnsubscribe(); callUnsubscribe = null; }
  if(callCandidateUnsub){ callCandidateUnsub(); callCandidateUnsub = null; }

  if(currentCallDoc){
    currentCallDoc.update({ status: "ended" }).catch(()=>{});
    currentCallDoc = null;
  }
  activeCallId = null;

  let rv = document.getElementById("remoteVideo");
  let lv = document.getElementById("localVideo");
  if(rv) rv.srcObject = null;
  if(lv) lv.srcObject = null;

  let banner = document.getElementById("incomingCallBanner");
  if(banner) banner.style.display = "none";

  show("chat");
};

/* ===== TOGGLE MUTE ===== */
window.toggleMute = function(){
  if(!localStream) return;
  let track = localStream.getAudioTracks()[0];
  if(!track) return;
  track.enabled = !track.enabled;
  let btn = document.getElementById("muteBtn");
  if(btn) btn.textContent = track.enabled ? "🎙️ Mute" : "🔇 Unmute";
};

/* ===== TOGGLE CAMERA ===== */
window.toggleCamera = function(){
  if(!localStream) return;
  let track = localStream.getVideoTracks()[0];
  if(!track) return;
  track.enabled = !track.enabled;
  let btn = document.getElementById("cameraBtn");
  if(btn) btn.textContent = track.enabled ? "📹 Cam Off" : "📷 Cam On";
};

/* ===== INCOMING CALL LISTENER ===== */
function listenForIncomingCalls(){
  if(!window.currentUser) return;

  // Listen for calls directed to me
  incomingCallUnsubscribe = db.collection("calls")
  .where("callTo","==", window.currentUser.uid)
  .where("status","==","ringing")
  .onSnapshot(snap=>{
    snap.docChanges().forEach(change=>{
      if(change.type === "added"){
        let data = change.doc.data();
        // Don't show if already in a call
        if(activeCallId) return;
        showIncomingCallBanner(change.doc.id, data.callerName||"Someone", data.callType||"video", data.callerPhoto||"");
      }
    });
  });

  // Also listen for group calls
  db.collection("calls")
  .where("callToGroup","array-contains", window.currentUser.uid)
  .where("status","==","ringing")
  .onSnapshot(snap=>{
    snap.docChanges().forEach(change=>{
      if(change.type === "added"){
        let data = change.doc.data();
        if(activeCallId) return;
        showIncomingCallBanner(change.doc.id, data.callerName||"Someone", data.callType||"video", data.callerPhoto||"");
      }
    });
  });
}

function showIncomingCallBanner(callId, callerName, callType, callerPhoto){
  let banner = document.getElementById("incomingCallBanner");
  if(!banner){
    banner = document.createElement("div");
    banner.id = "incomingCallBanner";
    banner.className = "incomingCallBanner";
    document.body.appendChild(banner);
  }

  banner.innerHTML = `
    <div class="incomingCallInfo">
      <div class="incomingCallAvatar">${callerPhoto?`<img src="${callerPhoto}">`:"👤"}</div>
      <div class="incomingCallText">
        <div class="incomingCallerName">${callerName}</div>
        <div class="incomingCallType">${callType==="video"?"📹 Video Call":"🎙️ Voice Call"}</div>
      </div>
    </div>
    <div class="incomingCallBtns">
      <button class="callAnswerBtn" onclick="answerIncomingCall('${callId}')">✅ Answer</button>
      <button class="callDeclineBtn" onclick="declineCall('${callId}')">❌ Decline</button>
    </div>
  `;
  banner.style.display = "flex";

  // Auto-dismiss after 30s if not answered
  setTimeout(()=>{
    if(banner.style.display !== "none") banner.style.display = "none";
  }, 30000);
}

window.declineCall = function(callId){
  db.collection("calls").doc(callId).update({ status: "declined" }).catch(()=>{});
  let banner = document.getElementById("incomingCallBanner");
  if(banner) banner.style.display = "none";
};

/* ===== SHORTCUTS ===== */
window.startVoiceCall = function(){ startCall("voice"); };
window.startVideoCall = function(){ startCall("video"); };

/* ===== INIT ===== */
let callsInitInterval = setInterval(()=>{
  if(window.currentUser){
    clearInterval(callsInitInterval);
    listenForIncomingCalls();
  }
}, 500);
