/* =============================================
   ConzChat Media Bar
   Camera, Gallery, Video, Voice Notes
   ============================================= */

let mediaRecorder = null;
let voiceChunks = [];
let isRecording = false;
let voiceTimer = null;
let voiceSeconds = 0;

/* ===== COMPRESS IMAGE TO BASE64 ===== */
function compressImageFile(file, maxSize, quality, callback){
  let img = new Image();
  let reader = new FileReader();
  reader.onload = ()=>{
    img.onload = ()=>{
      let canvas = document.createElement("canvas");
      let w = img.width, h = img.height;
      if(w > h){ if(w > maxSize){ h = h*(maxSize/w); w = maxSize; } }
      else { if(h > maxSize){ w = w*(maxSize/h); h = maxSize; } }
      canvas.width = w; canvas.height = h;
      canvas.getContext("2d").drawImage(img, 0, 0, w, h);
      callback(canvas.toDataURL("image/jpeg", quality));
    };
    img.src = reader.result;
  };
  reader.readAsDataURL(file);
}

/* ===== SEND MEDIA MESSAGE ===== */
function sendMediaMessage(type, url, isCamera, transcript){
  let msgData = {
    from: window.currentUser.uid,
    time: Date.now(),
    type: type,
    url: url,
    isCamera: isCamera || false,
    text: "",
    transcript: transcript || ""
  };

  if(window.currentGroup && typeof window.currentGroup === "object" && window.currentGroup.tag){
    // Public group
    db.collection("publicGroupMessages").add({
      ...msgData,
      groupId: window.currentGroup.id
    }).then(()=>{
      db.collection("publicGroups").doc(window.currentGroup.id).update({
        lastMessage: type === "image" ? "📷 Photo" : type === "video" ? "🎥 Video" : "🎙️ Voice Note",
        lastTime: Date.now()
      });
    }).catch(err=>showPopup("Send failed: " + err.message));
  } else if(window.currentGroup){
    // Private group
    db.collection("groupMessages").add({
      ...msgData,
      groupId: typeof window.currentGroup === "string" ? window.currentGroup : window.currentGroup.id
    }).catch(err=>showPopup("Send failed: " + err.message));
  } else if(window.currentChatUser){
    // DM
    db.collection("messages").add({
      ...msgData,
      to: window.currentChatUser,
      receipt: "S"
    }).catch(err=>showPopup("Send failed: " + err.message));
  }
}

/* ===== OPEN CAMERA (live photo) ===== */
window.openCamera = function(){
  let inp = document.getElementById("cameraInput");
  if(inp) inp.click();
};

/* ===== OPEN GALLERY (image) ===== */
window.openGallery = function(){
  let inp = document.getElementById("galleryInput");
  if(inp) inp.click();
};

/* ===== OPEN VIDEO PICKER ===== */
window.openVideoPicker = function(){
  let inp = document.getElementById("videoInput");
  if(inp) inp.click();
};

/* ===== HANDLE CAMERA INPUT ===== */
document.addEventListener("change", function(e){
  if(e.target.id === "cameraInput"){
    let file = e.target.files[0];
    if(!file) return;
    compressImageFile(file, 800, 0.8, (dataUrl)=>{
      sendMediaMessage("image", dataUrl, true);
    });
    e.target.value = "";
  }

  if(e.target.id === "galleryInput"){
    let file = e.target.files[0];
    if(!file) return;
    if(file.type.startsWith("image/")){
      compressImageFile(file, 1000, 0.82, (dataUrl)=>{
        sendMediaMessage("image", dataUrl, false);
      });
    }
    e.target.value = "";
  }

  if(e.target.id === "videoInput"){
    let file = e.target.files[0];
    if(!file) return;
    // Videos stored as base64 (small clips only — warn if large)
    if(file.size > 15 * 1024 * 1024){
      showPopup("Video too large. Please send clips under 15MB.");
      e.target.value = "";
      return;
    }
    let reader = new FileReader();
    reader.onload = ()=>{
      sendMediaMessage("video", reader.result, false);
    };
    reader.readAsDataURL(file);
    e.target.value = "";
  }
});

/* ===== VOICE NOTE RECORDING ===== */
window.startVoiceNote = async function(){
  if(isRecording){ stopVoiceNote(); return; }

  try{
    let stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    voiceChunks = [];
    mediaRecorder = new MediaRecorder(stream);

    mediaRecorder.ondataavailable = e => {
      if(e.data.size > 0) voiceChunks.push(e.data);
    };

    // Speech recognition for transcript
    let transcript = "";
    let recognition = null;
    if(window.SpeechRecognition || window.webkitSpeechRecognition){
      let SR = window.SpeechRecognition || window.webkitSpeechRecognition;
      recognition = new SR();
      recognition.continuous = true;
      recognition.interimResults = false;
      recognition.lang = "en-US";
      recognition.onresult = (e)=>{
        for(let i=e.resultIndex; i<e.results.length; i++){
          if(e.results[i].isFinal) transcript += e.results[i][0].transcript + " ";
        }
      };
      recognition.onerror = ()=>{};
      try{ recognition.start(); }catch(e){}
    }

    mediaRecorder.onstop = ()=>{
      stream.getTracks().forEach(t => t.stop());
      if(recognition){ try{ recognition.stop(); }catch(e){} }
      let blob = new Blob(voiceChunks, { type: "audio/webm" });
      let reader = new FileReader();
      reader.onload = ()=>{
        // Small delay to let recognition finish
        setTimeout(()=>{
          sendMediaMessage("voice", reader.result, false, transcript.trim());
        }, 600);
      };
      reader.readAsDataURL(blob);
      voiceChunks = [];
    };

    mediaRecorder.start();
    isRecording = true;
    voiceSeconds = 0;

    // Update button to show recording state
    let btn = document.getElementById("voiceNoteBtn");
    if(btn){
      btn.classList.add("recording");
      btn.title = "Tap to stop";
    }

    // Timer
    voiceTimer = setInterval(()=>{
      voiceSeconds++;
      let btn = document.getElementById("voiceNoteBtn");
      if(btn) btn.setAttribute("data-time", formatVoiceTime(voiceSeconds));
      // Auto-stop at 2 minutes
      if(voiceSeconds >= 120) stopVoiceNote();
    }, 1000);

  }catch(err){
    showPopup("Microphone access denied: " + err.message);
  }
};

window.stopVoiceNote = function(){
  if(!isRecording || !mediaRecorder) return;
  mediaRecorder.stop();
  isRecording = false;
  clearInterval(voiceTimer);
  voiceSeconds = 0;

  let btn = document.getElementById("voiceNoteBtn");
  if(btn){
    btn.classList.remove("recording");
    btn.removeAttribute("data-time");
    btn.title = "Voice Note";
  }
};

function formatVoiceTime(s){
  let m = Math.floor(s/60);
  let sec = s % 60;
  return `${m}:${sec.toString().padStart(2,"0")}`;
}

/* ===== TOGGLE MEDIA BAR ===== */
window.toggleMediaBar = function(){
  let bar = document.getElementById("mediaBar");
  if(!bar) return;
  bar.style.display = bar.style.display === "flex" ? "none" : "flex";
};
