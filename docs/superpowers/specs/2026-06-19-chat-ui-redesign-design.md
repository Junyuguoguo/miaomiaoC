# Chat UI Redesign Design Spec

> **Scope:** miaomiaoC online exam system — redesign both chat pages (ChatHub.vue and ChatPage.vue) with WeChat-inspired visuals and rich animations.

**Goal:** Transform the basic flat chat UI into a modern, WeChat-inspired messaging experience with bubble tails, gradients, glassmorphism, and smooth CSS animations.

**Architecture:** Visual redesign of two existing Vue 3 SFCs. No backend changes. No new files. All animations via CSS `@keyframes` and transitions. WebSocket logic, API calls, and data flow remain untouched.

**Tech Stack:** Vue 3 Composition API, Element Plus, CSS `@keyframes`, `backdrop-filter`, project CSS variables (`main.css`)

## Global Constraints

- Use project CSS variables: `--app-primary`, `--app-primary-dark`, `--app-primary-soft`, `--app-border`, `--app-text`, `--app-text-muted`, `--app-surface`, `--app-bg`, `--app-radius`, `--app-shadow-sm`, `--app-shadow-md`
- Preserve all existing script logic (WebSocket, API calls, routing, state management) — only template structure and styles change
- Element Plus components (`el-avatar`, `el-button`, `el-input`, `el-tag`) kept where they serve a purpose; replaced with native HTML where Element Plus adds no value
- `px` units (matching existing codebase convention, not `rpx`)
- No new npm dependencies
- Responsive: `@media (max-width: 768px)` breakpoint for sidebar collapse on ChatHub

---

## Part 1: ChatPage.vue — Private/Group Chat

### File
- `frontend/src/views/chat/ChatPage.vue` — full rewrite of template + style; script unchanged except for `formatTime` enhancement

### Bubble Styling

**Peer messages (left):**
- White background, `border-radius: 16px 16px 16px 4px` (rounded except bottom-left)
- Left tail via CSS `::before` border triangle, `10px`
- Max-width `60%`, padding `10px 16px`
- Shadow: `0 2px 8px rgba(0,0,0,0.06)`
- Text color: `var(--app-text)`, font-size `14px`, line-height `1.6`

**My messages (right):**
- Gradient: `linear-gradient(135deg, var(--app-primary), var(--app-primary-dark))`
- `border-radius: 16px 16px 4px 16px` (rounded except bottom-right)
- Right tail via CSS `::after` border triangle
- Text color: `#fff`
- Shadow: `0 2px 12px rgba(37,99,235,0.2)`

### Avatars
- `42px` circle via `el-avatar`
- Peer: `2px` solid `var(--app-primary-soft)` border
- My avatar: `2px` solid `var(--app-primary)` border

### Time Separators
- Display when interval > 5 minutes between messages
- Centered, pill background: `var(--app-surface-muted)`, `border-radius: 10px`, padding `4px 12px`
- Font-size `12px`, color `var(--app-text-muted)`
- Format: today → "HH:mm", yesterday → "昨天 HH:mm", older → "MM/DD HH:mm"

### Entrance Animation
- `@keyframes slideIn` — `translateY(16px), opacity: 0` → `translateY(0), opacity: 1`
- Duration `300ms`, easing `cubic-bezier(0.25, 0.46, 0.45, 0.94)`
- Apply via `.message-item.animate` class added on mount for existing messages, and on push for new messages

### Header
- Glassmorphism: `background: rgba(255,255,255,0.85)`, `backdrop-filter: blur(12px)`
- `border-bottom: 1px solid var(--app-border)`
- Left: back button with `hover` background transition
- Center: chat title
- Right: connection status dot (`8px` circle, green `#22c55e` or red `#ef4444`) with pulse animation on connected

