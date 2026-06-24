# 教师端数据统计页面视觉优化实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在教师端数据统计页面添加ECharts图表，提升数据可视化效果

**Architecture:** 在现有TeacherManagerPage.vue中引入ECharts，添加饼图、柱状图、折线图三个图表，数据从学生列表中计算

**Tech Stack:** Vue 3, ECharts 6.1.0, Element Plus

## Global Constraints

- 使用现有的ECharts引入方式（参考AdminPage.vue）
- 遵循现有的Element Plus组件风格
- 使用现有的teacher-stats API接口
- 保持现有表格功能不变

---

### Task 1: 引入ECharts并添加图表容器

**Files:**
- Modify: `frontend/src/views/teacher/TeacherManagerPage.vue`

**Interfaces:**
- Consumes: `studentList` - 现有的学生列表数据
- Produces: `gradeChartRef`, `questionChartRef`, `activityChartRef` - 图表容器引用

- [ ] **Step 1: 引入ECharts**

在TeacherManagerPage.vue的script部分，找到import语句，添加ECharts引入：

```javascript
import * as echarts from 'echarts/core'
import { BarChart, PieChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([BarChart, PieChart, LineChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent, CanvasRenderer])
```

- [ ] **Step 2: 添加图表容器引用**

在script部分添加图表容器引用：

```javascript
const gradeChartRef = ref(null)
const questionChartRef = ref(null)
const activityChartRef = ref(null)
let gradeChartInstance = null
let questionChartInstance = null
let activityChartInstance = null
```

- [ ] **Step 3: 添加图表容器到模板**

在概览卡片和表格之间添加图表容器：

```html
<!-- 图表区域 -->
<el-row :gutter="20" style="margin-top:20px">
  <el-col :span="12">
    <el-card shadow="never" class="chart-card">
      <template #header>
        <span>学生成绩分布</span>
      </template>
      <div ref="gradeChartRef" class="chart-container"></div>
    </el-card>
  </el-col>
  <el-col :span="12">
    <el-card shadow="never" class="chart-card">
      <template #header>
        <span>做题数分布</span>
      </template>
      <div ref="questionChartRef" class="chart-container"></div>
    </el-card>
  </el-col>
</el-row>

<el-row :gutter="20" style="margin-top:20px">
  <el-col :span="24">
    <el-card shadow="never" class="chart-card">
      <template #header>
        <span>学习活跃度趋势（近7天）</span>
      </template>
      <div ref="activityChartRef" class="chart-container"></div>
    </el-card>
  </el-col>
</el-row>
```

- [ ] **Step 4: 添加图表样式**

在style部分添加图表样式：

```css
.chart-card {
  margin-bottom: 0;
}

.chart-container {
  height: 300px;
  width: 100%;
}
```

- [ ] **Step 5: Commit**

```bash
git add frontend/src/views/teacher/TeacherManagerPage.vue
git commit -m "feat: add ECharts imports and chart containers to teacher stats page"
```

---

### Task 2: 添加数据计算函数

**Files:**
- Modify: `frontend/src/views/teacher/TeacherManagerPage.vue`

**Interfaces:**
- Consumes: `studentList` - 现有的学生列表数据
- Produces: `calculateGradeDistribution()`, `calculateQuestionDistribution()`, `calculateActivityData()` - 数据计算函数

- [ ] **Step 1: 添加成绩分布计算函数**

在script部分添加成绩分布计算函数：

```javascript
const calculateGradeDistribution = (students) => {
  let excellent = 0, good = 0, fail = 0, noData = 0
  students.forEach(s => {
    if (s.questionCount === 0) {
      noData++
    } else if (s.questionPassRate >= 80) {
      excellent++
    } else if (s.questionPassRate >= 60) {
      good++
    } else {
      fail++
    }
  })
  return { excellent, good, fail, noData }
}
```

- [ ] **Step 2: 添加做题数分布计算函数**

在script部分添加做题数分布计算函数：

```javascript
const calculateQuestionDistribution = (students) => {
  let count0 = 0, count10 = 0, count20 = 0, count50 = 0, count50plus = 0
  students.forEach(s => {
    const q = s.questionCount
    if (q === 0) count0++
    else if (q <= 10) count10++
    else if (q <= 20) count20++
    else if (q <= 50) count50++
    else count50plus++
  })
  return { count0, count10, count20, count50, count50plus }
}
```

- [ ] **Step 3: 添加活跃度数据计算函数**

在script部分添加活跃度数据计算函数（从学生最近活跃时间统计）：

```javascript
const calculateActivityData = (students) => {
  // 从学生最近活跃时间统计近7天活跃度
  const today = new Date()
  const days = []
  const counts = []
  
  for (let i = 6; i >= 0; i--) {
    const date = new Date(today)
    date.setDate(date.getDate() - i)
    const dateStr = date.toISOString().split('T')[0]
    days.push(dateStr.slice(5)) // MM-DD格式
    
    // 统计该日期有活跃的学生数
    const count = students.filter(s => {
      if (!s.lastActiveTime) return false
      const activeDate = new Date(s.lastActiveTime).toISOString().split('T')[0]
      return activeDate === dateStr
    }).length
    counts.push(count)
  }
  
  return { days, counts }
}
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/teacher/TeacherManagerPage.vue
git commit -m "feat: add chart data calculation functions"
```

---

### Task 3: 添加图表渲染函数

**Files:**
- Modify: `frontend/src/views/teacher/TeacherManagerPage.vue`

**Interfaces:**
- Consumes: `calculateGradeDistribution()`, `calculateQuestionDistribution()`, `calculateActivityData()` - 来自Task 2
- Produces: `renderGradeChart()`, `renderQuestionChart()`, `renderActivityChart()` - 图表渲染函数

