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
async function(){

let grid =
document.getElementById(
"groupMembersGrid"
);

grid.innerHTML = "";

if(
!window.currentGroup ||
!window.currentGroup.members
)
return;

for(const uid of
window.currentGroup.members){

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

let userDoc =
await db.collection("users")
.doc(uid)
.get();

let user =
userDoc.data() || {};

member.innerHTML = `

<div class="groupMemberPhoto">

${badge}

${
user.photo
?
`<img src="${user.photo}">`
:
"👤"
}

</div>

<div class="groupMemberName">

${
user.displayName
||
user.username
||
"Unknown User"
}

</div>

`;

grid.appendChild(member);

}

};
