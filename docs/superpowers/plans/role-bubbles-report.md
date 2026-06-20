# Role Bubbles Report

## Commit
`15e260f` on branch `1` -- `feat(chat): distinct bubble styles for student/VIP/teacher/admin roles`

## Files modified
- `/Users/a0000/Desktop/workplace/miaomiaoC/frontend/src/views/chat/ChatHub.vue`
- `/Users/a0000/Desktop/workplace/miaomiaoC/frontend/src/views/chat/ChatPage.vue`

## What changed

### Role badge system (senderRole codes)
| Code | Role      | Badge class      | Badge label |
|------|-----------|------------------|-------------|
| 1    | 普通学生  | (none)           | --          |
| 2    | VIP学生   | `vip-badge`      | VIP         |
| 3    | 教师      | `teacher-badge`  | 教师        |
| 4    | 管理员    | `admin-badge`    | 管理员      |

### Template changes

**ChatHub.vue (line ~155):** Badge placed inside `.sender-name` span after the user name. Bubble class now driven by `getBubbleClass(msg)`.

**ChatPage.vue (line ~38):** Badge placed inside the `.bubble` div before content text. Bubble class now driven by `getBubbleClass(msg)`.

### Script changes (both files)

Added `getBubbleClass(msg)` helper function that returns a class string combining:
- `bubble-self` or `bubble-peer` (based on sender vs current user)
- `bubble-vip` (role 2), `bubble-teacher` (role 3), or `bubble-admin` (role 4)

### CSS changes (both files)

**Removed:** Old `.teacher-badge` / `.teacher-tag` / `.bubble-teacher` gold-border styles.

**Added:** Unified role badge base `.role-badge` plus gradient color variants per role. Role-specific bubble styling for both peer (tinted background + colored border) and self (solid gradient background) states, including `::before`/`::after` arrow pseudo-element color overrides.

| Role      | Peer bubble bg           | Self bubble bg                    |
|-----------|--------------------------|-----------------------------------|
| VIP       | warm amber (`#fffbeb`)   | gold gradient (`#f59e0b-#d97706`)|
| Teacher   | soft green (`#ecfdf5`)   | emerald gradient (`#10b981-#059669`)|
| Admin     | light purple (`#f5f3ff`) | violet gradient (`#8b5cf6-#7c3aed`)|

## Verification
- Badge shows for roles 2/3/4 only, not for role 1 (普通学生)
- Badge is hidden on self messages in ChatHub (inside `.sender-name` which has `display:none` for self)
- Badge is visible for all messages in ChatPage (inside bubble)
- Base `.bubble-self` and `.bubble-peer` styles preserved for default look
