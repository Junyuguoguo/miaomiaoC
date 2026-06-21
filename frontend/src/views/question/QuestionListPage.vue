<template>
  <div class="question-list-container">
    <section class="list-hero">
      <div>
        <button class="back-link" type="button" @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回题库
        </button>
        <p class="eyebrow">Question Bank</p>
        <h1>{{ bankTitle }} - 题目列表</h1>
        <p>按难度和关键词筛选题目，进入机试练习区后可直接编写并运行 C 语言代码。</p>
      </div>
      <div class="list-hero-stats">
        <div>
          <strong>{{ originalQuestionList.length }}</strong>
          <span>题目总数</span>
        </div>
        <div>
          <strong>{{ filteredQuestionList.length }}</strong>
          <span>当前结果</span>
        </div>
        <div>
          <strong>{{ averageAcPercent }}%</strong>
          <span>平均通过率</span>
        </div>
      </div>
    </section>

    <div class="filter-bar">
      <div class="filter-left">
        <div class="filter-group">
          <span class="filter-label">难度</span>
          <el-button-group>
            <el-button
                size="small"
                :type="currentLevel === 'all' ? 'primary' : 'default'"
                @click="handleLevelChange('all')"
            >全部</el-button>
            <el-button
                size="small"
                :type="currentLevel === 1 ? 'primary' : 'default'"
                @click="handleLevelChange(1)"
            >简单</el-button>
            <el-button
                size="small"
                :type="currentLevel === 2 ? 'primary' : 'default'"
                @click="handleLevelChange(2)"
            >中等</el-button>
            <el-button
                size="small"
                :type="currentLevel === 3 ? 'primary' : 'default'"
                @click="handleLevelChange(3)"
            >困难</el-button>
          </el-button-group>
        </div>
      </div>
      <div class="filter-right">
        <el-input
            v-model="searchKeyword"
            placeholder="搜索题目ID或题目描述"
            clearable
            size="large"
            class="question-search"
            @clear="handleSearchClear"
            @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>
    </div>

    <!-- 题目列表 - 修复：绑定分页后的数据 -->
    <div class="question-table-container">
      <el-table
          :data="paginatedQuestionList"
          style="width: 100%"
          @row-click="handlePractice"
          row-class-name="question-row"
      >
        <el-table-column prop="questionId" label="题目ID" width="120" />
        <!-- 题目列修改 -->
        <el-table-column prop="questionDesc" label="题目" min-width="240">
          <template #default="{ row }">
            <div class="question-title">
              <span class="question-text">{{ row.questionDesc }}</span>
              <el-icon v-if="row.collected" color="#e6a23c"><StarFilled /></el-icon>
              <el-icon v-if="row.wrong" color="#f56c6c"><CircleCloseFilled /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="难度" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(mapLevelToText(row.level))" size="small">
              {{ mapLevelToText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="participantCount" label="提交次数" width="100" />
        <el-table-column prop="acRate" label="AC通过率" min-width="220">
          <template #default="{ row }">
            <div class="ac-rate-container">
              <el-progress
                  :percentage="Math.round(getValidAcRate(row.AC) * 100)"
                  :color="getAcRateColor(getValidAcRate(row.AC))"
                  :stroke-width="12"
              />
              <div class="ac-rate-actions">
                <el-button
                    type="primary"
                    size="small"
                    :icon="EditPen"
                    @click.stop="handlePractice(row)"
                    style="margin-left: 8px;"
                >
                  练习
                </el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-container" v-if="filteredQuestionList.length > 0">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredQuestionList.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>

    <div v-else-if="!loading" class="empty-state">
      <el-empty description="暂无题目数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, StarFilled, CircleCloseFilled, EditPen } from '@element-plus/icons-vue'
import { getAllQuestionList } from '@/api/question-bank.js'

const router = useRouter()
const route = useRoute()

const bankTitle = ref('')  // 新增：题库标题

// 筛选状态 - 修改为数字类型
const currentLevel = ref('all')  // 'all', '1', '2', '3'
const searchKeyword = ref('')
const bankId = ref('')
const loading = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 原始数据
const originalQuestionList = ref([])

// 过滤后数据（前端筛选）
const filteredQuestionList = computed(() => {
  let result = [...originalQuestionList.value]

  // 1. 难度筛选 - level是数字类型
  if (currentLevel.value !== 'all') {
    result = result.filter(item => item.level === currentLevel.value)
  }

  // 2. 模糊搜索（前端）
  if (searchKeyword.value && searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    result = result.filter(item =>
        item.questionId?.toString().toLowerCase().includes(keyword) ||
        item.questionDesc?.toLowerCase().includes(keyword)
    )
  }

  return result
})

// 分页后数据
const paginatedQuestionList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredQuestionList.value.slice(start, end)
})

