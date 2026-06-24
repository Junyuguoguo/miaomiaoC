# 学院初始化接口设计文档

## 问题背景

学院管理页面（菜单4）只显示数据库 `interview_college` 表中的数据，目前只有1个学院（计算机学院）。而前端多个下拉框使用硬编码的8个学院列表，两者不同步。

**目标**：创建一个初始化接口，调用时将13个学院插入到数据库中，使学院管理页面能够显示所有学院。

## 设计决策

### 方案对比

| 方案 | 描述 | 优点 | 缺点 |
|------|------|------|------|
| **方案1：SQL脚本插入** | 创建SQL脚本，将13个学院插入数据库 | 简单直接、可控性强 | 需要手动执行 |
| **方案2：创建初始化接口（选定）** | 创建API接口，调用时自动插入 | 可通过前端触发 | 增加额外接口 |
| 方案3：启动时自动初始化 | 应用启动时自动检查并插入 | 自动化 | 可能影响启动速度 |

**选择方案2**：创建初始化接口，因为可以通过前端触发，且不需要修改启动逻辑。

## 接口设计

### 接口信息

```
POST /api/admin/colleges/init
```

### 请求参数

无请求参数。

### 响应格式

```json
{
  "code": 200,
  "message": "初始化完成",
  "data": {
    "inserted": 12,
    "existed": 1,
    "total": 13
  }
}
```

### 错误响应

```json
{
  "code": 403,
  "message": "权限不足，仅管理员可操作"
}
```

## 数据来源

13个学院列表：

| 序号 | 学院名称 | 来源 |
|------|----------|------|
| 1 | 信息与通信工程学院 | 数据库初始 |
| 2 | 计算机学院 | 数据库初始 |
| 3 | 自动化学院 | 数据库初始 |
| 4 | 机械工程学院 | 数据库初始 |
| 5 | 经济管理学院 | 数据库初始 |
| 6 | 信息管理学院 | 数据库初始 |
| 7 | 马克思主义学院 | 数据库初始 |
| 8 | 外国语学院 | 数据库初始 |
| 9 | 理学院 | 数据库初始 |
| 10 | 公共管理与传媒学院 | 数据库初始 |
| 11 | 国际交流学院 | 数据库初始 |
| 12 | 电子信息学院 | 硬编码 |
| 13 | 人文社科学院 | 硬编码 |

## 实现逻辑

### 后端实现

1. **Controller层**：在 `AdminController` 中添加初始化接口
2. **Service层**：在 `InterviewCollegeRepository` 中添加批量插入方法
3. **权限校验**：只有管理员可以调用此接口

### 核心逻辑

```java
@PostMapping("/colleges/init")
public ResponseEntity<?> initColleges() {
    // 1. 权限校验
    if (!isAdmin()) {
        return ResponseEntity.status(403).body("权限不足");
    }
    
    // 2. 定义13个学院
    List<String> collegeNames = Arrays.asList(
        "信息与通信工程学院", "计算机学院", "自动化学院", 
        "机械工程学院", "经济管理学院", "信息管理学院",
        "马克思主义学院", "外国语学院", "理学院",
        "公共管理与传媒学院", "国际交流学院", 
        "电子信息学院", "人文社科学院"
    );
    
    // 3. 批量插入（使用INSERT IGNORE避免重复）
    int inserted = 0;
    int existed = 0;
    
    for (String name : collegeNames) {
        if (interviewCollegeRepository.findByName(name) == null) {
            InterviewCollege college = new InterviewCollege();
            college.setName(name);
            college.setSortOrder(inserted + 1);
            college.setEnabled(1);
            interviewCollegeRepository.save(college);
            inserted++;
        } else {
            existed++;
        }
    }
    
    // 4. 返回结果
    return ResponseEntity.ok(Map.of(
        "inserted", inserted,
        "existed", existed,
        "total", collegeNames.size()
    ));
}
```

## 测试用例

1. **正常初始化**：数据库为空时，调用接口应插入13个学院
2. **重复初始化**：再次调用接口，应返回已存在13个学院
3. **权限校验**：非管理员调用接口，应返回403错误
4. **数据验证**：初始化后，学院管理页面应显示13个学院

## 部署注意事项

1. 此接口为一次性初始化接口，建议在生产环境部署后调用一次
2. 接口具有幂等性，多次调用不会产生重复数据
3. 建议在调用后验证数据完整性

## 相关文件

- 后端Controller：`backend/src/main/java/sen/yuhuang/backend/controller/admin/AdminController.java`
- 后端Repository：`backend/src/main/java/sen/yuhuang/backend/repository/InterviewCollegeRepository.java`
- 后端实体：`backend/src/main/java/sen/yuhuang/backend/entity/InterviewCollege.java`
- 前端API：`frontend/src/api/admin.js`
- 前端页面：`frontend/src/views/admin/AdminPage.vue`
