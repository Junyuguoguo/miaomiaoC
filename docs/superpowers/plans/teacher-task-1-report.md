# Task 1 Report: Add `college` Field to User Entity + ChatMessageResponse

**Status:** COMPLETE

## Changes Made

### 1. `backend/src/main/java/sen/yuhuang/backend/entity/User.java`
- Added `college` field: `@Column(name = "college", length = 100) private String college;` after the `avatar` field.
- Lombok `@Data` auto-generates getter/setter.

### 2. `backend/src/main/java/sen/yuhuang/backend/dto/ChatMessageResponse.java`
- Added `private String senderCollege;` and `private Long senderRole;` after `senderAvatar`.

### 3. `backend/src/main/java/sen/yuhuang/backend/service/ChatMessageService.java`
Updated all four response-building paths to populate `senderCollege` and `senderRole`:
- **`sendPrivateMessage`**: Added `resp.setSenderCollege(sender.getCollege())` and `resp.setSenderRole(sender.getRoleId())`.
- **`sendRoomMessage`**: Same additions.
- **`getPrivateMessages`**: Wrapped avatar/college/role setting in `if (sender != null)` block.
- **`getRoomMessages`**: Replaced `avatarMap` (Map<Long, String>) with `senderMap` (Map<Long, User>) to batch-load full User objects, then set avatar, college, and role from the User entity.

### 4. `backend/src/main/java/sen/yuhuang/backend/controller/UserController.java`
- Added `String college = request.get("college");` in `updateUserInfo`.
- Passed `college` to `userService.updateUserInfo()`.

### 5. `backend/src/main/java/sen/yuhuang/backend/service/UserService.java`
- Added `String college` parameter to `updateUserInfo()` method signature.
- Passed `college` to `userRepository.updateUserByUserId()`.

### 6. `backend/src/main/java/sen/yuhuang/backend/repository/UserRepository.java`
- Added `u.college = :college` to the `@Query` UPDATE statement.
- Added `@Param("college") String college` to the `updateUserByUserId()` method signature.

## Verification
- `./mvnw compile -q` completed with **BUILD SUCCESS** (no errors).
