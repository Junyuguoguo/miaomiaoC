# Avatar Upload with Cropper Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Allow users to upload and crop custom avatar images instead of being limited to 18 presets.

**Architecture:** Backend file upload endpoint + frontend cropperjs crop dialog + existing avatar save flow. No schema changes — avatar remains a string path in the DB.

**Tech Stack:** Spring Boot MultipartFile, Vue 3, Element Plus, cropperjs

## Global Constraints

- Avatar stored as string path in `user.avatar` VARCHAR(255) — no schema change
- Backend saves to `uploads/avatars/`, returns path like `/uploads/avatars/user_5_xxx.jpg`
- Static resource mapping: `/uploads/**` → `file:uploads/`
- Max file size: 5MB, formats: jpg, png, webp
- Cropped output: 200x200px JPEG, quality 0.85
- Preserve existing preset avatar flow — upload is additional option
- Use existing `updateUserInfo` API to save avatar path

---

### Task 1: Backend — Upload Endpoint + Static Resource Config

**Files:**
- Modify: `backend/src/main/java/sen/yuhuang/backend/controller/UserController.java`
- Create: `backend/src/main/java/sen/yuhuang/backend/common/config/WebConfig.java`

**Interfaces:**
- Consumes: `MultipartFile file` from request, `X-Username` header (from `TokenAuthenticationFilter`)
- Produces: `Result<String>` with avatar URL path like `/uploads/avatars/user_5_1718xxx.jpg`

- [ ] **Step 1: Create WebConfig.java for static resource mapping**

Create file `backend/src/main/java/sen/yuhuang/backend/common/config/WebConfig.java`:

```java
package sen.yuhuang.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
```

- [ ] **Step 2: Add upload endpoint to UserController.java**

Open `backend/src/main/java/sen/yuhuang/backend/controller/UserController.java` and add these imports at the top:

```java
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
```

Then add the upload method before the closing `}` of the class (after the `getStatsData` method):

```java
@PostMapping("/uploadAvatar")
public Result uploadAvatar(@RequestParam("file") MultipartFile file,
                           @RequestHeader(value = "X-Username", required = false) String username) {
    if (file.isEmpty()) {
        return Result.badRequest("请选择要上传的文件");
    }

    String contentType = file.getContentType();
    Set<String> allowedTypes = Set.of("image/jpeg", "image/png", "image/webp");
    if (contentType == null || !allowedTypes.contains(contentType)) {
        return Result.badRequest("仅支持 JPG、PNG、WebP 格式");
    }

    if (file.getSize() > 5 * 1024 * 1024) {
        return Result.badRequest("文件大小不能超过 5MB");
    }

    // Determine file extension
    String ext = "jpg";
    if ("image/png".equals(contentType)) ext = "png";
    else if ("image/webp".equals(contentType)) ext = "webp";

    // Generate unique filename
    String filename = "user_" + (username != null ? username : "unknown") + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;

    // Save file
    String uploadDir = "uploads/avatars/";
    File dir = new File(uploadDir);
    if (!dir.exists()) {
        dir.mkdirs();
    }

    try {
        file.transferTo(new File(uploadDir + filename));
    } catch (IOException e) {
        return Result.error("文件上传失败: " + e.getMessage());
    }

    String avatarUrl = "/uploads/avatars/" + filename;
    return Result.ok(avatarUrl);
}
```

- [ ] **Step 3: Verify the backend compiles**

```bash
cd /Users/a0000/Desktop/workplace/miaomiaoC/backend && ./mvnw compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/sen/yuhuang/backend/controller/UserController.java \
        backend/src/main/java/sen/yuhuang/backend/common/config/WebConfig.java
git commit -m "feat(backend): add avatar upload endpoint and static resource mapping"
```

---

### Task 2: Frontend — Upload API + cropperjs Dependency

**Files:**
- Modify: `frontend/src/api/auth.js`
- Modify: `frontend/package.json`

