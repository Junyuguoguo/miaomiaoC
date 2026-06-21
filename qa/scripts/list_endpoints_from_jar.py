#!/usr/bin/env python3
"""List Spring MVC controller mappings from the packaged backend jar.

Usage:
  python qa/scripts/list_endpoints_from_jar.py backend/backend-0.0.1-SNAPSHOT.jar
"""
from __future__ import annotations

import re
import shutil
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path


def main() -> int:
    if len(sys.argv) != 2:
        print("Usage: python qa/scripts/list_endpoints_from_jar.py <backend-jar>", file=sys.stderr)
        return 2
    jar = Path(sys.argv[1])
    if not jar.is_file():
        print(f"Jar not found: {jar}", file=sys.stderr)
        return 2
    if not shutil.which("javap"):
        print("javap not found. Install/use a JDK, not only a JRE.", file=sys.stderr)
        return 2

    with tempfile.TemporaryDirectory() as tmp:
        tmp_path = Path(tmp)
        with zipfile.ZipFile(jar) as zf:
            for name in zf.namelist():
                if name.startswith("BOOT-INF/classes/") and name.endswith("Controller.class"):
                    zf.extract(name, tmp_path)
        root = tmp_path / "BOOT-INF" / "classes"
        classes = sorted(root.glob("**/*Controller.class"))
        for cls_file in classes:
            cls = str(cls_file.relative_to(root).with_suffix("")).replace("/", ".")
            out = subprocess.check_output(["javap", "-classpath", str(root), "-p", "-v", cls], text=True, errors="replace")
            base_match = re.search(r"RequestMapping\(\s*\n\s*value=\[\"([^\"]*)\"\]", out)
            base = base_match.group(1) if base_match else ""
            for chunk in re.split(r"\n  public ", out)[1:]:
                signature = "public " + chunk.split("\n", 1)[0]
                method_name_match = re.search(r"\s(\w+)\(", signature)
                method_name = method_name_match.group(1) if method_name_match else "?"
                for match in re.finditer(
                    r"org\.springframework\.web\.bind\.annotation\.(Get|Post|Put|Delete|Patch)Mapping\(\s*\n\s*value=\[\"([^\"]*)\"\]",
                    chunk,
                ):
                    verb = match.group(1).upper()
                    path = (base.rstrip("/") + "/" + match.group(2).lstrip("/")).replace("//", "/")
                    print(f"{verb:6} {path:42} -> {method_name}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
