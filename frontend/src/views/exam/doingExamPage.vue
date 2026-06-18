<template>
  <div class="exam-doing-container" ref="containerRef">
    <!-- 顶部栏 -->
    <div class="top-bar">
      <div class="system-title">{{title}}@{{version}}</div>
      <div class="exam-timer">
        <el-icon class="timer-icon"><Clock /></el-icon>
        <!-- 未开始 -->
        <div v-if="countDownStatus === 'notStart'">
          考试即将开始：{{ formatTime(timeLeft) }}
        </div>

        <!-- 进行中 -->
        <div v-else-if="countDownStatus === 'ongoing'">
          考试剩余时间：{{ formatTime(timeLeft) }}
        </div>

        <!-- 已结束 -->
        <div v-else>
          考试已结束
        </div>
      </div>
      <div class="top-right-placeholder">
        <el-button
            type="primary"
            size="small"
            @click="toggleQuestionList"
            style="margin-right: 8px"
        >
          {{ isShowQuestionList ? '关闭题目列表' : '查看题目列表' }}
        </el-button>
        <!-- 交卷确认面板（含未提交状态展示） -->
        <div class="question-actions" style="margin-left: 10px;">
          <!-- 初始状态：显示交卷按钮 -->
          <template v-if="showSubmitExamConfirm === 0">
            <el-button type="danger" size="small" @click="triggerSubmitExamConfirm" :loading="isSubmitting">
              交卷
            </el-button>
          </template>
          <!-- 确认交卷：显示是/否按钮 -->
          <template v-else-if="showSubmitExamConfirm === 1">
            <div class="confirm-step">
              <span class="confirm-text">确认交卷？</span>
              <el-button type="success" size="small" @click="confirmSubmitExam">是</el-button>
              <el-button type="info" size="small" @click="cancelSubmitExam">否</el-button>
            </div>
          </template>
          <!-- 已交卷 -->
          <el-tag v-else-if="showSubmitExamConfirm === 2" type="success" size="small">已交卷</el-tag>
          <!-- 交卷中 -->
          <el-tag v-else-if="showSubmitExamConfirm === 3" type="warning" size="small">交卷中...</el-tag>
          <!-- 新增：未提交题目状态（和提示文案联动） -->
          <el-tag v-else-if="showSubmitExamConfirm === 4" type="warning" size="small">还有未提交题目！</el-tag>
        </div>
      </div>
    </div>

    <!-- 题目列表弹窗/侧边栏 -->
    <div v-if="isShowQuestionList" class="question-list-modal">
      <div class="question-list-header">
        <h3>题目列表（共{{ questionList.length }}题）</h3>
        <el-button text @click="toggleQuestionList"><el-icon><Close /></el-icon></el-button>
      </div>
      <div class="question-list-content">
        <div
            v-for="(item, index) in questionList"
            :key="item.id"
            class="question-list-item"
            :class="{
            active: currentQuestionIndex === index,
            submitted: item.isSubmitted
          }"
            @click="switchQuestion(index)"
        >
          <span class="question-number">{{ index + 1 }}</span>
          <span class="question-title">{{ item.shortTitle || `第${index + 1}题` }}</span>
          <el-icon v-if="item.isSubmitted" class="submitted-icon"><Check /></el-icon>
        </div>
      </div>
    </div>

    <!-- 主内容区域：左右分栏 -->
    <div class="main-content">
      <!-- 左侧：题目描述 -->
      <div class="left-panel">
        <!-- 题目切换按钮 -->
        <div class="question-nav">
          <el-button
              size="small"
              @click="prevQuestion"
              :disabled="currentQuestionIndex <= 0"
          >
            上一题
          </el-button>
          <span class="current-question-info">
            第{{ currentQuestionIndex + 1 }}题 / 共{{ questionList.length }}题
          </span>
          <el-button
              size="small"
              @click="nextQuestion"
              :disabled="currentQuestionIndex >= questionList.length - 1"
          >
            下一题
          </el-button>
        </div>

        <div class="question-header">
          <div class="accepted-badge">
            得分: {{ currentQuestionScore }}/{{ currentQuestion.fullScore || 100 }}分
          </div>
          <div v-if="submitTipText" class="tip-box" :class="submitTipType">
            {{ submitTipText }}
          </div>
          <div class="question-actions">
            <!-- 未显示确认步骤时，显示提交按钮 -->
            <template v-if="showSubmitConfirm === 0">
              <el-button text bg size="small" @click="triggerSubmitConfirm">
                提交本题
              </el-button>
            </template>

            <!-- 显示提交确认步骤 -->
            <template v-else-if="showSubmitConfirm === 1">
              <div class="confirm-step">
                <span class="confirm-text">确认提交本题？</span>
                <el-button type="success" size="small" @click="confirmSubmitQuestion">是</el-button>
                <el-button type="info" size="small" @click="cancelSubmitQuestion">否</el-button>
              </div>
            </template>

            <!-- 本题已提交时显示标签 -->
            <el-tag v-else-if="showSubmitConfirm === 2" type="success" size="small">本题已提交</el-tag>
            <el-tag v-else-if="showSubmitConfirm === 3" type="warning" size="small">提交中...</el-tag>
          </div>
        </div>

        <div class="question-content" v-if="currentQuestion">
          <h3>题目描述</h3>
          <p>{{ currentQuestion.questionDesc }}</p>

          <h3>提示</h3>
          <p v-html="currentQuestion.hint?.replace(/\n/g, '<br>') || '暂无提示'"></p>

          <h3>输入格式</h3>
          <p>{{ currentQuestion.inputFormat }}</p>

          <h3>输出格式</h3>
          <p>{{ currentQuestion.outputFormat }}</p>

          <h3>样例</h3>
          <div class="sample" v-if="currentQuestion.sampleQuestions && currentQuestion.sampleQuestions.length">
            <div v-for="(sample, index) in currentQuestion.sampleQuestions" :key="index" class="sample-item">
              <span class="sample-label">输入 {{ index + 1 }}</span>
              <pre class="sample-content">{{ sample.input }}</pre>
              <span class="sample-label">输出 {{ index + 1 }}</span>
              <pre class="sample-content">{{ sample.output }}</pre>
            </div>
          </div>
          <div v-else class="no-sample">暂无样例</div>
        </div>
      </div>

      <!-- 右侧：编程区域 -->
      <div class="right-panel">
        <!-- C语言代码编辑器 -->
        <div class="code-card">
          <div class="editor-header">
            <div class="editor-title">
              <el-icon><Document /></el-icon>
              <span>main.c</span>
            </div>
            <!-- 👇 新增：保存按钮（带星号提示） -->
            <button
                class="save-btn"
                @click="handleSaveClick"
                :class="{ 'has-changes': isCodeChanged }"
            >
              <span v-if="isCodeChanged">*</span> 保存
            </button>
            <div class="editor-actions">
              <el-button
                  type="warning"
                  @click="toggleSyntaxHighlight"
                  :disabled="currentQuestion.isSubmitted"
                  size="small"
                  plain
              >
                {{ isHighlightEnabled ? 'VsCode风格' : '机考环境' }}
              </el-button>
              <el-button
                  type="primary"
                  @click="runCode"
                  :disabled="currentQuestion.isSubmitted || isLoading"
                  :loading="isLoading"
                  size="small"
              >
                运行测试
              </el-button>
            </div>
          </div>
          <!-- CodeMirror 容器 -->
          <div
              ref="editorRef"
              class="code-editor"
              :class="{ 'editor-disabled': currentQuestion.isSubmitted }"
          ></div>
        </div>

        <!-- 语法检查结果 -->
        <div v-if="syntaxErrors.length > 0" class="syntax-check">
          <div class="syntax-check-title">
            <el-icon><Warning /></el-icon>
            <span>语法检查 ({{ syntaxErrors.length }}个问题)</span>
          </div>
          <div class="syntax-errors">
            <div v-for="(error, index) in syntaxErrors" :key="index" class="error-item">
              {{ error }}
            </div>
          </div>
        </div>

        <!-- 结果面板 -->
        <div v-if="runResult" class="result-panel">
          <div class="panel-header" @click="togglePanelHeight">
            <div class="header-left">
              <el-icon :class="runResult.success ? 'success' : 'error'">
                <component :is="runResult.success ? 'CircleCheck' : 'CircleClose'" />
              </el-icon>
              <span>运行结果</span>
            </div>
            <el-icon class="toggle-icon">
              <ArrowDown v-if="!isPanelMax" />
              <ArrowUp v-else />
            </el-icon>
          </div>
          <div class="panel-content" :class="{ 'panel-max': isPanelMax }">
            <pre class="result-content">{{ runResult.output }}</pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
