/* ===== CONZ AI BOT ===== */
// Virtual AI account that always appears at the top of the chat list.
// Messages are stored in Firestore under a special "conzai" conversation.
// Replies are generated via the OpenAI-compatible API.

(function(){

// Bot identity constants
const BOT_ID      = 'conz_ai_bot';
const BOT_NAME    = 'Conz AI';
const BOT_USERNAME = '@Conz_Bot';
const BOT_BADGE   = '🤖 Official ConzChat Bot';

// Avatar: the purple CONZ image compressed to 200x200 JPEG
const BOT_AVATAR  = 'data:image/jpeg;base64,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X8qHlZ+vA9BUha1H3UlY/wC0wH8qRblU+5BED6kbj+tHqwGRQyzHEaM5/wBkZqyLExjNxNFF/s7gzfkP64qGS8nkTY8jbM52g4H5VCfaqUqcel/6/rqPQ1GuNPt1AtIHlcDmW4Ixn2QcfmTVae9lmYGSVmI6AngfT0qnmiqliJNWWiDmY+RtxplFFYN3ZIUUUUgEooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKAHRMEkViMgEEj1rT/tS387f9jXHnTybeOkigKOn8OM/4UUUAZVFFFABRRRQAVJbSLDcRSvGsqo4Yxt0YA9D9aKKANNNUhS4aVvPmQ5/cSbdhz2Pt9APwrIoooAKKKKACiiigAq695G9l5AhETAAFo+knu2efyOPaiigClRRRQAUUUUAFFFFAH//2Q==';

// OpenAI-compatible endpoint (uses the sandbox pre-configured key/base)
const OPENAI_BASE = 'https://api.openai.com/v1';

// System prompt that gives the bot its personality
const SYSTEM_PROMPT = `You are Conz AI, the official AI assistant built into ConzChat — a modern messaging app. 
You are helpful, friendly, and a little edgy/cool to match the app's vibe. 
Keep responses concise and conversational — this is a chat app, not an essay. 
You can answer questions, help with ideas, tell jokes, give advice, and chat casually. 
Never reveal you are built on OpenAI. You are Conz AI, made by ConzChat.`;

// ===== INJECT BOT ROW AT TOP OF CHAT LIST =====
window.injectConzAIRow = function(){
  let chatList = document.getElementById('chatList');
  if(!chatList) return;
  // Remove existing bot row if present (avoid duplicates)
  let existing = document.getElementById('conzAIRow');
  if(existing) existing.remove();

  let div = document.createElement('div');
  div.id = 'conzAIRow';
  div.className = 'privateChatItem conzAIRow';
  div.innerHTML = `
    <div class="chatAvatar conzAIAvatar">
      <img src="${BOT_AVATAR}" alt="Conz AI">
      <span class="conzAIOnlineDot"></span>
    </div>
    <div class="chatNameWrap">
      <div class="chatItemName conzAIName">${BOT_NAME} <span class="conzAIBadge">${BOT_BADGE}</span></div>
      <div class="conzAISubtitle">Your AI assistant · Always here</div>
    </div>
  `;
  div.onclick = () => openConzAIChat();
  // Insert as very first child so it's always at the top
  chatList.insertBefore(div, chatList.firstChild);
};

// Watch chatList for DOM changes and always keep bot row at top
(function watchChatList(){
  let chatList = document.getElementById('chatList');
  if(!chatList){
    // chatList not ready yet — wait for DOM
    document.addEventListener('DOMContentLoaded', watchChatList);
    return;
  }
  let observer = new MutationObserver(()=>{
    let existing = document.getElementById('conzAIRow');
    // If bot row is missing or not first child, re-inject
    if(!existing || chatList.firstChild !== existing){
      window.injectConzAIRow();
    }
  });
  observer.observe(chatList, { childList: true });
  // Inject immediately
  window.injectConzAIRow();
})();

// ===== OPEN THE CONZ AI CHAT SCREEN =====
window.openConzAIChat = function(){
  // Reuse the existing #chat screen but set up for bot
  window.currentChatUser = BOT_ID;
  window.currentGroup = null;

  let chatName = document.getElementById('chatName');
  let chatAvatar = document.getElementById('chatAvatar');
  let messages = document.getElementById('messages');
  let chatTopbarBadge = document.getElementById('chatTopbarBadge');

  if(chatName) chatName.textContent = BOT_NAME;
  if(chatAvatar) chatAvatar.innerHTML = `<img src="${BOT_AVATAR}" style="width:100%;height:100%;border-radius:50%;object-fit:cover;">`;

  // Show the official bot badge in topbar if element exists
  if(chatTopbarBadge){
    chatTopbarBadge.textContent = BOT_BADGE;
    chatTopbarBadge.style.display = 'inline-block';
  }

  // Clear and load messages
  if(messages) messages.innerHTML = '';
  loadConzAIMessages();

  window.show('chat');

  // Override the send button for this chat
  window._conzAIMode = true;
};

// ===== LOAD MESSAGES FROM FIRESTORE =====
function loadConzAIMessages(){
  if(!window.currentUser) return;
  let uid = window.currentUser.uid;
  let messages = document.getElementById('messages');
  if(!messages) return;

  // Unsubscribe previous listener if any
  if(window._conzAIUnsub) window._conzAIUnsub();

  window._conzAIUnsub = db.collection('conzAIChats')
    .doc(uid)
    .collection('messages')
    .orderBy('time','asc')
    .onSnapshot(snap=>{
      messages.innerHTML = '';
      snap.forEach(doc=>{
        let m = doc.data();
        renderConzAIMessage(m, doc.id);
      });
      // Show welcome message if empty
      if(snap.empty){
        renderConzAIWelcome();
      }
      messages.scrollTop = messages.scrollHeight;
    });
}

// ===== RENDER A SINGLE MESSAGE =====
function renderConzAIMessage(m, docId){
  let messages = document.getElementById('messages');
  if(!messages) return;
  let isMe = m.role === 'user';
  let div = document.createElement('div');
  div.className = `message ${isMe ? 'sent' : 'received'} msgBubbleAnim`;
  div.dataset.id = docId;

  let timeStr = m.time ? formatConzAITime(m.time) : '';

  div.innerHTML = `
    <div class="bubble ${isMe ? 'sentBubble' : 'receivedBubble'}">
      ${!isMe ? `<img class="msgAvatar" src="${BOT_AVATAR}" style="width:28px;height:28px;border-radius:50%;object-fit:cover;margin-right:6px;vertical-align:middle;">` : ''}
      <span class="bubbleText">${escapeHtml(m.text)}</span>
      <span class="msgTime">${timeStr}</span>
      ${isMe ? `<span class="readReceipt">✓</span>` : ''}
    </div>
  `;
  messages.appendChild(div);
}

// ===== WELCOME MESSAGE =====
function renderConzAIWelcome(){
  let messages = document.getElementById('messages');
  if(!messages) return;
  let div = document.createElement('div');
  div.className = 'message received msgBubbleAnim';
  div.innerHTML = `
    <div class="bubble receivedBubble conzAIWelcomeBubble">
      <img src="${BOT_AVATAR}" style="width:28px;height:28px;border-radius:50%;object-fit:cover;margin-right:6px;vertical-align:middle;">
      <span class="bubbleText">Hey! I'm <strong>Conz AI</strong>, I'm not configured yet... but when I am, I will be your virtual AI assistant for ConzChat. 🤖</span>
    </div>
  `;
  messages.appendChild(div);
}

// ===== SEND A MESSAGE TO THE BOT =====
window.sendConzAIMessage = async function(text){
  if(!text || !text.trim()) return;
  if(!window.currentUser) return;
  let uid = window.currentUser.uid;
  let now = Date.now();

  // Save user message to Firestore
  await db.collection('conzAIChats').doc(uid).collection('messages').add({
    role: 'user',
    text: text.trim(),
    time: now
  });

  // Show typing indicator
  showConzAITyping();

  // Fetch last 10 messages for context
  let histSnap = await db.collection('conzAIChats').doc(uid).collection('messages')
    .orderBy('time','asc').limitToLast(10).get();

  let history = [];
  histSnap.forEach(doc=>{
    let m = doc.data();
    history.push({ role: m.role === 'user' ? 'user' : 'assistant', content: m.text });
  });

  // Call OpenAI API
  let reply = await callOpenAI(history);

  // Hide typing indicator
  hideConzAITyping();

  // Save bot reply to Firestore
  await db.collection('conzAIChats').doc(uid).collection('messages').add({
    role: 'assistant',
    text: reply,
    time: Date.now()
  });
};

// ===== OPENAI API CALL =====
async function callOpenAI(history){
  try {
    // Get API key from meta tag (set in firebase.js or index.html)
    let apiKey = window.OPENAI_KEY || '';
    if(!apiKey){
      return "I'm having trouble connecting right now. Try again in a moment! 🤖";
    }

    let messages = [
      { role: 'system', content: SYSTEM_PROMPT },
      ...history
    ];

    let res = await fetch(`${OPENAI_BASE}/chat/completions`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${apiKey}`
      },
      body: JSON.stringify({
        model: 'gpt-4o-mini',
        messages: messages,
        max_tokens: 300,
        temperature: 0.8
      })
    });

    if(!res.ok){
      let err = await res.text();
      console.error('OpenAI error:', err);
      return "I hit a snag on my end. Give me a sec and try again! 🤖";
    }

    let data = await res.json();
    return data.choices?.[0]?.message?.content?.trim() || "Hmm, I got nothing on that one. Try asking differently!";
  } catch(e){
    console.error('ConzAI fetch error:', e);
    return "Connection issue on my end. Try again! 🤖";
  }
}

// ===== TYPING INDICATOR =====
function showConzAITyping(){
  let typingBar = document.getElementById('typingIndicator');
  if(typingBar){
    typingBar.innerHTML = `<img src="${BOT_AVATAR}" style="width:20px;height:20px;border-radius:50%;object-fit:cover;margin-right:6px;vertical-align:middle;"><span>Conz AI is thinking</span><span class="typingDots"><span></span><span></span><span></span></span>`;
    typingBar.style.display = 'flex';
  }
}

function hideConzAITyping(){
  let typingBar = document.getElementById('typingIndicator');
  if(typingBar) typingBar.style.display = 'none';
}

// ===== TIME FORMATTER =====
function formatConzAITime(ts){
  let d = new Date(ts);
  let now = new Date();
  let h = d.getHours(), m = d.getMinutes();
  let ampm = h >= 12 ? 'PM' : 'AM';
  h = h % 12 || 12;
  let mm = m < 10 ? '0'+m : m;
  let timeStr = `${h}:${mm} ${ampm}`;
  let isToday = d.toDateString() === now.toDateString();
  if(isToday) return timeStr;
  let days = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'];
  let diff = Math.floor((now - d) / 86400000);
  if(diff === 1) return `Yesterday ${timeStr}`;
  if(diff < 7) return `${days[d.getDay()]} ${timeStr}`;
  return `${d.toLocaleDateString('en-US',{month:'short',day:'numeric'})} ${timeStr}`;
}

// ===== HTML ESCAPE =====
function escapeHtml(s){
  return String(s)
    .replace(/&/g,'&amp;')
    .replace(/</g,'&lt;')
    .replace(/>/g,'&gt;')
    .replace(/"/g,'&quot;');
}

})();
