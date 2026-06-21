# Group Number + Pin/Mute + Student College + Dynamic UI Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add group numbers, pin/mute room settings, student college requirement, join-by-group-number, and dynamic UI animations across the platform.

**Architecture:** Backend adds group_number to ChatRoom + user_room_setting table + join-by-number API. Frontend adds pin/mute UI, join dialog, college requirement, and CSS animations.

**Tech Stack:** Spring Boot JPA, Vue 3, Element Plus, CSS @keyframes

## Global Constraints

- ChatRoom: add `group_number` VARCHAR(6) UNIQUE, auto-generated 6-digit number
- New table `user_room_setting`: user_id, room_id, is_pinned (boolean), is_muted (boolean)
- Student personal center: 报考院校 field required
- ChatHub: pinned rooms sorted first, muted rooms suppress notifications
- Dynamic UI: page transitions, card hover effects, stagger animations on all lists
- No new npm dependencies

---

### Task 1: Backend — Group Number + User Room Settings

**Files:**
- Modify: `backend/src/main/java/sen/yuhuang/backend/entity/ChatRoom.java`
- Create: `backend/src/main/java/sen/yuhuang/backend/entity/UserRoomSetting.java`
- Create: `backend/src/main/java/sen/yuhuang/backend/repository/UserRoomSettingRepository.java`
- Modify: `backend/src/main/java/sen/yuhuang/backend/service/ChatMessageService.java`
- Modify: `backend/src/main/java/sen/yuhuang/backend/controller/ChatController.java`

- [ ] **Step 1: Add group_number to ChatRoom entity**

```java
@Column(name = "group_number", length = 6, unique = true)
private String groupNumber;
```

- [ ] **Step 2: Create UserRoomSetting entity**

```java
package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_room_setting", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "room_id"}))
public class UserRoomSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "is_pinned")
    private Boolean isPinned = false;

    @Column(name = "is_muted")
    private Boolean isMuted = false;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() { this.createTime = LocalDateTime.now(); }
}
```

- [ ] **Step 3: Create UserRoomSettingRepository**

```java
package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sen.yuhuang.backend.entity.UserRoomSetting;
import java.util.List;
import java.util.Optional;

public interface UserRoomSettingRepository extends JpaRepository<UserRoomSetting, Long> {
    Optional<UserRoomSetting> findByUserIdAndRoomId(Long userId, Long roomId);
    List<UserRoomSetting> findByUserId(Long userId);
}
```

- [ ] **Step 4: Add methods to ChatMessageService**

```java
// Generate unique 6-digit group number
private String generateGroupNumber() {
    String number;
    do {
        number = String.format("%06d", (int)(Math.random() * 900000) + 100000);
    } while (chatRoomRepository.existsByGroupNumber(number));
    return number;
}

// Update createRoom to set group number
public ChatRoom createRoom(String name, String description, String college, Long creatorId) {
    ChatRoom room = new ChatRoom();
    room.setRoomName(name);
    room.setDescription(description);
    room.setCollege(college);
    room.setCreatorId(creatorId);
    room.setRoomType("PUBLIC");
    room.setCurrentMembers(0);
    room.setIsActive(true);
    room.setGroupNumber(generateGroupNumber());
    return chatRoomRepository.save(room);
}

// Find room by group number
public ChatRoom findByGroupNumber(String groupNumber) {
    return chatRoomRepository.findByGroupNumber(groupNumber).orElse(null);
}

// Toggle pin/mute
public UserRoomSetting togglePin(Long userId, Long roomId) {
    UserRoomSetting setting = userRoomSettingRepository.findByUserIdAndRoomId(userId, roomId)
            .orElse(new UserRoomSetting());
    setting.setUserId(userId);
    setting.setRoomId(roomId);
    setting.setIsPinned(!Boolean.TRUE.equals(setting.getIsPinned()));
    return userRoomSettingRepository.save(setting);
}

public UserRoomSetting toggleMute(Long userId, Long roomId) {
    UserRoomSetting setting = userRoomSettingRepository.findByUserIdAndRoomId(userId, roomId)
            .orElse(new UserRoomSetting());
    setting.setUserId(userId);
    setting.setRoomId(roomId);
    setting.setIsMuted(!Boolean.TRUE.equals(setting.getIsMuted()));
    return userRoomSettingRepository.save(setting);
}

public List<UserRoomSetting> getUserRoomSettings(Long userId) {
    return userRoomSettingRepository.findByUserId(userId);
}
```

- [ ] **Step 5: Add ChatRoomRepository query**

```java
Optional<ChatRoom> findByGroupNumber(String groupNumber);
boolean existsByGroupNumber(String groupNumber);
```

- [ ] **Step 6: Add endpoints to ChatController**

```java
// Join by group number
@GetMapping("/rooms/join/{groupNumber}")
public Result joinByGroupNumber(@PathVariable String groupNumber, @RequestHeader("X-Username") String username) {
    User user = userRepository.findByUsername(username).orElse(null);
    if (user == null) return Result.badRequest("用户不存在");
    ChatRoom room = chatMessageService.findByGroupNumber(groupNumber);
    if (room == null) return Result.badRequest("群号不存在");
    return Result.ok(room);
}

// Toggle pin
@PostMapping("/rooms/{roomId}/pin")
public Result togglePin(@PathVariable Long roomId, @RequestHeader("X-Username") String username) {
    User user = userRepository.findByUsername(username).orElse(null);
    if (user == null) return Result.badRequest("用户不存在");
    return Result.ok(chatMessageService.togglePin(user.getId(), roomId));
}

// Toggle mute
@PostMapping("/rooms/{roomId}/mute")
public Result toggleMute(@PathVariable Long roomId, @RequestHeader("X-Username") String username) {
    User user = userRepository.findByUsername(username).orElse(null);
    if (user == null) return Result.badRequest("用户不存在");
    return Result.ok(chatMessageService.toggleMute(user.getId(), roomId));
}

// Get user room settings
@GetMapping("/rooms/settings")
public Result getRoomSettings(@RequestHeader("X-Username") String username) {
    User user = userRepository.findByUsername(username).orElse(null);
    if (user == null) return Result.badRequest("用户不存在");
    return Result.ok(chatMessageService.getUserRoomSettings(user.getId()));
}
```

