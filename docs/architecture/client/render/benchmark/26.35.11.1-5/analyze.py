# -*- coding: utf-8 -*-
"""解析 26.35.11.1-5（B1-b 脏子树分块烘焙阶段验收批次）的数据。

数据来源（1.20.1 / Forge）：
- `cgc1 (N).txt`          Afterburner，4 步：空手 / 第一人称 ak47 / m4a1 裸配 / m4a1 满配
- `cgc1-builtin (N).txt`  F12 内置探针，3 段：ak47 换弹 / m4a1 裸配换弹 / m4a1 满配换弹
                          （每段 10 s，0/3/6 秒各触发一次换弹，末尾为空闲）

口径：帧时间 = 1000 / FPS；均值为 4 次独立启动的样本均值 ± 1 样本标准差。
Afterburner 与 F12 分开，F12 的帧时间含探针开销，只用于同口径比较。
"""
import os
import re
import statistics
import sys

BASE = os.path.dirname(os.path.abspath(__file__))
sys.stdout.reconfigure(encoding="utf-8")

AF_REC = re.compile(
    r"(\d{2}-\d{2}-\d{4}, \d{2}:\d{2}:\d{2}) (\S+) benchmark completed, (\d+) frames rendered in ([\d.]+) s\s*\n"
    r"\s*Average framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Minimum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Maximum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*1% low framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*0\.1% low framerate\s*:\s*([\d.]+) FPS")

AF_STEPS = ["空手", "ak47", "m4a1 裸配", "m4a1 满配"]
F12_STEPS = ["ak47 换弹", "m4a1 裸配换弹", "m4a1 满配换弹"]

# 关心的指标（F12 报告里的行名）
F12_METRICS = [
    "Average frame time", "CGC GPU busy", "CGC render per frame",
    "First-person submit", "Model render total", "Geometry traverse",
    "Scope render", "endBatch flush", "Delegate render", "State update",
    "Data refresh", "Functional nodes", "endBatch upload", "endBatch draw",
    "Static bake", "Cubes submitted", "Draw calls", "endBatch calls",
    "Upload bytes", "Allocated bytes", "Animation channels", "State changes",
    "Bake rebuilds", "Distinct RenderTypes", "Distinct nodes", "Dirty nodes",
    "Dirty subtree nodes", "Distinct cubes", "Dirty cubes",
]


def parse_af(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        text = f.read()
    out = []
    for m in AF_REC.finditer(text):
        ts, proc, frames, secs, avg, mn, mx, low1, low01 = m.groups()
        if proc.lower() != "javaw.exe":
            continue
        out.append({"ts": ts, "fps": float(avg), "min": float(mn), "max": float(mx),
                    "low1": float(low1), "frames": int(frames), "seconds": float(secs)})
    return out


def parse_f12(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        lines = f.read().splitlines()
    blocks, cur = [], None
    for line in lines:
        if "render profile completed" in line:
            if cur is not None:
                blocks.append(cur)
            cur = {}
            continue
        if cur is None:
            continue
        m = re.match(r"\s*(.+?)\s*:\s*(-\d+\.\d+|[\d.]+|-)\s*(ms|/ frame)?", line)
        if m:
            name, value = m.group(1).strip(), m.group(2)
            if name in F12_METRICS and name not in cur:
                cur[name] = None if value == "-" else float(value)
    if cur is not None:
        blocks.append(cur)
    return blocks


def stats(values):
    if not values:
        return None
    sd = statistics.stdev(values) if len(values) > 1 else 0.0
    return statistics.mean(values), sd, len(values)


def files(pattern):
    names = [f for f in os.listdir(BASE) if re.fullmatch(pattern, f)]
    return sorted(names, key=lambda f: int(re.search(r"\((\d+)\)", f).group(1)))


def main():
    print("## Afterburner：第一人称帧时间")
    runs = []
    for name in files(r"cgc1 \(\d+\)\.txt"):
        runs.append(parse_af(os.path.join(BASE, name)))
    buckets = [[] for _ in AF_STEPS]
    for recs in runs:
        for i, rec in enumerate(recs):
            if i < len(buckets):
                buckets[i].append(rec)
    for step, recs in zip(AF_STEPS, buckets):
        ft = [1000.0 / r["fps"] for r in recs]
        m, sd, n = stats(ft)
        print(f"  {step:<12} {m:6.3f} ± {sd:5.3f} ms  ({statistics.mean(r['fps'] for r in recs):7.1f} FPS)"
              f"  1% low {statistics.mean(r['low1'] for r in recs):6.1f}  n={n}")

    # 同次运行内相减：抵消进程级空手基线噪声
    print("\n## Afterburner：CGC 增量（同次运行 步骤 − 空手）")
    for k, step in enumerate(AF_STEPS[1:], 1):
        deltas = [1000.0 / recs[k]["fps"] - 1000.0 / recs[0]["fps"] for recs in runs if len(recs) > k]
        m, sd, n = stats(deltas)
        print(f"  {step:<12} {m:6.3f} ± {sd:5.3f} ms  n={n}")

    print("\n## F12：换弹段（10 s，含空闲）")
    blocks = [[] for _ in F12_STEPS]
    for name in files(r"cgc1-builtin \(\d+\)\.txt"):
        for i, block in enumerate(parse_f12(os.path.join(BASE, name))):
            if i < len(blocks):
                blocks[i].append(block)
    for metric in F12_METRICS:
        row = []
        for recs in blocks:
            vals = [r[metric] for r in recs if r.get(metric) is not None]
            if not vals:
                row.append(f"{'-':>18}")
            else:
                m, sd, n = stats(vals)
                row.append(f"{m:9.3f} ±{sd:6.3f}")
        print(f"  {metric:<24} " + " | ".join(row))


if __name__ == "__main__":
    main()
