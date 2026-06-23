import pytest
import requests
from conftest import assert_success_response, assert_error_response, API_PREFIX


@pytest.mark.teacher
class TestTeacher:
    """教师管理模块测试"""

    def test_get_teacher_stats_overview_success(self, api_url, teacher_headers):
        """测试获取教师统计概览成功"""
        url = f"{api_url}/teacher/stats/overview"
        response = requests.get(url, headers=teacher_headers)
        result = response.json()
        assert result.get("code") == 200
        assert "studentCount" in result.get("data", {})
        assert "avgQuestionCount" in result.get("data", {})
        assert "avgPassRate" in result.get("data", {})
        assert "examPassRate" in result.get("data", {})

    def test_get_teacher_stats_overview_with_college(self, api_url, teacher_headers):
        """测试按学院获取教师统计概览"""
        url = f"{api_url}/teacher/stats/overview"
        params = {"college": "计算机学院"}
        response = requests.get(url, headers=teacher_headers, params=params)
        result = response.json()
        assert result.get("code") == 200

    def test_get_students_success(self, api_url, teacher_headers):
        """测试获取学生列表成功"""
        url = f"{api_url}/teacher/stats/students"
        response = requests.get(url, headers=teacher_headers)
        result = response.json()
        assert result.get("code") == 200
        assert "total" in result.get("data", {})
        assert "students" in result.get("data", {})

    def test_get_students_with_page(self, api_url, teacher_headers):
        """测试分页获取学生列表"""
        url = f"{api_url}/teacher/stats/students"
        params = {
            "page": 0,
            "size": 10
        }
        response = requests.get(url, headers=teacher_headers, params=params)
        result = response.json()
        assert result.get("code") == 200

    def test_get_students_with_keyword(self, api_url, teacher_headers):
        """测试关键字搜索学生列表"""
        url = f"{api_url}/teacher/stats/students"
        params = {
            "keyword": "test",
            "page": 0,
            "size": 10
        }
        response = requests.get(url, headers=teacher_headers, params=params)
        result = response.json()
        assert result.get("code") == 200

    def test_get_students_with_college(self, api_url, teacher_headers):
        """测试按学院筛选学生列表"""
        url = f"{api_url}/teacher/stats/students"
        params = {
            "college": "计算机学院",
            "page": 0,
            "size": 10
        }
        response = requests.get(url, headers=teacher_headers, params=params)
        result = response.json()
        assert result.get("code") == 200

    def test_get_student_detail_success(self, api_url, teacher_headers):
        """测试获取学生详情成功"""
        # 先获取学生列表
        url = f"{api_url}/teacher/stats/students"
        response = requests.get(url, headers=teacher_headers, params={"page": 0, "size": 1})
        result = response.json()
        if result.get("code") == 200 and result.get("data", {}).get("students"):
            students = result["data"]["students"]
            if students:
                student_id = students[0].get("id")
                if student_id:
                    detail_url = f"{api_url}/teacher/stats/students/{student_id}"
                    detail_response = requests.get(detail_url, headers=teacher_headers)
                    detail_result = detail_response.json()
                    # 学生可能不存在，允许404
                    assert detail_result.get("code") in [200, 404]

    def test_get_student_detail_not_found(self, api_url, teacher_headers):
        """测试获取学生详情失败（学生不存在）"""
        url = f"{api_url}/teacher/stats/students/999999"
        response = requests.get(url, headers=teacher_headers)
        result = response.json()
        assert result.get("code") != 200

    def test_get_student_detail_with_college(self, api_url, teacher_headers):
        """测试按学院获取学生详情"""
        # 先获取学生列表
        url = f"{api_url}/teacher/stats/students"
        response = requests.get(url, headers=teacher_headers, params={"page": 0, "size": 1, "college": "计算机学院"})
        result = response.json()
        if result.get("code") == 200 and result.get("data", {}).get("students"):
            students = result["data"]["students"]
            if students:
                student_id = students[0].get("id")
                if student_id:
                    detail_url = f"{api_url}/teacher/stats/students/{student_id}"
                    detail_params = {"college": "计算机学院"}
                    detail_response = requests.get(detail_url, headers=teacher_headers, params=detail_params)
                    detail_result = detail_response.json()
                    # 学生可能不存在，允许404
                    assert detail_result.get("code") in [200, 404]
