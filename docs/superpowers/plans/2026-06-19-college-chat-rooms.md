# College Chat Rooms + Read/Unread Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix unread badge behavior, add college-based chat rooms with teacher management, redesign ChatHub as left-right split layout.

**Architecture:** Backend adds `college` to ChatRoom + room CRUD endpoints. Frontend redesigns ChatHub, fixes mark-as-read, adds registration college selection.

**Tech Stack:** Spring Boot JPA, Vue 3, Element Plus, WebSocket STOMP

## Global Constraints

- ChatRoom entity: add `college` VARCHAR(100) nullable (null = 全校大厅)
- User entity already has `college` field
- Room visibility: teachers see all, students see 全校大厅 + their college
- ChatHub: left panel rooms+contacts, right panel messages
- No new npm dependencies

---

### Task 1: Fix Read/Unread Badge

**Files:**
- Modify: `frontend/src/views/chat/ChatPage.vue`
- Modify: `frontend/src/views/chat/ChatHub.vue`

- [ ] **Step 1: ChatPage — mark all as read on mount**

In `ChatPage.vue`, after `loadMessages(0)` in `onMounted`, add markAllAsRead call:

```javascript
// In onMounted, after await loadMessages(0):
if (chatType.value === 'private' && route.query.userId) {
  markAllAsRead(Number(route.query.userId)).catch(console.error)
}
```

Make sure `markAllAsRead` is imported from `@/api/chat`.

- [ ] **Step 2: ChatHub — refresh contacts on route change**

In `ChatHub.vue`, add a watch on route to refresh contacts:

```javascript
import { watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

watch(() => route.path, (newPath) => {
  if (newPath === '/chat') {
    loadContacts()
  }
})
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/chat/ChatPage.vue frontend/src/views/chat/ChatHub.vue
git commit -m "fix(chat): mark messages as read on open, refresh contacts on return"
```

---

### Task 2: Backend — College Chat Rooms + CRUD

**Files:**
- Modify: `backend/src/main/java/sen/yuhuang/backend/entity/ChatRoom.java`
- Modify: `backend/src/main/java/sen/yuhuang/backend/controller/ChatController.java`
- Modify: `backend/src/main/java/sen/yuhuang/backend/service/ChatMessageService.java`
- Modify: `backend/src/main/java/sen/yuhuang/backend/repository/ChatRoomRepository.java`

- [ ] **Step 1: Add `college` field to ChatRoom entity**

```java
@Column(name = "college", length = 100)
private String college;
```

- [ ] **Step 2: Add room query to ChatRoomRepository**

```java
@Query("SELECT r FROM ChatRoom r WHERE r.college IS NULL OR r.college = :college ORDER BY r.id ASC")
List<ChatRoom> findVisibleRooms(@Param("college") String college);

@Query("SELECT r FROM ChatRoom r ORDER BY r.id ASC")
List<ChatRoom> findAllRooms();
```

- [ ] **Step 3: Add room methods to ChatMessageService**

```java
public List<ChatRoom> getVisibleRooms(Long userId) {
    User user = userRepository.findById(userId).orElse(null);
    if (user == null) return List.of();
    // Teachers and admins see all rooms
    if (user.getRoleId() != null && user.getRoleId() >= 3) {
        return chatRoomRepository.findAllRooms();
    }
    // Students see 全校大厅 (null college) + their college
    return chatRoomRepository.findVisibleRooms(user.getCollege());
}

public ChatRoom createRoom(String name, String description, String college) {
    ChatRoom room = new ChatRoom();
    room.setName(name);
    room.setDescription(description);
    room.setCollege(college);
    room.setType("PUBLIC");
    room.setMemberCount(0);
    return chatRoomRepository.save(room);
}

public void deleteRoom(Long roomId) {
    chatRoomRepository.deleteById(roomId);
}
```

- [ ] **Step 4: Add room endpoints to ChatController**

```java
@GetMapping("/rooms")
public Result getRooms(@RequestHeader("X-Username") String username) {
    User user = userRepository.findByUsername(username).orElse(null);
    if (user == null) return Result.badRequest("用户不存在");
    return Result.ok(chatMessageService.getVisibleRooms(user.getId()));
}

@PostMapping("/rooms")
public Result createRoom(@RequestBody Map<String, String> request,
                         @RequestHeader("X-Username") String username) {
    User user = userRepository.findByUsername(username).orElse(null);
    if (user == null || user.getRoleId() < 3) return Result.badRequest("仅教师可创建聊天室");
    String name = request.get("name");
    String college = request.get("college");
    String description = request.get("description");
    if (name == null || name.trim().isEmpty()) return Result.badRequest("房间名不能为空");
    return Result.ok(chatMessageService.createRoom(name, description, college));
}

@DeleteMapping("/rooms/{roomId}")
public Result deleteRoom(@PathVariable Long roomId, @RequestHeader("X-Username") String username) {
    User user = userRepository.findByUsername(username).orElse(null);
    if (user == null || user.getRoleId() < 3) return Result.badRequest("仅教师可删除聊天室");
    chatMessageService.deleteRoom(roomId);
    return Result.ok("删除成功");
}
```

- [ ] **Step 5: Verify compilation**

```bash
cd /Users/a0000/Desktop/workplace/miaomiaoC/backend && ./mvnw compile -q
```

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/sen/yuhuang/backend/entity/ChatRoom.java \
        backend/src/main/java/sen/yuhuang/backend/repository/ChatRoomRepository.java \
        backend/src/main/java/sen/yuhuang/backend/service/ChatMessageService.java \
        backend/src/main/java/sen/yuhuang/backend/controller/ChatController.java
