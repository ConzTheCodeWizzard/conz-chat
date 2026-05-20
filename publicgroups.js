console.log("Public Groups Loaded");
window.openPublicGroupCreate = function(){

  show("publicGroupCreate");

};

window.pickGroupPhoto = function(){

  document
  .getElementById("groupPhotoInput")
  .click();

};

window.selectedGroupPhoto = "";

document.addEventListener("change", function(e){

  if(e.target.id !== "groupPhotoInput")
  return;

  let file = e.target.files[0];

  if(!file) return;

  let reader = new FileReader();

  reader.onload = function(){

    window.selectedGroupPhoto =
    reader.result;

    document.getElementById(
      "groupPhotoPreview"
    ).innerHTML = `
      <img src="${reader.result}">
    `;

  };

  reader.readAsDataURL(file);

});

window.publicGroups = [];

window.createPublicGroup = function(){

let tag =
document
.getElementById("publicGroupTag")
.value
.trim();

let display =
document
.getElementById("publicGroupDisplay")
.value
.trim();

if(!tag || !display){

showPopup("Fill everything in");

return;

}

if(!tag.startsWith("#")){

showPopup("Group name must start with #");

return;

}

let valid =
/^#[a-zA-Z0-9_-]+$/;

if(!valid.test(tag)){

showPopup(
"Only letters, numbers, - and _ allowed"
);

return;

}

let exists =
window.publicGroups.find(
g => g.tag.toLowerCase()
=== tag.toLowerCase()
);

if(exists){

showPopup(
"This group name is already taken"
);

return;

}

let group = {

tag:tag,

displayName:display,

photo:
window.selectedGroupPhoto || "",

owner:
window.currentUser?.uid || "unknown",

admins:[],

members:[
window.currentUser?.uid || "unknown"
],

banned:[]

};

db.collection("publicGroups")
.add(group)
.then(()=>{

console.log(
"GROUP CREATED:",
group
);

showPopup(
"Public group created"
);

show("home");

})
.catch(err=>{

console.log(err);

showPopup(
"Failed to create group"
);

});

};

window.renderPublicGroups =
function(){

db.collection("publicGroups")
.where(
"members",
"array-contains",
window.currentUser?.uid || ""
)
.onSnapshot(snap=>{

let oldGroups =
document.querySelectorAll(
".publicGroupItem"
);

oldGroups.forEach(
el=>el.remove()
);

let groups = [];

snap.forEach(doc=>{

groups.push({

id:doc.id,

...doc.data()

});

});

groups.forEach(group=>{

let div =
document.createElement(
"div"
);

div.className =
"publicGroupItem";

div.innerHTML = `

<div class="chatAvatar">

${
group.photo
? `<img src="${group.photo}">`
: `👥`
}

</div>

<div>

<div>
${group.displayName}
</div>

<div style="
font-size:12px;
opacity:0.6;
margin-top:2px;
">

${group.tag}

</div>

</div>
`;

div.onclick = ()=>{

window.currentGroup =
group;

openPublicGroup(
group
);

};

chatList.prepend(div);

});

},
err=>{

alert(err.message);

});
};


window.openPublicGroup = function(group){

  document.getElementById(
"chatName"
).onclick = function(){

openGroupInfo(group);

};
document.getElementById(
"chatName"
).innerText =
group.displayName;

  
document.getElementById(
"messages"
).innerHTML = "";

  window.currentGroup = group;
renderPublicGroupMessages();
show("chat");

};

window.sendPublicGroupMessage =
function(){

if(!msgInput.value.trim())
return;

if(!window.currentGroup)
return;

if(!window.currentGroup.messages){

window.currentGroup.messages = [];

}

window.currentGroup.messages.push({

text:msgInput.value,

sender:
window.currentUser.uid,

name:
window.currentUser.displayName
|| "Unknown",

time:Date.now()

});

renderPublicGroupMessages();

msgInput.value="";

};


window.renderPublicGroupMessages = function(){

let messages =
document.getElementById("messages");

messages.innerHTML = "";

if(
!window.currentGroup ||
!window.currentGroup.messages
) return;

window.currentGroup.messages.forEach(msg => {

let wrap =
document.createElement("div");

wrap.className =
msg.sender === window.currentUser.uid
?
"msgWrap me"
:
"msgWrap";

let bubble =
document.createElement("div");

bubble.className = "msg";

bubble.innerHTML = `

<div style="
font-size:12px;
opacity:0.7;
margin-bottom:4px;
">
${msg.name || "Unknown"}
</div>

${msg.text}

<div style="
font-size:11px;
opacity:0.5;
margin-top:4px;
">
${new Date(
msg.time
).toLocaleTimeString()}
</div>

`;

wrap.appendChild(bubble);

messages.appendChild(wrap);

});

messages.scrollTop =
messages.scrollHeight;

};

window.searchPublicGroups =
function(){

let query =
document.getElementById(
"publicGroupSearchInput"
)
.value
.trim()
.toLowerCase();

let results =
document.getElementById(
"publicGroupResults"
);

results.innerHTML = "";

db.collection("publicGroups")
.get()
.then(snap=>{

snap.forEach(doc=>{

let group =
doc.data();

group.id = doc.id;

let tag =
(group.tag || "")
.toLowerCase();

let display =
(group.displayName || "")
.toLowerCase();

if(
query &&
!tag.includes(query) &&
!display.includes(query)
){
return;
}

let div =
document.createElement("div");

div.className =
"publicGroupItem";

div.innerHTML = `

<div class="chatAvatar">
${
group.photo
? `<img src="${group.photo}">`
: "👥"
}
</div>

<div style="flex:1;">

<div>
${group.displayName}
</div>

<div style="
font-size:12px;
opacity:0.6;
">
${group.tag}
</div>

</div>
`;

div.onclick = function(){

window.currentGroup =
group;

openPublicGroup(group);

};

results.appendChild(div);

});

});

};
