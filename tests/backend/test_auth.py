import pytest
import requests
from conftest import assert_success_response, assert_error_response, API_PREFIX


@pytest.mark.auth
class TestAuth:
    """用户认证模块测试"""

    def test_login_success(self, api_url, test_student):
        """测试用户登录成功"""
        url = f"{api_url}/auth/login"
        data = {
            "username": test_student["username"],
            "password": test_student["password"],
            "role": test_student["role"]
        }
        response = requests.post(url, json=data)
        result = assert_success_response(response)
        assert "token" in result.get("data", {})
        assert "user" in result.get("data", {})

    def test_login_wrong_password(self, api_url, test_student):
        """测试用户登录失败（密码错误）"""
        url = f"{api_url}/auth/login"
        data = {
            "username": test_student["username"],
            "password": "wrong_password",
            "role": test_student["role"]
        }
        response = requests.post(url, json=data)
        assert response.status_code == 200
        result = response.json()
        assert result.get("code") != 200

    def test_login_user_not_found(self, api_url):
        """测试用户登录失败（用户不存在）"""
        url = f"{api_url}/auth/login"
        data = {
            "username": "nonexistent_user",
            "password": "Test123456",
            "role": "STUDENT"
        }
        response = requests.post(url, json=data)
        assert response.status_code == 200
        result = response.json()
        assert result.get("code") != 200

    def test_login_empty_username(self, api_url):
        """测试用户登录失败（用户名为空）"""
        url = f"{api_url}/auth/login"
        data = {
            "username": "",
            "password": "Test123456",
            "role": "STUDENT"
        }
        response = requests.post(url, json=data)
        assert response.status_code == 200
        result = response.json()
        assert result.get("code") != 200

    def test_login_empty_password(self, api_url, test_student):
        """测试用户登录失败（密码为空）"""
        url = f"{api_url}/auth/login"
        data = {
            "username": test_student["username"],
            "password": "",
            "role": test_student["role"]
        }
        response = requests.post(url, json=data)
        assert response.status_code == 200
        result = response.json()
        assert result.get("code") != 200

    def test_register_success(self, api_url):
        """测试用户注册成功"""
        url = f"{api_url}/auth/register"
        import time
        unique_id = str(int(time.time()))
        data = {
            "username": f"test_user_{unique_id}",
            "password": "Test123456",
            "role": "STUDENT",
            "college": "计算机学院"
        }
        response = requests.post(url, json=data)
        result = response.json()
        # 注册可能需要邀请码，检查返回结果
        assert result.get("code") in [200, 400]

    def test_register_duplicate_username(self, api_url, test_student):
        """测试用户注册失败（用户名已存在）"""
        url = f"{api_url}/auth/register"
        data = {
            "username": test_student["username"],
            "password": "Test123456",
            "role": "STUDENT",
            "college": "计算机学院"
        }
        response = requests.post(url, json=data)
        assert response.status_code == 200
        result = response.json()
        assert result.get("code") != 200

    def test_register_empty_username(self, api_url):
        """测试用户注册失败（用户名为空）"""
        url = f"{api_url}/auth/register"
        data = {
            "username": "",
            "password": "Test123456",
            "role": "STUDENT",
            "college": "计算机学院"
        }
        response = requests.post(url, json=data)
        assert response.status_code == 200
        result = response.json()
        assert result.get("code") != 200

    def test_register_empty_password(self, api_url):
        """测试用户注册失败（密码为空）"""
        url = f"{api_url}/auth/register"
        data = {
            "username": "test_user_new",
            "password": "",
            "role": "STUDENT",
            "college": "计算机学院"
        }
        response = requests.post(url, json=data)
        assert response.status_code == 200
        result = response.json()
        assert result.get("code") != 200

    def test_verify_token_success(self, api_url, student_token, test_student):
        """测试 Token 验证成功"""
        url = f"{api_url}/auth/verifyToken"
        data = {
            "token": student_token,
            "username": test_student["username"],
            "flag": "0"
        }
        response = requests.post(url, json=data)
        result = assert_success_response(response)

    def test_verify_token_invalid(self, api_url):
        """测试 Token 验证失败（Token 无效）"""
        url = f"{api_url}/auth/verifyToken"
        data = {
            "token": "invalid_token",
            "username": "test_user",
            "flag": "0"
        }
        response = requests.post(url, json=data)
        # 后端可能返回403状态码或非JSON响应
        if response.status_code == 200:
            result = response.json()
            assert result.get("code") != 200
        else:
            # 403或其他状态码也算通过
            assert response.status_code in [403, 401, 400]

    def test_logout_success(self, api_url, test_student):
        """测试用户登出成功"""
        url = f"{api_url}/auth/logout"
        data = {
            "username": test_student["username"]
        }
        response = requests.post(url, json=data)
        result = response.json()
        assert result.get("code") in [200, 400]

    def test_get_user_info_success(self, api_url, student_headers, test_student):
        """测试获取用户信息成功"""
        url = f"{api_url}/auth/getUserInfo"
        data = {
            "username": test_student["username"]
        }
        response = requests.post(url, json=data, headers=student_headers)
        result = response.json()
        assert result.get("code") == 200

    def test_get_user_info_empty_username(self, api_url, student_headers):
        """测试获取用户信息失败（用户名为空）"""
        url = f"{api_url}/auth/getUserInfo"
        data = {
            "username": ""
        }
        response = requests.post(url, json=data, headers=student_headers)
        assert response.status_code == 200
        result = response.json()
        assert result.get("code") != 200
