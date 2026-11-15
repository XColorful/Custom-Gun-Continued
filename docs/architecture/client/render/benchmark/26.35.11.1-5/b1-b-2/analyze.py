# -*- coding: utf-8 -*-
"""解析 b1-b-2（光影下的烘焙 A/B）的数据。

数据来源：`cgc1 (N).txt`，共 6 次独立启动，每次 5 步：
空手 / ak47 / ak47+光影 / 满配 / 满配+光影。

文件 1–3 为 `StaticBake.ENABLED = true`，文件 4–6 为 `StaticBake.ENABLED = false`。
口径：帧时间 = 1000 / FPS；两组各自统计，配对差按文件序号一一对应。
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

STEPS = ["空手", "ak47", "ak47+光影", "满配", "满配+光影"]
ON, OFF = "烘焙开", "烘焙关"


def parse(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        text = f.read()
    out = []
    for m in AF_REC.finditer(text):
        if m.group(2).lower() != "javaw.exe":
            continue
        out.append({"fps": float(m.group(5)), "low1": float(m.group(8))})
    return out


def stat(values):
    sd = statistics.stdev(values) if len(values) > 1 else 0.0
    return statistics.mean(values), sd, len(values)


def main():
    names = [f for f in os.listdir(BASE) if re.fullmatch(r"cgc1 \(\d+\)\.txt", f)]
    names.sort(key=lambda f: int(re.search(r"\((\d+)\)", f).group(1)))
    runs = [parse(os.path.join(BASE, f)) for f in names]
    groups = {ON: runs[:3], OFF: runs[3:6]}

    print("## Afterburner：帧时间（ms / FPS）")
    for i, step in enumerate(STEPS):
        row = []
        for g in (ON, OFF):
            ft = [1000.0 / r[i]["fps"] for r in groups[g] if len(r) > i]
            low = [r[i]["low1"] for r in groups[g] if len(r) > i]
            m, sd, n = stat(ft)
            row.append(f"{m:6.3f} ± {sd:5.3f} ({1000.0 / m:6.1f} FPS, 1%low {statistics.mean(low):5.1f})")
        print(f"  {step:<10} 开 {row[0]} | 关 {row[1]}")

    print("\n## 配对差（关 − 开，正数 = 烘焙省下）")
    for i, step in enumerate(STEPS[1:], 1):
        deltas = [1000.0 / off[i]["fps"] - 1000.0 / on[i]["fps"]
                  for on, off in zip(groups[ON], groups[OFF]) if len(on) > i and len(off) > i]
        base = [1000.0 / off[i]["fps"] for off in groups[OFF] if len(off) > i]
        m, sd, n = stat(deltas)
        print(f"  {step:<10} {m:6.3f} ± {sd:5.3f} ms  ({m / statistics.mean(base) * 100:5.1f}%)  n={n}"
              f"  逐次 {[round(x, 3) for x in deltas]}")

    print("\n## CGC（同组 步骤 − 空手）")
    for i, step in enumerate(STEPS[1:], 1):
        for g in (ON, OFF):
            deltas = [1000.0 / r[i]["fps"] - 1000.0 / r[0]["fps"] for r in groups[g] if len(r) > i]
            m, sd, n = stat(deltas)
            print(f"  {step:<10} {g} {m:6.3f} ± {sd:5.3f} ms  n={n}")


if __name__ == "__main__":
    main()
