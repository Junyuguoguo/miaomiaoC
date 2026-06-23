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
          <img v-if="userStore.getUserAvatar" :src="fixAvatarUrl(userStore.getUserAvatar)" alt="头像" />
          <el-icon v-else><User /></el-icon>
        </el-avatar>
        <div class="user-name">{{ userStore.getUserName || '教师' }}</div>
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
        <el-menu-item index="6">
          <el-icon><Medal /></el-icon>
          <span>VIP设置</span>
        </el-menu-item>
        <el-menu-item index="7">
          <el-icon><Key /></el-icon>
          <span>VIP密钥管理</span>
        </el-menu-item>
        <el-menu-item index="8">
          <el-icon><ChatDotRound /></el-icon>
          <span>在线交流</span>
        </el-menu-item>
        <el-menu-item index="9">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
        <el-menu-item index="10">
          <el-icon><ChatLineSquare /></el-icon>
          <span>聊天室管理</span>
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
              v-if="userStore.getUserRoleId === 4"
              type="primary"
              plain
              @click="$router.push('/admin')"
          >
            <el-icon><Setting /></el-icon>
            返回管理员
          </el-button>
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
          <div class="violation-summary">
            <div class="violation-stat">
              <span>违规记录</span>
              <strong>{{ violationStats.total }}</strong>
            </div>
            <div class="violation-stat">
              <span>涉及学生</span>
              <strong>{{ violationStats.studentCount }}</strong>
            </div>
            <div class="violation-stat danger">
              <span>超过3次</span>
              <strong>{{ violationStats.forceSubmitCount }}</strong>
            </div>
          </div>
          <el-table v-loading="violationLoading" :data="violationList" border empty-text="暂无违规记录">
            <el-table-column label="ID" prop="id" width="80" />
            <el-table-column label="考试" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.examTitle || `考试ID ${row.examId}` }}
              </template>
            </el-table-column>
            <el-table-column label="学生" min-width="210" show-overflow-tooltip>
              <template #default="{ row }">
                <div class="student-cell">
                  <strong>{{ row.studentDisplay || `学生ID ${row.userId}` }}</strong>
                  <span>ID: {{ row.userId }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="切屏次数" width="110" align="center">
              <template #default="{ row }">
                <el-tag :type="Number(row.count) > 3 ? 'danger' : 'warning'">
                  {{ row.count || 0 }}次
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="违规类型" prop="violationType" min-width="170" show-overflow-tooltip />
            <el-table-column label="违规说明" prop="violationDesc" min-width="240" show-overflow-tooltip />
            <el-table-column label="违规时间" min-width="170">
              <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 5. 数据统计 -->
        <div v-if="currentMenu === '5'" class="page-content">
          <div class="page-title">{{ userStore.getUserRoleId === 4 ? '全校数据统计' : '本学院数据统计' }}</div>

          <!-- 概览卡片 -->
          <el-row :gutter="20" class="stats-overview-grid">
            <el-col :span="6">
              <el-card class="stat-card">{{ userStore.getUserRoleId === 4 ? '全校' : '本学院' }}学生数<br/>{{ teacherStats.studentCount ?? '-' }}</el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">平均做题数<br/>{{ teacherStats.avgQuestionCount ?? '-' }}</el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">平均正确率<br/>{{ teacherStats.avgPassRate != null ? teacherStats.avgPassRate + '%' : '-' }}</el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">考试及格率<br/>{{ teacherStats.examPassRate != null ? teacherStats.examPassRate + '%' : '-' }}</el-card>
            </el-col>
          </el-row>

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

          <!-- 学生明细表格 -->
          <el-card style="margin-top:20px;" shadow="never">
            <template #header>
              <div style="display:flex;align-items:center;justify-content:space-between;">
                <span>学生明细</span>
                <el-input v-model="studentKeyword" placeholder="搜索姓名/用户名" clearable style="width:220px;" @clear="loadTeacherStudents" @keyup.enter="loadTeacherStudents">
                  <template #append><el-button @click="loadTeacherStudents">搜索</el-button></template>
                </el-input>
              </div>
            </template>
            <el-table :data="studentList" stripe style="width:100%;" v-loading="studentLoading">
              <el-table-column prop="realName" label="姓名" width="120" />
              <el-table-column prop="username" label="用户名" width="120" />
              <el-table-column prop="questionCount" label="做题数" width="90" sortable />
              <el-table-column label="正确率" width="100" sortable sort-by="questionPassRate">
                <template #default="{ row }">{{ row.questionPassRate != null ? row.questionPassRate + '%' : '-' }}</template>
              </el-table-column>
              <el-table-column prop="examCount" label="考试次数" width="100" sortable />
              <el-table-column label="平均分" width="90" sortable sort-by="avgScore">
                <template #default="{ row }">{{ row.avgScore != null ? Number(row.avgScore).toFixed(1) : '-' }}</template>
              </el-table-column>
              <el-table-column label="最近活跃" min-width="140">
                <template #default="{ row }">{{ row.lastActiveTime || '-' }}</template>
              </el-table-column>
            </el-table>
            <div style="margin-top:16px;display:flex;justify-content:flex-end;">
              <el-pagination
                v-model:current-page="studentPage"
                :page-size="studentPageSize"
                :total="studentTotal"
                layout="total, prev, pager, next"
                @current-change="loadTeacherStudents"
              />
            </div>
          </el-card>
        </div>

        <!-- 6. VIP设置 -->
        <div v-if="currentMenu === '6'" class="page-content vip-manage-page">
          <div class="page-title-row">
            <div>
              <div class="page-title">VIP设置</div>
              <p>配置学生端展示的VIP价格、有效期以及管理员QQ/微信联系方式。</p>
            </div>
            <el-button type="primary" @click="openVipAdd">
              <el-icon><Plus /></el-icon>
              新增套餐
            </el-button>
          </div>

          <el-table v-loading="vipLoading" :data="vipPlanList" border>
            <el-table-column label="套餐" prop="planName" min-width="150" />
            <el-table-column label="价格" width="120" align="center">
              <template #default="{ row }">¥{{ formatVipPrice(row.price) }}</template>
            </el-table-column>
            <el-table-column label="有效天数" prop="durationDays" width="120" align="center" />
            <el-table-column label="QQ" prop="contactQq" min-width="140" show-overflow-tooltip />
            <el-table-column label="微信" prop="contactWechat" min-width="140" show-overflow-tooltip />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.enabled === 1 ? 'success' : 'info'">
                  {{ row.enabled === 1 ? '展示中' : '已隐藏' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="排序" prop="sortOrder" width="90" align="center" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button type="primary" link @click="openVipEdit(row)">编辑</el-button>
                  <el-button type="danger" link @click="removeVipPlan(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 7. VIP密钥管理 -->
        <div v-if="currentMenu === '7'" class="page-content vip-key-page">
          <div class="page-title-row">
            <div>
              <div class="page-title">VIP密钥管理</div>
              <p>生成VIP激活密钥，发给学生即可开通VIP。每个密钥只能使用一次。</p>
            </div>
            <el-button type="primary" @click="keyGenDialog = true">
              <el-icon><Key /></el-icon>
              生成密钥
            </el-button>
          </div>

          <div class="key-stats">
            <el-tag type="info" size="large">未使用: {{ keyStats.unusedCount }}</el-tag>
            <el-tag type="success" size="large">已使用: {{ keyStats.usedCount }}</el-tag>
          </div>

          <el-table v-loading="keyLoading" :data="vipKeyList" border>
            <el-table-column label="密钥" min-width="200">
              <template #default="{ row }">
                <div style="display:flex;align-items:center;gap:8px">
                  <code style="font-size:13px;letter-spacing:1px">{{ row.keyCode }}</code>
                  <el-button :icon="CopyDocument" link type="primary" @click="copyKeyCode(row.keyCode)" />
                </div>
              </template>
            </el-table-column>
            <el-table-column label="天数" prop="durationDays" width="90" align="center" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'warning' : 'success'" size="small">
                  {{ row.status === 0 ? '未使用' : '已使用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="备注" prop="note" min-width="140" show-overflow-tooltip />
            <el-table-column label="使用者" prop="usedByDisplay" min-width="210" show-overflow-tooltip>
              <template #default="{ row }">{{ row.usedByDisplay || row.usedBy || '-' }}</template>
            </el-table-column>
            <el-table-column label="使用时间" min-width="160">
              <template #default="{ row }">{{ row.usedTime || '-' }}</template>
            </el-table-column>
            <el-table-column label="生成时间" min-width="160">
              <template #default="{ row }">{{ row.createTime }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 0" type="danger" link @click="removeVipKey(row)">删除</el-button>
                <span v-else style="color:#999;font-size:12px">-</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 9. 个人中心 -->
        <div v-if="currentMenu === '9'" class="page-content">
          <div class="page-title">个人中心</div>
          <div class="profile-card">
            <div class="profile-avatar-section">
              <el-avatar :size="80" class="profile-avatar">
                <img v-if="profileForm.avatar" :src="profileForm.avatar" alt="头像" />
                <el-icon v-else><User /></el-icon>
              </el-avatar>
              <el-button type="primary" plain size="small" @click="triggerTeacherAvatarUpload">
                更换头像
              </el-button>
              <input ref="teacherAvatarInput" type="file" accept="image/jpeg,image/png,image/webp" style="display:none" @change="onTeacherAvatarChange" />
            </div>
            <el-form :model="profileForm" label-width="80px" class="profile-form">
              <el-form-item label="用户名">
                <el-input v-model="profileForm.username" disabled />
              </el-form-item>
              <el-form-item label="真实姓名">
                <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item label="所属学院">
                <el-input :model-value="profileForm.college" disabled />
                <div class="form-hint">学院由管理员通过邀请码设定，不可修改</div>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="profileSaving" @click="saveTeacherProfile">保存</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 10. 聊天室管理 -->
        <div v-if="currentMenu === '10'" class="page-content">
          <div class="page-title">聊天室管理</div>
          <div style="margin-bottom:15px;">
            <el-button type="primary" @click="showCreateRoomDialog = true">创建聊天室</el-button>
          </div>
          <el-table :data="chatRoomList" border>
            <el-table-column label="ID" prop="id" width="70" />
            <el-table-column label="房间名称" prop="roomName" />
            <el-table-column label="群号" width="120" align="center">
              <template #default="{ row }">
                <span class="group-num">{{ row.groupNumber }}</span>
                <el-button type="primary" link size="small" @click="copyText(row.groupNumber)">复制</el-button>
              </template>
            </el-table-column>
            <el-table-column label="所属学院" width="150">
              <template #default="{ row }">
                <el-tag v-if="row.college" type="warning">{{ row.college }}</el-tag>
                <el-tag v-else type="info">全校大厅</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.roomLevel === 'VIP'" type="danger" effect="dark">VIP</el-tag>
                <el-tag v-else type="success">普通</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="成员数" prop="currentMembers" width="100" align="center" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="openEditRoom(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDeleteRoom(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
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

    <el-dialog v-model="vipDialog" :title="vipForm.id ? '编辑VIP套餐' : '新增VIP套餐'" width="640px">
      <el-form :model="vipForm" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="套餐名称" required>
              <el-input v-model="vipForm.planName" placeholder="例如：月度VIP" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格" required>
              <el-input-number v-model="vipForm.price" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="有效天数" required>
              <el-input-number v-model="vipForm.durationDays" :min="1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="展示状态">
              <el-switch v-model="vipForm.enabled" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="管理员QQ">
              <el-input v-model="vipForm.contactQq" placeholder="学生端可复制" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="微信">
              <el-input v-model="vipForm.contactWechat" placeholder="学生端可复制" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="套餐权益">
          <el-input v-model="vipForm.benefits" type="textarea" :rows="3" placeholder="例如：VIP考试、VIP题库、冲刺资料" />
        </el-form-item>
        <el-form-item label="联系说明">
          <el-input v-model="vipForm.contactNote" type="textarea" :rows="2" placeholder="例如：联系时发送账号、套餐名称和付款截图" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="vipForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="vipDialog = false">取消</el-button>
        <el-button type="primary" @click="submitVipPlan">保存</el-button>
      </template>
    </el-dialog>

    <!-- 密钥生成弹窗 -->
    <el-dialog v-model="keyGenDialog" title="生成VIP密钥" width="480px">
      <el-form :model="keyGenForm" label-width="100px">
        <el-form-item label="开通天数" required>
          <el-select v-model="keyGenForm.durationDays" style="width:100%">
            <el-option label="30天（月卡）" :value="30" />
            <el-option label="90天（季卡）" :value="90" />
            <el-option label="180天（半年卡）" :value="180" />
            <el-option label="365天（年卡）" :value="365" />
          </el-select>
        </el-form-item>
        <el-form-item label="生成数量" required>
          <el-input-number v-model="keyGenForm.count" :min="1" :max="50" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="keyGenForm.note" placeholder="可选，例如：XX班期末考试专用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="keyGenDialog = false">取消</el-button>
        <el-button type="primary" @click="handleGenerateKeys">生成</el-button>
      </template>
    </el-dialog>

    <!-- 创建聊天室弹窗 -->
    <el-dialog v-model="showCreateRoomDialog" title="创建聊天室" width="400px">
      <el-form :model="newRoomForm" label-width="80px">
        <el-form-item label="房间名称">
          <el-input v-model="newRoomForm.name" placeholder="如：计算机学院交流群" />
        </el-form-item>
        <el-form-item label="所属学院">
          <el-select v-model="newRoomForm.college" placeholder="留空为全校大厅" clearable style="width:100%">
            <el-option label="计算机学院" value="计算机学院" />
            <el-option label="机械学院" value="机械学院" />
            <el-option label="电子信息学院" value="电子信息学院" />
            <el-option label="经济管理学院" value="经济管理学院" />
            <el-option label="外国语学院" value="外国语学院" />
            <el-option label="理学院" value="理学院" />
            <el-option label="人文社科学院" value="人文社科学院" />
            <el-option label="自动化学院" value="自动化学院" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间类型">
          <el-radio-group v-model="newRoomForm.roomLevel">
            <el-radio value="FREE">普通（免费用户自动加入）</el-radio>
            <el-radio value="VIP">VIP专属（需群号加入）</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateRoomDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateRoom">创建</el-button>
      </template>
    </el-dialog>

    <!-- 编辑聊天室弹窗 -->
    <el-dialog v-model="showEditRoomDialog" title="编辑聊天室" width="400px">
      <el-form :model="editRoomForm" label-width="80px">
        <el-form-item label="房间名称">
          <el-input v-model="editRoomForm.roomName" placeholder="房间名称" />
        </el-form-item>
        <el-form-item label="所属学院">
          <el-select v-model="editRoomForm.college" placeholder="留空为全校大厅" clearable style="width:100%">
            <el-option label="计算机学院" value="计算机学院" />
            <el-option label="机械学院" value="机械学院" />
            <el-option label="电子信息学院" value="电子信息学院" />
            <el-option label="经济管理学院" value="经济管理学院" />
            <el-option label="外国语学院" value="外国语学院" />
            <el-option label="理学院" value="理学院" />
            <el-option label="人文社科学院" value="人文社科学院" />
            <el-option label="自动化学院" value="自动化学院" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间类型">
          <el-radio-group v-model="editRoomForm.roomLevel">
            <el-radio value="FREE">普通（免费用户自动加入）</el-radio>
            <el-radio value="VIP">VIP专属（需群号加入）</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditRoomDialog = false">取消</el-button>
        <el-button type="primary" @click="handleEditRoom">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>



<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User, Document, Edit, Folder, Warning, DataAnalysis,
  SwitchButton, Refresh, Medal, Plus, Key, CopyDocument, ChatLineSquare, Setting
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from "@/stores/user"
import { updateUserInfo, uploadAvatar } from '@/api/auth.js'
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.css'
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
import {
  deleteVipPlan,
  getManageVipPlans,
  saveVipPlan
} from "@/api/vip.js"
import {
  generateVipKeys,
  listVipKeys,
  deleteVipKey
} from "@/api/vip-key.js"
import { getViolationRecords } from "@/api/exam.js"
import { getTeacherStatsOverview, getTeacherStatsStudents } from '@/api/teacher-stats'
import { getRooms as getRoomsApi, createRoom as createRoomApi, deleteRoom as deleteRoomApi, updateRoom as updateRoomApi } from '@/api/chat'
import * as echarts from 'echarts/core'
import { BarChart, PieChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([BarChart, PieChart, LineChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent, CanvasRenderer])

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

const vipLoading = ref(false)
const vipPlanList = ref([])
const vipDialog = ref(false)
const vipForm = ref({
  planName: '',
  price: 0,
  durationDays: 30,
  contactQq: '',
  contactWechat: '',
  contactNote: '',
  benefits: '',
  enabled: 1,
  sortOrder: 0
})

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
    await loadViolationRecords(true)
  } else if (index === '5') {
    console.log('加载统计数据')
    loadTeacherStats()
    loadTeacherStudents()
  } else if (index === '6') {
    console.log('加载VIP套餐')
    await loadVipPlanList()
  } else if (index === '7') {
    console.log('加载VIP密钥')
    await loadVipKeyList(true)
  } else if (index === '8') {
    router.push('/chat')
    return
  } else if (index === '9') {
    // 个人中心 — 加载用户信息
    loadTeacherProfile()
  } else if (index === '10') {
    await loadChatRooms()
  }
  currentMenu.value = index
  const titleMap = {
    '1': '模拟考试管理',
    '2': '题目管理',
    '3': '题库管理',
    '4': '违规记录管理',
    '5': '数据统计',
    '6': 'VIP设置',
    '7': 'VIP密钥管理',
    '8': '在线交流',
    '9': '个人中心',
    '10': '聊天室管理'
  }
  currentTitle.value = titleMap[index]
}

// === 个人中心 ===
const profileForm = ref({ username: '', realName: '', phone: '', email: '', avatar: '', college: '' })
const profileSaving = ref(false)
const teacherAvatarInput = ref(null)

const fixAvatarUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('/uploads/')) return '/api/auth/avatar/' + url.split('/').pop()
  return url
}

const loadTeacherProfile = () => {
  const info = userStore.$state.userInfo
  profileForm.value = {
    username: info.username || '',
    realName: info.real_name || info.realName || '',
    phone: info.phone || '',
    email: info.email || '',
    avatar: fixAvatarUrl(info.avatar),
    college: info.college || ''
  }
}

const saveTeacherProfile = async () => {
  profileSaving.value = true
  try {
    const userId = userStore.getUserId
    const res = await updateUserInfo({
      userId,
      avatar: profileForm.value.avatar,
      real_name: profileForm.value.realName,
      phone: profileForm.value.phone,
      email: profileForm.value.email,
      college: profileForm.value.college,
      school: userStore.$state.userInfo.school || '',
      major: userStore.$state.userInfo.major || '',
      score: userStore.$state.userInfo.score || ''
    })
    if (res && res.code === 200) {
      userStore.updateUserInfo({
        real_name: profileForm.value.realName,
        phone: profileForm.value.phone,
        email: profileForm.value.email,
        avatar: profileForm.value.avatar,
        college: profileForm.value.college
      })
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res?.message || '保存失败')
    }
  } catch {
    ElMessage.error('保存失败')
  } finally {
    profileSaving.value = false
  }
}

const triggerTeacherAvatarUpload = () => {
  teacherAvatarInput.value?.click()
}

const onTeacherAvatarChange = (e) => {
  const file = e.target.files[0]
  if (!file) return
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
    ElMessage.error('仅支持 JPG、PNG、WebP 格式')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 5MB')
    return
  }
  const reader = new FileReader()
  reader.onload = async (ev) => {
    try {
      const blob = await cropImageSimple(ev.target.result)
      const avatarFile = new File([blob], 'avatar.jpg', { type: 'image/jpeg' })
      const res = await uploadAvatar(avatarFile)
      if (res && res.code === 200) {
        profileForm.value.avatar = res.data
        ElMessage.success('头像上传成功')
      } else {
        ElMessage.error(res?.message || '上传失败')
      }
    } catch {
      ElMessage.error('头像上传失败')
    }
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

// Simple crop using canvas (no dialog needed for teacher)
const cropImageSimple = (src) => {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => {
      const size = Math.min(img.width, img.height)
      const sx = (img.width - size) / 2
      const sy = (img.height - size) / 2
      const canvas = document.createElement('canvas')
      canvas.width = 200
      canvas.height = 200
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, sx, sy, size, size, 0, 0, 200, 200)
      canvas.toBlob(b => b ? resolve(b) : reject(new Error('crop failed')), 'image/jpeg', 0.85)
    }
    img.onerror = reject
    img.src = src
  })
}

