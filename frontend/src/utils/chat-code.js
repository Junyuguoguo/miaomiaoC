const CODE_EXTENSIONS = {
  c: 'C',
  cpp: 'C++',
  h: 'C/C++',
  hpp: 'C++',
  java: 'Java',
  py: 'Python',
  js: 'JavaScript',
  ts: 'TypeScript',
  html: 'HTML',
  css: 'CSS',
  json: 'JSON',
  xml: 'XML',
  sql: 'SQL',
  sh: 'Shell',
  go: 'Go',
  rs: 'Rust',
  txt: 'Text'
}

export const CODE_PREVIEW_LIMIT = 60000

export function getFileExtension(name = '') {
  const cleanName = String(name).split('?')[0].split('#')[0]
  const dotIndex = cleanName.lastIndexOf('.')
  if (dotIndex < 0) return ''
  return cleanName.slice(dotIndex + 1).toLowerCase()
}

export function isCodeFileName(name = '') {
  return Boolean(CODE_EXTENSIONS[getFileExtension(name)])
}

export function getCodeLanguage(msg = {}) {
  const ext = getFileExtension(msg.fileName || msg.content || '')
  return CODE_EXTENSIONS[ext] || 'Code'
}

export function getCodeTitle(msg = {}) {
  if (msg.fileName) return msg.fileName
  const content = String(msg.content || '')
  if (content.startsWith('/uploads/')) return content.split('/').pop() || 'code'
  return '代码片段'
}

export function isDownloadableCode(msg = {}) {
  return typeof msg.content === 'string' && msg.content.startsWith('/uploads/')
}

export function looksLikeCode(content = '') {
  const text = String(content).trim()
  if (text.length < 24) return false

  let score = 0
  if (text.includes('\n')) score += 2
  if (/[{};]|=>|->|#include|<\/?[a-z][\s\S]*>/i.test(text)) score += 2
  if (/\b(class|def|function|const|let|var|return|import|public|private|while|for|if|else)\b/.test(text)) score += 2
  if (/\b(Optional|ListNode|String|Integer|void|boolean|print|console\.log)\b/.test(text)) score += 1
  if (/^\s*(#|\/\/|\/\*|\*)/m.test(text)) score += 1
  if ((text.match(/[()[\]{}]/g) || []).length >= 4) score += 1

  return score >= 3
}

export function shouldRenderCodeMessage(msg = {}) {
  if (msg.messageType === 'CODE') return true
  if (msg.messageType && msg.messageType !== 'TEXT') return false
  return looksLikeCode(msg.content)
}

export function getCodeDisplayContent(msg = {}) {
  if (msg.codeContent) return msg.codeContent
  if (isDownloadableCode(msg)) return ''
  return String(msg.content || '')
}

export async function readCodeFilePreview(file) {
  if (!file || !isCodeFileName(file.name)) return ''
  const text = await file.text()
  return text.length > CODE_PREVIEW_LIMIT
    ? `${text.slice(0, CODE_PREVIEW_LIMIT)}\n\n/* 预览已截断，下载文件查看完整内容 */`
    : text
}
