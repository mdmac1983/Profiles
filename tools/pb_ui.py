#!/usr/bin/env python3
"""pb_ui.py - what is in the stock power menu? Prints the text and bounds of every node with a text in a `uiautomator dump` file (emulator section pt phase D, BB1).

    python3 -I tools/pb_ui.py <dump.xml>

The emulator's stock menu has a second white card under the panel (a 4th entry) that Tj's tablet does not show; this names it. Never fails: unreadable input prints a note.
"""
import re
import sys


def main(argv):
    try:
        s = open(argv[0], errors="ignore").read()
    except Exception as e:
        print("(no dump: %s)" % str(e)[:60])
        return 0
    seen = []
    for m in re.finditer(r'<node[^>]*?text="([^"]+)"[^>]*?bounds="(\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\])"', s):
        item = "%s %s" % (m.group(1)[:24], m.group(2))
        if item not in seen:
            seen.append(item)
    print(" | ".join(seen[:14]) if seen else "(no texted nodes)")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
