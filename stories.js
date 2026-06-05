/* ===== CONZCHAT STORIES / STATUS ===== */

window.storiesData = [];
window.storyViewIndex = 0;
window.storyUserList = [];

/* ---- Compress story image ---- */
function compressStoryImage(file, callback){
  let canvas=document.createElement("canvas");
  let ctx=canvas.getContext("2d");
  let img=new Image();
  let url=URL.createObjectURL(file);
  img.onload=function(){
    let maxW=720, maxH=1280;
    let w=img.width, h=img.height;
    let scale=Math.min(maxW/w, maxH/h, 1);
    canvas.width=Math.round(w*scale);
    canvas.height=Math.round(h*scale);
    ctx.drawImage(img,0,0,canvas.width,canvas.height);
    URL.revokeObjectURL(url);
    callback(canvas.toDataURL("image/jpeg",0.7));
  };
  img.src=url;
}

/* ---- Post a text story ---- */
window.postTextStory=function(){
  let popup=document.getElementById('textStoryPopup');
  if(!popup) return;
  let inp=document.getElementById('textStoryInput');
  if(inp) inp.value='';
  popup.style.display='flex';
  if(inp) setTimeout(()=>inp.focus(),100);
};

window.submitTextStory=function(){
  let inp=document.getElementById('textStoryInput');
  let text=inp?inp.value.trim():'';
  if(!text){ showPopup("Please write something first"); return; }
  if(text.length>120){ showPopup("Max 120 characters"); return; }
  document.getElementById('textStoryPopup').style.display='none';
  let uid=window.currentUser.uid;
  let name=window.myData.displayName||window.myData.username;
  db.collection("stories").add({
    uid, name,
    photo: window.myData.photo||"",
    type:"text",
    text: text,
    time: Date.now(),
    expires: Date.now()+(24*60*60*1000),
    seenBy:[]
  }).then(()=>{ showPopup("Story posted!"); loadStories(); });
};

/* ---- Post a photo story ---- */
window.postPhotoStory=function(){
  let inp=document.getElementById("storyPhotoPicker");
  if(!inp){
    inp=document.createElement("input");
    inp.type="file"; inp.accept="image/*"; inp.id="storyPhotoPicker";
    inp.style.display="none";
    document.body.appendChild(inp);
    inp.addEventListener("change",function(){
      let f=inp.files[0];
      if(!f) return;
      compressStoryImage(f,function(dataUrl){
        let uid=window.currentUser.uid;
        let name=window.myData.displayName||window.myData.username;
        db.collection("stories").add({
          uid, name,
          photo: window.myData.photo||"",
          type:"image",
          imageUrl: dataUrl,
          time: Date.now(),
          expires: Date.now()+(24*60*60*1000),
          seenBy:[]
        }).then(()=>{ showPopup("Story posted!"); loadStories(); });
      });
    });
  }
  inp.click();
};

/* ---- Load stories rail ---- */
window.loadStories=function(){
  let rail=document.getElementById("storiesRail");
  if(!rail) return;
  let now=Date.now();
  db.collection("stories")
    .where("expires",">",now)
    .get()
    .then(snap=>{
      // Group by uid
      let byUser={};
      snap.forEach(doc=>{
        let s={id:doc.id,...doc.data()};
        if(!byUser[s.uid]) byUser[s.uid]={ uid:s.uid, name:s.name, photo:s.photo, stories:[] };
        byUser[s.uid].stories.push(s);
      });

      window.storyUserList=Object.values(byUser);
      // Sort: own story first, then others
      window.storyUserList.sort((a,b)=>{
        if(a.uid===window.currentUser.uid) return -1;
        if(b.uid===window.currentUser.uid) return 1;
        return 0;
      });

      rail.innerHTML="";

      // Add story button (always first)
      let addBtn=document.createElement("div");
      addBtn.className="storyItem storyAddBtn";
      addBtn.innerHTML=`<div class="storyRing storyAddRing"><div class="storyAvatar">${window.myData.photo?`<img src="${window.myData.photo}">`:"👤"}</div><div class="storyAddPlus">+</div></div><div class="storyName">My Story</div>`;
      addBtn.onclick=()=>openStoryPostMenu();
      rail.appendChild(addBtn);

      window.storyUserList.forEach((u,i)=>{
        let myUid=window.currentUser.uid;
        let seen=u.stories.every(s=>(s.seenBy||[]).includes(myUid));
        let item=document.createElement("div");
        item.className="storyItem";
        item.innerHTML=`<div class="storyRing ${seen?"storyRingSeen":"storyRingUnseen"}"><div class="storyAvatar">${u.photo?`<img src="${u.photo}">`:"👤"}</div></div><div class="storyName">${u.name.split(" ")[0]}</div>`;
        item.onclick=()=>openStoryViewer(i);
        rail.appendChild(item);
      });
    });
};

