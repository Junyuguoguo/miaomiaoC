// tests/frontend/helpers/selectors.ts
export const selectors = {
  login: {
    usernameInput: 'input[placeholder*="请输入账号"], input[placeholder*="用户名"]',
    passwordInput: 'input[placeholder*="请输入密码"], input[type="password"]',
    submitButton: 'button:has-text("登录")',
    roleButton: (role: string) => `button:has-text("${role}")`,
    errorMessage: '.el-message--error, .el-message, [class*="error"]',
    registerLink: '.el-link:has-text("注册"), a:has-text("注册")',
    forgotLink: '.el-link:has-text("找回密码"), a:has-text("忘记密码")'
  },
  register: {
    usernameInput: 'input[placeholder*="请输入账号"], input[placeholder*="用户名"]',
    passwordInput: 'input[placeholder*="请输入密码"], input[type="password"]',
    submitButton: 'button:has-text("注册")',
    errorMessage: '.el-message--error, .el-message, [class*="error"]',
    loginLink: '.el-link:has-text("登录"), a:has-text("登录")'
  },
  exam: {
    list: '.exam-card, .exam-item, [class*="exam"]',
    startButton: 'button:has-text("开始考试"), button:has-text("进入考试")',
    submitButton: 'button:has-text("提交"), button:has-text("交卷")',
    detail: '.exam-detail, .exam-info, [class*="detail"]'
  },
  chat: {
    roomList: '.chat-room, .room-item, [class*="room"]',
    createButton: 'button:has-text("创建"), button:has-text("新建")',
    messageInput: 'textarea[placeholder*="消息"], textarea[placeholder*="输入"], .el-textarea__inner',
    sendButton: 'button:has-text("发送"), button[type="submit"]',
    messageList: '.message-list, .chat-messages, [class*="message"]'
  },
  admin: {
    menu: '.menu, .sidebar, [class*="menu"], [class*="sidebar"], nav',
    userManage: 'text=用户管理, [class*="user"]',
    inviteManage: 'text=邀请码, [class*="invite"]',
    collegeManage: 'text=学院, [class*="college"]',
    statsOverview: 'text=统计, [class*="stats"]'
  },
  question: {
    list: '.question-card, .question-item, [class*="question"]',
    detail: '.question-detail, .question-info',
    submitButton: 'button:has-text("提交"), button:has-text("保存")',
    wrongList: '.wrong-question, .error-list'
  },
  teacher: {
    statsOverview: '.stats-overview, .statistics, [class*="stats"]',
    studentList: '.student-list, .student-table, [class*="student"]',
    questionManage: '.question-manage, .question-bank, [class*="question"]'
  }
};
