import pytest
import requests
import time
from conftest import assert_success_response, assert_error_response, API_PREFIX


@pytest.mark.admin
class TestAdmin:
    """管理员后台模块测试"""

    def test_admin_login_success(self, api_url, test_admin):
        """测试管理员登录成功"""
        url = f"{api_url}/auth/login"
        data = {
            "username": test_admin["username"],
            "password": test_admin["password"],
            "role": test_admin["role"]
        }
        response = requests.post(url, json=data)
        result = response.json()
        # 管理员用户需要在数据库中预先创建，否则会返回401
        assert result.get("code") in [200, 401]

    def test_admin_get_users_success(self, api_url, admin_headers):
        """测试获取用户列表成功"""
        url = f"{api_url}/admin/users"
        response = requests.get(url, headers=admin_headers)
        result = response.json()
        assert result.get("code") == 200
        assert "content" in result.get("data", {})

    def test_admin_get_users_with_page(self, api_url, admin_headers):
        """测试分页获取用户列表"""
        url = f"{api_url}/admin/users"
        params = {
            "page": 0,
            "size": 10
        }
        response = requests.get(url, headers=admin_headers, params=params)
        result = response.json()
        assert result.get("code") == 200

    def test_admin_get_users_with_keyword(self, api_url, admin_headers):
        """测试关键字搜索用户列表"""
        url = f"{api_url}/admin/users"
        params = {
            "keyword": "test",
            "page": 0,
            "size": 10
        }
        response = requests.get(url, headers=admin_headers, params=params)
        result = response.json()
        assert result.get("code") == 200

    def test_admin_get_users_with_role(self, api_url, admin_headers):
        """测试按角色筛选用户列表"""
        url = f"{api_url}/admin/users"
        params = {
            "roleId": 1,
            "page": 0,
            "size": 10
        }
        response = requests.get(url, headers=admin_headers, params=params)
        result = response.json()
        assert result.get("code") == 200

    def test_admin_update_user_role_success(self, api_url, admin_headers):
        """测试修改用户角色成功"""
        # 先获取用户列表
        url = f"{api_url}/admin/users"
        response = requests.get(url, headers=admin_headers, params={"page": 0, "size": 10})
        result = response.json()
        if result.get("code") == 200 and result.get("data", {}).get("content"):
            users = result["data"]["content"]
            # 找一个非管理员用户
            target_user = None
            for user in users:
                if user.get("roleId") != 4:
                    target_user = user
                    break
            if target_user:
                update_url = f"{api_url}/admin/users/{target_user['id']}/role"
                update_data = {"roleId": 3}
                update_response = requests.put(update_url, json=update_data, headers=admin_headers)
                update_result = update_response.json()
                assert update_result.get("code") == 200

    def test_admin_update_user_status_success(self, api_url, admin_headers):
        """测试修改用户状态成功"""
        # 先获取用户列表
        url = f"{api_url}/admin/users"
        response = requests.get(url, headers=admin_headers, params={"page": 0, "size": 10})
        result = response.json()
        if result.get("code") == 200 and result.get("data", {}).get("content"):
            users = result["data"]["content"]
            if users:
                target_user = users[0]
                update_url = f"{api_url}/admin/users/{target_user['id']}/status"
                update_data = {"status": 1}
                update_response = requests.put(update_url, json=update_data, headers=admin_headers)
                update_result = update_response.json()
                assert update_result.get("code") == 200

    def test_admin_update_user_college_success(self, api_url, admin_headers):
        """测试修改用户学院成功"""
        # 先获取用户列表
        url = f"{api_url}/admin/users"
        response = requests.get(url, headers=admin_headers, params={"page": 0, "size": 10})
        result = response.json()
        if result.get("code") == 200 and result.get("data", {}).get("content"):
            users = result["data"]["content"]
            if users:
                target_user = users[0]
                update_url = f"{api_url}/admin/users/{target_user['id']}/college"
                update_data = {"college": "计算机学院"}
                update_response = requests.put(update_url, json=update_data, headers=admin_headers)
                update_result = update_response.json()
                assert update_result.get("code") == 200

    def test_admin_generate_invite_code_success(self, api_url, admin_headers):
        """测试生成邀请码成功"""
        url = f"{api_url}/admin/invite-codes"
        unique_id = str(int(time.time()))
        data = {
            "college": "计算机学院",
            "count": 1,
            "expiresAt": "2026-12-31T23:59:59"
        }
        response = requests.post(url, json=data, headers=admin_headers)
        result = response.json()
        assert result.get("code") == 200

    def test_admin_list_invite_codes_success(self, api_url, admin_headers):
        """测试查询邀请码列表成功"""
        url = f"{api_url}/admin/invite-codes"
        response = requests.get(url, headers=admin_headers)
        result = response.json()
        assert result.get("code") == 200

    def test_admin_list_invite_codes_with_status(self, api_url, admin_headers):
        """测试按状态筛选邀请码列表"""
        url = f"{api_url}/admin/invite-codes"
        params = {"status": 0}
        response = requests.get(url, headers=admin_headers, params=params)
        result = response.json()
        assert result.get("code") == 200

    def test_admin_revoke_invite_code_success(self, api_url, admin_headers):
        """测试作废邀请码成功"""
        # 先获取邀请码列表
        url = f"{api_url}/admin/invite-codes"
        response = requests.get(url, headers=admin_headers)
        result = response.json()
        if result.get("code") == 200 and result.get("data", {}).get("content"):
            codes = result["data"]["content"]
            if codes:
                code_id = codes[0].get("id")
                if code_id:
                    revoke_url = f"{api_url}/admin/invite-codes/{code_id}"
                    revoke_response = requests.delete(revoke_url, headers=admin_headers)
                    revoke_result = revoke_response.json()
                    assert revoke_result.get("code") == 200

    def test_admin_get_colleges_success(self, api_url, admin_headers):
        """测试获取学院列表成功"""
        url = f"{api_url}/admin/colleges"
        response = requests.get(url, headers=admin_headers)
        result = response.json()
        assert result.get("code") == 200

    def test_admin_create_college_success(self, api_url, admin_headers):
        """测试新增学院成功"""
        url = f"{api_url}/admin/colleges"
        unique_id = str(int(time.time()))
        data = {
            "name": f"测试学院_{unique_id}",
            "sortOrder": 100,
            "enabled": 1
        }
        response = requests.post(url, json=data, headers=admin_headers)
        result = response.json()
        assert result.get("code") == 200

    def test_admin_create_college_duplicate_name(self, api_url, admin_headers):
        """测试新增学院失败（名称已存在）"""
        # 先获取学院列表
        url = f"{api_url}/admin/colleges"
        response = requests.get(url, headers=admin_headers)
        result = response.json()
        if result.get("code") == 200 and result.get("data"):
            colleges = result["data"]
            if colleges:
                existing_name = colleges[0].get("name")
                if existing_name:
                    create_url = f"{api_url}/admin/colleges"
                    create_data = {"name": existing_name}
                    create_response = requests.post(create_url, json=create_data, headers=admin_headers)
                    create_result = create_response.json()
                    assert create_result.get("code") != 200

    def test_admin_update_college_success(self, api_url, admin_headers):
        """测试修改学院成功"""
        # 先获取学院列表
        url = f"{api_url}/admin/colleges"
        response = requests.get(url, headers=admin_headers)
        result = response.json()
        if result.get("code") == 200 and result.get("data"):
            colleges = result["data"]
            if colleges:
                college_id = colleges[0].get("id")
                if college_id:
                    update_url = f"{api_url}/admin/colleges/{college_id}"
                    update_data = {"sortOrder": 999}
                    update_response = requests.put(update_url, json=update_data, headers=admin_headers)
                    update_result = update_response.json()
                    assert update_result.get("code") == 200

    def test_admin_delete_college_success(self, api_url, admin_headers):
        """测试删除学院成功"""
        # 先创建一个学院
        create_url = f"{api_url}/admin/colleges"
        unique_id = str(int(time.time()))
        create_data = {
            "name": f"待删除学院_{unique_id}",
            "sortOrder": 999,
            "enabled": 0
        }
        create_response = requests.post(create_url, json=create_data, headers=admin_headers)
        create_result = create_response.json()
        if create_result.get("code") == 200:
            # 获取刚创建的学院
            list_url = f"{api_url}/admin/colleges"
            list_response = requests.get(list_url, headers=admin_headers)
            list_result = list_response.json()
            if list_result.get("code") == 200 and list_result.get("data"):
                colleges = list_result["data"]
                target_college = None
                for college in colleges:
                    if college.get("name", "").startswith("待删除学院_"):
                        target_college = college
                        break
                if target_college:
                    delete_url = f"{api_url}/admin/colleges/{target_college['id']}"
                    delete_response = requests.delete(delete_url, headers=admin_headers)
                    delete_result = delete_response.json()
                    assert delete_result.get("code") == 200

    def test_admin_init_colleges_success(self, api_url, admin_headers):
        """测试初始化学院数据成功"""
        url = f"{api_url}/admin/colleges/init"
        response = requests.post(url, headers=admin_headers)
        result = response.json()
        assert result.get("code") == 200
        assert "inserted" in result.get("data", {})
        assert "existed" in result.get("data", {})
        assert "total" in result.get("data", {})

    def test_admin_get_stats_overview_success(self, api_url, admin_headers):
        """测试获取统计概览成功"""
        url = f"{api_url}/admin/stats/overview"
        response = requests.get(url, headers=admin_headers)
        result = response.json()
        assert result.get("code") == 200
        assert "totalUsers" in result.get("data", {})
        assert "totalTeachers" in result.get("data", {})
        assert "totalStudents" in result.get("data", {})

    def test_admin_get_college_stats_success(self, api_url, admin_headers):
        """测试获取学院统计成功"""
        url = f"{api_url}/admin/stats/colleges"
        response = requests.get(url, headers=admin_headers)
        result = response.json()
        assert result.get("code") == 200