git commit -m "feat(backend): add college chat rooms with CRUD and visibility filtering"
```

---

### Task 3: Frontend — Chat Room API + Registration College

**Files:**
- Modify: `frontend/src/api/chat.js`
- Modify: `frontend/src/views/login/RegisterPage.vue`

- [ ] **Step 1: Add room API functions to chat.js**

```javascript
export function getRooms() {
    return request({ url: '/api/chat/rooms', method: 'get' })
}

export function createRoom(data) {
    return request({ url: '/api/chat/rooms', method: 'post', data })
}

export function deleteRoom(roomId) {
    return request({ url: `/api/chat/rooms/${roomId}`, method: 'delete' })
}
```

- [ ] **Step 2: Add college selection to RegisterPage.vue**

Add a college dropdown after the role selector. The dropdown should show a predefined list of colleges. Only required when role is "student".

```html
<el-form-item label="所属学院" prop="college">
  <el-select v-model="registerForm.college" placeholder="请选择学院" style="width:100%">
    <el-option label="计算机学院" value="计算机学院" />
    <el-option label="机械学院" value="机械学院" />
    <el-option label="电子信息学院" value="电子信息学院" />
    <el-option label="经济管理学院" value="经济管理学院" />
    <el-option label="外国语学院" value="外国语学院" />
    <el-option label="理学院" value="理学院" />
    <el-option label="人文社科学院" value="人文社科学院" />
    <el-option label="自动化学院" value="自动化学院" />
  </el-select>
</el-form-item>
```

Add `college` to the register form data and send it with the registration request.

- [ ] **Step 3: Update backend register to accept college**

In `UserController.java` register method, extract `college` from request and pass to service.
In `UserService.java` register method, set `user.setCollege(college)`.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/api/chat.js frontend/src/views/login/RegisterPage.vue \
        backend/src/main/java/sen/yuhuang/backend/controller/UserController.java \
        backend/src/main/java/sen/yuhuang/backend/service/UserService.java
git commit -m "feat: add college selection to registration + room API functions"
```

---

### Task 4: Frontend — ChatHub Redesign (Left-Right Split)

**Files:**
- Rewrite: `frontend/src/views/chat/ChatHub.vue`

This is the largest task. The ChatHub needs to be redesigned from a single message area + sidebar to a left-right split layout where:
- Left panel (280px): room list at top, contact list at bottom
- Right panel: selected room's messages + input bar
- Click a room → load its messages
- Click a contact → navigate to /chat/private

Key changes:
- Fetch rooms on mount via `getRooms()`
- Display room list with college tags
- Right panel shows selected room's messages (reusing the current message area)
- WebSocket subscribes to the selected room's `/topic/room/{roomId}`
- When switching rooms, unsubscribe from old and subscribe to new
- Teacher sees "+" button to create new rooms
- Preserve existing search and contact sidebar functionality

- [ ] **Step 1: Rewrite ChatHub.vue**

Read the current ChatHub.vue for the script logic (WebSocket, message handling, search, contacts). Keep all the existing script functions but restructure the template and add room management.

Key template structure:
```html
<div class="chat-hub">
  <div class="hub-header">...</div>
  <div class="hub-body">
    <!-- Left panel -->
    <div class="left-panel">
      <div class="room-section">
        <h3>聊天大厅</h3>
        <div v-for="room in rooms" :key="room.id" class="room-card" @click="selectRoom(room)">
          {{ room.name }}
          <span v-if="room.college" class="college-tag">{{ room.college }}</span>
          <span v-else class="college-tag all">全校</span>
        </div>
        <button v-if="isTeacher" @click="showCreateRoom = true">+ 创建聊天室</button>
      </div>
      <div class="contact-section">
        <h3>最近联系人</h3>
        <!-- existing contact list -->
      </div>
    </div>
    <!-- Right panel -->
    <div class="right-panel">
      <div class="room-header">{{ currentRoom?.name || '综合交流大厅' }}</div>
      <div class="message-list" ref="messageListRef">
        <!-- existing message rendering -->
      </div>
      <div class="input-bar">
        <!-- existing input bar -->
      </div>
    </div>
  </div>
</div>
```

Script changes:
- Add `rooms`, `currentRoom`, `showCreateRoom` refs
- Add `loadRooms()` function calling `getRooms()` API
- Add `selectRoom(room)` function: set currentRoom, load messages, resubscribe WebSocket
- Add `createNewRoom()` function for teacher
- Compute `isTeacher` from `userStore.getUserRoleId >= 3`
- In `onMounted`: loadRooms(), loadContacts(), connect WebSocket, select first room

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/chat/ChatHub.vue
git commit -m "feat(chat): redesign ChatHub with room list + left-right split layout"
```

---

### Task 5: Teacher — Room Management in Admin Panel

**Files:**
- Modify: `frontend/src/views/teacher/TeacherManagerPage.vue`

- [ ] **Step 1: Add "聊天室管理" menu item**

Add menu item index "10" with `ChatDotRound` icon and "聊天室管理" label.
Add handler to load rooms when selected.
Add content section with room table (name, college, actions: delete).
Add create room dialog with name + college dropdown.

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/teacher/TeacherManagerPage.vue
git commit -m "feat(teacher): add chat room management to admin panel"
```

---

### Task 6: Final Verification

- [ ] **Step 1: Full test flow**

1. Register new student with college "计算机学院"
2. Login → ChatHub → see 全校大厅 + 计算机学院 room
3. Teacher creates "机械学院" room → student does NOT see it
4. Student clicks 计算机学院 room → sends message → works
5. Student opens private chat → messages marked as read → returns to ChatHub → badges updated
6. Teacher sees all rooms + can create/delete from admin panel

- [ ] **Step 2: Final commit**

```bash
git add -A
git commit -m "feat: complete college chat rooms with read/unread fix"
```
