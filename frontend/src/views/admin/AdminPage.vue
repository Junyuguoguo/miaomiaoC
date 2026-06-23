<template>
  <div class="admin-container">
    <!-- 侧边导航栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h3>管理员后台@v1.0.0</h3>
      </div>
      <div class="user-info">
        <el-avatar :size="60" class="user-avatar">
          <img v-if="userStore.getUserAvatar" :src="fixAvatarUrl(userStore.getUserAvatar)" alt="头像" />
          <el-icon v-else><User /></el-icon>
        </el-avatar>
        <div class="user-name">{{ userStore.getUserName || '管理员' }}</div>
        <div class="user-role">管理员</div>
      </div>
      <el-menu
        :default-active="currentMenu"
        class="sidebar-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item index="1">
          <el-icon><Key /></el-icon>
          <span>邀请码管理</span>
        </el-menu-item>
        <el-menu-item index="2">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="3">
          <el-icon><DataAnalysis /></el-icon>
          <span>全局统计</span>
        </el-menu-item>
        <el-menu-item index="4">
          <el-icon><OfficeBuilding /></el-icon>
          <span>学院管理</span>
        </el-menu-item>
      </el-menu>
      <div class="logout-btn-wrap">
        <el-button type="text" class="logout-btn" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <div class="content-header">
        <div class="header-title">{{ currentTitle }}</div>
        <div class="header-actions">
          <el-button type="text" @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-button type="primary" plain @click="$router.push('/teacher')">返回教师端</el-button>
        </div>
      </div>

      <div class="content-body">
        <!-- 1. 邀请码管理 -->
        <div v-if="currentMenu === '1'" class="page-content">
          <div class="page-title">邀请码管理</div>

          <!-- 生成表单 -->
          <el-card shadow="never" class="gen-card">
            <el-form :inline="true" :model="genForm" class="gen-form">
              <el-form-item label="学院">
                <el-select v-model="genForm.college" placeholder="选择学院" clearable style="width:180px">
                  <el-option v-for="c in collegeOptions" :key="c" :label="c" :value="c" />
                </el-select>
              </el-form-item>
              <el-form-item label="数量">
                <el-input-number v-model="genForm.count" :min="1" :max="50" style="width:140px" />
              </el-form-item>
              <el-form-item label="过期时间" required>
                <el-date-picker
                  v-model="genForm.expiresAt"
                  type="datetime"
                  placeholder="选择过期时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width:220px"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="genLoading" @click="handleGenerate">生成邀请码</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 筛选栏 -->
          <el-card shadow="never" class="gen-card">
            <div class="filter-bar">
              <el-select v-model="codeFilter.college" placeholder="按学院筛选" clearable style="width:180px">
                <el-option v-for="c in collegeOptions" :key="c" :label="c" :value="c" />
              </el-select>
              <el-select v-model="codeFilter.status" placeholder="按状态筛选" clearable style="width:140px">
                <el-option label="未使用" :value="0" />
                <el-option label="已使用" :value="1" />
                <el-option label="已作废" :value="2" />
              </el-select>
              <el-button type="primary" @click="handleCodeFilter">查询</el-button>
              <el-button @click="resetCodeFilter">重置</el-button>
            </div>
          </el-card>

          <!-- 邀请码表格 -->
          <el-table :data="inviteCodeList" v-loading="codeLoading" border class="data-table">
            <el-table-column label="邀请码" prop="code" min-width="220">
              <template #default="{ row }">
                <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap">
                  <code class="code-text">{{ row.code }}</code>
                  <el-button size="small" link type="primary" @click="copyCode(row.code)">复制码</el-button>
                  <el-button size="small" link type="success" @click="copyInviteLink(row.code)">复制链接</el-button>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="学院" prop="college" width="140" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="inviteStatusType(row.status)" size="small">
                  {{ inviteStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建者" prop="createdBy" width="120" />
            <el-table-column label="使用者" prop="usedBy" width="120">
              <template #default="{ row }">{{ row.usedBy || '-' }}</template>
            </el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="170" />
            <el-table-column label="过期时间" prop="expiresAt" width="170">
              <template #default="{ row }">{{ row.expiresAt || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right" align="center">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 0"
                  type="danger"
                  link
                  @click="handleRevokeCode(row.id)"
                >作废</el-button>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="codePage"
              v-model:page-size="codePageSize"
              :page-sizes="[10, 20, 50]"
              :total="codeTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadInviteCodes"
              @current-change="loadInviteCodes"
            />
          </div>
        </div>

        <!-- 2. 用户管理 -->
        <div v-if="currentMenu === '2'" class="page-content">
          <div class="page-title">用户管理</div>

          <el-card shadow="never" class="gen-card">
            <div class="filter-bar">
              <el-select v-model="userFilter.college" placeholder="按学院筛选" clearable style="width:180px">
                <el-option v-for="c in collegeOptions" :key="c" :label="c" :value="c" />
              </el-select>
              <el-select v-model="userFilter.role" placeholder="按角色筛选" clearable style="width:140px">
                <el-option v-for="(label, id) in roleMap" :key="id" :label="label" :value="Number(id)" />
              </el-select>
              <el-input v-model="userFilter.keyword" placeholder="搜索用户名/姓名" clearable style="width:220px" />
              <el-button type="primary" @click="loadUsers">查询</el-button>
            </div>
          </el-card>

          <el-table :data="userList" v-loading="userLoading" border class="data-table">
            <el-table-column label="ID" prop="id" width="70" align="center" />
            <el-table-column label="用户名" prop="username" width="130" />
            <el-table-column label="真实姓名" prop="realName" width="120">
              <template #default="{ row }">{{ row.realName || '-' }}</template>
            </el-table-column>
            <el-table-column label="角色" width="140" align="center">
              <template #default="{ row }">
                <el-select
                  :model-value="row.roleId"
                  size="small"
                  style="width:110px"
                  @change="(val) => handleRoleChange(row.id, row.roleId, val)"
                >
                  <el-option
                    v-for="(label, id) in roleMap"
                    :key="id"
                    :label="label"
                    :value="Number(id)"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="学院" width="180">
              <template #default="{ row }">
                <el-select
                  :model-value="row.college"
                  size="small"
                  clearable
                  placeholder="未设置"
                  style="width:150px"
                  @change="(val) => handleCollegeChange(row.id, val)"
                >
                  <el-option v-for="c in collegeOptions" :key="c" :label="c" :value="c" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-switch
                  :model-value="row.status === 1"
                  @change="(val) => handleStatusChange(row.id, val ? 1 : 0)"
                />
              </template>
            </el-table-column>
            <el-table-column label="创建时间" prop="createTime" min-width="170" />
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="userPage"
              v-model:page-size="userPageSize"
              :page-sizes="[10, 20, 50]"
              :total="userTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadUsers"
              @current-change="loadUsers"
            />
          </div>
        </div>

        <!-- 3. 全局统计 -->
        <div v-if="currentMenu === '3'" class="page-content">
          <div class="page-title">全局统计</div>

          <el-row :gutter="20" class="overview-row">
            <el-col :span="6">
              <el-card shadow="never" class="stat-card">
                <div class="stat-label">总用户数</div>
                <div class="stat-value">{{ overview.totalUsers ?? '-' }}</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="never" class="stat-card">
                <div class="stat-label">教师数</div>
                <div class="stat-value">{{ overview.totalTeachers ?? '-' }}</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="never" class="stat-card">
                <div class="stat-label">学员数</div>
                <div class="stat-value">{{ overview.totalStudents ?? '-' }}</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="never" class="stat-card">
                <div class="stat-label">考试总数</div>
                <div class="stat-value">{{ overview.totalExams ?? '-' }}</div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 图表区域 -->
          <el-row :gutter="20" style="margin-top:20px">
            <el-col :span="14">
              <el-card shadow="never" class="chart-card">
                <template #header>
                  <span>学院师生数对比</span>
                </template>
                <div ref="barChartRef" class="chart-container"></div>
              </el-card>
            </el-col>
            <el-col :span="10">
              <el-card shadow="never" class="chart-card">
                <template #header>
                  <span>用户角色分布</span>
                </template>
                <div ref="pieChartRef" class="chart-container"></div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 学院详细数据表 -->
          <el-card shadow="never" style="margin-top:20px">
            <template #header>
              <span>学院详细数据</span>
            </template>
            <el-table :data="collegeStatsList" v-loading="statsLoading" border class="data-table">
              <el-table-column label="学院" prop="college" min-width="160" />
              <el-table-column label="教师数" prop="teacherCount" width="100" align="center" />
              <el-table-column label="学员数" prop="studentCount" width="100" align="center" />
              <el-table-column label="平均做题数" width="140" align="center">
                <template #default="{ row }">
                  {{ row.avgQuestionCount != null ? Number(row.avgQuestionCount).toFixed(1) : '-' }}
                </template>
              </el-table-column>
              <el-table-column label="平均通过率" width="140" align="center">
                <template #default="{ row }">
                  {{ row.avgPassRate != null ? (Number(row.avgPassRate) * 100).toFixed(1) + '%' : '-' }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>

        <!-- 4. 学院管理 -->
        <div v-if="currentMenu === '4'" class="page-content">
          <el-card shadow="never">
            <template #header>
              <div style="display: flex; align-items: center; justify-content: space-between;">
                <span>学院管理</span>
                <div style="display: flex; gap: 12px;">
                  <el-button type="primary" @click="openCollegeAdd">新增学院</el-button>
                  <el-button type="success" @click="handleInitColleges" :loading="collegeInitLoading">初始化学院数据</el-button>
                </div>
              </div>
            </template>

            <el-table :data="collegeList" v-loading="collegeLoading" border class="data-table">
            <el-table-column label="ID" prop="id" width="80" align="center" />
            <el-table-column label="学院名称" prop="name" min-width="200" />
            <el-table-column label="排序" prop="sortOrder" width="100" align="center" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.enabled === 1 ? 'success' : 'info'" size="small">
                  {{ row.enabled === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="170" />
            <el-table-column label="操作" width="150" fixed="right" align="center">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button type="primary" link @click="openCollegeEdit(row)">编辑</el-button>
                  <el-button type="danger" link @click="handleDeleteCollege(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          </el-card>
        </div>

      </div>
    </div>

    <!-- 学院新增/编辑弹窗 -->
    <el-dialog v-model="collegeDialog" :title="collegeForm.id ? '编辑学院' : '新增学院'" width="480px">
      <el-form :model="collegeForm" label-width="80px">
        <el-form-item label="学院名称" required>
          <el-input v-model="collegeForm.name" placeholder="请输入学院名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="collegeForm.sortOrder" :min="0" :max="999" style="width:100%" />
          <div style="font-size:12px; color:#909399; margin-top:4px;">数字越小越靠前</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="collegeForm.enabled">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="collegeDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCollege">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts/core'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent, CanvasRenderer])
import {
  Key, User, DataAnalysis, SwitchButton, Refresh, OfficeBuilding
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  generateInviteCodes,
  listInviteCodes,
  revokeInviteCode,
  listUsers,
  updateUserRole,
  updateUserStatus,
  updateUserCollege,
  getAdminOverview,
  getCollegeStats,
  listColleges,
  createCollege,
  updateCollege,
  deleteCollege,
  initColleges
} from '@/api/admin'

const router = useRouter()
const userStore = useUserStore()

const fixAvatarUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return 'http://localhost:8080' + (url.startsWith('/') ? '' : '/') + url
}

// ──── 公共 ────
const collegeOptions = ['计算机学院', '机械学院', '电子信息学院', '经济管理学院', '外国语学院', '理学院', '人文社科学院', '自动化学院']
const roleMap = { 3: '教师', 4: '管理员' }
const roleTypeMap = { 3: 'success', 4: 'danger' }

const menuTitleMap = {
  '1': '邀请码管理',
  '2': '用户管理',
  '3': '全局统计',
  '4': '学院管理'
}

// ──── 菜单 ────
const currentMenu = ref('1')
const currentTitle = ref('邀请码管理')

const handleMenuSelect = async (index) => {
  currentMenu.value = index
  currentTitle.value = menuTitleMap[index] || '管理后台'
  await nextTick()
  if (index === '1') loadInviteCodes()
  else if (index === '2') loadUsers()
  else if (index === '3') { loadOverview(); loadCollegeStats() }
  else if (index === '4') loadColleges()
}

const handleRefresh = () => {
  handleMenuSelect(currentMenu.value)
  ElMessage.success('已刷新')
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定退出登录？', '退出确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    router.push('/login')
  } catch {
    // 取消
  }
}

// ──── 1. 邀请码管理 ────
const genForm = ref({ college: '', count: 5, expiresAt: '' })
const genLoading = ref(false)
const codeLoading = ref(false)
const inviteCodeList = ref([])
const codePage = ref(1)
const codePageSize = ref(10)
const codeTotal = ref(0)
const codeFilter = ref({ college: '', status: null })

const inviteStatusText = (status) => ({ 0: '未使用', 1: '已使用', 2: '已作废' }[status] || '未知')
const inviteStatusType = (status) => ({ 0: 'primary', 1: 'success', 2: 'info' }[status] || 'info')

const copyCode = async (code) => {
  try {
    await navigator.clipboard.writeText(code)
    ElMessage.success('已复制到剪贴板')
  } catch {
    const textarea = document.createElement('textarea')
    textarea.value = code
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('已复制到剪贴板')
  }
}

const copyInviteLink = async (code) => {
  const link = window.location.origin + '/register?inviteCode=' + code
  try {
    await navigator.clipboard.writeText(link)
    ElMessage.success('注册链接已复制到剪贴板')
  } catch {
    const textarea = document.createElement('textarea')
    textarea.value = link
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('注册链接已复制到剪贴板')
  }
}

const handleCodeFilter = () => {
  codePage.value = 1
  loadInviteCodes()
}

const resetCodeFilter = () => {
  codeFilter.value = { college: '', status: null }
  codePage.value = 1
  loadInviteCodes()
}

const loadInviteCodes = async () => {
  codeLoading.value = true
  try {
    const params = { page: codePage.value - 1, size: codePageSize.value }
    if (codeFilter.value.college) params.college = codeFilter.value.college
    if (codeFilter.value.status != null && codeFilter.value.status !== '') params.status = codeFilter.value.status
    const res = await listInviteCodes(params)
    if (res.code === 200) {
      inviteCodeList.value = res.data?.content || []
      codeTotal.value = res.data?.totalElements ?? inviteCodeList.value.length
    } else {
      ElMessage.error(res.message || '加载邀请码失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '加载邀请码异常')
  } finally {
    codeLoading.value = false
  }
}

const handleGenerate = async () => {
  if (!genForm.value.expiresAt) {
    ElMessage.warning('请选择过期时间')
    return
  }
  genLoading.value = true
  try {
    const payload = { count: genForm.value.count }
    if (genForm.value.college) payload.college = genForm.value.college
    if (genForm.value.expiresAt) payload.expiresAt = genForm.value.expiresAt

    const res = await generateInviteCodes(payload)
    if (res.code === 200) {
      ElMessage.success('邀请码生成成功')
      codePage.value = 1
      await loadInviteCodes()
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '生成邀请码异常')
  } finally {
    genLoading.value = false
  }
}

const handleRevokeCode = async (id) => {
  try {
    await ElMessageBox.confirm('确定作废该邀请码？作废后不可恢复。', '作废确认', {
      confirmButtonText: '确定作废',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await revokeInviteCode(id)
    if (res.code === 200) {
      ElMessage.success('已作废')
      await loadInviteCodes()
    } else {
      ElMessage.error(res.message || '作废失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '作废操作异常')
    }
  }
}

// ──── 2. 用户管理 ────
const userFilter = ref({ college: '', role: null, keyword: '' })
const userLoading = ref(false)
const userList = ref([])
const userPage = ref(1)
const userPageSize = ref(10)
const userTotal = ref(0)

const loadUsers = async () => {
  userLoading.value = true
  try {
    const params = {
      page: userPage.value - 1,
      size: userPageSize.value,
      roleIds: '3,4'
    }
    if (userFilter.value.college) params.college = userFilter.value.college
    if (userFilter.value.role != null && userFilter.value.role !== '') params.roleId = userFilter.value.role
    if (userFilter.value.keyword) params.keyword = userFilter.value.keyword

    const res = await listUsers(params)
    if (res.code === 200) {
      userList.value = res.data?.content || []
      userTotal.value = res.data?.totalElements ?? userList.value.length
    } else {
      ElMessage.error(res.message || '加载用户失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '加载用户异常')
  } finally {
    userLoading.value = false
  }
}

const handleRoleChange = async (userId, oldRole, newRole) => {
  const oldLabel = roleMap[oldRole] || '未知'
  const newLabel = roleMap[newRole] || '未知'
  try {
    await ElMessageBox.confirm(
      `确定将用户角色从「${oldLabel}」修改为「${newLabel}」？`,
      '角色修改确认',
      { confirmButtonText: '确定修改', cancelButtonText: '取消', type: 'warning' }
    )
    const res = await updateUserRole(userId, newRole)
    if (res.code === 200) {
      const user = userList.value.find(u => u.id === userId)
      if (user) user.roleId = newRole
      ElMessage.success('角色更新成功')
    } else {
      ElMessage.error(res.message || '角色更新失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '角色更新异常')
    }
  }
}

const handleStatusChange = async (userId, status) => {
  const action = status === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(
      `确定${action}该用户？`,
      '状态修改确认',
      { confirmButtonText: `确定${action}`, cancelButtonText: '取消', type: 'warning' }
    )
    const res = await updateUserStatus(userId, status)
    if (res.code === 200) {
      const user = userList.value.find(u => u.id === userId)
      if (user) user.status = status
      ElMessage.success(status === 1 ? '已启用' : '已禁用')
    } else {
      ElMessage.error(res.message || '状态更新失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '状态更新异常')
    }
  }
}

const handleCollegeChange = async (userId, college) => {
  try {
    const res = await updateUserCollege(userId, college)
    if (res.code === 200) {
      const user = userList.value.find(u => u.id === userId)
      if (user) user.college = college
      ElMessage.success('学院更新成功')
    } else {
      ElMessage.error(res.message || '学院更新失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '学院更新异常')
  }
}

// ──── 3. 全局统计 ────
const overview = ref({ totalUsers: 0, totalTeachers: 0, totalStudents: 0, totalExams: 0 })
const collegeStatsList = ref([])
const statsLoading = ref(false)
const barChartRef = ref(null)
const pieChartRef = ref(null)
let barChartInstance = null
let pieChartInstance = null

const renderBarChart = () => {
  if (!barChartRef.value || collegeStatsList.value.length === 0) return
  if (barChartInstance && !barChartInstance.isDisposed()) {
    barChartInstance.dispose()
  }
  barChartInstance = echarts.init(barChartRef.value)
  const names = collegeStatsList.value.map(c => c.college)
  const studentCounts = collegeStatsList.value.map(c => c.studentCount || 0)
  const teacherCounts = collegeStatsList.value.map(c => c.teacherCount || 0)
  barChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { bottom: '0', left: 'center' },
    grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
    xAxis: { type: 'category', data: names, axisLabel: { rotate: 30, fontSize: 11 } },
    yAxis: { type: 'value', name: '人数' },
    series: [
      {
        name: '教师数',
        type: 'bar',
        data: teacherCounts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#e6a23c' },
            { offset: 1, color: '#f3d19e' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        barMaxWidth: 30
      },
      {
        name: '学员数',
        type: 'bar',
        data: studentCounts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409eff' },
            { offset: 1, color: '#79bbff' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        barMaxWidth: 30
      }
    ]
  })
}

const renderPieChart = () => {
  if (!pieChartRef.value) return
  if (pieChartInstance && !pieChartInstance.isDisposed()) {
    pieChartInstance.dispose()
  }
  pieChartInstance = echarts.init(pieChartRef.value)
  const data = [
    { value: overview.value.totalTeachers || 0, name: '教师' },
    { value: overview.value.totalStudents || 0, name: '学员' }
  ]
  pieChartInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: '0', left: 'center' },
    series: [{
      name: '用户分布',
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%' },
      data,
      color: ['#409eff', '#67c23a']
    }]
  })
}

const loadOverview = async () => {
  try {
    const res = await getAdminOverview()
    if (res.code === 200) {
      overview.value = res.data || {}
      await nextTick()
      renderPieChart()
    } else {
      ElMessage.error(res.message || '加载概览失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '加载概览异常')
  }
}

const loadCollegeStats = async () => {
  statsLoading.value = true
  try {
    const res = await getCollegeStats()
    if (res.code === 200) {
      collegeStatsList.value = res.data || []
      await nextTick()
      renderBarChart()
    } else {
      ElMessage.error(res.message || '加载学院统计失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '加载学院统计异常')
  } finally {
    statsLoading.value = false
  }
}

// ──── 4. 学院管理 ────
const collegeList = ref([])
const collegeLoading = ref(false)
const collegeDialog = ref(false)
const collegeForm = ref({ name: '', sortOrder: 0, enabled: 1 })
const collegeInitLoading = ref(false)

const handleInitColleges = async () => {
  collegeInitLoading.value = true
  try {
    const res = await initColleges()
    if (res.code === 200) {
      ElMessage.success(`初始化完成：新增${res.data.inserted}个，已存在${res.data.existed}个`)
      loadColleges()
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '初始化失败')
  } finally {
    collegeInitLoading.value = false
  }
}

const loadColleges = async () => {
  collegeLoading.value = true
  try {
    const res = await listColleges()
    if (res.code === 200) {
      collegeList.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载学院列表失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '加载学院列表异常')
  } finally {
    collegeLoading.value = false
  }
}

const openCollegeAdd = () => {
  collegeForm.value = { name: '', sortOrder: 0, enabled: 1 }
  collegeDialog.value = true
}

const openCollegeEdit = (row) => {
  collegeForm.value = { id: row.id, name: row.name, sortOrder: row.sortOrder, enabled: row.enabled }
  collegeDialog.value = true
}

const handleSaveCollege = async () => {
  if (!collegeForm.value.name?.trim()) {
    ElMessage.warning('请输入学院名称')
    return
  }
  try {
    let res
    if (collegeForm.value.id) {
      res = await updateCollege(collegeForm.value.id, collegeForm.value)
    } else {
      res = await createCollege(collegeForm.value)
    }
    if (res.code === 200) {
      ElMessage.success(collegeForm.value.id ? '学院更新成功' : '学院创建成功')
      collegeDialog.value = false
      await loadColleges()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作异常')
  }
}

const handleDeleteCollege = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除学院「${row.name}」？删除后不可恢复。`, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteCollege(row.id)
    if (res.code === 200) {
      ElMessage.success('学院已删除')
      await loadColleges()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '删除异常')
    }
  }
}

// ──── 初始化 ────
const handleResize = () => {
  barChartInstance?.resize()
  pieChartInstance?.resize()
}

onMounted(() => {
  handleMenuSelect('1')
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  barChartInstance?.dispose()
  pieChartInstance?.dispose()
})
</script>

<style scoped>
.admin-container {
  display: flex;
  min-height: 100vh;
  background: #f0f2f5;
}

/* 侧边栏 */
.sidebar {
  width: 220px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.sidebar-header {
  padding: 20px 16px 12px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #e0e0e0;
  letter-spacing: 1px;
}

.user-info {
  padding: 20px 16px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.user-avatar {
  margin-bottom: 10px;
  background: rgba(255, 255, 255, 0.15);
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 4px;
}

.user-role {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.sidebar-menu {
  flex: 1;
  border-right: none !important;
  background: transparent !important;
  padding: 8px 0;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 46px;
  line-height: 46px;
  font-size: 14px;
  font-weight: 500;
  margin: 2px 8px;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.75);
  background: transparent;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.logout-btn-wrap {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.logout-btn {
  width: 100%;
  color: rgba(255, 255, 255, 0.6) !important;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.logout-btn:hover {
  color: #ff6b6b !important;
}

/* 主内容 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e8ecf1;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.page-content {
  max-width: 1200px;
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--app-primary, #409eff);
}

/* 表单卡片 */
.gen-card {
  margin-bottom: 20px;
  border-radius: 10px;
  border: 1px solid #e8ecf1;
}

.gen-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.gen-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

/* 表格 */
.data-table {
  border-radius: 10px;
  overflow: hidden;
}

.code-text {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  letter-spacing: 0.5px;
  color: var(--app-primary, #409eff);
  background: var(--app-primary-soft, #ecf5ff);
  padding: 2px 8px;
  border-radius: 4px;
}

.text-muted {
  color: #c0c4cc;
  font-size: 12px;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

/* 统计卡片 */
.overview-row {
  margin-bottom: 8px;
}

.stat-card {
  border-radius: 10px;
  border: 1px solid #e8ecf1;
  text-align: center;
  transition: box-shadow 0.2s;
}

.stat-card:hover {
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.12);
}

.stat-card :deep(.el-card__body) {
  padding: 28px 16px;
}

.stat-label {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--app-primary, #409eff);
  line-height: 1.2;
}

/* 图表卡片 */
.chart-card {
  border-radius: 10px;
  border: 1px solid #e8ecf1;
}

.chart-card :deep(.el-card__header) {
  padding: 12px 20px;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  border-bottom: 1px solid #f0f0f0;
}

.chart-container {
  width: 100%;
  height: 320px;
}

/* 响应式 */
@media (max-width: 768px) {
  .admin-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    flex-direction: row;
    flex-wrap: wrap;
  }

  .sidebar-header,
  .user-info {
    display: none;
  }

  .sidebar-menu {
    display: flex;
    overflow-x: auto;
    padding: 8px;
  }

  .sidebar-menu :deep(.el-menu-item) {
    white-space: nowrap;
  }

  .logout-btn-wrap {
    display: none;
  }

  .content-body {
    padding: 16px;
  }

  .overview-row :deep(.el-col) {
    flex: 0 0 50%;
    max-width: 50%;
    margin-bottom: 12px;
  }
}
</style>
