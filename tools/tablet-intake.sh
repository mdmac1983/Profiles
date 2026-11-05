#!/usr/bin/env bash
# tablet-intake.sh — save what Tj sends from the tablet INTO THE PROJECT and write research/tablet/<date>/FACTS.md (+ LATEST.md).
#   bash tools/tablet-intake.sh <tablet-report.txt | folder | zip>      # a file he attached, the whole launcher-probe folder, or a zip of it
#   bash tools/tablet-intake.sh -  < pasted-report.txt                  # the report text itself (he pasted it into the chat: write it to a file or pipe it)
#   options: --label NAME  --date YYYY-MM-DD  --out DIR  --keep-images (only when Tj agrees: a screenshot cannot be redacted)
# What it does: reads the input safely (never extracts a zip to disk, ignores unsafe names, caps sizes), keeps only small TEXT files, redacts serial numbers / IMEI-like numbers / MAC + IP addresses /
# e-mail addresses / Wi-Fi names, REFUSES the whole intake if anything shaped like a live credential is left, then lists red flags first. Afterwards: read research/tablet/LATEST.md, then
#   git add research/tablet && bash tools/ckpt.sh "tablet intake" "what to do about the red flags"
# The input is untrusted data, so Python runs isolated (-I): a file next to it can never be imported.
set -euo pipefail
D="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec python3 -I "$D/tablet_intake.py" "$@"