// 在 script setup 开头的导入部分，添加缺失的图标
import {
  Clock, Warning, Close, Check,
  CircleCheck, CircleClose,
  Document, ArrowDown, ArrowUp  // 添加这两个缺失的图标
} from '@element-plus/icons-vue'
import { examApi, getExamByExamId, getExamQuestion, submitQuestion,runTestCode,submitExam } from "@/api/exam"
import { StateEffect } from '@codemirror/state'
// CodeMirror 导入
import { EditorState } from '@codemirror/state'
import { EditorView, lineNumbers, highlightActiveLine, keymap } from '@codemirror/view'
import { cpp } from '@codemirror/lang-cpp'
import { linter, lintGutter } from '@codemirror/lint'
import { syntaxTree } from '@codemirror/language'
import { oneDark } from '@codemirror/theme-one-dark'
// 👇 新增：自动补全提示导入
import { defaultKeymap, indentWithTab, undo, redo } from '@codemirror/commands'
import { autocompletion, completionKeymap } from '@codemirror/autocomplete'

const router = useRouter()
const route = useRoute()
const examId = ref(null)
const containerRef = ref(null)
const title = ref('')
const version = ref('')

// 考试基本信息
const examInfo = ref({
  startTime: '',
  examDuration: '',
  endTime: ''     // 新增：考试结束时间
})

// 题目列表
const questionList = ref([])
const currentQuestionIndex = ref(0)
const isShowQuestionList = ref(false)
const codeCache = ref({})
const submitStatus = ref({})

// 当前题目相关
const currentQuestion = ref({})
const currentCode = ref('')
const currentQuestionScore = ref(0)
const executeTime = ref('0ms')

// 运行状态
const isLoading = ref(false)
const isSubmitting = ref(false)
const syntaxErrors = ref([])
const runResult = ref(null)

// 提交本题确认步骤控制
const showSubmitConfirm = ref(0)
const SUBMIT_STATUS = {
  HIDE: 0,
  CONFIRM: 1,
  SUBMITTED: 2,
  DOING: 3
}
const currentSubmitQuestionId = ref('')

// 提示文本
const submitTipText = ref('')
const submitTipType = ref('')
let tipTimeout = null

// 每题独立缓存
const runResultCache = ref({})
const scoreCache = ref({})
const executeTimeCache = ref({})

// CodeMirror 实例
const editorRef = ref(null)
let cmEditor = null

// 面板展开状态
const isPanelMax = ref(false)

// 全屏相关
const isFullscreen = ref(false)
const fullscreenRetryCount = ref(0)
const maxRetries = 5
const isRecovering = ref(false)
const fullscreenExitCount = ref(0)
const maxExitWarnings = 3

// ==================== CodeMirror 自定义语法检查 +高亮+提示+保存====================
// 在 ref 声明部分添加
const isHighlightEnabled = ref(true) // 语法高亮开关
// ====== 新增状态变量（放在其他 ref 定义区） ======
const isCodeChanged = ref(false); // 标记代码是否有未保存变更
const cLinter = linter(view => {
  const diagnostics = []
  const tree = syntaxTree(view.state)

  tree.iterate({
    enter: (node) => {
      if (node.type.isError) {
        diagnostics.push({
          from: node.from,
          to: node.to,
          severity: 'error',
          message: '语法错误'
        })
      }
    }
  })

  return diagnostics
})
// 提示词
const cKeywords = [
  'int', 'char', 'float', 'double', 'void', 'short', 'long', 'unsigned', 'signed',
  'if', 'else', 'for', 'while', 'do', 'switch', 'case', 'default', 'break', 'continue', 'return',
  'struct', 'typedef', 'enum', 'union', 'static', 'const', 'extern', 'register', 'volatile',
  'sizeof', 'include', 'define', 'printf', 'scanf', 'malloc', 'free', 'NULL'
]
const cCompletion = (context) => {
  const word = context.matchBefore(/\w*/)
  if (!word) return null
  if (word.from === word.to && !context.explicit) return null
  return {
    from: word.from,
    options: cKeywords.map(key => ({
      label: key,
      type: 'keyword',
      boost: 1
    }))
  }
}

// 纯色主题 - 所有文字都是白色/灰色，无语法高亮
const plainTheme = EditorView.theme({
  '&': {
    color: '#e0e0e0',
    backgroundColor: '#1e1e1e',
  },
  '.cm-content': {
    caretColor: '#e0e0e0',
  },
  '.cm-line': {
    color: '#e0e0e0',
  },
  // 行号区域样式 - 改为灰色
  '.cm-gutter': {
    backgroundColor: '#1e1e1e',
    color: '#6e6e6e',  // 行号文字颜色改为灰色
  },
  '.cm-lineNumbers': {
    color: '#6e6e6e',  // 行号数字颜色改为灰色
  },
  '.cm-activeLineGutter': {
    backgroundColor: '#2d2d2d',  // 当前行行号背景色
    color: '#a0a0a0',  // 当前行行号颜色稍微亮一点
  },
  // 覆盖所有语法高亮的颜色，统一为白色
  '.cm-tag': { color: '#e0e0e0' },
  '.cm-attribute': { color: '#e0e0e0' },
  '.cm-string': { color: '#e0e0e0' },
  '.cm-number': { color: '#e0e0e0' },
  '.cm-variable': { color: '#e0e0e0' },
  '.cm-variable-2': { color: '#e0e0e0' },
  '.cm-variable-3': { color: '#e0e0e0' },
  '.cm-property': { color: '#e0e0e0' },
  '.cm-operator': { color: '#e0e0e0' },
  '.cm-keyword': { color: '#e0e0e0' },
  '.cm-atom': { color: '#e0e0e0' },
  '.cm-def': { color: '#e0e0e0' },
  '.cm-type': { color: '#e0e0e0' },
  '.cm-comment': { color: '#888888' },
  '.cm-meta': { color: '#e0e0e0' },
  '.cm-builtin': { color: '#e0e0e0' },
  '.cm-qualifier': { color: '#e0e0e0' },
  '.cm-bracket': { color: '#e0e0e0' },
  '.cm-error': { color: '#f44747' },
})
// 在 script 开头定义一个函数来获取扩展配置
// 修改 getBaseExtensions 函数
// 只改这一个函数即可
const getBaseExtensions = (isReadOnly = false, enableHighlight = true) => {
  const extensions = [
    lineNumbers(),
    highlightActiveLine(),
    cpp(),
    lintGutter(),
    cLinter,
    // 👇 机考环境关闭自动补全提示词
    ...(enableHighlight ? [autocompletion({ override: [cCompletion] })] : []),
    keymap.of([
      ...defaultKeymap,
      ...(enableHighlight ? completionKeymap : []), // 机考环境关闭补全快捷键
      indentWithTab,
      { key: 'Ctrl-z', run: undo, preventDefault: true, stop: true },
      { key: 'Ctrl-y', run: redo, preventDefault: true, stop: true },
      { key: 'Ctrl-s', run: (view) => { saveCurrentCodeToCache(); isCodeChanged.value = false; return true; }, preventDefault: true, stop: true },
      { key: 'Cmd-z', run: undo, preventDefault: true, stop: true },
      { key: 'Cmd-s', run: (view) => { saveCurrentCodeToCache(); return true; }, preventDefault: true, stop: true }
    ]),
    EditorView.updateListener.of((update) => {
      if (update.docChanged) {
        currentCode.value = update.state.doc.toString()
        if (window.syntaxTimeout) clearTimeout(window.syntaxTimeout)
        window.syntaxTimeout = setTimeout(() => { checkSyntax() }, 500)
      }
    }),
    EditorView.theme({
      '&': { height: '100%', fontSize: '16px', fontFamily: 'Fira Code, Consolas, "Courier New", monospace' },
      '.cm-line': { lineHeight: '1.6' },
      '.cm-content[contenteditable=false]': { opacity: 0.9 }
    }),
    EditorState.readOnly.of(isReadOnly)
  ]
  if (enableHighlight) {
    extensions.push(oneDark)
  } else {
    extensions.push(plainTheme)
  }
  return extensions
}
// ==================== 新增：手动保存当前代码到缓存（Ctrl+S触发） ====================
const saveCurrentCodeToCache = () => {
  console.log('保存函数触发！');
  if (!currentQuestion.value?.id) {
    ElMessage.warning('当前无题目ID，无法保存');
    return;
  }
  const qid = currentQuestion.value.id;
  codeCache.value[qid] = currentCode.value;
  // 👇 保存后移除星号
  isCodeChanged.value = false;
  // 全屏模式下用 console 提示（替代 ElMessage）
  console.log(`✅ 题目 ${qid} 代码已保存到缓存`);
};

