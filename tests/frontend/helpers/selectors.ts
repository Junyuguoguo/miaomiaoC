// tests/frontend/helpers/selectors.ts
export const selectors = {
  login: {
    usernameInput: 'input[placeholder*="用户名"], input[name="username"]',
    passwordInput: 'input[type="password"]',
    submitButton: 'button:has-text("登录")',
    errorMessage: '.el-message--error, .error-message, [class*="error"]',
    registerLink: 'a:has-text("注册"), button:has-text("注册")',
    forgotLink: 'a:has-text("忘记密码"), button:has-text("忘记密码")'
  },
  register: {
    usernameInput: 'input[placeholder*="用户名"], input[name="username"]',
    passwordInput: 'input[type="password"]',
    submitButton: 'button:has-text("注册")',
    errorMessage: '.el-message--error, .error-message, [class*="error"]',
    loginLink: 'a:has-text("登录"), button:has-text("登录")'
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
    messageInput: 'textarea, input[placeholder*="消息"], input[placeholder*="输入"]',
    sendButton: 'button:has-text("发送"), button[type="submit"]',
    messageList: '.message-list, .chat-messages, [class*="message"]'
  },
  admin: {
    menu: '.menu, .sidebar, [class*="menu"], [class*="sidebar"]',
    userManage: 'text=用户管理, text=Users, [class*="user"]',
    inviteManage: 'text=邀请码, text=Invite, [class*="invite"]',
    collegeManage: 'text=学院, text=College, [class*="college"]',
    statsOverview: 'text=统计, text=Stats, [class*="stats"]'
  },
  question: {
    list: '.question-card, .question-item, [class*="question"]',
    detail: '.question-detail, .question-info',
    submitButton: 'button:has-text("提交"), button:has-text("保存")',
    wrongList: '.wrong-question, .error-list'
  },
  teacher: {
    statsOverview: '.stats-overview, .statistics',
    studentList: '.student-list, .student-table',
    questionManage: '.question-manage, .question-bank'
  }
};