const averageAcPercent = computed(() => {
  if (!originalQuestionList.value.length) return 0
  const total = originalQuestionList.value.reduce((sum, item) => sum + getValidAcRate(item.AC), 0)
  return Math.round((total / originalQuestionList.value.length) * 100)
})

// 加载题目列表
const loadBankQuestionList = async (forceRefresh = false) => {
  if (!forceRefresh && originalQuestionList.value.length > 0) {
    return
  }

  const userId = localStorage.getItem('userId')
  if (!userId || userId.trim() === '') {
    ElMessage.warning('用户未登录，请先登录')
    return
  }

  loading.value = true
  try {
    const res = await getAllQuestionList({
      userId: userId,
      bankId: bankId.value || ''
    })

    if (res && res.code === 200) {
      originalQuestionList.value = (res.data || []).map(item => {
        return {
          ...item,
          level: item.level ?? '0',  // 确保level是字符串
          AC: isNaN(Number(item.AC)) ? 0 : Number(item.AC)
        }
      })
      console.log('题目列表加载成功，共', originalQuestionList.value.length, '道题目')
      console.log("res",res)
      // 重置分页
      currentPage.value = 1
    } else {
      ElMessage.error(res?.message || '加载题目列表失败')
    }
  } catch (err) {
    console.error('加载题目列表失败:', err)
    ElMessage.error('加载题目列表失败，请稍后重试')
    originalQuestionList.value = []
  } finally {
    loading.value = false
  }
}

// 重置筛选和分页
const resetFiltersAndPagination = () => {
  currentLevel.value = '全部'
  searchKeyword.value = ''
  currentPage.value = 1
  pageSize.value = 10
}

// 难度变化处理
const handleLevelChange = (level) => {
  currentLevel.value = level
  currentPage.value = 1
}

// 搜索处理 - 修复：重置分页到第一页
const handleSearch = () => {
  currentPage.value = 1
}

// 清空搜索处理
const handleSearchClear = () => {
  searchKeyword.value = ''
  currentPage.value = 1
}

// 练习题目按钮点击事件
const handlePractice = (row) => {
  // 跳转到做题页面，携带题目ID
  router.push({
    path: `/question/practiceQuestion/${row.questionId}`
  })
}

// 返回题库
const handleBack = () => {
  router.back()
}

// 分页事件
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 难度映射
const mapLevelToText = (level) => {
  const levelMap = { '0': '入门', '1': '简单', '2': '中等', '3': '困难' }
  return levelMap[level] || '入门'
}

const getLevelTagType = (level) => {
  const levelMap = { '简单': 'success', '中等': 'warning', '困难': 'danger', '入门': 'info' }
  return levelMap[level] || 'info'
}

const getValidAcRate = (rate) => {
  const numRate = Number(rate)
  return isNaN(numRate) ? 0 : Math.max(0, Math.min(1, numRate))
}

// 通过率颜色映射
const getAcRateColor = (rate) => {
  if (rate >= 0.6) return '#409eff'
  if (rate >= 0.4) return '#e6a23c'
  return '#f56c6c'
}

