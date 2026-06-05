/* =============================================
   ConzChat Calls — Voice & Video
   DMs and Groups via WebRTC + Firestore signaling
   ============================================= */

let localStream = null;
let peerConnection = null;
let currentCallDoc = null;
let currentCallType = "video"; // "video" or "voice"
let callUnsubscribe = null;
let callAnswerUnsubscribe = null;
let incomingCallUnsubscribe = null;

const ICE_SERVERS = {
  iceServers: [
    { urls: "stun:stun.l.google.com:19302" },
    { urls: "stun:stun1.l.google.com:19302" }
  ]
};

/* ===== START CALL (caller side) ===== */
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

    // Set call screen title
    let callTarget = window.currentChatUser
      ? document.getElementById("chatName").textContent.trim()
      : (typeof window.currentGroup === "object" ? window.currentGroup.displayName||window.currentGroup.name : "Group");

    document.getElementById("callScreenTitle").textContent =
      (currentCallType === "video" ? "📹 " : "🎙️ ") + callTarget;

    show("videoCall");

    // Create Firestore call doc
    let callDoc = db.collection("calls").doc();
    currentCallDoc = callDoc;

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
        callDoc.collection("offerCandidates").add(event.candidate.toJSON());
      }
    };

    peerConnection.onconnectionstatechange = () => {
      let state = peerConnection.connectionState;
      document.getElementById("callStatus").textContent =
        state === "connected" ? "Connected" :
        state === "disconnected" ? "Disconnected" :
        state === "failed" ? "Call Failed" : state;
    };

    const offerDesc = await peerConnection.createOffer();
    await peerConnection.setLocalDescription(offerDesc);

    // Determine who to call
    let callTo = window.currentChatUser || null;
    let callGroup = null;
    if(window.currentGroup){
      callGroup = typeof window.currentGroup === "object"
        ? window.currentGroup.id
        : window.currentGroup;
    }

    await callDoc.set({
      offer: { sdp: offerDesc.sdp, type: offerDesc.type },
      callerId: window.currentUser.uid,
      callerName: window.myData?.displayName || window.myData?.username || "Someone",
      callTo: callTo,
      callGroup: callGroup,
      callType: currentCallType,
      status: "ringing",
      created: Date.now()
    });

    document.getElementById("callIdDisplay").textContent = "Call ID: " + callDoc.id;
    document.getElementById("callStatus").textContent = "Ringing...";

    // Listen for answer
    callUnsubscribe = callDoc.onSnapshot(snapshot => {
      const data = snapshot.data();
      if(!data) return;
      if(!peerConnection.currentRemoteDescription && data.answer){
        const answerDesc = new RTCSessionDescription(data.answer);
        peerConnection.setRemoteDescription(answerDesc);
        document.getElementById("callStatus").textContent = "Connecting...";
      }
    });

    // Listen for answer ICE candidates
    callAnswerUnsubscribe = callDoc.collection("answerCandidates").onSnapshot(snapshot => {
      snapshot.docChanges().forEach(change => {
        if(change.type === "added"){
          peerConnection.addIceCandidate(new RTCIceCandidate(change.doc.data()));
        }
      });
    });

  }catch(err){
    showPopup("Could not start call: " + err.message);
    endCall();
  }
};

/* ===== ANSWER CALL (callee side) ===== */
window.answerIncomingCall = async function(callId){
  if(!callId){ showPopup("No call ID."); return; }

  try{
    localStream = await navigator.mediaDevices.getUserMedia({
      video: currentCallType === "video",
      audio: true
    });

    document.getElementById("localVideo").srcObject = localStream;
    document.getElementById("remoteVideo").srcObject = null;
    show("videoCall");

    let callDoc = db.collection("calls").doc(callId);
    currentCallDoc = callDoc;

    let callData = (await callDoc.get()).data();
    if(!callData){ showPopup("Call not found."); return; }

    currentCallType = callData.callType || "video";
    document.getElementById("callScreenTitle").textContent =
      (currentCallType === "video" ? "📹 " : "🎙️ ") + (callData.callerName || "Incoming Call");
    document.getElementById("callStatus").textContent = "Connecting...";

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
        callDoc.collection("answerCandidates").add(event.candidate.toJSON());
      }
    };

    peerConnection.onconnectionstatechange = () => {
      let state = peerConnection.connectionState;
      document.getElementById("callStatus").textContent =
        state === "connected" ? "Connected" :
        state === "disconnected" ? "Disconnected" :
        state === "failed" ? "Call Failed" : state;
    };

    await peerConnection.setRemoteDescription(new RTCSessionDescription(callData.offer));

    const answerDesc = await peerConnection.createAnswer();
    await peerConnection.setLocalDescription(answerDesc);

    await callDoc.update({
      answer: { type: answerDesc.type, sdp: answerDesc.sdp },
      status: "answered"
    });

    // Listen for offer ICE candidates
    callDoc.collection("offerCandidates").onSnapshot(snapshot => {
      snapshot.docChanges().forEach(change => {
        if(change.type === "added"){
          peerConnection.addIceCandidate(new RTCIceCandidate(change.doc.data()));
        }
      });
    });

    // Hide incoming call banner
    let banner = document.getElementById("incomingCallBanner");
    if(banner) banner.style.display = "none";

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
  if(peerConnection){
    peerConnection.close();
    peerConnection = null;
  }
  if(callUnsubscribe){ callUnsubscribe(); callUnsubscribe = null; }
  if(callAnswerUnsubscribe){ callAnswerUnsubscribe(); callAnswerUnsubscribe = null; }

  if(currentCallDoc){
    currentCallDoc.update({ status: "ended" }).catch(()=>{});
    currentCallDoc = null;
  }

  document.getElementById("localVideo").srcObject = null;
  document.getElementById("remoteVideo").srcObject = null;
  document.getElementById("callStatus").textContent = "";

  // Hide incoming banner
  let banner = document.getElementById("incomingCallBanner");
  if(banner) banner.style.display = "none";

  show("chat");
};

