# Task 2: College Chat Rooms - Implementation Report

## Summary
Successfully implemented college-based chat rooms with CRUD operations and visibility filtering.

## Changes Made

### 1. ChatRoom Entity (`backend/src/main/java/sen/yuhuang/backend/entity/ChatRoom.java`)
- Added `college` field with `@Column(name = "college", length = 100)` annotation
- Field allows NULL values to represent university-wide (global) rooms
- Uses Lombok `@Data` for auto-generated getters/setters

### 2. ChatRoomRepository (`backend/src/main/java/sen/yuhuang/backend/repository/ChatRoomRepository.java`)
Added two new query methods:
- `findVisibleRooms(String college)` - Returns rooms where college is NULL (university-wide) OR matches user's college
- `findAllRooms()` - Returns all rooms (for admin users)

### 3. ChatMessageService (`backend/src/main/java/sen/yuhuang/backend/service/ChatMessageService.java`)
Added three new methods:
- `getVisibleRooms(Long userId)` - Returns visible rooms based on user role (admin sees all, users see university-wide + their college)
- `createRoom(String name, String description, String college)` - Creates a new PUBLIC chat room
- `deleteRoom(Long roomId)` - Deletes a chat room by ID

### 4. ChatController (`backend/src/main/java/sen/yuhuang/backend/controller/ChatController.java`)
Modified and added endpoints:
- **Modified** `GET /api/chat/rooms` - Now uses visibility filtering based on authenticated user
- **Added** `POST /api/chat/rooms` - Creates new room (requires teacher/admin role: roleId >= 3)
- **Added** `DELETE /api/chat/rooms/{roomId}` - Deletes room (requires teacher/admin role: roleId >= 3)

## Field Name Adaptations
Adapted the implementation to match existing entity field names:
- Used `roomName` instead of `name`
- Used `roomType` instead of `type`
- Used `currentMembers` instead of `memberCount`

## Visibility Logic
- **Admin/Teacher users (roleId >= 3)**: Can see all rooms
- **Regular users**: Can see university-wide rooms (college = NULL) + rooms in their college

## Compilation & Verification
- Backend compiles successfully with no errors
- All changes follow existing code patterns and conventions

## Commit
```
feat(backend): add college chat rooms with CRUD and visibility filtering
```

Commit ID: 9c14e90

## Files Modified
1. `backend/src/main/java/sen/yuhuang/backend/entity/ChatRoom.java`
2. `backend/src/main/java/sen/yuhuang/backend/repository/ChatRoomRepository.java`
3. `backend/src/main/java/sen/yuhuang/backend/service/ChatMessageService.java`
4. `backend/src/main/java/sen/yuhuang/backend/controller/ChatController.java`
