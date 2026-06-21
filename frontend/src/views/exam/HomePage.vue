<template>
  <div class="student-center-container student-shell">
    <!-- 侧边导航栏 -->
    <div class="sidebar student-sidebar">
      <!-- 系统标题 -->
      <div class="sidebar-header">
        <BrandMark
            :title="title || '机试在线考试系统'"
            :subtitle="version ? `MiaomiaoC ${version}` : '学校机试刷题与模拟考试平台'"
        />
      </div>
      <!-- 用户信息 -->
      <div class="user-info">
        <el-avatar :size="60" class="user-avatar">
          <!-- 如果有头像就显示图片，否则显示默认图标 -->
          <img v-if="userInfo.avatar" :src="fixAvatarUrl(userInfo.avatar)" alt="头像" />
          <el-icon v-else><User /></el-icon>
        </el-avatar>
        <div class="user-name">{{ studentDisplayName }}</div>
        <div class="user-role">{{ userRoleName }}</div>
      </div>
      <!-- 导航菜单 -->
      <el-menu
          :default-active="currentMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
      >
        <el-menu-item index="1">
          <el-icon><UserFilled /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
        <el-menu-item index="2" @click="handleToExamRecord">
          <el-icon><Document /></el-icon>
          <span>考试记录</span>
        </el-menu-item>
        <!-- 左侧菜单 - 仅加@click，其他不动 -->
        <el-menu-item index="4" @click="handleToOnlineExam">
          <el-icon><Timer /></el-icon>
          <span>在线考试</span>
        </el-menu-item>
        <el-menu-item index="5" @click="handleToQuestionList">
          <el-icon><Collection /></el-icon>
          <span>题库合集</span>
        </el-menu-item>
        <el-menu-item index="6" @click="handleToVipUpgrade">
          <el-icon><Medal /></el-icon>
          <span>升级VIP</span>
        </el-menu-item>
        <el-menu-item index="7" @click="handleToChat">
          <el-icon><ChatDotRound /></el-icon>
          <span>在线交流</span>
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
    <div class="main-content student-main">
      <!-- 顶部操作栏 -->
      <div class="content-header student-topbar">
        <div class="header-title-wrap">
          <div class="header-title">{{ currentTitle }}</div>
          <p>机试在线刷题与模拟考试平台</p>
        </div>
        <div class="student-search-pill">
          <el-icon><Search /></el-icon>
          <span>搜索题目、知识点、试卷...</span>
          <kbd>Ctrl + K</kbd>
        </div>
        <div class="header-actions">
          <el-button
              v-if="userInfo.RoleId === 1"
              type="primary"
              plain
              @click="handleToVipUpgrade"
          >
            <el-icon><Medal /></el-icon>
            升级VIP
          </el-button>
          <el-button
              type="text"
              @click="handleRefresh"
          >
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-dropdown @command="handleCommand">
            <el-button type="text">
              <el-icon><Setting /></el-icon>
              设置
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="nothing">暂无</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      <!-- 内容切换区域 -->
      <div class="content-body student-content">
        <!-- 1. 个人中心 -->
        <div v-if="currentMenu === '1'" class="page-content personal-center">
          <section class="student-hero dashboard-hero">
            <div>
              <p class="eyebrow">Student Workspace</p>
              <h1>欢迎回来，{{ studentDisplayName }}！</h1>
              <p>
                今天也要加油学习。保持练习节奏，把每一次机试训练都变成稳定进步。
              </p>
              <div class="hero-actions">
                <el-button type="primary" size="large" @click="handleToQuestionList">
                  <el-icon><Collection /></el-icon>
                  开始刷题
                </el-button>
                <el-button size="large" plain @click="handleToOnlineExam">
                  <el-icon><Timer /></el-icon>
                  模拟考试
                </el-button>
              </div>
            </div>
            <div class="student-code-orbit dashboard-orbit" aria-hidden="true">
              <span class="student-code-orbit__mark">C</span>
              <span class="student-code-chip student-code-chip--one">&lt;/&gt;</span>
              <span class="student-code-chip student-code-chip--two">main.c</span>
            </div>
          </section>

          <div class="dashboard-layout">
            <SoftCard class="profile-panel" padding="lg">
              <div class="section-heading">
                <div>
                  <h2>我的信息</h2>
                  <p>{{ isVipActive ? 'VIP 学习权益已开启' : '完善资料后开始系统训练' }}</p>
                </div>
                <el-button type="primary" plain @click="handleEditInfo">
                  <el-icon><Edit /></el-icon>
                  编辑资料
                </el-button>
              </div>
              <div class="profile-summary">
                <el-avatar :size="76" class="user-avatar">
                  <img v-if="userInfo.avatar" :src="fixAvatarUrl(userInfo.avatar)" alt="头像" />
                  <el-icon v-else><User /></el-icon>
                </el-avatar>
                <div>
                  <strong>{{ studentDisplayName }}</strong>
                  <span>{{ userRoleName }}</span>
                </div>
              </div>
              <div class="info-grid dashboard-info-grid">
                <div class="info-item">
                  <span class="label">用户名</span>
                  <span class="value">{{ userInfo.username || '--' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">真实姓名</span>
                  <span class="value">{{ personalRealNameText }}</span>
                </div>
                <div class="info-item">
                  <span class="label">学校</span>
                  <span class="value">{{ userInfo.school || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">专业</span>
                  <span class="value">{{ userInfo.major || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">手机号</span>
                  <span class="value">{{ userInfo.phone || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">邮箱</span>
                  <span class="value">{{ userInfo.email || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">会员到期</span>
                  <span class="value">{{ userInfo.vipExpireTime ? formatDate(userInfo.vipExpireTime) : '无' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">隐藏真名</span>
                  <span class="value">
                    <el-switch
                        v-model="hideRealNameOnStudentPage"
                        :disabled="!hasRealName"
                        active-text="隐藏"
                        inactive-text="显示"
                        @change="handleRealNamePrivacyChange"
                    />
                  </span>
                </div>
              </div>
            </SoftCard>

            <SoftCard class="task-panel" padding="lg">
              <div class="section-heading">
                <div>
                  <h2>今日学习任务</h2>
                  <p>完成关键动作，保持机试手感。</p>
                </div>
              </div>
              <div class="task-progress">
                <ProgressRing
                    :value="completedTaskCount"
                    :max="dashboardTasks.length"
                    :label="`${completedTaskCount}/${dashboardTasks.length}`"
                    caption="任务完成"
                    tone="purple"
                />
                <ul class="task-list">
                  <li
                      v-for="task in dashboardTasks"
                      :key="task.label"
                      :class="{ done: task.done }"
                  >
                    <span aria-hidden="true">{{ task.done ? '✓' : '' }}</span>
                    {{ task.label }}
                  </li>
                </ul>
              </div>
            </SoftCard>
          </div>

          <div class="quick-actions-grid">
            <QuickActionCard title="继续考试" description="进入在线考试列表" tone="blue" @select="handleToOnlineExam">
              <template #icon><el-icon><Document /></el-icon></template>
            </QuickActionCard>
            <QuickActionCard title="题库练习" description="按题库巩固知识点" tone="green" @select="handleToQuestionList">
              <template #icon><el-icon><Collection /></el-icon></template>
            </QuickActionCard>
            <QuickActionCard title="错题本" description="复盘近期薄弱题目" tone="purple" @select="handleToQuestionList">
              <template #icon><el-icon><Warning /></el-icon></template>
            </QuickActionCard>
            <QuickActionCard title="聊天中心" description="交流解题思路" tone="cyan" @select="handleToChat">
              <template #icon><el-icon><ChatDotRound /></el-icon></template>
            </QuickActionCard>
          </div>

          <div class="student-grid student-grid--4">
            <MetricCard label="参加考试次数" :value="stats.examCount ?? '--'" hint="累计记录" trend="+练习" tone="blue">
              <template #icon><el-icon><Document /></el-icon></template>
            </MetricCard>
            <MetricCard label="平均通过率" :value="formattedPassRate" hint="基于学习统计" tone="green">
              <template #icon><el-icon><TrendCharts /></el-icon></template>
            </MetricCard>
            <MetricCard label="笔记数量" :value="stats.noteCount ?? '--'" hint="复习沉淀" tone="purple">
              <template #icon><el-icon><EditPen /></el-icon></template>
            </MetricCard>
            <MetricCard label="完成题目数" :value="stats.questionCount ?? '--'" hint="累计刷题" tone="orange">
              <template #icon><el-icon><Clock /></el-icon></template>
            </MetricCard>
          </div>

          <div class="dashboard-lower-grid">
            <SoftCard padding="lg">
              <div class="section-heading">
                <div>
                  <h2>学习日历</h2>
                  <p>连续学习 {{ learningDays || '--' }} 天</p>
                </div>
              </div>
              <div class="calendar-strip">
                <span v-for="day in calendarDays" :key="day" :class="{ active: day === '今' }">{{ day }}</span>
              </div>
            </SoftCard>
            <SoftCard padding="lg">
              <div class="section-heading">
                <div>
                  <h2>最近成就</h2>
                  <p>{{ achievementText }}</p>
                </div>
              </div>
              <div class="achievement-row">
                <span class="achievement-medal" aria-hidden="true">C</span>
                <div>
                  <strong>{{ achievementTitle }}</strong>
                  <small>继续保持训练节奏，下一次模拟考试会更稳。</small>
                </div>
              </div>
            </SoftCard>
          </div>
        </div>
        <!-- 编辑资料模态框 -->
        <el-dialog
            v-model="editInfoDialogVisible"
            :title="mustCompleteRealName ? '请填写真实姓名' : '编辑个人资料'"
            width="600px"
            :close-on-click-modal="!mustCompleteRealName"
            :close-on-press-escape="!mustCompleteRealName"
            :show-close="!mustCompleteRealName"
            @close="resetEditForm"
        >
          <!-- 头像选择区域 -->
          <div style="text-align: center; margin-bottom: 20px;">
            <!-- 当前头像预览 -->
            <div class="avatar-preview" @click="showAvatarSelector = true">
              <el-avatar :size="100" class="preview-avatar">
                <img v-if="selectedAvatar" :src="fixAvatarUrl(selectedAvatar)" alt="头像" />
                <el-icon v-else><User /></el-icon>
              </el-avatar>
              <div class="avatar-tip">点击选择头像</div>
            </div>
          </div>

          <el-form
              ref="editFormRef"
              :model="editForm"
              :rules="editFormRules"
              label-width="100px"
          >
            <el-form-item label="真实姓名" prop="real_name">
              <el-input v-model="editForm.real_name" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="editForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="报考院校" prop="school">
              <el-select v-model="editForm.school" placeholder="请选择报考院校" style="width: 100%;">
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
            <el-form-item label="报考专业" prop="major">
              <el-input v-model="editForm.major" placeholder="请输入报考专业" />
            </el-form-item>
            <el-form-item label="初试分数" prop="score">
              <el-input v-model="editForm.score" placeholder="请输入初试分数" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="editForm.email" placeholder="请输入邮箱" type="email" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button v-if="!mustCompleteRealName" @click="editInfoDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitEditInfo">提交</el-button>
          </template>
        </el-dialog>

        <!-- 头像选择弹窗 -->
        <el-dialog
            v-model="showAvatarSelector"
            title="选择头像"
            width="500px"
            :append-to-body="true"
            destroy-on-close
        >
          <div class="avatar-selector-modal">
            <div class="avatar-list-modal">
              <div
                  v-for="avatar in avatarList"
                  :key="avatar.id"
                  class="avatar-item-modal"
                  :class="{ active: selectedAvatar === avatar.url }"
                  @click="confirmSelectAvatar(avatar.url)"
              >
                <el-avatar :size="80">
                  <img :src="avatar.url" :alt="avatar.name" />
                </el-avatar>
                <div class="avatar-name-modal">{{ avatar.name }}</div>
              </div>
            </div>
            <el-divider />
            <div class="upload-avatar-section">
              <el-button type="primary" plain @click="triggerAvatarUpload">
                <el-icon><Upload /></el-icon>
                上传自定义头像
              </el-button>
            </div>
          </div>
          <template #footer>
            <el-button @click="showAvatarSelector = false">取消</el-button>
            <el-button type="primary" @click="showAvatarSelector = false">确定</el-button>
          </template>
        </el-dialog>

        <!-- 裁剪头像弹窗 -->
        <el-dialog
            v-model="cropDialogVisible"
            title="裁剪头像"
            width="460px"
            :append-to-body="true"
            :close-on-click-modal="false"
            @close="closeCropDialog"
        >
          <div class="crop-container">
            <img ref="cropImageRef" class="crop-image" alt="裁剪图片" />
          </div>
          <template #footer>
            <el-button @click="closeCropDialog">取消</el-button>
            <el-button type="primary" :loading="cropUploading" @click="confirmCropUpload">
              确认上传
            </el-button>
          </template>
        </el-dialog>
        <!-- 2. 考试记录 -->
        <div v-if="currentMenu === '2'" class="page-content exam-record">
          <el-card shadow="never">
            <div class="search-bar">
              <el-input
                  v-model="searchKeyword"
                  placeholder="请输入考试名称搜索"
                  :prefix-icon="Search"
                  class="search-input"
                  clearable
              />
            </div>
            <el-table :data="filteredExamRecordList" border style="width: 100%">
              <el-table-column prop="examName" label="考试名称" min-width="200" />
              <el-table-column label="考试时间" width="200">
                <template #default="{ row }">
                  {{ row.examTime ? formatDate(row.examTime) : '暂无' }}
                </template>
              </el-table-column>
              <el-table-column prop="score" label="成绩" width="100">
                <template #default="{ row }">
                  <span :class="row.score >= 60 ? 'pass-score' : 'fail-score'">
                    {{ row.score }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '及格' : '不及格' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button type="primary" link size="small" @click="handleViewDetail(row)">查看详情</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
                v-model:current-page="examPage.current"
                v-model:page-size="examPage.size"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="filteredExamRecordList.length"
                class="pagination"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
            />
            <!-- 考试详情弹窗 -->
            <el-dialog
                v-model="detailDialogVisible"
                title="考试详情"
                width="80%"
                top="20px"
                destroy-on-close
            >
              <!-- 弹窗内容区域 -->
              <div class="exam-detail-container">
                <!-- 考试基础信息 -->
                <div class="exam-basic-info">
                  <el-descriptions :column="3" border>
                    <el-descriptions-item label="考试名称">{{ currentExam?.examName }}</el-descriptions-item>
                    <el-descriptions-item label="考试时间">
                      {{ currentExam?.examTime ? formatDate(currentExam.examTime) : '暂无' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="考试成绩">
                      <span :class="currentExam?.status === 1 ? '及格':'不及格'">
                        {{ currentExam?.score }}
                      </span>
                    </el-descriptions-item>
                  </el-descriptions>
                </div>

                <!-- 题目列表 -->
                <div class="questions-list" style="margin-top: 20px;">
                  <h4 style="margin-bottom: 10px; font-size: 16px; font-weight: 600;">题目详情</h4>
                  <!-- 遍历题目（适配后端返回格式） -->
                  <el-collapse v-if="currentExam?.questions && currentExam.questions.length">
                    <el-collapse-item
                        v-for="(question, qIndex) in currentExam.questions"
                        :key="qIndex"
                        :title="`第${qIndex+1}题:${question.questionDesc?.length > 90 ? question.questionDesc.slice(0, 90) + '...' : question.questionDesc || '无题目描述'}`"
                    >
                      <!-- 题目内容 -->
                      <div class="question-content" style="margin-bottom: 10px;">
                        <p><strong>题目描述：</strong>{{ question.questionDesc || '无' }}</p>
                        <p><strong>题目提示：</strong>{{ question.hint || '未知' }}</p>
                        <p><strong>本题得分：</strong>{{ question.sore }}{{'/'}}{{ question.fullScore || '未知' }}</p>
                        <p><strong>输入格式：</strong>{{ question.inputFormat || '未知' }}</p>
                        <p><strong>输出格式：</strong>{{ question.outputFormat || '未知' }}</p>

                      </div>

                      <!-- 测试用例 -->
                      <div class="test-cases" style="margin: 10px 0;">
                        <p style="font-weight: 600; margin-bottom: 5px;">测试用例：</p>
                        <el-table :data="question.testSampleResultsDtos || []" border size="small" style="width: 100%;">
                          <el-table-column prop="input" label="输入参数" />
                          <el-table-column prop="output" label="预期输出" />
                          <el-table-column prop="actualOutput" label="实际输出">
                            <template #default="{ row }">
                              <span>{{ row.actualOutput || '无实际输出' }}</span>
                            </template>
                          </el-table-column>
                          <el-table-column prop="usedTime" label="用时" />
                          <el-table-column prop="result" label="测试结果">
                            <template #default="{ row }">
                              <!-- isPassed: 1-通过 0-失败 -->
                              <el-tag :type="row.isPassed === '1' ? 'success' : 'danger'">
                                {{ row.isPassed === '1' ? '通过' : '失败' }}
                              </el-tag>
                            </template>
                          </el-table-column>
                          <el-table-column label="错误信息">
                            <template #default="{ row }">
                              <!-- 有错误信息时显示红色查看详情按钮 -->
                              <el-button
                                  v-if="row.errorMessage"
                                  type="text"
                                  size="small"
                                  @click="handleViewError(row.errorMessage)"
                                  style="color: #F56C6C; padding: 0;"
                              >
                                查看错误详情
                              </el-button>
                              <!-- 无错误信息时显示绿色文字 -->
                              <span v-else style="color: #67C23A;">无错误信息</span>
                            </template>
                          </el-table-column>
                        </el-table>
                      </div>

                      <!-- 用户提交代码 -->
                      <div class="submit-code" style="margin-top: 10px;">
                        <p style="font-weight: 600; margin-bottom: 5px;">提交代码：</p>
                        <el-input
                            type="textarea"
                            :rows="6"
                            :model-value="question.code || '暂无提交代码'"
                            placeholder="暂无提交代码"
                            readonly
                            style="background: #f5f7fa; font-family: monospace;"
                        />
                      </div>
                    </el-collapse-item>
                  </el-collapse>
                  <div v-else style="text-align: center; color: #999; padding: 20px;">
                    暂无题目数据
                  </div>
                </div>
              </div>

              <!-- 弹窗底部按钮 -->
              <template #footer>
                <div class="dialog-footer">
                  <!-- 关闭按钮 -->
                  <el-button @click="detailDialogVisible = false">关闭</el-button>
                </div>
              </template>
            </el-dialog>

            <!-- 错误详情弹窗（放在表格外，el-card内） -->
            <el-dialog
                v-model="errorDialogVisible"
                title="错误信息详情"
                width="60%"
                top="20px"
                destroy-on-close
            >
              <div class="error-detail-content">
                <el-input
                    type="textarea"
                    v-model="currentErrorMessage"
                    :rows="10"
                    readonly
                    style="background: #f5f7fa; font-family: monospace;"
                    placeholder="暂无错误信息"
                />
              </div>
              <template #footer>
                <el-button @click="errorDialogVisible = false">关闭</el-button>
              </template>
            </el-dialog>
          </el-card>
        </div>
        <!-- 5. 在线考试 -->
        <div v-if="currentMenu === '4'" class="page-content online-exam">
          <el-card shadow="never">
            <!-- 加载骨架屏 -->
            <el-skeleton :loading="onlineExamLoading" animated :rows="3">
              <template #template>
                <div v-for="i in 3" :key="i" style="margin-bottom: 16px;">
                  <el-skeleton-item variant="card" style="height: 180px;" />
                </div>
              </template>

              <template #default>
                <!-- 考试列表 -->
                <div v-if="onlineExamList.length > 0" class="exam-list">
                  <el-card
                      class="exam-card"
                      v-for="exam in onlineExamList"
                      :key="exam.id"
                      shadow="hover"
                  >
                    <div class="exam-card-header">
                      <div class="exam-card-title">{{ exam.examTitle }}</div>
                      <el-tag :type="getExamStatusTagType(exam.examStatus)">
                        {{ exam.examStatus === 0 ? '未开始' : (exam.examStatus === 1 ? '进行中' : '已结束') }}
                      </el-tag>
                    </div>
                    <div class="exam-card-body">
                      <div class="exam-info-item">
                        <el-icon><Clock /></el-icon>
                        <span>考试时长：{{ exam.examDuration }}分钟</span>
                      </div>
                      <div class="exam-info-item">
                        <el-icon><Calendar /></el-icon>
                        <span>考试时间：{{ exam.startTime ? formatDate(exam.startTime) : '暂无' }}</span>
                      </div>
                      <div class="exam-info-item">
                        <el-icon><User /></el-icon>
                        <span>参与人数：{{ exam.participantCount }}人</span>
                      </div>
                      <div class="exam-info-item">
                        <el-icon><Book /></el-icon>
                        <span>题目数量：{{ exam.questionCount }}题</span>
                      </div>
                    </div>
                    <div class="exam-card-footer">
                      <el-button
                          type="primary"
                          :disabled="exam.examStatus !== 1"
                          @click="handleStartExam(exam.id)"
                      >
                        <el-icon><Play /></el-icon>
                        {{ exam.examStatus === 1 ? '开始考试' : (exam.examStatus === 0 ? '未开始' : '已结束') }}
                      </el-button>
                    </div>
                  </el-card>
                </div>
                <el-empty v-else description="暂无考试" />
              </template>
            </el-skeleton>
          </el-card>
        </div>
        <!-- 6.题库合集 -->
        <div v-if="currentMenu === '5'" class="page-content question-bank">
          <el-card shadow="never">
            <!-- 顶部操作栏 -->
            <div class="bank-header-bar">
              <div class="bank-tabs">
                <el-radio-group v-model="activeBankTab" @change="handleTabChange">
                  <el-radio-button label="all">全部题库</el-radio-button>
                  <el-radio-button label="collected">我的收藏</el-radio-button>
                  <el-radio-button label="wrong">错题记录</el-radio-button>
                </el-radio-group>
              </div>
              <div class="bank-search">
                <!-- 错题本标签页显示搜索题目，其他标签页显示搜索题库 -->
                <el-input
                    v-if="activeBankTab !== 'wrong'"
                    v-model="bankKeyword"
                    placeholder="搜索题库名称"
                    :prefix-icon="Search"
                    clearable
                    style="width: 260px"
                    @clear="handleBankSearch"
                    @keyup.enter="handleBankSearch"
                />
                <el-input
                    v-else
                    v-model="wrongKeyword"
                    placeholder="搜索题目"
                    :prefix-icon="Search"
                    clearable
                    style="width: 260px"
                    @clear="handleWrongSearch"
                    @keyup.enter="handleWrongSearch"
                />
              </div>
            </div>

            <!-- 全部题库和收藏标签页 -->
            <template v-if="activeBankTab !== 'wrong'">
              <!-- 加载骨架屏 -->
              <el-skeleton :loading="bankLoading" animated :rows="3">
                <template #template>
                  <div class="bank-list-skeleton">
                    <div v-for="i in 6" :key="i" class="bank-skeleton-item">
                      <el-skeleton-item variant="rect" style="height: 160px; border-radius: 12px;" />
                    </div>
                  </div>
                </template>

                <template #default>
                  <!-- 题库列表 -->
                  <div v-if="paginatedBankList.length > 0" class="bank-list">
                    <el-card
                        class="bank-card"
                        v-for="bank in paginatedBankList"
                        :key="bank.id"
                        shadow="hover"
                    >
                      <div class="bank-card-content">
                        <!-- 卡片头部：标题和收藏按钮 -->
                        <div class="bank-card-header">
                          <div class="bank-title">
                            <span class="title-text">{{ bank.title }}</span>
                            <el-tag
                                :type="getLevelTagType(bank.level)"
                                size="small"
                                class="level-tag"
                            >
                              {{ bank.level }}
                            </el-tag>
                            <el-tag
                                v-if="bank.isVip === 1"
                                type="danger"
                                size="small"
                                class="vip-tag"
                            >
                              VIP
                            </el-tag>
                          </div>
                          <el-button
                              :type="bank.collected ? 'warning' : 'default'"
                              size="small"
                              class="collect-btn"
                              @click="handleCollectBank(bank)"
                          >
                            <el-icon><StarFilled v-if="bank.collected" /><Star v-else /></el-icon>
                            {{ bank.collected ? '已收藏' : '收藏' }}
                          </el-button>
                        </div>

                        <!-- 题库描述 -->
                        <div class="bank-desc">{{ bank.desc }}</div>

                        <!-- 题库元信息 -->
                        <div class="bank-meta">
                          <div class="meta-item">
                            <el-icon><Document /></el-icon>
                            <span>{{ bank.questionCount }}题</span>
                          </div>
                          <div class="meta-item" v-if="bank.recommend">
                            <el-icon><TrendCharts /></el-icon>
                            <span>推荐度 {{ bank.recommend }}%</span>
                          </div>
                          <div class="meta-item">
                            <el-icon><View /></el-icon>
                            <span>参与次数{{ bank.viewCount || 0 }}</span>
                          </div>
                        </div>

                        <!-- 底部按钮 -->
                        <div class="bank-footer">
                          <el-button
                              type="primary"
                              size="small"
                              @click="handlePractice(bank)"
                          >
                            <el-icon><Promotion /></el-icon>
                            开始练习
                          </el-button>
                        </div>
                      </div>
                    </el-card>
                  </div>

                  <!-- 空状态 -->
                  <el-empty v-else description="暂无题库数据" />
                </template>
              </el-skeleton>

              <!-- 分页 -->
              <div v-if="totalBankCount > 0" class="bank-pagination">
                <el-pagination
                    v-model:current-page="bankPage.current"
                    v-model:page-size="bankPage.size"
                    :page-sizes="[6, 12, 24, 48]"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="totalBankCount"
                    @size-change="handleBankSizeChange"
                    @current-change="handleBankCurrentChange"
                />
                <div v-if="hasMoreBankPage" class="bank-next-hint">
                  下一页还有 {{ remainingBankCount }} 个题库
                </div>
              </div>
            </template>

            <!-- 错题本标签页 - 对齐考试记录的单行布局 + 详情模态框 -->
            <template v-else>
              <!-- 加载骨架屏 -->
              <el-skeleton :loading="wrongLoading" animated :rows="3">
                <template #template>
                  <div v-for="i in 3" :key="i" class="wrong-skeleton-item-row" style="margin-bottom: 12px;">
                    <el-skeleton-item variant="text" style="width: 30%;" />
                    <el-skeleton-item variant="text" style="width: 20%; margin-left: 10px;" />
                    <el-skeleton-item variant="text" style="width: 15%; margin-left: 10px;" />
                    <el-skeleton-item variant="text" style="width: 10%; margin-left: 10px;" />
                    <el-skeleton-item variant="button" style="width: 80px; margin-left: 10px;" />
                  </div>
                </template>
                <template #default>
                  <!-- 错题单行列表（对齐考试记录风格） -->
                  <div v-if="filteredWrongList.length > 0" class="wrong-container">
                    <!-- 列表头部（参考考试记录的标题栏） -->
                    <div class="wrong-list-header">
                      <div class="header-item" style="width: 30%;">题目信息</div>
                      <div class="header-item" style="width: 20%;">错题来源</div>
                      <div class="header-item" style="width: 15%;">错误时间</div>
                      <div class="header-item" style="width: 10%;">题目难度</div>
                      <div class="header-item" style="width: 25%; text-align: right;">操作</div>
                    </div>
                    <!-- 错题列表项 -->
                    <div class="wrong-list-row">
                      <div
                          class="wrong-item-row"
                          v-for="wrong in paginatedWrongList"
                          :key="wrong.id"
                      >
                        <!-- 题目信息（主内容） -->
                        <div class="wrong-item-col" style="width: 30%;">
                          <el-tag type="danger" size="small" class="wrong-tag-row">错题</el-tag>
                          <span class="title-text-row" :title="wrong.questionDesc || '暂无标题'">
                {{ (wrong.questionDesc || '暂无标题').slice(0,10) }}{{ (wrong.questionDesc || '').length >10 ? '...' : '' }}
              </span>
                        </div>
                        <!-- 错题来源 -->
                        <div class="wrong-item-col" style="width: 20%;">
                          <span class="source-text-row">{{ wrong.bankTitle || '未知题库' }}</span>
                        </div>
                        <!-- 错误时间 -->
                        <div class="wrong-item-col" style="width: 15%;">
                          <span class="time-text-row">{{ formatDate(wrong.lastWrongTime) }}</span>
                        </div>
                        <!-- 题目难度（替换错误次数） -->
                        <div class="wrong-item-col" style="width: 10%;">
                          <el-tag :type="getLevelTagType(mapLevelToText(wrong.level))" size="small">
                            {{ mapLevelToText(wrong.level) }}
                          </el-tag>
                        </div>
                        <!-- 操作按钮 -->
                        <div class="wrong-item-col" style="width: 25%; text-align: right;">
                          <el-button
                              type="primary"
                              size="small"
                              @click="openWrongDetailModal(wrong)"
                              icon="View"
                          >
                            查看详情
                          </el-button>
                          <el-button
                              type="success"
                              size="small"
                              @click="handleRePractice(wrong)"
                              icon="Edit"
                          >
                            重新练习
                          </el-button>
                          <el-button
                              type="text"
                              size="small"
                              @click="handleRemoveWrongQuestion(wrong.questionId)"
                              icon="Delete"
                              text-color="#ff4d4f"
                          >
                            移除
                          </el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!-- 空状态 -->
                  <el-empty v-else description="暂无错题，继续加油！" />
                </template>
              </el-skeleton>

              <!-- 分页 -->
              <div v-if="totalWrongCount > 0" class="wrong-pagination-row">
                <el-pagination
                    v-model:current-page="wrongPage.current"
                    v-model:page-size="wrongPage.size"
                    :page-sizes="[5, 10, 20, 50]"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="totalWrongCount"
                    @size-change="handleWrongSizeChange"
                    @current-change="handleWrongCurrentChange"
                />
              </div>

              <!-- 错题详情模态框 -->
              <el-dialog
                  v-model="wrongDetailModalVisible"
                  title="错题详情"
                  width="80%"
                  top="20px"
                  destroy-on-close
                  draggable
              >
                <div v-if="currentWrongQuestion" class="wrong-detail-content">
                  <!-- 错题基础信息 -->
                  <div class="detail-section">
                    <h4 class="section-title">基础信息</h4>
                    <div class="info-row">
                      <span class="info-label">题目标题：</span>
                      <span class="info-value">{{ currentWrongQuestion.questionDesc || '暂无' }}</span>
                    </div>
                    <div class="info-row">
                      <span class="info-label">错题来源：</span>
                      <span class="info-value">{{ currentWrongQuestion.bankTitle || '未知题库' }}</span>
                    </div>
                    <div class="info-row">
                      <span class="info-label">最后错误时间：</span>
                      <span class="info-value">{{ formatDate(currentWrongQuestion.lastWrongTime) }}</span>
                    </div>
                    <div class="info-row">
                      <span class="info-label">题目难度：</span>
                      <span class="info-value">
            <el-tag :type="getLevelTagType(mapLevelToText(currentWrongQuestion.level))">
              {{ mapLevelToText(currentWrongQuestion.level) }}
            </el-tag>
          </span>
                    </div>
                  </div>

                  <!-- 题目格式信息（补充后端字段） -->
                  <div class="detail-section">
                    <h4 class="section-title">题目格式</h4>
                    <div class="info-row">
                      <span class="info-label">输入格式：</span>
                      <span class="info-value">{{ currentWrongQuestion.inputFormat || '暂无' }}</span>
                    </div>
                    <div class="info-row">
                      <span class="info-label">输出格式：</span>
                      <span class="info-value">{{ currentWrongQuestion.outputFormat || '暂无' }}</span>
                    </div>
                    <div class="info-row">
                      <span class="info-label">题目提示：</span>
                      <span class="info-value">{{ currentWrongQuestion.hint || '暂无提示' }}</span>
                    </div>
                  </div>

                  <!-- 新增：用户提交的代码展示 -->
                  <div class="detail-section" v-if="currentWrongQuestion.code">
                    <h4 class="section-title">提交代码</h4>
                    <div class="code-content">
                      <pre>{{ currentWrongQuestion.code }}</pre>
                    </div>
                  </div>
                  <!-- 新增：题目样例（适配sampleDesc文本格式） -->
                  <div class="detail-section" v-if="currentWrongQuestion.sampleQuestionList && currentWrongQuestion.sampleQuestionList.length > 0">
                    <h4 class="section-title">题目样例</h4>
                    <div class="sample-list">
                      <div v-for="(sample, index) in currentWrongQuestion.sampleQuestionList" :key="index" class="sample-item">
                        <!-- 直接渲染含换行符的sampleDesc，保留格式 -->
                        <div class="sample-content-text">
                          <pre>输入{{index+1}}:{{sample.input}}</pre>
                          <pre>输出{{index+1}}:{{sample.output}}</pre>
                        </div>
                      </div>
                    </div>
                  </div>
                  <!-- 新增：测试用例执行情况 -->
                  <div class="test-case-table">
                    <table>
                      <thead>
                      <tr>
                        <th width="80px">用例编号</th>
                        <th>输入</th>
                        <th>期望输出</th>
                        <th>实际输出</th>
                        <th width="80px">执行结果</th>
                        <th width="150px">错误信息</th>
                      </tr>
                      </thead>
                      <tbody>
                      <tr v-for="(test, index) in currentWrongQuestion.testSampleResultList" :key="index">
                        <td>{{ test.name }}</td>
                        <td class="test-input">{{ test.input || '-' }}</td>
                        <td class="test-expected">{{ test.output || '-' }}</td>
                        <td class="test-actual">{{ test.actualOutput || '-' }}</td>
                        <td>
                          <el-tag :type="test.isPassed === '1' ? 'success' : 'danger'" size="small">
                            {{ test.isPassed === '1' ? '通过' : '失败' }}
                          </el-tag>
                        </td>
                        <td>
                          <!-- 有错误信息时显示红色查看详情按钮 -->
                          <el-button
                              v-if="test.errorMessage"
                              type="text"
                              size="small"
                              @click="handleViewError2(test.errorMessage)"
                              style="color: #F56C6C; padding: 0;"
                          >
                            查看错误详情
                          </el-button>
                          <!-- 无错误信息时显示绿色文字 -->
                          <span v-else style="color: #67C23A;">无错误信息</span>
                        </td>
                      </tr>
                      </tbody>
                    </table>
                  </div>                  <!-- 错误原因/解析 -->
                  <div class="detail-section" v-if="currentWrongQuestion.errorMessage">
                    <h4 class="section-title">错误解析</h4>
                    <div class="analysis-content">
                      <p class="error-msg">{{ currentWrongQuestion.errorMessage }}</p>
                    </div>
                  </div>
                </div>
                <div v-else class="empty-detail">
                  <el-empty description="暂无错题详情数据" />
                </div>

                <!-- 模态框底部按钮 -->
                <template #footer>
                  <el-button @click="wrongDetailModalVisible = false">关闭</el-button>
                  <el-button type="primary" @click="handleRePractice(currentWrongQuestion)">重新练习</el-button>
                </template>
              </el-dialog>
            </template>
          </el-card>
        </div>

        <!-- 7. VIP升级 -->
        <div v-if="currentMenu === '6'" class="page-content vip-upgrade-page">
          <section class="vip-hero-panel">
            <div>
              <div class="eyebrow">VIP Access</div>
              <h2>开通 VIP，解锁更多考试与题库</h2>
              <p>
                VIP 适合需要集中冲刺机考、反复练习真题和参加会员专属模拟考试的同学。选择套餐后联系管理员或教师，确认后会手动为账号开通。
              </p>
            </div>
            <div class="vip-status-card">
              <span class="status-label">当前账号</span>
              <strong>{{ isVipActive ? 'VIP有效' : '普通学生' }}</strong>
              <small>{{ userInfo.vipExpireTime ? `到期：${formatDate(userInfo.vipExpireTime)}` : '暂未开通VIP' }}</small>
            </div>
          </section>

          <!-- 密钥兑换 -->
          <section class="vip-redeem-section">
            <div class="redeem-card">
              <div class="redeem-info">
                <h3>密钥兑换</h3>
                <p>如果你有VIP激活密钥，输入后即可直接开通VIP。</p>
              </div>
              <div class="redeem-action">
                <el-input
                  v-model="redeemKeyCode"
                  placeholder="请输入VIP密钥，如 XXXX-XXXX-XXXX-XXXX"
                  clearable
                  style="width:320px"
                />
                <el-button type="primary" :loading="redeemLoading" @click="handleRedeemKey">兑换</el-button>
              </div>
            </div>
          </section>

          <el-skeleton :loading="vipLoading" animated :rows="4">
            <template #default>
              <div v-if="vipPlanList.length" class="vip-plan-grid">
                <article
                    v-for="plan in vipPlanList"
                    :key="plan.id"
                    class="vip-plan-card"
                >
                  <div class="plan-topline">
                    <span class="plan-name">{{ plan.planName }}</span>
                    <el-tag type="warning" effect="plain">{{ plan.durationDays }}天</el-tag>
                  </div>
                  <div class="plan-price">
                    <span>¥</span>{{ formatPrice(plan.price) }}
                  </div>
                  <p class="plan-benefits">{{ plan.benefits || '解锁VIP考试、VIP题库与后续会员内容。' }}</p>

                  <div class="contact-panel">
                    <div class="contact-row">
                      <span>管理员QQ</span>
                      <strong>{{ plan.contactQq || '后台待设置' }}</strong>
                      <el-button size="small" text @click="copyContact(plan.contactQq)">复制</el-button>
                    </div>
                    <div class="contact-row">
                      <span>微信</span>
                      <strong>{{ plan.contactWechat || '后台待设置' }}</strong>
                      <el-button size="small" text @click="copyContact(plan.contactWechat)">复制</el-button>
                    </div>
                  </div>

                  <div class="plan-note">
                    <el-icon><ChatDotRound /></el-icon>
                    <span>{{ plan.contactNote || '联系时请发送账号、套餐名称和付款截图。' }}</span>
                  </div>
                </article>
              </div>
              <el-empty v-else description="管理员还没有配置VIP套餐" />
            </template>
          </el-skeleton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed,onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage ,ElLoading,ElMessageBox } from 'element-plus'
import {
  User, UserFilled, Document, DataBoard, EditPen, Timer, Collection,
  SwitchButton, Refresh, Setting, Camera, Edit, Clock, Search,
  Plus, Star, StarFilled, Calendar, Upload, Medal, Warning, Promotion,
  TrendCharts, View, ChatDotRound
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { updateUserInfo,getStatsData,uploadAvatar } from '@/api/auth.js'
import {useUserStore,STORAGE_KEYS} from "@/stores/user.js";
import {batchUpdateExamStatus, getExamList, getExamRecordList} from "@/api/exam.js";
import {
  getBankList,
  getCollectBankIds,
  getWrongBankCount,
  getWrongQuestionList,
  toggleCollectBank,
  removeWrongQuestion, addViewCount
} from "@/api/question-bank.js";
import { getVipPlans } from "@/api/vip.js";
import { redeemVipKey } from "@/api/vip-key.js";
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.css'
import BrandMark from '@/components/student/BrandMark.vue'
import SoftCard from '@/components/student/SoftCard.vue'
import MetricCard from '@/components/student/MetricCard.vue'
import ProgressRing from '@/components/student/ProgressRing.vue'
import QuickActionCard from '@/components/student/QuickActionCard.vue'

const router = useRouter()
const title = ref('')
const version = ref('')
const REAL_NAME_HIDE_KEY = 'student_real_name_hidden'
// 当前选中的菜单和标题
const currentMenu = ref('1')
const currentTitle = ref('个人中心')

// 用户信息
const userStore = useUserStore()

// 搜索关键词
const searchKeyword = ref('')
const noteKeyword = ref('')
// 添加这个计算属性
const filteredExamRecordList = computed(() => {
  if (!examRecordList.value || examRecordList.value.length === 0) {
    return []
  }

  let list = [...examRecordList.value]

  // 根据考试名称模糊搜索
  if (searchKeyword.value && searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    list = list.filter(record =>
        record.examName?.toLowerCase().includes(keyword)
    )
  }

  return list
})

// 编辑头像=======================================================
// 是否显示头像选择器
const showAvatarSelector = ref(false)

// 临时选中的头像（在选择器中暂存）
const tempSelectedAvatar = ref('')

// 打开头像选择器
const openAvatarSelector = () => {
  tempSelectedAvatar.value = selectedAvatar.value
  showAvatarSelector.value = true
}

// 在选择器中点击头像（暂存，不立即确认）
const selectAvatarTemp = (avatarUrl) => {
  tempSelectedAvatar.value = avatarUrl
}

// 确认选择头像
const confirmSelectAvatar = (avatarUrl) => {
  selectedAvatar.value = avatarUrl
  showAvatarSelector.value = false
}
// 当前选中的头像
const selectedAvatar = ref('')

const fixAvatarUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('/uploads/')) return '/api/auth/avatar/' + url.split('/').pop()
  return url
}

// 选择头像
const selectAvatar = (avatarUrl) => {
  selectedAvatar.value = avatarUrl
  console.log('选择头像:', avatarUrl)
}

// 预设头像列表
const avatarList = ref([
    { id: 1, name: '默认头像1', url: '/avatars/1.jpg' },
    { id: 2, name: '默认头像2', url: '/avatars/2.jpg' },
    { id: 3, name: '默认头像3', url: '/avatars/3.jpg' },
    { id: 4, name: '默认头像4', url: '/avatars/4.jpg' },
    { id: 5, name: '默认头像5', url: '/avatars/5.jpg' },
    { id: 6, name: '默认头像6', url: '/avatars/6.jpg' },
    { id: 7, name: '默认头像7', url: '/avatars/7.jpg' },
    { id: 8, name: '默认头像8', url: '/avatars/8.jpg' },
    { id: 9, name: '默认头像9', url: '/avatars/9.jpg' },
    { id: 10, name: '默认头像10', url: '/avatars/10.jpg' },
    { id: 11, name: '默认头像11', url: '/avatars/11.jpg' },
    { id: 12, name: '默认头像12', url: '/avatars/12.jpg' },
    { id: 13, name: '默认头像13', url: '/avatars/13.jpg' },
    { id: 14, name: '默认头像14', url: '/avatars/14.jpg' },
    { id: 15, name: '默认头像15', url: '/avatars/15.jpg' },
    { id: 16, name: '默认头像16', url: '/avatars/16.jpg' },
    { id: 17, name: '默认头像17', url: '/avatars/17.jpg' },
    { id: 18, name: '默认头像18', url: '/avatars/18.jpg' }
])

const avatarUrl = ref('')  // 预览头像URL

// Avatar upload & crop
const cropDialogVisible = ref(false)
const cropImageRef = ref(null)
let cropperInstance = null
const cropUploading = ref(false)

// Trigger file input click
const triggerAvatarUpload = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/jpeg,image/png,image/webp'
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (!file) return

    // Validate
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
      ElMessage.error('仅支持 JPG、PNG、WebP 格式')
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error('文件大小不能超过 5MB')
      return
    }

    // Show crop dialog
    cropDialogVisible.value = true
    nextTick(() => {
      if (cropImageRef.value) {
        cropImageRef.value.src = URL.createObjectURL(file)
        cropperInstance = new Cropper(cropImageRef.value, {
          aspectRatio: 1,
          viewMode: 1,
          dragMode: 'move',
          autoCropArea: 0.9,
          responsive: true,
          background: false,
          guides: false,
          cropBoxMovable: true,
          cropBoxResizable: true,
        })
      }
    })
  }
  input.click()
}

// Confirm crop and upload
const confirmCropUpload = async () => {
  if (!cropperInstance) return
  cropUploading.value = true
  try {
    const canvas = cropperInstance.getCroppedCanvas({
      width: 200,
      height: 200,
      imageSmoothingQuality: 'high'
    })
    const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/jpeg', 0.85))
    const file = new File([blob], 'avatar.jpg', { type: 'image/jpeg' })
    const res = await uploadAvatar(file)
    if (res && res.code === 200) {
      selectedAvatar.value = res.data
      ElMessage.success('头像上传成功')
      closeCropDialog()
    } else {
      ElMessage.error(res?.message || '上传失败')
    }
  } catch (err) {
    console.error('上传失败:', err)
    ElMessage.error('头像上传失败')
  } finally {
    cropUploading.value = false
  }
}

// Close crop dialog and destroy cropper
const closeCropDialog = () => {
  if (cropperInstance) {
    cropperInstance.destroy()
    cropperInstance = null
  }
  cropDialogVisible.value = false
}


// 编辑头像=======================================================

// 编辑资料相关信息=======================================================

// 编辑资料模态框相关
const editInfoDialogVisible = ref(false)
const editFormRef = ref(null)
const mustCompleteRealName = ref(false)
const hideRealNameOnStudentPage = ref(localStorage.getItem(REAL_NAME_HIDE_KEY) === '1')

// 编辑表单数据（回显用户当前信息）
const editForm = reactive({
  real_name: '',
  phone: '',
  school: '',
  major: '',
  score: '',
  email: ''
})

// 表单验证规则
const editFormRules = reactive({
  real_name: [
    {
      validator: (rule, value, callback) => {
        if (!String(value || '').trim()) {
          callback(new Error('请输入真实姓名'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  school: [
    { required: true, message: '请选择报考院校', trigger: 'change' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
})

// 编辑资料按钮点击事件
const fillEditForm = () => {
  // 回显用户当前信息到表单
  editForm.real_name = userInfo.value.realName || ''
  editForm.phone = userInfo.value.phone || ''
  editForm.school = userInfo.value.school || ''
  editForm.major = userInfo.value.major || ''
  editForm.score = userInfo.value.score || ''
  editForm.email = userInfo.value.email || ''

  // 设置当前选中的头像（如果有就显示，没有就为空）
  selectedAvatar.value = fixAvatarUrl(userInfo.value.avatar) || ''
}

const handleEditInfo = () => {
  fillEditForm()

  // 打开模态框
  editInfoDialogVisible.value = true
}

const requireRealNameProfile = () => {
  if (hasRealName.value) return
  mustCompleteRealName.value = true
  fillEditForm()
  editInfoDialogVisible.value = true
  ElMessage.warning('请先填写真实姓名后再使用网站')
}

const handleRealNamePrivacyChange = (hidden) => {
  localStorage.setItem(REAL_NAME_HIDE_KEY, hidden ? '1' : '0')
  ElMessage.success(hidden ? '学生页已隐藏真实姓名' : '学生页已显示真实姓名')
}

// 重置编辑表单
const resetEditForm = () => {
  if (editFormRef.value) {
    editFormRef.value.resetFields()
  }
}

// 提交编辑资料
const submitEditInfo = async () => {
  const newUserStore = useUserStore()

  // 1. 从 localStorage 获取 JSON 字符串
  const dataStr = localStorage.getItem('user_info')

// 2. 解析为对象（增加空值校验，避免 null/undefined 解析报错）
  let newUserInfo = {} // 默认空对象
  if (dataStr) {
    try {
      newUserInfo = JSON.parse(dataStr) // 核心解析方法
    } catch (error) {
      console.error('用户信息解析失败：', error)
      ElMessage.error('用户信息异常，请重新登录') // 可选：给用户提示
    }
  }

  if (!editFormRef.value) return

  try {
    // 表单验证
    await editFormRef.value.validate()
    // 合并头像数据
    const submitData = {
      ...editForm,
      avatar: selectedAvatar.value,  // 使用选中的头像
      userId: newUserInfo.id || userInfo.value.id || userStore.getUserId
    }
    submitData.real_name = String(submitData.real_name || '').trim()

    // 调用更新接口
    const res = await updateUserInfo(submitData)
    console.log("sssss",res)

    if (res.code === 200) {
      ElMessage.success(res.message || '资料修改成功！')

      // 更新本地用户信息
      newUserStore.updateUserProfile(
          submitData.real_name,
          submitData.phone,
          submitData.school,
          submitData.major,
          submitData.score,
          submitData.email,
          submitData.avatar
      )

      // 更新显示信息
      userInfo.value.realName = submitData.real_name
      userInfo.value.avatar = submitData.avatar
      userInfo.value.email = submitData.email
      userInfo.value.major = submitData.major
      userInfo.value.phone = submitData.phone
      userInfo.value.school = submitData.school
      userInfo.value.score = submitData.score

      // 关闭模态框
      mustCompleteRealName.value = false
      editInfoDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '资料修改失败！')
    }
  } catch (error) {
    ElMessage.error('表单验证失败，请检查输入！')
    console.error('提交失败：', error)
  }
}

// 编辑资料相关信息=======================================================

// 考试记录相关信息=======================================================
const detailDialogVisible = ref(false) // 详情弹窗显示状态
const currentExam = ref({})// 当前选中的考试详情
const errorDialogVisible = ref(false);
const currentErrorMessage = ref('');
// 查看错误详情--这是错题本的错误详情
const handleViewError2 = (errorMessage) => {
  ElMessageBox.alert(errorMessage, '错误详情', {
    confirmButtonText: '确定',
    type: 'error',
    customClass: 'error-message-dialog',
    dangerouslyUseHTMLString: true
  })
}
// 查看错误详情--这是考试记录的错误详情
const handleViewError = (errorMsg) => {
  currentErrorMessage.value = errorMsg;
  errorDialogVisible.value = true;
};
// "vite-plugin-vue-devtools": "^8.0.6"自动构建
const handleToExamRecord = () => {
  currentMenu.value = '2'
  currentTitle.value = '考试记录'
  loadExamRecordList()
}
// 考试记录列表（示例数据）
const examRecordList = ref([])

const loadExamRecordList = async (forceRefresh = false) => {
  // 如果不是强制刷新，且有数据，就不请求后端
  if (!forceRefresh && examRecordList.value && examRecordList.value.length > 0) {
    return
  }

  const userId = localStorage.getItem('userId');
  if(userId === null) {
    ElMessage('加载考试记录失败！登陆过期')
    router.push('/login')
  }

  try {
    const res = await getExamRecordList({
      userId:userId
    })
    if(res.code === 200){
      examRecordList.value = res.data || []
    }else{
      ElMessage.error(res.message || '考试列表加载失败')
    }
  } catch (err) {
    ElMessage.error('考试列表加载失败')
  }
}
// 查看详情（打开弹窗）
const handleViewDetail = (row) => {
  console.log("row",row)
  currentExam.value = row; // 赋值当前考试数据
  detailDialogVisible.value = true; // 打开弹窗
}
// 记录笔记
const handleRecordNote = () => {
  // 可根据需求实现笔记记录逻辑（如打开笔记编辑弹窗/跳转到笔记页面）
  this.$message.info('笔记记录功能已触发，可在此补充具体逻辑');
  // 示例：跳转到我的笔记页面并携带考试信息
  // this.currentMenu = '3';
  // 或打开笔记编辑弹窗
}
// 考试记录相关信息=======================================================


// 左侧在线考试相关信息=======================================================
// 缓存时间戳
let lastFetchTime = null;
const CACHE_DURATION = 5 * 60 * 1000; // 5分钟缓存,之后请求后端
// 设置定时器定期刷新状态（每1分钟）
let statusTimer = null;
const onlineExamLoading = ref(false)
// 跳转到在线考试页面（切换菜单 + 可直接扩展接口调用）
const handleToOnlineExam = () => {
  currentMenu.value = '4'
  currentTitle.value = '在线考试'
  loadOnlineExamList()
}
// 加载在线考试列表（带缓存）
const loadOnlineExamList = async (forceRefresh = false) => {
  const now = Date.now();

  // 判断是否需要刷新：强制刷新 OR 无缓存数据 OR 缓存过期
  if (!forceRefresh && onlineExamList.value && onlineExamList.value.length > 0 && lastFetchTime && (now - lastFetchTime) < CACHE_DURATION) {
    // 使用缓存数据，但需要更新状态
    updateExamStatus();
    return;
  }

  const roleJsonStr = localStorage.getItem(STORAGE_KEYS.ROLE);
  let roleObj = null;

  if (roleJsonStr) {
    try {
      roleObj = JSON.parse(roleJsonStr);
    } catch (e) {
      console.error('角色JSON解析失败：', e);
      roleObj = null;
    }
  } else {
    console.warn('本地存储中无角色信息');
    return;
  }

  try {
    onlineExamLoading.value = true;
    const res = await getExamList({
      roleId: roleObj.id
    });

    if (res.code === 200) {
      onlineExamList.value = res.data || [];
      lastFetchTime = now; // 更新缓存时间
      updateExamStatus(); // 更新状态
    } else {
      ElMessage.error(res.message || '考试列表加载失败');
    }
  } catch (err) {
    console.error('加载考试列表失败:', err);
    ElMessage.error('考试列表加载失败');
  } finally {
    onlineExamLoading.value = false;
  }
}
// 更新考试状态（前端计算）
const updateExamStatus = () => {
  if (!onlineExamList.value || onlineExamList.value.length === 0) return;

  const now = new Date();
  const updatedExams = []; // 存储需要更新的考试
  let hasChanges = false; // 标记是否有状态变化

  onlineExamList.value.forEach(exam => {
    const startTime = new Date(exam.startTime);
    const endTime = new Date(exam.endTime);
    let newStatus;

    if (now < startTime) {
      newStatus = 0; // 未开始
    } else if (now >= startTime && now <= endTime) {
      newStatus = 1; // 进行中
    } else {
      newStatus = 2; // 已结束
    }

    // 检测状态是否发生变化
    if (exam.examStatus !== newStatus) {
      hasChanges = true;
      updatedExams.push({
        id: exam.id,
        oldStatus: exam.examStatus,
        newStatus: newStatus
      });
      exam.examStatus = newStatus; // 更新前端状态
    }
  });
  // 如果有状态变化，持久化到数据库
  if (hasChanges && updatedExams.length > 0) {
    console.log('检测到考试状态变化:', updatedExams);
    syncExamStatusToBackend(updatedExams);
  }
}
// 同步考试状态到后端
const syncExamStatusToBackend = async (updatedExams) => {
  try {
    // 批量更新考试状态
    const res = await batchUpdateExamStatus(updatedExams);

    if (res.code === 200) {
      console.log('考试状态同步成功:', updatedExams);
    } else {
      console.error('考试状态同步失败:', res.message);
    }
  } catch (error) {
    console.error('同步考试状态失败:', error);
  }
}
// 在线考试列表（示例数据）
const onlineExamList = ref([])
onMounted(() => {
  // 每分钟更新一次考试状态（不请求后端）
  statusTimer = setInterval(() => {
    if (onlineExamList.value && onlineExamList.value.length > 0) {
      updateExamStatus();
    }
  }, 60 * 1000);
});
// 左侧在线考试相关信息=======================================================

// 左侧题库合集相关信息=======================================================

// 缓存相关
let bankLastFetchTime = null
const BANK_CACHE_DURATION = 5 * 60 * 1000 // 5分钟缓存

// 状态变量
const activeBankTab = ref('all')        // 当前选中的标签页
const bankKeyword = ref('')              // 搜索关键词 - 这个报错说没定义
const bankLoading = ref(false)           // 加载状态
const allBankList = ref([])              // 所有题库数据
const collectedBankIds = ref([])         // 收藏的题库ID列表
const wrongBankMap = ref({})             // 错题数量映射
// 过滤后的数据（根据tab和搜索关键词）
const filteredBankList = computed(() => {
  if (!allBankList.value || !Array.isArray(allBankList.value)) {
    return []
  }

  let list = [...allBankList.value]

  // 根据标签页过滤
  if (activeBankTab.value === 'collected') {
    list = list.filter(bank => collectedBankIds.value.includes(bank.id))
  } else if (activeBankTab.value === 'wrong') {
    list = list.filter(bank => wrongBankMap.value[bank.id] > 0)
  }

  // 根据搜索关键词过滤
  if (bankKeyword.value && bankKeyword.value.trim()) {
    const keyword = bankKeyword.value.trim().toLowerCase()
    list = list.filter(bank =>
        bank.title?.toLowerCase().includes(keyword) ||
        bank.desc?.toLowerCase().includes(keyword)
    )
  }

  return list
})
// 分页参数
const bankPage = reactive({
  current: 1,
  size: 6
})
const handleToQuestionList = () => {
  currentMenu.value = '5'
  currentTitle.value = '题库合集'
  loadAllBanks()
}

const vipLoading = ref(false)
const vipPlanList = ref([])

// VIP密钥兑换
const redeemKeyCode = ref('')
const redeemLoading = ref(false)

const handleRedeemKey = async () => {
  if (!redeemKeyCode.value?.trim()) {
    ElMessage.warning('请输入VIP密钥')
    return
  }
  const currentUserId = userStore.getUserId || localStorage.getItem('userId')
  if (!currentUserId) {
    ElMessage.error('登录信息异常，请重新登录后再兑换')
    router.push('/login')
    return
  }
  redeemLoading.value = true
  try {
    const res = await redeemVipKey({
      keyCode: redeemKeyCode.value.trim(),
      userId: currentUserId
    })
    if (res.code === 200) {
      ElMessage.success(res.message || 'VIP开通成功')
      redeemKeyCode.value = ''
      if (res.data) {
        userStore.setUser(res.data)
        localStorage.setItem('userId', res.data.id || currentUserId)
        loadUserData()
      }
    } else {
      ElMessage.error(res.message || '兑换失败')
    }
  } catch (error) {
    console.error('兑换VIP密钥失败:', error)
  } finally {
    redeemLoading.value = false
  }
}

const handleToVipUpgrade = () => {
  currentMenu.value = '6'
  currentTitle.value = '升级VIP'
  loadVipPlans()
}

const handleToChat = () => {
  router.push('/chat')
}

const loadVipPlans = async (forceRefresh = false) => {
  if (!forceRefresh && vipPlanList.value.length > 0) {
    return
  }
  try {
    vipLoading.value = true
    const res = await getVipPlans()
    if (res.code === 200) {
      vipPlanList.value = res.data || []
    } else {
      ElMessage.error(res.message || 'VIP套餐加载失败')
    }
  } catch (error) {
    console.error('VIP套餐加载失败:', error)
    ElMessage.error('VIP套餐加载失败，请稍后重试')
  } finally {
    vipLoading.value = false
  }
}

const formatPrice = (price) => {
  const number = Number(price || 0)
  return new Intl.NumberFormat('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(number)
}

const copyContact = async (value) => {
  if (!value || value === '后台待设置') {
    ElMessage.warning('管理员还没有设置这个联系方式')
    return
  }
  try {
    await navigator.clipboard.writeText(value)
    ElMessage.success('联系方式已复制')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.info(`请手动复制：${value}`)
  }
}
// 获取难度对应的标签类型
const getLevelTagType = (level) => {
  const levelMap = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger',
    '入门': 'info'
  }
  return levelMap[level] || 'info'
}
// 当前页显示的数据（前端分页）
const paginatedBankList = computed(() => {
  const start = (bankPage.current - 1) * bankPage.size
  const end = start + bankPage.size
  return filteredBankList.value.slice(start, end)
})
// 总数量（用于分页）
const totalBankCount = computed(() => {
  return filteredBankList.value.length
})
const remainingBankCount = computed(() => {
  return Math.max(totalBankCount.value - bankPage.current * bankPage.size, 0)
})
const hasMoreBankPage = computed(() => remainingBankCount.value > 0)
// 加载所有题库（一次性请求）
const loadAllBanks = async () => {
  try {
    bankLoading.value = true

    const params = {
      page: 1,
      size: 99999,  // 设置一个足够大的数字，一次性获取所有
      keyword: '',
      userId: localStorage.getItem('userId')
    }

    const res = await getBankList(params)

    if (res.code === 200) {
      const responseData = res.data.data || res.data
      const content = responseData.content || responseData.list || []

      allBankList.value = Array.isArray(content) ? content : []
      console.log('加载所有题库:', allBankList.value.length, '条')
    } else {
      ElMessage.error(res.message || '题库列表加载失败')
    }
  } catch (err) {
    console.error('加载题库列表失败:', err)
    ElMessage.error('题库列表加载失败')
  } finally {
    bankLoading.value = false
  }
}
// 加载收藏状态
const loadCollectStatus = async () => {
  try {
    const res = await getCollectBankIds({ userId: localStorage.getItem('userId') })
    console.log('收藏接口返回:', res)

    if (res.code === 200) {
      collectedBankIds.value = res.data || []
      console.log('收藏的题库ID列表:', collectedBankIds.value)
      console.log('收藏数量:', collectedBankIds.value.length)

      // 检查 allBankList 中哪些被标记为收藏
      allBankList.value.forEach(bank => {
        const isCollected = collectedBankIds.value.includes(bank.id)
        console.log(`题库: ${bank.title}, ID: ${bank.id}, 是否收藏: ${isCollected}`)
      })
    }
  } catch (err) {
    console.error('加载收藏状态失败:', err)
  }
}
// 加载错题数量
const loadWrongStatus = async () => {
  try {
    const res = await getWrongBankCount({
      userId:localStorage.getItem('userId')
    })
    if (res.code === 200) {
      wrongBankMap.value = res.data || {}
      console.log('错题数量:', wrongBankMap.value)
    }
  } catch (err) {
    console.error('加载错题状态失败:', err)
  }
}
// 标签页切换 - 重置页码，不请求后端
const handleTabChange = (tab) =>  {
  activeBankTab.value = tab
  bankPage.current = 1  // 重置到第一页
  // 如果切换到错题本标签页，加载错题数据
  if (tab === 'wrong') {
    loadWrongQuestions()
  }
}
// 搜索 - 重置页码，不请求后端
const handleBankSearch = () => {
  bankPage.current = 1
  // 不需要请求后端，computed会自动过滤
}
// 分页大小改变
const handleBankSizeChange = (size) => {
  bankPage.size = size
  bankPage.current = 1
}
// 页码改变
const handleBankCurrentChange = (page) => {
  bankPage.current = page
}
// 收藏/取消收藏
const handleCollectBank = async (bank) => {
  try {
    const res = await toggleCollectBank({
      bankId: bank.id,
      userId: localStorage.getItem('userId'),
      collected: !collectedBankIds.value.includes(bank.id)
    })

    if (res.code === 200) {
      // 更新本地收藏状态
      if (collectedBankIds.value.includes(bank.id)) {
        collectedBankIds.value = collectedBankIds.value.filter(id => id !== bank.id)
      } else {
        collectedBankIds.value.push(bank.id)
      }

      // 同时更新 allBankList 中的 collected 字段（用于显示）
      const targetBank = allBankList.value.find(b => b.id === bank.id)
      if (targetBank) {
        targetBank.collected = !targetBank.collected
      }

      ElMessage.success(collectedBankIds.value.includes(bank.id) ? '收藏成功' : '取消收藏成功')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (err) {
    console.error('收藏操作失败:', err)
    ElMessage.error('操作失败，请稍后重试')
  }
}
// 开始练习
const handlePractice = async (bank) => {
  if (bank.isVip === 1 && !isVipActive.value) {
    try {
      await ElMessageBox.confirm(
          '该题库为VIP专属题库，开通VIP后即可练习。请联系管理员购买VIP密钥，或前往升级VIP页面查看联系方式。',
          '需要开通VIP',
          {
            confirmButtonText: '去升级VIP',
            cancelButtonText: '稍后再说',
            type: 'warning'
          }
      )
      handleToVipUpgrade()
    } catch {
      // 用户取消，无需处理
    }
    return
  }

  // 更新题库参与次数
  updateBankViewCount(bank.id);

  router.push({
    name: 'questionList',
    query: {
      id: bank.id,
      title: bank.title
    }
  })
}
const updateBankViewCount = async (id) => {
  try {
    const res = await addViewCount({
      bankId:id
    })
    if(res.code === 200){
      //更新成功
    }else {
      ElMessage(res.message || '更新题库失败！')
    }
  }catch (e){
    ElMessage( '更新题库异常！')
  }
}

// 初始化
onMounted(async () => {
  // 并行请求所有数据
  await Promise.all([
    loadAllBanks(),
    loadCollectStatus(),
    loadWrongStatus()
  ])
})


// 错题列表变量
const wrongQuestionList = ref([])      // 错题列表
const wrongLoading = ref(false)        // 加载状态
const wrongKeyword = ref('')           // 错题搜索关键词
const wrongPage = reactive({           // 分页参数
  current: 1,
  size: 10
})
const totalWrongCount = ref(0)         // 总错题数
// 新增模态框相关响应式变量
const wrongDetailModalVisible = ref(false);
const currentWrongQuestion = ref({});
// 新增：将后端 level 数字(0/1/2/3) 映射为文本
const mapLevelToText = (level) => {
  const levelMap = {
    '0': '入门',
    '1': '简单',
    '2': '中等',
    '3': '困难'
  }
  return levelMap[level] || '入门';
};
// 打开错题详情模态框
const openWrongDetailModal = (wrong) => {
  // 赋值当前错题数据
  currentWrongQuestion.value = wrong;
  wrongDetailModalVisible.value = true;
};
// 过滤后的错题列表（根据搜索关键词）
const filteredWrongList = computed(() => {
  if (!wrongQuestionList.value || !Array.isArray(wrongQuestionList.value)) {
    return []
  }

  let list = [...wrongQuestionList.value]

  // 根据搜索关键词过滤
  if (wrongKeyword.value && wrongKeyword.value.trim()) {
    const keyword = wrongKeyword.value.trim().toLowerCase()
    list = list.filter(wrong =>
        wrong.questionDesc?.toLowerCase().includes(keyword) ||
        wrong.bankTitle?.toLowerCase().includes(keyword)
    )
  }

  return list
})
// 当前页显示的错题（前端分页）
const paginatedWrongList = computed(() => {
  const start = (wrongPage.current - 1) * wrongPage.size
  const end = start + wrongPage.size
  return filteredWrongList.value.slice(start, end)
})
// 加载错题列表
const loadWrongQuestions = async (forceRefresh = false) => {
  if (!forceRefresh && wrongQuestionList.value && wrongQuestionList.value.length > 0) {
    return
  }
  try {
    wrongLoading.value = true
    const userId = localStorage.getItem('userId')

    if (!userId) {
      ElMessage.warning('用户未登录')
      return
    }

    // 调用获取错题列表的API
    const res = await getWrongQuestionList({
      userId: userId,
    })

    if (res.code === 200) {
      wrongQuestionList.value = res.data || []
      totalWrongCount.value = wrongQuestionList.value.length
      console.log('加载错题列表:', res)
    } else {
      ElMessage.error(res.message || '加载错题本失败')
    }
  } catch (err) {
    console.error('加载错题本失败:', err)
    ElMessage.error('加载错题本失败，请稍后重试')
  } finally {
    wrongLoading.value = false
  }
}
// 移除错题
const handleRemoveWrongQuestion = async (wrongId) => {
  try {
    // 调用移除错题的API
    const res = await removeWrongQuestion({
      wrongId: wrongId,
      userId:localStorage.getItem('userId')
    })
    if (res.code === 200) {
      ElMessage.success('移除成功')
      // 重新加载错题列表
      await loadWrongQuestions(true)
    } else {
      ElMessage.error(res.message || '移除失败')
    }
  } catch (err) {
    console.error('移除错题失败:', err)
    ElMessage.error('移除失败，请稍后重试')
  }
}
// 查看错题详情
const handleViewWrongQuestion = (wrong) => {
  router.push({
    path: '/exam/practice',
    query: {
      questionId: wrong.questionId,
      bankId: wrong.questionBankId,
      isWrong: true
    }
  })
}
// 重新练习错题
const handleRePractice = (wrong) => {
  router.push({
    path: `/question/practiceQuestion/${wrong.questionId}`
  })
}
// 搜索错题
const handleWrongSearch = () => {
  wrongPage.current = 1
}
// 错题分页处理
const handleWrongSizeChange = (size) => {
  wrongPage.size = size
  wrongPage.current = 1
}
const handleWrongCurrentChange = (page) => {
  wrongPage.current = page
}
// 左侧题库合集相关信息=======================================================

// 菜单切换处理
const handleMenuSelect = (index) => {
  currentMenu.value = index
  const titleMap = {
    '1': '个人中心',
    '2': '考试记录',
    '3': '我的笔记',
    '4': '在线考试',
    '5': '题库合集',
    '6': '升级VIP'
  }
  currentTitle.value = titleMap[index]
}

// 用户信息 - 初始为空对象
// 确保 userInfo 正确定义为 ref
const userInfo = ref({
  id: null,
  username: '',
  realName: '',
  phone: '',
  school: '',
  major: '',
  score: '',
  email: '',
  RoleId: null,
  vipExpireTime: null,
  createTime: null,
  avatar: ''
})

const hasRealName = computed(() => Boolean(String(userInfo.value.realName || '').trim()))

const studentDisplayName = computed(() => {
  if (hasRealName.value && !hideRealNameOnStudentPage.value) {
    return userInfo.value.realName
  }
  return userInfo.value.username || '学生'
})

const personalRealNameText = computed(() => {
  if (!hasRealName.value) return '未设置'
  return hideRealNameOnStudentPage.value ? '已隐藏' : userInfo.value.realName
})

// 添加角色名称计算属性
const userRoleName = computed(() => {
  const roleMap = {
    1: '学生',
    2: 'VIP学生',
    3: '教师',
    4: '管理员'
  }
  return roleMap[userInfo.value.RoleId] || '未知角色'
})

const isVipActive = computed(() => {
  if (Number(userInfo.value.RoleId) === 2) {
    return true
  }
  if (!userInfo.value.vipExpireTime) {
    return false
  }
  return dayjs(userInfo.value.vipExpireTime).isAfter(dayjs())
})

const calendarDays = ['一', '二', '三', '四', '五', '六', '今']

const passRatePercent = computed(() => {
  const raw = Number(stats.value.passRate || 0)
  const normalized = raw > 1 ? raw : raw * 100
  return Math.max(0, Math.min(100, normalized))
})

const formattedPassRate = computed(() => `${passRatePercent.value.toFixed(1)}%`)

const learningDays = computed(() => {
  if (!userInfo.value.createTime) return 0
  const days = dayjs().diff(dayjs(userInfo.value.createTime), 'day') + 1
  return Math.max(1, days)
})

const dashboardTasks = computed(() => [
  { label: '完善个人资料', done: hasRealName.value },
  { label: '练习15道编程题', done: Number(stats.value.questionCount || 0) >= 15 },
  { label: '完成1次模拟考试', done: Number(stats.value.examCount || 0) >= 1 },
  { label: '复习错题本', done: activeBankTab.value === 'wrong' && Number(totalWrongCount.value || 0) > 0 },
  { label: '进入交流大厅', done: false }
])

const completedTaskCount = computed(() => dashboardTasks.value.filter(task => task.done).length)

const achievementTitle = computed(() => {
  if (Number(stats.value.questionCount || 0) >= 100) return 'C语言新星'
  if (Number(stats.value.examCount || 0) >= 1) return '模拟考试已启动'
  return '学习旅程已开启'
})

const achievementText = computed(() => {
  if (Number(stats.value.questionCount || 0) > 0) {
    return `已经完成 ${stats.value.questionCount} 道题目`
  }
  return '完成第一道题后会点亮学习成就'
})

// 添加日期格式化函数
const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 个人中心统计数据
const stats = ref({
  examCount: null,
  passRate: null,
  noteCount: null,
  questionCount: null
})

// 加载用户信息
// 加载用户信息
const loadUserData = () => {
  // 从 localStorage 获取角色信息
  const roleStr = localStorage.getItem('user_role')
  let roleId = null

  if (roleStr) {
    try {
      const roleData = JSON.parse(roleStr)
      roleId = roleData.id
    } catch (e) {
      console.error('解析角色信息失败:', e)
    }
  }

  // 从 store 获取用户信息
  userInfo.value.id = userStore.getUserId
  userInfo.value.username = userStore.getUserName
  userInfo.value.realName = userStore.getUserRealName || ''
  userInfo.value.phone = userStore.getUserPhone || ''
  userInfo.value.school = userStore.getUserSchool || ''
  userInfo.value.major = userStore.getUserMajor || ''
  userInfo.value.score = userStore.getUserScore || ''
  userInfo.value.email = userStore.getUserEmail || ''
  userInfo.value.vipExpireTime = userStore.getVipExpireTime
  userInfo.value.createTime = userStore.getCreateTime
  userInfo.value.avatar = userStore.getUserAvatar || ''

  // 设置角色ID（优先使用最新用户信息，localStorage 角色作为旧数据兜底）
  userInfo.value.RoleId = userStore.getUserRoleId || roleId

  console.log('加载的用户信息:', userInfo.value)
}

onMounted(async () => {
  title.value = import.meta.env.VITE_APP_TITLE
  version.value = import.meta.env.VITE_APP_VERSION

  // 加载个人信息
  loadUserData()
  requireRealNameProfile()

  // 加载统计数据
  const statsData = await loadStatsData(localStorage.getItem('userId'))
  if(statsData.code === 200){
    stats.value = statsData.data;
  }else{
    ElMessage.error(statsData.message || '用户学习数据获取失败！')
  }
})

const loadStatsData = async (userId) =>{
  try {
    const data = await getStatsData({
      userId: userId
    })
    return data
  } catch (error) {
      console.error('加载统计数据失败:', error)
    return null
  }
}

// 我的笔记列表（示例数据）
const noteList = ref([
  {
    id: 1,
    title: 'Vue3组合式API学习笔记',
    createTime: '2024-03-05',
    tag: 'Vue',
    content: '组合式API是Vue3的核心特性，包括setup、ref、reactive、computed、watch等API...'
  },
  {
    id: 2,
    title: 'JavaScript异步编程总结',
    createTime: '2024-03-08',
    tag: 'JavaScript',
    content: '异步编程方式：回调函数、Promise、async/await...'
  }
])


// 适配数字状态的考试标签类型（0=未开始，1=进行中，2=已结束）
const getExamStatusTagType = (status) => {
  switch (status) {
    case 1: // 进行中 - 绿色成功标签
      return 'success'
    case 0: // 未开始 - 蓝色信息标签
      return 'info'
    case 2: // 已结束 - 灰色默认标签
      return 'default'
    default: // 兜底
      return 'warning'
  }
}

// 分页参数
const examPage = reactive({
  current: 1,
  size: 10
})
const notePage = reactive({
  current: 1,
  size: 10
})

// 分页处理
const handleSizeChange = (val) => {
  console.log(`每页 ${val} 条`)
}
const handleCurrentChange = (val) => {
  console.log(`当前页 ${val}`)
}
// 开始考试
const handleStartExam = (id) => {
  router.push(`/exam/startExam/${id}`)
}

// 新建笔记
const handleAddNote = () => {
  ElMessage.info('新建笔记')
}

// 下拉菜单命令处理
const handleCommand = (command) => {
  const map = {
    // password: '修改密码',
    // profile: '个人资料',
    // notification: '通知设置'
    nothing:'暂无'
  }
  ElMessage.info(map[command])
}
// 刷新冷却状态
const isRefreshing = ref(false)
// 刷新函数（带冷却）
const handleRefresh = async () => {
  // 检查是否在冷却中
  if (isRefreshing.value) {
    ElMessage.warning('操作过于频繁，请稍后再试')
    return
  }
  // 开始冷却
  isRefreshing.value = true

  try {
    // 显示加载提示
    const loading = ElLoading.service({
      fullscreen: false,
      text: '刷新中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })

    // 根据当前菜单刷新数据
    switch (currentMenu.value) {
      case '1':
        await refreshPersonalCenter()
        break
      case '2':
        await loadExamRecordList(true)
        break
      case '3':
        //await loadNoteList(true)
        break
      case '4':
        await loadOnlineExamList(true)
        break
      case '5':
        await loadAllBanks(true)
        if (activeBankTab.value === 'wrong'){
          await loadWrongQuestions(true);
        }
        break
      case '6':
        await loadVipPlans(true)
        break
    }

    ElMessage.success('刷新成功')
    loading.close()
  } catch (error) {
    console.error('刷新失败:', error)
    ElMessage.error('刷新失败，请稍后重试')
  } finally {
    // 5秒后解除冷却
    setTimeout(() => {
      isRefreshing.value = false
    }, 5000)
  }
}
// 刷新个人中心数据
const refreshPersonalCenter = async () => {
  // 重新获取用户信息
  await loadUserData()
  // 重新获取统计数据
  await loadStatsData(localStorage.getItem('userId'))
}
// 退出登录
const handleLogout = () => {

  localStorage.clear();

  ElMessage.info('退出登录成功')
  router.push('/login')
}
</script>

<style scoped>
/* 让错误信息弹窗内容可滚动 */
:deep(.error-message-dialog .el-message-box__content) {
  max-height: 400px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
}
/* 头像预览区域 */
.avatar-preview {
  cursor: pointer;
  display: inline-block;
  text-align: center;
  transition: all 0.3s;
  padding: 10px;
  border-radius: 8px;
}

.avatar-preview:hover {
  background-color: #f5f7fa;
  transform: translateY(-2px);
}

.avatar-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.preview-avatar {
  margin: 0 auto;
  background-color: #f5f7fa;
}

/* 头像选择器模态框样式 */
.avatar-selector-modal {
  padding: 20px 0;
}

.avatar-list-modal {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  justify-items: center;
  align-items: center;
}

.avatar-item-modal {
  cursor: pointer;
  text-align: center;
  padding: 15px;
  border-radius: 12px;
  transition: all 0.3s;
  border: 2px solid transparent;
  width: 100%;
}

.avatar-item-modal:hover {
  background-color: #f5f7fa;
  transform: translateY(-2px);
}

.avatar-item-modal.active {
  border-color: #409eff;
  background-color: #ecf5ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.avatar-name-modal {
  font-size: 12px;
  color: #666;
  margin-top: 8px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .avatar-list-modal {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }

  .avatar-item-modal {
    padding: 10px;
  }

  .avatar-item-modal .el-avatar {
    width: 60px;
    height: 60px;
  }
}
/* 头像选择器样式 */
.avatar-selector {
  margin-top: 15px;
}

.avatar-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  text-align: center;
}

.avatar-list {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.avatar-item {
  cursor: pointer;
  text-align: center;
  padding: 10px;
  border-radius: 8px;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.avatar-item:hover {
  background-color: #f5f7fa;
  transform: translateY(-2px);
}

.avatar-item.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.avatar-name {
  font-size: 12px;
  color: #666;
  margin-top: 8px;
}

.preview-avatar {
  margin: 0 auto 10px;
  background-color: #f5f7fa;
}
/* 题目样例样式（适配含换行符的文本） */
.sample-list {
  padding: 8px 0;
}

.sample-item {
  margin-bottom: 16px;
  padding: 12px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.sample-item:last-child {
  margin-bottom: 0;
}

/* 保留换行符的样例文本 */
.sample-content-text {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
}

.sample-content-text pre {
  margin: 0;
  color: #333;
  line-height: 1.6;
  white-space: pre-wrap; /* 保留换行符，自动换行 */
  word-break: break-all;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .sample-item {
    padding: 8px;
  }
  .sample-content-text {
    font-size: 12px;
  }
}
/* 题目样例样式 */
.sample-list {
  padding: 8px 0;
}

.sample-item {
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.sample-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}


/* 测试用例表格样式 */
.test-case-table {
  overflow-x: auto;
}

.test-case-table table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.test-case-table th,
.test-case-table td {
  padding: 8px 12px;
  border: 1px solid #e6e6e6;
  text-align: left;
}

.test-case-table th {
  background-color: #f5f7fa;
  color: #666;
  font-weight: 600;
}

.test-case-table tbody tr:hover {
  background-color: #fafafa;
}

.test-input, .test-expected, .test-actual {
  word-break: break-all;
  color: #333;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .sample-row {
    flex-wrap: wrap;
  }

  .sample-sub-label {
    width: 100%;
    margin-bottom: 4px;
  }

  .test-case-table th,
  .test-case-table td {
    padding: 6px 8px;
  }

  .test-case-table th:nth-child(2),
  .test-case-table td:nth-child(2),
  .test-case-table th:nth-child(3),
  .test-case-table td:nth-child(3) {
    min-width: 100px;
  }
}
/* 错题本容器 - 对齐考试记录风格 */
.wrong-container {
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  overflow: hidden;
  margin: 10px 0;
}

/* 列表头部（参考考试记录标题栏） */
.wrong-list-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e6e6e6;
  font-weight: 600;
  font-size: 13px;
  color: #666;
}

.header-item {
  text-align: left;
}

/* 单行错题列表 */
.wrong-list-row {
  background-color: #fff;
}

/* 单行错题项 */
.wrong-item-row {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.wrong-item-row:last-child {
  border-bottom: none;
}

.wrong-item-row:hover {
  background-color: #fafafa;
}

/* 列布局（统一宽度） */
.wrong-item-col {
  display: flex;
  align-items: center;
  overflow: hidden;
}

/* 标签样式 */
.wrong-tag-row {
  background-color: #fef0f0;
  border-color: #f56c6c;
  color: #f56c6c;
  font-size: 10px;
  padding: 0 6px;
  margin-right: 8px;
}

/* 标题文本 */
.title-text-row {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 来源文本 */
.source-text-row {
  font-size: 13px;
  color: #666;
}

/* 时间文本 */
.time-text-row {
  font-size: 13px;
  color: #999;
}

/* 分页样式 */
.wrong-pagination-row {
  padding: 12px 0;
  display: flex;
  justify-content: flex-end;
}

/* 错题详情模态框样式 */
.wrong-detail-content {
  padding: 8px 0;
}

.detail-section {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #e6e6e6;
}

.detail-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.section-title {
  margin: 0 0 12px 0;
  font-size: 15px;
  color: #333;
  font-weight: 600;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-label {
  width: 100px;
  color: #666;
  flex-shrink: 0;
}

.info-value {
  color: #333;
  flex: 1;
}

.answer-row {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.answer-label {
  width: 100px;
  color: #666;
  flex-shrink: 0;
}

.answer-value {
  flex: 1;
  padding: 4px 8px;
  border-radius: 4px;
}

.user-answer {
  background-color: #fef0f0;
  color: #f56c6c;
}

.correct-answer {
  background-color: #f0f9ff;
  color: #409eff;
}

.analysis-content {
  padding: 8px;
  background-color: #f9f9f9;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.6;
}

.error-msg {
  color: #f56c6c;
  margin: 0;
}

.empty-detail {
  padding: 40px 0;
  text-align: center;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .wrong-item-row {
    flex-wrap: wrap;
    gap: 12px;
    padding: 12px;
  }

  .wrong-item-col {
    width: 100% !important;
    text-align: left !important;
  }

  .wrong-list-header {
    display: none; /* 移动端隐藏标题栏 */
  }

  .wrong-detail-content {
    padding: 0;
  }

  .info-row, .answer-row {
    flex-wrap: wrap;
  }

  .info-label, .answer-label {
    width: 100%;
    margin-bottom: 4px;
  }
}
/* 错题本容器 - 对齐考试记录风格 */
.wrong-container {
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  overflow: hidden;
  margin: 10px 0;
}

/* 列表头部（参考考试记录标题栏） */
.wrong-list-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e6e6e6;
  font-weight: 600;
  font-size: 13px;
  color: #666;
}

.header-item {
  text-align: left;
}

/* 单行错题列表 */
.wrong-list-row {
  background-color: #fff;
}

/* 单行错题项 */
.wrong-item-row {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.wrong-item-row:last-child {
  border-bottom: none;
}

.wrong-item-row:hover {
  background-color: #fafafa;
}

/* 列布局（统一宽度） */
.wrong-item-col {
  display: flex;
  align-items: center;
  overflow: hidden;
}

/* 标签样式 */
.wrong-tag-row {
  background-color: #fef0f0;
  border-color: #f56c6c;
  color: #f56c6c;
  font-size: 10px;
  padding: 0 6px;
  margin-right: 8px;
}

/* 标题文本 */
.title-text-row {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 来源文本 */
.source-text-row {
  font-size: 13px;
  color: #666;
}

/* 时间文本 */
.time-text-row {
  font-size: 13px;
  color: #999;
}

/* 错误次数 */
.count-text-row {
  font-size: 13px;
  color: #e6a23c;
}

/* 分页样式 */
.wrong-pagination-row {
  padding: 12px 0;
  display: flex;
  justify-content: flex-end;
}

/* 错题详情模态框样式 */
.wrong-detail-content {
  padding: 8px 0;
}

.detail-section {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #e6e6e6;
}

.detail-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.section-title {
  margin: 0 0 12px 0;
  font-size: 15px;
  color: #333;
  font-weight: 600;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-label {
  width: 100px;
  color: #666;
  flex-shrink: 0;
}

.info-value {
  color: #333;
  flex: 1;
}

.answer-row {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.answer-label {
  width: 100px;
  color: #666;
  flex-shrink: 0;
}

.answer-value {
  flex: 1;
  padding: 4px 8px;
  border-radius: 4px;
}

.user-answer {
  background-color: #fef0f0;
  color: #f56c6c;
}

.correct-answer {
  background-color: #f0f9ff;
  color: #409eff;
}

.analysis-content {
  padding: 8px;
  background-color: #f9f9f9;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.6;
}

.error-msg {
  color: #f56c6c;
  margin: 0 0 8px 0;
}

.analysis-msg {
  color: #666;
  margin: 0;
}

.empty-detail {
  padding: 40px 0;
  text-align: center;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .wrong-item-row {
    flex-wrap: wrap;
    gap: 12px;
    padding: 12px;
  }

  .wrong-item-col {
    width: 100% !important;
    text-align: left !important;
  }

  .wrong-list-header {
    display: none; /* 移动端隐藏标题栏 */
  }

  .wrong-detail-content {
    padding: 0;
  }

  .info-row, .answer-row {
    flex-wrap: wrap;
  }

  .info-label, .answer-label {
    width: 100%;
    margin-bottom: 4px;
  }
}
/* 错题本-单行列表样式 核心 */
.wrong-list-row {
  padding: 16px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin: 10px 0;
}

/* 单行错题项 */
.wrong-item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 8px;
  border-bottom: 1px solid #f5f5f5;
  transition: background-color 0.2s ease;
}

/* 最后一行去掉下边框 */
.wrong-item-row:last-child {
  border-bottom: none;
}

/* 鼠标悬浮高亮 */
.wrong-item-row:hover {
  background-color: #fafafa;
}

/* 左侧基础信息 */
.wrong-base-info-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  flex-wrap: wrap;
}

.wrong-tag-row {
  background-color: #fef0f0;
  border-color: #f56c6c;
  color: #f56c6c;
  font-size: 10px;
  padding: 0 6px;
}

.title-text-row {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  max-width: 200px; /* 限制标题宽度，避免挤压其他内容 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.source-text-row {
  font-size: 12px;
  color: #666;
  background-color: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.time-text-row {
  font-size: 12px;
  color: #999;
}

.count-text-row {
  font-size: 12px;
  color: #e6a23c;
}

/* 右侧操作按钮 */
.wrong-actions-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 骨架屏适配 */
.wrong-skeleton-item-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

/* 分页样式 */
.wrong-pagination-row {
  padding: 12px 16px;
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

/* 响应式适配（移动端） */
@media (max-width: 768px) {
  .wrong-base-info-row {
    gap: 8px;
  }
  .title-text-row {
    max-width: 120px;
  }
  .wrong-actions-row {
    gap: 4px;
  }
  .wrong-item-row {
    flex-wrap: wrap;
    gap: 8px;
    padding: 10px 4px;
  }
  .wrong-actions-row {
    margin-top: 8px;
    align-self: flex-end;
  }
}
.meta-item-small .el-icon {
  font-size: 11px;
}
/* 小卡片底部操作按钮 */
.wrong-footer-small {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 4px;
}
.wrong-footer-small .el-button {
  padding: 4px 8px;
  font-size: 12px;
}
/* 小卡片骨架屏 */
.wrong-skeleton-item-small {
  margin-bottom: 16px;
}
/* 小卡片分页 */
.wrong-pagination-small {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
}
/* 响应式适配小卡片 */
@media (max-width: 768px) {
  .wrong-list-small {
    padding: 12px;
  }
  .wrong-card-small {
    height: auto;
  }
  .wrong-card-small-content {
    gap: 12px;
  }
  .question-meta-small {
    gap: 12px;
  }
  .wrong-footer-small {
    flex-wrap: wrap;
    gap: 6px;
  }
  .wrong-footer-small .el-button {
    flex: 1;
    min-width: 80px;
  }
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}


.wrong-answer .answer-label {
  color: #f56c6c;
}

.correct-answer .answer-label {
  color: #67c23a;
}


.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #999;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .answer-compare {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .wrong-footer {
    flex-direction: column;
  }

  .wrong-footer .el-button {
    width: 100%;
  }

  .wrong-list {
    padding: 12px;
  }

  .wrong-card-content {
    padding: 15px;
  }
}
.collect-btn {
  padding: 5px 10px;
  font-size: 12px;
}

.collect-btn .el-icon {
  margin-right: 4px;
}
/* 在 style 标签中添加以下样式 */
/* VIP 标签样式 */
.vip-tag {
  background-color: #fef0f0;
  border-color: #f56c6c;
  color: #f56c6c;
  font-size: 10px;
  height: 20px;
  line-height: 20px;
  padding: 0 6px;
}

/* VIP 文字样式 */
.vip-text {
  color: #f56c6c;
  font-weight: 500;
}

/* 错题文字样式 */
.wrong-text {
  color: #e6a23c;
}

/* 元信息中的图标样式调整 */
.meta-item .el-icon {
  font-size: 12px;
}
/* 题库合集样式 */
.question-bank {
  padding: 0;
}

.bank-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  flex-wrap: wrap;
  gap: 15px;
}

.bank-tabs {
  flex-shrink: 0;
}

.bank-search {
  flex-shrink: 0;
}

/* 骨架屏样式 */
.bank-list-skeleton {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
  padding: 20px;
}

.bank-skeleton-item {
  height: 160px;
}

/* 题库卡片列表 */
.bank-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
  padding: 20px;
}

.bank-card {
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--app-border);
}

.bank-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.12);
  border-color: var(--app-primary-light, rgba(37, 99, 235, 0.3));
}

.bank-card-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 卡片头部 */
.bank-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.bank-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  flex: 1;
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.level-tag {
  flex-shrink: 0;
}

.collect-btn {
  flex-shrink: 0;
  margin-left: 8px;
}

/* 题库描述 */
.bank-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 42px;
}

/* 元信息 */
.bank-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  padding: 8px 0;
  border-top: 1px solid #f5f5f5;
  border-bottom: 1px solid #f5f5f5;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #999;
}

.meta-item .el-icon {
  font-size: 14px;
}

/* 底部按钮 */
.bank-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 4px;
}

.bank-footer .el-button {
  min-width: 100px;
}

/* 分页样式 */
.bank-pagination {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 16px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.bank-next-hint {
  color: #64748b;
  font-size: 13px;
  white-space: nowrap;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .bank-header-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .bank-search {
    width: 100%;
  }

  .bank-search .el-input {
    width: 100% !important;
  }

  .bank-list {
    grid-template-columns: 1fr;
    padding: 12px;
  }

  .bank-list-skeleton {
    grid-template-columns: 1fr;
  }

  .title-text {
    max-width: 180px;
  }

  .bank-meta {
    gap: 12px;
  }

  .bank-footer .el-button {
    width: 100%;
  }
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  padding: 10px 0;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}
.stat-icon {
  width: 40px;
  height: 40px;
  line-height: 40px;
  text-align: center;
  background: #e8f4f8;
  border-radius: 50%;
  color: #409eff;
  font-size: 18px;
}
.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}
.stat-label {
  font-size: 14px;
  color: #666;
}
/* 整体布局 */
.student-center-container {
  display: flex;
  height: 100vh;
  background-color: #f5f7fa;
}
/* 侧边栏 */
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
/* 用户信息 */
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
/* 侧边栏菜单 */
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
/* 退出登录按钮 */
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
/* 主内容区域 */
.main-content {
  flex: 1;
  margin-left: 250px;
  padding: 20px;
  overflow-y: auto;
}
/* 内容头部 */
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
/* 内容主体 */
.content-body {
  background-color: #fff;
  border-radius: 8px;
  min-height: calc(100vh - 120px);
}
.page-content {
  padding: 20px;
}
/* 个人中心样式 */
.personal-center {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.info-card {
  border-radius: 8px;
}
.personal-info {
  display: flex;
  padding: 20px;
  gap: 30px;
}
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.big-avatar {
  margin-bottom: 10px;
}
.edit-avatar-btn {
  width: 100%;
}
.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 15px;
}
.info-item {
  display: flex;
  align-items: center;
  font-size: 14px;
}
.info-item .label {
  color: #666;
  width: 80px;
}
.info-item .value {
  color: #333;
  flex: 1;
}
.edit-info-btn {
  width: 120px;
  margin-top: 10px;
}
/* 数据概览 */
.stats-card-wrap {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}
.stats-card {
  flex: 1;
  min-width: 200px;
  border-radius: 8px;
}
.stats-item {
  display: flex;
  align-items: center;
  padding: 10px;
}
.stats-icon {
  font-size: 32px;
  color: #409EFF;
  margin-right: 15px;
}
.stats-content {
  flex: 1;
}
.stats-num {
  font-size: 24px;
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
}
.stats-text {
  font-size: 14px;
  color: #666;
}
/* 考试记录样式 */
.exam-record {
  padding: 0;
}
.search-bar {
  display: flex;
  gap: 15px;
  padding: 20px;
  align-items: center;
  flex-wrap: wrap;
}
.search-input {
  width: 300px;
}
.status-select {
  width: 150px;
}
.date-picker {
  width: 300px;
}
.pagination {
  margin-top: 20px;
  padding: 0 20px 20px;
  text-align: right;
}
.pass-score {
  color: #67C23A;
  font-weight: 500;
}
.fail-score {
  color: #F56C6C;
}
/* 学习统计样式 */
.learn-stats {
  padding: 0;
}
.chart-tabs {
  margin-bottom: 20px;
}
.chart-container {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.chart-placeholder {
  text-align: center;
  color: #999;
}
.chart-icon {
  font-size: 48px;
  margin-bottom: 15px;
}
.chart-text {
  font-size: 18px;
  margin-bottom: 10px;
}
/* 我的笔记样式 */
.my-notes {
  padding: 0;
}
.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  flex-wrap: wrap;
  gap: 15px;
}
.add-note-btn {
  margin-bottom: 10px;
}
.note-search-input {
  width: 300px;
}
.note-list {
  padding: 0 20px 20px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}
.note-item {
  height: 200px;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: all 0.3s;
}
.note-item:hover {
  transform: translateY(-2px);
}
.note-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.note-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
}
.note-content {
  flex: 1;
  font-size: 14px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-line-orient: vertical;
  margin-bottom: 10px;
  line-height: 1.5;
}
.note-actions {
  text-align: right;
  border-top: 1px solid #f0f0f0;
  padding-top: 10px;
}
/* 在线考试样式 */
.online-exam {
  padding: 0;
}
.exam-list {
  padding: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}
.exam-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
}
.exam-card:hover {
  transform: translateY(-2px);
}
.exam-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.exam-card-title {
  font-size: 16px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 10px;
}
.exam-card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}
.exam-info-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #666;
}
.exam-info-item .el-icon {
  margin-right: 8px;
  color: #409EFF;
}
.exam-card-footer {
  text-align: right;
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
}
/* 题库合集样式 */
.question-bank {
  padding: 0;
}
.bank-category {
  margin-bottom: 20px;
}
.bank-list {
  padding: 0 20px 20px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}
.bank-item {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
}
.bank-item:hover {
  transform: translateY(-2px);
}
.bank-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.bank-title {
  font-size: 16px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 10px;
}
.bank-body {
  flex: 1;
  margin-bottom: 15px;
}
.bank-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-line-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
  min-height: 42px;
}
.bank-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  font-size: 12px;
  color: #999;
}
.bank-footer {
  text-align: right;
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
}
/* 响应式适配 */
@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    height: auto;
    position: relative;
  }
  .main-content {
    margin-left: 0;
  }
  .personal-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 20px;
  }
  .info-item {
    justify-content: center;
  }
  .stats-card-wrap {
    flex-direction: column;
  }
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .search-input, .status-select, .date-picker {
    width: 100%;
  }
  .exam-list, .bank-list, .note-list {
    grid-template-columns: 1fr;
  }
  .note-header {
    flex-direction: column;
    align-items: stretch;
  }
  .note-search-input {
    width: 100%;
  }
}
/* 个人信息区域样式 */
.personal-info {
  display: flex;
  padding: 20px;
  gap: 30px;
}
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  min-width: 120px;
}
.big-avatar {
  margin-bottom: 10px;
}
.edit-avatar-btn {
  width: 100%;
}
.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px 30px;
}
.info-item {
  display: flex;
  align-items: flex-start;
  font-size: 14px;
  line-height: 1.5;
}
.info-item .label {
  color: #666;
  width: 100px;
  flex-shrink: 0;
  font-weight: normal;
}
.info-item .value {
  color: #333;
  flex: 1;
  word-break: break-word;
}
.edit-info-btn {
  width: 120px;
  margin-top: 10px;
  align-self: flex-start;
}
/* 响应式适配 */
@media screen and (max-width: 768px) {
  .personal-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  .info-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  .info-item {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  .info-item .label {
    width: auto;
    margin-bottom: 4px;
  }
  .edit-info-btn {
    align-self: center;
    width: 100%;
    max-width: 200px;
  }
}

/* Visual refresh: quiet exam workspace */
.student-center-container {
  min-height: 100vh;
  background: var(--app-bg);
}

.sidebar {
  background: var(--app-surface);
  border-right: 1px solid var(--app-border);
  box-shadow: none;
}

.sidebar-header {
  border-bottom-color: var(--app-border);
}

.sidebar-header h3,
.header-title {
  color: var(--app-text);
  font-weight: 750;
  letter-spacing: 0;
}

.user-info {
  border-bottom-color: var(--app-border);
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

.sidebar-menu :deep(.el-menu-item:hover) {
  color: var(--app-primary);
  background: var(--app-primary-soft);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  color: var(--app-primary);
  background: var(--app-primary-soft);
}

.main-content {
  background:
      linear-gradient(180deg, #f8fbff 0%, var(--app-bg) 42%, #edf4fb 100%);
}

.content-header {
  min-height: 56px;
  padding: 0 2px;
}

.content-body {
  background: transparent;
  border-radius: 0;
}

.page-content {
  padding: 4px 0 28px;
}

.page-content > .el-card,
.stats-card > .el-card,
.info-card {
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  box-shadow: var(--app-shadow-sm);
}

.info-item .label {
  color: var(--app-text-muted);
  font-weight: 650;
}

.info-item .value,
.stat-value {
  color: var(--app-text);
  font-weight: 750;
}

.stats-grid,
.exam-list,
.bank-list {
  gap: 16px;
}

.stat-item,
.exam-card,
.bank-card {
  border-radius: var(--app-radius);
}

.bank-card,
.exam-card {
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.bank-card:hover,
.exam-card:hover {
  transform: translateY(-2px);
  border-color: var(--app-border-strong);
}

.vip-tag,
.vip-text {
  color: var(--app-vip);
  font-weight: 800;
}

.vip-hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 260px;
  gap: 20px;
  align-items: stretch;
  padding: 32px;
  margin-bottom: 18px;
  background: linear-gradient(135deg, #fef9ef 0%, #fff5e6 40%, #ffecd2 100%);
  border: 1px solid #f5deb3;
  border-radius: var(--app-radius);
  box-shadow: 0 4px 20px rgba(255, 193, 7, 0.1);
}

.eyebrow {
  color: #e6a817;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.vip-hero-panel h2 {
  margin: 8px 0 10px;
  color: #5a3e00;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.2;
  text-wrap: balance;
}

.vip-hero-panel p {
  max-width: 680px;
  margin: 0;
  color: #8b6914;
  line-height: 1.75;
}

.vip-status-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 18px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid #f0d9a0;
  border-radius: var(--app-radius-sm);
  backdrop-filter: blur(4px);
}

.status-label {
  color: #a07820;
  font-size: 13px;
  font-weight: 700;
}

.vip-status-card strong {
  margin: 6px 0;
  color: #5a3e00;
  font-size: 24px;
  font-weight: 800;
}

.vip-status-card small {
  color: #8b6914;
}

.vip-redeem-section {
  margin-bottom: 18px;
}

.redeem-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 20px 28px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  box-shadow: var(--app-shadow-sm);
}

.redeem-info h3 {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 700;
  color: var(--app-text);
}

.redeem-info p {
  margin: 0;
  color: var(--app-text-muted);
  font-size: 14px;
}

.redeem-action {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.vip-plan-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.vip-plan-card {
  display: flex;
  flex-direction: column;
  min-height: 340px;
  padding: 24px;
  background: linear-gradient(180deg, #fffdf7 0%, #fff 100%);
  border: 1px solid #f0d9a0;
  border-radius: var(--app-radius);
  box-shadow: 0 2px 12px rgba(255, 193, 7, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;
}

.vip-plan-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(255, 193, 7, 0.15);
}

.plan-topline,
.contact-row,
.plan-note {
  display: flex;
  align-items: center;
  gap: 10px;
}

.plan-topline {
  justify-content: space-between;
}

.plan-name {
  color: #5a3e00;
  font-size: 18px;
  font-weight: 800;
}

.plan-price {
  margin: 20px 0 12px;
  color: #d4930a;
  font-size: 38px;
  font-weight: 850;
  font-variant-numeric: tabular-nums;
  line-height: 1;
}

.plan-price span {
  margin-right: 4px;
  color: #a07820;
  font-size: 18px;
}

.plan-benefits {
  min-height: 54px;
  margin: 0 0 18px;
  color: #8b6914;
}

.contact-panel {
  display: grid;
  gap: 8px;
  margin-top: auto;
  padding: 12px;
  background: var(--app-surface-soft);
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius-sm);
}

.contact-row {
  justify-content: space-between;
  min-height: 32px;
}

.contact-row span {
  color: var(--app-text-muted);
  font-size: 13px;
}

.contact-row strong {
  min-width: 0;
  color: var(--app-text);
  font-weight: 800;
  overflow-wrap: anywhere;
}

.plan-note {
  align-items: flex-start;
  margin-top: 14px;
  color: var(--app-text-muted);
  line-height: 1.6;
}

@media (max-width: 1024px) {
  .vip-plan-grid {
    grid-template-columns: 1fr;
  }

  .vip-hero-panel {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .student-center-container {
    display: block;
  }

  .main-content {
    padding: 16px;
  }

  .vip-hero-panel {
    padding: 20px;
  }
}

/* Student visual system page overrides */
.student-center-container.student-shell {
  display: grid;
  height: auto;
  min-height: 100vh;
  background:
      radial-gradient(circle at 18% 8%, rgba(37, 99, 235, 0.10), transparent 30%),
      radial-gradient(circle at 86% 0%, rgba(124, 92, 255, 0.09), transparent 26%),
      linear-gradient(180deg, #f8fbff 0%, #f3f7fe 48%, #edf4fb 100%);
}

.sidebar.student-sidebar {
  width: auto;
  position: sticky;
  background: rgba(255, 255, 255, 0.86);
}

.sidebar-header {
  padding: 0 4px 22px;
  text-align: left;
}

.user-info {
  margin: 6px 0 18px;
  padding: 22px 10px;
  background: linear-gradient(180deg, rgba(239, 246, 255, 0.84), rgba(255, 255, 255, 0.7));
  border: 1px solid rgba(207, 220, 240, 0.82);
  border-radius: 8px;
}

.user-avatar {
  border: 3px solid rgba(255, 255, 255, 0.92);
  box-shadow: 0 16px 28px rgba(37, 99, 235, 0.16);
}

.main-content.student-main {
  margin-left: 0;
  padding: 24px 28px 34px;
  background: transparent;
}

.content-header.student-topbar {
  min-height: 60px;
  margin-bottom: 22px;
}

.header-title-wrap {
  min-width: 0;
}

.header-title-wrap p {
  margin: 3px 0 0;
  color: var(--app-text-muted);
  font-size: 13px;
}

.student-search-pill {
  width: min(520px, 38vw);
  min-height: 48px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 14px;
  color: #7a8ba6;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(207, 220, 240, 0.82);
  border-radius: 8px;
  box-shadow: 0 12px 24px rgba(40, 78, 142, 0.06);
}

.student-search-pill span {
  min-width: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.student-search-pill kbd {
  padding: 4px 9px;
  color: #7a8ba6;
  background: #f2f6fd;
  border: 1px solid rgba(207, 220, 240, 0.82);
  border-radius: 7px;
  font-family: inherit;
  font-size: 12px;
  font-weight: 750;
}

.content-body.student-content {
  display: block;
  background: transparent;
}

.dashboard-hero {
  margin-bottom: 18px;
}

.dashboard-hero .eyebrow {
  margin: 0 0 10px;
  color: var(--app-primary);
  font-size: 12px;
  font-weight: 850;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 22px;
  flex-wrap: wrap;
}

.dashboard-orbit {
  width: min(260px, 100%);
}

.dashboard-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(300px, 0.65fr);
  gap: 18px;
  margin-bottom: 18px;
}

.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}

.section-heading h2 {
  margin: 0;
  color: var(--app-text);
  font-size: 20px;
  font-weight: 850;
}

.section-heading p {
  margin: 5px 0 0;
  color: var(--app-text-muted);
  font-size: 13px;
}

.profile-summary {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.profile-summary strong {
  display: block;
  color: var(--app-text);
  font-size: 22px;
  font-weight: 850;
}

.profile-summary span {
  display: inline-flex;
  margin-top: 6px;
  padding: 3px 10px;
  color: var(--app-primary);
  background: var(--app-primary-soft);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.dashboard-info-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 20px;
}

.dashboard-info-grid .info-item {
  align-items: center;
  min-height: 42px;
  padding: 10px 12px;
  background: rgba(248, 251, 255, 0.78);
  border: 1px solid rgba(225, 234, 247, 0.92);
  border-radius: 8px;
}

.dashboard-info-grid .label {
  width: 88px;
  color: #718199;
}

.task-panel {
  min-height: 100%;
}

.task-progress {
  display: grid;
  justify-items: center;
  gap: 18px;
}

.task-list {
  width: 100%;
  display: grid;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.task-list li {
  display: grid;
  grid-template-columns: 22px minmax(0, 1fr);
  align-items: center;
  min-height: 34px;
  color: var(--app-text-muted);
  font-size: 14px;
  font-weight: 700;
}

.task-list li span {
  width: 18px;
  height: 18px;
  display: grid;
  place-items: center;
  border: 1px solid #d9e4f3;
  border-radius: 5px;
  color: #fff;
  font-size: 12px;
}

.task-list li.done {
  color: var(--app-text);
}

.task-list li.done span {
  background: var(--app-success);
  border-color: var(--app-success);
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.dashboard-lower-grid {
  display: grid;
  grid-template-columns: minmax(0, 0.85fr) minmax(0, 1.15fr);
  gap: 18px;
  margin-top: 18px;
}

.calendar-strip {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 10px;
}

.calendar-strip span {
  min-height: 46px;
  display: grid;
  place-items: center;
  color: #7586a1;
  background: #f5f8fd;
  border: 1px solid rgba(218, 229, 245, 0.92);
  border-radius: 8px;
  font-weight: 800;
}

.calendar-strip span.active {
  color: #fff;
  background: linear-gradient(135deg, #1f7bff, #7c5cff);
  border-color: transparent;
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.22);
}

.achievement-row {
  display: grid;
  grid-template-columns: 76px minmax(0, 1fr);
  gap: 16px;
  align-items: center;
}

.achievement-medal {
  width: 76px;
  height: 76px;
  display: grid;
  place-items: center;
  color: #fff;
  border-radius: 50%;
  background: linear-gradient(135deg, #f59e0b, #ffc857);
  box-shadow: 0 18px 34px rgba(245, 158, 11, 0.26);
  font-size: 34px;
  font-weight: 950;
}

.achievement-row strong {
  display: block;
  color: var(--app-text);
  font-size: 20px;
  font-weight: 850;
}

.achievement-row small {
  display: block;
  margin-top: 6px;
  color: var(--app-text-muted);
  line-height: 1.6;
}

.exam-record > .el-card,
.online-exam > .el-card,
.question-bank > .el-card {
  border-radius: 8px;
  border-color: rgba(207, 220, 240, 0.88);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 14px 34px rgba(40, 78, 142, 0.08);
}

.search-bar,
.bank-header-bar {
  background: linear-gradient(180deg, rgba(248, 251, 255, 0.92), rgba(255, 255, 255, 0.9));
}

.exam-card,
.bank-card,
.vip-plan-card {
  border-radius: 8px;
}

@media (max-width: 1180px) {
  .student-center-container.student-shell {
    grid-template-columns: 232px minmax(0, 1fr);
  }

  .student-search-pill {
    width: min(420px, 34vw);
  }

  .quick-actions-grid,
  .student-grid.student-grid--4 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-layout,
  .dashboard-lower-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .student-center-container.student-shell {
    display: block;
  }

  .sidebar.student-sidebar {
    position: relative;
    height: auto;
    border-right: 0;
  }

  .main-content.student-main {
    padding: 18px;
  }

  .content-header.student-topbar {
    align-items: stretch;
  }

  .student-search-pill {
    width: 100%;
  }
}

@media (max-width: 680px) {
  .main-content.student-main {
    padding: 14px;
  }

  .dashboard-info-grid,
  .quick-actions-grid,
  .student-grid.student-grid--4 {
    grid-template-columns: 1fr;
  }

  .section-heading,
  .profile-summary {
    align-items: stretch;
    flex-direction: column;
  }

  .dashboard-info-grid .info-item {
    display: grid;
    gap: 4px;
  }

  .dashboard-info-grid .label {
    width: auto;
  }
}

/* Avatar upload & crop */
.upload-avatar-section {
  text-align: center;
  padding: 8px 0;
}

.crop-container {
  width: 100%;
  height: 320px;
  overflow: hidden;
}

.crop-image {
  max-width: 100%;
  max-height: 100%;
  display: block;
}
</style>
