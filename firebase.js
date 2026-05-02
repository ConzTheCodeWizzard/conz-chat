// IMPORT FIREBASE (CDN)
const firebaseConfig = {
  apiKey: "AIzaSyB7JVaVsJCRX1uWzxr8oR3_bDF18mnj8to",
  authDomain: "conzchat.firebaseapp.com",
  projectId: "conzchat",
  storageBucket: "conzchat.firebasestorage.app",
  messagingSenderId: "431124465784",
  appId: "1:431124465784:web:055dbf6a8767b2f898458d"
};

// LOAD FIREBASE LIBRARIES
const script1 = document.createElement("script");
script1.src = "https://www.gstatic.com/firebasejs/9.23.0/firebase-app-compat.js";
document.head.appendChild(script1);

const script2 = document.createElement("script");
script2.src = "https://www.gstatic.com/firebasejs/9.23.0/firebase-auth-compat.js";
document.head.appendChild(script2);

const script3 = document.createElement("script");
script3.src = "https://www.gstatic.com/firebasejs/9.23.0/firebase-firestore-compat.js";
document.head.appendChild(script3);

// WAIT FOR LOAD THEN INIT
setTimeout(() => {
  firebase.initializeApp(firebaseConfig);
}, 1000);
