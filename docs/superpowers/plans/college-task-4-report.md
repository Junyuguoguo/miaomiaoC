# Task 4 Report: ChatHub Left-Right Split Layout Redesign

**Date:** 2026-06-19
**File:** `frontend/src/views/chat/ChatHub.vue`
**Commit:** `83594fe` feat(chat): redesign ChatHub with room list + left-right split layout

## Changes Made

### Template Restructure
- Replaced single message area + sidebar with left-right split layout
- **Left panel (280px):**
  - Room section at top with room cards (name + college tag)
  - Contact section at bottom with search + contact list
  - "Create room" button visible for teachers only (role >= 3)
- **Right panel (flex: 1):**
  - Room header showing current room name + connection badge
  - Message list (preserved all existing rendering: avatars, bubbles, teacher badges, time separators)
  - Input bar (preserved existing send behavior)
- Added `el-dialog` for room creation with name + college select

### Script Changes
- Removed `publicRoomId` ref (replaced by `currentRoom`)
- Added room state: `rooms`, `currentRoom`, `showCreateRoom`, `newRoom`, `currentRoomSubId`
- Added computed: `isTeacher` = `getUserRoleId >= 3`
- Added `loadRooms()`: calls `getRooms()` API, auto-selects first room
- Added `selectRoom(room)`: loads messages via `getRoomMessages(room.id, 0, 50)`, joins room, subscribes WebSocket
- Added `handleCreateRoom()`: calls `createRoom()` API with `roomName` + optional `college`, reloads rooms
- Updated `handleMessageReceived` to filter by `currentRoom.value.id`
- Updated `sendMessage` to use `currentRoom.value.id` instead of `publicRoomId.value`
- Updated `connectAndSubscribe` to subscribe to current room after connect
- Imported `getRooms` and `createRoom` from `@/api/chat`

### CSS Changes
- `.hub-body`: flex layout with `height: calc(100vh - 56px)`
- `.left-panel`: 280px width, border-right, vertical flex, overflow scroll
- `.right-panel`: flex: 1, vertical flex column
- `.room-card`: hover effect, `.active` highlight with border
- `.college-tag`: gold gradient pill for specific colleges, gray for "全校"
- `.add-room-btn`: dashed border text button
- `.room-header`: 48px height, smaller than hub-header
- `.room-badge`: smaller connection badge in room header
- Mobile responsive: left panel hidden on screens <= 768px

### Preserved
- All message rendering, bubble styles, teacher badge, avatar click to private chat
- Contact search, contact list, unread badges
- WebSocket connection logic, optimistic message rendering
- Time formatting, scroll-to-bottom behavior
- Back navigation logic (teacher vs student)

## API Dependencies
- `getRooms()` - GET `/api/chat/rooms`
- `createRoom(data)` - POST `/api/chat/rooms` (body: `{ roomName, college? }`)
- `getRoomMessages(roomId, page, size)` - GET `/api/chat/room/{roomId}`
- `joinRoom(roomId)` - POST `/api/chat/rooms/{roomId}/join`

## Notes
- Uses `room.roomName || room.name` as fallback for room display name
- `currentRoomSubId` tracks WebSocket subscription for future room switching
- `handleMessageReceived` filters messages by `roomId` to avoid cross-room leakage
