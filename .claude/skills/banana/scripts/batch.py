#!/usr/bin/env python3
"""Banana Claude -- Batch Generation from CSV

Process batch image generation requests from a CSV file.
Validates, estimates cost, and outputs parsed rows for generation.

CSV format:
  prompt,ratio,resolution,model,preset
  "coffee shop hero image",16:9,2K,,
  "product shot of headphones",1:1,1K,,

Usage:
    batch.py --csv FILE
"""

import argparse
import csv
import json
import sys
from pathlib import Path

DEFAULT_MODEL = "gemini-3.1-flash-image-preview"
DEFAULT_RESOLUTION = "1K"
DEFAULT_RATIO = "1:1"

PRICING = {
    "gemini-3.1-flash-image-preview": {"512": 0.020, "1K": 0.039, "2K": 0.078, "4K": 0.156},
    "gemini-2.5-flash-image": {"512": 0.020, "1K": 0.039},
}

VALID_RATIOS = {"1:1", "16:9", "9:16", "4:3", "3:4", "2:3", "3:2",
                "4:5", "5:4", "1:4", "4:1", "1:8", "8:1", "21:9"}
VALID_RESOLUTIONS = {"512", "1K", "2K", "4K"}


def _lookup_cost(model, resolution):
    model_pricing = PRICING.get(model, PRICING[DEFAULT_MODEL])
    return model_pricing.get(resolution, model_pricing.get("1K", 0.039))


def main():
    parser = argparse.ArgumentParser(description="Banana Claude Batch CSV Processor")
    parser.add_argument("--csv", required=True, help="Path to CSV file with generation requests")
    args = parser.parse_args()

    csv_path = Path(args.csv).expanduser().resolve()
    if not csv_path.exists():
        print(json.dumps({"error": True, "message": f"CSV file not found: {csv_path}"}))
        sys.exit(1)

    rows = []
    errors = []
    total_cost = 0.0

    try:
        with open(csv_path, newline="", encoding="utf-8") as f:
            reader = csv.DictReader(f)

            if not reader.fieldnames or "prompt" not in [h.strip().lower() for h in reader.fieldnames]:
                print(json.dumps({"error": True, "message": "CSV must have a 'prompt' column header"}))
                sys.exit(1)

            for i, row in enumerate(reader, start=2):
                prompt = row.get("prompt", "").strip()
                if not prompt:
                    errors.append({"row": i, "error": "Empty prompt"})
                    continue

                ratio = (row.get("ratio", "") or "").strip() or DEFAULT_RATIO
                resolution = (row.get("resolution", "") or "").strip() or DEFAULT_RESOLUTION
                model = (row.get("model", "") or "").strip() or DEFAULT_MODEL
                preset = (row.get("preset", "") or "").strip()

                row_warnings = []
                if ratio not in VALID_RATIOS:
                    row_warnings.append(f"Invalid ratio '{ratio}', using default '{DEFAULT_RATIO}'")
                    ratio = DEFAULT_RATIO
                if resolution not in VALID_RESOLUTIONS:
                    row_warnings.append(f"Invalid resolution '{resolution}', using default '{DEFAULT_RESOLUTION}'")
                    resolution = DEFAULT_RESOLUTION

                cost = _lookup_cost(model, resolution)
                total_cost += cost

                entry = {
                    "row": i,
                    "prompt": prompt,
                    "ratio": ratio,
                    "resolution": resolution,
                    "model": model,
                    "preset": preset,
                    "estimated_cost": cost,
                }
                if row_warnings:
                    entry["warnings"] = row_warnings

                rows.append(entry)

    except (IOError, csv.Error) as e:
        print(json.dumps({"error": True, "message": str(e)}))
        sys.exit(1)

    print(json.dumps({
        "rows": rows,
        "total_count": len(rows),
        "estimated_total_cost": round(total_cost, 3),
        "validation_errors": errors,
    }, indent=2))


if __name__ == "__main__":
    main()
