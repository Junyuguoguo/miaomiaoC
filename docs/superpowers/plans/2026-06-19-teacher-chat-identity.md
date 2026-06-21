# Teacher Identity in Chat Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Teachers can set their college (学院); in chat, teacher messages display with a gold border and college badge so students can identify teacher identity.

**Architecture:** Add `college` field to User entity, propagate through ChatMessageResponse to frontend. Chat UI renders teacher messages differently. No new files — modify existing endpoints and components.

**Tech Stack:** Spring Boot JPA, Vue 3, Element Plus

## Global Constraints

- `college` column: VARCHAR(100), nullable, default null — no migration needed (JPA ddl-auto: update)
- Only teachers (role_id=3) and admins (role_id=4) should set college; students ignore this field
- ChatMessageResponse adds `senderCollege` (String) and `senderRole` (Long)
- Teacher bubble style: gold border `2px solid #D4A843` + shadow `0 2px 12px rgba(212, 168, 67, 0.2)` + college badge label
- Student bubble style: unchanged (white/gradient blue)
- Use existing `fixAvatarUrl()` pattern for avatar URLs
- Preserve all existing functionality

---

### Task 1: Backend — Add `college` Field to User Entity + ChatMessageResponse

**Files:**
- Modify: `backend/src/main/java/sen/yuhuang/backend/entity/User.java`
- Modify: `backend/src/main/java/sen/yuhuang/backend/dto/ChatMessageResponse.java`
- Modify: `backend/src/main/java/sen/yuhuang/backend/service/ChatMessageService.java`
- Modify: `backend/src/main/java/sen/yuhuang/backend/controller/UserController.java`

**Interfaces:**
- User entity: adds `@Column(name = "college", length = 100) private String college;`
- ChatMessageResponse: adds `senderCollege` (String) and `senderRole` (Long)
- updateUserInfo endpoint: accepts `college` field in request body

- [ ] **Step 1: Add `college` field to User entity**

Open `backend/src/main/java/sen/yuhuang/backend/entity/User.java`. Find the `avatar` field declaration (around line 67-68) and add after it:

```java
@Column(name = "college", length = 100)
private String college;
```

Also add getter/setter (or ensure Lombok `@Data` generates them).

- [ ] **Step 2: Add fields to ChatMessageResponse**

Open `backend/src/main/java/sen/yuhuang/backend/dto/ChatMessageResponse.java`. Add after `senderAvatar`:

```java
private String senderCollege;
private Long senderRole;
```

- [ ] **Step 3: Update ChatMessageService to set new fields**

Open `backend/src/main/java/sen/yuhuang/backend/service/ChatMessageService.java`.

In `sendPrivateMessage` (around line 98), after setting `resp.setSenderAvatar(sender.getAvatar())`, add:

```java
resp.setSenderCollege(sender.getCollege());
resp.setSenderRole(sender.getRoleId());
```

In `sendRoomMessage` (around line 138), after setting `resp.setSenderAvatar(sender.getAvatar())`, add:

```java
resp.setSenderCollege(sender.getCollege());
resp.setSenderRole(sender.getRoleId());
```

In `getPrivateMessages` (the lambda that sets avatar), after `resp.setSenderAvatar(...)`, add:

```java
if (sender != null) {
    resp.setSenderCollege(sender.getCollege());
    resp.setSenderRole(sender.getRoleId());
}
```

In `getRoomMessages` (the batch-load section), extend the `avatarMap` pattern to also store college and role. Replace the existing batch logic with:

```java
// Batch-load sender info
Set<Long> senderIds = messagePage.getContent().stream()
        .map(ChatMessage::getSenderId).collect(Collectors.toSet());
Map<Long, User> senderMap = new HashMap<>();
if (!senderIds.isEmpty()) {
    userRepository.findByIds(new ArrayList<>(senderIds))
            .forEach(u -> senderMap.put(u.getId(), u));
}

return messagePage.map(msg -> {
    ChatMessageResponse resp = convertToResponse(msg);
    User sender = senderMap.get(msg.getSenderId());
    if (sender != null) {
        resp.setSenderAvatar(sender.getAvatar());
        resp.setSenderCollege(sender.getCollege());
        resp.setSenderRole(sender.getRoleId());
    }
    return resp;
});
```

- [ ] **Step 4: Update UserController to accept `college` in updateUserInfo**

Open `backend/src/main/java/sen/yuhuang/backend/controller/UserController.java`. In the `updateUserInfo` method, add:

```java
String college = request.get("college");
```

And pass it to the service. Also update `UserService.updateUserInfo()` to accept and save `college`.

- [ ] **Step 5: Update UserService.updateUserInfo**

Open `backend/src/main/java/sen/yuhuang/backend/service/UserService.java`. Find the `updateUserInfo` method and add `college` parameter. Update the repository query to include `u.college = :college`.

Also update `UserRepository.java` — find the `@Query` annotation for updateUserInfo and add `u.college = :college`.

- [ ] **Step 6: Verify compilation**

