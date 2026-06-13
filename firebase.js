// ============================================================
//  ConzChat-Kik Bridge  —  firebase.js  (REPLACEMENT)
//  Replaces all Firebase calls with calls to the Kik Gateway.
//  Drop this file in place of the original firebase.js.
// ============================================================

// ── Configuration ────────────────────────────────────────────
// Change this to wherever your gateway server is running.
// For local testing: "http://localhost:5000"
// For production: "https://your-server.com"
window.GATEWAY_URL = "http://localhost:5000";

// ── Socket.IO connection ──────────────────────────────────────
// We load socket.io-client from CDN (added in index.html)
let _socket = null;
let _socketReady = false;
let _socketQueue = [];

function getSocket() {
  if (!_socket) {
    _socket = io(window.GATEWAY_URL, { transports: ["websocket", "polling"] });
    _socket.on("connect", () => {
      _socketReady = true;
      console.log("[KikBridge] Socket connected:", _socket.id);
      _socketQueue.forEach(fn => fn(_socket));
      _socketQueue = [];
    });
    _socket.on("disconnect", () => {
      _socketReady = false;
      console.warn("[KikBridge] Socket disconnected");
    });
  }
  return _socket;
}

function withSocket(fn) {
  const s = getSocket();
  if (_socketReady) fn(s);
  else _socketQueue.push(fn);
}

// Initialise socket immediately so it's ready when needed
getSocket();

// ── Session token ─────────────────────────────────────────────
// Stored in localStorage so it survives page refreshes
window._kikToken = localStorage.getItem("kik_token") || null;

