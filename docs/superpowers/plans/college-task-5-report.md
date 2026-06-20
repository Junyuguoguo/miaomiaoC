# Task 5: Chat Room Management in Teacher Admin Panel

## Status: Completed

## Summary
Added "聊天室管理" (Chat Room Management) section to the teacher admin panel (`TeacherManagerPage.vue`).

## Changes Made

### File: `frontend/src/views/teacher/TeacherManagerPage.vue`

1. **Sidebar menu item** - Added `el-menu-item index="10"` with `ChatLineSquare` icon after "个人中心" (index 9).

2. **Icon import** - Added `ChatLineSquare` to the `@element-plus/icons-vue` import.

3. **API import** - Added `getRooms`, `createRoom`, `deleteRoom` from `@/api/chat` (aliased as `getRoomsApi`, `createRoomApi`, `deleteRoomApi`).

4. **Reactive data** - Added `chatRoomList`, `showCreateRoomDialog`, `newRoomForm` refs.

5. **Methods** - Added `loadChatRooms()`, `handleCreateRoom()`, `handleDeleteRoom()` functions with proper validation, confirmation dialogs, and error handling.

6. **Menu handler** - Added `case '10'` in `handleMenuSelect` to call `loadChatRooms()`, and added `'10': '聊天室管理'` to the title map.

7. **Refresh support** - Updated `handleRefresh` to reload chat rooms when on menu index '10'.

8. **Template sections** - Added:
   - Chat room table (ID, room name, college tag, member count, delete action)
   - Create room dialog with room name input and college selector (8 college options + null for "全校大厅")

## Dependencies
- Chat API endpoints (`/api/chat/rooms` GET/POST/DELETE) must exist in backend