// 组件挂载
onMounted(async () => {
  // 从路由参数中获取题库ID
  // id 从 params 或 query 获取
  bankId.value = route.params.id || route.query.id || ''
  // title 从 query 获取
  bankTitle.value = route.query.title || '题目列表'
  await loadBankQuestionList()
})
</script>

<style scoped>
.question-title {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.question-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
  max-width: 100%;
}

/* 针对表格单元格的全局样式 */
:deep(.el-table .cell) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.question-list-container {
  min-height: 100vh;
  padding: 28px;
  background:
      radial-gradient(circle at 18% 8%, rgba(37, 99, 235, 0.10), transparent 30%),
      radial-gradient(circle at 88% 0%, rgba(124, 92, 255, 0.09), transparent 26%),
      linear-gradient(180deg, #f8fbff 0%, #f3f7fe 48%, #edf4fb 100%);
  color: var(--app-text);
}

.list-hero {
  min-height: 210px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 360px);
  gap: 24px;
  align-items: center;
  padding: 32px;
  margin-bottom: 18px;
  background:
      linear-gradient(120deg, rgba(255, 255, 255, 0.94), rgba(245, 248, 255, 0.84)),
      radial-gradient(circle at 82% 46%, rgba(124, 92, 255, 0.20), transparent 36%);
  border: 1px solid rgba(207, 220, 240, 0.88);
  border-radius: 8px;
  box-shadow: 0 18px 42px rgba(40, 78, 142, 0.09);
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 18px;
  padding: 0;
  color: var(--app-primary);
  background: transparent;
  border: 0;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}

.eyebrow {
  margin: 0 0 8px;
  color: var(--app-primary);
  font-size: 12px;
  font-weight: 850;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.list-hero h1 {
  margin: 0;
  color: var(--app-text);
  font-size: clamp(30px, 4vw, 46px);
  font-weight: 950;
  line-height: 1.15;
  letter-spacing: 0;
}

.list-hero p {
  max-width: 680px;
  margin: 14px 0 0;
  color: var(--app-text-muted);
  font-size: 15px;
  line-height: 1.75;
}

.list-hero-stats {
  display: grid;
  gap: 12px;
}

.list-hero-stats div {
  min-height: 76px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(207, 220, 240, 0.86);
  border-radius: 8px;
  box-shadow: 0 12px 28px rgba(40, 78, 142, 0.06);
}

.list-hero-stats strong {
  color: var(--app-text);
  font-size: 30px;
  font-weight: 900;
  font-variant-numeric: tabular-nums;
}

.list-hero-stats span {
  color: var(--app-text-muted);
  font-size: 13px;
  font-weight: 750;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 18px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(207, 220, 240, 0.88);
  border-radius: 8px;
  box-shadow: 0 14px 34px rgba(40, 78, 142, 0.08);
  flex-wrap: wrap;
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  color: var(--app-text-muted);
  font-weight: 800;
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.question-table-container {
  margin-bottom: 20px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(207, 220, 240, 0.88);
  border-radius: 8px;
  box-shadow: 0 14px 34px rgba(40, 78, 142, 0.08);
  overflow: hidden;
}

.question-row {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.question-row:hover {
  background-color: #f4f8ff !important;
}

.question-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* AC通过率 */
.ac-rate-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.ac-rate-container :deep(.el-progress) {
  flex: 1;
  min-width: 120px;
}

.ac-rate-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex: 0 0 auto;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 16px 4px 0;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

@media (max-width: 768px) {
  .question-list-container {
    padding: 16px;
  }

  .list-hero {
    grid-template-columns: 1fr;
    padding: 22px;
  }

  .filter-bar {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .filter-right {
    width: 100%;
    justify-content: space-between;
  }

  .question-search {
    width: 100%;
  }

  .ac-rate-container {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .ac-rate-actions {
    width: 100%;
    justify-content: flex-start;
    margin-top: 4px;
  }
}
</style>
