<template>
  <div class="practice-doing-container" ref="containerRef">
    <!-- 顶部栏 -->
    <div class="top-bar">
      <div class="system-title">{{ title }}@{{ version }}</div>
      <div class="top-right-placeholder">
        <!-- 新增：返回题目列表按钮 -->
        <el-button
            type="default"
            size="small"
            @click="goBackQuestionList"
            style="margin-right: 8px"
        >
          返回
        </el-button>
        <!-- 提交本题相关操作 -->
        <div class="question-actions">
          <!-- 初始状态：显示提交按钮 -->
          <template v-if="showSubmitConfirm === 0">
            <el-button type="primary" size="small" @click="triggerSubmitConfirm" :loading="isSubmitting">
              提交本题
            </el-button>
          </template>
          <!-- 确认提交本题：显示是/否按钮 -->
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
    </div>

    <!-- 主内容区域：左右分栏 -->
    <div class="main-content">
      <!-- 左侧：题目描述 -->
      <div class="left-panel">
        <div class="question-header">
          <div class="accepted-badge">
            得分: {{ currentQuestionScore }}/{{ currentQuestion.fullScore || 100 }}分
          </div>
          <div v-if="submitTipText" class="tip-box" :class="submitTipType">
            {{ submitTipText }}
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
            <!-- 保存按钮（带星号提示代码未保存） -->
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
import { ElMessage, ElLoading } from 'element-plus'
// Element Plus 图标
import {
  Document, Warning, CircleCheck, CircleClose,
  ArrowDown, ArrowUp
} from '@element-plus/icons-vue'
// 接口（假设均存在，沿用原定义）
import { getQuestionById, submitQuestion, runTestCode } from "@/api/question-bank.js"
// CodeMirror 相关导入
import { StateEffect } from '@codemirror/state'
import { EditorState } from '@codemirror/state'
import { EditorView, lineNumbers, highlightActiveLine, keymap } from '@codemirror/view'
import { cpp } from '@codemirror/lang-cpp'
import { linter, lintGutter } from '@codemirror/lint'
import { syntaxTree } from '@codemirror/language'
import { oneDark } from '@codemirror/theme-one-dark'
import { defaultKeymap, indentWithTab, undo, redo } from '@codemirror/commands'
import { autocompletion, completionKeymap } from '@codemirror/autocomplete'

const router = useRouter()
const route = useRoute()
const containerRef = ref(null)
const editorRef = ref(null)
let cmEditor = null // CodeMirror 实例

// 路由参数：单题ID
const questionId = ref(route.params.id)

// 当前题目相关
const currentQuestion = ref({})
const currentCode = ref('')
const currentQuestionScore = ref(0)
const executeTime = ref('0ms')
const codeCache = ref({}) // 代码缓存
const runResultCache = ref({}) // 运行结果缓存
const scoreCache = ref({}) // 分数缓存
const executeTimeCache = ref({}) // 执行时间缓存

// 运行/提交状态
const isLoading = ref(false)
const isSubmitting = ref(false)
const syntaxErrors = ref([])
const runResult = ref(null)
const showSubmitConfirm = ref(0) // 提交确认步骤
const SUBMIT_STATUS = {
  HIDE: 0,
  CONFIRM: 1,
  SUBMITTED: 2,
  DOING: 3
}
const currentSubmitQuestionId = ref('')
const title = ref('')
const version = ref('')

// 提示文本
const submitTipText = ref('')
const submitTipType = ref('')
let tipTimeout = null

// CodeMirror 配置
const isHighlightEnabled = ref(true) // 语法高亮开关
const isCodeChanged = ref(false); // 代码未保存标记
const isPanelMax = ref(false) // 结果面板展开状态




// ==================== CodeMirror 核心配置（原逻辑完全保留） ====================
// 纯色主题-机考环境（无高亮）
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
  '.cm-gutter': {
    backgroundColor: '#1e1e1e',
    color: '#6e6e6e',
  },
  '.cm-lineNumbers': {
    color: '#6e6e6e',
  },
  '.cm-activeLineGutter': {
    backgroundColor: '#2d2d2d',
    color: '#a0a0a0',
  },
  '.cm-comment': { color: '#888888' },
  '.cm-error': { color: '#f44747' },
  '.cm-tag,.cm-attribute,.cm-string,.cm-number,.cm-variable': { color: '#e0e0e0' }
})

// C语言语法检查器
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

// C语言自动补全关键字
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
    options: cKeywords.map(key => ({ label: key, type: 'keyword', boost: 1 }))
  }
}

