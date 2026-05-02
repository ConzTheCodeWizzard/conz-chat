/const firebaseConfig = {
  apiKey: "AIzaSyB7JVaVsJCRX1uWzxr8oR3_bDF18mnj8to",
  authDomain: "conzchat.firebaseapp.com",
  projectId: "conzchat",
  storageBucket: "conzchat.firebasestorage.app",
  messagingSenderId: "431124465784",
  appId: "1:431124465784:web:055dbf6a8767b2f898458d"
};

// INIT IMMEDIATELY
firebase.initializeApp(firebaseConfig);

// GLOBAL ACCESS
const auth = firebase.auth();
const db = firebase.firestore();
