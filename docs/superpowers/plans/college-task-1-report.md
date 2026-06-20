# College Task 1 Report -- Unread Badge Fix

**Date:** 2026-06-19
**Commit:** `96565e7`
**Message:** `fix(chat): mark messages as read on open, refresh contacts on return`

---

## Summary

Fixed unread badge behavior so that:
1. Opening a private chat immediately marks all messages from that contact as read.
2. Returning to ChatHub refreshes the contact list, clearing stale unread badges.

## Changes

### ChatPage.vue (`frontend/src/views/chat/ChatPage.vue`)

- Added `markAllAsRead` to the import from `@/api/chat` (alongside existing `markAsRead`).
- In `onMounted`, after `await loadMessages(0)`, added a conditional call:
  ```js
  if (chatType.value === 'private' && route.query.userId) {
    markAllAsRead(Number(route.query.userId)).catch(console.error)
  }
  ```
  This clears the unread count for the contact on the server as soon as the user opens the chat.

### ChatHub.vue (`frontend/src/views/chat/ChatHub.vue`)

- Added `watch` to the Vue import.
- Added `useRoute` to the `vue-router` import (alongside existing `useRouter`).
- Created `const route = useRoute()`.
- Added a route path watcher before `onMounted`:
  ```js
  watch(() => route.path, (newPath) => {
    if (newPath === '/chat') {
      loadContacts()
    }
  })
  ```
  This ensures the contact list (and its unread badges) refreshes whenever the user navigates back to ChatHub from a private chat or any other route.

## How It Works

1. **Opening a chat:** When ChatPage mounts for a private chat, `markAllAsRead(userId)` fires in the background. The server clears the unread count for that sender. Errors are logged but do not block the UI.

2. **Returning to ChatHub:** The `watch` on `route.path` detects navigation to `/chat` and calls `loadContacts()`, which re-fetches the contact list from the server. Since the server already zeroed the unread count, the badge disappears.
