# 邀请码注册链接 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让管理员可以生成邀请码的注册链接，分享给教师直接注册

**Architecture:** 纯前端改动。注册页读取 URL 参数自动填入邀请码，管理员页在邀请码表格新增「复制链接」按钮

**Tech Stack:** Vue 3, Element Plus, window.location API

## Global Constraints

- 链接域名自动取 `window.location.origin`
- 邀请码文本和复制链接按钮同时保留
- 不改后端

---

### Task 1: 注册页自动读取 URL 邀请码参数

**Files:**
- Modify: `frontend/src/views/login/RegisterPage.vue:168-171`

**Interfaces:**
- Produces: URL 格式 `域名/register?inviteCode=XXX`，注册页自动填入 `registerForm.inviteCode`

- [ ] **Step 1: 修改 onMounted 读取 URL 参数**

在 `RegisterPage.vue` 的 `onMounted` 中，读取 `?inviteCode=XXX` 并自动填入表单：

```js
onMounted(async () => {
  title.value = import.meta.env.VITE_APP_TITLE
  version.value = import.meta.env.VITE_APP_VERSION
  
  // 读取 URL 中的邀请码参数，自动填入
  const urlParams = new URLSearchParams(window.location.search)
  const inviteCodeFromUrl = urlParams.get('inviteCode')
  if (inviteCodeFromUrl) {
    registerForm.inviteCode = inviteCodeFromUrl
  }
})
```

- [ ] **Step 2: 验证构建**

Run: `cd frontend && npm run build`
Expected: ✓ built in Xs

---

### Task 2: 管理员页邀请码表格新增「复制链接」按钮

**Files:**
- Modify: `frontend/src/views/admin/AdminPage.vue` (邀请码表格的邀请码列)

**Interfaces:**
- Consumes: `row.code` — 邀请码字符串
- Produces: 复制到剪贴板的链接格式 `origin/register?inviteCode=XXX`

- [ ] **Step 1: 修改邀请码列模板，增加复制码和复制链接按钮**

```vue
<el-table-column label="邀请码" prop="code" min-width="220">
  <template #default="{ row }">
    <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap">
      <code class="code-text">{{ row.code }}</code>
      <el-button size="small" link type="primary" @click="copyCode(row.code)">复制码</el-button>
      <el-button size="small" link type="success" @click="copyInviteLink(row.code)">复制链接</el-button>
    </div>
  </template>
</el-table-column>
```

- [ ] **Step 2: 添加 copyInviteLink 函数**

在 `copyCode` 函数后面添加：

```js
const copyInviteLink = async (code) => {
  const link = window.location.origin + '/register?inviteCode=' + code
  try {
    await navigator.clipboard.writeText(link)
    ElMessage.success('注册链接已复制到剪贴板')
  } catch {
    const textarea = document.createElement('textarea')
    textarea.value = link
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('注册链接已复制到剪贴板')
  }
}
```

- [ ] **Step 3: 验证构建**

Run: `cd frontend && npm run build`
Expected: ✓ built in Xs

- [ ] **Step 4: 提交**

```bash
git add frontend/src/views/login/RegisterPage.vue frontend/src/views/admin/AdminPage.vue
git commit -m "feat: invite code registration link - auto-fill from URL + copy link button"
```
