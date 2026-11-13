# -*- coding: utf-8 -*-
"""画「固定毫秒开销」对不同基础帧率的影响（光照解耦 A/B 取舍用）。

横轴：假设渲染重构已经把光照开销完全去掉后的基础帧率（连续，对数刻度）。
曲线一：基础帧率本身。
曲线二：同一帧再叠加一次固定的 Δ 毫秒（一次光照变化的剩余开销）后的瞬时帧率，
        即 1000 / (1000 / fps + Δ)。

数据口径见 ./lighting-decoupling.md 与 ../benchmark/26.35.11.1-6/测试数据计算报告.md。
"""
import os

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib import font_manager
import numpy as np

BASE = os.path.dirname(os.path.abspath(__file__))

# 一次光照变化的固定开销（ms）：A 之后 B 仍要多传的部分（满配+光影约 0.45 ms）
DELTA_MS = 0.45
# 对比标注用的参考帧率
REFERENCE_FPS = (60, 90, 120, 144, 165, 240)
# 超过这个帧率，固定开销对人眼没有意义
IRRELEVANT_FPS = 300


def setup_font():
    for f in (r"C:\Windows\Fonts\msyh.ttc", r"C:\Windows\Fonts\simhei.ttf"):
        if os.path.exists(f):
            font_manager.fontManager.addfont(f)
    plt.rcParams["font.sans-serif"] = ["Microsoft YaHei", "SimHei", "DejaVu Sans"]
    plt.rcParams["axes.unicode_minus"] = False
    plt.rcParams["axes.spines.top"] = False
    plt.rcParams["axes.spines.right"] = False


def with_overhead(fps, delta_ms):
    return 1000.0 / (1000.0 / fps + delta_ms)


def main():
    setup_font()
    xs = np.geomspace(60, 1200, 800)

    fig, ax = plt.subplots(figsize=(10.5, 6.4))
    ax.plot(xs, xs, color="#9aa0a6", linewidth=2, label="无额外开销（A 已完全去掉）")
    ax.plot(xs, with_overhead(xs, DELTA_MS), color="#1a73e8", linewidth=2.4,
            label=f"叠加固定 {DELTA_MS:g} ms/次")

    ax.axvspan(IRRELEVANT_FPS, xs[-1], color="#e8f0fe", alpha=0.7, zorder=0)
    ax.text(520, 95, ">300 FPS：该开销对人眼意义不大", color="#5f6368", fontsize=11)

    for fps in REFERENCE_FPS:
        y = float(with_overhead(fps, DELTA_MS))
        ax.axvline(fps, color="#dadce0", linestyle="--", linewidth=1, zorder=1)
        ax.scatter([fps], [y], color="#1a73e8", s=26, zorder=3)
        ax.annotate(f"{y:.0f} FPS（{y / fps * 100 - 100:+.1f}%）",
                    xy=(fps, y), xytext=(6, -14), textcoords="offset points",
                    fontsize=9, color="#1a73e8")
        ax.text(fps, 1.03, f"{fps}", transform=ax.get_xaxis_transform(),
                ha="center", va="bottom", fontsize=9, color="#5f6368")

    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_xlim(xs[0], xs[-1])
    ax.set_ylim(50, 1300)
    ax.set_xticks([60, 90, 120, 144, 165, 240, 300, 600, 1000])
    ax.set_xticklabels(["60", "90", "120", "144", "165", "240", "300", "600", "1000"])
    ax.set_yticks([50, 60, 90, 120, 144, 165, 240, 300, 600, 1000])
    ax.set_yticklabels(["50", "60", "90", "120", "144", "165", "240", "300", "600", "1000"])
    ax.set_xlabel("去掉光照开销后的基础帧率（FPS）")
    ax.set_ylabel("该帧叠加一次固定开销后的瞬时帧率（FPS）")
    ax.set_title(f"固定 {DELTA_MS:g} ms/次的光照开销对不同基础帧率的影响", fontsize=13)
    ax.grid(True, which="both", color="#f1f3f4", linewidth=0.8)
    ax.legend(loc="upper left", frameon=False)

    out = os.path.join(BASE, "光照解耦-固定开销对帧率.png")
    fig.tight_layout()
    fig.savefig(out, dpi=160)
    print("written:", out)
    for fps in REFERENCE_FPS + (1000,):
        y = float(with_overhead(fps, DELTA_MS))
        print(f"  {fps:>5} FPS -> {y:7.1f} FPS ({y / fps * 100 - 100:+.1f}%)")


if __name__ == "__main__":
    main()
