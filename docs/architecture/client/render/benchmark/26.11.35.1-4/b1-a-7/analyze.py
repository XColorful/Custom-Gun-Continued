# -*- coding: utf-8 -*-
"""解析 b1-a-7：换弹场景（第一人称，DebugKey 自动跑 3 次换弹，10 秒后自动停止）。

数据（n=4，每个文件 = 一次独立启动）：
- `cgc1 (N).txt`           Afterburner，4 步：空手 / ak47 / m4a1 裸配 / m4a1 满配（均为静止）
- `cgc1-builtin (N).txt`   F12 内置探针，3 段：上述三把枪各跑一次换弹序列

口径：帧时间 = 1000 / FPS；均值 ± 1 样本标准差。
换弹段的计数器均值会被静止帧稀释，脏节点看「Dirty nodes (per frame)」里的 max / p99。
"""
import os
import re
import statistics
import sys

sys.stdout.reconfigure(encoding="utf-8")

BASE = os.path.dirname(os.path.abspath(__file__))
AF_STEPS = ["空手", "ak47 静止", "m4a1 裸配 静止", "m4a1 满配 静止"]
RELOAD_STEPS = ["ak47 换弹", "m4a1 裸配 换弹", "m4a1 满配 换弹"]

AF_AVG = re.compile(r"Average framerate\s*:\s*([\d.]+) FPS")
AF_LOW = re.compile(r"1% low framerate\s*:\s*([\d.]+) FPS")
STAT = re.compile(r"(-?[\d.]+) / frame \(min\s+(-?[\d.]+) / max\s+(-?[\d.]+) / p99\s+(-?[\d.]+)")


def read(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        return f.read()


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
    """读取 `<label> : avg / frame (min .. / max .. / p99 ..)` 四个数"""
    for line in block.splitlines():
        if len(line) >= 41 and line[:38].strip() == label:
            m = STAT.search(line[41:])
            if m:
                return tuple(float(x) for x in m.groups())
    return None


def stat(values):
    values = [v for v in values if v is not None]
    if not values:
        return "-"
    if len(values) == 1:
        return f"{values[0]:.3f}"
    return f"{statistics.mean(values):.3f} ± {statistics.stdev(values):.3f}"


if __name__ == "__main__":
    START = 5  # 1–4 为脏子树探针修复前的构建，5–8 为修复后
    RUNS = range(START, START + 4)

    print("== Afterburner（帧时间，n=4）")
    for step in range(len(AF_STEPS)):
        fps, low, ms = [], [], []
        for i in RUNS:
            text = read(os.path.join(BASE, f"cgc1 ({i}).txt"))
            fps.append(float(AF_AVG.findall(text)[step]))
            low.append(float(AF_LOW.findall(text)[step]))
            ms.append(1000.0 / fps[-1])
        print(f"{AF_STEPS[step]:<20}{statistics.mean(fps):>8.1f} FPS{statistics.mean(ms):>9.3f} ms"
              f"  1% low {statistics.mean(low):>6.1f}")

    runs = []
    for i in RUNS:
        text = read(os.path.join(BASE, f"cgc1-builtin ({i}).txt"))
        runs.append(text.split("customgun render profile completed")[1:])

    print("\n== F12 换弹段（n=4）")
    fields = ["Average frame time", "CGC render per frame", "First-person submit", "State update",
              "Data refresh", "Geometry traverse", "endBatch flush", "Delegate render",
              "Scope render", "endBatch upload", "endBatch draw"]
    print(f"{'指标':<24}" + "".join(f"{s:>20}" for s in RELOAD_STEPS))
    for f in fields:
        cells = [stat([field(runs[r][s], f) for r in range(4)]) for s in range(len(RELOAD_STEPS))]
        print(f"{f:<24}" + "".join(f"{c:>20}" for c in cells))

    print()
    counters = ["Cubes submitted", "Animation channels", "State changes", "Distinct nodes",
                "Dirty nodes", "Dirty subtree nodes", "endBatch calls", "Draw calls",
                "Upload bytes", "Allocated bytes"]
    print(f"{'计数':<24}" + "".join(f"{s:>20}" for s in RELOAD_STEPS))
    for c in counters:
        cells = [stat([field(runs[r][s], c) for r in range(4)]) for s in range(len(RELOAD_STEPS))]
        print(f"{c:<24}" + "".join(f"{c2:>20}" for c2 in cells))

    print("\n== 脏节点按帧（avg / min / max / p99）")
    for label in ["Dirty nodes (per frame)", "Dirty subtree nodes (per frame)"]:
        print(label)
        for s, step in enumerate(RELOAD_STEPS):
            vals = [stat_line(runs[r][s], label) for r in range(4)]
            vals = [v for v in vals if v]
            if not vals:
                print(f"  {step:<20} -")
                continue
            avg = [v[0] for v in vals]
            mn = [v[1] for v in vals]
            mx = [v[2] for v in vals]
            p99 = [v[3] for v in vals]
            print(f"  {step:<20} avg {statistics.mean(avg):6.2f}  min {statistics.mean(mn):6.2f}"
                  f"  max {statistics.mean(mx):7.2f}  p99 {statistics.mean(p99):6.2f}")

    print("\n== share（均值）")
    for label in ["Dirty node share", "Dirty subtree share"]:
        cells = [stat([field(runs[r][s], label) for r in range(4)]) for s in range(len(RELOAD_STEPS))]
        print(f"{label:<24}" + "".join(f"{c:>20}" for c in cells))
