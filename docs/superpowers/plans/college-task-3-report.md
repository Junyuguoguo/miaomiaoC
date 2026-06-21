# Task 3 Report: College Selection in Registration + Room API Functions

## Status: COMPLETED

## Changes Made

### 1. frontend/src/api/chat.js
- Added `getRooms()` - GET `/api/chat/rooms`
- Added `createRoom(data)` - POST `/api/chat/rooms`
- Added `deleteRoom(roomId)` - DELETE `/api/chat/rooms/{roomId}`

### 2. frontend/src/views/login/RegisterPage.vue
- Added `collegeOptions` array with 8 colleges: 计算机学院, 机械学院, 电子信息学院, 经济管理学院, 外国语学院, 理学院, 人文社科学院, 自动化学院
- Added `college` field to `registerForm` reactive model
- Added `college` validation rule (required, trigger: change)
- Added `<el-select>` dropdown for college selection in the form template
- Included `college` in the registration API request payload

### 3. backend/.../controller/UserController.java
- Extract `college` from request map in `register()` method
- Pass `college` to `userService.register()`

### 4. backend/.../service/UserService.java
- Updated `register()` method signature to accept `college` parameter
- Added `.college(college)` to User builder before saving

## Commit
- Hash: 1 639bab3
- Message: feat: add college selection to registration + room API functions