// ====== 新增按钮点击事件 ======
const handleSaveClick = () => {
  saveCurrentCodeToCache();
};
// ====== 监听代码变化（自动加星号） ======
watch(currentCode, (newVal, oldVal) => {
  // 排除初始化和空值情况
  if (newVal !== oldVal && newVal !== undefined && oldVal !== undefined) {
    isCodeChanged.value = true;
  }
});

// 创建一个统一的函数来更新编辑器状态
const updateEditorState = (newCode, isReadOnly, enableHighlight = isHighlightEnabled.value) => {
  if (!cmEditor) return

  // 更新代码内容
  cmEditor.dispatch({
    changes: { from: 0, to: cmEditor.state.doc.length, insert: newCode }
  })

  // 重新配置扩展，包括主题
  const extensions = getBaseExtensions(isReadOnly, enableHighlight)
  cmEditor.dispatch({
    effects: StateEffect.reconfigure.of(extensions)
  })
}
// 自定义C语言语法检查
/**
 * C语言基础语法检查（仅检查核心结构+关键字拼写）
 * 1. 检查main函数是否存在
 * 2. 括号匹配检查（{} () []）
 * 3. 分号基础检查
 * 4. 关键字常见拼写错误检查（精准匹配，无模糊判断）
 */
const checkSyntax = async () => {
  try {
    syntaxErrors.value = [];
    const code = currentCode.value.trim();

    // 1. 空代码检查
    if (!code) {
      syntaxErrors.value.push('错误：代码不能为空');
      return;
    }

    // ==================== 核心基础检查 ====================
    const lines = code.split('\n');

    // 2. Main函数检查（精准匹配，无多余条件）
    const mainRegex = /\bint\s+main\s*\(/i;
    if (!mainRegex.test(code)) {
      syntaxErrors.value.push('错误：缺少main函数（建议格式：int main()）');
    }

    // 3. 括号匹配检查（经典栈算法，精准定位）
    const bracketStack = [];
    let lineNum = 1;
    // 跳过字符串/注释的括号检测
    let inString = false;
    let inComment = false;

    for (let i = 0; i < code.length; i++) {
      const char = code[i];
      const nextChar = code[i + 1];

      // 换行处理
      if (char === '\n') {
        lineNum++;
        continue;
      }

      // 跳过注释
      if (char === '/' && nextChar === '*' && !inString) {
        inComment = true;
        i++;
        continue;
      }
      if (char === '*' && nextChar === '/' && inComment) {
        inComment = false;
        i++;
        continue;
      }
      if (inComment) continue;

      // 跳过单行注释
      if (char === '/' && nextChar === '/') {
        while (i < code.length && code[i] !== '\n') i++;
        continue;
      }

      // 跳过字符串
      if (char === '"' && !inComment) inString = !inString;
      if (inString) continue;

      // 括号入栈
      if (['{', '(', '['].includes(char)) {
        bracketStack.push({ char, line: lineNum });
      }
      // 括号出栈匹配
      else if (['}', ')', ']'].includes(char)) {
        const matchChar = char === '}' ? '{' : char === ')' ? '(' : '[';
        const last = bracketStack.pop();

        if (!last || last.char !== matchChar) {
          syntaxErrors.value.push(`错误：第${lineNum}行 多余的"${char}"`);
        }
      }
    }

    // 检查未闭合的括号
    bracketStack.forEach(item => {
      syntaxErrors.value.push(`错误：第${item.line}行 "${item.char}" 未闭合`);
    });

    // 4. 分号基础检查（仅检查明显错误）
    lines.forEach((line, idx) => {
      const lineNumber = idx + 1;
      const cleanLine = line.replace(/".*?"/g, '').replace(/\/\/.*/, '').trim();

      // 检查连续分号
      if (cleanLine.includes(';;')) {
        syntaxErrors.value.push(`警告：第${lineNumber}行 存在多余分号;;`);
      }

      // 检查if/for/while后直接跟分号（逻辑错误）
      if (/if\s*\(.*\)\s*;|for\s*\(.*\)\s*;|while\s*\(.*\)\s*;/g.test(cleanLine)) {
        syntaxErrors.value.push(`警告：第${lineNumber}行 条件语句后多余分号`);
      }
    });

    // 5. 关键字常见拼写错误检查（精准匹配，无模糊判断）
    // 只检查高频错误，避免误判
    const keywordErrors = [
      { correct: 'int', wrong: /\bitn\b|\binet\b/g, msg: '可能将int拼写成' },
      { correct: 'char', wrong: /\bchra\b|\bcarh\b/g, msg: '可能将char拼写成' },
      { correct: 'float', wrong: /\bflaot\b|\bfloa\b/g, msg: '可能将float拼写成' },
      { correct: 'double', wrong: /\bdoubel\b|\bdoulbe\b/g, msg: '可能将double拼写成' },
      { correct: 'if', wrong: /\bfi\b/g, msg: '可能将if拼写成' },
      { correct: 'else', wrong: /\bels\b|\besle\b/g, msg: '可能将else拼写成' },
      { correct: 'for', wrong: /\brof\b/g, msg: '可能将for拼写成' },
      { correct: 'while', wrong: /\bwihle\b|\bwhiel\b/g, msg: '可能将while拼写成' },
      { correct: 'return', wrong: /\bretrun\b|\bretunr\b/g, msg: '可能将return拼写成' }
    ];

    lines.forEach((line, idx) => {
      const lineNumber = idx + 1;
      const cleanLine = line.replace(/".*?"/g, '').replace(/\/\/.*/, '').replace(/\/\*[\s\S]*?\*\//g, '');

      keywordErrors.forEach(({ correct, wrong, msg }) => {
        const matches = cleanLine.match(wrong);
        if (matches) {
          syntaxErrors.value.push(`警告：第${lineNumber}行 ${msg} ${matches.join('/')}（正确：${correct}）`);
        }
      });
    });

  } catch (error) {
    console.error('语法检查失败：', error);
    syntaxErrors.value = ['错误：语法检查异常'];
  }
};

const toggleSyntaxHighlight = () => {
  if (currentQuestion.value?.isSubmitted) {
    ElMessage.warning('本题已提交，无法修改代码样式')
    return
  }

  isHighlightEnabled.value = !isHighlightEnabled.value

  if (cmEditor) {
    const newExtensions = getBaseExtensions(
        currentQuestion.value?.isSubmitted || false,
        isHighlightEnabled.value
    )
    cmEditor.dispatch({
      effects: StateEffect.reconfigure.of(newExtensions)
    })
  }

  ElMessage.success(isHighlightEnabled.value ? '已启用语法高亮' : '已取消语法高亮')
}
// ==================== CodeMirror 初始化 ====================
const initCodeEditor = (initialCode = '') => {
  if (cmEditor) {
    cmEditor.destroy()
    cmEditor = null
  }

  if (!editorRef.value) return

  const extensions = getBaseExtensions(
      currentQuestion.value?.isSubmitted || false,
      isHighlightEnabled.value
  )

  const state = EditorState.create({
    doc: initialCode,
    extensions: extensions
  })

  cmEditor = new EditorView({
    state: state,
    parent: editorRef.value
  })
}


// ==================== 加载遮罩 ====================
let loadingInstance = null
const showLoading = (text = '正在处理，请稍候...') => {
  isLoading.value = true
  loadingInstance = ElLoading.service({
    lock: true,
    text: text,
    background: 'rgba(0, 0, 0, 0.7)',
    spinner: 'el-icon-loading'
  })
}

const closeLoading = () => {
  isLoading.value = false
  if (loadingInstance) {
    loadingInstance.close()
    loadingInstance = null
  }
}

// ==================== 提示方法 ====================
const showSubmitTip = (text, type = 'warning', duration = 3000) => {
  if (tipTimeout) {
    clearTimeout(tipTimeout)
  }
  submitTipText.value = text
  submitTipType.value = type
  if (duration > 0) {
    tipTimeout = setTimeout(() => {
      submitTipText.value = ''
    }, duration)
  }
}

const clearSubmitTip = () => {
  submitTipText.value = ''
  if (tipTimeout) {
    clearTimeout(tipTimeout)
    tipTimeout = null
  }
}

// ==================== 倒计时相关 + 自动交卷 ====================
// 倒计时相关
const timeLeft = ref(0)
let timer = null
const countDownStatus = ref('notStart')

// 自动交卷标志，防止重复提交
let autoSubmitTriggered = ref(false)

const formatTime = (seconds) => {
  if (seconds < 0) return '00:00:00'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const initCountDown = () => {
  // 清除之前的定时器
  if (timer) {
    clearInterval(timer)
    timer = null
  }

  // 检查必要参数
  if (!examInfo.value.startTime || (!examInfo.value.endTime && !examInfo.value.examDuration)) {
    console.warn('缺少考试时间信息')
    timeLeft.value = 0
    countDownStatus.value = 'notStart'
    return
  }

  const startTimestamp = new Date(examInfo.value.startTime).getTime()
  const nowTimestamp = Date.now()

  // 优先使用 endTime，如果没有则使用 startTime + examDuration 计算
  let endTimestamp
  if (examInfo.value.endTime) {
    endTimestamp = new Date(examInfo.value.endTime).getTime()
  } else {
    const durationSeconds = Number(examInfo.value.examDuration) * 60
    endTimestamp = startTimestamp + durationSeconds * 1000
  }

  console.log('考试开始时间:', new Date(startTimestamp))
  console.log('考试结束时间:', new Date(endTimestamp))
  console.log('当前时间:', new Date(nowTimestamp))

  // 情况1：考试还未开始
  if (nowTimestamp < startTimestamp) {
    countDownStatus.value = 'notStart'
    const timeToStart = Math.floor((startTimestamp - nowTimestamp) / 1000)
    timeLeft.value = timeToStart

    timer = setInterval(() => {
      const currentNow = Date.now()
      if (currentNow >= startTimestamp) {
        // 考试已开始，切换到进行中状态
        clearInterval(timer)
        startExamCountDown(endTimestamp)
      } else {
        timeLeft.value = Math.floor((startTimestamp - currentNow) / 1000)
        if (timeLeft.value <= 0) {
          clearInterval(timer)
          startExamCountDown(endTimestamp)
        }
      }
    }, 1000)
  }
  // 情况2：考试正在进行中
  else if (nowTimestamp < endTimestamp) {
    startExamCountDown(endTimestamp)
  }
  // 情况3：考试已结束
  else {
    countDownStatus.value = 'ended'
    timeLeft.value = 0
    // 自动交卷
    autoSubmitExamOnTimeout()
  }
}

const startExamCountDown = (endTimestamp) => {
  countDownStatus.value = 'ongoing'

  const updateTimeLeft = () => {
    const nowTimestamp = Date.now()
    const remaining = Math.floor((endTimestamp - nowTimestamp) / 1000)

    if (remaining <= 0) {
      // 考试时间到
      clearInterval(timer)
      timer = null
      timeLeft.value = 0
      countDownStatus.value = 'ended'
      // 自动交卷
      autoSubmitExamOnTimeout()
      return false
    }

    timeLeft.value = remaining
    return true
  }

  // 立即更新一次
  if (!updateTimeLeft()) return

  // 设置定时器每秒更新
  timer = setInterval(() => {
    updateTimeLeft()
  }, 1000)
}
// 自动交卷函数
const autoSubmitExamOnTimeout = async () => {
  // 防止重复触发
  if (autoSubmitTriggered.value) return
  autoSubmitTriggered.value = true

  // 检查是否已经交卷
  if (showSubmitExamConfirm.value === EXAM_SUBMIT_STATUS.SUBMITTED) {
    console.log('考试已经交卷，无需自动交卷')
    return
  }

  //  先退出全屏
  if (document.fullscreenElement) {
    await document.exitFullscreen()
    console.log('已退出全屏模式')
  }

  // 等待一小段时间，让全屏退出动画完成
  await new Promise(resolve => setTimeout(resolve, 300))

  // 显示提示
  ElMessage.warning('考试时间已到，系统将自动交卷！')

  // 等待2秒，给用户一个提示缓冲
  await new Promise(resolve => setTimeout(resolve, 2000))

  // 执行交卷逻辑
  await executeAutoSubmit()
}
// 自动提交所有未提交的题目
const autoSubmitAllQuestions = async () => {
  const unSubmittedQuestions = []

  // 找出所有未提交的题目
  questionList.value.forEach((question, index) => {
    if (!question.isSubmitted && question.id) {
      unSubmittedQuestions.push({
        index: index,
        id: question.id,
        code: codeCache.value[question.id] || ''
      })
    }
  })

  if (unSubmittedQuestions.length === 0) {
    return { successCount: 0, failCount: 0 }
  }

  console.log(`发现 ${unSubmittedQuestions.length} 道未提交的题目，开始自动提交...`)
  ElMessage.info(`正在自动提交 ${unSubmittedQuestions.length} 道未提交的题目...`)

  let successCount = 0
  let failCount = 0

  for (const question of unSubmittedQuestions) {
    try {
      const userId = localStorage.getItem('userId')
      const submitData = {
        userId: String(userId),
        examId: String(examId.value || ''),
        questionId: String(question.id),
        code: String(question.code || '') // 即使是空字符串也提交
      }

      const result = await submitQuestion(submitData)

      if (result && result.code === 200) {
        // 更新提交状态
        const qid = question.id
        submitStatus.value[qid] = true

        // 更新题目列表中的提交状态
        const targetQuestion = questionList.value.find(q => q.id === qid)
        if (targetQuestion) {
          targetQuestion.isSubmitted = true
        }

        // 保存分数到缓存（关键：立即更新）
        if (result.data?.score !== undefined) {
          scoreCache.value[qid] = result.data.score
        } else {
          scoreCache.value[qid] = 0
        }

        // 如果是当前题目，更新显示
        if (currentQuestion.value?.id === qid) {
          currentQuestionScore.value = result.data?.score || 0
          if (result.data?.usedTime) {
            executeTime.value = result.data.usedTime + 'ms'
          }
        }

        successCount++
        console.log(`题目 ${question.id} 自动提交成功，得分: ${result.data?.score || 0}`)
      } else {
        failCount++
        console.error(`题目 ${question.id} 自动提交失败:`, result?.message)
        // 即使提交失败，也标记为已尝试（防止无限重试）
        const targetQuestion = questionList.value.find(q => q.id === question.id)
        if (targetQuestion) {
          targetQuestion.isSubmitted = true
        }
      }
    } catch (error) {
      failCount++
      console.error(`题目 ${question.id} 自动提交异常:`, error)
      // 异常情况也标记为已处理
      const targetQuestion = questionList.value.find(q => q.id === question.id)
      if (targetQuestion) {
        targetQuestion.isSubmitted = true
      }
    }
  }

  console.log(`自动提交完成：成功 ${successCount} 题，失败 ${failCount} 题`)

  if (failCount > 0) {
    ElMessage.warning(`自动提交完成，成功 ${successCount} 题，失败 ${failCount} 题`)
  } else if (successCount > 0) {
    ElMessage.success(`成功自动提交 ${successCount} 道题目`)
  }

  return { successCount, failCount }
}

// 执行自动交卷
const executeAutoSubmit = async () => {
  try {
    isSubmitting.value = true
    showSubmitExamConfirm.value = EXAM_SUBMIT_STATUS.DOING

    // 保存当前题目的代码到缓存
    if (currentQuestion.value?.id) {
      codeCache.value[currentQuestion.value.id] = currentCode.value
    }

    const userId = localStorage.getItem('userId')
    if (!userId) {
      ElMessage.error('请重新登录')
      autoSubmitTriggered.value = false
      showSubmitExamConfirm.value = EXAM_SUBMIT_STATUS.HIDE
      return
    }

    // 自动提交所有未提交的题目
    const { successCount, failCount } = await autoSubmitAllQuestions()

    // 等待一小段时间，确保缓存更新完成
    await new Promise(resolve => setTimeout(resolve, 500))

    // 重新计算总分（从分数缓存，包括刚提交的）
    let scoreTotal = 0
    if (scoreCache.value && Object.keys(scoreCache.value).length > 0) {
      Object.values(scoreCache.value).forEach(score => {
        const numScore = Number(score || 0)
        if (!isNaN(numScore)) {
          scoreTotal += numScore
        }
      })
    }

    console.log(`最终总分: ${scoreTotal}, 提交成功: ${successCount}, 失败: ${failCount}`)

    // 调用后台交卷接口
    const res = await submitExam({
      userId: userId,
      examId: examId.value,
      scoreTotal: scoreTotal
    })

    if (res.code === 200) {
      showSubmitExamConfirm.value = EXAM_SUBMIT_STATUS.SUBMITTED
      ElMessage.success('自动交卷成功！正在跳转...')

      // 清理全屏与定时器
      stopFullscreenGuard()
      if (timer) clearInterval(timer)
      if (document.fullscreenElement) {
        await document.exitFullscreen()
      }

      // 延迟跳转到结果页
      setTimeout(() => {
        router.push(`/exam/resultExam/${examId.value}`)
      }, 1500)
    } else {
      ElMessage.error(res.message || '自动交卷失败，请手动交卷')
      autoSubmitTriggered.value = false
      showSubmitExamConfirm.value = EXAM_SUBMIT_STATUS.HIDE
    }
  } catch (err) {
    console.error('自动交卷失败:', err)
    ElMessage.error('自动交卷失败，请手动交卷')
    autoSubmitTriggered.value = false
    showSubmitExamConfirm.value = EXAM_SUBMIT_STATUS.HIDE
  } finally {
    isSubmitting.value = false
  }
}

// ==================== 题目切换逻辑 ====================
const switchQuestion = async (index) => {
  if (index < 0 || index >= questionList.value.length) return

  // 保存当前题的代码和结果到缓存
  if (currentQuestion.value?.id) {
    const qid = currentQuestion.value.id
    codeCache.value[qid] = currentCode.value
    if (runResult.value) {
      runResultCache.value[qid] = runResult.value
    }
    if (currentQuestionScore.value > 0) {
      scoreCache.value[qid] = currentQuestionScore.value
    }
    if (executeTime.value) {
      executeTimeCache.value[qid] = executeTime.value
    }
  }

  // 切换题目
  currentQuestionIndex.value = index
  currentQuestion.value = questionList.value[index]

  const qid = currentQuestion.value.id

  // 从缓存加载代码
  currentCode.value = codeCache.value[qid] || ''

  // 从缓存加载结果
  runResult.value = runResultCache.value[qid] || null
  currentQuestionScore.value = scoreCache.value[qid] || 0
  executeTime.value = executeTimeCache.value[qid] || '0ms'

  // 更新提交状态
  if (currentQuestion.value.isSubmitted) {
    showSubmitConfirm.value = SUBMIT_STATUS.SUBMITTED
  } else {
    showSubmitConfirm.value = SUBMIT_STATUS.HIDE
  }

  syntaxErrors.value = []
  isShowQuestionList.value = false

  // 更新编辑器
  await nextTick()
  if (cmEditor) {
    updateEditorState(currentCode.value, currentQuestion.value.isSubmitted, isHighlightEnabled.value)
  }
  // 对新题进行语法检查
  await checkSyntax()
}

const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    switchQuestion(currentQuestionIndex.value - 1)
  }
}

const nextQuestion = () => {
  if (currentQuestionIndex.value < questionList.value.length - 1) {
    switchQuestion(currentQuestionIndex.value + 1)
  }
}

const toggleQuestionList = () => {
  isShowQuestionList.value = !isShowQuestionList.value
}

// ==================== 提交题目 ====================
const triggerSubmitConfirm = async () => {
  clearSubmitTip()

  await checkSyntax()

  const hasError = syntaxErrors.value.some(e => e.startsWith('错误：'))
  if (hasError) {
    showSubmitTip('请先修复语法错误后再提交', 'error')
    return
  }

  if (!currentCode.value || currentCode.value.trim() === '') {
    showSubmitTip('代码不能为空', 'warning')
    return
  }

  currentSubmitQuestionId.value = currentQuestion.value.id
  showSubmitConfirm.value = SUBMIT_STATUS.CONFIRM
}

const confirmSubmitQuestion = async () => {
  clearSubmitTip()

  if (!currentSubmitQuestionId.value) {
    showSubmitTip('题目ID错误', 'error')
    showSubmitConfirm.value = SUBMIT_STATUS.HIDE
    return
  }

  if (!currentCode.value || currentCode.value.trim() === '') {
    showSubmitTip('代码不能为空', 'warning')
    showSubmitConfirm.value = SUBMIT_STATUS.HIDE
    return
  }

  const userId = localStorage.getItem('userId')
  if (!userId) {
    showSubmitTip('请重新登录', 'error')
    showSubmitConfirm.value = SUBMIT_STATUS.HIDE
    return
  }

  showLoading('正在提交并测试用例...')
  showSubmitConfirm.value = SUBMIT_STATUS.DOING

  try {
    isSubmitting.value = true

    // 确保所有值都是普通类型
    const submitData = {
      userId: String(userId),
      examId: String(examId.value || ''),
      questionId: String(currentSubmitQuestionId.value || ''),
      code: String(currentCode.value || '')
    }

    console.log('提交数据:', submitData) // 调试用

    // 直接调用，不经过任何包装
    const result = await submitQuestion(submitData)

    console.log('提交结果:', result) // 调试用

    if (result && result.code === 200) {
      // 更新提交状态
      const qid = currentSubmitQuestionId.value

      // 直接赋值，不使用展开运算符
      submitStatus.value[qid] = true

      if (questionList.value[currentQuestionIndex.value]) {
        questionList.value[currentQuestionIndex.value].isSubmitted = true
      }

      currentQuestionScore.value = result.data?.score || 0
      executeTime.value = (result.data?.usedTime || 0) + 'ms'

      // 更新运行结果
      runResult.value = {
        success: result.data?.passCount === result.data?.totalCount,
        output: `提交结果：${result.data?.message || '成功'}
            通过用例：${result.data?.passCount || 0}/${result.data?.totalCount || 0}
            通过率：${result.data?.passRate || '0%'}
            得分：${result.data?.score || 0}
            耗时：${result.data?.usedTime || 0}ms`
      }

      // 保存到缓存
      if (qid) {
        runResultCache.value[qid] = runResult.value
        scoreCache.value[qid] = result.data?.score || 0
        executeTimeCache.value[qid] = executeTime.value
      }

      showSubmitTip('提交成功！', 'success', 2000)
      showSubmitConfirm.value = SUBMIT_STATUS.SUBMITTED


      if (cmEditor) {
        updateEditorState(currentCode.value, currentQuestion.value?.isSubmitted || false)
      }

    } else {
      showSubmitTip('提交失败：' + (result?.message || '未知错误'), 'error')
      showSubmitConfirm.value = SUBMIT_STATUS.HIDE
    }
  } catch (error) {
    console.error('提交异常详情:', error)
    showSubmitTip('提交失败：' + (error.message || '请重试'), 'error')
    showSubmitConfirm.value = SUBMIT_STATUS.HIDE
  } finally {
    isSubmitting.value = false
    closeLoading()
    currentSubmitQuestionId.value = ''
  }
}

const cancelSubmitQuestion = () => {
  showSubmitConfirm.value = SUBMIT_STATUS.HIDE
  currentSubmitQuestionId.value = ''
}

// ==================== 运行代码 ====================

const runCode = async () => {
  await checkSyntax()

  const hasError = syntaxErrors.value.some(e => e.startsWith('错误：'))
  if (hasError) {
    ElMessage.error('代码存在语法错误，请先修复')
    return
  }

  if (questionList.value[currentQuestionIndex.value]?.isSubmitted) {
    ElMessage.warning('本题已提交，禁止再次运行')
    return
  }

  showLoading('正在运行测试用例...')

  try {
    // 1. 构造运行代码的请求参数（和提交逻辑保持一致）
    const runData = {
      code: String(currentCode.value || ''),
      questionId: String(currentQuestion.value.id || '')
    }

    console.log('运行代码请求参数:', runData)

    // 2. 调用真实的运行代码API（替换掉模拟逻辑）
    const result = await runTestCode(runData) // 注意：这里函数名和API名重复，建议把API改为 runCodeRequest

    // 3. 解析接口返回结果
    if (result && result.code === 200) {
      // 3.1 构造标准化的运行结果
      const realResult = {
        success: result.data?.passCount === result.data?.totalCount,
        output: `测试结果：${result.data?.message || '运行成功'}
            通过用例：${result.data?.passCount || 0}/${result.data?.totalCount || 0}
            通过率：${result.data?.passRate || '0%'}
            耗时：${result.data?.usedTime || 0}ms`
      }
      // 3.2 更新运行结果和缓存
      runResult.value = realResult
      if (currentQuestion.value?.id) {
        runResultCache.value[currentQuestion.value.id] = realResult
      }
    } else {
      // 接口返回非200的错误处理
      const errorMsg = result?.message || '运行失败，请检查代码后重试'
      const errorResult = {
        success: false,
        output: `测试结果：${errorMsg}
            通过用例：0/0
            通过率：0%
            耗时：0ms`
      }
      runResult.value = errorResult
      ElMessage.error(errorMsg)
    }
  } catch (error) {
    // 网络异常/其他未知错误处理
    console.error('代码运行异常:', error)
    const errorResult = {
      success: false,
      output: `测试结果：运行出错（${error.message || '未知错误'}）
        通过用例：0/0
        通过率：0%
        耗时：0ms`
    }
    runResult.value = errorResult
  } finally {
    closeLoading()
  }
}

// 补充：建议将API请求单独封装（避免函数名冲突）
const runCodeRequest = async (params) => {
  try {
    const response = await fetch('/api/run-code', { // 替换为你的真实接口地址
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(params)
    })
    return await response.json()
  } catch (err) {
    throw new Error('接口请求失败：' + err.message)
  }
}

// ==================== 交卷 ====================
const showSubmitExamConfirm = ref(0)
const EXAM_SUBMIT_STATUS = {
  HIDE: 0,        // 隐藏确认面板（显示交卷按钮）
  CONFIRM: 1,     // 显示确认面板（是/否按钮）
  SUBMITTED: 2,   // 已交卷
  DOING: 3,       // 交卷中
  UN_SUBMITTED: 4 // 存在未提交题目（新增状态）
}
// 触发交卷确认（核心修改：未提交题目时切换状态+自定义提示）
const triggerSubmitExamConfirm = () => {
  // 校验未提交题目
  const unSubmittedQuestions = checkUnSubmittedQuestions()

  if (unSubmittedQuestions.length > 0) {
    // 2. 切换到未提交题目状态（可用于页面展示提示）
    showSubmitExamConfirm.value = EXAM_SUBMIT_STATUS.UN_SUBMITTED;
    // 3. 可选：3秒后自动恢复为初始状态（避免状态一直停留）
    setTimeout(() => {
      if (showSubmitExamConfirm.value === EXAM_SUBMIT_STATUS.UN_SUBMITTED) {
        showSubmitExamConfirm.value = EXAM_SUBMIT_STATUS.HIDE;
      }
    }, 3000);
    return;
  }

  // 校验通过，显示交卷确认面板
  showSubmitExamConfirm.value = EXAM_SUBMIT_STATUS.CONFIRM
}

const checkUnSubmittedQuestions = () => {
  const unSubmittedIds = [];
  // 遍历题目列表，筛选未提交的题目ID
  if (questionList.value && questionList.value.length > 0) {
    questionList.value.forEach(question => {
      // 条件：题目有ID + 未标记为已提交（isSubmitted是提交本题时的标记字段）
      if (question.id && !question.isSubmitted) {
        unSubmittedIds.push(question.id);
      }
    });
  }
  return unSubmittedIds;
}
// 取消交卷
const cancelSubmitExam = () => {
  showSubmitExamConfirm.value = EXAM_SUBMIT_STATUS.HIDE
}
// 确认交卷（核心逻辑）
const confirmSubmitExam = async () => {
  try {
    isSubmitting.value = true
    showSubmitExamConfirm.value = EXAM_SUBMIT_STATUS.DOING

    // 保存最后一题代码
    if (currentQuestion.value?.id) {
      codeCache.value[currentQuestion.value.id] = currentCode.value
    }

    const userId = localStorage.getItem('userId')
    if (!userId) {
      ElMessage.error('请重新登录')
      return
    }

    // ========== 新增：从缓存计算总分 ==========
    let scoreTotal = 0;
    // 遍历分数缓存（scoreCache.value 是题目ID为key，分数为value的对象）
    if (scoreCache.value && Object.keys(scoreCache.value).length > 0) {
      Object.values(scoreCache.value).forEach(score => {
        // 兼容分数为null/undefined/字符串的情况，转为数字累加
        scoreTotal += Number(score || 0);
      });
    }
    // 调用后台交卷接口（只保存记录即可）
    const res = await submitExam({
      userId: userId,
      examId: examId.value,
      scoreTotal:scoreTotal
    })
    if (res.code === 200) {
      showSubmitExamConfirm.value = EXAM_SUBMIT_STATUS.SUBMITTED
      // 清理全屏与定时器
      stopFullscreenGuard()
      if (timer) clearInterval(timer)
      if (document.fullscreenElement) document.exitFullscreen()
      // 跳转到结果页
      router.push(`/exam/resultExam/${examId.value}`)
    } else {
      //ElMessage.error(res.message || '交卷失败')
      showSubmitExamConfirm.value = EXAM_SUBMIT_STATUS.HIDE
    }
  } catch (err) {
    console.error(err)
    //ElMessage.error('交卷请求异常')
    showSubmitExamConfirm.value = EXAM_SUBMIT_STATUS.HIDE
  } finally {
    isSubmitting.value = false
  }
}
// ==================== 加载考试数据 ====================
const loadExamData = async (id) => {
  try {
    showLoading('加载考试数据...')

    // 获取考试基本信息
    const examRes = await getExamByExamId({ examId: id })
    if (examRes.code === 200) {
      examInfo.value.startTime = examRes.data.startTime
      examInfo.value.endTime = examRes.data.endTime      // 新增：获取结束时间
      examInfo.value.examDuration = examRes.data.examDuration

      console.log('考试信息加载成功:', {
        startTime: examInfo.value.startTime,
        endTime: examInfo.value.endTime,
        examDuration: examInfo.value.examDuration
      })
    } else {
      ElMessage.error(examRes.message || '获取考试信息失败！')
      return
    }

    // 获取题目详情
    const questionRes = await getExamQuestion({ examId: id })
    if (questionRes.code === 200) {
      questionList.value = questionRes.data.map((item, index) => ({
        ...item,
        isSubmitted: submitStatus.value[item.id] || false,
        shortTitle: item.questionDesc?.substring(0, 30) + '...' || `第${index + 1}题`,
        score: 0
      }))

      // 初始化第一题
      if (questionList.value.length > 0) {
        await switchQuestion(0)
      }
    } else {
      ElMessage.error(questionRes.message || '获取考试题目失败！')
    }
    // 初始化计时器
    initCountDown()
  } catch (error) {
    console.error('加载考试数据失败：', error)
    ElMessage.error('加载考试数据失败')
  } finally {
    closeLoading()
  }
}
// ==================== 全屏相关 ====================
const reenterFullscreen = async (isRetry = false) => {
  if (document.fullscreenElement || isRecovering.value) return true
  const elem = containerRef.value
  if (!elem) return false

  isRecovering.value = true
  try {
    await nextTick()
    if (elem.requestFullscreen) {
      await elem.requestFullscreen()
    } else if (elem.webkitRequestFullscreen) {
      await elem.webkitRequestFullscreen()
    } else if (elem.msRequestFullscreen) {
      await elem.msRequestFullscreen()
    }

    isFullscreen.value = true
    fullscreenRetryCount.value = 0
    isRecovering.value = false
    return true
  } catch (err) {
    if (fullscreenRetryCount.value < maxRetries) {
      fullscreenRetryCount.value++
      isRecovering.value = false
      setTimeout(() => {
        reenterFullscreen(true)
      }, 500)
    } else {
      isRecovering.value = false
      fullscreenRetryCount.value = 0
      ElMessage.warning('无法自动进入全屏，请点击页面任意位置')
    }
    return false
  }
}

const handleFullscreenChange = () => {
  const wasFullscreen = isFullscreen.value
  isFullscreen.value = !!document.fullscreenElement

  if (wasFullscreen && !isFullscreen.value) {
    if (autoSubmitTriggered.value || showSubmitExamConfirm.value === EXAM_SUBMIT_STATUS.SUBMITTED) {
      return
    }

    fullscreenExitCount.value++

    if (fullscreenExitCount.value > maxExitWarnings) {
      ElMessage.error('您已超过3次退出全屏，系统将自动交卷！')
      setTimeout(() => {
        forceSubmitByFullscreenViolation()
      }, 1000)
      return
    }

    recordFullscreenViolation(fullscreenExitCount.value)
    const remainingWarnings = maxExitWarnings - fullscreenExitCount.value
    const warningTail = remainingWarnings > 0
        ? `剩余 ${remainingWarnings} 次提醒机会，超过3次将自动交卷！`
        : '再次退出全屏将自动交卷！'
    ElMessage.warning(`警告：您已退出全屏模式 (${fullscreenExitCount.value}/${maxExitWarnings}次)，${warningTail}`)

    setTimeout(() => {
      reenterFullscreen()
    }, 200)
  }
}

const recordFullscreenViolation = async (count, violationType = '退出全屏/切屏') => {
  const userId = localStorage.getItem('userId')
  if (!examId.value || !userId) return
  try {
    await examApi.logViolation({
      examId: String(examId.value),
      userId: String(userId),
      violationType,
      count: String(count)
    })
  } catch (error) {
    console.error('记录违规失败:', error)
  }
}

const forceSubmitByFullscreenViolation = async () => {
  if (autoSubmitTriggered.value || showSubmitExamConfirm.value === EXAM_SUBMIT_STATUS.SUBMITTED) return
  autoSubmitTriggered.value = true
  stopFullscreenGuard()
  await recordFullscreenViolation(fullscreenExitCount.value, '退出全屏/切屏超过3次，强制交卷')
  await executeAutoSubmit()
}

const handleKeyDown = (e) => {
  if (e.key === 'Escape' || e.keyCode === 27) {
    e.preventDefault()
    e.stopPropagation()
    if (!document.fullscreenElement) {
      setTimeout(() => {
        reenterFullscreen()
      }, 100)
    }
    return false
  }

  if (e.key === 'Tab' || e.keyCode === 9) {
    e.preventDefault()
    e.stopPropagation()
    ElMessage.warning('考试期间禁止使用TAB键')
    return false
  }
}

const handleDocumentClick = () => {
  if (!document.fullscreenElement && !isRecovering.value && !autoSubmitTriggered.value) {
    reenterFullscreen()
  }
}

let fullscreenGuard = null

const startFullscreenGuard = () => {
  if (fullscreenGuard) return
  fullscreenGuard = setInterval(() => {
    if (!document.fullscreenElement && !isRecovering.value) {
      reenterFullscreen()
    }
  }, 2000)
}

const stopFullscreenGuard = () => {
  if (fullscreenGuard) {
    clearInterval(fullscreenGuard)
    fullscreenGuard = null
  }
}

const togglePanelHeight = () => {
  isPanelMax.value = !isPanelMax.value
}

// ==================== 生命周期 ====================
onMounted(async () => {
  title.value = import.meta.env.VITE_APP_TITLE
  version.value = import.meta.env.VITE_APP_VERSION

  examId.value = route.params.id
  await loadExamData(examId.value)
  // 👇 重置自动交卷标志
  autoSubmitTriggered.value = false

  setTimeout(() => {
    reenterFullscreen()
  }, 500)


  startFullscreenGuard()

  document.addEventListener('fullscreenchange', handleFullscreenChange)
  document.addEventListener('keydown', handleKeyDown, { capture: true })
  window.addEventListener('keydown', handleKeyDown, { capture: true })

  document.addEventListener('click', handleDocumentClick)

  // 初始化编辑器
  nextTick(() => {
    initCodeEditor(currentCode.value)
  })
  // 👇 新增：阻止浏览器默认 Ctrl+S/Ctrl+Z
  document.addEventListener('keydown', (e) => {
    // Ctrl+S
    if (e.ctrlKey && e.key === 's') {
      e.preventDefault();
      e.stopPropagation();
      saveCurrentCodeToCache(); // 手动触发保存
    }
    // Ctrl+Z（可选，确保不被浏览器拦截）
    if (e.ctrlKey && e.key === 'z') {
      e.preventDefault();
      e.stopPropagation();
      if (cmEditor) undo(cmEditor); // 手动触发撤销
    }
  }, true); // 捕获阶段触发，优先级更高
})


// 监听倒计时
watch([() => examInfo.value.startTime, () => examInfo.value.examDuration], () => {
  initCountDown()
}, { immediate: true })

onUnmounted(() => {
  stopFullscreenGuard()
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
  document.removeEventListener('keydown', handleKeyDown, { capture: true })
  document.removeEventListener('click', handleDocumentClick)
  window.removeEventListener('keydown', handleKeyDown, { capture: true })
  if (timer) clearInterval(timer)
  if (cmEditor) cmEditor.destroy()
  if (window.syntaxTimeout) clearTimeout(window.syntaxTimeout)
  if (tipTimeout) clearTimeout(tipTimeout)
})
</script>

<style scoped>
/* 全屏时按钮不被遮挡 */
:fullscreen .editor-header {
  position: sticky;
  top: 0;
  z-index: 10;
}
/* 兼容不同浏览器的全屏伪类 */
:-webkit-full-screen .editor-header {
  position: sticky;
  top: 0;
  z-index: 10;
}
.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #1e1e1e;
  border-bottom: 1px solid #333;
}
.editor-header span {
  color: #e0e0e0;
  font-size: 14px;
}
.save-btn {
  padding: 4px 12px;
  background: #2d8cf0;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.save-btn:hover {
  background: #1c7ed6;
}
.save-btn.has-changes span {
  color: #ff4d4f; /* 星号红色提示 */
  font-weight: bold;
  margin-right: 2px;
}
.code-mirror-container {
  height: calc(100% - 40px); /* 适配头部高度 */
}
.exam-doing-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

