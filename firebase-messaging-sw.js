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

// Background message handler — shows real OS notification when app is not in foreground
messaging.onBackgroundMessage(function(payload){
  console.log("[SW] Background message received:", payload);

  let title = (payload.notification && payload.notification.title) || "ConzChat";
  let body  = (payload.notification && payload.notification.body)  || "You have a new message";
  let icon  = (payload.notification && payload.notification.icon)  || "/icon-192.png";
  let data  = payload.data || {};

  let options = {
    body: body,
    icon: icon,
    badge: "/icon-192.png",
    tag: data.chatId || "conzchat-msg",
    renotify: true,
    vibrate: [200, 100, 200],
    data: {
      url: data.url || "/"
    }
  };

  return self.registration.showNotification(title, options);
});

// When user taps the notification, open the app
self.addEventListener("notificationclick", function(event){
  event.notification.close();
  let url = (event.notification.data && event.notification.data.url) || "/";
  event.waitUntil(
    clients.matchAll({ type: "window", includeUncontrolled: true }).then(function(clientList){
      for(let client of clientList){
        if(client.url.includes(self.location.origin) && "focus" in client){
          return client.focus();
        }
      }
      if(clients.openWindow) return clients.openWindow(url);
    })
  );
});
