# Task 1 Report: Backend - Group Number + User Room Settings

## 完成状态

✅ 已完成所有步骤

## 修改内容

### 1. ChatRoom 实体 - 添加 groupNumber 字段
**文件:** `/Users/a0000/Desktop/workplace/miaomiaoC/backend/src/main/java/sen/yuhuang/backend/entity/ChatRoom.java`

- 新增字段 `groupNumber` (VARCHAR 6, UNIQUE)
- 用于存储6位自动生成的群号

### 2. UserRoomSetting 实体 - 新建
**文件:** `/Users/a0000/Desktop/workplace/miaomiaoC/backend/src/main/java/sen/yuhuang/backend/entity/UserRoomSetting.java`

- 新建实体，映射 `user_room_setting` 表
- 包含字段：
  - `id` (主键)
  - `userId` (用户ID)
  - `roomId` (房间ID)
  - `isPinned` (是否置顶, 默认 false)
  - `isMuted` (是否免打扰, 默认 false)
  - `createTime` (创建时间)
- 唯一约束：`user_id + room_id`

### 3. UserRoomSettingRepository - 新建
**文件:** `/Users/a0000/Desktop/workplace/miaomiaoC/backend/src/main/java/sen/yuhuang/backend/repository/UserRoomSettingRepository.java`

- 新建仓库接口
- 提供查询方法：
  - `findByUserIdAndRoomId(Long userId, Long roomId)` - 查询用户特定房间的设置
  - `findByUserId(Long userId)` - 查询用户所有房间设置

### 4. ChatRoomRepository - 更新
**文件:** `/Users/a0000/Desktop/workplace/miaomiaoC/backend/src/main/java/sen/yuhuang/backend/repository/ChatRoomRepository.java`

- 新增查询方法：
  - `findByGroupNumber(String groupNumber)` - 根据群号查找房间
  - `existsByGroupNumber(String groupNumber)` - 检查群号是否已存在

### 5. ChatMessageService - 更新
**文件:** `/Users/a0000/Desktop/workplace/miaomiaoC/backend/src/main/java/sen/yuhuang/backend/service/ChatMessageService.java`

- 注入 `UserRoomSettingRepository`
- 新增私有方法：
  - `generateGroupNumber()` - 生成唯一的6位群号（100000-999999）
- 更新 `createRoom` 方法：
  - 创建房间时自动生成群号
- 新增公共方法：
  - `findByGroupNumber(String groupNumber)` - 根据群号查找房间
  - `togglePin(Long userId, Long roomId)` - 切换房间置顶状态
  - `toggleMute(Long userId, Long roomId)` - 切换房间免打扰状态
  - `getUserRoomSettings(Long userId)` - 获取用户所有房间设置

### 6. ChatController - 更新
**文件:** `/Users/a0000/Desktop/workplace/miaomiaoC/backend/src/main/java/sen/yuhuang/backend/controller/ChatController.java`

- 新增 API 端点：
  - `GET /api/chat/rooms/join/{groupNumber}` - 根据群号获取房间信息
  - `POST /api/chat/rooms/{roomId}/pin` - 切换房间置顶状态
  - `POST /api/chat/rooms/{roomId}/mute` - 切换房间免打扰状态
  - `GET /api/chat/rooms/settings` - 获取用户房间设置列表

## 编译验证

✅ 编译成功，无错误

## 技术细节

### 群号生成策略
- 6位数字，范围 100000-999999
- 使用 `String.format("%06d", ...)` 格式化
- 循环生成直到找到唯一群号
- 使用数据库唯一约束保证并发安全

### 用户房间设置
- 使用 `upsert` 模式：先查询，不存在则新建
- `togglePin` 和 `toggleMute` 方法自动处理创建和更新
- 使用 `Boolean.TRUE.equals()` 防止空指针

### API 设计
- 所有端点都需要身份验证（通过 `X-Username` 头）
- 统一使用 `Result` 类返回响应
- 错误信息使用中文

## 数据库迁移

需要执行以下 SQL 创建 `user_room_setting` 表：

```sql
CREATE TABLE user_room_setting (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    is_pinned BOOLEAN DEFAULT FALSE,
    is_muted BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_room (user_id, room_id),
    INDEX idx_user_id (user_id),
    INDEX idx_room_id (room_id)
);
```

并在 `chat_room` 表中添加 `group_number` 列：

```sql
ALTER TABLE chat_room ADD COLUMN group_number VARCHAR(6) UNIQUE;
```

## 下一步

Task 1 已完成，可以继续 Task 2（前端 API + 学生学院要求）。
