# 在线考试系统 (MiaoMiaoC)

一个基于 Spring Boot 4.x + Vue 3 的现代化在线考试系统，支持学生端、教师端和管理员端。

## 📋 项目简介

MiaoMiaoC 是一个功能完善的在线考试平台，提供题库管理、模拟考试、错题本、VIP会员等核心功能。系统采用前后端分离架构，使用 JWT Token 进行身份认证，Redis 缓存优化性能。

### 主要特性

- ✅ **多角色权限管理**：支持学生、教师、管理员三种角色
- ✅ **题库管理系统**：支持多种题型（单选、多选、判断、填空、编程题）
- ✅ **在线考试**：实时计时、自动提交、防作弊机制
- ✅ **智能组卷**：随机抽题、难度均衡
- ✅ **成绩分析**：详细的答题统计和错题分析
- ✅ **错题本**：自动收集错题，支持针对性练习
- ✅ **VIP会员系统**：积分兑换、会员权益管理
- ✅ **邮件服务**：找回密码、验证码发送
- ✅ **代码编辑器**：内置 C/C++ 代码编辑器，支持在线编译运行

## 🏗️ 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 开发语言 |
| Spring Boot | 4.0.3 | 核心框架 |
| Spring Security | 7.x | 安全框架 |
| Spring Data JPA | - | ORM 框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.0+ | 缓存数据库 |
| JWT | - | Token 认证 |
| Hutool | 5.8.22 | Java 工具库 |
| Lombok | - | 简化代码 |
| Spring Mail | - | 邮件服务 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 3.5.29 | 渐进式 JavaScript 框架 |
| Vite | 7.3.1 | 构建工具 |
| Element Plus | 2.13.5 | UI 组件库 |
| Pinia | 3.0.4 | 状态管理 |
| Vue Router | 4.6.4 | 路由管理 |
| Axios | 1.13.6 | HTTP 客户端 |
| CodeMirror | 6.x | 代码编辑器 |
| Day.js | 1.11.20 | 日期处理库 |

##  项目结构

```
miaomiaoC/
├── backend/                    # 后端项目
│   ├── src/main/java/sen/yuhuang/backend/
│   │   ├── common/            # 公共模块
│   │   │   ├── config/        # 配置类（CORS、Security、Redis等）
│   │   │   ├── filter/        # 过滤器（Token认证）
│   │   │   └── Result.java    # 统一响应封装
│   │   ├── controller/        # 控制器层
│   │   │   ├── UserController.java           # 用户管理
│   │   │   ├── QuestionController.java       # 题目管理
│   │   │   ├── QuestionBankController.java   # 题库管理
│   │   │   ├── SimulationExamController.java # 模拟考试
│   │   │   ├── VipKeyController.java         # VIP密钥
│   │   │   ├── VipPlanController.java        # VIP套餐
│   │   │   └── teacher/                      # 教师端接口
│   │   ├── service/           # 业务逻辑层
│   │   ├── repository/        # 数据访问层
│   │   ├── entity/            # 实体类
│   │   ├── dto/               # 数据传输对象
│   │   └── BackendApplication.java  # 启动类
│   ├── src/main/resources/
│   │   ├── application.yml    # 配置文件
│   │   └── application.example.yml  # 配置示例
│   ├── pom.xml                # Maven 依赖
│   └── compose.yaml           # Docker Compose 配置
│
├── frontend/                  # 前端项目
│   ├── src/
│   │   ├── api/              # API 接口定义
│   │   │   ├── auth.js       # 认证接口
│   │   │   ├── exam.js       # 考试接口
│   │   │   ├── question-bank.js  # 题库接口
│   │   │   ├── vip.js        # VIP接口
│   │   │   ── vip-key.js    # VIP密钥接口
│   │   ├── components/       # 公共组件
│   │   ├── router/           # 路由配置
│   │   ├── stores/           # Pinia 状态管理
│   │   │   ── user.js       # 用户状态
│   │   ├── utils/            # 工具函数
│   │   │   ├── request.js    # Axios 封装
│   │   │   └── c-syntax-checker.js  # C语法检查器
│   │   └── views/            # 页面视图
│   │       ├── login/        # 登录注册页
│   │       ├── exam/         # 考试相关页面
│   │       ├── question/     # 题库练习页面
│   │       ├── admin/        # 管理员页面
│   │       └── teacher/      # 教师管理页面
│   ├── env/                  # 环境变量
│   │   ├── .env.development  # 开发环境
│   │   └── .env.production   # 生产环境
│   ├── vite.config.js        # Vite 配置
│   └── package.json          # 依赖配置
│
├── deploy/                   # 部署相关文件
│   ├── nginx/               # Nginx 配置
│   └── sql/                 # 数据库迁移脚本
├── qa/                       # 测试相关
│   ├── tests/               # Pytest 测试用例
│   └── requirements.txt     # Python 依赖
├── miaomiaoc.sql            # 数据库初始化脚本
└── README.md                # 项目文档
```

## 🚀 快速开始

### 环境要求