// ── API helper ────────────────────────────────────────────────
async function _apiPost(path, body) {
  const res = await fetch(window.GATEWAY_URL + path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  return res.json();
}

async function _apiGet(path, params = {}) {
  const qs = new URLSearchParams(params).toString();
  const res = await fetch(`${window.GATEWAY_URL}${path}?${qs}`);
  return res.json();
}

// ── Fake Firebase auth object ─────────────────────────────────
// The rest of app.js calls auth.onAuthStateChanged, auth.signOut, etc.
// We emulate that interface here.

let _authStateListeners = [];
let _currentKikUser = null;  // { uid (=jid), username, displayName, email, ... }

function _notifyAuthListeners(user) {
  _authStateListeners.forEach(fn => fn(user));
}

const auth = {
  currentUser: null,

  onAuthStateChanged(callback) {
    _authStateListeners.push(callback);
    // If already logged in, fire immediately
    if (_currentKikUser) callback(_currentKikUser);
    return () => {
      _authStateListeners = _authStateListeners.filter(fn => fn !== callback);
    };
  },

  async signInWithEmailAndPassword(usernameOrEmail, password) {
    // Kik uses username, not email. We accept either.
    const username = usernameOrEmail.includes("@")
      ? usernameOrEmail.split("@")[0]  // strip domain if user typed email
      : usernameOrEmail;

    return new Promise((resolve, reject) => {
      withSocket(async (socket) => {
        const { token } = await _apiPost("/api/login", {
          username,
          password,
          sid: socket.id,
        });
        window._kikToken = token;
        localStorage.setItem("kik_token", token);

        // Wait for auth_success or auth_error
        const onSuccess = (data) => {
          socket.off("auth_error", onError);
          socket.off("auth_captcha", onCaptcha);
          _currentKikUser = {
            uid: data.username,  // use username as UID
            username: data.username,
            displayName: data.username,
            email: "",
          };
          auth.currentUser = _currentKikUser;
          _notifyAuthListeners(_currentKikUser);
          resolve(_currentKikUser);
        };
        const onError = (data) => {
          socket.off("auth_success", onSuccess);
          socket.off("auth_captcha", onCaptcha);
          reject(new Error(data.message));
        };
        const onCaptcha = (data) => {
          socket.off("auth_success", onSuccess);
          socket.off("auth_error", onError);
          // Show captcha UI
          window._pendingCaptcha = data;
          showCaptchaPrompt(data.captcha_url, data.stc_id);
          reject(new Error("captcha_required"));
        };

        socket.once("auth_success", onSuccess);
        socket.once("auth_error", onError);
        socket.once("auth_captcha", onCaptcha);
      });
    });
  },

  async createUserWithEmailAndPassword(email, password) {
    // Kik signup — called from signup() in app.js
    // app.js sets signupUsername separately, so we read it from the form
    const username = (document.getElementById("signupUsername") || {}).value || "";
    const firstName = username;
    const lastName = "User";
    const birthday = "2000-01-01"; // default; ideally add a birthday field to signup form

    return new Promise((resolve, reject) => {
      withSocket(async (socket) => {
        const { token } = await _apiPost("/api/signup", {
          username,
          email,
          password,
          first_name: firstName,
          last_name: lastName,
          birthday,
          sid: socket.id,
        });
        window._kikToken = token;
        localStorage.setItem("kik_token", token);

        socket.once("signup_success", (data) => {
          socket.off("signup_error");
          _currentKikUser = {
            uid: data.username,
            username: data.username,
            displayName: data.username,
            email,
          };
          auth.currentUser = _currentKikUser;
          _notifyAuthListeners(_currentKikUser);
          // Emulate Firebase result shape
          resolve({ user: _currentKikUser });
        });
        socket.once("signup_error", (data) => {
          socket.off("signup_success");
          reject(new Error(data.message));
        });
      });
    });
  },

  async signOut() {
    if (window._kikToken) {
      await _apiPost("/api/logout", { token: window._kikToken });
      window._kikToken = null;
      localStorage.removeItem("kik_token");
    }
    _currentKikUser = null;
    auth.currentUser = null;
    _notifyAuthListeners(null);
  },

  // Stub for EmailAuthProvider (used in settings/change-password)
  EmailAuthProvider: {
    credential(email, password) {
      return { email, password };
    },
  },
};

// ── Captcha prompt helper ─────────────────────────────────────
function showCaptchaPrompt(url, stcId) {
  const overlay = document.createElement("div");
  overlay.id = "captchaOverlay";
  overlay.style.cssText = `
    position:fixed;inset:0;background:rgba(0,0,0,0.85);z-index:99999;
    display:flex;flex-direction:column;align-items:center;justify-content:center;
    color:#fff;font-family:sans-serif;padding:20px;text-align:center;
  `;
  overlay.innerHTML = `
    <h2 style="color:#ff0033;margin-bottom:12px;">Kik Captcha Required</h2>
    <p style="max-width:400px;opacity:0.85;margin-bottom:16px;">
      Kik detected a new device login. Open the link below in a browser,
      solve the captcha, then copy the <strong>response</strong> value from the
      network tab and paste it below.
    </p>
    <a href="${url}" target="_blank" style="color:#ff0033;word-break:break-all;margin-bottom:16px;">${url}</a>
    <input id="captchaInput" placeholder="Paste captcha response here..."
      style="width:100%;max-width:400px;padding:10px;border-radius:6px;border:1px solid #ff0033;
             background:#111;color:#fff;margin-bottom:12px;font-size:14px;">
    <button onclick="submitCaptcha('${stcId}')"
      style="background:#ff0033;color:#fff;border:none;padding:10px 24px;border-radius:6px;
             font-size:15px;cursor:pointer;">Submit Captcha</button>
  `;
  document.body.appendChild(overlay);
}

window.submitCaptcha = async function(stcId) {
  const input = document.getElementById("captchaInput");
  const result = input ? input.value.trim() : "";
  if (!result) { alert("Please paste the captcha response first."); return; }
  await _apiPost("/api/captcha", {
    token: window._kikToken,
    result,
    stc_id: stcId,
  });
  const overlay = document.getElementById("captchaOverlay");
  if (overlay) overlay.remove();
};

// ── Fake Firestore db object ──────────────────────────────────
// We emulate the Firestore chained API:
//   db.collection("users").doc(uid).get()
//   db.collection("messages").add({...})
//   db.collection("messages").where(...).onSnapshot(...)
// Each call is translated to a gateway API call or a Socket.IO subscription.

// In-memory stores (populated from Kik data via socket events)
const _store = {
  users: {},        // jid -> user doc
  messages: {},     // id -> message doc
  groups: {},       // jid -> group doc
  publicGroups: {}, // jid -> group doc
  roster: [],
};

// Populate store from socket events
withSocket((socket) => {
  socket.on("profile_update", (data) => {
    if (data.jid) {
      _store.users[data.jid] = {
        uid: data.username,
        username: data.username,
        displayName: data.display_name || data.username,
        photo: data.pic_url || "",
        email: data.email || "",
        banned: false,
        blockedUsers: [],
        premium: false,
      };
    }
  });

  socket.on("roster_update", (data) => {
    _store.roster = data.roster || [];
    data.roster.forEach((u) => {
      _store.users[u.jid] = {
        uid: u.username || u.jid,
        username: u.username || u.jid,
        displayName: u.display_name || u.username || u.jid,
        photo: u.pic_url || "",
        banned: false,
        blockedUsers: [],
        premium: false,
      };
    });
    // Notify any active onSnapshot listeners for "users"
    _notifySnapshots("users");
  });

  socket.on("peer_info", (data) => {
    (data.users || []).forEach((u) => {
      _store.users[u.jid] = {
        uid: u.username || u.jid,
        username: u.username || u.jid,
        displayName: u.display_name || u.username || u.jid,
        photo: u.pic_url || "",
        banned: false,
        blockedUsers: [],
        premium: false,
      };
    });
    _notifySnapshots("users");
  });

  socket.on("chat_message", (msg) => {
    const id = msg.id || ("dm_" + Date.now());
    _store.messages[id] = {
      id,
      from: msg.from,
      to: _currentKikUser ? _currentKikUser.uid : "",
      text: msg.body,
      time: msg.timestamp,
      type: "text",
      receipt: "D",
    };
    _notifySnapshots("messages", id);
  });

  socket.on("group_message", (msg) => {
    const id = msg.id || ("gm_" + Date.now());
    _store.messages[id] = {
      id,
      from: msg.from,
      groupId: msg.group_jid,
      text: msg.body,
      time: msg.timestamp,
      type: "text",
    };
    _notifySnapshots("groupMessages", id);
  });

  socket.on("group_search_results", (data) => {
    (data.groups || []).forEach((g) => {
      _store.publicGroups[g.jid] = {
        id: g.jid,
        tag: g.hashtag || g.name,
        tagLower: (g.hashtag || g.name || "").toLowerCase(),
        displayName: g.name,
        photo: g.pic_url || "",
        memberCount: g.member_count || 0,
        members: [],
        admins: [],
        owner: "",
        banned: [],
        lastMessage: "",
        lastTime: Date.now(),
      };
    });
    _notifySnapshots("publicGroups");
  });

  socket.on("is_typing", (data) => {
    const key = `${data.from}_${_currentKikUser ? _currentKikUser.uid : ""}`;
    _store["dmTyping"] = _store["dmTyping"] || {};
    _store["dmTyping"][key] = { from: data.from, typing: data.is_typing };
    _notifySnapshots("dmTyping");
  });

  socket.on("disconnected", () => {
    auth.signOut();
  });
});

// ── Snapshot listener registry ────────────────────────────────
const _snapshots = {};  // collection -> list of { query, callback }

function _notifySnapshots(collection, changedId) {
  const listeners = _snapshots[collection] || [];
  listeners.forEach(({ query, callback }) => {
    const docs = _getMatchingDocs(collection, query);
    const snap = _makeSnapshot(docs);
    callback(snap);
  });
}

function _getMatchingDocs(collection, query) {
  const store = _store[collection] || {};
  let docs = Object.entries(store).map(([id, data]) => ({ id, data: { ...data } }));

  if (query && query.wheres) {
    query.wheres.forEach(({ field, op, value }) => {
      docs = docs.filter(({ data }) => {
        if (op === "==" || op === "==") return data[field] === value;
        if (op === "array-contains") return Array.isArray(data[field]) && data[field].includes(value);
        return true;
      });
    });
  }

  if (query && query.orderByField) {
    const dir = query.orderByDir === "desc" ? -1 : 1;
    docs.sort((a, b) => ((a.data[query.orderByField] || 0) - (b.data[query.orderByField] || 0)) * dir);
  }

  if (query && query.limitN) {
    docs = docs.slice(0, query.limitN);
  }

  return docs;
}

function _makeSnapshot(docs) {
  return {
    empty: docs.length === 0,
    docs: docs.map(({ id, data }) => ({
      id,
      data: () => data,
      exists: true,
    })),
    forEach(fn) { docs.forEach(({ id, data }) => fn({ id, data: () => data, exists: true })); },
  };
}

function _makeDocSnapshot(id, data) {
  return {
    id,
    exists: !!data,
    data: () => data || {},
  };
}

// ── Firestore collection builder ──────────────────────────────
const db = {
  collection(collectionName) {
    return new CollectionRef(collectionName);
  },
};

class CollectionRef {
  constructor(name) {
    this._name = name;
    this._query = { wheres: [], orderByField: null, orderByDir: "asc", limitN: null };
  }

  doc(id) {
    return new DocRef(this._name, id);
  }

  where(field, op, value) {
    const clone = this._clone();
    clone._query.wheres.push({ field, op, value });
    return clone;
  }

  orderBy(field, dir = "asc") {
    const clone = this._clone();
    clone._query.orderByField = field;
    clone._query.orderByDir = dir;
    return clone;
  }

  limit(n) {
    const clone = this._clone();
    clone._query.limitN = n;
    return clone;
  }

  _clone() {
    const c = new CollectionRef(this._name);
    c._query = JSON.parse(JSON.stringify(this._query));
    return c;
  }

  async get() {
    // Trigger a server-side fetch for certain collections
    await this._triggerFetch();
    const docs = _getMatchingDocs(this._name, this._query);
    return _makeSnapshot(docs);
  }

  async add(data) {
    return this._handleAdd(data);
  }

  onSnapshot(callback, errorCallback) {
    // Register listener
    if (!_snapshots[this._name]) _snapshots[this._name] = [];
    const entry = { query: this._query, callback };
    _snapshots[this._name].push(entry);

    // Fire immediately with current data
    const docs = _getMatchingDocs(this._name, this._query);
    callback(_makeSnapshot(docs));

    // Trigger a server fetch
    this._triggerFetch();

    // Return unsubscribe function
    return () => {
      _snapshots[this._name] = (_snapshots[this._name] || []).filter(e => e !== entry);
    };
  }

  async _triggerFetch() {
    const token = window._kikToken;
    if (!token) return;

    if (this._name === "users") {
      await _apiGet("/api/roster", { token });
    } else if (this._name === "publicGroups") {
      // Nothing to auto-fetch; populated by search
    } else if (this._name === "messages") {
      await _apiGet("/api/message_history", { token });
    }
  }

  async _handleAdd(data) {
    const token = window._kikToken;
    if (!token) throw new Error("Not authenticated");

    if (this._name === "messages") {
      // DM message
      const to = data.to || window.currentChatUser;
      if (to) {
        await _apiPost("/api/send_message", { token, to_jid: to, body: data.text });
      }
      const id = "local_" + Date.now();
      _store.messages[id] = { id, ...data };
      return { id };
    }

    if (this._name === "groupMessages") {
      const groupId = data.groupId || (window.currentGroup && (window.currentGroup.id || window.currentGroup));
      if (groupId) {
        await _apiPost("/api/send_group_message", { token, group_jid: groupId, body: data.text });
      }
      const id = "local_gm_" + Date.now();
      _store.groupMessages = _store.groupMessages || {};
      _store.groupMessages[id] = { id, ...data };
      return { id };
    }

    if (this._name === "publicGroupMessages") {
      const groupId = data.groupId || (window.currentGroup && (window.currentGroup.id || window.currentGroup));
      if (groupId) {
        await _apiPost("/api/send_group_message", { token, group_jid: groupId, body: data.text });
      }
      const id = "local_pgm_" + Date.now();
      _store.publicGroupMessages = _store.publicGroupMessages || {};
      _store.publicGroupMessages[id] = { id, ...data };
      return { id };
    }

    if (this._name === "groups") {
      // Private group creation — not supported via Kik unofficial API
      throw new Error("Group creation is not supported via the Kik gateway. Create groups in the Kik app.");
    }

    if (this._name === "publicGroups") {
      throw new Error("Public group creation is not supported via the Kik gateway. Create groups in the Kik app.");
    }

    // Fallback: store locally
    const id = "local_" + Date.now();
    _store[this._name] = _store[this._name] || {};
    _store[this._name][id] = { id, ...data };
    return { id };
  }
}

class DocRef {
  constructor(collection, id) {
    this._collection = collection;
    this._id = id;
  }

  async get() {
    const store = _store[this._collection] || {};
    // Try to find by id or by uid field
    let data = store[this._id];
    if (!data) {
      // Search by uid field
      data = Object.values(store).find(d => d.uid === this._id || d.username === this._id);
    }

    // If user not found, request from server
    if (!data && this._collection === "users" && window._kikToken) {
      await _apiGet("/api/search_users", { token: window._kikToken, username: this._id });
      // Wait briefly for socket event
      await new Promise(r => setTimeout(r, 1500));
      data = store[this._id] || Object.values(store).find(d => d.uid === this._id || d.username === this._id);
    }

    return _makeDocSnapshot(this._id, data);
  }

  async set(data) {
    // Most set() calls in ConzChat are for sessions/dmTyping — we handle them locally
    _store[this._collection] = _store[this._collection] || {};
    _store[this._collection][this._id] = { ...data };
    _notifySnapshots(this._collection);
    return;
  }

  async update(data) {
    const token = window._kikToken;
    _store[this._collection] = _store[this._collection] || {};
    const existing = _store[this._collection][this._id] || {};
    _store[this._collection][this._id] = { ...existing, ...data };

    // Handle special update cases
    if (this._collection === "users" && token) {
      if (data.displayName !== undefined) {
        // Change display name on Kik
        // (kik unofficial: change_display_name requires first+last)
        const name = data.displayName || "";
        const parts = name.split(" ");
        const first = parts[0] || name;
        const last = parts.slice(1).join(" ") || "";
        // We don't expose this endpoint yet; log it
        console.log("[KikBridge] Display name change:", first, last);
      }
    }

    _notifySnapshots(this._collection);
    return;
  }

  async delete() {
    _store[this._collection] = _store[this._collection] || {};
    delete _store[this._collection][this._id];
    _notifySnapshots(this._collection);
  }

  onSnapshot(callback) {
    // Register a doc-level listener
    const collName = this._collection;
    const docId = this._id;
    if (!_snapshots[collName]) _snapshots[collName] = [];

    const wrappedCallback = (snap) => {
      // Find the specific doc in the snapshot
      const doc = snap.docs.find(d => d.id === docId);
      if (doc) callback(doc);
    };

    const entry = { query: { wheres: [] }, callback: wrappedCallback };
    _snapshots[collName].push(entry);

    // Fire immediately
    const store = _store[collName] || {};
    const data = store[docId];
    callback(_makeDocSnapshot(docId, data));

    return () => {
      _snapshots[collName] = (_snapshots[collName] || []).filter(e => e !== entry);
    };
  }

  collection(subCollection) {
    // Subcollection — treat as top-level for simplicity
    return new CollectionRef(`${this._collection}_${subCollection}`);
  }
}

// ── Search helper exposed to app.js ──────────────────────────
window.searchUsersOnKik = async function(query) {
  const token = window._kikToken;
  if (!token) return;
  await _apiGet("/api/search_users", { token, username: query });
};

window.searchGroupsOnKik = async function(query) {
  const token = window._kikToken;
  if (!token) return;
  await _apiGet("/api/search_groups", { token, query });
};

// ── Typing indicator helpers ──────────────────────────────────
window.sendKikTyping = async function(peerJid, isTyping) {
  const token = window._kikToken;
  if (!token) return;
  await _apiPost("/api/typing", { token, peer_jid: peerJid, is_typing: isTyping });
};

// ── Auto-reconnect on page load ───────────────────────────────
// If a token is stored, attempt to verify it's still valid
(async function tryAutoReconnect() {
  const token = window._kikToken;
  if (!token) return;
  try {
    const res = await _apiGet("/api/health");
    if (res.status !== "ok") {
      window._kikToken = null;
      localStorage.removeItem("kik_token");
    }
    // Note: The session may have expired server-side (server restart).
    // In that case, auth.onAuthStateChanged will fire with null and
    // the user will be shown the login screen.
  } catch (e) {
    console.warn("[KikBridge] Gateway unreachable:", e.message);
  }
})();

console.log("[KikBridge] firebase.js replacement loaded ✓");
