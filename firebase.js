// ===== Conz was here =====
// Conz AI — OpenAI API key for the built-in bot
// Replace with your actual OpenAI API key from platform.openai.com
window.OPENAI_KEY = 'YOUR_OPENAI_API_KEY_HERE';

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
let messaging = null;

// Register service worker and set up push notifications
if("serviceWorker" in navigator){
  navigator.serviceWorker.register("/firebase-messaging-sw.js").then(registration=>{
    try{
      messaging = firebase.messaging();
      messaging.useServiceWorker(registration);

      // Request notification permission
      Notification.requestPermission().then(permission=>{
        if(permission === "granted"){
          messaging.getToken({
            vapidKey: "BOZAAEfBvHwcKjFA59G4BDZyZLCI1vmp3WotfFuCkZEE6BF0pQfbwZ5e6ebiWvGWfsTutgN1Fef1r9i5Go9ATJk",
            serviceWorkerRegistration: registration
          }).then(token=>{
            if(token){
              console.log("FCM Token:", token);
              // Save token to Firestore when user is logged in
              auth.onAuthStateChanged(user=>{
                if(user){
                  db.collection("users").doc(user.uid).update({ fcmToken: token }).catch(()=>{});
                }
              });
            }
          }).catch(err=>{ console.warn("FCM token error:", err.message); });
        }
      }).catch(()=>{});

      // Foreground message handler — show in-app banner when app is open
      messaging.onMessage(payload=>{
        let title = (payload.notification && payload.notification.title) || "ConzChat";
        let body  = (payload.notification && payload.notification.body)  || "New message";
        let icon  = (payload.notification && payload.notification.icon)  || "/icon-192.png";

        // Show OS notification even in foreground (if permission granted)
        if(Notification.permission === "granted"){
          new Notification(title, { body, icon, badge: "/icon-192.png", tag: "conzchat-fg" });
        } else {
          // Fallback: in-app toast banner
          showInAppNotification(title, body, icon);
        }
      });

    }catch(err){ console.warn("Messaging setup error:", err.message); }
  }).catch(err=>{ console.warn("SW registration failed:", err.message); });
}

// In-app notification toast (fallback when OS notification not available)
function showInAppNotification(title, body, icon){
  let existing = document.getElementById("inAppNotifBanner");
  if(existing) existing.remove();

  let banner = document.createElement("div");
  banner.id = "inAppNotifBanner";
  banner.style.cssText = `
    position:fixed;top:0;left:0;right:0;z-index:999999;
    background:#1a1a1a;border-bottom:2px solid #ff0033;
    padding:12px 16px;display:flex;align-items:center;gap:12px;
    box-shadow:0 4px 20px rgba(255,0,51,0.4);
    animation:slideDown 0.3s ease;
    cursor:pointer;
  `;
  banner.innerHTML = `
    ${icon ? `<img src="${icon}" style="width:36px;height:36px;border-radius:50%;">` : ""}
    <div style="flex:1;">
      <div style="font-weight:bold;font-size:14px;color:#fff;">${title}</div>
      <div style="font-size:13px;opacity:0.75;color:#ccc;margin-top:2px;">${body}</div>
    </div>
    <div style="font-size:18px;opacity:0.5;padding:4px 8px;">✕</div>
  `;
  banner.onclick = ()=>{ banner.remove(); };
  document.body.appendChild(banner);
  setTimeout(()=>{ if(banner.parentNode) banner.remove(); }, 5000);
}
