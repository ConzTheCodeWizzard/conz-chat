const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();
const db = admin.firestore();
const messaging = admin.messaging();

/* ─── Helper: send FCM to a user ─── */
async function sendPush(toUid, title, body, data={}) {
  try {
    const userDoc = await db.collection("users").doc(toUid).get();
    const token = userDoc.data()?.fcmToken;
    if (!token) return;

    await messaging.send({
      token,
      notification: { title, body },
      data: { ...data, click_action: "FLUTTER_NOTIFICATION_CLICK" },
      android: {
        priority: "high",
        notification: {
          sound: "default",
          channelId: "conzchat_messages",
          icon: "ic_notification",
          color: "#ff0055"
        }
      },
      apns: {
        payload: { aps: { sound: "default", badge: 1 } }
      }
    });
  } catch (err) {
    console.error("Push error for", toUid, err.message);
  }
}

/* ─── 1. DM message notification ─── */
exports.onNewDM = functions.firestore
  .document("messages/{msgId}")
  .onCreate(async (snap) => {
    const m = snap.data();
    if (!m || !m.to || !m.from) return;

    // Don't notify if sender === recipient
    if (m.from === m.to) return;

    // Get sender name
    const senderDoc = await db.collection("users").doc(m.from).get();
    const sender = senderDoc.data() || {};
    const senderName = sender.displayName || sender.username || "Someone";

    let body = m.type === "image" ? "📷 Sent a photo"
             : m.type === "video" ? "🎥 Sent a video"
             : m.type === "voice" ? "🎙️ Sent a voice note"
             : m.text || "New message";

    await sendPush(m.to, senderName, body, { type: "dm", from: m.from });
  });

/* ─── 2. Private group message notification ─── */
exports.onNewGroupMessage = functions.firestore
  .document("groupMessages/{msgId}")
  .onCreate(async (snap) => {
    const m = snap.data();
    if (!m || !m.groupId || !m.from) return;

    const groupDoc = await db.collection("groups").doc(m.groupId).get();
    const group = groupDoc.data() || {};
    const members = group.members || [];

    const senderDoc = await db.collection("users").doc(m.from).get();
    const sender = senderDoc.data() || {};
    const senderName = sender.displayName || sender.username || "Someone";

    let body = m.type === "image" ? "📷 Sent a photo"
             : m.type === "voice" ? "🎙️ Sent a voice note"
             : m.text || "New message";

    const title = `${senderName} in ${group.name || "Group"}`;

    const pushPromises = members
      .filter(uid => uid !== m.from)
      .map(uid => sendPush(uid, title, body, { type: "group", groupId: m.groupId }));

    await Promise.all(pushPromises);
  });

/* ─── 3. Public group message notification ─── */
exports.onNewPublicGroupMessage = functions.firestore
  .document("publicGroupMessages/{msgId}")
  .onCreate(async (snap) => {
    const m = snap.data();
    if (!m || !m.groupId || !m.from) return;

    const groupDoc = await db.collection("publicGroups").doc(m.groupId).get();
    const group = groupDoc.data() || {};
    const members = group.members || [];

    const senderDoc = await db.collection("users").doc(m.from).get();
    const sender = senderDoc.data() || {};
    const senderName = sender.displayName || sender.username || "Someone";

    let body = m.text || "New message";
    const title = `${senderName} in #${group.tag || group.name || "Group"}`;

    const pushPromises = members
      .filter(uid => uid !== m.from)
      .map(uid => sendPush(uid, title, body, { type: "publicGroup", groupId: m.groupId }));

    await Promise.all(pushPromises);
  });

/* ─── 4. Friend request notification ─── */
exports.onFriendRequest = functions.firestore
  .document("friendRequests/{reqId}")
  .onCreate(async (snap) => {
    const r = snap.data();
    if (!r || !r.to || !r.from) return;
    const name = r.fromName || "Someone";
    await sendPush(r.to, "New Friend Request", `${name} wants to be your friend`, { type: "friendRequest" });
  });

/* ─── 5. Cleanup expired stories (runs every hour) ─── */
exports.cleanupStories = functions.pubsub
  .schedule("every 60 minutes")
  .onRun(async () => {
    const now = Date.now();
    const snap = await db.collection("stories").where("expires", "<", now).get();
    const batch = db.batch();
    snap.forEach(doc => batch.delete(doc.ref));
    await batch.commit();
    console.log(`Deleted ${snap.size} expired stories`);
  });