- [ ] **Step 1: 添加成绩分布饼图渲染函数**

在script部分添加成绩分布饼图渲染函数：

```javascript
const renderGradeChart = () => {
  if (!gradeChartRef.value || studentList.value.length === 0) return
  if (gradeChartInstance && !gradeChartInstance.isDisposed()) {
    gradeChartInstance.dispose()
  }
  gradeChartInstance = echarts.init(gradeChartRef.value)
  const { excellent, good, fail, noData } = calculateGradeDistribution(studentList.value)
  
  gradeChartInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: { bottom: '0', left: 'center' },
    series: [{
      name: '成绩分布',
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%' },
      data: [
        { value: excellent, name: '优秀' },
        { value: good, name: '良好' },
        { value: fail, name: '不及格' },
        { value: noData, name: '未做题' }
      ],
      color: ['#67c23a', '#409eff', '#e6a23c', '#dcdfe6']
    }]
  })
}
```

- [ ] **Step 2: 添加做题数分布柱状图渲染函数**

在script部分添加做题数分布柱状图渲染函数：

```javascript
const renderQuestionChart = () => {
  if (!questionChartRef.value || studentList.value.length === 0) return
  if (questionChartInstance && !questionChartInstance.isDisposed()) {
    questionChartInstance.dispose()
  }
  questionChartInstance = echarts.init(questionChartRef.value)
  const { count0, count10, count20, count50, count50plus } = calculateQuestionDistribution(studentList.value)
  
  questionChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
    xAxis: { type: 'category', data: ['0题', '1-10题', '11-20题', '21-50题', '50+题'] },
    yAxis: { type: 'value', name: '学生数' },
    series: [{
      name: '学生数',
      type: 'bar',
      data: [count0, count10, count20, count50, count50plus],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409eff' },
          { offset: 1, color: '#79bbff' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      barMaxWidth: 40
    }]
  })
}
```

- [ ] **Step 3: 添加活跃度折线图渲染函数**

在script部分添加活跃度折线图渲染函数：

```javascript
const renderActivityChart = () => {
  if (!activityChartRef.value || studentList.value.length === 0) return
  if (activityChartInstance && !activityChartInstance.isDisposed()) {
    activityChartInstance.dispose()
  }
  activityChartInstance = echarts.init(activityChartRef.value)
  const { days, counts } = calculateActivityData(studentList.value)
  
  activityChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
    xAxis: { type: 'category', data: days },
    yAxis: { type: 'value', name: '活跃学生数' },
    series: [{
      name: '活跃学生数',
      type: 'line',
      data: counts,
      smooth: true,
      itemStyle: { color: '#409eff' },
      areaStyle: { color: 'rgba(64, 158, 255, 0.1)' }
    }]
  })
}
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/teacher/TeacherManagerPage.vue
git commit -m "feat: add chart rendering functions for grade, question, and activity charts"
```

---

### Task 4: 调用图表渲染并添加窗口resize处理

**Files:**
- Modify: `frontend/src/views/teacher/TeacherManagerPage.vue`

**Interfaces:**
- Consumes: `renderGradeChart()`, `renderQuestionChart()`, `renderActivityChart()` - 来自Task 3
- Produces: 图表在页面加载时自动渲染，窗口resize时自动调整

- [ ] **Step 1: 在loadTeacherStudents中调用图表渲染**

修改loadTeacherStudents函数，在数据加载成功后调用图表渲染：

```javascript
const loadTeacherStudents = async () => {
  studentLoading.value = true
  try {
    const res = await getTeacherStatsStudents({ keyword: studentKeyword.value || undefined, page: studentPage.value - 1, size: studentPageSize.value })
    if (res && res.code === 200) {
      studentList.value = res.data.students || []
      studentTotal.value = res.data.total || 0
      
      // 渲染图表
      await nextTick()
      renderGradeChart()
      renderQuestionChart()
      renderActivityChart()
    }
  } catch (e) { console.error('加载学生列表失败:', e) }
  finally { studentLoading.value = false }
}
```

- [ ] **Step 2: 添加窗口resize监听**

在script部分添加窗口resize监听：

```javascript
const handleResize = () => {
  if (gradeChartInstance && !gradeChartInstance.isDisposed()) {
    gradeChartInstance.resize()
  }
  if (questionChartInstance && !questionChartInstance.isDisposed()) {
    questionChartInstance.resize()
  }
  if (activityChartInstance && !activityChartInstance.isDisposed()) {
    activityChartInstance.resize()
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (gradeChartInstance && !gradeChartInstance.isDisposed()) {
    gradeChartInstance.dispose()
  }
  if (questionChartInstance && !questionChartInstance.isDisposed()) {
    questionChartInstance.dispose()
  }
  if (activityChartInstance && !activityChartInstance.isDisposed()) {
    activityChartInstance.dispose()
  }
})
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/teacher/TeacherManagerPage.vue
git commit -m "feat: integrate chart rendering with data loading and handle window resize"
```

---

### Task 5: 测试验证

**Files:**
- Test: 手动测试

- [ ] **Step 1: 启动前端服务**

Run: `cd frontend && npm run dev`
Expected: Local: http://localhost:5173/

- [ ] **Step 2: 测试图表显示**

1. 登录教师账号
2. 进入数据统计页面（菜单5）
3. 验证显示3个图表：成绩分布饼图、做题数分布柱状图、活跃度折线图
4. 验证图表数据正确

- [ ] **Step 3: 测试响应式布局**

1. 调整浏览器窗口大小
2. 验证图表自动调整大小

- [ ] **Step 4: 测试数据更新**

1. 使用搜索功能筛选学生
2. 验证图表数据自动更新

- [ ] **Step 5: Final Commit**

```bash
git add -A
git commit -m "test: verify teacher stats visual optimization"
```
