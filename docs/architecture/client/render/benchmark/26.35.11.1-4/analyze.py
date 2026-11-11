# -*- coding: utf-8 -*-
"""解析 26.35.11.1-4（B1-a 静态烘焙阶段验收批次）的 Afterburner 数据。

数据来源（仅 1.20.1 / Forge，本批次不开内置探针）：
- `cgc1-multi (N).txt`            CGC，4 步：空手（9 把 ak47 展示框）/ 第一人称 ak47 /
                                  开光影·空手 / 开光影·第一人称 ak47
- `tacz1-multi (N).txt`           TaCZ 1.2.0，同一 4 步与协议
- `tacz2-multi (N).txt`           TaCZ 1.2.0 + Accelerated Rendering，同上
- `b1-a-1/cgc1-multi (1).txt`     同一 4 步，修复前构建的验证跑（n=1）
- `b1-a-2/cgc1-multi (N).txt`     2 步：空手（9 个空展示框）/ 开光影·空手（n=3）
- `b1-a-3/cgc1-multi (N).txt`     `StaticBake#ENABLED = false` 的 A/B 对照：空手 / 开光影·空手（n=4）

跨批对照（只读，不修改上一批目录）：
- `../26.35.11.1-2/cgc1-multi (N).txt`             重构前，9 把 ak47 展示框（n=6，第 1 段为空手）
- `../26.35.11.1-2/multi-baseline/cgc1-multi (N).txt` 重构前场景基线，9 个空展示框（n=6）

`26.35.11.1` 的 TaCZ multi 只有 1 次且步骤顺序不同，已被本批取代，不参与计算。

口径：帧时间 = 1000 / FPS；所有「平均」为重复样本的均值 ± 1 样本标准差。
每个数据文件 = 一次独立的游戏启动 + 进档，跨文件差异含进程级噪声。

假设：`b1-a-2` 的展示框内没有枪（`标准.txt` 只写「物品展示框」）。依据是它的空手帧数
（≈1165）与 `26.35.11.1-2/multi-baseline` 的空场基线（≈1174）一致，而有枪的同场景重构前
只有 ≈184。
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
PREV = os.path.join(os.path.dirname(BASE), "26.35.11.1-2")

AF_REC = re.compile(
    r"(\d{2}-\d{2}-\d{4}, \d{2}:\d{2}:\d{2}) (\S+) benchmark completed, (\d+) frames rendered in ([\d.]+) s\s*\n"
    r"\s*Average framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Minimum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Maximum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*1% low framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*0\.1% low framerate\s*:\s*([\d.]+) FPS")

POST_STEPS = ["空手（9 把 ak47）", "第一人称 ak47", "开光影·空手", "开光影·第一人称 ak47"]
BASE_STEPS = ["空手（空展示框）", "开光影·空手（空展示框）"]
AB_STEPS = ["空手（烘焙关）", "开光影·空手（烘焙关）"]


def parse(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        text = f.read()
    out = []
    for m in AF_REC.finditer(text):
        ts, proc, frames, secs, avg, mn, mx, low1, low01 = m.groups()
        if proc.lower() != "javaw.exe":
            continue
        out.append({
            "ts": ts, "process": proc, "frames": int(frames), "seconds": float(secs),
            "fps": float(avg), "min": float(mn), "max": float(mx),
            "low1": float(low1), "low01": float(low01),
        })
    return out


def group(dirpath, pattern, step_names):
    """目录内每个文件按步序取记录，合并成「每步一组样本」"""
    files = sorted(
        (f for f in os.listdir(dirpath) if re.fullmatch(pattern, f)),
        key=lambda f: int(re.search(r"\((\d+)\)", f).group(1)))
    buckets = [[] for _ in step_names]
    for name in files:
        recs = parse(os.path.join(dirpath, name))
        for i, rec in enumerate(recs):
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
        "min": statistics.mean(r["min"] for r in recs),
    }


def fmt(s):
    return f"{s['fps']:7.1f} ± {s['fps_sd']:4.1f} FPS | {s['ft']:6.3f} ± {s['ft_sd']:5.3f} ms | 1% low {s['low1']:6.1f} | n={s['n']}"


def print_table(title, step_names, buckets, note=None):
    print(f"\n## {title}")
    if note:
        print(f"   {note}")
    for name, recs in zip(step_names, buckets):
        s = stats(recs)
        print(f"  {name:<24} {fmt(s) if s else '—'}")


def main():
    post = group(BASE, r"cgc1-multi \(\d+\)\.txt", POST_STEPS)
    tacz1 = group(BASE, r"tacz1-multi \(\d+\)\.txt", POST_STEPS)
    tacz2 = group(BASE, r"tacz2-multi \(\d+\)\.txt", POST_STEPS)
    pre_fix = group(os.path.join(BASE, "b1-a-1"), r"cgc1-multi \(\d+\)\.txt", POST_STEPS)
    shader_base = group(os.path.join(BASE, "b1-a-2"), r"cgc1-multi \(\d+\)\.txt", BASE_STEPS)
    ab = group(os.path.join(BASE, "b1-a-3"), r"cgc1-multi \(\d+\)\.txt", AB_STEPS)
    prev_guns = group(PREV, r"cgc1-multi \(\d+\)\.txt", ["9 把 ak47（空手）", "9 把 ak47 + 手持"])
    prev_scene = group(os.path.join(PREV, "multi-baseline"), r"cgc1-multi \(\d+\)\.txt", ["空展示框（空手）"])

    print_table("CGC（修复后构建，9 把 ak47 展示框）", POST_STEPS, post)
    print_table("TaCZ 1.2.0（同协议）", POST_STEPS, tacz1)
    print_table("TaCZ 1.2.0 + Accelerated Rendering（同协议）", POST_STEPS, tacz2)
    print_table("b1-a-1（修复前构建，同一 4 步，n=1）", POST_STEPS, pre_fix)
    print_table("b1-a-2（9 个空展示框，开光影基线）", BASE_STEPS, shader_base)
    print_table("b1-a-3（StaticBake#ENABLED=false，A/B 对照）", AB_STEPS, ab)
    print_table("26.35.11.1-2（重构前，无光影）", ["9 把 ak47（空手）", "9 把 ak47 + 手持"], prev_guns)
    print_table("26.35.11.1-2 场景基线（重构前，空展示框）", ["空展示框（空手）"], prev_scene)

    post9 = stats(post[0])
    post_fp = stats(post[1])
    post_sh = stats(post[2])
    post_sh_fp = stats(post[3])
    ta1 = [stats(b) for b in tacz1]
    ta2 = [stats(b) for b in tacz2]
    pre9 = stats(prev_guns[0])
    pre_scene = stats(prev_scene[0])
    scene_base = stats(shader_base[0])
    shader_empty = stats(shader_base[1])
    ab_off = stats(ab[0])
    ab_sh = stats(ab[1])

    print("\n## 推导")
    pre_cost = pre9["ft"] - pre_scene["ft"]
    post_cost = post9["ft"] - scene_base["ft"]
    print(f"  无光影 9 把枪成本：重构前 {pre_cost:.3f} ms（{pre9['ft']:.3f} − {pre_scene['ft']:.3f}）"
          f" → 重构后 {post_cost:.3f} ms（{post9['ft']:.3f} − {scene_base['ft']:.3f}，基线取 b1-a-2 第 1 步）")
    print(f"  单枪：{pre_cost / 9:.4f} ms → {post_cost / 9:.4f} ms，"
          f"降幅 {100.0 * (1 - post_cost / pre_cost):.1f}%")
    print(f"  9 把枪整帧：{pre9['ft']:.3f} → {post9['ft']:.3f} ms，"
          f"{100.0 * (1 - post9['ft'] / pre9['ft']):.1f}%")
    print(f"  第一人称相对同状态空手：无光影 +{post_fp['ft'] - post9['ft']:.3f} ms，"
          f"开光影 +{post_sh_fp['ft'] - post_sh['ft']:.3f} ms")
    print(f"  开光影 9 把枪相对空展示框：+{post_sh['ft'] - shader_empty['ft']:.3f} ms"
          f"（{post_sh['ft']:.3f} vs {shader_empty['ft']:.3f}），占帧 "
          f"{100.0 * (post_sh['ft'] - shader_empty['ft']) / post_sh['ft']:.1f}%")
    print(f"  A/B 烘焙关：空手 {ab_off['ft']:.3f} ms（重构前 {pre9['ft']:.3f}，验证构建一致），"
          f"开光影 {ab_sh['ft']:.3f} ms → 烘焙开 {post_sh['ft']:.3f} ms（{ab_sh['ft'] / post_sh['ft']:.2f}×）")
    print("  场景基线：重构前 %.3f ms / b1-a-2 %.3f ms（差 %+.3f ms，跨进程噪声）"
          % (pre_scene["ft"], scene_base["ft"], scene_base["ft"] - pre_scene["ft"]))
    b2 = [r for r in shader_base[0] if r["seconds"] > 5.0]
    if len(b2) != len(shader_base[0]):
        print("  注：b1-a-2 第 3 个文件的首段只有 1.9 s；剔除后空展示框为 %.3f ms（n=%d）"
              % (stats(b2)["ft"], len(b2)))

    print("\n## 与 TaCZ 对照（同场景、同协议）")
    print(f"  无光影·空手：CGC {post9['ft']:.3f} / TaCZ {ta1[0]['ft']:.3f} / "
          f"TaCZ+AR {ta2[0]['ft']:.3f} ms（CGC − TaCZ {post9['ft'] - ta1[0]['ft']:+.3f}）")
    print(f"  无光影·持枪：CGC {post_fp['ft']:.3f} / TaCZ {ta1[1]['ft']:.3f} / "
          f"TaCZ+AR {ta2[1]['ft']:.3f} ms（CGC − TaCZ {post_fp['ft'] - ta1[1]['ft']:+.3f}）")
    print(f"  开光影·空手：CGC {post_sh['ft']:.3f} / TaCZ {ta1[2]['ft']:.3f} / "
          f"TaCZ+AR {ta2[2]['ft']:.3f} ms")
    print(f"  开光影·持枪：CGC {post_sh_fp['ft']:.3f} / TaCZ {ta1[3]['ft']:.3f} / "
          f"TaCZ+AR {ta2[3]['ft']:.3f} ms")
    print(f"  重构前 CGC 空手 {pre9['ft']:.3f} ms = TaCZ 的 {pre9['ft'] / ta1[0]['ft']:.1f}×")
    print(f"  TaCZ 9 把枪成本（同一空展示框基线 {scene_base['ft']:.3f}）："
          f"{ta1[0]['ft'] - scene_base['ft']:.3f} ms，单枪 {(ta1[0]['ft'] - scene_base['ft']) / 9:.4f} ms")

    plot(post9, post_fp, post_sh, post_sh_fp, pre9, ta1, ta2, scene_base, ab_off, ab_sh)


def setup_font():
    for f in (r"C:\Windows\Fonts\msyh.ttc", r"C:\Windows\Fonts\simhei.ttf"):
        if os.path.exists(f):
            font_manager.fontManager.addfont(f)
    plt.rcParams["font.sans-serif"] = ["Microsoft YaHei", "SimHei", "DejaVu Sans"]
    plt.rcParams["axes.unicode_minus"] = False
    plt.rcParams["axes.spines.top"] = False
    plt.rcParams["axes.spines.right"] = False


def plot(post9, post_fp, post_sh, post_sh_fp, pre9, ta1, ta2, scene_base, ab_off, ab_sh):
    setup_font()
    fig, axes = plt.subplots(1, 2, figsize=(13, 5.2))
    fig.suptitle("B1-a 静态烘焙：重构前后、TaCZ 对照与光影 A/B（Afterburner，1.20.1）", fontsize=14, y=0.99)

    ax = axes[0]
    labels = ["CGC 重构前", "烘焙关", "CGC B1-a", "TaCZ 1.2.0", "TaCZ + AR"]
    vals = [pre9["ft"], ab_off["ft"], post9["ft"], ta1[0]["ft"], ta2[0]["ft"]]
    errs = [pre9["ft_sd"], ab_off["ft_sd"], post9["ft_sd"], ta1[0]["ft_sd"], ta2[0]["ft_sd"]]
    colors = ["#8c8c8c", "#c98a2a", "#b8442f", "#2f6db3", "#6a9a3f"]
    x = np.arange(len(vals))
    ax.bar(x, vals, 0.55, yerr=errs, capsize=2, color=colors, error_kw=dict(lw=0.8))
    for xi, v in zip(x, vals):
        ax.annotate(f"{v:.2f}", (xi, v), textcoords="offset points", xytext=(0, 3), ha="center", fontsize=9)
    ax.axhline(scene_base["ft"], ls="--", lw=0.8, color="#555555")
    ax.annotate(f"空展示框基线 {scene_base['ft']:.2f}", (len(vals) - 0.45, scene_base["ft"]),
                xytext=(0, -12), textcoords="offset points", ha="right", fontsize=8, color="#555555")
    ax.set_xticks(x)
    ax.set_xticklabels(labels, fontsize=9)
    ax.set_ylabel("平均帧时间 (ms)")
    ax.set_title("A. 无光影：9 个展示框（空手）", fontsize=11)
    ax.grid(axis="y", alpha=0.25)

    ax = axes[1]
    labels = ["烘焙关", "CGC B1-a", "TaCZ 1.2.0", "TaCZ + AR"]
    vals = [ab_sh["ft"], post_sh["ft"], ta1[2]["ft"], ta2[2]["ft"]]
    errs = [ab_sh["ft_sd"], post_sh["ft_sd"], ta1[2]["ft_sd"], ta2[2]["ft_sd"]]
    colors = ["#c98a2a", "#b8442f", "#2f6db3", "#6a9a3f"]
    x = np.arange(len(vals))
    ax.bar(x, vals, 0.55, yerr=errs, capsize=2, color=colors, error_kw=dict(lw=0.8))
    for xi, v in zip(x, vals):
        ax.annotate(f"{v:.2f}", (xi, v), textcoords="offset points", xytext=(0, 3), ha="center", fontsize=9)
    ax.set_xticks(x)
    ax.set_xticklabels(labels, fontsize=9)
    ax.set_ylabel("平均帧时间 (ms)")
    ax.set_title("B. 开光影·空手：烘焙关 vs 三端", fontsize=11)
    ax.grid(axis="y", alpha=0.25)

    fig.tight_layout(rect=[0, 0, 1, 0.94])
    out = os.path.join(BASE, "测试数据总览-1-帧时间.png")
    fig.savefig(out, dpi=130)
    plt.close(fig)
    print(f"\n图已生成：{out}")


if __name__ == "__main__":
    main()
