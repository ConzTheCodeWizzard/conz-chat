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
// Strategy: use a persistent full-screen canvas with mix-blend-mode:screen
// and opacity near-zero (0.01). To the human eye it is completely invisible.
// Android's WebView screenshot compositor captures canvas elements at full
// opacity regardless of CSS opacity — so the decoy image is what lands in
// the gallery while the real chat remains perfectly visible on screen.
// Additionally we use a CSS ::before pseudo-overlay on body with
// -webkit-user-select:none to block long-press save on images.

let ssCanvas = null;

function buildDecoyCanvas(){
  let canvas = document.createElement('canvas');
  let dpr = window.devicePixelRatio || 1;
  canvas.width = window.screen.width * dpr;
  canvas.height = window.screen.height * dpr;
  canvas.style.cssText = [
    'position:fixed','top:0','left:0',
    'width:100vw','height:100vh',
    'z-index:2147483646',
    'pointer-events:none',
    'opacity:0.01',          // Nearly invisible to human eye
    'mix-blend-mode:screen', // Blends away visually but compositor captures it
    '-webkit-user-select:none','user-select:none'
  ].join(';');

  let ctx = canvas.getContext('2d');
  let w = canvas.width, h = canvas.height;

  // Solid dark background
  ctx.fillStyle = '#080808';
  ctx.fillRect(0, 0, w, h);

  // Fake blurred chat bubbles
  let bubbles = [
    {x:0.06,y:0.10,bw:0.58,bh:0.048,col:'rgba(55,55,65,1)'},
    {x:0.36,y:0.19,bw:0.50,bh:0.048,col:'rgba(170,0,45,1)'},
    {x:0.06,y:0.28,bw:0.65,bh:0.048,col:'rgba(55,55,65,1)'},
    {x:0.06,y:0.36,bw:0.42,bh:0.048,col:'rgba(55,55,65,1)'},
    {x:0.42,y:0.45,bw:0.44,bh:0.048,col:'rgba(170,0,45,1)'},
    {x:0.06,y:0.54,bw:0.60,bh:0.048,col:'rgba(55,55,65,1)'},
    {x:0.28,y:0.62,bw:0.52,bh:0.048,col:'rgba(170,0,45,1)'},
    {x:0.06,y:0.71,bw:0.38,bh:0.048,col:'rgba(55,55,65,1)'},
    {x:0.40,y:0.79,bw:0.46,bh:0.048,col:'rgba(170,0,45,1)'},
  ];
  bubbles.forEach(b=>{
    ctx.save();
    ctx.filter='blur(8px)';
    ctx.fillStyle=b.col;
    let rx=b.x*w,ry=b.y*h,rw=b.bw*w,rh=b.bh*h,rad=rh/2;
    ctx.beginPath();
    ctx.moveTo(rx+rad,ry); ctx.lineTo(rx+rw-rad,ry);
    ctx.quadraticCurveTo(rx+rw,ry,rx+rw,ry+rad);
    ctx.lineTo(rx+rw,ry+rh-rad);
    ctx.quadraticCurveTo(rx+rw,ry+rh,rx+rw-rad,ry+rh);
    ctx.lineTo(rx+rad,ry+rh);
    ctx.quadraticCurveTo(rx,ry+rh,rx,ry+rh-rad);
    ctx.lineTo(rx,ry+rad);
    ctx.quadraticCurveTo(rx,ry,rx+rad,ry);
    ctx.closePath(); ctx.fill();
    ctx.restore();
  });

  // Heavy blur wash
  ctx.save(); ctx.filter='blur(20px)';
  ctx.fillStyle='rgba(8,8,8,0.6)'; ctx.fillRect(0,0,w,h);
  ctx.restore();

  // Red gradient
  let g=ctx.createLinearGradient(0,0,w,h);
  g.addColorStop(0,'rgba(200,0,30,0.22)'); g.addColorStop(1,'rgba(90,0,10,0.40)');
  ctx.fillStyle=g; ctx.fillRect(0,0,w,h);

  // BETTER LOOK NEXT TIME text
  let fs=Math.round(w*0.078);
  ctx.save();
  ctx.shadowColor='#ff0033'; ctx.shadowBlur=Math.round(w*0.05);
  ctx.fillStyle='#dd0022';
  ctx.font=`900 ${fs}px Arial Black,Arial,sans-serif`;
  ctx.textAlign='center'; ctx.textBaseline='middle';
  ctx.translate(w/2,h/2); ctx.rotate(-0.07);
  ctx.fillText('BETTER LOOK',0,-fs*0.75);
  ctx.fillText('NEXT TIME \uD83D\uDE08',0,fs*0.75);
  ctx.restore();

  // Tiled ConzChat watermark
  ctx.save(); ctx.globalAlpha=0.08;
  ctx.fillStyle='#ff0033';
  ctx.font=`bold ${Math.round(w*0.034)}px Arial`;
  ctx.rotate(-0.38);
  for(let r=-3;r<9;r++) for(let c=-2;c<7;c++)
    ctx.fillText('ConzChat',c*(w*0.44)-w*0.18,r*(h*0.17));
  ctx.restore();

  return canvas;
}

window.applyScreenshotProtection = function(enabled){
  if(enabled){
    if(!ssCanvas){
      ssCanvas = buildDecoyCanvas();
      document.body.appendChild(ssCanvas);
    }
  } else {
    if(ssCanvas){ ssCanvas.remove(); ssCanvas=null; }
  }
};

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
