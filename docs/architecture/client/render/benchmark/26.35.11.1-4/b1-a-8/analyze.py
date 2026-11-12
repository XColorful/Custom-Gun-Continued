# -*- coding: utf-8 -*-
"""解析 b1-a-8：换弹的脏 cube 口径（只有 F12 采集，无 Afterburner）。

数据（n=3）：`cgc1 (N).txt`，每个文件 3 段 = ak47 / m4a1 裸配 / m4a1 满配 各跑一次自动换弹。
口径：均值 ± 1 样本标准差；换弹段整段 10 秒，脏 cube 的峰值看 stat 行里的 max / p99。
"""
import os
import re
import statistics
import sys

sys.stdout.reconfigure(encoding="utf-8")

BASE = os.path.dirname(os.path.abspath(__file__))
STEPS = ["ak47 换弹", "m4a1 裸配 换弹", "m4a1 满配 换弹"]
RUNS = range(1, 4)
STAT = re.compile(r"(-?[\d.]+) / frame \(min\s+(-?[\d.]+) / max\s+(-?[\d.]+) / p99\s+(-?[\d.]+)")

FIELDS = ["Average frame time", "CGC render per frame", "Geometry traverse", "Cubes submitted",
          "Distinct cubes", "Dirty cubes", "Dirty node share", "Dirty subtree share", "Dirty cube share"]


def read_block(path, index):
    with open(path, encoding="utf-8", errors="replace") as f:
        text = f.read()
    return text.split("customgun render profile completed")[1:][index]


def field(block, label):
    for line in block.splitlines():
        if len(line) >= 41 and line[:38].strip() == label:
            value = line[41:].strip()
            if value.startswith("-"):
                return None
            m = re.match(r"(-?[\d.]+)", value)
            return float(m.group(1)) if m else None
    return None


def stat_line(block, label):
    for line in block.splitlines():
        if len(line) >= 41 and line[:38].strip() == label:
            m = STAT.search(line[41:])
            if m:
                return tuple(float(x) for x in m.groups())
    return None


def mean_sd(values):
    values = [v for v in values if v is not None]
    if not values:
        return "-"
    if len(values) == 1:
        return f"{values[0]:.2f}"
    return f"{statistics.mean(values):.2f} ± {statistics.stdev(values):.2f}"


if __name__ == "__main__":
    runs = [[read_block(os.path.join(BASE, f"cgc1 ({i}).txt"), s) for s in range(len(STEPS))] for i in RUNS]
    print(f"{'指标':<22}" + "".join(f"{s:>20}" for s in STEPS))
    for f in FIELDS:
        cells = [mean_sd([field(runs[r][s], f) for r in range(len(runs))]) for s in range(len(STEPS))]
        print(f"{f:<22}" + "".join(f"{c:>20}" for c in cells))

    print("\n脏 cube 峰值 / p99（按帧）")
    for s, step in enumerate(STEPS):
        vals = [stat_line(runs[r][s], "Dirty cubes (per frame)") for r in range(len(runs))]
        vals = [v for v in vals if v]
        avg = statistics.mean(v[0] for v in vals)
        mx = statistics.mean(v[2] for v in vals)
        p99 = statistics.mean(v[3] for v in vals)
        distinct = statistics.mean(field(runs[r][s], "Distinct cubes") for r in range(len(runs)))
        print(f"  {step:<20} avg {avg:7.2f}  max {mx:7.1f}  p99 {p99:7.1f}"
              f"  → p99 占比 {p99 * 100.0 / distinct:5.1f}%  峰值占比 {mx * 100.0 / distinct:5.1f}%")
