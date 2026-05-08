importScripts('https://www.gstatic.com/firebasejs/8.10.1/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/8.10.1/firebase-messaging.js');

firebase.initializeApp({
  apiKey: "AIzaSyB7JVaVsJCRX1uWzxr8oR3_bDF18mnj8to",
  authDomain: "conzchat.firebaseapp.com",
  projectId: "conzchat",
  storageBucket: "conzchat.firebasestorage.app",
  messagingSenderId: "431124465784",
  appId: "1:431124465784:web:055dbf6a8767b2f898458d"
});

const messaging = firebase.messaging();
