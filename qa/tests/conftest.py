import os
from pathlib import Path

import pytest
import requests


def project_root() -> Path:
    configured = os.getenv("MIAOMIAO_PROJECT_ROOT")
    if configured:
        return Path(configured).expanduser().resolve()
    return Path(__file__).resolve().parents[2]


def app_base_url() -> str:
    return os.getenv("APP_BASE_URL", "http://127.0.0.1:8080").rstrip("/")


def get_username() -> str:
    return os.getenv("MIAOMIAO_USERNAME", "123456")


def get_password() -> str:
    return os.getenv("MIAOMIAO_PASSWORD", "hys123")


def get_role() -> str:
    return os.getenv("MIAOMIAO_ROLE", "student")


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


@pytest.fixture(scope="session")
def auth_token() -> str:
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