import re

import pytest

from conftest import app_base_url, get_password, get_username


pytestmark = pytest.mark.ui


def test_login_page_student_flow(page):
    page.goto(app_base_url() + "/login")
    page.get_by_placeholder(re.compile("账号")).fill(get_username())
    page.get_by_placeholder(re.compile("密码")).fill(get_password())
    page.get_by_role("button", name=re.compile("登录")).click()
    page.wait_for_url(re.compile(r"/(exam|teacher)(/.*)?$"), timeout=10000)