import pytest

from conftest import as_json, get_password, get_role, get_username, post_json


@pytest.mark.api
def test_login_success_returns_token_and_user():
    response = post_json("/api/auth/login", {
        "username": get_username(),
        "password": get_password(),
        "role": get_role(),
    })

    assert response.status_code < 500

    body = as_json(response)

    assert body.get("code") == 200, body
    assert body.get("data", {}).get("token"), body
    assert body.get("data", {}).get("user", {}).get("username") == get_username(), body


@pytest.mark.api
def test_login_wrong_password_is_rejected():
    response = post_json("/api/auth/login", {
        "username": get_username(),
        "password": "__wrong_password__",
        "role": get_role(),
    })

    assert response.status_code < 500

    body = as_json(response)

    assert body.get("code") != 200, body


@pytest.mark.api
def test_login_missing_username_is_rejected():
    response = post_json("/api/auth/login", {
        "username": "",
        "password": get_password(),
        "role": get_role(),
    })

    assert response.status_code < 500

    body = as_json(response)

    assert body.get("code") != 200, body