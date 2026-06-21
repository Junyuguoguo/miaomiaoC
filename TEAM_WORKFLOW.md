# 团队协作开发指南

本文档说明如何让团队成员共同参与 miaomiaoC 项目的开发。

## 📋 目录

1. [快速开始 - 邀请协作者](#快速开始---邀请协作者)
2. [开发者加入流程](#开发者加入流程)
3. [分支管理策略](#分支管理策略)
4. [日常开发工作流](#日常开发工作流)
5. [代码审查流程](#代码审查流程)
6. [冲突解决](#冲突解决)
7. [最佳实践](#最佳实践)

---

## 快速开始 - 邀请协作者

### 方式一：GitHub 仓库设置（推荐）

#### 步骤 1：在 GitHub 上添加协作者

1. 访问你的 GitHub 仓库：https://github.com/Junyuguoguo/miaomiaoC
2. 点击 **Settings**（设置）标签
3. 在左侧菜单找到 **Collaborators and teams**（协作者和团队）
4. 点击 **Add people**（添加人员）
5. 输入同事的 GitHub 用户名或邮箱
6. 选择权限级别：
   - **Read**: 只能查看和克隆代码
   - **Write**: 可以推送代码到非保护分支
   - **Admin**: 完全控制（包括删除仓库）
7. 点击 **Add [username] to this repository**

#### 步骤 2：同事接受邀请

同事会收到邮件邀请，或者在 GitHub 通知中心看到邀请：
- 点击邀请链接
- 接受邀请后即可访问仓库

---

### 方式二：使用组织（适合大团队）

如果团队超过 3 人，建议创建 GitHub Organization：

1. 访问 https://github.com/organizations/new
2. 创建组织（免费计划支持无限成员）
3. 将仓库转移到组织下
4. 创建团队并分配权限
5. 邀请成员加入组织

---

## 开发者加入流程

### 新成员首次设置

#### 1. 克隆仓库

```bash
# 克隆主仓库
git clone https://github.com/Junyuguoguo/miaomiaoC.git
cd miaomiaoC

# 或者使用 SSH（需要先配置 SSH 密钥）
git clone git@github.com:Junyuguoguo/miaomiaoC.git
cd miaomiaoC
```

#### 2. 配置 Git 用户信息

```bash
# 设置用户名和邮箱（与 GitHub 账号一致）
git config user.name "Your Name"
git config user.email "your-email@example.com"

# 全局配置（只需设置一次）
git config --global user.name "Your Name"
git config --global user.email "your-email@example.com"
```

#### 3. 安装依赖

**后端：**
```bash
cd backend
mvn clean install
```

**前端：**
```bash
cd frontend
npm install
```

#### 4. 配置环境

复制配置文件并修改：

```bash
# 后端配置
cd backend/src/main/resources
cp application.example.yml application.yml
# 编辑 application.yml，修改数据库、Redis、邮件等配置

# 前端配置（通常不需要修改，使用默认即可）
cat frontend/env/.env.development
```

#### 5. 启动项目

参考 [README.md](README.md) 中的快速开始部分。

---

## 分支管理策略

我们采用 **Git Flow** 简化版分支策略：

### 分支类型

| 分支类型 | 命名规范 | 说明 | 生命周期 |
|---------|---------|------|---------|
| **main/master** | `main` 或 `master` | 生产环境代码 | 永久 |
| **develop** | `develop` | 开发主分支 | 永久 |
| **feature** | `feature/xxx` | 功能开发分支 | 临时 |
| **bugfix** | `bugfix/xxx` | Bug 修复分支 | 临时 |
| **hotfix** | `hotfix/xxx` | 紧急修复分支 | 临时 |

### 分支工作流程

```
main (生产)
  ↑ merge
develop (开发)
  ↑ merge
feature/user-login (新功能)
  ↑ commit
feature/user-login (本地开发)
```

### 具体操作

#### 创建功能分支

```bash
# 确保在 develop 分支上
git checkout develop
git pull origin develop

# 创建并切换到新功能分支
git checkout -b feature/user-authentication

# 或者
git branch feature/user-authentication
git checkout feature/user-authentication
```

#### 分支命名示例

- ✅ `feature/user-login` - 用户登录功能
- ✅ `feature/vip-payment` - VIP 支付功能
- ✅ `bugfix/registration-error` - 注册错误修复
- ✅ `hotfix/security-vulnerability` - 安全漏洞紧急修复
- ❌ `new-feature` - 不够明确
- ❌ `test` - 太笼统
- ❌ `zhangsan-branch` - 应包含功能描述

---

## 日常开发工作流

### 标准开发流程

#### 1. 开始新任务

```bash
# 切换到 develop 分支并更新
git checkout develop
git pull origin develop

# 创建功能分支
git checkout -b feature/your-feature-name
```

#### 2. 开发过程中

```bash
# 编写代码...

# 查看更改
git status

# 添加文件到暂存区
git add .
# 或者只添加特定文件
git add src/api/auth.js

# 提交更改（使用规范的提交信息）
git commit -m "feat: 添加用户登录功能"
# 或
git commit -m "fix: 修复注册页面验证问题"
# 或
git commit -m "docs: 更新 API 文档"
```

#### 3. 推送分支到远程

```bash
# 首次推送需要设置上游分支
git push -u origin feature/your-feature-name

# 后续推送直接执行
git push
```

#### 4. 创建 Pull Request

1. 访问 GitHub 仓库页面
2. 点击 **Pull requests** 标签
3. 点击 **New pull request**
4. 选择分支：
   - base: `develop`
   - compare: `feature/your-feature-name`
5. 填写 PR 标题和描述
6. 点击 **Create pull request**

#### 5. 等待代码审查

- 团队成员审查代码
- 根据反馈进行修改
- 审查通过后合并到 develop

#### 6. 同步最新代码

```bash
# 如果 develop 有新提交，需要同步
git checkout develop
git pull origin develop

# 回到功能分支
git checkout feature/your-feature-name

# 合并 develop 的最新代码
git merge develop

# 解决冲突（如果有）
# 然后继续推送
git push
```

---

## 代码审查流程

### Pull Request 规范

#### PR 标题格式

```
<type>: <description>

示例：
feat: 添加 VIP 会员系统
fix: 修复注册时密码验证问题
docs: 更新部署文档
refactor: 重构用户认证模块
```

#### PR 描述模板

```markdown
## 变更说明
简要描述本次变更的内容

## 相关 Issue
关联的 Issue 编号（如有）

## 测试方法
1. 步骤一
2. 步骤二
3. 预期结果

## 截图（如有 UI 变更）
[截图]

## 检查清单
- [ ] 代码符合规范
- [ ] 已添加必要的注释
- [ ] 已进行自测
- [ ] 已更新相关文档
```

### 审查要点

#### 后端代码
- [ ] 代码是否符合 Java 规范
- [ ] 是否有适当的异常处理
- [ ] 是否添加了必要的日志
- [ ] 是否有 SQL 注入风险
- [ ] 是否有内存泄漏风险
- [ ] API 响应格式是否统一

#### 前端代码
- [ ] 代码是否符合 Vue 规范
- [ ] 组件是否合理拆分
- [ ] 是否有适当的错误提示
- [ ] 是否处理了加载状态
- [ ] 样式是否兼容不同浏览器
- [ ] 是否有 XSS 安全风险

#### 通用
- [ ] 提交信息是否清晰
- [ ] 是否有不必要的调试代码
- [ ] 是否更新了相关文档
- [ ] 是否影响了现有功能

### 审查反馈

**批准合并：**
```
LGTM (Looks Good To Me)
✅ 代码质量良好，可以合并
```

**需要修改：**
```
❌ 需要修改：
1. 第 XX 行缺少空指针检查
2. 变量命名不符合规范
3. 需要添加单元测试
```

**请求更改：**
使用 GitHub 的 "Request changes" 功能，明确指出需要修改的地方。

---

## 冲突解决

### 何时会发生冲突？

当多人修改了同一个文件的同一部分时，Git 无法自动合并，就会产生冲突。

### 解决步骤

#### 1. 发现冲突

```bash
git merge develop
# 输出：CONFLICT (content): Merge conflict in src/api/auth.js
```

#### 2. 查看冲突文件

```bash
git status
# 显示：both modified: src/api/auth.js
```

#### 3. 打开冲突文件

冲突标记格式：
```javascript
<<<<<<< HEAD
// 你当前的代码
export function login(data) {
    return request({ url: '/api/auth/login', method: 'post', data })
}
=======
// develop 分支的代码
export function login(data) {
    return request({ 
        url: '/api/auth/login', 
        method: 'post', 
        data,
        timeout: 10000 
    })
}
>>>>>>> develop
```

#### 4. 解决冲突

手动编辑文件，保留需要的代码，删除冲突标记：

```javascript
// 解决后的代码
export function login(data) {
    return request({ 
        url: '/api/auth/login', 
        method: 'post', 
        data,
        timeout: 10000 
    })
}
```

#### 5. 标记冲突已解决

```bash
git add src/api/auth.js
```

#### 6. 完成合并

```bash
git commit -m "merge: 解决与 develop 分支的冲突"
```

### 预防冲突的技巧

1. **经常同步**：每天开始工作前 pull 最新代码
2. **小步快跑**：频繁提交小的更改，而不是大量更改后一次性提交
3. **沟通协作**：如果多人要修改同一文件，提前沟通协调
4. **模块化设计**：减少多人修改同一文件的可能性

---

## 最佳实践

### 提交信息规范

遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

#### Type 类型

| 类型 | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat: 添加用户登录功能` |
| `fix` | Bug 修复 | `fix: 修复注册验证问题` |
| `docs` | 文档更新 | `docs: 更新 API 文档` |
| `style` | 代码格式 | `style: 格式化代码` |
| `refactor` | 重构 | `refactor: 重构用户模块` |
| `perf` | 性能优化 | `perf: 优化查询性能` |
| `test` | 测试相关 | `test: 添加登录单元测试` |
| `chore` | 构建/工具 | `chore: 更新依赖版本` |

#### Scope（可选）

影响的模块范围：
- `feat(auth): 添加 OAuth 登录`
- `fix(exam): 修复考试计时器 bug`
- `docs(readme): 更新 README`

### 代码规范

#### 后端（Java）

1. 遵循阿里巴巴 Java 开发手册
2. 类名使用 PascalCase：`UserController`
3. 方法名使用 camelCase：`getUserById()`
4. 常量使用 UPPER_SNAKE_CASE：`MAX_RETRY_COUNT`
5. 包名全小写：`com.example.controller`
6. 添加必要的 JavaDoc 注释

#### 前端（Vue）

1. 遵循 Vue 官方风格指南
2. 组件名使用 PascalCase：`UserLogin.vue`
3. 变量/函数使用 camelCase：`userData`, `fetchUserList()`
4. 常量使用 UPPER_SNAKE_CASE：`API_BASE_URL`
5. CSS 类名使用 kebab-case：`.user-profile`
6. 使用 `<script setup>` 语法

### 定期同步

```bash
# 每天早上开始工作前
git checkout develop
git pull origin develop

# 如果正在功能分支上工作
git checkout feature/your-feature
git merge develop  # 或 git rebase develop
```

### 清理无用分支

```bash
# 查看所有分支
git branch -a

# 删除本地已合并的分支
git branch --merged develop | grep -v "develop" | xargs git branch -d

# 删除远程已合并的分支（谨慎操作）
git push origin --delete feature/completed-feature
```

### 回退错误提交

```bash
# 撤销最后一次提交（保留更改）
git reset --soft HEAD~1

# 撤销最后一次提交（丢弃更改）
git reset --hard HEAD~1

# 创建一个新的提交来撤销之前的提交（推荐用于已推送的提交）
git revert <commit-hash>
```

---

## 常见问题 FAQ

### Q1: 如何查看谁修改了某段代码？

```bash
# 查看文件的历史提交
git log --oneline src/api/auth.js

# 查看某行的修改历史
git blame src/api/auth.js
```

### Q2: 如何临时保存未完成的更改？

```bash
# 保存当前工作状态
git stash save "WIP: 正在开发登录功能"

# 查看保存的列表
git stash list

# 恢复最近的工作
git stash pop

# 恢复指定的工作
git stash apply stash@{1}
```

### Q3: 如何比较两个分支的差异？

```bash
# 比较当前分支与 develop 的差异
git diff develop

# 比较两个分支的文件差异
git diff develop feature/user-login -- src/api/auth.js
```

### Q4: 如何只拉取某个文件或目录？

Git 不支持部分拉取，但可以稀疏检出：

```bash
# 启用稀疏检出
git sparse-checkout init

# 指定要检出的目录
git sparse-checkout set frontend/src/api/

# 恢复正常检出
git sparse-checkout disable
```

### Q5: 不小心提交了敏感信息怎么办？

**立即操作：**

1. 如果刚提交且未推送：
```bash
git reset --hard HEAD~1
```

2. 如果已推送到远程：
```bash
# 使用 BFG Repo-Cleaner 或 git filter-branch 清除历史
# 然后强制推送（警告：会覆盖远程历史）
git push --force-with-lease
```

3. **重要**：立即轮换泄露的密钥/密码！

---

## 工具推荐

### Git GUI 客户端

- **[GitHub Desktop](https://desktop.github.com/)** - 官方客户端，简单易用
- **[SourceTree](https://www.sourcetreeapp.com/)** - Atlassian 出品，功能强大
- **[GitKraken](https://www.gitkraken.com/)** - 跨平台，可视化优秀
- **[VS Code Git](https://code.visualstudio.com/docs/editor/versioncontrol)** - IDE 内置

### 命令行增强

- **[oh-my-zsh](https://ohmyz.sh/)** - Zsh 框架，带 Git 插件
- **[git-extras](https://github.com/tj/git-extras)** - Git 扩展命令
- **[lazygit](https://github.com/jesseduffield/lazygit)** - 终端 Git UI

### 代码审查工具

- **GitHub Pull Requests** - 内置审查功能
- **[Reviewable](https://reviewable.io/)** - 高级审查工具
- **[CodeClimate](https://codeclimate.com/)** - 自动化代码质量检查

---

## 联系与支持

如有问题，请通过以下方式联系：

1. 在项目 Issues 中提问：https://github.com/Junyuguoguo/miaomiaoC/issues
2. 团队内部沟通工具（Slack/钉钉/企业微信等）
3. 邮件联系项目负责人

---

**最后更新**: 2026-06-18  
**维护者**: Yuhuang Sen