- **JDK**: 21+
- **Node.js**: ^20.19.0 || >=22.12.0
- **MySQL**: 8.0+
- **Redis**: 7.0+
- **Maven**: 3.6+
- **npm/pnpm**: 最新稳定版

### 安装步骤

#### 1. 克隆项目

```bash
git clone https://github.com/your-username/miaomiaoC.git
cd miaomiaoC
```

#### 2. 配置数据库

创建 MySQL 数据库并导入初始数据：

```bash
mysql -u root -p
CREATE DATABASE miaomiaoc CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE miaomiaoc;
source miaomiaoc.sql;
```

#### 3. 启动 Redis

```bash
# macOS (使用 Homebrew)
brew install redis
redis-server

# Linux
sudo apt-get install redis-server
sudo systemctl start redis

# Windows
# 下载并安装 Redis for Windows
```

#### 4. 配置后端

复制配置文件并修改数据库连接信息：

```bash
cd backend/src/main/resources
cp application.example.yml application.yml
```

编辑 `application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/miaomiaoc?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password
  
  data:
    redis:
      host: localhost
      port: 6379
      password: ""  # 如果设置了密码则填写
  
  mail:
    host: smtp.163.com
    port: 465
    username: your_email@163.com
    password: your_authorization_code  # 授权码，非邮箱密码
```

#### 5. 启动后端

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动。

#### 6. 配置前端

```bash
cd frontend
npm install
```

检查环境变量配置：

```bash
# 查看 frontend/env/.env.development
cat env/.env.development
```

确保 `VITE_API_BASE_URL=http://localhost:8080/`

#### 7. 启动前端

```bash
npm run dev
```

前端服务将在 `http://localhost:5173` 启动。

### 访问系统

打开浏览器访问：**http://localhost:5173**

默认账号（根据数据库初始化脚本）：
- 管理员：`admin` / `admin123`
- 教师：`teacher1` / `teacher123`
- 学生：`student1` / `student123`

## 🔧 配置说明

### 后端配置

主要配置文件：`backend/src/main/resources/application.yml`

#### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/miaomiaoc
    username: root
    password: 12345678
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update  # 自动更新表结构
    show-sql: false     # 是否显示 SQL
```

#### Redis 配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 10000ms
```

#### 邮件服务配置

```yaml
spring:
  mail:
    host: smtp.163.com      # SMTP 服务器地址
    port: 465               # SSL 端口
    username: xxx@163.com   # 发件人邮箱
    password: xxxxxx        # 授权码（非邮箱密码）
```

**获取 163 邮箱授权码**：
1. 登录 163 邮箱网页版
2. 进入「设置」→「POP3/SMTP/IMAP」
3. 开启 SMTP 服务
4. 生成授权码并复制

#### JWT 配置

```yaml
jwt:
  redis:
    expire-time: 43200  # Token 过期时间（秒），43200 = 12小时
```

### 前端配置

环境变量文件：`frontend/env/.env.development`

```env
# 开发环境 - 请求本地后端
VITE_API_BASE_URL=http://localhost:8080/
VITE_APP_TITLE=在线考试系统
VITE_APP_VERSION=v1.1.0
```

生产环境配置：`frontend/env/.env.production`

```env
# 生产环境 - 请求相对路径（通过 Nginx 代理）
VITE_API_BASE_URL=/
VITE_APP_TITLE=在线考试系统
VITE_APP_VERSION=v1.1.0
```

## 📡 API 接口

### 认证相关

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/logout` | POST | 用户登出 |
| `/api/auth/register` | POST | 用户注册 |
| `/api/auth/sendEmail` | POST | 发送验证码 |
| `/api/auth/resetPassword` | PUT | 重置密码 |
| `/api/auth/verifyToken` | POST | 验证 Token |
| `/api/auth/getStatsData` | POST | 获取用户信息 |
| `/api/auth/updateUserInfo` | PUT | 更新用户信息 |

### 题库管理

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/bank/**` | GET/POST/PUT/DELETE | 题库 CRUD |
| `/api/question/**` | GET/POST/PUT/DELETE | 题目 CRUD |

