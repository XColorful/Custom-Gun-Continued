# -*- coding: utf-8 -*-
"""画「B2 合批的固定节省」对不同基础帧率的影响。

横轴：未合批（B1）时的基础帧率。
曲线一：未合批本身。
曲线二：每帧固定省下 Δ 毫秒（9 个同款满配展示框的实测值）后的瞬时帧率，
        即 1000 / (1000 / fps - Δ)。

数据口径见 ./测试数据计算报告.md
（b2-2 满配 multi 空手档 AF 帧时间 2.146 → 1.725 ms，n=4；b1-a-5 对照）。
"""
import os

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib import font_manager
import numpy as np

BASE = os.path.dirname(os.path.abspath(__file__))

# 9 个同款满配展示框：空手档 2.146 ms → 1.725 ms
DELTA_MS = 0.421
# 实测点（未合批帧率, 合批后帧率）
MEASURED = (1000.0 / 2.146, 1000.0 / 1.725)
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


def with_saving(fps, delta_ms):
    return 1000.0 / (1000.0 / fps - delta_ms)


def main():
    setup_font()
    xs = np.geomspace(60, 1000, 800)

    fig, ax = plt.subplots(figsize=(10.5, 6.4))
    ax.plot(xs, xs, color="#9aa0a6", linewidth=2, label="未合批（B1）")
    ax.plot(xs, with_saving(xs, DELTA_MS), color="#188038", linewidth=2.4,
            label=f"合批后（每帧省 {DELTA_MS:g} ms）")

    ax.axvspan(IRRELEVANT_FPS, xs[-1], color="#e6f4ea", alpha=0.7, zorder=0)
    ax.text(430, 60, ">300 FPS：该收益对人眼意义不大", color="#5f6368", fontsize=11)

    for fps in REFERENCE_FPS:
        y = float(with_saving(fps, DELTA_MS))
        ax.axvline(fps, color="#dadce0", linestyle="--", linewidth=1, zorder=1)
        ax.scatter([fps], [y], color="#188038", s=26, zorder=3)
        ax.annotate(f"{y:.0f} FPS（{y - fps:+.0f}，{y / fps * 100 - 100:+.1f}%）",
                    xy=(fps, y), xytext=(6, -14), textcoords="offset points",
                    fontsize=9, color="#188038")
        ax.text(fps, 1.03, f"{fps}", transform=ax.get_xaxis_transform(),
                ha="center", va="bottom", fontsize=9, color="#5f6368")

    mx, my = MEASURED
    ax.scatter([mx], [my], color="#d93025", s=46, zorder=4)
    ax.annotate(f"本批满配 multi：{mx:.0f} → {my:.0f} FPS",
                xy=(mx, my), xytext=(-150, 18), textcoords="offset points",
                fontsize=10, color="#d93025",
                arrowprops=dict(arrowstyle="-", color="#d93025", linewidth=1))

    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_xlim(xs[0], xs[-1])
    ax.set_ylim(50, 1100)
    ax.set_xticks([60, 90, 120, 144, 165, 240, 300, 600, 1000])
    ax.set_xticklabels(["60", "90", "120", "144", "165", "240", "300", "600", "1000"])
    ax.set_yticks([50, 60, 90, 120, 144, 165, 240, 300, 600, 1000])
    ax.set_yticklabels(["50", "60", "90", "120", "144", "165", "240", "300", "600", "1000"])
    ax.set_xlabel("未合批（B1）的基础帧率（FPS）")
    ax.set_ylabel("合批后的瞬时帧率（FPS）")
    ax.set_title(f"9 个同款展示框：每帧固定省 {DELTA_MS:g} ms 对不同基础帧率的影响", fontsize=13)
    ax.grid(True, which="both", color="#f1f3f4", linewidth=0.8)
    ax.legend(loc="upper left", frameon=False)

    out = os.path.join(BASE, "B2收益-固定0.42ms对帧率的影响.png")
    fig.tight_layout()
    fig.savefig(out, dpi=160)
    print("written:", os.path.basename(out).encode("ascii","replace").decode())
    for fps in REFERENCE_FPS + (466, 1000):
        y = float(with_saving(fps, DELTA_MS))
        print(f"  {fps:>5} FPS -> {y:7.1f} FPS ({y / fps * 100 - 100:+.1f}%)")


if __name__ == "__main__":
    main()
