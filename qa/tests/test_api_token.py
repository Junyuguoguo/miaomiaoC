import pytest
import requests

from conftest import app_base_url, get_password, get_role, get_username


def post_json(path: str, payload: dict, token: str | None = None) -> requests.Response:
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"

    try:
        return requests.post(
            app_base_url() + path,
            json=payload,
            headers=headers,
            timeout=8,
        )
    except requests.exceptions.RequestException as exc:
        pytest.skip(f"Server is not reachable at {app_base_url()}: {exc}")


def as_json(response: requests.Response) -> dict:
    try:
        return response.json()
    except ValueError as exc:
        pytest.fail(
            f"Response is not JSON. status={response.status_code}, "
            f"body={response.text[:300]!r}, error={exc}"
        )


def login_and_get_token() -> str:
    response = post_json("/api/auth/login", {
        "username": get_username(),
        "password": get_password(),
        "role": get_role(),
    })

    assert response.status_code < 500
    body = as_json(response)

    assert body.get("code") == 200, body

    token = body.get("data", {}).get("token")
    assert token, body

    return token


@pytest.mark.api
def test_login_can_get_token():
    token = login_and_get_token()

    assert isinstance(token, str)
    assert len(token) > 10