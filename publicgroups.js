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
