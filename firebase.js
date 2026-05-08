// ===== Conz was here =====

const firebaseConfig = {
  apiKey: "AIzaSyB7JVaVsJCRX1uWzxr8oR3_bDF18mnj8to",
  authDomain: "conzchat.firebaseapp.com",
  projectId: "conzchat",
  storageBucket: "conzchat.firebasestorage.app",
  messagingSenderId: "431124465784",
  appId: "1:431124465784:web:055dbf6a8767b2f898458d"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);

// Services
const auth = firebase.auth();
const db = firebase.firestore();
const messaging = firebase.messaging();
Notification.requestPermission().then((permission) => {

  if (permission === "granted") {

    messaging.getToken({
      vapidKey: "BOZAAEfBvHwcKjFA59G4BDZyZLCI1vmp3WotfFuCkZEE6BF0pQfbwZ5e6ebiWvGWfsTutgN1Fef1r9i5Go9ATJk"
    }).then((token) => {

      console.log(token);

    });

  }

});
