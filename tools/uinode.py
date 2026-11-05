#!/usr/bin/env python3
"""uinode.py - find a node in a `uiautomator dump` file; print its bounds as "x1 y1 x2 y2" (or the value of one attribute). Prints nothing when there is no match.
Used by tools/emulator_scenario.sh (the phone-guide checks). Pure standard library; run it with -I.

  python3 -I tools/uinode.py <dump.xml> --contains TEXT [--exact] [--row-of TEXT2] [--attr NAME] [--nth N]

  --contains TEXT   nodes whose text contains TEXT (with --exact: whose text IS TEXT)
  --row-of TEXT2    among those, the one whose vertical centre lies inside the first node whose text contains TEXT2 (a "Done" box in the same row as a card title)
  --attr NAME       print that attribute (for example checked) instead of the bounds
  --nth N           the N-th match (default 1)
"""
import re
import sys
import xml.etree.ElementTree as ET


def bounds(node):
    m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.get("bounds", ""))
    return tuple(int(x) for x in m.groups()) if m else None


def main(argv):
    if len(argv) < 2:
        return 2
    path, args = argv[0], argv[1:]
    opt = {"contains": None, "exact": False, "row_of": None, "attr": None, "nth": 1}
    i = 0
    while i < len(args):
        a = args[i]
        if a == "--exact":
            opt["exact"] = True
        elif a in ("--contains", "--row-of", "--attr", "--nth") and i + 1 < len(args):
            i += 1
            opt[a[2:].replace("-", "_")] = int(args[i]) if a == "--nth" else args[i]
        else:
            return 2
        i += 1
    try:
        text = open(path, encoding="utf-8", errors="replace").read()
        start = text.find("<?xml")
        if start < 0:
            start = max(text.find("<hierarchy"), 0)
        end = text.rfind("</hierarchy>")                 # uiautomator may print a status line after the document
        root = ET.fromstring(text[start:end + len("</hierarchy>")] if end >= 0 else text[start:])
    except Exception:
        return 1
    nodes = [n for n in root.iter("node") if bounds(n)]
    want = opt["contains"] or ""
    hits = [n for n in nodes if (n.get("text", "") == want if opt["exact"] else want in n.get("text", ""))]
    if opt["row_of"]:
        anchor = next((n for n in nodes if opt["row_of"] in n.get("text", "")), None)
        if anchor is None:
            return 0
        y1, y2 = bounds(anchor)[1], bounds(anchor)[3]
        hits = [n for n in hits if y1 <= (bounds(n)[1] + bounds(n)[3]) // 2 <= y2]
    if len(hits) < opt["nth"]:
        return 0
    n = hits[opt["nth"] - 1]
    print(n.get(opt["attr"], "") if opt["attr"] else " ".join(str(v) for v in bounds(n)))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
