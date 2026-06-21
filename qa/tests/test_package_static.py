from pathlib import Path
import re

import pytest

from conftest import project_root


@pytest.mark.static
def test_deploy_package_has_expected_parts():
    root = project_root()
    assert (root / "frontend-dist" / "index.html").is_file()
    assert (root / "backend" / "backend-0.0.1-SNAPSHOT.jar").is_file()
    assert (root / "backend" / "application.yml").is_file()
    assert (root / "database" / "miaomiaoc.sql").is_file()
    assert (root / "deploy" / "nginx" / "miaomiaoC2.conf").is_file()


@pytest.mark.static
def test_index_referenced_assets_exist():
    root = project_root()
    index = (root / "frontend-dist" / "index.html").read_text(encoding="utf-8")
    asset_paths = re.findall(r'(?:src|href)="/([^"]+\.(?:js|css|ico))"', index)
    assert asset_paths, "index.html should reference built JS/CSS/favicon assets"
    missing = [p for p in asset_paths if not (root / "frontend-dist" / p).exists()]
    assert not missing, f"Missing built assets: {missing}"


@pytest.mark.static
def test_nginx_proxies_api_to_backend():
    root = project_root()
    conf = (root / "deploy" / "nginx" / "miaomiaoC2.conf").read_text(encoding="utf-8")
    assert "location /api/" in conf
    assert "proxy_pass http://127.0.0.1:8080" in conf
    assert "try_files $uri $uri/ /index.html" in conf


@pytest.mark.static
def test_backend_config_uses_externalized_env_values():
    root = project_root()
    yml = (root / "backend" / "application.yml").read_text(encoding="utf-8")
    for token in ["${SERVER_PORT", "${DB_URL", "${DB_USERNAME", "${DB_PASSWORD", "${REDIS_HOST"]:
        assert token in yml
