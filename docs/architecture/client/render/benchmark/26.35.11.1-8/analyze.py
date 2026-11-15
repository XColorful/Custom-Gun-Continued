# -*- coding: utf-8 -*-
"""解析 26.35.11.1-8（静态展示与 TaCZ 差额归因）的 Afterburner 与内置探针数据。

数据来源（1.20.1 / Forge，两端都不开光影包）：
- `cgc1-multi (N).txt`               CGC，2 步：空手（9 把 ak47 展示框）/ 第一人称 ak47
- `cgc1-multi-builtin (N).txt`       同场景的 F12 探针（每个文件 1 段，含第一人称，n=4）
- `tacz1-multi (N).txt`              TaCZ 1.2.0，同一 2 步协议
- `multi-baseline/cgc1-multi (N).txt`    CGC，1 步：空手（9 个空展示框）
- `multi-baseline/tacz-multi (N).txt`    TaCZ，同上
- `multi-baseline/cgc1-multi-builtin (N).txt` 空展示框的 F12 探针（无提交，只作对照）

`tacz1-multi (2).txt` 只有一段（第 2 步），首段缺失；按帧数（≈503 FPS）归到第 2 步。

口径：帧时间 = 1000 / FPS；所有「平均」为重复样本的均值 ± 1 样本标准差。
每个数据文件 = 一次独立的游戏启动 + 进档，跨文件差异含进程级噪声。
「每枪」=（有枪帧时间 − 同端同批空展示框基线）/ 9。
"""
import os
import re
import statistics
import sys

import matplotlib
matplotlib.use("Agg")
import numpy as np
from matplotlib import font_manager
import matplotlib.pyplot as plt

sys.stdout.reconfigure(encoding="utf-8")

BASE = os.path.dirname(os.path.abspath(__file__))
BASELINE = os.path.join(BASE, "multi-baseline")

AF_REC = re.compile(
    r"(\d{2}-\d{2}-\d{4}, \d{2}:\d{2}:\d{2}) (\S+) benchmark completed, (\d+) frames rendered in ([\d.]+) s\s*\n"
    r"\s*Average framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Minimum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Maximum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*1% low framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*0\.1% low framerate\s*:\s*([\d.]+) FPS")

# `Name :  value unit (...)`，unit ∈ ms / / frame / %；未命中的项是 `-`
PROBE_REC = re.compile(r"^\s{2,}(\S[^:]*?)\s*:\s+(?:([\d.]+)\s*(ms|/ frame|%)|(-))\s*(?:\(|$)", re.M)

STEPS = ["空手（9 把 ak47）", "第一人称 ak47"]
BASE_STEP = ["空手（空展示框）"]