### 考试相关

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/exam/start` | POST | 开始考试 |
| `/api/exam/submit` | POST | 提交试卷 |
| `/api/exam/result` | GET | 查询成绩 |
| `/api/wrong-question/**` | GET/POST | 错题本管理 |

### VIP 相关

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/vip/plans` | GET | 获取 VIP 套餐列表 |
| `/api/vip-keys/redeem` | POST | 兑换 VIP 密钥 |

### 教师端

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/teacher/**` | GET/POST/PUT/DELETE | 教师专属功能 |

## 🛠️ 开发指南

### 后端开发

#### 添加新接口

1. 在 `controller` 包下创建或修改 Controller 类
2. 在 `service` 包下实现业务逻辑
3. 在 `repository` 包下定义数据访问接口
4. 在 `entity` 包下定义实体类
5. 在 `dto` 包下定义 DTO 对象

示例：

```java
@RestController
@RequestMapping("/api/example")
public class ExampleController {
    
    @Autowired
    private ExampleService exampleService;
    
    @GetMapping("/list")
    public Result list() {
        return Result.success(exampleService.getList());
    }
}
```

#### 统一响应格式

所有接口返回统一的 `Result` 对象：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

常用状态码：
- `200`: 成功
- `400`: 参数错误
- `401`: 未授权
- `403`: 禁止访问
- `404`: 资源不存在
- `500`: 服务器内部错误

### 前端开发

#### 添加新页面

1. 在 `src/views` 目录下创建 Vue 组件
2. 在 `src/router/index.js` 中添加路由配置
3. 在 `src/api` 中定义对应的 API 接口

示例路由配置：

```javascript
{
  path: '/example',
  name: 'Example',
  component: () => import('@/views/example/ExamplePage.vue'),
  meta: { requiresAuth: true }
}
```

#### API 调用

使用封装好的 `request` 工具：

```javascript
import request from '@/utils/request'

export function getExampleList() {
  return request({
    url: '/api/example/list',
    method: 'get'
  })
}
```

#### 状态管理

使用 Pinia 管理全局状态：

```javascript
// src/stores/user.js
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    }
  }
})
```

## 🐳 Docker 部署

### 使用 Docker Compose

项目提供了 Docker Compose 配置文件，可以快速启动所有服务：

```bash
cd backend
docker-compose up -d
```

这将启动：
- MySQL 数据库
- Redis 缓存
- Spring Boot 应用

### 手动部署

#### 1. 打包后端

```bash
cd backend
mvn clean package -DskipTests
```

生成的 JAR 包位于：`target/backend-0.0.1-SNAPSHOT.jar`

#### 2. 打包前端

```bash
cd frontend
npm run build
```

生成的静态文件位于：`dist/` 目录

#### 3. 配置 Nginx

参考 `deploy/nginx/aa.junyuguoguo.xyz.conf` 配置反向代理：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端 API 代理
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

#### 4. 启动服务

```bash
# 启动后端
nohup java -jar backend-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# 重启 Nginx
sudo systemctl restart nginx
```

## 🧪 测试

### 后端测试

```bash
cd backend
mvn test
```

### 前端测试

```bash
cd frontend
npm run test
```

### API 自动化测试

项目提供了基于 Pytest 的 API 测试套件：

```bash
cd qa
pip install -r requirements.txt
pytest tests/
```

## 📝 常见问题

### 1. 后端启动失败

**问题**：提示数据库连接失败

**解决**：
- 检查 MySQL 服务是否启动
- 确认 `application.yml` 中的数据库配置正确
- 确认数据库 `miaomiaoc` 已创建并导入初始数据

### 2. 前端跨域问题

**问题**：浏览器控制台显示 CORS 错误

**解决**：
- 检查后端 `CorsConfig.java` 配置是否正确
- 确认 `SecurityConfig.java` 中没有覆盖 CORS 配置
- 开发环境下，Vite 代理配置应正确指向后端地址

### 3. Redis 连接失败

**问题**：提示无法连接 Redis

**解决**：
- 检查 Redis 服务是否启动：`redis-cli ping` 应返回 `PONG`
- 确认 `application.yml` 中的 Redis 配置正确
- 如果设置了密码，确保密码正确

### 4. 邮件发送失败

**问题**：发送验证码时提示邮件发送失败

**解决**：
- 确认使用的是邮箱授权码，而非邮箱密码
- 检查 SMTP 服务器地址和端口是否正确
- 确认邮箱已开启 SMTP 服务

### 5. 注册时显示"网络错误"

**问题**：注册页面一直显示"网络错误，请检查网络连接"

**解决**：
1. 检查前端服务是否正常启动（访问 http://localhost:5173）
2. 检查后端服务是否正常启动（访问 http://localhost:8080）
3. 检查浏览器控制台的 Network 标签，查看请求是否成功
4. 清除浏览器缓存或使用无痕模式
5. 重启前端服务：`npm run dev`

### 6. Token 过期问题

**问题**：登录后一段时间操作提示未授权

**解决**：
- 检查 `application.yml` 中的 `jwt.redis.expire-time` 配置
- 确认 Redis 服务正常运行
- 前端会自动刷新 Token，如仍有问题请清除浏览器缓存重新登录

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

### 提交 Issue

请提供：
- 问题描述
- 复现步骤
- 预期行为
- 实际行为
- 环境信息（操作系统、浏览器、版本号等）
- 截图或日志（如有）

### 提交 PR

1. Fork 本仓库
2. 创建功能分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -m 'feat: add your feature'`
4. 推送分支：`git push origin feature/your-feature`
5. 提交 Pull Request

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 前端遵循 Vue 官方风格指南
- 提交前运行代码格式化
- 添加必要的注释和文档

##  许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 👥 作者

- **汪俊宇** - 主要开发者

##  致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [CodeMirror](https://codemirror.net/)
- [Hutool](https://hutool.cn/)

## 📞 联系方式

如有问题或建议，欢迎通过以下方式联系：

- Email: 1743654545@qq.com
- GitHub Issues: [https://github.com/your-username/miaomiaoC/issues](https://github.com/your-username/miaomiaoC/issues)

---

⭐ 如果这个项目对你有帮助，请给个 Star 支持一下！
