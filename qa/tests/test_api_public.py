import pytest
import requests

from conftest import app_base_url


@pytest.mark.api
def test_vip_plans_endpoint_responds_json():
    try:
        response = requests.get(app_base_url() + "/api/vip/plans", timeout=8)
    except requests.exceptions.RequestException as exc:
        pytest.skip(f"Server is not reachable at {app_base_url()}: {exc}")
    assert response.status_code < 500
    try:
        body = response.json()
    except ValueError:
        pytest.fail(f"Expected JSON, got status={response.status_code}, body={response.text[:300]!r}")
    assert "code" in body, body
