"""Summarize completed Android instrumentation results; a missing finish is never a pass."""
import json
import re
import sys
from pathlib import Path

results = {}
incomplete = []
strict = "--check" in sys.argv
for filename in (arg for arg in sys.argv[1:] if arg != "--check"):
    path = Path(filename)
    record = {}
    text = path.read_text(errors="replace")
    for line in text.splitlines():
        match = re.match(r"INSTRUMENTATION_STATUS: ([^=]+)=(.*)", line)
        if match:
            record[match[1]] = match[2]
        elif line.startswith("INSTRUMENTATION_STATUS_CODE:"):
            code = int(line.split(":", 1)[1])
            if code != 1 and "class" in record and "test" in record:
                key = record["class"] + "#" + record["test"]
                results[key] = {
                    "result": "PASS" if code == 0 else "FAIL" if code < 0 else str(code),
                    "evidence": path.name,
                    **({"reason": record["stack"]} if "stack" in record else {}),
                }
            record = {}
    if "INSTRUMENTATION_CODE: -1" not in text:
        incomplete.append(path.name)
summary = {
    "passed": sum(r["result"] == "PASS" for r in results.values()),
    "failed": sum(r["result"] != "PASS" for r in results.values()),
    "incomplete_logs": incomplete,
    "tests": results,
}
print(json.dumps(summary, indent=2))
if strict and (summary["failed"] or incomplete or not summary["passed"]):
    sys.exit(1)
