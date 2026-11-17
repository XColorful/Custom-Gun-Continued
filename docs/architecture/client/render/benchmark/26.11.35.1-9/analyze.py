# -*- coding: utf-8 -*-
"""解析 26.11.35.1-9（CGC 26.11.36 vs TaCZ 1.2.0）的 Afterburner 数据。

数据来源（1.20.1 / Forge 47.4.23，两端都不开光影包）：
- `cgc1 (N).txt`   CGC，2 步：空手 / 第一人称 `tacz:ak47`，各 4 次
- `tacz1 (N).txt`  TaCZ 1.2.0，同一 2 步协议
- 测试环境与采集步骤见 `新宿决战.txt`

口径：帧时间 = 1000 / FPS；均值为重复样本的均值 ± 1 样本标准差。
每个数据文件 = 一次独立的游戏启动 + 进档，跨文件差异含进程级噪声。
「单枪渲染帧时间」= 同端同批（持枪帧时间 − 空手帧时间），即归因到枪械渲染的那部分。
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

AF_REC = re.compile(
    r"(\d{2}-\d{2}-\d{4}, \d{2}:\d{2}:\d{2}) (\S+) benchmark completed, (\d+) frames rendered in ([\d.]+) s\s*\n"
    r"\s*Average framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Minimum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Maximum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*1% low framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*0\.1% low framerate\s*:\s*([\d.]+) FPS")

STEPS = ["空手（基准）", "第一人称 ak47"]


def parse_af(path):
    with open(path, encoding="utf-8-sig", errors="replace") as f:
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


def group_af(pattern):
    files = sorted(
        (f for f in os.listdir(BASE) if re.fullmatch(pattern, f)),
        key=lambda f: int(re.search(r"\((\d+)\)", f).group(1)))
    buckets = [[] for _ in STEPS]
    for name in files:
        for i, rec in enumerate(parse_af(os.path.join(BASE, name))):
            if i < len(buckets):
                rec["file"] = name
                buckets[i].append(rec)
    return buckets


def stats(recs):
    if not recs:
        return None
    fps = [r["fps"] for r in recs]
    ft = [1000.0 / r["fps"] for r in recs]
    low1 = [r["low1"] for r in recs]
    low01 = [r["low01"] for r in recs]
    low1_ft = [1000.0 / v for v in low1]
    sd = statistics.stdev if len(recs) > 1 else (lambda _: 0.0)
    return {
        "n": len(recs),
        "fps": statistics.mean(fps), "fps_sd": sd(fps),
        "ft": statistics.mean(ft), "ft_sd": sd(ft),
        "low1": statistics.mean(low1), "low1_sd": sd(low1),
        "low1_ft": statistics.mean(low1_ft), "low1_ft_sd": sd(low1_ft),
        "low01": statistics.mean(low01), "low01_sd": sd(low01),
    }


def fmt(s):
    return (f"{s['fps']:7.1f} ± {s['fps_sd']:5.1f} FPS | {s['ft']:6.4f} ± {s['ft_sd']:6.4f} ms | "
            f"1% low {s['low1']:6.1f} ± {s['low1_sd']:4.1f} | 0.1% low {s['low01']:6.1f} | n={s['n']}")


def main():
    cgc = group_af(r"cgc1 \(\d+\)\.txt")
    tacz = group_af(r"tacz1 \(\d+\)\.txt")

    for title, buckets in (("CGC 26.11.36", cgc), ("TaCZ 1.2.0", tacz)):
        print(f"\n## {title}")
        for name, recs in zip(STEPS, buckets):
            s = stats(recs)
            print(f"  {name:<16} {fmt(s) if s else '—'}")

    cb, c1 = stats(cgc[0]), stats(cgc[1])
    tb, t1 = stats(tacz[0]), stats(tacz[1])

    # 单枪渲染帧时间：持枪帧时间 − 空手帧时间
    c_inc = c1["ft"] - cb["ft"]
    t_inc = t1["ft"] - tb["ft"]

    # 标准误：均值标准误合成
    def se(s, key):
        return (s[key] / (s["n"] ** 0.5)) if s["n"] > 1 else 0.0

    def combo(*terms):
        return sum(x * x for x in terms) ** 0.5

    se_base_ft = combo(se(cb, "ft_sd"), se(tb, "ft_sd"))
    se_gun_ft = combo(se(c1, "ft_sd"), se(t1, "ft_sd"))
    se_inc = combo(se(c1, "ft_sd"), se(cb, "ft_sd"), se(t1, "ft_sd"), se(tb, "ft_sd"))
    se_low1 = combo(se(c1, "low1_sd"), se(t1, "low1_sd"))
    se_fps = combo(se(cb, "fps_sd"), se(tb, "fps_sd"))

    print("\n## 四个指标（均值）")
    print(f"  基准帧数          CGC {cb['fps']:7.1f} / TaCZ {tb['fps']:7.1f} FPS"
          f"（差 {cb['fps'] - tb['fps']:+.1f}，{abs(cb['fps'] - tb['fps']) / se_fps:.1f}σ）")
    print(f"  单枪渲染帧数      CGC {c1['fps']:7.1f} / TaCZ {t1['fps']:7.1f} FPS"
          f"（{c1['fps'] / t1['fps']:.2f}×，+{100.0 * (c1['fps'] / t1['fps'] - 1):.1f}%）")
    print(f"  单枪帧时间        CGC {c1['ft']:6.4f} / TaCZ {t1['ft']:6.4f} ms"
          f"（差 {t1['ft'] - c1['ft']:.4f} ms，{abs(t1['ft'] - c1['ft']) / se_gun_ft:.1f}σ）")
    print(f"  1% low 帧         CGC {c1['low1']:7.1f} / TaCZ {t1['low1']:7.1f} FPS"
          f"（{c1['low1'] / t1['low1']:.2f}×，{abs(c1['low1'] - t1['low1']) / se_low1:.1f}σ）")

    print("\n## 派生")
    print(f"  基准帧时间        CGC {cb['ft']:.4f} / TaCZ {tb['ft']:.4f} ms"
          f"（差 {cb['ft'] - tb['ft']:+.4f} ms，{abs(cb['ft'] - tb['ft']) / se_base_ft:.1f}σ，噪声内）")
    print(f"  单枪渲染帧时间    CGC {c_inc:.4f} / TaCZ {t_inc:.4f} ms"
          f"（降低 {100.0 * (1 - c_inc / t_inc):.1f}%，成本比 {t_inc / c_inc:.2f}×，"
          f"差 {c_inc - t_inc:+.4f} ± {se_inc:.4f} ms，{abs(c_inc - t_inc) / se_inc:.1f}σ）")
    print(f"  单枪帧时间        {t1['ft'] / c1['ft']:.2f}×（整帧口径）")
    print(f"  1% low 帧         {c1['low1'] / t1['low1']:.2f}×；"
          f"1% low 帧时间 {1000 / c1['low1']:.4f} → {1000 / t1['low1']:.4f} ms"
          f"（降低 {100.0 * (1 - (1000 / c1['low1']) / (1000 / t1['low1'])):.1f}%）")
    print(f"  1% low 占均值     CGC {100.0 * c1['low1'] / c1['fps']:.1f}% / "
          f"TaCZ {100.0 * t1['low1'] / t1['fps']:.1f}%")

    plot(cb, tb, c1, t1, c_inc, t_inc)


def setup_font():
    for f in (r"C:\Windows\Fonts\msyh.ttc", r"C:\Windows\Fonts\simhei.ttf"):
        if os.path.exists(f):
            font_manager.fontManager.addfont(f)
    plt.rcParams["font.sans-serif"] = ["Microsoft YaHei", "SimHei", "DejaVu Sans"]
    plt.rcParams["axes.unicode_minus"] = False
    plt.rcParams["axes.spines.top"] = False
    plt.rcParams["axes.spines.right"] = False


CGC_COLOR = "#b8442f"
TACZ_COLOR = "#2f6db3"


def plot(cb, tb, c1, t1, c_inc, t_inc):
    setup_font()
    fig, axes = plt.subplots(3, 1, figsize=(12, 12.4))
    fig.suptitle("CGC 26.11.36 vs TaCZ 1.2.0（Afterburner，n=4，不开光影）",
                 fontsize=14, x=0.02, ha="left")

    def sd_sum(*vals):
        return sum(v * v for v in vals) ** 0.5

    def grouped(ax, groups, xlabel, title, fmt="{:.1f}"):
        """groups: [(标签, CGC 值, CGC 误差, TaCZ 值, TaCZ 误差)]，每个标签两条横条。"""
        n = len(groups)
        for i, (label, cv, ce, tv, te) in enumerate(groups):
            ax.barh(i - 0.19, cv, 0.34, xerr=ce, color=CGC_COLOR, capsize=2,
                    error_kw=dict(lw=0.8))
            ax.barh(i + 0.19, tv, 0.34, xerr=te, color=TACZ_COLOR, capsize=2,
                    error_kw=dict(lw=0.8))
            ax.annotate(fmt.format(cv), (cv, i - 0.19), xytext=(4, 0),
                        textcoords="offset points", va="center", fontsize=9)
            ax.annotate(fmt.format(tv), (tv, i + 0.19), xytext=(4, 0),
                        textcoords="offset points", va="center", fontsize=9)
        ax.set_yticks(range(n))
        ax.set_yticklabels([g[0] for g in groups], fontsize=10)
        ax.invert_yaxis()
        top = max(v + e for _, cv, ce, tv, te in groups for v, e in ((cv, ce), (tv, te)))
        ax.set_xlim(0, top * 1.3)
        ax.set_xlabel(xlabel, fontsize=9)
        ax.set_title(title, loc="left", fontsize=11)
        ax.grid(axis="x", alpha=0.25)
        for i in range(1, n):
            ax.axhline(i - 0.5, color="#dddddd", lw=0.6, zorder=0)

    grouped(axes[0], [
        ("基准帧数（空手）", cb["fps"], cb["fps_sd"], tb["fps"], tb["fps_sd"]),
        ("单枪渲染帧数（持枪 ak47）", c1["fps"], c1["fps_sd"], t1["fps"], t1["fps_sd"]),
        ("1% low 帧（持枪 ak47）", c1["low1"], c1["low1_sd"], t1["low1"], t1["low1_sd"]),
    ], "帧数 (FPS)", "A. 帧数（越高越好）")

    grouped(axes[1], [
        ("基准帧时间（空手）", cb["ft"], cb["ft_sd"], tb["ft"], tb["ft_sd"]),
        ("单枪帧时间（持枪 ak47）", c1["ft"], c1["ft_sd"], t1["ft"], t1["ft_sd"]),
        ("单枪渲染帧时间（增量）", c_inc, sd_sum(c1["ft_sd"], cb["ft_sd"]),
         t_inc, sd_sum(t1["ft_sd"], tb["ft_sd"])),
        ("1% low 帧时间（持枪 ak47）", c1["low1_ft"], c1["low1_ft_sd"],
         t1["low1_ft"], t1["low1_ft_sd"]),
    ], "帧时间 (ms)", f"B. 帧时间（越低越好）——单枪渲染帧时间降低 "
       f"{100.0 * (1 - c_inc / t_inc):.1f}%", fmt="{:.3f}")

    ax = axes[2]
    perf = [
        ("基准帧数", cb["fps"] / tb["fps"]),
        ("单枪整帧", c1["fps"] / t1["fps"]),
        ("1% low 帧", c1["low1"] / t1["low1"]),
        ("单枪渲染（成本倒数）", t_inc / c_inc),
    ]
    ys = list(range(len(perf)))
    ax.barh(ys, [v for _, v in perf], 0.5, color=CGC_COLOR)
    for y, (label, v) in zip(ys, perf):
        ax.annotate(f"{v:.2f}×", (v, y), xytext=(4, 0), textcoords="offset points",
                    va="center", fontsize=9)
    ax.axvline(1.0, color=TACZ_COLOR, ls="--", lw=1.2)
    ax.annotate("TaCZ 1.00×", (1.0, -0.62), xytext=(4, 0), textcoords="offset points",
                va="center", fontsize=9, color=TACZ_COLOR)
    ax.set_yticks(ys)
    ax.set_yticklabels([p[0] for p in perf], fontsize=10)
    ax.set_ylim(len(perf) - 0.5, -0.85)
    ax.set_xlim(0, max(v for _, v in perf) * 1.35)
    ax.set_xlabel("相对性能（倍）", fontsize=9)
    ax.set_title("C. 渲染性能（TaCZ = 1.00×，越高越好）", loc="left", fontsize=11)
    ax.grid(axis="x", alpha=0.25)

    from matplotlib.patches import Patch
    fig.legend(handles=[Patch(color=CGC_COLOR, label="CGC 26.11.36"),
                        Patch(color=TACZ_COLOR, label="TaCZ 1.2.0")],
               loc="upper right", ncol=2, frameon=False, fontsize=10)
    fig.tight_layout(rect=[0, 0, 1, 0.945])
    out = os.path.join(BASE, "测试数据总览-1-端间对比.png")
    fig.savefig(out, dpi=130)
    plt.close(fig)
    print(f"\n图已生成：{out}")


if __name__ == "__main__":
    main()