def parse_af(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        text = f.read()
    out = []
    for m in AF_REC.finditer(text):
        ts, proc, frames, secs, avg, mn, mx, low1, low01 = m.groups()
        if proc.lower() != "javaw.exe":
            continue
        out.append({
            "ts": ts, "frames": int(frames), "seconds": float(secs),
            "fps": float(avg), "min": float(mn), "max": float(mx),
            "low1": float(low1), "low01": float(low01),
        })
    return out


def group_af(dirpath, pattern, step_names, single_is_last=False):
    files = sorted(
        (f for f in os.listdir(dirpath) if re.fullmatch(pattern, f)),
        key=lambda f: int(re.search(r"\((\d+)\)", f).group(1)))
    buckets = [[] for _ in step_names]
    for name in files:
        recs = parse_af(os.path.join(dirpath, name))
        if single_is_last and len(recs) == 1 and len(step_names) > 1:
            offsets = [(len(step_names) - 1, recs[0])]
        else:
            offsets = list(enumerate(recs))
        for i, rec in offsets:
            if i < len(buckets):
                rec["file"] = name
                buckets[i].append(rec)
    return buckets


def stats(recs):
    if not recs:
        return None
    fps = [r["fps"] for r in recs]
    ft = [1000.0 / r["fps"] for r in recs]
    sd = statistics.stdev if len(recs) > 1 else (lambda _: 0.0)
    return {
        "n": len(recs),
        "fps": statistics.mean(fps), "fps_sd": sd(fps),
        "ft": statistics.mean(ft), "ft_sd": sd(ft),
        "low1": statistics.mean(r["low1"] for r in recs),
        "low01": statistics.mean(r["low01"] for r in recs),
    }


def parse_probes(path):
    """一个文件里的全部 F12 段，每段是 {名称: (值, 单位)}"""
    with open(path, encoding="utf-8", errors="replace") as f:
        text = f.read()
    blocks = []
    for chunk in text.split("customgun render profile completed")[1:]:
        items = {}
        for m in PROBE_REC.finditer(chunk):
            name, value, unit, dash = m.groups()
            name = name.strip()
            if dash:
                items[name] = None
            elif name not in items:
                items[name] = (float(value), unit)
        if items:
            blocks.append(items)
    return blocks


def group_probes(dirpath, pattern):
    """同名探针在多个文件间的均值 / 标准差"""
    files = sorted(
        (f for f in os.listdir(dirpath) if re.fullmatch(pattern, f)),
        key=lambda f: int(re.search(r"\((\d+)\)", f).group(1)))
    per_name = {}
    for name in files:
        for block in parse_probes(os.path.join(dirpath, name)):
            for key, val in block.items():
                per_name.setdefault(key, []).append(val)
    out = {}
    for key, vals in per_name.items():
        nums = [v[0] for v in vals if v is not None]
        units = [v[1] for v in vals if v is not None]
        if not nums:
            continue
        sd = statistics.stdev if len(nums) > 1 else (lambda _: 0.0)
        out[key] = {
            "n": len(nums), "mean": statistics.mean(nums), "sd": sd(nums),
            "unit": units[0], "all": nums,
        }
    return out


def fmt(s):
    return (f"{s['fps']:7.1f} ± {s['fps_sd']:4.1f} FPS | {s['ft']:6.4f} ± {s['ft_sd']:6.4f} ms | "
            f"1% low {s['low1']:6.1f} | 0.1% low {s['low01']:6.1f} | n={s['n']}")


def print_table(title, step_names, buckets, note=None):
    print(f"\n## {title}")
    if note:
        print(f"   {note}")
    for name, recs in zip(step_names, buckets):
        s = stats(recs)
        print(f"  {name:<22} {fmt(s) if s else '—'}")


def show_probes(title, probes, keys, per_submission=0, note=None):
    print(f"\n## {title}")
    if note:
        print(f"   {note}")
    for key in keys:
        p = probes.get(key)
        if p is None:
            print(f"  {key:<26} —")
            continue
        per = f" | 每提交 {p['mean'] / per_submission * 1000:6.2f} µs" if per_submission else ""
        print(f"  {key:<26} {p['mean']:7.4f} ± {p['sd']:6.4f} {p['unit']}{per} | n={p['n']}")


def main():
    guns = group_af(BASE, r"cgc1-multi \(\d+\)\.txt", STEPS)
    tacz = group_af(BASE, r"tacz1-multi \(\d+\)\.txt", STEPS, single_is_last=True)
    cgc_base = group_af(BASELINE, r"cgc1-multi \(\d+\)\.txt", BASE_STEP)
    tacz_base = group_af(BASELINE, r"tacz-multi \(\d+\)\.txt", BASE_STEP)

    print_table("CGC（9 把 ak47 展示框）", STEPS, guns)
    print_table("TaCZ 1.2.0（同协议）", STEPS, tacz, "tacz1-multi (2) 只有第 2 步，已按帧数归位")
    print_table("CGC 空展示框基线", BASE_STEP, cgc_base)
    print_table("TaCZ 空展示框基线", BASE_STEP, tacz_base)

    cb, tb = stats(cgc_base[0]), stats(tacz_base[0])
    c1, c2 = stats(guns[0]), stats(guns[1])
    t1, t2 = stats(tacz[0]), stats(tacz[1])

    print("\n## 推导：静态展示（第 1 步，空手）")
    print(f"  基线：CGC {cb['ft']:.4f} ms / TaCZ {tb['ft']:.4f} ms（差 {cb['ft'] - tb['ft']:+.4f} ms，"
          f"{100.0 * (cb['ft'] - tb['ft']) / tb['ft']:+.1f}%）")
    print(f"  9 把枪：CGC {c1['ft']:.4f} / TaCZ {t1['ft']:.4f} ms（差 {c1['ft'] - t1['ft']:+.4f} ms）")
    cgc_gun = (c1["ft"] - cb["ft"]) / 9
    tacz_gun = (t1["ft"] - tb["ft"]) / 9
    print(f"  每枪（同批基线相减）：CGC {cgc_gun * 1000:.2f} µs / TaCZ {tacz_gun * 1000:.2f} µs"
          f"（差 {(cgc_gun - tacz_gun) * 1000:+.2f} µs/枪，合计 {(cgc_gun - tacz_gun) * 9:.4f} ms）")
    se_raw = (c1["ft_sd"] ** 2 / c1["n"] + t1["ft_sd"] ** 2 / t1["n"]) ** 0.5
    se = (c1["ft_sd"] ** 2 / c1["n"] + cb["ft_sd"] ** 2 / cb["n"]
          + t1["ft_sd"] ** 2 / t1["n"] + tb["ft_sd"] ** 2 / tb["n"]) ** 0.5
    print(f"  有枪差 {c1['ft'] - t1['ft']:+.4f} ± {se_raw:.4f} ms（{abs(c1['ft'] - t1['ft']) / se_raw:.1f}σ）")
    print(f"  每枪差的 1σ 约 {se / 9 * 1000:.2f} µs（四项标准误合成），即 "
          f"{abs(cgc_gun - tacz_gun) / (se / 9):.1f}σ")
    print(f"  第 2 步（+第一人称）：CGC {c2['ft']:.4f} / TaCZ {t2['ft']:.4f} ms"
          f"（CGC 快 {t2['ft'] - c2['ft']:.4f} ms）")

    probes = group_probes(BASE, r"cgc1-multi-builtin \(\d+\)\.txt")
    base_probes = group_probes(BASELINE, r"cgc1-multi-builtin \(\d+\)\.txt")
    subs = probes.get("World-item submissions")
    n_sub = subs["mean"] if subs else 9.0

    show_probes("CGC 探针（第 2 步：9 把展示框 + 第一人称，n=4）", probes, [
        "Average frame time", "CGC render per frame", "World-item submissions", "Draw calls",
        "Nodes captured", "Distinct RenderTypes", "Bake rebuilds", "Light patches",
        "Bake upload bytes", "Allocated bytes",
        "World-item submit", "Data refresh", "Scope render", "Static status",
        "Functional nodes", "Model render total", "Delegate render",
        "endBatch draw", "endBatch upload",
        "First-person submit", "Hand event entry", "State update",
    ], per_submission=0)
    show_probes("CGC 每提交分解（分母 = World-item submissions）", probes, [
        "World-item submit", "Static status", "Data refresh", "Functional nodes",
    ], per_submission=n_sub,
        note="`Functional nodes` 嵌套在 `Static status` 内；draw 时间未单独按静态拆分，见报告第 3 节")

    show_probes("空展示框的 F12 对照（无任何提交）", base_probes, [
        "Average frame time", "World-item submissions", "Draw calls", "Upload bytes",
        "endBatch draw", "endBatch upload",
    ])

    if probes.get("Static status") and probes.get("Data refresh") and probes.get("World-item submit"):
        status = probes["Static status"]["mean"] / n_sub * 1000
        data = probes["Data refresh"]["mean"] / n_sub * 1000
        submit = probes["World-item submit"]["mean"] / n_sub * 1000
        draw = (submit - status - data)
        print("\n## 每提交成本的闭合（每帧均值 / 9）")
        print(f"  World-item submit {submit:.2f} µs = Static status {status:.2f} + Data refresh {data:.2f}"
              f" + 余量 {draw:.2f} µs（= 2 次 draw + 缓存键 / 矩阵 / getBuffer）")
        print(f"  状态段占每提交 {100.0 * status / submit:.1f}%，是 CGC−TaCZ 每枪差的 "
              f"{status / ((cgc_gun - tacz_gun) * 1000):.1f} 倍")

    plot(cb, tb, c1, t1, c2, t2, probes, n_sub)


def setup_font():
    for f in (r"C:\Windows\Fonts\msyh.ttc", r"C:\Windows\Fonts\simhei.ttf"):
        if os.path.exists(f):
            font_manager.fontManager.addfont(f)
    plt.rcParams["font.sans-serif"] = ["Microsoft YaHei", "SimHei", "DejaVu Sans"]
    plt.rcParams["axes.unicode_minus"] = False
    plt.rcParams["axes.spines.top"] = False
    plt.rcParams["axes.spines.right"] = False


def plot(cb, tb, c1, t1, c2, t2, probes, n_sub):
    setup_font()
    fig, axes = plt.subplots(1, 2, figsize=(13, 5.2))
    fig.suptitle("静态展示与 TaCZ 的差额归因（Afterburner + F12，1.20.1，不开光影）", fontsize=14, y=0.99)

    ax = axes[0]
    labels = ["CGC 空展示框", "TaCZ 空展示框", "CGC 9 把 ak47", "TaCZ 9 把 ak47"]
    vals = [cb["ft"], tb["ft"], c1["ft"], t1["ft"]]
    errs = [cb["ft_sd"], tb["ft_sd"], c1["ft_sd"], t1["ft_sd"]]
    colors = ["#b0b0b0", "#8c8c8c", "#b8442f", "#2f6db3"]
    x = np.arange(len(vals))
    ax.bar(x, vals, 0.55, yerr=errs, capsize=2, color=colors, error_kw=dict(lw=0.8))
    for xi, v in zip(x, vals):
        ax.annotate(f"{v:.3f}", (xi, v), textcoords="offset points", xytext=(0, 3), ha="center", fontsize=9)
    ax.set_xticks(x)
    ax.set_xticklabels(labels, fontsize=9)
    ax.set_ylabel("平均帧时间 (ms)")
    ax.set_title("A. 场景基线与 9 把枪展示（n=4 / TaCZ 有枪 n=3）", fontsize=11)
    ax.grid(axis="y", alpha=0.25)

    ax = axes[1]
    keys = [("Static status", "#b8442f"), ("Data refresh", "#2f6db3"), ("余量（draw 等）", "#c9a227")]
    names, means, errs, cols = [], [], [], []
    submit = probes["World-item submit"]
    status = probes["Static status"]
    data = probes["Data refresh"]
    rest = [s - st - da for s, st, da in zip(submit["all"], status["all"], data["all"])]
    means = [status["mean"] / n_sub * 1000, data["mean"] / n_sub * 1000, statistics.mean(rest) / n_sub * 1000]
    errs = [status["sd"] / n_sub * 1000, data["sd"] / n_sub * 1000,
            (statistics.stdev(rest) if len(rest) > 1 else 0.0) / n_sub * 1000]
    names = [k for k, _ in keys]
    cols = [c for _, c in keys]
    bottom = 0.0
    for name, m, e, c in zip(names, means, errs, cols):
        ax.bar(0, m, 0.5, bottom=bottom, yerr=e, capsize=2, color=c, label=name,
               error_kw=dict(lw=0.8))
        if m > 1.5:
            ax.annotate(f"{name}\n{m:.1f} µs", (0, bottom + m / 2), ha="center", va="center",
                        fontsize=8, color="white")
        bottom += m
    ax.annotate(f"合计 {bottom:.1f} µs", (0.42, bottom), ha="left", va="center", fontsize=9)
    ax.set_xlim(-0.6, 1.1)
    ax.set_xticks([])
    ax.set_ylabel("每提交成本 (µs)")
    ax.set_title("B. CGC 单次静态提交的构成（F12，9 次/帧）", fontsize=11)
    ax.grid(axis="y", alpha=0.25)

    fig.tight_layout(rect=[0, 0, 1, 0.94])
    out = os.path.join(BASE, "测试数据总览-1-帧时间.png")
    fig.savefig(out, dpi=130)
    plt.close(fig)
    print(f"\n图已生成：{out}")


if __name__ == "__main__":
    main()
