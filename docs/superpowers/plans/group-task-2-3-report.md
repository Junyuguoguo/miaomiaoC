# Tasks 2 & 3 Report: Room APIs + College Requirement + ChatHub Redesign

## Summary

Implemented Tasks 2 and 3 from the group-pin-mute-college plan. All changes compile successfully (vite build passes).

## Files Modified

### 1. `frontend/src/api/chat.js`
- Added `joinByGroupNumber(groupNumber)` -- GET `/api/chat/rooms/join/{groupNumber}`
- Added `togglePinRoom(roomId)` -- POST `/api/chat/rooms/{roomId}/pin`
- Added `toggleMuteRoom(roomId)` -- POST `/api/chat/rooms/{roomId}/mute`
- Added `getRoomSettings()` -- GET `/api/chat/rooms/settings`

### 2. `frontend/src/views/exam/HomePage.vue`
- Changed "报考院校" field from `el-input` to `el-select` dropdown
- Options: 计算机学院, 机械学院, 电子信息学院, 经济管理学院, 外国语学院, 理学院, 人文社科学院, 自动化学院
- Added required validation rule: `{ required: true, message: '请选择报考院校', trigger: 'change' }`

### 3. `frontend/src/views/chat/ChatHub.vue` -- Major redesign

**Template changes:**
- Added "加入群聊" button in room section header
- Room list now uses `sortedRooms` (pinned first) with stagger animation (40ms delay per card)
- Room cards show pin icon and mute icon when active
- Room cards support right-click context menu (置顶/免打扰 toggle)
- Room header redesigned: group number display + copy button on the left, pin/mute toggle buttons + connection badge on the right
- Added join dialog (el-dialog) with group number input
- Added context menu overlay with positioned menu

**Script changes:**
- Imported 4 new API functions
- Added state refs: `roomSettings`, `showJoinDialog`, `joinGroupNumber`, `contextMenuRoom`, `showContextMenu`, `contextMenuPos`
- Added computed `sortedRooms` (pinned first, then by id)
- Added `isRoomPinned(roomId)` and `isRoomMuted(roomId)` helpers
- Added `loadRoomSettings()` -- loads user pin/mute settings
- Added `handleTogglePin(roomId)` and `handleToggleMute(roomId)` -- toggles and reloads
- Added `openContextMenu()` / `closeContextMenu()` -- right-click context menu
- Added `handleJoinGroup()` -- joins room by group number, adds to list
- Added `copyGroupNumber()` -- copies group number to clipboard
- Modified `loadRooms()` to call `loadRoomSettings()` after loading rooms, auto-selects first sorted room

**CSS changes:**
- `.section-header` flex layout for title + join button
- `.join-room-btn` styled outline button
- `.room-card` stagger `fadeInUp` animation (300ms, delay per index)
- `.room-card` hover lift: `translateY(-2px)` + shadow
- `.room-card.active` left border: `3px solid var(--app-primary)`
- `.room-card-left` / `.room-icons` / `.room-icon` for pin/mute icons
- `.room-header-left` / `.room-header-right` layout
- `.group-number-display` small gray badge with copy button
- `.header-icon-btn` for pin/mute toggle buttons
- `.context-menu-overlay` / `.context-menu` / `.context-menu-item` positioned absolutely
- `.join-dialog-body` / `.join-hint` clean styling

## Build Status

Vite build passes cleanly (7.23s, no errors).

## Commit

```
feat: add group number, pin/mute, join-by-number, dynamic UI to ChatHub + require college
```

3 files changed, 437 insertions(+), 17 deletions(-)
