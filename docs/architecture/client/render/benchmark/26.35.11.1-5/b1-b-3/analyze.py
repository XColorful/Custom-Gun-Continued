# -*- coding: utf-8 -*-
"""解析 b1-b-3（光照变化下的重烘焙）的数据。

数据来源：本目录 4 次独立启动（n=4），每次 = Afterburner + F12 同一进程内先后运行。
- `cgc1 (N).txt`：Afterburner 7 步 =
  空手（夜晚静止）/ ak47 移动 / ak47+光影 静止 / ak47+光影 移动 /
  满配 移动 / 满配+光影 静止 / 满配+光影 移动
- `cgc1-builtin (N).txt`：F12 4 段 = ak47 移动 / ak47+光影 移动 / 满配 移动 / 满配+光影 移动

口径：帧时间 = 1000 / FPS；均值 ± 1 样本标准差。`Static bake` 的「命中帧比例」从探针行尾读取，
折算重建帧/秒 = 命中比例 × 该段 FPS。
"""
import os
import re
import statistics
import sys

BASE = os.path.dirname(os.path.abspath(__file__))
sys.stdout.reconfigure(encoding="utf-8")

AF_REC = re.compile(
    r"(?:\d{2}-\d{2}-\d{4}, \d{2}:\d{2}:\d{2}) (\S+) benchmark completed, (\d+) frames rendered in ([\d.]+) s\s*\n"
    r"\s*Average framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Minimum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Maximum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*1% low framerate\s*:\s*([\d.]+) FPS")

AF_STEPS = ["空手（夜晚）", "ak47 移动", "ak47+光影 静止", "ak47+光影 移动",
            "满配 移动", "满配+光影 静止", "满配+光影 移动"]
F12_STEPS = ["ak47 移动", "ak47+光影 移动", "满配 移动", "满配+光影 移动"]
PROBES = ["Average frame time", "Maximum frame time", "1% high frame time",
          "CGC render per frame", "Model render total", "Draw calls",
          "Bake rebuilds", "Static bake", "Cubes submitted", "Upload bytes", "Scope render"]


def files(pattern):
    names = [f for f in os.listdir(BASE) if re.fullmatch(pattern, f)]
    return sorted(names, key=lambda f: int(re.search(r"\((\d+)\)", f).group(1)))


def parse_af(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        text = f.read()
    out = []
    for m in AF_REC.finditer(text):
        if m.group(1).lower() != "javaw.exe":
            continue
        out.append({"fps": float(m.group(4)), "low1": float(m.group(7))})
    return out


def parse_f12(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        parts = f.read().split("customgun render profile completed")
    blocks = []
    for part in parts[1:]:
        d = {}
        for line in part.splitlines():
            m = re.match(r"\s*(.+?)\s*:\s*(-?[\d.]+|-)\s*(ms|/ frame)?", line)
            if not m:
                continue
            name = m.group(1).strip()
            if name == "Static bake":
                d["Static bake"] = None if m.group(2) == "-" else float(m.group(2))
                pct = re.search(r"([\d.]+)% of frames", line)
                d["Static bake hits"] = float(pct.group(1)) if pct else None
            elif name in PROBES and name not in d:
                d[name] = None if m.group(2) == "-" else float(m.group(2))
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
        print(f"  {step:<16} {m:6.3f} ± {sd:5.3f} ms  ({1000.0 / m:7.1f} FPS)  1% low {statistics.mean(low):6.1f}  n={n}")

    print("\n## Afterburner：相对空手（逐文件配对）")
    for i in (1, 4):
        deltas = [1000.0 / r[i]["fps"] - 1000.0 / r[0]["fps"] for r in runs if len(r) > i]
        m, sd, n = stat(deltas)
        print(f"  {AF_STEPS[i]:<16} {m:6.3f} ± {sd:5.3f} ms  n={n}")
    print("  移动 − 静止（光影下，逐文件配对）")
    for gun, stat_idx, move_idx in (("ak47", 2, 3), ("满配", 5, 6)):
        deltas = [1000.0 / r[move_idx]["fps"] - 1000.0 / r[stat_idx]["fps"] for r in runs]
        m, sd, n = stat(deltas)
        print(f"    {gun:<8} {m:6.3f} ± {sd:5.3f} ms  n={n}")

    print("\n## F12：移动段（10 s 左右）")
    blocks = [parse_f12(os.path.join(BASE, f)) for f in files(r"cgc1-builtin \(\d+\)\.txt")]
    for i, step in enumerate(F12_STEPS):
        print(f"  [{step}]")
        for key in PROBES:
            vals = [b[i][key] for b in blocks if len(b) > i and b[i].get(key) is not None]
            if not vals:
                print(f"    {key:<24}{'-':>12}")
                continue
            m, sd, n = stat(vals)
            print(f"    {key:<24}{m:10.3f} ± {sd:7.3f}")
        hits = [b[i]["Static bake hits"] for b in blocks if len(b) > i and b[i].get("Static bake hits") is not None]
        fps = [b[i]["Average frame time"] for b in blocks if len(b) > i and b[i].get("Average frame time") is not None]
        if hits:
            m, sd, n = stat(hits)
            mean_fps = 1000.0 / statistics.mean(fps)
            print(f"    {'Static bake 命中帧':<24}{m:9.2f}% ± {sd:6.2f}   ≈{m / 100 * mean_fps:5.1f} 帧/秒")


if __name__ == "__main__":
    main()
