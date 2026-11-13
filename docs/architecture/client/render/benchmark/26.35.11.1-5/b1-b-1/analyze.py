# -*- coding: utf-8 -*-
"""解析 b1-b-1（换弹前后的待机帧时间）的数据。

数据来源：
- `cgc1 (N).txt`          Afterburner，5 步：空手 / ak47 换弹前 / ak47 换弹后 /
                          m4a1 满配换弹前 / m4a1 满配换弹后
- `cgc1-builtin (N).txt`  F12，2 段：ak47 换弹 / 满配换弹（每段 10 s，3 次换弹）

口径：帧时间 = 1000 / FPS；均值 ± 1 样本标准差；「换弹后 − 换弹前」按逐文件配对相减。
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
    r"\s*1% low framerate\s*:\s*([\d.]+) FPS")

AF_STEPS = ["空手", "ak47 换弹前", "ak47 换弹后", "满配 换弹前", "满配 换弹后"]
PAIRS = [("ak47", 1, 2), ("满配", 3, 4)]
PROBES = ["CGC render per frame", "Model render total", "Draw calls", "Bake rebuilds",
          "Static bake", "Cubes submitted", "Scope render", "Distinct RenderTypes"]


def files(pattern):
    names = [f for f in os.listdir(BASE) if re.fullmatch(pattern, f)]
    return sorted(names, key=lambda f: int(re.search(r"\((\d+)\)", f).group(1)))


def parse_af(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        text = f.read()
    out = []
    for m in AF_REC.finditer(text):
        if m.group(2).lower() != "javaw.exe":
            continue
        out.append({"fps": float(m.group(5)), "low1": float(m.group(8))})
    return out


def parse_f12(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        parts = f.read().split("customgun render profile completed")
    blocks = []
    for part in parts[1:]:
        d = {}
        for line in part.splitlines():
            m = re.match(r"\s*(.+?)\s*:\s*(-?[\d.]+|-)\s*(ms|/ frame)?", line)
            if m and m.group(1).strip() in PROBES and m.group(1).strip() not in d:
                d[m.group(1).strip()] = None if m.group(2) == "-" else float(m.group(2))
        blocks.append(d)
    return blocks


def stat(values):
    sd = statistics.stdev(values) if len(values) > 1 else 0.0
    return statistics.mean(values), sd, len(values)


def main():
    runs = [parse_af(os.path.join(BASE, f)) for f in files(r"cgc1 \(\d+\)\.txt")]
    print("## Afterburner：帧时间")
    for i, step in enumerate(AF_STEPS):
        ft = [1000.0 / r[i]["fps"] for r in runs if len(r) > i]
        low = [r[i]["low1"] for r in runs if len(r) > i]
        m, sd, n = stat(ft)
        print(f"  {step:<14} {m:6.3f} ± {sd:5.3f} ms  ({1000.0 / m:7.1f} FPS)  1% low {statistics.mean(low):6.1f}  n={n}")

    print("\n## 换弹后 − 换弹前（逐文件配对）")
    for name, before, after in PAIRS:
        deltas = [1000.0 / r[after]["fps"] - 1000.0 / r[before]["fps"] for r in runs if len(r) > after]
        base = [1000.0 / r[before]["fps"] for r in runs if len(r) > after]
        m, sd, n = stat(deltas)
        print(f"  {name:<6} +{m:6.3f} ± {sd:5.3f} ms  (+{m / statistics.mean(base) * 100:4.1f}%)  n={n}"
              f"  逐次 {[round(x, 3) for x in deltas]}")

    print("\n## F12：换弹段（10 s，3 次换弹）")
    blocks = [parse_f12(os.path.join(BASE, f)) for f in files(r"cgc1-builtin \(\d+\)\.txt")]
    for i, step in enumerate(["ak47 换弹", "满配 换弹"]):
        print(f"  [{step}]")
        for key in PROBES:
            vals = [b[i][key] for b in blocks if len(b) > i and b[i].get(key) is not None]
            if not vals:
                print(f"    {key:<24}{'-':>12}")
                continue
            m, sd, n = stat(vals)
            print(f"    {key:<24}{m:9.3f} ± {sd:6.3f}")


if __name__ == "__main__":
    main()
