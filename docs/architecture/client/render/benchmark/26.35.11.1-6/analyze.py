# -*- coding: utf-8 -*-
"""解析 26.35.11.1-6 / b1-c-b（B1-c-B 光照原地改写）的数据。

数据来源：`b1-c-b/` 下 6 次独立启动，每次 = Afterburner + F12 同一进程内先后运行。
- `cgc1 (N).txt`：Afterburner 7 步 =
  空手（夜晚静止）/ ak47 移动 / ak47+光影 静止 / ak47+光影 移动 /
  满配 移动 / 满配+光影 静止 / 满配+光影 移动
- `cgc1-builtin (N).txt`：F12 4 段 = ak47 移动 / ak47+光影 移动 / 满配 移动 / 满配+光影 移动

三次构建，用 `python analyze.py 首次 末次` 分别统计：
- 第 1–5 次：B1-c-B，瞄具子网格仍按光照建键（第 5 次起多了定位探针）
- 第 6 次：瞄具子网格也改成原地改写

口径：帧时间 = 1000 / FPS；均值 ± 1 样本标准差。
`Bake rebuilds` 是每帧的重建次数（一次光照变化会重建多个分块网格）；
`Static bake` 的「命中帧比例」是发生了重建的帧占比。
"""
import os
import re
import statistics
import sys

BASE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "b1-c-b")
sys.stdout.reconfigure(encoding="utf-8")

# (首次, 末次) 文件序号；None 表示全部
RUNS = None

AF_REC = re.compile(
    r"(?:\d{2}-\d{2}-\d{4}, \d{2}:\d{2}:\d{2}) (\S+) benchmark completed, (\d+) frames rendered in ([\d.]+) s\s*\n"
    r"\s*Average framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Minimum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Maximum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*1% low framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*0\.1% low framerate\s*:\s*([\d.]+) FPS")

AF_STEPS = ["空手（夜晚）", "ak47 移动", "ak47+光影 静止", "ak47+光影 移动",
            "满配 移动", "满配+光影 静止", "满配+光影 移动"]
F12_STEPS = ["ak47 移动", "ak47+光影 移动", "满配 移动", "满配+光影 移动"]
PROBES = ["Average frame time", "1% high frame time", "Maximum frame time",
          "CGC render per frame", "Draw calls", "Bake rebuilds",
          "Static bake", "Bake emit", "Bake buffer create", "Bake upload", "Light update"]
COUNTERS = ["Bake upload bytes", "Light patches", "Light patch bytes",
            "Bake: new key", "Bake: light fill", "Bake: patch failed", "Bake: chunk invalid",
            "Light patch: no copy", "Light patch: no vbo"]


def files(pattern):
    names = [f for f in os.listdir(BASE) if re.fullmatch(pattern, f)]
    names = sorted(names, key=lambda f: int(re.search(r"\((\d+)\)", f).group(1)))
    if RUNS is None:
        return names
    return [f for f in names if RUNS[0] <= int(re.search(r"\((\d+)\)", f).group(1)) <= RUNS[1]]