/* 顶部栏 */
.top-bar {
  width: 100%;
  padding: 12px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-sizing: border-box;
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.system-title {
  font-size: 18px;
  color: #fff;
  font-weight: 500;
  width: 300px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.exam-timer {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(0, 0, 0, 0.3);
  padding: 6px 20px;
  border-radius: 30px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  font-size: 16px;
  backdrop-filter: blur(5px);
}

.timer-icon {
  color: #ffd700;
  font-size: 18px;
}

.top-right-placeholder {
  width: 400px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  display: flex;
  padding: 20px;
  gap: 20px;
  overflow: hidden;
}

/* 左侧面板 */
.left-panel {
  width: 40%;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.question-nav {
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
  border-radius: 12px 12px 0 0;
}

.current-question-info {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.question-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8f9fa;
}

.accepted-badge {
  color: #67c23a;
  font-size: 14px;
  font-weight: 500;
  background: #f0f9eb;
  padding: 4px 10px;
  border-radius: 20px;
}

.question-content {
  padding: 24px;
  flex: 1;
}

.question-content h3 {
  margin: 24px 0 12px;
  font-size: 16px;
  color: #333;
  font-weight: 600;
  border-left: 4px solid #409eff;
  padding-left: 12px;
}

.question-content h3:first-child {
  margin-top: 0;
}

.question-content p {
  margin: 0 0 16px;
  font-size: 14px;
  color: #4a4a4a;
  line-height: 1.8;
}

/* 样例样式 */
.sample {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e8e8e8;
}

.sample-item {
  margin-bottom: 16px;
}

.sample-item:last-child {
  margin-bottom: 0;
}

.sample-label {
  font-size: 12px;
  color: #999;
  display: block;
  margin-bottom: 4px;
  font-weight: 500;
}

.sample-content {
  background: white;
  padding: 10px 14px;
  border-radius: 6px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  color: #333;
  margin: 0 0 10px 0;
  border: 1px solid #e8e8e8;
  overflow-x: auto;
}

.no-sample {
  color: #999;
  font-size: 14px;
  padding: 20px;
  text-align: center;
  background: #f8f9fa;
  border-radius: 8px;
}

/* 右侧面板 */
.right-panel {
  width: 60%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
}

/* 代码卡片 */
.code-card {
  background: #1e1e1e;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  height: 450px;
}

.editor-header {
  padding: 12px 16px;
  background: #2d2d2d;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #3d3d3d;
}

.editor-title {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e0e0e0;
  font-size: 14px;
  font-weight: 500;
}

.editor-title .el-icon {
  color: #409eff;
}

.editor-actions {
  display: flex;
  gap: 8px;
}

.code-editor {
  flex: 1;
  height: calc(100% - 50px);
  overflow: hidden;
}

.editor-disabled {
  opacity: 0.8;
}

/* 语法检查区域 */
.syntax-check {
  background: #2d2d2d;
  border-radius: 8px;
  padding: 12px 16px;
  border: 1px solid #3d3d3d;
  max-height: 150px;
  overflow-y: auto;
}

.syntax-check-title {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #ff6b6b;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 8px;
}

.syntax-errors {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.error-item {
  color: #ff6b6b;
  font-size: 12px;
  padding: 4px 8px;
  font-family: 'Consolas', monospace;
  background: #3d3d3d;
  border-radius: 4px;
  border-left: 3px solid #ff6b6b;
}

/* 结果面板 */
.result-panel {
  background: #2d2d2d;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #3d3d3d;
  margin-top: auto;
}

.panel-header {
  padding: 10px 16px;
  background: #252526;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  border-bottom: 1px solid #3d3d3d;
  transition: background 0.2s;
}

.panel-header:hover {
  background: #2a2a2a;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #e0e0e0;
  font-size: 13px;
  font-weight: 500;
}

.header-left .success {
  color: #4ec9b0;
}

.header-left .error {
  color: #f44747;
}

.toggle-icon {
  color: #888;
  font-size: 16px;
}

.panel-content {
  height: 200px;
  overflow-y: auto;
  transition: height 0.3s ease;
}

.panel-max {
  height: 400px;
}

.result-content {
  color: #dcdcdc;
  font-family: 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
  padding: 16px;
}

/* 题目列表弹窗 */
.question-list-modal {
  position: absolute;
  top: 80px;
  right: 40px;
  width: 350px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
  z-index: 1000;
  max-height: 80vh;
  overflow: hidden;
}

.question-list-header {
  padding: 12px 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-list-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.question-list-content {
  padding: 8px;
  max-height: calc(80vh - 60px);
  overflow-y: auto;
}

.question-list-item {
  padding: 10px 12px;
  border-radius: 6px;
  margin-bottom: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.2s;
}

.question-list-item:hover {
  background: #f0f7ff;
}

.question-list-item.active {
  background: #e6f7ff;
  border: 1px solid #91d5ff;
}

.question-list-item.submitted {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
}

.question-number {
  font-weight: 600;
  color: #409eff;
  margin-right: 8px;
  min-width: 24px;
  text-align: center;
}

.question-title {
  flex: 1;
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.submitted-icon {
  color: #67c23a;
  font-size: 14px;
}

/* 提示框 */
.tip-box {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  border: 1px solid;
}

.tip-box.error {
  color: #f5222d;
  background: #fff1f0;
  border-color: #ffa39e;
}

.tip-box.warning {
  color: #faad14;
  background: #fffbe6;
  border-color: #ffe58f;
}

.tip-box.success {
  color: #52c41a;
  background: #f6ffed;
  border-color: #b7eb8f;
}

/* 提交确认步骤 */
.confirm-step {
  display: flex;
  align-items: center;
  gap: 8px;
}

.confirm-text {
  font-size: 14px;
  color: #333;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #2d2d2d;
}

::-webkit-scrollbar-thumb {
  background: #666;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #888;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .left-panel {
    width: 45%;
  }
  .right-panel {
    width: 55%;
  }
}
</style>