**Interfaces:**
- Consumes: `File` or `Blob` object from cropper
- Produces: `Promise<AxiosResponse>` — response `data.data` contains the avatar URL path

- [ ] **Step 1: Add uploadAvatar function to auth.js**

Open `frontend/src/api/auth.js` and append at the end of the file:

```javascript
export function uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/api/auth/uploadAvatar',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}
```

- [ ] **Step 2: Install cropperjs**

```bash
cd /Users/a0000/Desktop/workplace/miaomiaoC/frontend && npm install cropperjs
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/api/auth.js frontend/package.json frontend/package-lock.json
git commit -m "feat(frontend): add uploadAvatar API and cropperjs dependency"
```

---

### Task 3: Frontend — Avatar Selector with Upload & Crop in HomePage.vue

**Files:**
- Modify: `frontend/src/views/exam/HomePage.vue`

**Interfaces:**
- Consumes: `uploadAvatar(file)` from `@/api/auth.js`
- Consumes: `Cropper` from `cropperjs`
- Produces: updates `selectedAvatar` ref with uploaded avatar URL — existing save flow handles the rest

- [ ] **Step 1: Add cropperjs CSS import**

Open `frontend/src/views/exam/HomePage.vue` and find the `<script setup>` section. Add this import at the top of the script (after the existing imports):

```javascript
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.css'
```

Also add the `uploadAvatar` import. Find the existing auth imports and add `uploadAvatar`:

```javascript
import { updateUserInfo, /* ...other existing imports... */ uploadAvatar } from '@/api/auth'
```

- [ ] **Step 2: Add cropper state variables**

After the existing avatar-related refs (around line 1101), add:

```javascript
// Avatar upload & crop
const cropDialogVisible = ref(false)
const cropImageRef = ref(null)
let cropperInstance = null
const cropUploading = ref(false)
```

- [ ] **Step 3: Add crop methods**

After the existing avatar methods (around line 1106), add:

```javascript
// Trigger file input click
const triggerAvatarUpload = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/jpeg,image/png,image/webp'
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (!file) return

    // Validate
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
      ElMessage.error('仅支持 JPG、PNG、WebP 格式')
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error('文件大小不能超过 5MB')
      return
    }

    // Show crop dialog
    cropDialogVisible.value = true
    nextTick(() => {
      if (cropImageRef.value) {
        cropImageRef.value.src = URL.createObjectURL(file)
        cropperInstance = new Cropper(cropImageRef.value, {
          aspectRatio: 1,
          viewMode: 1,
          dragMode: 'move',
          autoCropArea: 0.9,
          responsive: true,
          background: false,
          guides: false,
          cropBoxMovable: true,
          cropBoxResizable: true,
        })
      }
    })
  }
  input.click()
}

// Confirm crop and upload
const confirmCropUpload = async () => {
  if (!cropperInstance) return
  cropUploading.value = true
  try {
    const canvas = cropperInstance.getCroppedCanvas({
      width: 200,
      height: 200,
      imageSmoothingQuality: 'high'
    })
    const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/jpeg', 0.85))
    const file = new File([blob], 'avatar.jpg', { type: 'image/jpeg' })
    const res = await uploadAvatar(file)
    if (res && res.code === 200) {
      selectedAvatar.value = res.data
      ElMessage.success('头像上传成功')
      closeCropDialog()
    } else {
      ElMessage.error(res?.message || '上传失败')
    }
  } catch (err) {
    console.error('上传失败:', err)
    ElMessage.error('头像上传失败')
  } finally {
    cropUploading.value = false
  }
}

// Close crop dialog and destroy cropper
const closeCropDialog = () => {
  if (cropperInstance) {
    cropperInstance.destroy()
    cropperInstance = null
  }
  cropDialogVisible.value = false
}
```

- [ ] **Step 4: Add crop dialog template and upload button to avatar selector**

Find the avatar selector dialog (around line 280-308). After the `</div>` closing `avatar-selector-modal` and before the `<template #footer>`, add the upload section:

