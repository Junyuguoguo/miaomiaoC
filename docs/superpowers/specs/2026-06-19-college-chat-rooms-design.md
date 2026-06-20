# College Chat Rooms + Read/Unread Fix Design Spec

> **Scope:** miaomiaoC — fix read/unread badge behavior, add college-based chat rooms, redesign ChatHub layout.

**Goal:** Students chat within their college room; teachers manage all rooms; unread badges update correctly.

**Architecture:** Add `college` field to ChatRoom and User registration. Redesign ChatHub as left-right split (room list + message area). Fix frontend mark-as-read integration.

## Global Constraints

- ChatRoom entity: add `college` column VARCHAR(100), nullable (null = 全校大厅)
- User entity already has `college` field (added in teacher identity feature)
- Student registration: add college selection dropdown (required for students)
- ChatHub: left panel = room list + contacts, right panel = messages + input
- Room visibility: students see 全校大厅 + their college room; teachers see all rooms
- Teacher can create/delete rooms from teacher admin panel
- Backend: add CRUD endpoints for room management
- Fix: ChatPage calls `markAllAsRead(contactId)` on mount; ChatHub refreshes on route change

---

## Part 1: Fix Read/Unread

### ChatPage.vue
- After `loadMessages(0)` completes in `onMounted`, call `markAllAsRead(route.query.userId)` for private chats
- This marks all messages from that contact as read

### ChatHub.vue
- Add `onActivated` or watch route to reload contacts when returning from ChatPage
- Alternatively: call `loadContacts()` in `onMounted` AND add a `watch` on route path

---

## Part 2: Backend — College Chat Rooms

### ChatRoom entity
- Add `college` field (VARCHAR 100, nullable)
- Existing `type` field (PUBLIC/PRIVATE) — keep as-is, add college filtering

### New endpoints in ChatController
- `GET /api/chat/rooms` — list rooms visible to current user (filtered by role/college)
- `POST /api/chat/rooms` — create room (teacher only): name, description, college
- `DELETE /api/chat/rooms/{roomId}` — delete room (teacher only)

### Room visibility logic
- If user is teacher/admin: return all rooms
- If user is student: return rooms where college IS NULL (全校大厅) OR college matches user's college

---

## Part 3: Frontend — Registration College Selection

### RegisterPage.vue
- Add college dropdown (required for students, optional for teachers)
- Options: predefined list of colleges (计算机学院, 机械学院, etc.)
- Send `college` field in registration request

### Backend register endpoint
- Accept `college` field, save to User.college

---

## Part 4: Frontend — ChatHub Redesign

### Layout
- Left panel (280px): room list at top, contacts at bottom
- Right panel: current room's messages + input bar
- Click a room → load its messages in right panel
- Click a contact → open private chat (navigate to /chat/private)

### Room list
- Show room name + college tag (or "全校" for null college)
- Active room highlighted
- Teacher sees "+" button to create room

### Teacher room management
- Create room dialog: name, college (dropdown), description
- Delete room: long press or button

---

## Part 5: Backend — Room Management for Teachers

### TeacherManagerPage.vue
- Add "聊天室管理" menu item (index "10")
- Table: room name, college, member count, actions (delete)
- Create room dialog: name, college dropdown, description

### Backend
- Reuse the CRUD endpoints from Part 2

---

## Testing

- Student A (计算机学院) → sees 全校大厅 + 计算机学院 room
- Student B (机械学院) → sees 全校大厅 + 机械学院 room (NOT 计算机学院)
- Teacher → sees all rooms
- Teacher creates "计算机学院" room → students in that college see it
- Open private chat → unread badge disappears on ChatHub
- Send message in private chat → return to ChatHub → badge updated
