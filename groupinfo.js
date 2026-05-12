window.openGroupInfo = function(group){

window.currentGroup = group;

let photo =
document.getElementById(
"groupInfoPhoto"
);

if(group.photo){

photo.innerHTML = `
<img src="${group.photo}">
`;

}else{

photo.innerHTML = "#";

}

document.getElementById(
"groupInfoName"
).innerText =
group.displayName;

document.getElementById(
"groupInfoTag"
).innerText =
group.tag;

renderGroupMembers();

show("groupInfo");

};

window.renderGroupMembers =
function(){

let grid =
document.getElementById(
"groupMembersGrid"
);

grid.innerHTML = "";

if(!window.currentGroup)
return;

window.currentGroup.members
.forEach(uid => {

let member =
document.createElement("div");

member.className =
"groupMember";

let isOwner =
uid ===
window.currentGroup.owner;

let isAdmin =
window.currentGroup.admins
.includes(uid);

let badge = "";

if(isOwner){

badge = `
<div class="ownerBadge">
👑
</div>
`;

}else if(isAdmin){

badge = `
<div class="adminBadge">
★
</div>
`;

}

member.innerHTML = `

<div class="groupMemberPhoto">

${badge}

</div>

<div class="groupMemberName">

${uid}

</div>

`;

grid.appendChild(member);

});

};
