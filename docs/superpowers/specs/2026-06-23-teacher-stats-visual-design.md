# 教师端数据统计页面视觉优化设计文档

## 问题背景

教师后台管理系统的数据统计页面（菜单5）视觉效果较差，只有4个概览卡片和一个学生明细表格，缺少图表展示，不够直观。

**目标**：在现有基础上添加ECharts图表，提升数据可视化效果。

## 设计决策

### 方案对比

| 方案 | 描述 | 优点 | 缺点 |
|------|------|------|------|
| **方案1：渐进式优化（选定）** | 在现有基础上添加3个图表，保持现有布局 | 改动小、风险低、快速见效 | 视觉冲击力一般 |
| 方案2：全面重构 | 重新设计整个页面布局，添加图表+动画 | 视觉效果好、现代感强 | 改动大、风险高 |
| 方案3：模块化设计 | 将页面拆分为多个独立卡片，每个卡片一个功能 | 灵活性高、易维护 | 开发量中等 |

**选择方案1**：渐进式优化，因为项目已有ECharts（AdminPage.vue在用），可以直接复用。

## 页面布局设计

### 整体布局

```
┌─────────────────────────────────────────────────────────┐
│  全校数据统计（标题）                                      │
├─────────┬─────────┬─────────┬─────────┬─────────────────┤
│ 学生数  │ 平均做题 │ 平均正确率│ 考试及格率│ （现有4个卡片）  │
├─────────┴─────────┴─────────┴─────────┴─────────────────┤
│  ┌──────────────────────┐  ┌──────────────────────┐     │
│  │  学生成绩分布（饼图） │  │  做题数分布（柱状图） │     │
│  │  - 优秀(>=80%)       │  │  0-10, 11-20, ...   │     │
│  │  - 良好(60-80%)      │  │                     │     │
│  │  - 不及格(<60%)      │  │                     │     │
│  └──────────────────────┘  └──────────────────────┘     │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────┐│
│  │  学习活跃度趋势（折线图）                             ││
│  │  近7天/30天做题数变化                                ││
│  └─────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────┤
│  学生明细表格（保持现有）                                  │
└─────────────────────────────────────────────────────────┘
```

### 图表区域尺寸

- **饼图和柱状图行**：`el-row` + `el-col :span="12"`（各占50%宽度）
- **折线图行**：`el-row` + `el-col :span="24"`（全宽）
- **图表容器高度**：300px

## 图表详细设计

### 1. 学生成绩分布饼图

**数据来源**：从学生列表数据中统计

**分类规则**：
- 优秀：正确率 >= 80%
- 良好：60% <= 正确率 < 80%
- 不及格：正确率 < 60%
- 未做题：做题数为0

**图表配置**：
```javascript
{
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
      { value: excellentCount, name: '优秀' },
      { value: goodCount, name: '良好' },
      { value: failCount, name: '不及格' },
      { value: noDataCount, name: '未做题' }
    ],
    color: ['#67c23a', '#409eff', '#e6a23c', '#dcdfe6']
  }]
}
```

### 2. 做题数分布柱状图

**数据来源**：从学生列表数据中统计

**分组规则**：
- 0题
- 1-10题
- 11-20题
- 21-50题
- 50+题

**图表配置**：
```javascript
{
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
}
```

### 3. 学习活跃度趋势折线图

**数据来源**：需要新增后端API或从现有数据中计算

**时间范围**：近7天

**图表配置**：
```javascript
{
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
  xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
  yAxis: { type: 'value', name: '做题数' },
  series: [{
    name: '做题数',
    type: 'line',
    data: dailyQuestionCounts,
    smooth: true,
    itemStyle: { color: '#409eff' },
    areaStyle: { color: 'rgba(64, 158, 255, 0.1)' }
  }]
}
```

## 数据计算逻辑

### 饼图数据计算

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

### 柱状图数据计算

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

## 技术实现

### 前端修改

**文件**：`frontend/src/views/teacher/TeacherManagerPage.vue`

1. **引入ECharts**（参考AdminPage.vue的引入方式）
2. **添加图表容器**（在概览卡片和表格之间）
3. **添加图表渲染函数**
4. **添加数据计算函数**

### 后端修改

**文件**：`backend/src/main/java/sen/yuhuang/backend/service/TeacherStatsService.java`

可能需要：
1. 新增每日活跃度统计接口（或从现有数据中计算）
2. 优化学生列表查询，一次性获取所有需要的数据

## 测试用例

1. **饼图正确性**：验证各分类人数和百分比计算正确
2. **柱状图正确性**：验证各分组人数统计正确
3. **折线图正确性**：验证每日做题数统计正确
4. **响应式布局**：验证不同屏幕尺寸下图表正常显示
5. **数据更新**：验证切换搜索条件后图表数据更新

## 相关文件

- 前端页面：`frontend/src/views/teacher/TeacherManagerPage.vue`
- 前端API：`frontend/src/api/teacher-stats.js`
- 后端Controller：`backend/src/main/java/sen/yuhuang/backend/controller/teacher/TeacherStatsController.java`
- 后端Service：`backend/src/main/java/sen/yuhuang/backend/service/TeacherStatsService.java`
- 参考实现：`frontend/src/views/admin/AdminPage.vue`（ECharts引入和渲染）
