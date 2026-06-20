# Task 2 Report — Frontend Upload API + cropperjs Dependency

**Date:** 2026-06-19
**Status:** COMPLETE

## What was done

### 1. Added `uploadAvatar` function to `frontend/src/api/auth.js`

- Appended `uploadAvatar(file)` export at the end of `auth.js`
- Uses `FormData` with `multipart/form-data` content type
- Posts to `/api/auth/uploadAvatar`
- Compatible with `File` or `Blob` objects from cropperjs

### 2. Installed `cropperjs` dependency

- Ran `npm install cropperjs` in `frontend/`
- Added 12 packages (cropperjs + transitive deps)
- Updated `package.json` and `package-lock.json`

### 3. Committed changes

- Commit: `fca702c` on branch `1`
- Message: `feat(frontend): add uploadAvatar API and cropperjs dependency`
- Files: `frontend/src/api/auth.js`, `frontend/package.json`, `frontend/package-lock.json`

## Files modified

- `/Users/a0000/Desktop/workplace/miaomiaoC/frontend/src/api/auth.js` — added `uploadAvatar` function
- `/Users/a0000/Desktop/workplace/miaomiaoC/frontend/package.json` — added `cropperjs` dependency
- `/Users/a0000/Desktop/workplace/miaomiaoC/frontend/package-lock.json` — lockfile updated

## Next step

Task 3: Add the avatar upload UI with cropperjs crop dialog to `HomePage.vue`.