const resetVipForm = () => {
  vipForm.value = {
    planName: '',
    price: 0,
    durationDays: 30,
    contactQq: '',
    contactWechat: '',
    contactNote: '联系时请发送账号、套餐名称和付款截图。',
    benefits: '解锁VIP考试、VIP题库、重点练习资料和后续新增会员内容。',
    enabled: 1,
    sortOrder: vipPlanList.value.length + 1
  }
}

const loadVipPlanList = async (forceRefresh = false) => {
  if (vipPlanList.value.length > 0 && !forceRefresh) {
    return
  }
  try {
    vipLoading.value = true
    const res = await getManageVipPlans()
    if (res.code === 200) {
      vipPlanList.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载VIP套餐失败')
    }
  } catch (error) {
    console.error('加载VIP套餐失败:', error)
    ElMessage.error(error.response?.data?.message || '加载VIP套餐异常')
  } finally {
    vipLoading.value = false
  }
}

const openVipAdd = () => {
  resetVipForm()
  vipDialog.value = true
}

const openVipEdit = (row) => {
  vipForm.value = {
    ...row,
    price: Number(row.price || 0),
    durationDays: Number(row.durationDays || 30),
    enabled: Number(row.enabled ?? 1),
    sortOrder: Number(row.sortOrder || 0)
  }
  vipDialog.value = true
}

