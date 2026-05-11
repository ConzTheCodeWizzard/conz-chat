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

alert("Fill everything in");

return;

}

if(!tag.startsWith("#")){

alert("Group name must start with #");

return;

}

let valid =
/^#[a-zA-Z0-9_-]+$/;

if(!valid.test(tag)){

alert(
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

alert(
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

window.publicGroups.push(group);

console.log(
"GROUP CREATED:",
group
);
renderPublicGroups();
alert(
"Public group created"
);

show("home");

};

window.renderPublicGroups = function(){

let chatList =
document.getElementById("chatList");

let old =
document.querySelectorAll(".publicGroupItem");

old.forEach(x => x.remove());

window.publicGroups.forEach(group => {

let div =
document.createElement("div");

div.className =
"publicGroupItem";

div.innerHTML = `

<div class="chatAvatar">

${
group.photo
?
`<img src="${group.photo}">`
:
"#"
}

</div>

<div>

<div>
${group.displayName}
</div>

<div style="
opacity:0.6;
font-size:12px;
">
${group.tag}
</div>

</div>

`;

chatList.prepend(div);

});

};
