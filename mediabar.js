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
function sendMediaMessage(type, url, isCamera, transcript, viewOnce){
  let msgData = {
    from: window.currentUser.uid,
    time: Date.now(),
    type: type,
    url: url,
    isCamera: isCamera || false,
    text: "",
    transcript: transcript || "",
    viewOnce: viewOnce || false,
    viewed: false
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
    let useCamera = !!(window.conzMods && window.conzMods.fakeCamera);
    let useViewOnce = !!window.viewOnceMode;
    if(file.type.startsWith("image/")){
      compressImageFile(file, 1000, 0.82, (dataUrl)=>{
        sendMediaMessage("image", dataUrl, useCamera, "", useViewOnce);
        if(useViewOnce){ window.viewOnceMode=false; window.toggleViewOnce && window.toggleViewOnce(); }
      });
    }
    e.target.value = "";
  }

  if(e.target.id === "videoInput"){
    let file = e.target.files[0];
    if(!file) return;
    if(file.size > 15 * 1024 * 1024){
      showPopup("Video too large. Please send clips under 15MB.");
      e.target.value = "";
      return;
    }
    let useCamera = !!(window.conzMods && window.conzMods.fakeCamera);
    let useViewOnce = !!window.viewOnceMode;
    let reader = new FileReader();
    reader.onload = ()=>{
      sendMediaMessage("video", reader.result, useCamera, "", useViewOnce);
      if(useViewOnce){ window.viewOnceMode=false; }
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

/* ===== CONZCHAT MODS ===== */
window.conzMods = {};
window.viewOnceMode = false;

window.saveMod = function(key, val){
  window.conzMods[key] = val;
  try{ localStorage.setItem('conz_mods', JSON.stringify(window.conzMods)); }catch(e){}
};

window.loadMods = function(){
  try{
    let saved = localStorage.getItem('conz_mods');
    if(saved) window.conzMods = JSON.parse(saved);
  }catch(e){}
  // Apply toggle states to checkboxes
  setTimeout(()=>{
    let r = document.getElementById('modDisableReceipts');
    let t = document.getElementById('modDisableTyping');
    let f = document.getElementById('modFakeCamera');
    let s = document.getElementById('modDisableScreenshots');
    if(r) r.checked = !!window.conzMods.disableReceipts;
    if(t) t.checked = !!window.conzMods.disableTyping;
    if(f) f.checked = !!window.conzMods.fakeCamera;
    if(s) s.checked = !!window.conzMods.disableScreenshots;
    // Re-apply screenshot protection on load
    applyScreenshotProtection(!!window.conzMods.disableScreenshots);
  }, 800);
};
window.loadMods();

/* ===== SCREENSHOT PROTECTION ===== */
// How it works: when enabled, we overlay the entire app with a canvas element
// that is styled with CSS -webkit-user-select:none and pointer-events:none.
// On visibilitychange (screen capture / screenshot attempt on Android Chrome),
// we replace the visible content with a blurred decoy canvas that has the
// "BETTER LOOK NEXT TIME 😈" watermark burned in — this is what ends up
// in the screenshot. The real content is hidden behind it.
let screenshotOverlay = null;
let screenshotOverlayActive = false;

window.applyScreenshotProtection = function(enabled){
  if(enabled){
    // Listen for visibility change (screenshot / screen record triggers this on Android)
    document.addEventListener('visibilitychange', handleScreenshotAttempt);
    // Also listen for blur (app loses focus = screenshot on many Android browsers)
    window.addEventListener('blur', handleScreenshotAttempt);
  } else {
    document.removeEventListener('visibilitychange', handleScreenshotAttempt);
    window.removeEventListener('blur', handleScreenshotAttempt);
    // Remove overlay if present
    if(screenshotOverlay){
      screenshotOverlay.remove();
      screenshotOverlay = null;
      screenshotOverlayActive = false;
    }
  }
};

function handleScreenshotAttempt(){
  if(!window.conzMods || !window.conzMods.disableScreenshots) return;
  if(screenshotOverlayActive) return;
  screenshotOverlayActive = true;

  // Build the decoy overlay canvas
  let canvas = document.createElement('canvas');
  canvas.width = window.screen.width * window.devicePixelRatio;
  canvas.height = window.screen.height * window.devicePixelRatio;
  canvas.style.cssText = [
    'position:fixed','top:0','left:0','width:100vw','height:100vh',
    'z-index:2147483647','pointer-events:none',
    '-webkit-user-select:none','user-select:none'
  ].join(';');

  let ctx = canvas.getContext('2d');
  let w = canvas.width, h = canvas.height;

  // Dark background
  ctx.fillStyle = '#0a0a0a';
  ctx.fillRect(0, 0, w, h);

  // Fake blurred chat bubbles — just coloured blobs to look like chat
  let bubbleData = [
    {x:0.08,y:0.12,bw:0.55,bh:0.045,col:'rgba(60,60,70,0.9)'},
    {x:0.38,y:0.21,bw:0.48,bh:0.045,col:'rgba(180,0,50,0.7)'},
    {x:0.08,y:0.30,bw:0.62,bh:0.045,col:'rgba(60,60,70,0.9)'},
    {x:0.08,y:0.37,bw:0.40,bh:0.045,col:'rgba(60,60,70,0.9)'},
    {x:0.45,y:0.46,bw:0.42,bh:0.045,col:'rgba(180,0,50,0.7)'},
    {x:0.08,y:0.55,bw:0.58,bh:0.045,col:'rgba(60,60,70,0.9)'},
    {x:0.30,y:0.63,bw:0.50,bh:0.045,col:'rgba(180,0,50,0.7)'},
    {x:0.08,y:0.72,bw:0.35,bh:0.045,col:'rgba(60,60,70,0.9)'},
  ];
  bubbleData.forEach(b=>{
    ctx.save();
    ctx.filter = 'blur(6px)';
    ctx.fillStyle = b.col;
    let rx=b.x*w, ry=b.y*h, rw=b.bw*w, rh=b.bh*h, rad=rh/2;
    ctx.beginPath();
    ctx.moveTo(rx+rad,ry);
    ctx.lineTo(rx+rw-rad,ry);
    ctx.quadraticCurveTo(rx+rw,ry,rx+rw,ry+rad);
    ctx.lineTo(rx+rw,ry+rh-rad);
    ctx.quadraticCurveTo(rx+rw,ry+rh,rx+rw-rad,ry+rh);
    ctx.lineTo(rx+rad,ry+rh);
    ctx.quadraticCurveTo(rx,ry+rh,rx,ry+rh-rad);
    ctx.lineTo(rx,ry+rad);
    ctx.quadraticCurveTo(rx,ry,rx+rad,ry);
    ctx.closePath();
    ctx.fill();
    ctx.restore();
  });

  // Heavy blur overlay to make bubbles unreadable
  ctx.save();
  ctx.filter = 'blur(18px)';
  ctx.fillStyle = 'rgba(10,10,10,0.55)';
  ctx.fillRect(0,0,w,h);
  ctx.restore();

  // Red gradient overlay
  let grad = ctx.createLinearGradient(0,0,w,h);
  grad.addColorStop(0,'rgba(180,0,30,0.18)');
  grad.addColorStop(1,'rgba(80,0,10,0.35)');
  ctx.fillStyle = grad;
  ctx.fillRect(0,0,w,h);

  // Main watermark text
  let fontSize = Math.round(w * 0.075);
  ctx.save();
  ctx.shadowColor = '#ff0033';
  ctx.shadowBlur = Math.round(w * 0.04);
  ctx.fillStyle = '#cc0022';
  ctx.font = `900 ${fontSize}px Arial Black, Arial, sans-serif`;
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  // Rotate slightly for drama
  ctx.translate(w/2, h/2);
  ctx.rotate(-0.08);
  ctx.fillText('BETTER LOOK', 0, -fontSize*0.7);
  ctx.fillText('NEXT TIME 😈', 0, fontSize*0.7);
  ctx.restore();

  // Subtle diagonal repeat watermark
  ctx.save();
  ctx.globalAlpha = 0.07;
  ctx.fillStyle = '#ff0033';
  ctx.font = `bold ${Math.round(w*0.035)}px Arial`;
  ctx.rotate(-0.4);
  for(let row=-3;row<8;row++){
    for(let col=-2;col<6;col++){
      ctx.fillText('ConzChat',col*(w*0.45)-w*0.2, row*(h*0.18));
    }
  }
  ctx.restore();

  document.body.appendChild(canvas);
  screenshotOverlay = canvas;

  // Remove overlay after 1.5s so the real app comes back
  setTimeout(()=>{
    if(screenshotOverlay){ screenshotOverlay.remove(); screenshotOverlay=null; }
    screenshotOverlayActive = false;
  }, 1500);
}

window.toggleViewOnce = function(){
  window.viewOnceMode = !window.viewOnceMode;
  let btn = document.getElementById('viewOnceBtn');
  if(btn){
    btn.style.background = window.viewOnceMode ? 'rgba(255,0,85,0.25)' : '';
    btn.style.borderColor = window.viewOnceMode ? '#ff0055' : '';
    btn.querySelector('.mediaBarLabel').textContent = window.viewOnceMode ? 'View Once ON' : 'View Once';
  }
};

/* ===== TOGGLE MEDIA BAR ===== */
window.toggleMediaBar = function(){
  let bar = document.getElementById("mediaBar");
  if(!bar) return;
  bar.style.display = bar.style.display === "flex" ? "none" : "flex";
};