Replace the existing avatar selector dialog content with:

```html
        <!-- 头像选择弹窗 -->
        <el-dialog
            v-model="showAvatarSelector"
            title="选择头像"
            width="500px"
            :append-to-body="true"
            destroy-on-close
        >
          <div class="avatar-selector-modal">
            <div class="avatar-list-modal">
              <div
                  v-for="avatar in avatarList"
                  :key="avatar.id"
                  class="avatar-item-modal"
                  :class="{ active: selectedAvatar === avatar.url }"
                  @click="confirmSelectAvatar(avatar.url)"
              >
                <el-avatar :size="80">
                  <img :src="avatar.url" :alt="avatar.name" />
                </el-avatar>
                <div class="avatar-name-modal">{{ avatar.name }}</div>
              </div>
            </div>
            <el-divider />
            <div class="upload-avatar-section">
              <el-button type="primary" plain @click="triggerAvatarUpload">
                <el-icon><Upload /></el-icon>
                上传自定义头像
              </el-button>
            </div>
          </div>
          <template #footer>
            <el-button @click="showAvatarSelector = false">取消</el-button>
            <el-button type="primary" @click="showAvatarSelector = false">确定</el-button>
          </template>
        </el-dialog>

        <!-- 裁剪头像弹窗 -->
        <el-dialog
            v-model="cropDialogVisible"
            title="裁剪头像"
            width="460px"
            :append-to-body="true"
            :close-on-click-modal="false"
            @close="closeCropDialog"
        >
          <div class="crop-container">
            <img ref="cropImageRef" class="crop-image" alt="裁剪图片" />
          </div>
          <template #footer>
            <el-button @click="closeCropDialog">取消</el-button>
            <el-button type="primary" :loading="cropUploading" @click="confirmCropUpload">
              确认上传
            </el-button>
          </template>
        </el-dialog>
```

- [ ] **Step 5: Add styles for crop dialog and upload section**

Find the existing `<style scoped>` section in HomePage.vue and add these styles:

```css
/* Avatar upload & crop */
.upload-avatar-section {
  text-align: center;
  padding: 8px 0;
}

.crop-container {
  width: 100%;
  height: 320px;
  overflow: hidden;
}

.crop-image {
  max-width: 100%;
  max-height: 100%;
  display: block;
}
```

- [ ] **Step 6: Verify the Upload icon is available**

The `Upload` icon from Element Plus Icons is globally registered in `main.js`. If not, add it to the component's imports. Check with:

```bash
grep "Upload" /Users/a0000/Desktop/workplace/miaomiaoC/frontend/src/main.js
```

If not found, the icon is available globally via `import * as ElementPlusIconsVue from '@element-plus/icons-vue'` (confirmed in main.js line 9).

- [ ] **Step 7: Commit**

```bash
git add frontend/src/views/exam/HomePage.vue
git commit -m "feat(frontend): add avatar upload with cropperjs crop dialog"
```

---

### Task 4: Final Verification

- [ ] **Step 1: Full visual regression**

Start dev server:
```bash
cd /Users/a0000/Desktop/workplace/miaomiaoC/frontend && npm run dev
```

Start backend:
```bash
cd /Users/a0000/Desktop/workplace/miaomiaoC/backend && ./mvnw spring-boot:run
```

Test flow:
1. Login → go to homepage → click avatar → selector dialog opens
2. Verify: 18 preset avatars still show and selectable
3. Verify: "上传自定义头像" button below the grid
4. Click upload → file picker opens → select an image
5. Verify: crop dialog appears with the image, circular crop area
6. Adjust crop → click "确认上传"
7. Verify: avatar preview updates to the uploaded image
8. Save profile → reload page → avatar persists
9. Test: select invalid file type → error message
10. Test: select file > 5MB → error message

- [ ] **Step 2: Final commit**

```bash
git add -A
git commit -m "feat: complete avatar upload with cropperjs crop functionality"
```
