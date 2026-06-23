import pytest
import requests
import json
import time
from typing import Dict, Any

# 测试配置
BASE_URL = "http://localhost:8080"
API_PREFIX = "/api"

# 测试用户配置（使用数据库中的真实用户）
TEST_USERS = {
    "student": {
        "username": "123456",
        "password": "hys123",
        "role": "STUDENT",
        "college": "计算机学院"
    },
    "teacher": {
        "username": "t123456",
        "password": "hys123",
        "role": "TEACHER",
        "college": "计算机学院"
    },
    "admin": {
        "username": "admin",
        "password": "admin123",
        "role": "ADMIN",
        "college": "计算机学院"
    }
}


@pytest.fixture(scope="session")
def base_url():
    """基础 URL"""
    return BASE_URL


@pytest.fixture(scope="session")
def api_url():
    """API 前缀 URL"""
    return f"{BASE_URL}{API_PREFIX}"


@pytest.fixture(scope="session")
def test_student():
    """测试学生用户"""
    return TEST_USERS["student"]


@pytest.fixture(scope="session")
def test_teacher():
    """测试教师用户"""
    return TEST_USERS["teacher"]


@pytest.fixture(scope="session")
def test_admin():
    """测试管理员用户"""
    return TEST_USERS["admin"]


@pytest.fixture(scope="session")
def student_token(api_url, test_student):
    """学生用户 Token（登录获取）"""
    url = f"{api_url}/auth/login"
    data = {
        "username": test_student["username"],
        "password": test_student["password"],
        "role": test_student["role"]
    }
    response = requests.post(url, json=data)
    result = response.json()
    if result.get("code") == 200:
        return result.get("data", {}).get("token")
    return None


@pytest.fixture(scope="session")
def teacher_token(api_url, test_teacher):
    """教师用户 Token（登录获取）"""
    url = f"{api_url}/auth/login"
    data = {
        "username": test_teacher["username"],
        "password": test_teacher["password"],
        "role": test_teacher["role"]
    }
    response = requests.post(url, json=data)
    result = response.json()
    if result.get("code") == 200:
        return result.get("data", {}).get("token")
    return None


@pytest.fixture(scope="session")
def admin_token(api_url, test_admin):
    """管理员用户 Token（登录获取）"""
    url = f"{api_url}/auth/login"
    data = {
        "username": test_admin["username"],
        "password": test_admin["password"],
        "role": test_admin["role"]
    }
    response = requests.post(url, json=data)
    result = response.json()
    if result.get("code") == 200:
        return result.get("data", {}).get("token")
    return None


@pytest.fixture(scope="session")
def student_headers(student_token):
    """学生用户请求头"""
    if not student_token:
        pytest.skip("学生用户登录失败，跳过测试")
    return {
        "Content-Type": "application/json",
        "X-Username": TEST_USERS["student"]["username"],
        "Authorization": f"Bearer {student_token}"
    }


@pytest.fixture(scope="session")
def teacher_headers(teacher_token):
    """教师用户请求头"""
    if not teacher_token:
        pytest.skip("教师用户登录失败，跳过测试")
    return {
        "Content-Type": "application/json",
        "X-Username": TEST_USERS["teacher"]["username"],
        "Authorization": f"Bearer {teacher_token}"
    }


@pytest.fixture(scope="session")
def admin_headers(admin_token):
    """管理员用户请求头"""
    if not admin_token:
        pytest.skip("管理员用户登录失败，跳过测试")
    return {
        "Content-Type": "application/json",
        "X-Username": TEST_USERS["admin"]["username"],
        "Authorization": f"Bearer {admin_token}"
    }


@pytest.fixture(scope="function")
def cleanup_test_data():
    """清理测试数据（在测试后执行）"""
    created_ids = []
    yield created_ids
    # 这里可以添加清理逻辑
    # 例如：删除创建的测试数据
    pass


def assert_success_response(response: requests.Response, expected_code: int = 200):
    """断言成功响应"""
    assert response.status_code == 200
    result = response.json()
    assert result.get("code") == expected_code
    return result


def assert_error_response(response: requests.Response, expected_code: int = 400):
    """断言错误响应"""
    result = response.json()
    assert result.get("code") == expected_code
    return result
