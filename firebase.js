var firebaseConfig = {
  apiKey:"AIzaSyB7JVaVsJCRX1uWzxr8oR3_bDF18mnj8to",
  projectId:"conzchat"
};

firebase.initializeApp(firebaseConfig);

const db = firebase.firestore();
const auth = firebase.auth();