// 获取CodeMirror扩展配置
const getBaseExtensions = (isReadOnly = false, enableHighlight = true) => {
  const extensions = [
    lineNumbers(),
    highlightActiveLine(),
    cpp(),
    lintGutter(),
    cLinter,
    ...(enableHighlight ? [autocompletion({ override: [cCompletion] })] : []),
    keymap.of([
      ...defaultKeymap,
      ...(enableHighlight ? completionKeymap : []),
      indentWithTab,
      { key: 'Ctrl-z', run: undo, preventDefault: true, stop: true },
      { key: 'Ctrl-y', run: redo, preventDefault: true, stop: true },
      { key: 'Cmd-z', run: undo, preventDefault: true, stop: true },
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
  enableHighlight ? extensions.push(oneDark) : extensions.push(plainTheme)
  return extensions
}

// 新增：防抖锁
let isSaving = false;

const saveCurrentCodeToCache = () => {
  // 防抖：同一时间只执行一次
  if (isSaving) return;
  isSaving = true;

  setTimeout(() => {
    try {
      const qid = currentQuestion.value?.id || questionId.value;
      if (!qid) {
        console.warn('保存失败：未获取到题目ID');
        ElMessage.info('⚠️ 题目数据加载中，请稍候再保存');
        return;
      }
      codeCache.value[qid] = currentCode.value;
      isCodeChanged.value = false;
      ElMessage.success(`✅ 题目 ${qid} 代码已保存`);
    } finally {
      isSaving = false; // 释放锁
    }
  }, 200); // 200ms防抖，避免连续触发
};

// 保存按钮点击事件
const handleSaveClick = () => {
  saveCurrentCodeToCache();
};

// 监听代码变化，标记未保存
watch(currentCode, (newVal, oldVal) => {
  if (newVal !== oldVal && newVal !== undefined && oldVal !== undefined) {
    isCodeChanged.value = true;
  }
});

// 更新编辑器状态
const updateEditorState = (newCode, isReadOnly, enableHighlight = isHighlightEnabled.value) => {
  if (!cmEditor) return
  cmEditor.dispatch({ changes: { from: 0, to: cmEditor.state.doc.length, insert: newCode } })
  const extensions = getBaseExtensions(isReadOnly, enableHighlight)
  cmEditor.dispatch({ effects: StateEffect.reconfigure.of(extensions) })
}

// 初始化CodeMirror编辑器
const initCodeEditor = (initialCode = '') => {
  if (cmEditor) {
    cmEditor.destroy()
    cmEditor = null
  }
  if (!editorRef.value) return
  const extensions = getBaseExtensions(currentQuestion.value?.isSubmitted || false, isHighlightEnabled.value)
  const state = EditorState.create({ doc: initialCode, extensions: extensions })
  cmEditor = new EditorView({ state: state, parent: editorRef.value })
}

// 切换语法高亮/机考环境
const toggleSyntaxHighlight = () => {
  if (currentQuestion.value?.isSubmitted) {
    ElMessage.warning('本题已提交，无法修改代码样式')
    return
  }
  isHighlightEnabled.value = !isHighlightEnabled.value
  if (cmEditor) {
    const newExtensions = getBaseExtensions(currentQuestion.value?.isSubmitted || false, isHighlightEnabled.value)
    cmEditor.dispatch({ effects: StateEffect.reconfigure.of(newExtensions) })
  }
  ElMessage.success(isHighlightEnabled.value ? '已启用VsCode语法高亮' : '已切换为机考无高亮环境')
}

// ==================== C语言语法检查（原逻辑完全保留） ====================
const checkSyntax = async () => {
  try {
    syntaxErrors.value = [];
    const code = currentCode.value.trim();
    if (!code) {
      syntaxErrors.value.push('错误：代码不能为空');
      return;
    }
    const lines = code.split('\n');
    // 1. 检查main函数
    const mainRegex = /\bint\s+main\s*\(/i;
    if (!mainRegex.test(code)) {
      syntaxErrors.value.push('错误：缺少main函数（建议格式：int main()）');
    }
    // 2. 括号匹配检查
    const bracketStack = [];
    let lineNum = 1;
    let inString = false;
    let inComment = false;
    for (let i = 0; i < code.length; i++) {
      const char = code[i];
      const nextChar = code[i + 1];
      if (char === '\n') { lineNum++; continue; }
      if (char === '/' && nextChar === '*' && !inString) { inComment = true; i++; continue; }
      if (char === '*' && nextChar === '/' && inComment) { inComment = false; i++; continue; }
      if (inComment) continue;
      if (char === '/' && nextChar === '/') { while (i < code.length && code[i] !== '\n') i++; continue; }
      if (char === '"' && !inComment) inString = !inString;
      if (inString) continue;
      if (['{', '(', '['].includes(char)) { bracketStack.push({ char, line: lineNum }); }
      else if (['}', ')', ']'].includes(char)) {
        const matchChar = char === '}' ? '{' : char === ')' ? '(' : '[';
        const last = bracketStack.pop();
        if (!last || last.char !== matchChar) {
          syntaxErrors.value.push(`错误：第${lineNum}行 多余的"${char}"`);
        }
      }
    }
    bracketStack.forEach(item => {
      syntaxErrors.value.push(`错误：第${item.line}行 "${item.char}" 未闭合`);
    });
    // 3. 分号检查
    lines.forEach((line, idx) => {
      const lineNumber = idx + 1;
      const cleanLine = line.replace(/".*?"/g, '').replace(/\/\/.*/, '').trim();
      if (cleanLine.includes(';;')) {
        syntaxErrors.value.push(`警告：第${lineNumber}行 存在多余分号;;`);
      }
      if (/if\s*\(.*\)\s*;|for\s*\(.*\)\s*;|while\s*\(.*\)\s*;/g.test(cleanLine)) {
        syntaxErrors.value.push(`警告：第${lineNumber}行 条件语句后多余分号`);
      }
    });
    // 4. 关键字拼写检查
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

// ==================== 加载遮罩（原逻辑保留） ====================
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
  if (loadingInstance) { loadingInstance.close(); loadingInstance = null; }
}

// ==================== 提示方法（原逻辑保留） ====================
const showSubmitTip = (text, type = 'warning', duration = 3000) => {
  if (tipTimeout) clearTimeout(tipTimeout)
  submitTipText.value = text
  submitTipType.value = type
  if (duration > 0) {
    tipTimeout = setTimeout(() => { submitTipText.value = '' }, duration)
  }
}
const clearSubmitTip = () => {
  submitTipText.value = ''
  if (tipTimeout) { clearTimeout(tipTimeout); tipTimeout = null; }
}

// ==================== 运行代码（原逻辑完全保留） ====================
const runCode = async () => {
  await checkSyntax()
  const hasError = syntaxErrors.value.some(e => e.startsWith('错误：'))
  if (hasError) {
    ElMessage.error('代码存在语法错误，请先修复')
    return
  }
  if (currentQuestion.value?.isSubmitted) {
    ElMessage.warning('本题已提交，禁止再次运行')
    return
  }
  showLoading('正在运行测试用例...')
  try {
    const runData = {
      code: String(currentCode.value || ''),
      questionId: questionId.value
    }
    const result = await runTestCode(runData)
    if (result && result.code === 200) {
      console.log('result',result)
      const realResult = {
        success: result.data?.passCount === result.data?.totalCount,
        output: `=== 🧪 代码运行结果 ===
📌 测试状态：${result.data?.message || '运行成功'}
📊 整体统计：
  • 通过用例：${result.data?.passCount || 0}/${result.data?.totalCount || 0}
  • 通过率：${result.data?.passRate || '0%'}
  • 总得分：${result.data?.score || 0} 分
  • 总耗时：${result.data?.usedTime || 0} ms

📝 用例执行详情：
${result.data?.testSampleRecord?.length
            ? result.data.testSampleRecord.map((item, index) => {
              // 格式化单条用例信息，替换换行符为缩进
              return `
${item.replace(/\n/g, '\n    ')}`;
            }).join('\n')
            : '  暂无测试用例执行记录'
        }

====================`
      };

      runResult.value = realResult
      if (currentQuestion.value?.id) {
        runResultCache.value[currentQuestion.value.id] = realResult
      }
    } else {
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

// ==================== 提交题目（原逻辑完全保留） ====================
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
  if (!questionId.value) {
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
    const submitData = {
      userId: String(userId),
      questionId: String(questionId.value || ''),
      code: String(currentCode.value || '')
    }
    const result = await submitQuestion(submitData)
    if (result && result.code === 200) {
      const qid = questionId.value
      currentQuestion.value.isSubmitted = true
      currentQuestionScore.value = result.data?.score || 0
      executeTime.value = (result.data?.usedTime || 0) + 'ms'
      const realResult = {
        success: result.data?.passCount === result.data?.totalCount,
        output: `=== 🧪 代码运行结果 ===
📌 测试状态：${result.data?.message || '运行成功'}
📊 整体统计：
  • 通过用例：${result.data?.passCount || 0}/${result.data?.totalCount || 0}
  • 通过率：${result.data?.passRate || '0%'}
  • 总得分：${result.data?.score || 0} 分
  • 总耗时：${result.data?.usedTime || 0} ms

📝 用例执行详情：
${result.data?.testSampleRecord?.length
            ? result.data.testSampleRecord.map((item, index) => {
              // 格式化单条用例信息，替换换行符为缩进
              return `
${item.replace(/\n/g, '\n    ')}`;
            }).join('\n')
            : '  暂无测试用例执行记录'
        }

====================`
      };

      runResult.value = realResult

      // 缓存结果
      runResultCache.value[qid] = runResult.value
      scoreCache.value[qid] = result.data?.score || 0
      executeTimeCache.value[qid] = executeTime.value
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

// ==================== 新增：返回题目列表 ====================
const goBackQuestionList = () => {
  // 可根据实际路由调整，此处默认返回上一级/练习列表页
  router.back()
  // 若需指定路由，替换为：router.push('/practice/question-list')
}

// ==================== 加载题目数据（适配单题） ====================
const loadQuestionData = async (id) => {
  try {
    showLoading('加载题目数据...')
    // 获取单题详情（新接口：getQuestionById）
    const questionRes = await getQuestionById({ questionId: id })
    if (questionRes.code === 200) {
      console.log('questionRes',questionRes)
      // 格式化题目数据，与原逻辑保持一致
      const question = {
        ...questionRes.data,
        isSubmitted: false,
        shortTitle: questionRes.data.questionDesc?.substring(0, 30) + '...' || '当前练习题目'
      }
      currentQuestion.value = question
      currentCode.value = codeCache.value[id] || ''
      runResult.value = runResultCache.value[id] || null
      currentQuestionScore.value = scoreCache.value[id] || 0
      executeTime.value = executeTimeCache.value[id] || '0ms'
      showSubmitConfirm.value = question.isSubmitted ? SUBMIT_STATUS.SUBMITTED : SUBMIT_STATUS.HIDE
      // 初始化编辑器
      await nextTick()
      initCodeEditor(currentCode.value)
      // 语法检查
      await checkSyntax()
    } else {
      ElMessage.error(questionRes.message || '获取题目详情失败！')
    }
  } catch (error) {
    console.error('加载题目数据失败：', error)
    ElMessage.error('加载题目数据失败，请刷新页面')
  } finally {
    closeLoading()
  }
}

// ==================== 结果面板展开/收起 ====================
const togglePanelHeight = () => {
  isPanelMax.value = !isPanelMax.value
}

// ==================== 生命周期（移除全屏/计时相关） ====================
onMounted(async () => {
  title.value = import.meta.env.VITE_APP_TITLE
  version.value = import.meta.env.VITE_APP_VERSION

  if (!questionId.value) {
    ElMessage.error('缺少题目ID，请选择正确题目')
    goBackQuestionList()
    return
  }
  // 加载单题数据
  await loadQuestionData(questionId.value)

  // 修复：移除全局Ctrl+S监听，只保留编辑器内部的快捷键
  document.addEventListener('keydown', (e) => {
    if (e.ctrlKey && e.key === 'z') { // 只保留Ctrl+Z
      e.preventDefault(); e.stopPropagation(); if (cmEditor) undo(cmEditor);
    }
  }, true);
})

onUnmounted(() => {
  // 销毁编辑器、清除定时器
  if (cmEditor) cmEditor.destroy()
  if (window.syntaxTimeout) clearTimeout(window.syntaxTimeout)
  if (tipTimeout) clearTimeout(tipTimeout)
  // 移除事件监听
  document.removeEventListener('keydown', () => {}, true)
})
</script>

<style scoped>
/* 基础容器 */
.practice-doing-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

/* 顶部栏（简化，移除计时） */
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
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}
.top-right-placeholder {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  align-items: center;
}

/* 主内容区域（原样式保留） */
.main-content {
  flex: 1;
  display: flex;
  padding: 20px;
  gap: 20px;
  overflow: hidden;
}

/* 左侧题目面板（移除上下题按钮，简化样式） */
.left-panel {
  width: 40%;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}
.question-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8f9fa;
  flex-wrap: wrap;
  gap: 10px;
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

/* 样例样式（原样式保留） */
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

/* 右侧编程面板（原样式完全保留） */
.right-panel {
  width: 60%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
}
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
  flex-wrap: wrap;
  gap: 10px;
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
  color: #ff4d4f;
  font-weight: bold;
  margin-right: 2px;
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

/* 语法检查（原样式保留） */
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

/* 结果面板（原样式保留） */
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

/* 提示框+提交确认（原样式保留） */
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
.confirm-step {
  display: flex;
  align-items: center;
  gap: 8px;
}
.confirm-text {
  font-size: 14px;
  color: #333;
}

/* 滚动条样式（原样式保留） */
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

/* 响应式调整（原样式保留） */
@media (max-width: 1200px) {
  .left-panel {
    width: 45%;
  }
  .right-panel {
    width: 55%;
  }
}
@media (max-width: 992px) {
  .main-content {
    flex-direction: column;
  }
  .left-panel, .right-panel {
    width: 100%;
  }
  .code-card {
    height: 350px;
  }
}
</style>