- [ ] **Step 7: Verify compilation and commit**

```bash
cd /Users/a0000/Desktop/workplace/miaomiaoC/backend && ./mvnw compile -q
git add backend/src/main/java/sen/yuhuang/backend/ && git commit -m "feat(backend): add group numbers, pin/mute room settings, join-by-number API"
```

---

### Task 2: Frontend — Room APIs + Student College Requirement

**Files:**
- Modify: `frontend/src/api/chat.js`
- Modify: `frontend/src/views/exam/HomePage.vue`

- [ ] **Step 1: Add new API functions to chat.js**

```javascript
export function joinByGroupNumber(groupNumber) {
    return request({ url: `/api/chat/rooms/join/${groupNumber}`, method: 'get' })
}

export function togglePinRoom(roomId) {
    return request({ url: `/api/chat/rooms/${roomId}/pin`, method: 'post' })
}

export function toggleMuteRoom(roomId) {
    return request({ url: `/api/chat/rooms/${roomId}/mute`, method: 'post' })
}

export function getRoomSettings() {
    return request({ url: '/api/chat/rooms/settings', method: 'get' })
}
```

- [ ] **Step 2: Make 报考院校 required in HomePage.vue**

In the edit profile form, change the school field to use a college dropdown (same as registration). Add validation rule: required.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/api/chat.js frontend/src/views/exam/HomePage.vue
git commit -m "feat(frontend): add room setting APIs + require college in student profile"
```

---

### Task 3: Frontend — ChatHub with Group Number, Pin/Mute, Join, Dynamic UI

**Files:**
- Modify: `frontend/src/views/chat/ChatHub.vue`

This is the largest task. Key changes to ChatHub:

### 3a: Room list — pin/mute/group number
- Load user room settings on mount
- Sort rooms: pinned first, then by id
- Show pin icon on pinned rooms
- Right-click or button on room → context menu: 置顶/取消置顶, 免打扰/取消免打扰
- Show group number in room header

### 3b: Join by group number
- Add "加入群聊" button above room list
- Click → dialog with group number input
- Submit → call joinByGroupNumber API → add room to list

### 3c: Dynamic UI animations
- Room cards: stagger fadeInUp on load (each delay 40ms)
- Room cards: hover lift effect (translateY(-2px) + shadow)
- Message area: smooth scroll
- Page header: glassmorphism
- Active room: left border accent + subtle background

### 3d: Room header improvements
- Show group number: "群号: 10001"
- Copy group number button
- Pin/mute toggle icons

- [ ] **Step 1: Implement all ChatHub changes**

Read the current ChatHub.vue, then add:
1. Import new API functions
2. Add `roomSettings`, `showJoinDialog`, `joinGroupNumber` refs
3. Add `loadRoomSettings()` function
4. Add `handleJoinGroup()` function
5. Add `handleTogglePin(roomId)` and `handleToggleMute(roomId)` functions
6. Sort rooms with pinned first
7. Add join dialog template
8. Add pin/mute context menu on room cards
9. Add group number display in room header
10. Add all CSS animations (stagger, hover, transitions)

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/chat/ChatHub.vue
git commit -m "feat(chat): add group number, pin/mute, join-by-number, dynamic UI to ChatHub"
```

---

### Task 4: Teacher — Show Group Number in Room Management

**Files:**
- Modify: `frontend/src/views/teacher/TeacherManagerPage.vue`

- [ ] **Step 1: Add group_number column to room management table**

Add table column showing room group number with copy button.

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/teacher/TeacherManagerPage.vue
git commit -m "feat(teacher): show group number in chat room management"
```

---

### Task 5: Global Dynamic UI Polish

**Files:**
- Modify: `frontend/src/assets/main.css`
- Modify: `frontend/src/views/chat/ChatPage.vue`

- [ ] **Step 1: Add global transition styles to main.css**

```css
/* Page transitions */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Card hover lift */
.card-hover {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.card-hover:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(16, 32, 51, 0.12);
}
```

- [ ] **Step 2: Add dynamic effects to ChatPage.vue**

- Input focus: border color transition + shadow expand
- New message: slide-in animation (already exists, verify it works)
- Extension panel: slide-up animation (already exists)

- [ ] **Step 3: Commit**

```bash
git add frontend/src/assets/main.css frontend/src/views/chat/ChatPage.vue
git commit -m "feat(ui): add global dynamic transitions and hover effects"
```

---

### Task 6: Final Verification

- [ ] **Step 1: Full test flow**

1. Teacher creates room → group number auto-generated (e.g., 100001)
2. Teacher sees group number in admin panel, can copy
3. Student joins by entering group number in ChatHub
4. Student pins a room → it moves to top of list
5. Student mutes a room → no notification badge
6. Student edits profile → must select 报考学院
7. All pages have smooth animations and hover effects

- [ ] **Step 2: Final commit**

```bash
git add -A && git commit -m "feat: complete group numbers, pin/mute, join-by-number, dynamic UI"
```
