<template>
  <div class="teacher-admin-container">
    <!-- 侧边导航栏（复用可行菜单逻辑） -->
    <div class="sidebar">
      <!-- 系统标题 -->
      <div class="sidebar-header">
        <h3>教师后台管理系统@v1.0.0</h3>
      </div>
      <div class="user-info">
        <el-avatar :size="60" class="user-avatar">
          <el-icon><User /></el-icon>
        </el-avatar>
        <div class="user-name">教师账号</div>
        <div class="user-role">教师</div>
      </div>
      <!-- 导航菜单（复用可行的菜单选择逻辑） -->
      <el-menu
          :default-active="currentMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
      >
        <el-menu-item index="1">
          <el-icon><Document /></el-icon>
          <span>模拟考试管理</span>
        </el-menu-item>
        <el-menu-item index="2">
          <el-icon><Edit /></el-icon>
          <span>题目管理</span>
        </el-menu-item>
        <el-menu-item index="3">
          <el-icon><Folder /></el-icon>
          <span>题库管理</span>
        </el-menu-item>
        <el-menu-item index="4">
          <el-icon><Warning /></el-icon>
          <span>违规记录管理</span>
        </el-menu-item>
        <el-menu-item index="5">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
      </el-menu>
      <!-- 退出登录 -->
      <div class="logout-btn-wrap">
        <el-button
            type="text"
            class="logout-btn"
            @click="handleLogout"
        >
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 顶部操作栏 -->
      <div class="content-header">
        <div class="header-title">{{ currentTitle }}</div>
        <div class="header-actions">
          <el-button
              type="text"
              @click="handleRefresh"
          >
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <!-- 内容切换区域 -->
      <div class="content-body">
        <!-- 1. 模拟考试管理 (简化) -->
        <div v-if="currentMenu === '1'" class="page-content">
          <div class="page-title">模拟考试管理</div>

          <!-- 上方搜索 + 新增按钮 已对齐 -->
          <div style="margin-bottom: 15px; display:flex; gap:10px; align-items: center; flex-wrap: wrap;">
            <el-input v-model="examQuery" placeholder="搜索考试名称" style="width:240px" />
            <el-button type="primary" @click="openExamAdd">新增考试</el-button>
          </div>

          <el-table :data="examList" border>
            <el-table-column label="ID" prop="id" align="center" />
            <el-table-column label="考试名称" prop="examTitle" />
            <el-table-column label="开始时间" prop="startTime" align="center" />
            <el-table-column label="结束时间" prop="endTime" align="center" />
            <el-table-column label="时长(分钟)" prop="examDuration" align="center" />
            <el-table-column label="题目数" prop="questionCount" align="center" />
            <el-table-column label="参与人数" prop="participantCount" align="center" />

            <el-table-column label="状态" align="center">
              <template #default="scope">
                <el-tag :type="['warning','success','info'][scope.row.examStatus]">
                  {{ ['未开始','进行中','已结束'][scope.row.examStatus] }}
                </el-tag>
              </template>
            </el-table-column>

            <!-- VIP考试列 -->
            <el-table-column label="是否VIP考试" width="120" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.needVip === 1 ? 'warning' : 'success'" size="small">
                  {{ scope.row.needVip === 1 ? 'VIP专属' : '免费' }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="140" fixed="right">
              <template #default="scope">
                <div class="action-buttons">
                  <el-button type="primary" link @click="openExamEdit(scope.row)">编辑</el-button>
                  <el-button type="danger" link @click="deleteExam(scope.row.id)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 2. 题目管理 (增强版：包含新字段) -->
        <div v-if="currentMenu === '2'" class="page-content">
          <div class="page-title">题目管理</div>
          <div style="margin-bottom:15px; display:flex; gap:10px; flex-wrap:wrap;">
            <el-input v-model="questionQuery" placeholder="搜索题目名称或描述" style="width:280px" />
            <el-select
                v-model="questionBankFilter"
                placeholder="按题库筛选"
                clearable
                filterable
                style="width:220px"
            >
              <el-option
                  v-for="bank in bankList"
                  :key="bank.id"
                  :label="bank.title"
                  :value="bank.id"
              />
            </el-select>
            <el-button type="primary" @click="openQuestionAdd">新增题目</el-button>
          </div>

          <el-table :data="paginatedQuestionList" border style="width:100%">
            <el-table-column label="ID" prop="id" width="70" />
            <el-table-column label="题目名称" prop="questionName" min-width="150" show-overflow-tooltip />
            <el-table-column label="题目描述" prop="questionDesc" min-width="200" show-overflow-tooltip />
            <el-table-column label="所属题库" width="150">
              <template #default="{ row }">
                {{ row.bankTitle }}
              </template>
            </el-table-column>
            <el-table-column label="满分" prop="fullScore" width="80" />
            <el-table-column label="难度" width="80">
              <template #default="scope">
                {{ ['入门','简单','中等','困难'][scope.row.level] }}
              </template>
            </el-table-column>
            <el-table-column label="类型" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.questionType === 'bank' ? 'primary' : 'success'" size="small">
                  {{ scope.row.questionType === 'bank' ? '题库题' : '考试题' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="160" />
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="scope">
                <div class="action-buttons">
                  <el-button type="primary" link @click="openQuestionEdit(scope.row)">编辑</el-button>
                  <el-button type="danger" link @click="deleteQuestion(scope.row.id)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <!-- 题目分页 -->
          <div class="pagination-container">
            <el-pagination
                v-model:current-page="questionCurrentPage"
                v-model:page-size="questionPageSize"
                :page-sizes="[5, 10, 20, 50]"
                :total="questionTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleQuestionPageSizeChange"
                @current-change="handleQuestionCurrentPageChange"
            />
          </div>
        </div>

        <!-- 题目弹窗 (增强版：包含题目名称和新字段) -->
        <el-dialog v-model="questionDialog" :title="questionForm.id ? '编辑题目' : '新增题目'" width="800px" top="5vh">
          <el-form :model="questionForm" label-width="110px" label-position="right">
            <el-form-item label="题目名称" required>
              <el-input v-model="questionForm.questionName" placeholder="请输入题目名称" maxlength="100" show-word-limit />
            </el-form-item>

            <el-form-item label="题目描述" required>
              <el-input v-model="questionForm.questionDesc" type="textarea" :rows="3" placeholder="请输入题目描述" />
            </el-form-item>

            <!-- 原题目类型 & 题库选择部分保留，新增考试选择 -->
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="所属题库" :required="questionForm.questionType === 'bank'">
                  <el-select
                      v-model="questionForm.bankId"
                      placeholder="请选择题库（仅题库题需选）"
                      clearable
                      filterable
                      style="width:100%"
                      :disabled="questionForm.questionType === 'exam'"
                  >
                    <el-option
                        v-for="bank in bankList"
                        :key="bank.id"
                        :label="bank.title"
                        :value="bank.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="题目类型" required>
                  <el-select v-model="questionForm.questionType" style="width:100%">
                    <el-option label="题库题" value="bank" />
                    <el-option label="考试题" value="exam" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <!-- 新增：考试题专属 - 选择对应考试 -->
            <el-row :gutter="20" v-if="questionForm.questionType === 'exam'">
              <el-col :span="24">
                <el-form-item label="所属考试" required>
                  <el-select
                      v-model="questionForm.examId"
                      placeholder="请选择所属考试（仅考试题需选）"
                      clearable
                      filterable
                      style="width:100%"
                  >
                    <el-option
                        v-for="exam in examList"
                        :key="exam.id"
                        :label="exam.examTitle"
                        :value="exam.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="满分分值" required>
                  <el-input-number v-model="questionForm.fullScore" :min="0" :max="100" :precision="1" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="难度" required>
                  <el-select v-model="questionForm.level" style="width:100%">
                    <el-option label="入门" :value="0" />
                    <el-option label="简单" :value="1" />
                    <el-option label="中等" :value="2" />
                    <el-option label="困难" :value="3" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="提示">
              <el-input v-model="questionForm.hint" placeholder="解题提示（可选）" />
            </el-form-item>

            <el-form-item label="输入格式说明">
              <el-input v-model="questionForm.inputFormat" type="textarea" :rows="2" placeholder="例如：第一行输入整数n，第二行输入n个整数" />
            </el-form-item>
            <el-form-item label="输出格式说明">
              <el-input v-model="questionForm.outputFormat" type="textarea" :rows="2" placeholder="例如：输出一行，为计算结果" />
            </el-form-item>

            <!-- 样例输入输出 (支持多个) -->
            <el-form-item label="样例输入输出">
              <div v-for="(sample, idx) in questionForm.samples" :key="idx" style="margin-bottom: 16px; border: 1px solid #eee; padding: 12px; border-radius: 6px;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                  <span style="font-weight: bold;">样例 {{ idx + 1 }}</span>
                  <el-button type="danger" size="small" link @click="removeSample(idx)">删除</el-button>
                </div>
                <el-input v-model="sample.input" type="textarea" :rows="2" placeholder="样例输入" style="margin-bottom: 8px;" />
                <el-input v-model="sample.output" type="textarea" :rows="2" placeholder="样例输出" />
              </div>
              <el-button type="primary" size="small" plain @click="addSample">+ 添加样例</el-button>
            </el-form-item>

            <el-form-item label="测试用例 (JSON)">
              <el-input v-model="questionForm.testCasesJson" type="textarea" :rows="4" placeholder='例如：[{"input":"5\n1 2 3 4 5","output":"15"},{"input":"3\n1 1 1","output":"3"}]' />
              <div style="font-size:12px; color:#909399;">格式: 数组，每个元素包含input和output字段</div>
            </el-form-item>

            <el-form-item label="参考答案">
              <el-input v-model="questionForm.answer" type="textarea" :rows="3" placeholder="参考答案（代码或文本，可为空）" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="questionDialog = false">取消</el-button>
            <el-button type="primary" @click="saveQuestion">保存</el-button>
          </template>
        </el-dialog>

        <!-- 3. 题库管理 (添加VIP和困难度字段) -->
        <div v-if="currentMenu === '3'" class="page-content">
          <div class="page-title">题库管理</div>
          <div style="margin-bottom:15px;">
            <el-button type="primary" @click="openBankAdd">新增题库</el-button>
          </div>
          <el-table :data="paginatedBankList" border style="width:100%">
            <el-table-column label="ID" prop="id" width="80" />
            <el-table-column label="题库名称" prop="title" min-width="150" show-overflow-tooltip />
            <el-table-column label="描述" prop="desc" min-width="180" show-overflow-tooltip />
            <el-table-column label="困难度" width="100" align="center">
              <template #default="scope">
                <el-tag :type="getDifficultyType(scope.row.difficulty)" size="small">
                  {{ getDifficultyText(scope.row.difficulty) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="是否需要VIP" width="120" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.needVip === 1 ? 'warning' : 'success'" size="small">
                  {{ scope.row.needVip === 1 ? '需要VIP' : '免费' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="题目数" prop="questionCount" width="100" align="center" />
            <el-table-column label="学习人数" prop="viewCount" width="100" align="center" />
            <el-table-column label="推荐度" width="120" align="center">
              <template #default="scope">
                <el-progress :percentage="scope.row.recommend" :stroke-width="8" :show-text="false" style="width:60px; display:inline-block;" />
                <span style="font-size:12px; margin-left:5px;">{{ scope.row.recommend }}%</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
                  {{ scope.row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right" align="center">
              <template #default="scope">
                <div class="action-buttons">
                  <el-button type="primary" link @click="openBankEdit(scope.row)">编辑</el-button>
                  <el-button type="danger" link @click="deleteBank(scope.row.id)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <!-- 题库分页 -->
          <div class="pagination-container">
            <el-pagination
                v-model:current-page="bankCurrentPage"
                v-model:page-size="bankPageSize"
                :page-sizes="[5, 10, 20, 50]"
                :total="bankTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleBankPageSizeChange"
                @current-change="handleBankCurrentPageChange"
            />
          </div>
        </div>

        <!-- 题库弹窗 (添加VIP和困难度字段) -->
        <el-dialog v-model="bankDialog" :title="bankForm.id ? '编辑题库' : '新增题库'" width="600px">
          <el-form :model="bankForm" label-width="110px" label-position="right">
            <el-form-item label="题库名称" required>
              <el-input v-model="bankForm.title" placeholder="请输入题库名称" maxlength="50" show-word-limit />
            </el-form-item>

            <el-form-item label="描述">
              <el-input v-model="bankForm.desc" type="textarea" :rows="3" placeholder="请输入题库描述" />
            </el-form-item>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="困难度" required>
                  <el-select v-model="bankForm.difficulty" placeholder="请选择题库困难度" style="width:100%">
                    <el-option label="入门" :value="0" />
                    <el-option label="简单" :value="1" />
                    <el-option label="中等" :value="2" />
                    <el-option label="困难" :value="3" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="是否需要VIP" required>
                  <el-radio-group v-model="bankForm.needVip">
                    <el-radio :value="0">免费</el-radio>
                    <el-radio :value="1">需要VIP</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="题目数量">
                  <el-input-number v-model="bankForm.questionCount" :min="0" :max="9999" style="width:100%" disabled />
                  <div style="font-size:12px; color:#909399;">系统自动统计</div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="推荐度">
                  <el-slider v-model="bankForm.recommend" :min="0" :max="100" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="状态">
              <el-radio-group v-model="bankForm.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="bankDialog = false">取消</el-button>
            <el-button type="primary" @click="saveBank">保存</el-button>
          </template>
        </el-dialog>

        <!-- 4. 违规记录管理 -->
        <div v-if="currentMenu === '4'" class="page-content">
          <div class="page-title">违规记录管理</div>
          <el-table :data="violationList" border>
            <el-table-column label="ID" prop="id" />
            <el-table-column label="考试ID" prop="examId" />
            <el-table-column label="学生ID" prop="userId" />
            <el-table-column label="违规说明" prop="violationDesc" min-width="250" />
            <el-table-column label="违规时间" prop="createTime" />
            <el-table-column label="处理状态">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
                  {{ scope.row.status === 1 ? '已处理' : '未处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="scope">
                <el-button text @click="handleViolation(scope.row)">
                  {{ scope.row.status === 0 ? '标记已处理' : '已处理' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 5. 数据统计 -->
        <div v-if="currentMenu === '5'" class="page-content">
          <div class="page-title">数据统计</div>
          <el-row :gutter="20" class="stats-overview-grid">
            <el-col :span="6">
              <el-card class="stat-card">总考试数<br/>{{ stats.totalExam }}</el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">总题目数<br/>{{ stats.totalQuestion }}</el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">总题库数<br/>{{ stats.totalBank }}</el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">违规记录<br/>{{ stats.totalViolation }}</el-card>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top:20px;">
            <el-col :span="6">
              <el-card class="stat-card">VIP考试数<br/>{{ stats.examVip }}</el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">免费考试数<br/>{{ stats.examFree }}</el-card>
            </el-col>
          </el-row>

          <div class="stats-chart-grid">
            <el-card class="chart-card" shadow="never">
              <template #header>
                <div class="chart-card-header">
                  <span>考试状态分布</span>
                  <span class="chart-card-meta">共 {{ stats.totalExam }} 场</span>
                </div>
              </template>
              <div class="chart-card-body">
                <div class="donut-chart-shell">
                  <div class="donut-chart" :style="buildDonutStyle(examStatusChartData)">
                    <div class="donut-chart-center">
                      <strong>{{ stats.totalExam }}</strong>
                      <span>考试总数</span>
                    </div>
                  </div>
                </div>
                <div class="chart-legend">
                  <div
                      v-for="item in examStatusChartData"
                      :key="item.label"
                      class="legend-row"
                  >
                    <div class="legend-row-top">
                      <div class="legend-label">
                        <span class="legend-dot" :style="{ backgroundColor: item.color }"></span>
                        <span>{{ item.label }}</span>
                      </div>
                      <div class="legend-value">{{ item.value }} / {{ item.percent }}%</div>
                    </div>
                    <div class="legend-track">
                      <div class="legend-fill" :style="{ width: item.percent + '%', backgroundColor: item.color }"></div>
                    </div>
                  </div>
                </div>
              </div>
            </el-card>

            <el-card class="chart-card" shadow="never">
              <template #header>
                <div class="chart-card-header">
                  <span>题目难度分布</span>
                  <span class="chart-card-meta">共 {{ stats.totalQuestion }} 题</span>
                </div>
              </template>
              <div class="difficulty-bars">
                <div
                    v-for="item in difficultyChartData"
                    :key="item.label"
                    class="difficulty-row"
                >
                  <div class="difficulty-header">
                    <div class="difficulty-title">
                      <span class="difficulty-chip" :style="{ backgroundColor: item.softColor, color: item.color }">{{ item.label }}</span>
                    </div>
                    <div class="difficulty-value">{{ item.value }} 题</div>
                  </div>
                  <div class="difficulty-track">
                    <div class="difficulty-fill" :style="{ width: item.percent + '%', background: item.gradient }"></div>
                  </div>
                  <div class="difficulty-footer">{{ item.percent }}% 的题目位于这个难度档</div>
                </div>
              </div>
            </el-card>
          </div>

          <div class="stats-chart-grid secondary">
            <el-card class="chart-card compact" shadow="never">
              <template #header>
                <div class="chart-card-header">
                  <span>考试权限结构</span>
                  <span class="chart-card-meta">VIP / 免费</span>
                </div>
              </template>
              <div class="segment-card">
                <div class="segment-track">
                  <div
                      v-for="item in accessChartData"
                      :key="item.label"
                      class="segment-block"
                      :style="{ width: item.percent + '%', background: item.gradient }"
                  ></div>
                </div>
                <div class="segment-legend">
                  <div v-for="item in accessChartData" :key="item.label" class="segment-row">
                    <div class="legend-label">
                      <span class="legend-dot" :style="{ backgroundColor: item.color }"></span>
                      <span>{{ item.label }}</span>
                    </div>
                    <strong>{{ item.value }}</strong>
                  </div>
                </div>
              </div>
            </el-card>

            <el-card class="chart-card compact" shadow="never">
              <template #header>
                <div class="chart-card-header">
                  <span>运行概览</span>
                  <span class="chart-card-meta">关键比例</span>
                </div>
              </template>
              <div class="summary-metrics">
                <div class="summary-metric">
                  <span>每场考试平均题量</span>
                  <strong>{{ averageQuestionsPerExam }}</strong>
                </div>
                <div class="summary-metric">
                  <span>每题库平均题量</span>
                  <strong>{{ averageQuestionsPerBank }}</strong>
                </div>
                <div class="summary-metric">
                  <span>违规记录密度</span>
                  <strong>{{ violationDensity }}</strong>
                </div>
              </div>
            </el-card>
          </div>
        </div>
      </div>
    </div>

    <!-- 考试弹窗 (简化) -->
    <el-dialog v-model="examDialog" title="考试信息" width="600px">
      <el-form :model="examForm" label-width="100px">
        <el-form-item label="考试名称"><el-input v-model="examForm.examTitle" /></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="examForm.startTime" type="datetime" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="examForm.endTime" type="datetime" /></el-form-item>
        <el-form-item label="时长"><el-input v-model="examForm.examDuration" /></el-form-item>
        <el-form-item label="题目数量"><el-input v-model="examForm.questionCount" /></el-form-item>
        <el-form-item label="选择题目">
          <el-select
              v-model="examForm.selectedQuestionIds"
              multiple
              filterable
              clearable
              collapse-tags
              collapse-tags-tooltip
              placeholder="可从题库选择题目加入考试"
              style="width: 100%"
          >
            <el-option
                v-for="question in availableBankQuestions"
                :key="question.id"
                :label="`${question.bankTitle} - ${question.questionName}`"
                :value="question.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="examForm.examStatus">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
        </el-form-item>
        <!-- 新增：是否VIP考试 -->
        <el-form-item label="是否VIP考试" required>
          <el-radio-group v-model="examForm.needVip">
            <el-radio :value="0">免费</el-radio>
            <el-radio :value="1">VIP专属</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="examDialog = false">取消</el-button>
        <el-button type="primary" @click="saveExam">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>



<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User, Document, Edit, Folder, Warning, DataAnalysis,
  SwitchButton, Refresh
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from "@/stores/user"
import dayjs from 'dayjs'
import {
  addQuestion,
  deleteBank as deleteBankApi,
  deleteExam as deleteExamApi,
  deleteQ,
  loadBankData,
  loadExamData,
  loadQuestionData,
  saveBank as saveBankApi,
  saveExam as saveExamApi,
  updateQuestion
} from "@/api/teacher.js"

// 初始化路由和用户状态管理
const router = useRouter()
const userStore = useUserStore()

// ======================================================================考试管理
const saveExam = async () => {
  if (!examForm.value.examTitle?.trim()) {
    ElMessage.warning('请输入考试名称')
    return
  }
  if (!examForm.value.startTime || !examForm.value.endTime) {
    ElMessage.warning('请选择开始和结束时间')
    return
  }
  if (!examForm.value.examDuration || Number(examForm.value.examDuration) <= 0) {
    ElMessage.warning('考试时长必须大于0')
    return
  }

  const payload = {
    id: examForm.value.id,
    examTitle: examForm.value.examTitle,
    startTime: dayjs(examForm.value.startTime).format('YYYY-MM-DD HH:mm:ss'),
    endTime: dayjs(examForm.value.endTime).format('YYYY-MM-DD HH:mm:ss'),
    examDuration: Number(examForm.value.examDuration),
    examStatus: Number(examForm.value.examStatus ?? 0),
    needVip: Number(examForm.value.needVip ?? 0),
    selectedQuestionIds: (examForm.value.selectedQuestionIds || []).map(id => Number(id))
  }

  try {
    const res = await saveExamApi(payload)
    if (res.code === 200) {
      await Promise.all([loadExamList(true), loadQuestionList(true)])
      ElMessage.success(examForm.value.id ? '编辑成功' : '新增成功')
      examDialog.value = false
    } else {
      ElMessage.error(res.message || '保存考试失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '保存考试异常')
  }
}

// 新增考试初始化 - 新增needVip默认值0
const openExamAdd = () => {
  loadQuestionList(true)
  examForm.value = {
    examStatus: 0,
    questionCount: 0,
    examDuration: 120,
    needVip: 0,
    selectedQuestionIds: []
  };
  examDialog.value = true
}

// 编辑考试初始化 - 自动回显needVip（无则默认0）
const openExamEdit = (row) => {
  const selectedQuestionIds = questionList.value
      .filter(question =>
          Number(question.examId) === Number(row.id) &&
          (Number(question.sourceType) === 2 || question.bankId)
      )
      .map(question => Number(question.id))
  examForm.value = {
    ...row,
    needVip: row.needVip !== undefined ? row.needVip : 0,
    selectedQuestionIds
  };
  examDialog.value = true
}

// 修复bug1：递归调用问题 - 改为调用后端api loadExamData，而非自身
const loadExamList = async (forceRefresh = false) => {
  if (examList.value.length > 0 && !forceRefresh) {
    console.log('使用缓存的考试数据，共', examList.value.length, '条')
    return
  }
  try {
    // 修复：替换 const res = await loadExamList() 为真实接口
    const res = await loadExamData()
    if(res.code === 200){
      examList.value = res.data.map(item => ({
        id: item.id,
        examTitle: item.examTitle || '未命名考试',
        startTime: item.startTime,
        endTime: item.endTime,
        examDuration: item.examDuration,
        questionCount: item.questionCount,
        participantCount: item.participantCount,
        examStatus: item.examStatus,
        needVip: item.isVipOnly !== undefined ? item.isVipOnly : 0
      }))
      console.log('examList.value', examList.value)
    }else{
      // 修复bug2：后端接口失败时，加载模拟数据，避免空数组
      examList.value = [
        { id: 1, examTitle: '2025计算机考研模拟考', startTime: '2025-03-20 09:00:00', endTime: '2025-03-20 12:00:00', examDuration: 180, questionCount: 25, participantCount: 126, examStatus: 1, needVip: 0 },
        { id: 2, examTitle: 'Java基础专项测试', startTime: '2025-04-01 14:00:00', endTime: '2025-04-01 15:30:00', examDuration: 90, questionCount: 20, participantCount: 89, examStatus: 0, needVip: 1 }
      ]
      ElMessage.warning('加载考试数据失败，已使用模拟数据')
    }
  }catch (error){
    // 修复bug2：接口异常时，加载模拟数据
    examList.value = [
      { id: 1, examTitle: '2025计算机考研模拟考', startTime: '2025-03-20 09:00:00', endTime: '2025-03-20 12:00:00', examDuration: 180, questionCount: 25, participantCount: 126, examStatus: 1, needVip: 0 },
      { id: 2, examTitle: 'Java基础专项测试', startTime: '2025-04-01 14:00:00', endTime: '2025-04-01 15:30:00', examDuration: 90, questionCount: 20, participantCount: 89, examStatus: 0, needVip: 1 }
    ]
    ElMessage.error(error.response?.data?.message || '加载考试数据异常，已使用模拟数据')
  }
}

// ======================================================================题目管理
// 题目分页相关
const questionCurrentPage = ref(1)
const questionPageSize = ref(10)
const questionTotal = computed(() => filteredQuestionList.value.length)

// 分页后的题目列表
const paginatedQuestionList = computed(() => {
  const start = (questionCurrentPage.value - 1) * questionPageSize.value
  const end = start + questionPageSize.value
  return filteredQuestionList.value.slice(start, end)
})

// 分页大小改变
const handleQuestionPageSizeChange = (val) => {
  questionPageSize.value = val
  questionCurrentPage.value = 1
}

// 当前页改变
const handleQuestionCurrentPageChange = (val) => {
  questionCurrentPage.value = val
}

// 菜单核心逻辑
const currentMenu = ref('1')  // 临时改为'1'(考试管理),测试是否能显示内容
const currentTitle = ref('模拟考试管理')

// 增强的题目列表，包含题目名称字段
const questionList = ref([])

const loadQuestionList = async (forceRefresh = false) => {
  if (questionList.value.length > 0 && !forceRefresh) {
    console.log('使用缓存的题目数据，共', questionList.value.length, '条')
    return
  }
  try {
    const res = await loadQuestionData()
    if(res.code === 200){
      questionList.value = res.data.map(item => ({
        id: item.id,
        questionName: item.questionName || '未命名题目',
        questionDesc: item.questionDesc,
        bankId: item.bankId ? Number(item.bankId) : null,  // 转换为数字
        bankTitle: item.bankTitle || '',
        examId: item.examId ? Number(item.examId) : null,  // 转换为数字
        examTitle: item.examTitle || '',
        fullScore: parseFloat(item.fullScore),
        level: parseInt(item.level),
        hint: item.hint || '',
        inputFormat: item.inputFormat || '',
        outputFormat: item.outputFormat || '',
        createTime: item.createTime,
        answer: item.answerCode || '',
        sourceType: item.type ? Number(item.type) : null,
        questionType: item.type
            ? (Number(item.type) === 2 ? 'bank' : 'exam')
            : ((item.bankId && item.bankId !== null && item.bankId !== '') ? 'bank' : 'exam'),
        samples: item.sampleQuestionList ? item.sampleQuestionList.map(sample => ({
          input: sample.input || '',
          output: sample.output || '',
          sampleDesc: sample.sampleDesc || ''
        })) : [],
        testCasesJson: item.testSampleList ? JSON.stringify(item.testSampleList.map(test => ({
          input: test.input,
          output: test.output
        }))) : '[]'
      }))
      // 同步bankTitle
      questionList.value.forEach(q => syncBankTitle(q))
      console.log('questionList.value',questionList.value)
    }else{
      ElMessage.error(res.message || '加载题目数据失败，请重试')
    }
  }catch (error){
    ElMessage.error(error.response?.data?.message || '加载题目数据异常，请重试')
  }
}

// 保存或者编辑
const saveQuestion = async () => {
  // 1. 基础校验
  if (!questionForm.value.questionName?.trim()) {
    ElMessage.warning('请输入题目名称')
    return
  }
  if (!questionForm.value.questionDesc?.trim()) {
    ElMessage.warning('请输入题目描述')
    return
  }
  if (questionForm.value.fullScore === undefined || questionForm.value.fullScore < 0) {
    ElMessage.warning('满分分值必须大于等于0')
    return
  }

  // 2. 新增：题库题/考试题专属校验
  if (questionForm.value.questionType === 'bank' && !questionForm.value.bankId) {
    ElMessage.warning('题库题必须选择所属题库')
    return
  }
  if (questionForm.value.questionType === 'exam' && !questionForm.value.examId) {
    ElMessage.warning('考试题必须选择所属考试')
    return
  }

  // 3. 测试用例JSON校验
  let testCasesParsed = []
  try {
    testCasesParsed = JSON.parse(questionForm.value.testCasesJson || '[]')
    if (!Array.isArray(testCasesParsed)) throw new Error('必须是数组')
  } catch (e) {
    ElMessage.error('测试用例格式错误，请输入有效的JSON数组')
    return
  }

  const now = dayjs().format('YYYY-MM-DD HH:mm:ss')

  // 4. 新增：处理考试名称（与题库名称逻辑一致）
  let bankTitle = ''
  let bankId = null
  let examTitle = ''
  let examId = null

  // 题库题处理
  if (questionForm.value.questionType === 'bank' && questionForm.value.bankId) {
    bankId = questionForm.value.bankId
    const bank = bankList.value.find(b => b.id === bankId)
    bankTitle = bank ? bank.title : ''
  }
  // 考试题处理
  if (questionForm.value.questionType === 'exam' && questionForm.value.examId) {
    examId = questionForm.value.examId
    const exam = examList.value.find(e => e.id === examId)
    examTitle = exam ? exam.examTitle : ''
  }

  if (questionForm.value.id) {
    // 编辑模式
    const idx = questionList.value.findIndex(x => x.id === questionForm.value.id)
    if (idx !== -1) {
      try{
        // 获取原始数据
        const originalQuestion = questionList.value[idx]

        // 准备提交的数据，只有变化的数据才提交
        const submitData = {
          id: questionForm.value.id
        }

        // 检查基本信息是否有变化
        if (questionForm.value.questionName !== originalQuestion.questionName) {
          submitData.questionName = questionForm.value.questionName
        }
        if (questionForm.value.questionDesc !== originalQuestion.questionDesc) {
          submitData.questionDesc = questionForm.value.questionDesc
        }
        if (questionForm.value.fullScore !== originalQuestion.fullScore) {
          submitData.fullScore = questionForm.value.fullScore
        }
        if (questionForm.value.level !== originalQuestion.level) {
          submitData.level = questionForm.value.level
        }
        if (questionForm.value.hint !== originalQuestion.hint) {
          submitData.hint = questionForm.value.hint
        }
        if (questionForm.value.inputFormat !== originalQuestion.inputFormat) {
          submitData.inputFormat = questionForm.value.inputFormat
        }
        if (questionForm.value.outputFormat !== originalQuestion.outputFormat) {
          submitData.outputFormat = questionForm.value.outputFormat
        }
        if (questionForm.value.answer !== originalQuestion.answer) {
          submitData.answer = questionForm.value.answer
        }
        if (bankId !== originalQuestion.bankId) {
          submitData.bankId = bankId
        }
        if (examId !== originalQuestion.examId) {
          submitData.examId = examId
        }

        // 检查 samples 是否有变化
        const currentSamplesStr = JSON.stringify(questionForm.value.samples)
        const originalSamplesStr = JSON.stringify(originalQuestion.samples || [])
        if (currentSamplesStr !== originalSamplesStr) {
          submitData.samples = currentSamplesStr
        } else {
          submitData.samples = null  // 没变化就传 null
        }

        // 检查 testCasesJson 是否有变化
        if (questionForm.value.testCasesJson !== (originalQuestion.testCasesJson || '[]')) {
          submitData.testCasesJson = questionForm.value.testCasesJson
        } else {
          submitData.testCasesJson = null  // 没变化就传 null
        }

        console.log('提交的数据:', submitData)

        const res = await updateQuestion(submitData)
        if(res.code === 200) {
          // 更新本地数据 - 只更新实际变化的字段
          const updatedQuestion = {...originalQuestion}

          // 更新基础字段
          updatedQuestion.questionName = questionForm.value.questionName
          updatedQuestion.questionDesc = questionForm.value.questionDesc
          updatedQuestion.fullScore = questionForm.value.fullScore
          updatedQuestion.level = questionForm.value.level
          updatedQuestion.hint = questionForm.value.hint
          updatedQuestion.inputFormat = questionForm.value.inputFormat
          updatedQuestion.outputFormat = questionForm.value.outputFormat
          updatedQuestion.answer = questionForm.value.answer
          updatedQuestion.samples = questionForm.value.samples
          updatedQuestion.testCasesJson = questionForm.value.testCasesJson

          // 根据题目类型更新关联字段
          if (questionForm.value.questionType === 'bank') {
            // 题库题：更新 bankId 和 bankTitle
            updatedQuestion.bankId = bankId
            updatedQuestion.bankTitle = bankTitle
            // 清除考试相关字段
            updatedQuestion.examId = null
            updatedQuestion.examTitle = ''
          } else if (questionForm.value.questionType === 'exam') {
            // 考试题：更新 examId 和 examTitle
            updatedQuestion.examId = examId
            updatedQuestion.examTitle = examTitle
            // 清除题库相关字段（保留原有的 bankTitle，但不显示）
            updatedQuestion.bankId = null
            // bankTitle 保持不变，因为表格中不会显示
          }

          questionList.value[idx] = updatedQuestion
          ElMessage.success('编辑成功')
        }else{
          ElMessage.error(res.message || '编辑失败')
        }
      }catch (e){
        ElMessage.error('更新题目异常：' + e.message)
      }
    }
  } else {
    // 新增模式 - 所有字段都需要提交
    try{
      const newId = Date.now()
      const newQuestion = {
        id: newId,
        questionName: questionForm.value.questionName,
        questionDesc: questionForm.value.questionDesc,
        fullScore: questionForm.value.fullScore,
        level: questionForm.value.level,
        hint: questionForm.value.hint,
        inputFormat: questionForm.value.inputFormat,
        outputFormat: questionForm.value.outputFormat,
        answer: questionForm.value.answer,
        bankId: questionForm.value.bankId,
        examId: questionForm.value.examId,
        questionType: questionForm.value.questionType,
        samples: JSON.stringify(questionForm.value.samples),
        testCasesJson: questionForm.value.testCasesJson,
        createTime: now
      }

      const res = await addQuestion(newQuestion)
      if(res.code === 200){
        questionList.value.push({
          ...newQuestion
        })
        ElMessage.success(res.message || '新增成功！')
      }else{
        ElMessage.error(res.message || '新增问题失败！')
      }
    }catch (e) {
      ElMessage.error('新增问题异常：' + e.message)
    }
  }
  questionDialog.value = false
}

// ======================================================================题库管理
// 题库分页相关
const bankCurrentPage = ref(1)
const bankPageSize = ref(10)
const bankTotal = computed(() => bankList.value.length)

// 添加困难度相关的辅助函数
const getDifficultyText = (difficulty) => {
  const map = { 0: '入门', 1: '简单', 2: '中等', 3: '困难' }
  return map[difficulty] || '未知'
}

const getDifficultyType = (difficulty) => {
  const map = { 0: 'info', 1: '', 2: 'warning', 3: 'danger' }
  return map[difficulty] || ''
}

// 分页后的题库列表
const paginatedBankList = computed(() => {
  const start = (bankCurrentPage.value - 1) * bankPageSize.value
  const end = start + bankPageSize.value
  return bankList.value.slice(start, end)
})

// 分页大小改变
const handleBankPageSizeChange = (val) => {
  bankPageSize.value = val
  bankCurrentPage.value = 1
}

// 当前页改变
const handleBankCurrentPageChange = (val) => {
  bankCurrentPage.value = val
}

// 题库数据（用于下拉）
const bankList = ref([])

// 修复 loadBankList 函数，确保在题库管理菜单时正确加载
const loadBankList = async (forceRefresh = false) => {
  if (bankList.value.length > 0 && !forceRefresh) {
    console.log('使用缓存的题库数据，共', bankList.value.length, '条')
    return
  }
  try {
    const res = await loadBankData()
    if (res.code === 200) {
      bankList.value = res.data.map(item => ({
        id: item.id,
        title: item.title || '未命名题库',
        desc: item.desc || '',
        difficulty: item.level !== undefined ? item.level : (item.difficulty !== undefined ? item.difficulty : 1),
        needVip: item.isVip !== undefined ? item.isVip : (item.needVip !== undefined ? item.needVip : 0),
        questionCount: item.questionCount || 0,
        viewCount: item.viewCount || 0,
        recommend: item.recommend || 80,
        status: item.status !== undefined ? item.status : 1,
        createTime: item.createTime || dayjs().format('YYYY-MM-DD')
      }))
      console.log('bankList.value', bankList.value)
    } else {
      ElMessage.warning(res.message || '加载题库数据失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '加载题库数据异常')
  }
}

// 修复 handleMenuSelect 中的题库加载
const handleMenuSelect = async (index) => {
  // 切换菜单前，根据不同菜单加载对应数据
  if (index === '2') {
    console.log('加载题目数据 + 题库数据 + 考试数据')
    // 改为并行加载，提升速度，避免阻塞
    await Promise.all([loadQuestionList(), loadBankList(), loadExamList()])
  } else if (index === '3') {
    console.log('加载题库数据')
    await loadBankList()
  } else if (index === '1') {
    console.log('加载考试数据')
    await Promise.all([loadExamList(), loadQuestionList()])
  } else if (index === '4') {
    console.log('加载违规记录数据')
  } else if (index === '5') {
    console.log('加载统计数据')
    // 统计数据不需要额外加载，直接从已有数据计算
  }
  currentMenu.value = index
  const titleMap = {
    '1': '模拟考试管理',
    '2': '题目管理',
    '3': '题库管理',
    '4': '违规记录管理',
    '5': '数据统计'
  }
  currentTitle.value = titleMap[index]
}

// 页面加载时初始化 - 修复白屏问题
onMounted(async () => {
  console.log('教师端页面加载，初始化数据...')
  // 根据默认菜单加载对应数据
  await handleMenuSelect(currentMenu.value)
})

const openBankAdd = () => {
  bankForm.value = {
    status: 1,
    questionCount: 0,
    viewCount: 0,
    recommend: 80,
    difficulty: 1,
    needVip: 0
  };
  bankDialog.value = true
}

const openBankEdit = (row) => {
  bankForm.value = {
    ...row,
    difficulty: row.difficulty !== undefined ? row.difficulty : 1,
    needVip: row.needVip !== undefined ? row.needVip : 0
  };
  bankDialog.value = true
}

// 修复 saveBank 函数
const saveBank = async () => {
  if (!bankForm.value.title?.trim()) {
    ElMessage.warning('请输入题库名称')
    return
  }
  const payload = {
    id: bankForm.value.id,
    title: bankForm.value.title,
    desc: bankForm.value.desc || '',
    difficulty: Number(bankForm.value.difficulty ?? 1),
    needVip: Number(bankForm.value.needVip ?? 0),
    recommend: Number(bankForm.value.recommend ?? 80),
    status: Number(bankForm.value.status ?? 1)
  }
  try {
    const res = await saveBankApi(payload)
    if (res.code === 200) {
      await Promise.all([loadBankList(true), loadQuestionList(true)])
      ElMessage.success(bankForm.value.id ? '编辑成功' : '新增成功')
      bankDialog.value = false
    } else {
      ElMessage.error(res.message || '保存题库失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '保存题库异常')
  }
}

// 修复 deleteBank 函数，添加确认提示
const deleteBank = (id) => {
  const hasQuestions = questionList.value.some(q => Number(q.bankId) === Number(id))
  if (hasQuestions) {
    ElMessage.warning('该题库下存在题目，请先删除相关题目')
    return
  }
  ElMessageBox.confirm('确定要删除该题库吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteBankApi({ id })
      if (res.code === 200) {
        await Promise.all([loadBankList(true), loadQuestionList(true)])
        ElMessage.success('删除成功')
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除题库异常')
    }
  }).catch(() => {})
}

// ==============================================
// 模拟数据 & 公共方法
// ==============================================
const examList = ref([])
const violationList = ref([
  { id: 1, examId: 1, userId: 1001, violationDesc: '切屏超过3次', createTime: '2025-03-20 09:15:22', status: 0 }
])

// 自动同步 bankTitle (根据bankId)
const syncBankTitle = (question) => {
  const bank = bankList.value.find(b => b.id === question.bankId)
  question.bankTitle = bank ? bank.title : '未知题库'
}

// 题目搜索过滤
const questionQuery = ref('')
const questionBankFilter = ref(null)
const filteredQuestionList = computed(() => {
  let list = questionList.value
  if (questionBankFilter.value !== null && questionBankFilter.value !== undefined && questionBankFilter.value !== '') {
    list = list.filter(q => Number(q.bankId) === Number(questionBankFilter.value))
  }
  if (!questionQuery.value) return list
  const keyword = questionQuery.value.toLowerCase()
  return list.filter(q =>
      q.questionName?.toLowerCase().includes(keyword) ||
      q.questionDesc?.toLowerCase().includes(keyword)
  )
})

// 统计 (包含新字段难度分布)
const stats = computed(() => ({
  totalExam: examList.value.length,
  totalQuestion: questionList.value.length,
  totalBank: bankList.value.length,
  totalViolation: violationList.value.length,
  exam0: examList.value.filter(i => i.examStatus === 0).length,
  exam1: examList.value.filter(i => i.examStatus === 1).length,
  exam2: examList.value.filter(i => i.examStatus === 2).length,
  examVip: examList.value.filter(i => i.needVip === 1).length,
  examFree: examList.value.filter(i => i.needVip === 0).length,
  lv0: questionList.value.filter(i => i.level === 0).length,
  lv1: questionList.value.filter(i => i.level === 1).length,
  lv2: questionList.value.filter(i => i.level === 2).length,
  lv3: questionList.value.filter(i => i.level === 3).length
}))

const safeDividePercent = (value, total) => {
  if (!total) return 0
  return Math.round((value / total) * 100)
}

const buildChartItems = (items) => {
  const total = items.reduce((sum, item) => sum + item.value, 0)
  return items.map(item => ({
    ...item,
    percent: safeDividePercent(item.value, total)
  }))
}

const buildDonutStyle = (items) => {
  const total = items.reduce((sum, item) => sum + item.value, 0)
  if (!total) {
    return {
      background: 'conic-gradient(#e5e7eb 0deg 360deg)'
    }
  }

  let currentAngle = 0
  const segments = items.map(item => {
    const angle = (item.value / total) * 360
    const segment = `${item.color} ${currentAngle}deg ${currentAngle + angle}deg`
    currentAngle += angle
    return segment
  })

  return {
    background: `conic-gradient(${segments.join(', ')})`
  }
}

const examStatusChartData = computed(() => buildChartItems([
  { label: '未开始', value: stats.value.exam0, color: '#f59e0b' },
  { label: '进行中', value: stats.value.exam1, color: '#10b981' },
  { label: '已结束', value: stats.value.exam2, color: '#64748b' }
]))

const difficultyChartData = computed(() => buildChartItems([
  {
    label: '入门',
    value: stats.value.lv0,
    color: '#0f766e',
    softColor: '#ccfbf1',
    gradient: 'linear-gradient(90deg, #14b8a6 0%, #0f766e 100%)'
  },
  {
    label: '简单',
    value: stats.value.lv1,
    color: '#2563eb',
    softColor: '#dbeafe',
    gradient: 'linear-gradient(90deg, #60a5fa 0%, #2563eb 100%)'
  },
  {
    label: '中等',
    value: stats.value.lv2,
    color: '#d97706',
    softColor: '#fef3c7',
    gradient: 'linear-gradient(90deg, #fbbf24 0%, #d97706 100%)'
  },
  {
    label: '困难',
    value: stats.value.lv3,
    color: '#dc2626',
    softColor: '#fee2e2',
    gradient: 'linear-gradient(90deg, #f87171 0%, #dc2626 100%)'
  }
]))

const accessChartData = computed(() => buildChartItems([
  {
    label: 'VIP考试',
    value: stats.value.examVip,
    color: '#7c3aed',
    gradient: 'linear-gradient(90deg, #8b5cf6 0%, #7c3aed 100%)'
  },
  {
    label: '免费考试',
    value: stats.value.examFree,
    color: '#059669',
    gradient: 'linear-gradient(90deg, #34d399 0%, #059669 100%)'
  }
]))

const averageQuestionsPerExam = computed(() => {
  if (!stats.value.totalExam) return '0.0 题/场'
  return `${(stats.value.totalQuestion / stats.value.totalExam).toFixed(1)} 题/场`
})

const averageQuestionsPerBank = computed(() => {
  if (!stats.value.totalBank) return '0.0 题/库'
  return `${(stats.value.totalQuestion / stats.value.totalBank).toFixed(1)} 题/库`
})

const violationDensity = computed(() => {
  if (!stats.value.totalExam) return '0%'
  return `${safeDividePercent(stats.value.totalViolation, stats.value.totalExam)}%`
})

// ==============================================
// 考试 CRUD (简化)
// ==============================================
const examDialog = ref(false)
const examForm = ref({})
const examQuery = ref('')
const availableBankQuestions = computed(() => {
  return questionList.value
      .filter(question =>
          Number(question.sourceType) === 2 ||
          (question.bankId !== null && question.bankId !== undefined)
      )
      .map(question => ({
        id: Number(question.id),
        questionName: question.questionName || '未命名题目',
        bankTitle: question.bankTitle || '未命名题库',
        createTime: question.createTime
      }))
      .sort((a, b) => {
        const bankCompare = a.bankTitle.localeCompare(b.bankTitle, 'zh-CN')
        if (bankCompare !== 0) {
          return bankCompare
        }
        const timeA = dayjs(a.createTime).valueOf()
        const timeB = dayjs(b.createTime).valueOf()
        if (timeA !== timeB) {
          return timeB - timeA
        }
        return a.id - b.id
      })
})
const deleteExam = (id) => {
  const hasQuestions = questionList.value.some(q => Number(q.examId) === Number(id))
  if (hasQuestions) {
    ElMessage.warning('该考试下存在考试题，请先删除相关题目')
    return
  }
  ElMessageBox.confirm('确定要删除该考试吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteExamApi({ id })
      if (res.code === 200) {
        await Promise.all([loadExamList(true), loadQuestionList(true)])
        ElMessage.success('删除成功')
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除考试异常')
    }
  }).catch(() => {})
}

// ==============================================
// 题目 CRUD (增强版)
// ==============================================
const questionDialog = ref(false)
const questionForm = ref({
  questionDesc: '',
  bankId: null,
  examTitle: null,
  fullScore: 10,
  level: 1,
  hint: '',
  inputFormat: '',
  outputFormat: '',
  samples: [],
  testCasesJson: '[]',
  answer: '',
  questionType: 'bank'
})

// 添加/删除样例
const addSample = () => {
  questionForm.value.samples.push({ input: '', output: '' })
}
const removeSample = (index) => {
  questionForm.value.samples.splice(index, 1)
}

// 新增题目初始化
const openQuestionAdd = async () => {
  await Promise.all([loadBankList(true), loadExamList(true)])
  questionForm.value = {
    questionName: '',
    questionDesc: '',
    bankId: null,
    examId: null,
    fullScore: 10,
    level: 1,
    hint: '',
    inputFormat: '',
    outputFormat: '',
    samples: [],
    testCasesJson: '[]',
    answer: '',
    questionType: 'bank'
  }
  questionDialog.value = true
}

// 编辑题目初始化 - 修复回显问题
const openQuestionEdit = (row) => {
  // 确保类型正确
  const questionType = row.bankId ? 'bank' : 'exam'
  // 根据 examId 从 examList 中查找考试标题
  let examTitle = ''
  if (row.examId) {
    const exam = examList.value.find(e => e.id === Number(row.examId))
    examTitle = exam ? exam.examTitle : ''
  }

  questionForm.value = {
    ...row,
    // 确保 bankId 和 examId 类型一致（都转为数字或都保持原样）
    bankId: row.bankId ? Number(row.bankId) : null,
    examId: row.examId ? Number(row.examId) : null,
    examTitle: examTitle, // 根据 examList 计算出的考试标题
    questionType: questionType,
    samples: row.samples ? row.samples.map(s => ({ ...s })) : [],
    testCasesJson: row.testCasesJson || '[]'
  }
  console.log('编辑时 questionForm:', questionForm.value)
  questionDialog.value = true
}

const deleteQuestion = async (id) => {
  // 确认删除提示框
  ElMessageBox.confirm(
      '此操作将永久删除该题目, 是否继续?',
      '提示',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    // 用户点了确认 → 执行删除
    try{
      const res = await deleteQ({
        id:id
      })
      if(res.code === 200){
        questionList.value = questionList.value.filter(i => i.id !== id)
        ElMessage.success('删除成功')
      }else{
        ElMessage.error('删除失败')
      }
    }catch (e) {
      ElMessage.error('删除异常')
    }

  }).catch(() => {
    // 用户点了取消
    ElMessage.info('已取消删除')
  })
}

// ==============================================
// 题库 CRUD
// ==============================================
const bankDialog = ref(false)
const bankForm = ref({})

// ==============================================
// 违规处理
// ==============================================
const handleViolation = (row) => { row.status = 1; ElMessage.success('已标记处理') }

// ==============================================
// 其他功能
// ==============================================
const handleLogout = () => {
  // 第1步：调用store里的logout方法，清除token和用户信息
  userStore.logout()

  // 第2步：弹出提示
  ElMessage.success('退出成功')

  // 第3步：跳转到登录页面
  router.push('/login')
}
const handleRefresh = () => {
  // 刷新时重新加载所有核心数据
  Promise.all([loadQuestionList(true), loadBankList(true), loadExamList(true)])
  ElMessage.success('刷新成功')
}

</script>

<style scoped>
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
/* 在 style 标签中添加 */
.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
}

.action-buttons .el-button {
  margin: 0;
  padding: 5px 8px;
}

/* 确保表格操作列内容垂直居中 */
.el-table .cell {
  display: flex;
  align-items: center;
  justify-content: center;
}
/* 复用原有样式，适当调整弹窗宽度等 */
.teacher-admin-container {
  display: flex;
  height: 100vh;
  background-color: #f5f7fa;
}
.sidebar {
  width: 250px;
  height: 100%;
  background-color: #fff;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 100;
}
.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e8e8e8;
  text-align: center;
}
.sidebar-header h3 {
  margin: 0;
  color: #333;
  font-size: 16px;
  font-weight: 500;
}
.user-info {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #e8e8e8;
}
.user-avatar {
  margin: 0 auto 10px;
}
.user-name {
  font-size: 16px;
  color: #333;
  font-weight: 500;
  margin-bottom: 5px;
}
.user-role {
  font-size: 12px;
  color: #666;
}
.sidebar-menu {
  flex: 1;
  border-right: none;
}
.sidebar-menu :deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  font-size: 14px;
}
.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: #ecf5ff;
  color: #409EFF;
}
.logout-btn-wrap {
  padding: 20px;
  border-top: 1px solid #e8e8e8;
}
.logout-btn {
  color: #F56C6C;
  display: block;
  width: 100%;
  text-align: center;
}
.main-content {
  flex: 1;
  margin-left: 250px;
  padding: 20px;
  overflow-y: auto;
}
.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.header-title {
  font-size: 20px;
  color: #333;
  font-weight: 500;
}
.header-actions {
  display: flex;
  gap: 10px;
}
.content-body {
  background-color: #fff;
  border-radius: 8px;
  min-height: calc(100vh - 120px);
}
.page-content {
  padding: 20px;
}
.page-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}
.stat-card {
  text-align: center;
  padding: 25px 10px;
  font-size: 16px;
  font-weight: bold;
  line-height: 1.7;
}
.stats-overview-grid :deep(.el-card__body) {
  padding: 22px 16px;
}
.stats-chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  margin-top: 20px;
}
.stats-chart-grid.secondary {
  grid-template-columns: 1.1fr 0.9fr;
}
.chart-card {
  border: 1px solid #e8eef5;
}
.chart-card.compact :deep(.el-card__body) {
  padding-top: 18px;
}
.chart-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-weight: 600;
  color: #1f2937;
}
.chart-card-meta {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}
.chart-card-body {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 28px;
  align-items: center;
}
.donut-chart-shell {
  display: flex;
  justify-content: center;
}
.donut-chart {
  width: 190px;
  height: 190px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.donut-chart::after {
  content: '';
  position: absolute;
  inset: 18px;
  border-radius: 50%;
  background: #fff;
}
.donut-chart-center {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #111827;
}
.donut-chart-center strong {
  font-size: 34px;
  line-height: 1;
}
.donut-chart-center span {
  margin-top: 8px;
  font-size: 13px;
  color: #6b7280;
}
.chart-legend {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.legend-row-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}
.legend-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #374151;
  font-size: 14px;
}
.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  flex: none;
}
.legend-value {
  color: #111827;
  font-size: 13px;
  font-weight: 600;
}
.legend-track,
.difficulty-track,
.segment-track {
  width: 100%;
  background: #eef2f7;
  border-radius: 999px;
  overflow: hidden;
}
.legend-track {
  height: 8px;
}
.legend-fill,
.difficulty-fill {
  height: 100%;
  border-radius: inherit;
}
.difficulty-bars {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.difficulty-row {
  padding: 14px 16px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #edf2f7;
}
.difficulty-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}
.difficulty-chip {
  display: inline-flex;
  align-items: center;
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
}
.difficulty-value {
  color: #111827;
  font-weight: 700;
}
.difficulty-track {
  height: 12px;
}
.difficulty-footer {
  margin-top: 8px;
  font-size: 12px;
  color: #6b7280;
}
.segment-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.segment-track {
  display: flex;
  height: 16px;
}
.segment-block {
  height: 100%;
}
.segment-legend {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.segment-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #edf2f7;
}
.summary-metrics {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.summary-metric {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f8fafc 0%, #eef6ff 100%);
  border: 1px solid #e5edf6;
  color: #374151;
}
.summary-metric strong {
  color: #111827;
  font-size: 18px;
}
@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    height: auto;
    position: relative;
  }
  .main-content {
    margin-left: 0;
  }
  .stats-overview-grid :deep(.el-col) {
    max-width: 100%;
    flex: 0 0 100%;
  }
  .stats-chart-grid,
  .stats-chart-grid.secondary,
  .chart-card-body,
  .segment-legend {
    grid-template-columns: 1fr;
  }
  .chart-card-body {
    gap: 20px;
  }
  .donut-chart {
    width: 170px;
    height: 170px;
  }
}
</style>
