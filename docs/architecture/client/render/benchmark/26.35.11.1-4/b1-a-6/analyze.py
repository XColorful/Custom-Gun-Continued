# -*- coding: utf-8 -*-
"""解析 b1-a-6：multi 空展示框场景基线（9 个石头 + 9 个空展示框，无枪）。

数据：
- `cgc1-multi (N).txt`           Afterburner；1–4 为加探针前的构建，5–8 为加探针后的构建
- `cgc1-multi-builtin (N).txt`   F12 内置探针，同上

口径：帧时间 = 1000 / FPS；均值 ± 1 样本标准差（n=4）。AF 与 F12 分开运行。
"""
import os
import re
import statistics
import sys

sys.stdout.reconfigure(encoding="utf-8")

BASE = os.path.dirname(os.path.abspath(__file__))
AF_AVG = re.compile(r"Average framerate\s*:\s*([\d.]+) FPS")
AF_LOW = re.compile(r"1% low framerate\s*:\s*([\d.]+) FPS")


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


def blocks(pattern, start):
    out = []
    for i in range(start, start + 4):
        text = read(os.path.join(BASE, pattern.format(n=i)))
        out.append(text.split("customgun render profile completed")[1:])
    return out


def af(pattern, start):
    fps, low = [], []
    for i in range(start, start + 4):
        text = read(os.path.join(BASE, pattern.format(n=i)))
        fps.append(float(AF_AVG.search(text).group(1)))
        low.append(float(AF_LOW.search(text).group(1)))
    return fps, low


def show(title, values, unit):
    print(f"{title:<28}{statistics.mean(values):>10.3f} ± {statistics.stdev(values):<6.3f} {unit}")


if __name__ == "__main__":
    print("== 加探针后（5–8）")
    fps, low = af("cgc1-multi ({n}).txt", 5)
    ms = [1000.0 / f for f in fps]
    show("AF 帧时间", ms, "ms")
    print(f"{'AF 1% low':<28}{statistics.mean(low):>10.1f} FPS")
    b = blocks("cgc1-multi-builtin ({n}).txt", 5)
    for label in ["Average frame time", "GPU frame busy", "endBatch upload", "endBatch draw",
                  "Draw calls", "Upload bytes", "Allocated bytes"]:
        vals = [field(blk[0], label) for blk in b]
        vals = [v for v in vals if v is not None]
        print(f"{label:<28}{statistics.mean(vals):>10.3f} ± {statistics.stdev(vals):<6.3f}")

    print("\n== 加探针前（1–4，对照）")
    fps0, low0 = af("cgc1-multi ({n}).txt", 1)
    show("AF 帧时间", [1000.0 / f for f in fps0], "ms")
    b0 = blocks("cgc1-multi-builtin ({n}).txt", 1)
    vals = [field(blk[0], "Average frame time") for blk in b0]
    show("F12 Average frame time", vals, "ms")
