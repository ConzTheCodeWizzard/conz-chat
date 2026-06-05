# Deploy ConzChat Cloud Functions

## One-time setup

```bash
npm install -g firebase-tools
firebase login
firebase init functions   # select your existing project, choose Node 18
```

## Deploy

```bash
cd functions
npm install
firebase deploy --only functions
```

## What gets deployed

| Function | Trigger | What it does |
|---|---|---|
| `onNewDM` | New `messages` doc | Sends push to recipient of a DM |
| `onNewGroupMessage` | New `groupMessages` doc | Sends push to all group members |
| `onNewPublicGroupMessage` | New `publicGroupMessages` doc | Sends push to all public group members |
| `onFriendRequest` | New `friendRequests` doc | Notifies user of incoming friend request |
| `cleanupStories` | Every 60 minutes | Deletes expired stories from Firestore |

## Notes

- The client already saves each user's FCM token to `users/{uid}.fcmToken` on login
- Make sure your Firebase project has the **Blaze (pay-as-you-go)** plan enabled — Cloud Functions require it (free tier is very generous)
- After deploying, real push notifications will work even when the app is completely closed
