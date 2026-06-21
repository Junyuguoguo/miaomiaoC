# Task 3 Report: Avatar Upload with CropperJS Crop Dialog

**Status:** Completed
**File:** `/Users/a0000/Desktop/workplace/miaomiaoC/frontend/src/views/exam/HomePage.vue`

## Changes Made

### 1. Imports (lines 1047-1071)
- Added `nextTick` to Vue imports (line 1047)
- Added `uploadAvatar` to auth API imports (line 1057)
- Added `Cropper` from `cropperjs` (line 1070)
- Added `import 'cropperjs/dist/cropper.css'` (line 1071)

### 2. Crop State Variables (lines 1161-1165)
- `cropDialogVisible` — controls crop dialog visibility
- `cropImageRef` — ref to the `<img>` element in crop dialog
- `cropperInstance` — stores the Cropper.js instance (non-reactive `let`)
- `cropUploading` — loading state during upload

### 3. Crop Methods (lines 1167-1243)
- `triggerAvatarUpload()` — creates file input, validates format/size, opens crop dialog with Cropper instance
- `confirmCropUpload()` — gets 200x200 cropped canvas, converts to JPEG blob, uploads via `uploadAvatar()`, updates `selectedAvatar`
- `closeCropDialog()` — destroys Cropper instance, closes dialog

### 4. Template Changes (lines 280-335)
- Updated avatar selector dialog: added `<el-divider />` and "上传自定义头像" button with Upload icon below preset grid
- Added crop dialog (`cropDialogVisible`): shows image for cropping, with cancel/confirm-upload buttons and loading state

### 5. CSS Styles (lines 4424-4438)
- `.upload-avatar-section` — centered padding for upload button
- `.crop-container` — 320px height with hidden overflow
- `.crop-image` — max-width/height 100%, block display

## Key Design Decisions
- Upload icon (`Upload`) is globally registered in main.js — no import needed in component
- Cropper initialized in `nextTick()` after dialog renders and image src is set
- Crop output: 200x200px JPEG, quality 0.85 (matches backend constraints)
- File validation: client-side format check (JPEG/PNG/WebP) and size limit (5MB) before opening crop dialog
- `cropperInstance` uses `let` (not `ref`) intentionally — no need for reactivity
- `append-to-body` on both dialogs prevents z-index stacking issues

## Commit
```
feat(frontend): add avatar upload with cropperjs crop dialog
```
SHA: `fc815e5`