/* ===== TOGGLE MUTE ===== */
window.toggleMute = function(){
  if(!localStream) return;
  let audioTrack = localStream.getAudioTracks()[0];
  if(!audioTrack) return;
  audioTrack.enabled = !audioTrack.enabled;
  let btn = document.getElementById("muteBtn");
  if(btn) btn.textContent = audioTrack.enabled ? "🎙️ Mute" : "🔇 Unmute";
};

/* ===== TOGGLE CAMERA ===== */
window.toggleCamera = function(){
  if(!localStream) return;
  let videoTrack = localStream.getVideoTracks()[0];
  if(!videoTrack) return;
  videoTrack.enabled = !videoTrack.enabled;
  let btn = document.getElementById("cameraBtn");
  if(btn) btn.textContent = videoTrack.enabled ? "📹 Cam Off" : "📷 Cam On";
};

/* ===== INCOMING CALL LISTENER ===== */
function listenForIncomingCalls(){
  if(!window.currentUser) return;

  incomingCallUnsubscribe = db.collection("calls")
  .where("callTo","==", window.currentUser.uid)
  .where("status","==","ringing")
  .onSnapshot(snap=>{
    snap.docChanges().forEach(change=>{
      if(change.type === "added"){
        let data = change.doc.data();
        let callId = change.doc.id;
        showIncomingCallBanner(callId, data.callerName || "Someone", data.callType || "video");
      }
    });
  });
}

function showIncomingCallBanner(callId, callerName, callType){
  let banner = document.getElementById("incomingCallBanner");
  if(!banner){
    banner = document.createElement("div");
    banner.id = "incomingCallBanner";
    banner.className = "incomingCallBanner";
    document.body.appendChild(banner);
  }

  banner.innerHTML = `
    <div class="incomingCallInfo">
      <div class="incomingCallIcon">${callType === "video" ? "📹" : "🎙️"}</div>
      <div class="incomingCallText">
        <div class="incomingCallerName">${callerName}</div>
        <div class="incomingCallType">${callType === "video" ? "Video Call" : "Voice Call"}</div>
      </div>
    </div>
    <div class="incomingCallBtns">
      <button class="callAnswerBtn" onclick="answerIncomingCall('${callId}')">✅ Answer</button>
      <button class="callDeclineBtn" onclick="declineCall('${callId}')">❌ Decline</button>
    </div>
  `;
  banner.style.display = "flex";
}

window.declineCall = function(callId){
  db.collection("calls").doc(callId).update({ status: "declined" }).catch(()=>{});
  let banner = document.getElementById("incomingCallBanner");
  if(banner) banner.style.display = "none";
};

/* ===== MANUAL CALL JOIN (legacy support) ===== */
window.joinCallById = async function(){
  let callId = document.getElementById("callInput")?.value?.trim();
  if(!callId){ showPopup("Enter a Call ID first."); return; }
  currentCallType = "video";
  await answerIncomingCall(callId);
};

/* ===== START VOICE CALL SHORTCUT ===== */
window.startVoiceCall = function(){ startCall("voice"); };
window.startVideoCall = function(){ startCall("video"); };

/* ===== INIT — start listening once auth is ready ===== */
let callsInitInterval = setInterval(()=>{
  if(window.currentUser){
    clearInterval(callsInitInterval);
    listenForIncomingCalls();
  }
}, 500);
