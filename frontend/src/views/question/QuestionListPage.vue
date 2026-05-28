<template>
  <div class="question-list-container">
    <!-- 顶部筛选栏 -->
    <div class="filter-bar">
      <div class="filter-left">
        <h2 class="page-title">{{ bankTitle }} - 题目列表</h2>
        <!-- 难度筛选 -->
        <div class="filter-group">
          <span class="filter-label">难度：</span>
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
        <!-- 搜索框 -->
        <el-input
            v-model="searchKeyword"
            placeholder="输入关键词模糊查询"
            clearable
            size="small"
            style="width: 240px;"
            @clear="handleSearchClear"
            @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button icon="Search" @click="handleSearch" />
          </template>
        </el-input>
        <!-- 返回题库按钮 -->
        <el-button type="default" size="small" @click="handleBack" style="margin-left: 12px;">
          <el-icon><ArrowLeft /></el-icon>
          返回主页
        </el-button>
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
              <!-- 缩短进度条宽度（占30%） -->
              <el-progress
                  :percentage="Math.round(getValidAcRate(row.AC) * 100)"
                  :color="getAcRateColor(getValidAcRate(row.AC))"
                  :stroke-width="16"
                  style="width: 30%;"
              />
              <!-- 通过率文本 + 练习按钮（占30%） -->
              <div class="ac-rate-actions">
                <!-- 练习题目按钮 -->
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

    <!-- 分页 - 修复：只有当有数据时才显示 -->
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

    <!-- 空状态提示 -->
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
/* 在style标签中添加 */
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
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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
  color: #666;
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 题目列表 */
.question-table-container {
  margin-bottom: 20px;
}

.question-row {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.question-row:hover {
  background-color: #f5f7fa !important;
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
  gap: 8px;
}

.ac-rate-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  width: 30%;
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

/* 响应式适配 */
@media (max-width: 768px) {
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