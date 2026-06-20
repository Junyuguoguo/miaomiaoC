# Avatar Upload with Cropper Design Spec

> **Scope:** miaomiaoC — add custom avatar upload with image cropping for both students and teachers.

**Goal:** Allow users to upload and crop their own avatar images instead of being limited to 18 preset avatars.

**Architecture:** Add a file upload endpoint to the existing `UserController`, a crop-upload UI component in the existing avatar selector dialog in `HomePage.vue`, and use `cropperjs` for client-side image cropping. The existing preset avatar flow is preserved.

**Tech Stack:** Spring Boot MultipartFile, Vue 3, Element Plus, cropperjs

## Global Constraints

- Avatar stored as a string path in `user.avatar` column (VARCHAR 255) — no schema change needed
- Backend saves uploaded files to a configurable upload directory (default: `uploads/avatars/`)
- Frontend serves uploaded files via static resource mapping (`/uploads/**`)
- Max file size: 5MB, accepted formats: jpg, png, webp
- Cropped output: circular, 200x200px JPEG, quality 0.85
- Preserve existing preset avatar selection — upload is an additional option
- Use existing `updateUserInfo` API to save the new avatar path after upload

---

## Part 1: Backend — File Upload Endpoint

### File
- Modify: `backend/src/main/java/sen/yuhuang/backend/controller/UserController.java`

### Endpoint
- `POST /api/auth/uploadAvatar`
- Auth: requires valid token (existing `TokenAuthenticationFilter`)
- Request: `MultipartFile file`
- Response: `Result<String>` with the avatar URL path

### Validation
- File not empty
- Content type: `image/jpeg`, `image/png`, `image/webp`
- Max size: 5MB

### Storage
- Save to `uploads/avatars/` directory (relative to working directory)
- Filename: `user_{userId}_{timestamp}.{ext}`
- Create directory if not exists
- Return path: `/uploads/avatars/user_{userId}_{timestamp}.{ext}`

### Static Resource Config
- Modify: `backend/src/main/java/sen/yuhuang/backend/common/config/WebConfig.java` (or create if needed)
- Add resource handler: `/uploads/**` → `file:uploads/`

---

## Part 2: Frontend — Upload API

### File
- Modify: `frontend/src/api/auth.js`

### Function
- `uploadAvatar(file)` — sends `FormData` with `file` field to `POST /api/auth/uploadAvatar`
- Returns the server response (avatar URL path)

---

## Part 3: Frontend — Avatar Selector with Upload & Crop

### File
- Modify: `frontend/src/views/exam/HomePage.vue`

### Changes to Avatar Selector Dialog
- Keep existing 18 preset avatars grid
- Add divider + "上传自定义头像" button below the grid
- Button triggers hidden `<input type="file" accept="image/jpeg,image/png,image/webp">`
- On file select: show crop dialog

### Crop Dialog
- New dialog with `cropperjs` instance
- Circular crop area, aspect ratio 1:1
- Preview of cropped result
- "确认" button → upload cropped image → update avatar
- "取消" button → close

### Crop & Upload Flow
1. User selects file → create object URL → show in cropper
2. User adjusts crop → click "确认"
3. `cropper.getCroppedCanvas({ width: 200, height: 200 })` → `toBlob('image/jpeg', 0.85)`
4. Upload blob via `uploadAvatar(blob)` API
5. On success: update `selectedAvatar` with returned URL
6. User saves profile → `updateUserInfo` sends new avatar path to backend

### Integration with Existing Flow
- `selectedAvatar` can now hold either a preset path (`/avatars/1.jpg`) or an uploaded path (`/uploads/avatars/user_5_xxx.jpg`)
- No change to the profile save logic — it already sends `avatar` field to `updateUserInfo`

---

## Part 4: Dependency

### File
- Modify: `frontend/package.json`

### Add
- `cropperjs` (latest, ~1.6.x) — client-side image cropping
- `cropperjs/dist/cropper.css` — imported in the component

---

## Testing

- Visual verification in browser dev server
- Test: click avatar → selector dialog shows preset grid + upload button
- Test: click upload → file picker opens, select image → crop dialog appears
- Test: adjust crop → click confirm → image uploads, avatar preview updates
- Test: save profile → avatar persists on page reload
- Test: upload invalid file type → error message shown
- Test: upload file > 5MB → error message shown
- Test: preset avatars still work as before