```bash
cd /Users/a0000/Desktop/workplace/miaomiaoC/backend && ./mvnw compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/sen/yuhuang/backend/entity/User.java \
        backend/src/main/java/sen/yuhuang/backend/dto/ChatMessageResponse.java \
        backend/src/main/java/sen/yuhuang/backend/service/ChatMessageService.java \
        backend/src/main/java/sen/yuhuang/backend/controller/UserController.java \
        backend/src/main/java/sen/yuhuang/backend/service/UserService.java \
        backend/src/main/java/sen/yuhuang/backend/repository/UserRepository.java
git commit -m "feat(backend): add college field for teachers, include in chat responses"
```

---

### Task 2: Frontend — Store + Teacher Personal Center + API

**Files:**
- Modify: `frontend/src/stores/user.js`
- Modify: `frontend/src/views/teacher/TeacherManagerPage.vue`
- Modify: `frontend/src/views/exam/HomePage.vue` (updateUserProfile call)

- [ ] **Step 1: Add `college` to user store**

Open `frontend/src/stores/user.js`.

In the default `userInfo` object (around line 80), add: `college: ''`

In the `getUserCollege` getter area (around line 120), add:
```javascript
getUserCollege: (state) => state.userInfo?.college || '',
```

In `setUser` (around line 145), add: `college: userData.college || ''`

In `updateUserProfile` (around line 165), add `college` parameter and include it in `updateData`.

- [ ] **Step 2: Add college input to teacher personal center**

Open `frontend/src/views/teacher/TeacherManagerPage.vue`.

In the profile form template, add a college field before the save button:

```html
<el-form-item label="所属学院">
  <el-input v-model="profileForm.college" placeholder="请输入所属学院" />
</el-form-item>
```

In `loadTeacherProfile()`, add: `college: info.college || ''`

In `saveTeacherProfile()`, add `college: profileForm.value.college` to both the API call and the store update.

In `profileForm` ref, add `college: ''` to the initial value.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/stores/user.js \
        frontend/src/views/teacher/TeacherManagerPage.vue
git commit -m "feat(frontend): add college field to teacher personal center and user store"
```

---

### Task 3: Frontend — Chat UI with Teacher Gold Border + College Badge

**Files:**
- Modify: `frontend/src/views/chat/ChatHub.vue`
- Modify: `frontend/src/views/chat/ChatPage.vue`

- [ ] **Step 1: Update ChatHub.vue to show teacher identity**

Open `frontend/src/views/chat/ChatHub.vue`.

In the message row template, update the bubble section to show college badge for teachers:

```html
<div class="msg-body">
  <span v-if="msg.senderId !== currentUserId" class="sender-name">
    {{ msg.senderName || '匿名用户' }}
    <span v-if="msg.senderRole === 3 || msg.senderRole === 4" class="teacher-badge">
      {{ msg.senderCollege || '教师' }}
    </span>
  </span>
  <div class="bubble" :class="[
    msg.senderId === currentUserId ? 'bubble-self' : 'bubble-peer',
    (msg.senderRole === 3 || msg.senderRole === 4) ? 'bubble-teacher' : ''
  ]">
    {{ msg.content }}
  </div>
</div>
```

Add CSS styles:

```css
.teacher-badge {
  display: inline-block;
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 8px;
  background: linear-gradient(135deg, #D4A843, #B8922E);
  color: #fff;
  margin-left: 6px;
  font-weight: 600;
  vertical-align: middle;
}

.bubble-teacher {
  border: 2px solid #D4A843 !important;
  box-shadow: 0 2px 12px rgba(212, 168, 67, 0.2) !important;
}
```

- [ ] **Step 2: Update ChatPage.vue to show teacher identity**

Open `frontend/src/views/chat/ChatPage.vue`.

In the message item template, update the bubble section:

```html
<div class="message-body">
  <div class="bubble" :class="[
    msg.senderId === currentUserId ? 'bubble-self' : 'bubble-peer',
    (msg.senderRole === 3 || msg.senderRole === 4) ? 'bubble-teacher' : ''
  ]">
    <span v-if="(msg.senderRole === 3 || msg.senderRole === 4) && msg.senderCollege" class="teacher-tag">
      {{ msg.senderCollege }}
    </span>
    {{ msg.content }}
  </div>
</div>
```

Add CSS styles:

```css
.teacher-tag {
  display: inline-block;
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 6px;
  background: rgba(212, 168, 67, 0.15);
  color: #B8922E;
  margin-right: 6px;
  margin-bottom: 4px;
  font-weight: 600;
}

.bubble-teacher {
  border: 2px solid #D4A843;
  box-shadow: 0 2px 12px rgba(212, 168, 67, 0.2);
}
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/chat/ChatHub.vue \
        frontend/src/views/chat/ChatPage.vue
git commit -m "feat(chat): teacher messages with gold border and college badge"
```

---

### Task 4: Final Verification

- [ ] **Step 1: Full test flow**

1. Login as teacher → 个人中心 → 设置所属学院（如"计算机学院"）→ 保存
2. 进入在线交流 → 发送消息 → 验证消息有金色边框 + 学院标签
3. Login as student → 进入在线交流 → 验证教师消息显示金色边框 + "计算机学院"标签
4. 学生发消息 → 验证学生消息样式不变（白色/蓝色气泡，无边框）
5. 验证教师侧边栏显示真实头像和用户名

- [ ] **Step 2: Final commit**

```bash
git add -A
git commit -m "feat: complete teacher identity display in chat with college badge"
```
