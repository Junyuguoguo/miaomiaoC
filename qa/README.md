# miaomiaoC2 自动化测试入门脚手架

这个目录适合复制到解压后的 `miaomiaoC2/qa/` 下使用。项目本身是部署包：前端为 `frontend-dist/`，后端为 `backend/backend-0.0.1-SNAPSHOT.jar`，数据库为 `database/*.sql`。因此这里采用黑盒测试：先测部署包完整性，再测 API，最后测 UI。

## 1. 安装测试依赖

```bash
cd miaomiaoC2
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -r qa/requirements.txt
```

## 2. 先跑静态测试

静态测试不需要启动后端，用来确认部署包是否完整、前端引用资源是否存在、Nginx 代理是否配置 `/api/`。

```bash
pytest qa/tests/test_package_static.py
```

如果你的 qa 目录不在项目根目录下，可以指定：

```bash
MIAOMIAO_PROJECT_ROOT=/path/to/miaomiaoC2 pytest qa/tests/test_package_static.py
```

## 3. 启动后端测试环境

方式 A：你已经在本机或服务器跑好了项目。

```bash
export APP_BASE_URL=http://127.0.0.1:8080
pytest qa/tests/test_api_auth.py qa/tests/test_api_public.py
```

方式 B：用 Docker Compose 拉起 MySQL、Redis、后端 jar。

```bash
cd miaomiaoC2
# 注意：首次启动会导入 database/miaomiaoc.sql，数据较多，需等待 MySQL healthcheck 完成。
docker compose -f qa/docker-compose.qa.yml up -d
export APP_BASE_URL=http://127.0.0.1:8080
pytest qa/tests/test_api_auth.py qa/tests/test_api_public.py
```

默认测试账号来自项目 README：

```bash
export MIAOMIAO_USERNAME=123456
export MIAOMIAO_PASSWORD=hys123
export MIAOMIAO_ROLE=student
```

## 4. 跑 UI 登录冒烟测试

```bash
playwright install chromium
export APP_BASE_URL=http://你的站点地址
pytest qa/tests/test_ui_login.py --browser chromium
```

如果只启动了后端 jar，没有部署 `frontend-dist/`，UI 测试不会通过；它需要访问完整前端页面。

## 5. 如何继续扩展

建议按这个顺序增加用例：

1. 登录鉴权：正确账号、错误密码、空用户名、不同角色登录。
2. 题库查询：登录后调用 `/api/bank/getBankList`、`/api/bank/getAllQuestionList`。
3. 题目详情：对一个固定题目 ID 调 `/api/bank/getQuestionById`。
4. 考试列表：调用 `/api/exam/getExamList`。
5. 提交类接口：放到独立测试库，测试后清理数据，避免污染真实数据库。
6. UI：只做关键路径冒烟，例如登录、进入考试首页、进入题库页。

## 6. 安全提醒

`database/miaomiaoc.sql` 里包含大量用户数据和明文密码样式的数据。学习测试时不要把它当生产数据使用；建议在本地测试库中导入，并单独创建测试账号。