### Input Bar
- Glassmorphism: `background: rgba(255,255,255,0.9)`, `backdrop-filter: blur(12px)`, `border-top: 1px solid var(--app-border)`
- `box-shadow: 0 -2px 12px rgba(0,0,0,0.04)`
- Layout: `[image-btn] [input] [send/+btn]` with `10px` gap
- Image button: `40px` rounded square, `var(--app-primary-soft)` background, camera CSS icon
- Input: native `<input>` (replace `el-input textarea`), `height: 40px`, `border-radius: 20px`, `background: var(--app-surface-muted)`
- On focus: border transitions to `var(--app-primary)`, shadow expands `0 0 0 3px rgba(37,99,235,0.1)`
- Send/Plus button: same pattern as bistu chat — empty → "+" circle, has text → blue "发送" pill

### Extension Panel
- Slides up from below input bar
- 3 icons: 拍照、相册、位置 (all show `ElMessage.info('功能开发中')`)
- `@keyframes slideUp` — `translateY(100%) → translateY(0)`, `250ms ease-out`

---

## Part 2: ChatHub.vue — Chat Lobby

### File
- `frontend/src/views/chat/ChatHub.vue` — full rewrite of template + style; script unchanged except minor `formatTime` enhancement

### Message Area (left main)
- Same bubble style, time separators, and entrance animation as ChatPage
- Group chat context: show sender name above bubble (font-size `12px`, color `var(--app-text-muted)`)
- Own messages: right-aligned, no sender name shown
- Messages load with stagger animation: each `.message-row` delays `index * 40ms`

### Header
- Glassmorphism: `background: rgba(255,255,255,0.85)`, `backdrop-filter: blur(12px)`
- Left: back button with hover color transition
- Center: "综合交流大厅" title, font-weight `600`
- Right: connection badge — green pill with `@keyframes pulse` (opacity `1 → 0.5 → 1`, `2s infinite`) on the dot indicator

### Input Bar
- Same glassmorphism style as ChatPage
- Enter sends, Shift+Enter newline (native `<textarea>` with `@keydown.enter.exact.prevent`)
- Send button: same send/+ toggle pattern

### Right Sidebar (contacts + search)
- Background: `var(--app-surface)`, `border-left: 1px solid var(--app-border)`
- Width: `260px`, collapses on mobile (`@media max-width: 768px` → hidden)

**Search box:**
- `border-radius: 8px`, `background: var(--app-surface-muted)`
- Focus: `border-color: var(--app-primary)`, `box-shadow: 0 0 0 3px rgba(37,99,235,0.08)` transition
- Debounced search (existing 300ms logic preserved)

**Contact cards:**
- `padding: 10px 12px`, `border-radius: var(--app-radius)`
- Avatar: `36px` circle
- Name: `font-size: 14px`, `font-weight: 500`, `color: var(--app-text)`
- Hover: `background: var(--app-primary-soft)`, `transform: translateY(-1px)`, `transition: 150ms`
- Unread badge: `background: var(--app-danger)`, `color: #fff`, capsule shape, `min-width: 20px`, `height: 20px`, `border-radius: 10px`
- Count > 99 displays "99+"

**Empty state:**
- Centered muted icon + text, `padding: 40px 0`

### Stagger Animations
- Contact items: `@keyframes fadeInUp`, each delayed `index * 30ms`
- Message rows: same as ChatPage but with `index * 40ms` delay

---

## Part 3: Script Changes (minimal)

### ChatPage.vue script
- Add `formatTime` enhancement: detect today/yesterday/older (currently only formats `HH:mm`)
- Add `animate` class toggling for new messages in `handleMessageReceived` and initial `loadMessages`
- All WebSocket, routing, API logic untouched

### ChatHub.vue script
- Add `formatTime` enhancement: same today/yesterday/older logic
- Add stagger delay computation for messages and contacts
- All WebSocket, routing, API logic untouched

---

## Testing

- Visual verification in browser dev server (`npm run dev`)
- Test: ChatPage — send message → bubble slides in with correct side animation
- Test: ChatPage — input focus → border turns blue with shadow
- Test: ChatPage — type text → "+" transforms to "发送"
- Test: ChatPage — tap "+" → extension panel slides up
- Test: ChatHub — messages load with stagger animation
- Test: ChatHub — contacts hover → lift + blue background
- Test: ChatHub — search input focus → shadow expands
- Test: responsive — resize to <768px → sidebar hides