/* ---- Story post menu ---- */
window.openStoryPostMenu=function(){
  let old=document.getElementById("storyPostMenu");
  if(old){ old.remove(); return; }
  let menu=document.createElement("div");
  menu.id="storyPostMenu";
  menu.className="storyPostMenu";
  menu.innerHTML=`
    <button class="storyPostBtn" onclick="postPhotoStory();document.getElementById('storyPostMenu').remove()">📷 Photo Story</button>
    <button class="storyPostBtn" onclick="postTextStory();document.getElementById('storyPostMenu').remove()">✏️ Text Story</button>
    <button class="storyPostBtn storyPostCancel" onclick="document.getElementById('storyPostMenu').remove()">Cancel</button>
  `;
  let rail=document.getElementById("storiesRail");
  if(rail) rail.parentNode.insertBefore(menu, rail.nextSibling);
  else document.body.appendChild(menu);
};

/* ---- Story viewer ---- */
window.openStoryViewer=function(userIndex){
  window.storyViewIndex=userIndex;
  window.storyItemIndex=0;
  renderStoryViewer();
};

function renderStoryViewer(){
  let old=document.getElementById("storyViewer");
  if(old) old.remove();

  let users=window.storyUserList;
  if(!users||!users[window.storyViewIndex]) return;
  let u=users[window.storyViewIndex];
  let stories=u.stories;
  if(!stories||!stories.length) return;
  let si=window.storyItemIndex;
  if(si>=stories.length){ closeStoryViewer(); return; }
  let s=stories[si];

  // Mark as seen
  let myUid=window.currentUser.uid;
  if(!(s.seenBy||[]).includes(myUid)){
    db.collection("stories").doc(s.id).update({ seenBy: firebase.firestore.FieldValue.arrayUnion(myUid) });
  }

  let viewer=document.createElement("div");
  viewer.id="storyViewer";
  viewer.className="storyViewer";

  // Progress bars
  let bars=stories.map((_,i)=>`<div class="storyProgressBar"><div class="storyProgressFill" id="storyBar${i}" style="width:${i<si?'100%':i===si?'0%':'0%'}"></div></div>`).join("");

  let content="";
  if(s.type==="image"){
    content=`<img src="${s.imageUrl}" class="storyImage">`;
  } else {
    content=`<div class="storyTextContent">${s.text}</div>`;
  }

  viewer.innerHTML=`
    <div class="storyHeader">
      <div class="storyProgressBars">${bars}</div>
      <div class="storyUserInfo">
        <div class="storyUserAvatar">${u.photo?`<img src="${u.photo}">`:"👤"}</div>
        <div class="storyUserName">${u.name}</div>
        <div class="storyTime">${formatKikTime(s.time)}</div>
      </div>
      <button class="storyCloseBtn" onclick="closeStoryViewer()">✕</button>
    </div>
    <div class="storyContent">${content}</div>
    <div class="storyTapLeft" onclick="prevStory()"></div>
    <div class="storyTapRight" onclick="nextStory()"></div>
  `;

  document.body.appendChild(viewer);

  // Animate current bar
  let dur=s.type==="image"?5000:4000;
  let bar=document.getElementById("storyBar"+si);
  if(bar){
    bar.style.transition=`width ${dur}ms linear`;
    requestAnimationFrame(()=>{ bar.style.width="100%"; });
  }

  // Auto-advance
  clearTimeout(window._storyTimer);
  window._storyTimer=setTimeout(()=>nextStory(), dur);
};

window.nextStory=function(){
  clearTimeout(window._storyTimer);
  let u=window.storyUserList[window.storyViewIndex];
  if(window.storyItemIndex < u.stories.length-1){
    window.storyItemIndex++;
    renderStoryViewer();
  } else if(window.storyViewIndex < window.storyUserList.length-1){
    window.storyViewIndex++;
    window.storyItemIndex=0;
    renderStoryViewer();
  } else {
    closeStoryViewer();
  }
};

window.prevStory=function(){
  clearTimeout(window._storyTimer);
  if(window.storyItemIndex>0){
    window.storyItemIndex--;
    renderStoryViewer();
  } else if(window.storyViewIndex>0){
    window.storyViewIndex--;
    window.storyItemIndex=0;
    renderStoryViewer();
  }
};

window.closeStoryViewer=function(){
  clearTimeout(window._storyTimer);
  let v=document.getElementById("storyViewer");
  if(v) v.remove();
  loadStories(); // Refresh seen state
};