def parse_af(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        text = f.read()
    out = []
    for m in AF_REC.finditer(text):
        if m.group(1).lower() != "javaw.exe":
            continue
        out.append({"fps": float(m.group(4)), "low1": float(m.group(7)),
                    "low01": float(m.group(8))})
    return out


def parse_f12(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        parts = f.read().split("customgun render profile completed")
    blocks = []
    for part in parts[1:]:
        d = {"frames": None}
        m = re.search(r"(\d+) frames rendered in ([\d.]+) s", part)
        if m:
            d["frames"] = int(m.group(1))
            d["seconds"] = float(m.group(2))
        for line in part.splitlines():
            m = re.match(r"\s*(.+?)\s*:\s*(-?[\d.]+|-)\s*(ms|/ frame)?", line)
            if not m:
                continue
            name = m.group(1).strip()
            if name == "Static bake":
                d["Static bake"] = None if m.group(2) == "-" else float(m.group(2))
                pct = re.search(r"([\d.]+)% of frames", line)
                d["Static bake hits"] = float(pct.group(1)) if pct else None
            elif (name in PROBES or name in COUNTERS) and name not in d:
                d[name] = None if m.group(2) == "-" else float(m.group(2))
        blocks.append(d)
    return blocks


def stat(values):
    sd = statistics.stdev(values) if len(values) > 1 else 0.0
    return statistics.mean(values), sd, len(values)


def main():
    print(f"## 统计范围：第 {RUNS[0]}–{RUNS[1]} 次" if RUNS else "## 统计范围：全部")
    runs = [parse_af(os.path.join(BASE, f)) for f in files(r"cgc1 \(\d+\)\.txt")]
    print("## Afterburner：帧时间（n=%d）" % len(runs))
    for i, step in enumerate(AF_STEPS):
        ft = [1000.0 / r[i]["fps"] for r in runs if len(r) > i]
        low1 = [r[i]["low1"] for r in runs if len(r) > i]
        low01 = [r[i]["low01"] for r in runs if len(r) > i]
        m, sd, n = stat(ft)
        print(f"  {step:<14} {m:6.3f} ± {sd:5.3f} ms ({1000.0 / m:7.1f} FPS)"
              f"   1% low {statistics.mean(low1):6.1f}   0.1% low {statistics.mean(low01):6.1f}  n={n}")

    print("\n## Afterburner：移动 − 静止（光影下，逐文件配对）")
    for gun, stat_idx, move_idx in (("ak47", 2, 3), ("满配", 5, 6)):
        deltas = [1000.0 / r[move_idx]["fps"] - 1000.0 / r[stat_idx]["fps"] for r in runs]
        m, sd, n = stat(deltas)
        print(f"  {gun:<8} {m:6.3f} ± {sd:5.3f} ms  n={n}")

    print("\n## F12：移动段")
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
        for key in COUNTERS:
            vals = [b[i][key] for b in blocks if len(b) > i and b[i].get(key) is not None]
            if not vals:
                print(f"    {key:<24}{'-':>12}")
                continue
            m, sd, n = stat(vals)
            print(f"    {key:<24}{m:10.3f} ± {sd:7.3f}")
        causes = ["Bake: new key", "Bake: light fill", "Bake: patch failed", "Bake: chunk invalid"]
        derived = []
        for b in blocks:
            if len(b) <= i or b[i].get("Bake rebuilds") is None:
                continue
            if any(b[i].get(k) is None for k in causes):
                continue
            derived.append(b[i]["Bake rebuilds"] - sum(b[i][k] for k in causes))
        if derived:
            m, sd, n = stat(derived)
            print(f"    {'多实例整模路径烘焙（差额）':<20}{m:10.3f} ± {sd:7.3f}")
        hits = [b[i]["Static bake hits"] for b in blocks if len(b) > i and b[i].get("Static bake hits") is not None]
        if hits:
            m, sd, _ = stat(hits)
            fps = statistics.mean([1000.0 / b[i]["Average frame time"] for b in blocks
                                   if len(b) > i and b[i].get("Average frame time")])
            print(f"    {'重建帧占比':<24}{m:9.2f}% ± {sd:6.2f}   ≈{m / 100 * fps:5.2f} 帧/秒")
        per_bake = [b[i]["Bake upload bytes"] / b[i]["Bake rebuilds"] for b in blocks
                    if len(b) > i and b[i].get("Bake upload bytes") is not None and b[i].get("Bake rebuilds")]
        if per_bake:
            m, sd, n = stat(per_bake)
            print(f"    {'单次重建字节':<24}{m / 1024:9.1f} KB ± {sd / 1024:6.1f}  n={n}")
        per_event = [b[i]["Bake upload bytes"] / (b[i]["Static bake hits"] / 100.0)
                     for b in blocks
                     if len(b) > i and b[i].get("Bake upload bytes") is not None
                     and b[i].get("Static bake hits")]
        if per_event:
            m, sd, n = stat(per_event)
            print(f"    {'每次事件重建字节':<24}{m / 1024:9.1f} KB ± {sd / 1024:6.1f}  n={n}")
        per_frame_bake = [b[i]["Bake rebuilds"] / (b[i]["Static bake hits"] / 100.0)
                          for b in blocks
                          if len(b) > i and b[i].get("Bake rebuilds") is not None
                          and b[i].get("Static bake hits")]
        if per_frame_bake:
            m, sd, n = stat(per_frame_bake)
            print(f"    {'每次事件重建块数':<24}{m:9.1f} ± {sd:6.1f}  n={n}")
        total = [b[i]["Bake upload bytes"] * b[i]["frames"] for b in blocks
                 if len(b) > i and b[i].get("Bake upload bytes") is not None and b[i].get("frames")]
        if total:
            m, sd, n = stat(total)
            print(f"    {'整段重建总量':<24}{m / 1048576:9.2f} MB ± {sd / 1048576:6.2f}  n={n}")


if __name__ == "__main__":
    # 可选：python analyze.py 首次 末次（按文件序号筛选，比较不同构建时用）
    RUNS = (int(sys.argv[1]), int(sys.argv[2])) if len(sys.argv) >= 3 else None
    main()
