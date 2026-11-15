# -*- coding: utf-8 -*-
"""解析 b1-a-5（B1-b 前置探针批次）的数据。

数据来源（1.20.1 / Forge，`StaticBake.ENABLED = true`、`FORCE_REBUILD = false`）：
- `cgc1 (N).txt`               Afterburner，第一人称 4 步：空手 / ak47 裸配 / m4a1 裸配 / m4a1 满配
- `cgc1-builtin (N).txt`       F12 内置探针，同样 4 步，与 Afterburner 交替运行（不重叠）
- `cgc1-multi (N).txt`         Afterburner，multi 3 步：9 个 m4a1 满配展示框 / +第一人称 ak47 / +第一人称 m4a1 满配
- `cgc-multi-builtin (N).txt`  F12 内置探针，同样 3 步

口径：帧时间 = 1000 / FPS；均值 ± 1 样本标准差（n=4）。
Afterburner 与 F12 分两次运行，F12 采集期间的帧时间不参与统计。
"""
import os
import re
import statistics
import sys

sys.stdout.reconfigure(encoding="utf-8")

BASE = os.path.dirname(os.path.abspath(__file__))

FP_STEPS = ["空手", "ak47 裸配", "m4a1 裸配", "m4a1 满配"]
MULTI_STEPS = ["9 满配展示框", "+第一人称 m4a1 裸配", "+第一人称 m4a1 满配"]

AF_AVG = re.compile(r"Average framerate\s*:\s*([\d.]+) FPS")
AF_LOW = re.compile(r"1% low framerate\s*:\s*([\d.]+) FPS")


def read(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        return f.read()


def af_runs(pattern, steps, n=4):
    rows = [[] for _ in steps]
    for i in range(1, n + 1):
        text = read(os.path.join(BASE, pattern.format(n=i)))
        avg = [float(x) for x in AF_AVG.findall(text)]
        low = [float(x) for x in AF_LOW.findall(text)]
        assert len(avg) >= len(steps), (pattern, i, len(avg))
        for s in range(len(steps)):
            rows[s].append((avg[s], low[s]))
    return rows


def builtin_blocks(pattern, n=4):
    blocks = []
    for i in range(1, n + 1):
        text = read(os.path.join(BASE, pattern.format(n=i)))
        parts = text.split("customgun render profile completed")
        blocks.append(parts[1:])
    return blocks


def field(block, label):
    for line in block.splitlines():
        if len(line) >= 41 and line[:38].strip() == label:
            value = line[41:].strip()
            if value.startswith("-"):
                return None
            m = re.match(r"(-?[\d.]+)", value)
            return float(m.group(1)) if m else None
    return None


def stat(values):
    values = [v for v in values if v is not None]
    if not values:
        return None
    if len(values) == 1:
        return f"{values[0]:.3f}"
    return f"{statistics.mean(values):.3f} ± {statistics.stdev(values):.3f}"


def report_af(title, rows, steps):
    print(f"\n== {title}（Afterburner，n=4）")
    print(f"{'步骤':<20}{'FPS':>16}{'帧时间 ms':>16}{'1% low':>12}")
    for step, row in zip(steps, rows):
        fps = [r[0] for r in row]
        low = [r[1] for r in row]
        ms = [1000.0 / f for f in fps]
        print(f"{step:<20}{statistics.mean(fps):>10.1f} ± {statistics.stdev(fps):<4.1f}"
              f"{statistics.mean(ms):>10.3f} ± {statistics.stdev(ms):<5.3f}{statistics.mean(low):>10.1f}")
    base = statistics.mean([1000.0 / r[0] for r in rows[0]])
    print("相对空手的增量:", end="")
    for step, row in zip(steps[1:], rows[1:]):
        ms = statistics.mean([1000.0 / r[0] for r in row])
        print(f"  {step} +{ms - base:.3f} ms", end="")
    print()


def report_builtin(title, blocks_per_run, steps):
    print(f"\n== {title}（F12 内置探针，n=4）")
    fields = ["Average frame time", "GPU frame busy", "CGC GPU busy", "CGC render per frame",
              "First-person submit", "World-item submit", "State update", "Data refresh",
              "Scope render", "Geometry traverse", "endBatch flush", "Delegate render",
              "Functional nodes", "Stencil clear", "endBatch upload", "endBatch draw"]
    print(f"{'指标':<22}" + "".join(f"{s:>18}" for s in steps))
    for f in fields:
        cells = []
        for step in range(len(steps)):
            per_run = [field(blocks_per_run[i][step], f) for i in range(len(blocks_per_run))]
            cells.append(stat(per_run) or "-")
        print(f"{f:<22}" + "".join(f"{c:>18}" for c in cells))

    counters = ["First-person submissions", "endBatch calls", "Cubes submitted", "Draw calls",
                "Upload bytes", "Allocated bytes", "Animation channels", "State changes",
                "Bake rebuilds", "Distinct RenderTypes", "Distinct nodes", "Dirty nodes"]
    print()
    print(f"{'计数':<22}" + "".join(f"{s:>18}" for s in steps))
    for c in counters:
        cells = []
        for step in range(len(steps)):
            per_run = [field(blocks_per_run[i][step], c) for i in range(len(blocks_per_run))]
            cells.append(stat(per_run) or "-")
        print(f"{c:<22}" + "".join(f"{c2:>18}" for c2 in cells))


if __name__ == "__main__":
    report_af("第一人称", af_runs("cgc1 ({n}).txt", FP_STEPS), FP_STEPS)
    report_af("multi", af_runs("cgc1-multi ({n}).txt", MULTI_STEPS), MULTI_STEPS)
    report_builtin("第一人称", builtin_blocks("cgc1-builtin ({n}).txt"), FP_STEPS)
    report_builtin("multi", builtin_blocks("cgc-multi-builtin ({n}).txt"), MULTI_STEPS)