const submitVipPlan = async () => {
  if (!vipForm.value.planName?.trim()) {
    ElMessage.warning('请输入套餐名称')
    return
  }
  if (Number(vipForm.value.durationDays) <= 0) {
    ElMessage.warning('有效天数必须大于0')
    return
  }
  try {
    const res = await saveVipPlan(vipForm.value)
    if (res.code === 200) {
      ElMessage.success('VIP套餐已保存')
      vipDialog.value = false
      await loadVipPlanList(true)
    } else {
      ElMessage.error(res.message || '保存VIP套餐失败')
    }
  } catch (error) {
    console.error('保存VIP套餐失败:', error)
    ElMessage.error(error.response?.data?.message || '保存VIP套餐异常')
  }
}

const removeVipPlan = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除“${row.planName}”吗？`, '删除VIP套餐', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteVipPlan({ id: row.id })
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadVipPlanList(true)
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除VIP套餐失败:', error)
      ElMessage.error('删除VIP套餐失败')
    }
  }
}

const formatVipPrice = (price) => {
  return new Intl.NumberFormat('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(Number(price || 0))
}

// ========== VIP密钥管理 ==========
const keyGenDialog = ref(false)
const keyLoading = ref(false)
const vipKeyList = ref([])
const keyStats = ref({ unusedCount: 0, usedCount: 0 })
const keyGenForm = ref({ durationDays: 30, count: 5, note: '' })

const loadVipKeyList = async (forceRefresh = false) => {
  if (vipKeyList.value.length > 0 && !forceRefresh) return
  keyLoading.value = true
  try {
    const res = await listVipKeys({ userId: userStore.userInfo?.id })
    if (res.code === 200 && res.data) {
      vipKeyList.value = res.data.keys || []
      keyStats.value = { unusedCount: res.data.unusedCount || 0, usedCount: res.data.usedCount || 0 }
    }
  } catch (error) {
    ElMessage.error('加载密钥列表失败')
  } finally {
    keyLoading.value = false
  }
}

const handleGenerateKeys = async () => {
  try {
    const res = await generateVipKeys({
      userId: userStore.userInfo?.id,
      durationDays: keyGenForm.value.durationDays,
      count: keyGenForm.value.count,
      note: keyGenForm.value.note || ''
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '生成成功')
      keyGenDialog.value = false
      await loadVipKeyList(true)
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch (error) {
    ElMessage.error('生成密钥失败')
  }
}

const copyKeyCode = (code) => {
  navigator.clipboard.writeText(code).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.warning('复制失败，请手动复制')
  })
}

const removeVipKey = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该密钥？', '删除密钥', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteVipKey({ id: row.id })
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadVipKeyList(true)
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const formatDateTime = (value) => {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'
}

// 页面加载时初始化 - 修复白屏问题
onMounted(async () => {
  console.log('教师端页面加载，初始化数据...')
  // 根据默认菜单加载对应数据
  await handleMenuSelect(currentMenu.value)
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
const violationLoading = ref(false)
const violationList = ref([])

// 本学院数据统计
const teacherStats = ref({})
const studentList = ref([])
const studentKeyword = ref('')
const studentPage = ref(1)
const studentPageSize = ref(20)
const studentTotal = ref(0)
const studentLoading = ref(false)

// 聊天室管理
const chatRoomList = ref([])
const showCreateRoomDialog = ref(false)
const newRoomForm = ref({ name: '', college: '', roomLevel: 'FREE' })

const loadViolationRecords = async (forceRefresh = false) => {
  if (violationList.value.length > 0 && !forceRefresh) return
  violationLoading.value = true
  try {
    const res = await getViolationRecords({ userId: userStore.userInfo?.id })
    if (res.code === 200) {
      const records = res.data?.records || res.data || []
      violationList.value = records.map(item => ({
        ...item,
        count: Number(item.count || 0)
      }))
    } else {
      violationList.value = []
      ElMessage.warning(res.message || '加载违规记录失败')
    }
  } catch (error) {
    violationList.value = []
    ElMessage.error('加载违规记录异常')
  } finally {
    violationLoading.value = false
  }
}

const violationStats = computed(() => {
  const studentIds = new Set(violationList.value.map(item => item.userId).filter(Boolean))
  return {
    total: violationList.value.length,
    studentCount: studentIds.size,
    forceSubmitCount: violationList.value.filter(item => Number(item.count) > 3).length
  }
})

const gradeChartRef = ref(null)
const questionChartRef = ref(null)
const activityChartRef = ref(null)
let gradeChartInstance = null
let questionChartInstance = null
let activityChartInstance = null

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

const calculateActivityData = (students) => {
  const today = new Date()
  const days = []
  const counts = []

  const parsedDates = students.map(s => {
    if (!s.lastActiveTime) return null
    const d = new Date(s.lastActiveTime)
    return { year: d.getFullYear(), month: d.getMonth(), date: d.getDate() }
  })

  for (let i = 6; i >= 0; i--) {
    const date = new Date(today)
    date.setDate(date.getDate() - i)
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    days.push(`${month}-${day}`)

    const targetYear = date.getFullYear()
    const targetMonth = date.getMonth()
    const targetDate = date.getDate()

    let count = 0
    for (let j = 0; j < parsedDates.length; j++) {
      const pd = parsedDates[j]
      if (pd && pd.year === targetYear && pd.month === targetMonth && pd.date === targetDate) {
        count++
      }
    }
    counts.push(count)
  }

  return { days, counts }
}

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

// 本学院统计方法
const loadTeacherStats = async () => {
  try {
    const res = await getTeacherStatsOverview()
    if (res && res.code === 200) teacherStats.value = res.data || {}
  } catch (e) { console.error('加载统计概览失败:', e) }
}

const loadTeacherStudents = async () => {
  studentLoading.value = true
  try {
    const res = await getTeacherStatsStudents({ keyword: studentKeyword.value || undefined, page: studentPage.value - 1, size: studentPageSize.value })
    if (res && res.code === 200) {
      studentList.value = res.data.students || []
      studentTotal.value = res.data.total || 0
      
      await nextTick()
      renderGradeChart()
      renderQuestionChart()
      renderActivityChart()
    }
  } catch (e) { console.error('加载学生列表失败:', e) }
  finally { studentLoading.value = false }
}

// 聊天室管理方法
const loadChatRooms = async () => {
  try {
    const res = await getRoomsApi()
    if (res && res.code === 200) chatRoomList.value = res.data || []
  } catch { ElMessage.error('加载聊天室失败') }
}

const handleCreateRoom = async () => {
  if (!newRoomForm.value.name.trim()) { ElMessage.warning('请输入房间名称'); return }
  try {
    const res = await createRoomApi({ name: newRoomForm.value.name, college: newRoomForm.value.college || null, roomLevel: newRoomForm.value.roomLevel || 'FREE' })
    if (res && res.code === 200) {
      ElMessage.success('创建成功')
      showCreateRoomDialog.value = false
      newRoomForm.value = { name: '', college: '', roomLevel: 'FREE' }
      await loadChatRooms()
    }
  } catch { ElMessage.error('创建失败') }
}

const handleDeleteRoom = async (roomId) => {
  try {
    await ElMessageBox.confirm('确定删除该聊天室？', '提示', { type: 'warning' })
    const res = await deleteRoomApi(roomId)
    if (res && res.code === 200) { ElMessage.success('删除成功'); await loadChatRooms() }
  } catch {}
}

// 编辑聊天室
const showEditRoomDialog = ref(false)
const editRoomForm = ref({ id: null, roomName: '', college: '', roomLevel: 'FREE' })

const openEditRoom = (row) => {
  editRoomForm.value = {
    id: row.id,
    roomName: row.roomName || '',
    college: row.college || '',
    roomLevel: row.roomLevel || 'FREE'
  }
  showEditRoomDialog.value = true
}

const handleEditRoom = async () => {
  if (!editRoomForm.value.roomName.trim()) { ElMessage.warning('请输入房间名称'); return }
  try {
    const res = await updateRoomApi(editRoomForm.value.id, {
      roomName: editRoomForm.value.roomName,
      college: editRoomForm.value.college || null,
      roomLevel: editRoomForm.value.roomLevel
    })
    if (res && res.code === 200) {
      ElMessage.success('修改成功')
      showEditRoomDialog.value = false
      await loadChatRooms()
    }
  } catch { ElMessage.error('修改失败') }
}

const copyText = (text) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制: ' + text)
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

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
  if (currentMenu.value === '6') {
    loadVipPlanList(true)
  } else if (currentMenu.value === '7') {
    loadVipKeyList(true)
  } else if (currentMenu.value === '4') {
    loadViolationRecords(true)
  } else if (currentMenu.value === '10') {
    loadChatRooms()
  } else {
    // 刷新时重新加载所有核心数据
    Promise.all([loadQuestionList(true), loadBankList(true), loadExamList(true)])
  }
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
.violation-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}
.violation-stat {
  border: 1px solid #e8eef5;
  border-radius: 8px;
  padding: 14px 16px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.violation-stat span {
  color: #64748b;
  font-size: 13px;
}
.violation-stat strong {
  color: #1f2937;
  font-size: 22px;
}
.violation-stat.danger strong {
  color: #f56c6c;
}
.student-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.student-cell strong {
  color: #1f2937;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.student-cell span {
  color: #94a3b8;
  font-size: 12px;
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

/* Visual refresh: operational admin console */
.teacher-admin-container {
  min-height: 100vh;
  background: var(--app-bg);
}

.sidebar {
  background: var(--app-surface);
  border-right: 1px solid var(--app-border);
  box-shadow: none;
}

.sidebar-header,
.user-info,
.logout-btn-wrap {
  border-color: var(--app-border);
}

.sidebar-header h3,
.header-title,
.page-title {
  color: var(--app-text);
  font-weight: 750;
  letter-spacing: 0;
}

.user-name {
  color: var(--app-text);
  font-weight: 700;
}

.user-role {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  margin-top: 8px;
  padding: 3px 10px;
  color: var(--app-primary);
  background: var(--app-primary-soft);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.sidebar-menu :deep(.el-menu-item) {
  margin: 4px 12px;
  border-radius: 8px;
  color: var(--app-text-muted);
  font-weight: 650;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-menu-item.is-active) {
  color: var(--app-primary);
  background: var(--app-primary-soft);
}

.main-content {
  background:
      linear-gradient(180deg, #f8fbff 0%, var(--app-bg) 42%, #edf4fb 100%);
}

.content-body {
  background: transparent;
  border-radius: 0;
}

.page-content {
  padding: 4px 0 28px;
}

.page-title {
  margin-bottom: 18px;
  padding-bottom: 0;
  border-bottom: 0;
}

.page-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
  padding: 22px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  box-shadow: var(--app-shadow-sm);
}

.page-title-row .page-title {
  margin-bottom: 6px;
}

.page-title-row p {
  margin: 0;
  color: var(--app-text-muted);
}

.stat-card,
.chart-card,
.vip-manage-page :deep(.el-table) {
  border-radius: var(--app-radius);
  box-shadow: var(--app-shadow-sm);
}

.stat-card {
  color: var(--app-text);
  border: 1px solid var(--app-border);
}

.chart-card {
  border-color: var(--app-border);
}

.summary-metric,
.segment-row {
  border-radius: var(--app-radius-sm);
  background: var(--app-surface-soft);
  border-color: var(--app-border);
}

.vip-manage-page :deep(.el-table) {
  border: 1px solid var(--app-border);
  overflow: hidden;
}

.vip-key-page .key-stats {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.vip-key-page :deep(.el-table) {
  border-radius: var(--app-radius);
  box-shadow: var(--app-shadow-sm);
  border: 1px solid var(--app-border);
  overflow: hidden;
}

@media (max-width: 768px) {
  .teacher-admin-container {
    display: block;
  }

  .main-content {
    padding: 16px;
  }

  .page-title-row {
    flex-direction: column;
  }
}

/* 个人中心 */
.profile-card {
  max-width: 500px;
  background: #fff;
  border-radius: 10px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.profile-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.profile-avatar {
  border: 3px solid var(--app-primary-soft, #e8f0ff);
}

.profile-form {
  max-width: 400px;
}

.form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.chart-card {
  margin-bottom: 0;
}

.chart-container {
  height: 300px;
  width: 100%;
}
</style>
