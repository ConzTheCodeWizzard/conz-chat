window.openGroupInfo = function(group){
  // Always refresh group data from Firestore
  let col = (group.tag) ? "publicGroups" : "groups";
  let gId = group.id;

  db.collection(col).doc(gId).get().then(doc=>{
    let freshGroup = { id: doc.id, ...doc.data() };
    window.currentGroup = freshGroup;

    let photo = document.getElementById("groupInfoPhoto");
    if(freshGroup.photo){
      photo.innerHTML = `<img src="${freshGroup.photo}" style="width:100%;height:100%;object-fit:cover;border-radius:50%;">`;
    } else {
      photo.innerHTML = "👥";
    }

    document.getElementById("groupInfoName").innerText = freshGroup.displayName || freshGroup.name || "";
    document.getElementById("groupInfoTag").innerText = freshGroup.tag || `${(freshGroup.members||[]).length} members`;

    // Show/hide owner-only controls
    let isOwner = freshGroup.owner === window.currentUser.uid;
    let ownerControls = document.getElementById("ownerControls");
    if(ownerControls) ownerControls.style.display = isOwner ? "flex" : "none";

    renderGroupMembers();
    show("groupInfo");
  }).catch(err=>{
    showPopup("Error loading group info: " + err.message);
  });
};

window.renderGroupMembers = async function(){
  let grid = document.getElementById("groupMembersGrid");
  grid.innerHTML = "";

  if(!window.currentGroup || !window.currentGroup.members) return;

  let isOwner = window.currentGroup.owner === window.currentUser.uid;
  let isAdmin = (window.currentGroup.admins||[]).includes(window.currentUser.uid);

  for(const uid of window.currentGroup.members){
    let member = document.createElement("div");
    member.className = "groupMember";

    let memberIsOwner = uid === window.currentGroup.owner;
    let memberIsAdmin = (window.currentGroup.admins||[]).includes(uid);

    let userDoc = await db.collection("users").doc(uid).get();
    let user = userDoc.data() || {};

    let badge = "";
    if(memberIsOwner){
      badge = `<div class="ownerBadge"><span class="badgeCrown">👑</span> Owner</div>`;
    } else if(memberIsAdmin){
      badge = `<div class="adminBadge"><span class="badgeStar">★</span> Admin</div>`;
    }

    // Context menu for owner
    let contextMenu = "";
    if(isOwner && uid !== window.currentUser.uid){
      if(!memberIsAdmin && !memberIsOwner){
        contextMenu = `<button class="memberActionBtn" onclick="promoteToAdmin('${uid}')">Make Admin</button>`;
      } else if(memberIsAdmin){
        contextMenu = `<button class="memberActionBtn" onclick="removeAdmin('${uid}')">Remove Admin</button>`;
      }
    }

    member.innerHTML = `
      <div class="groupMemberPhoto">
        ${user.photo ? `<img src="${user.photo}">` : "👤"}
        ${badge}
      </div>
      <div class="groupMemberName">
        ${user.displayName || user.username || "Unknown User"}
      </div>
      ${contextMenu}
    `;

    grid.appendChild(member);
  }
};
