# 学院初始化接口实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 创建一个初始化接口，调用时将13个学院插入到数据库中，使学院管理页面能够显示所有学院。

**Architecture:** 在现有AdminController中添加POST /api/admin/colleges/init接口，使用INSERT IGNORE避免重复插入，前端添加触发按钮。

**Tech Stack:** Spring Boot, JPA, Vue 3, Element Plus

## Global Constraints

- 使用现有的权限校验模式（getAdminUser方法）
- 遵循现有的Result响应格式
- 使用现有的InterviewCollegeRepository
- 前端使用现有的request工具

---

### Task 1: 后端 - 添加初始化接口

**Files:**
- Modify: `backend/src/main/java/sen/yuhuang/backend/controller/admin/AdminController.java:605-607`

**Interfaces:**
- Consumes: `getAdminUser(request)` - 现有的权限校验方法
- Produces: `POST /api/admin/colleges/init` - 初始化接口

- [ ] **Step 1: 在AdminController中添加初始化接口**

在第605行（deleteCollege方法之后）添加以下代码：

```java
/**
 * 初始化学院数据（将13个学院插入数据库）
 */
@PostMapping("/colleges/init")
public Result initColleges(HttpServletRequest request) {
    User admin = getAdminUser(request);
    if (admin == null) {
        return Result.forbidden("无管理员权限");
    }

    List<String> collegeNames = Arrays.asList(
        "信息与通信工程学院", "计算机学院", "自动化学院",
        "机械工程学院", "经济管理学院", "信息管理学院",
        "马克思主义学院", "外国语学院", "理学院",
        "公共管理与传媒学院", "国际交流学院",
        "电子信息学院", "人文社科学院"
    );

    int inserted = 0;
    int existed = 0;

    for (String name : collegeNames) {
        InterviewCollege existing = interviewCollegeRepository.findByName(name);
        if (existing == null) {
            InterviewCollege college = new InterviewCollege();
            college.setName(name);
            college.setSortOrder(inserted + 1);
            college.setEnabled(1);
            interviewCollegeRepository.save(college);
            inserted++;
        } else {
            existed++;
        }
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("inserted", inserted);
    result.put("existed", existed);
    result.put("total", collegeNames.size());
    return Result.ok(result);
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/sen/yuhuang/backend/controller/admin/AdminController.java
git commit -m "feat: add college init API endpoint"
```

---

### Task 2: 前端 - 添加API调用

**Files:**
- Modify: `frontend/src/api/admin.js:61`

**Interfaces:**
- Consumes: `request` - 现有的HTTP请求工具
- Produces: `initColleges()` - 前端API函数

- [ ] **Step 1: 在admin.js中添加initColleges函数**

在第61行（deleteCollege函数之后）添加以下代码：

```javascript
export function initColleges() {
    return request({ url: '/api/admin/colleges/init', method: 'post' })
}
```

- [ ] **Step 2: 验证语法正确**

Run: `cd frontend && npm run lint`
Expected: No errors

- [ ] **Step 3: Commit**

```bash
git add frontend/src/api/admin.js
git commit -m "feat: add initColleges API function"
```

---

### Task 3: 前端 - 添加初始化按钮

**Files:**
- Modify: `frontend/src/views/admin/AdminPage.vue:311-340`

**Interfaces:**
- Consumes: `initColleges()` - 来自Task 2的API函数
- Produces: 初始化按钮和成功提示

- [ ] **Step 1: 导入initColleges函数**

在AdminPage.vue的script部分，找到import语句，添加initColleges导入：

```javascript
import { 
  // ... 现有导入 ...
  listColleges, createCollege, updateCollege, deleteCollege, initColleges 
} from '@/api/admin'
```

- [ ] **Step 2: 添加初始化按钮**

在学院管理内容区（约311-340行），在表格上方添加初始化按钮：

```html
<el-card>
  <template #header>
    <div style="display: flex; justify-content: space-between; align-items: center;">
      <span>学院管理</span>
      <el-button type="primary" @click="handleInitColleges" :loading="collegeInitLoading">
        初始化学院数据
      </el-button>
    </div>
  </template>
  <!-- 现有表格代码 -->
</el-card>
```

- [ ] **Step 3: 添加loading状态和处理函数**

在学院管理的JavaScript逻辑部分（约787-862行），添加：

```javascript
const collegeInitLoading = ref(false)

const handleInitColleges = async () => {
  collegeInitLoading.value = true
  try {
    const res = await initColleges()
    if (res.code === 200) {
      ElMessage.success(`初始化完成：新增${res.data.inserted}个，已存在${res.data.existed}个`)
      loadColleges() // 重新加载学院列表
    }
  } catch (e) {
    ElMessage.error('初始化失败')
  } finally {
    collegeInitLoading.value = false
  }
}
```

- [ ] **Step 4: 验证编译通过**

Run: `cd frontend && npm run build`
Expected: Build successful

- [ ] **Step 5: Commit**

```bash
git add frontend/src/views/admin/AdminPage.vue
git commit -m "feat: add college init button to admin page"
```

---

### Task 4: 测试验证

**Files:**
- Test: 手动测试

- [ ] **Step 1: 启动后端服务**

Run: `cd backend && mvn spring-boot:run`
Expected: Application started

- [ ] **Step 2: 启动前端服务**

Run: `cd frontend && npm run dev`
Expected: Local: http://localhost:5173/

- [ ] **Step 3: 测试初始化功能**

1. 登录管理员账号
2. 进入学院管理页面
3. 点击"初始化学院数据"按钮
4. 验证显示成功消息：新增12个，已存在1个
5. 验证表格显示13个学院

- [ ] **Step 4: 测试幂等性**

再次点击"初始化学院数据"按钮，验证：
- 显示：新增0个，已存在13个
- 表格仍然显示13个学院

- [ ] **Step 5: 测试权限校验**

使用非管理员账号登录，验证：
- 无法看到初始化按钮（或点击后返回403错误）

- [ ] **Step 6: Final Commit**

```bash
git add -A
git commit -m "test: verify college init functionality"
```
