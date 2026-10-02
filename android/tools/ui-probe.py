"""Dedicated emulator QA helper. Tap only bounds from a fresh native UI dump."""
import argparse
import re
import subprocess
import time
import xml.etree.ElementTree as ET
from pathlib import Path


def adb(*args):
    return subprocess.check_output(["adb", *args], timeout=45)


def snapshot(path):
    for attempt in range(4):
        raw = adb("exec-out", "uiautomator", "dump", "/dev/tty").decode(errors="replace")
        if "<?xml" in raw and "</hierarchy>" in raw:
            xml = raw[raw.index("<?xml"):raw.index("</hierarchy>") + len("</hierarchy>")]
            Path(path).write_text(xml)
            return ET.fromstring(xml)
        Path(path + ".diagnostic.txt").write_text(raw)
        time.sleep(1)
    raise RuntimeError("Native UI dump did not return a complete hierarchy")


def scroll(root):
    nodes = [n for n in root.iter("node") if n.get("scrollable") == "true"
             and "Horizontal" not in n.get("class", "")]
    if not nodes:
        raise RuntimeError("No vertical native scroll container in fresh dump")
    x1, y1, x2, y2 = bounds(nodes[0])
    x = (x1 + x2) // 2
    margin = max(35, (y2 - y1) // 5)
    adb("shell", "input", "swipe", str(x), str(y2-margin), str(x), str(y1+margin), "600")
    time.sleep(1)


def matches(root, label):
    described = [n for n in root.iter("node") if label == n.get("content-desc")]
    return described or [n for n in root.iter("node") if label in (n.get("text"), n.get("resource-id"))]


def bounds(node):
    return tuple(map(int, re.findall(r"\d+", node.get("bounds", ""))))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("action", choices=["snapshot", "tap", "input", "replace", "scroll"])
    parser.add_argument("path")
    parser.add_argument("label", nargs="?")
    parser.add_argument("value", nargs="?")
    args = parser.parse_args()
    root = snapshot(args.path)
    if args.action == "snapshot":
        for node in root.iter("node"):
            text = node.get("text") or node.get("content-desc")
            if text or node.get("scrollable") == "true":
                print({k: node.get(k) for k in ["text", "content-desc", "resource-id", "class", "enabled", "scrollable", "bounds"]})
        return
    if args.action == "scroll":
        scroll(root)
        return
    nodes = matches(root, args.label)
    for attempt in range(6):
        if nodes:
            break
        scroll(root)
        root = snapshot(args.path)
        nodes = matches(root, args.label)
    if not nodes:
        raise RuntimeError(f"Target absent in fresh dump: {args.label}")
    if len(nodes) != 1:
        raise RuntimeError(f"Ambiguous target ({len(nodes)}): {args.label}")
    node = nodes[0]
    if node.get("enabled") == "false":
        raise RuntimeError(f"Disabled target: {args.label}")
    x1, y1, x2, y2 = bounds(node)
    if x2 <= x1 or y2 <= y1:
        raise RuntimeError(f"Target has no visible bounds: {args.label}")
    adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
    if args.action in ("input", "replace"):
        if args.value is None:
            raise RuntimeError("Input needs a value")
        if args.action == "replace":
            adb("shell", "input", "keyevent", "123", *("67" for _ in range(100)))
        adb("shell", "input", "text", args.value.replace(" ", "%s"))


if __name__ == "__main__":
    main()
