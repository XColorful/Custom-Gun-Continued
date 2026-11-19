# -*- coding: utf-8 -*-
"""生成《渲染优化说明》（wiki 美术页）用的五张图。

数据全部取自 docs/architecture/client/render 下的基准测试结论，不重新解析原始数据：
- 渲染开销组成分析：benchmark/26.11.35.1-1 第 2 节（1.20.1 / 26.3 各 6 次重复，第一人称裸配）
- 静态烘焙优化测量：benchmark/26.11.35.1-2 第 4 节（重构前）与 26.11.35.1-4 第 1–4 节（B1-a）
- 光照影响测量：benchmark/26.11.35.1-5/b1-b-3（解耦前）与 26.11.35.1-6/b1-c-b（解耦后）
- 脏子树测量：benchmark/26.11.35.1-4/b1-a-8（换弹 p99 脏 cube 占比）
- 26.11.36性能测试：benchmark/26.11.35.1-9（CGC 26.11.36 vs TaCZ 1.2.0）

运行：python plot_artist_rendering.py（输出到本目录）
"""
import os
import sys

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib import font_manager
from matplotlib.patches import Patch

sys.stdout.reconfigure(encoding="utf-8")
BASE = os.path.dirname(os.path.abspath(__file__))

CGC = "#b8442f"          # 主色：几何遍历 / 优化后
TACZ = "#2f6db3"
GRAY = "#8c8c8c"         # 优化前
FLUSH = "#d08a63"
DELEGATE = "#2f6db3"
STATE = "#4c9a8a"
SNAPSHOT = "#9aa7b5"
DATAREF = "#c9ced4"
SUBMIT = "#7d6aa8"
SKIP = "#b9cdd8"


def setup():
    for f in (r"C:\Windows\Fonts\msyh.ttc", r"C:\Windows\Fonts\simhei.ttf"):
        if os.path.exists(f):
            font_manager.fontManager.addfont(f)
    plt.rcParams["font.sans-serif"] = ["Microsoft YaHei", "SimHei", "DejaVu Sans"]
    plt.rcParams["axes.unicode_minus"] = False
    plt.rcParams["axes.spines.top"] = False
    plt.rcParams["axes.spines.right"] = False


def save(fig, name):
    out = os.path.join(BASE, name)
    fig.savefig(out, dpi=130)
    plt.close(fig)
    print("图已生成：", out)


# ---------------------------------------------------------------- 图 1
def fig_cost_breakdown():
    setup()
    rows = [
        ("1.20.1（Forge）", [
            ("几何遍历", 0.3108, CGC, "white"),
            ("模型flush", 0.1180, FLUSH, "black"),
            ("delegate", 0.0843, DELEGATE, "white"),
            ("状态更新", 0.0332, STATE, "white"),
            ("节点快照", 0.0103, SNAPSHOT, "black"),
            ("数据刷新", 0.0030, DATAREF, "black"),
        ]),
        ("26.3（NeoForge）", [
            ("几何遍历", 0.2252, CGC, "white"),
            ("模型flush", 0.0051, FLUSH, "black"),
            ("状态更新", 0.0338, STATE, "white"),
            ("模型提交", 0.0092, SUBMIT, "white"),
            ("数据刷新", 0.0043, DATAREF, "black"),
            ("delegate", 0.0010, DELEGATE, "white"),
        ]),
    ]
    fig, ax = plt.subplots(figsize=(11.6, 4.6))
    for y, (label, segs) in enumerate(rows):
        left = 0.0
        for name, value, color, tcolor in segs:
            ax.barh(y, value, 0.42, left=left, color=color, edgecolor="white", lw=0.6)
            if value >= 0.03:
                ax.text(left + value / 2, y, f"{value:.2f}", ha="center", va="center",
                        fontsize=9, color=tcolor)
            left += value
        ax.text(left + 0.012, y, f"合计 {left:.2f} ms", va="center", fontsize=9.5)
    ax.set_yticks([0, 1])
    ax.set_yticklabels([r[0] for r in rows], fontsize=10.5)
    ax.set_ylim(-0.75, 1.85)
    ax.set_xlim(0, 0.64)
    ax.set_xlabel("每帧耗时 (ms)", fontsize=9.5)
    ax.grid(axis="x", alpha=0.25)
    ax.set_title("一次第一人称持枪提交的开销组成（裸配，不开光影，n=6）",
                 loc="left", fontsize=12)
    ax.text(0, 1.53, "红色是逐 cube 重新计算顶点；26.3 的顶点生成与节点快照都在 flush 回调内，"
            "不能与 flush 重复相加", fontsize=8.8, color="#555555")
    fig.legend(handles=[
        Patch(color=CGC, label="几何遍历（逐 cube 算顶点）"),
        Patch(color=FLUSH, label="模型flush（提交/上传）"),
        Patch(color=DELEGATE, label="delegate（配件/手臂/文字）"),
        Patch(color=STATE, label="状态更新"),
        Patch(color=SNAPSHOT, label="节点快照"),
        Patch(color=SUBMIT, label="模型提交（26.3）"),
        Patch(color=DATAREF, label="数据刷新"),
    ], loc="lower center", ncol=4, frameon=False, fontsize=9)
    fig.tight_layout(rect=[0, 0.08, 1, 1])
    save(fig, "渲染开销组成分析.png")


# ---------------------------------------------------------------- 图 2
def fig_static_bake():
    setup()
    fig, axes = plt.subplots(1, 3, figsize=(12.4, 4.6))
    panels = [
        (axes[0], "9 把展示框（不开光影）", 5.449, 1.174, 0.86, "空场景基线 0.86",
         -78.5, "{:.3f}"),
        (axes[1], "单把展示框的成本（不开光影）", 0.511, 0.034, None, None,
         -93.3, "{:.3f}"),
        (axes[2], "9 把展示框（开光影）", 23.949, 7.193, 6.679, "空展示框基线 6.68",
         -70.0, "{:.2f}"),
    ]
    for ax, title, before, after, base, base_label, drop, fmt in panels:
        ax.barh([1, 0], [before, after], 0.5, color=[GRAY, CGC])
        vmax = before
        for y, v in ((1, before), (0, after)):
            ax.text(v + vmax * 0.02, y, fmt.format(v), va="center", fontsize=9.5)
        if base is not None:
            ax.axvline(base, color="#555555", ls="--", lw=1.0)
            ax.text(base, 1.52, base_label, fontsize=8.5, color="#555555", ha="center")
        ax.set_yticks([1, 0])
        ax.set_yticklabels(["优化前", "优化后"], fontsize=10)
        ax.set_ylim(-0.6, 1.7)
        ax.set_xlim(0, vmax * 1.22)
        ax.set_xlabel("帧时间 (ms)", fontsize=9)
        ax.set_title(title, loc="left", fontsize=10.5)
        ax.grid(axis="x", alpha=0.25)
        ax.text(0.98, 0.88, f"{drop:.1f}%", transform=ax.transAxes, ha="right",
                fontsize=13, color=CGC, weight="bold")
    fig.suptitle("静态烘焙前后（Afterburner，每档 4 次，n=4）", fontsize=12.5,
                 x=0.01, ha="left")
    fig.text(0.01, 0.015, "开光影时已贴近 GPU 下限：9 把枪 7.19 ms，空展示框 6.68 ms；"
             "关烘焙时为 23.95 ms（CPU 受限）", fontsize=8.5, color="#555555")
    fig.tight_layout(rect=[0, 0.06, 1, 0.94])
    save(fig, "静态烘焙优化测量.png")


# ---------------------------------------------------------------- 图 3
def fig_light():
    setup()
    fig, axes = plt.subplots(1, 2, figsize=(11.6, 4.4),
                             gridspec_kw={"width_ratios": [1.45, 1]})
    ax = axes[0]
    labels = ["平均帧率", "1% low 帧率"]
    before = [145.6, 59.1]
    after = [146.3, 130.5]
    x = [0, 1]
    ax.bar([i - 0.19 for i in x], before, 0.36, color=GRAY, label="光照解耦前")
    ax.bar([i + 0.19 for i in x], after, 0.36, color=CGC, label="光照解耦后")
    for i, (b, a) in enumerate(zip(before, after)):
        ax.text(i - 0.19, b + 4, f"{b:.1f}", ha="center", fontsize=9.5)
        ax.text(i + 0.19, a + 4, f"{a:.1f}", ha="center", fontsize=9.5)
    ax.set_xticks(x)
    ax.set_xticklabels(labels, fontsize=10, color="black")
    ax.set_ylabel("帧率 (FPS)", fontsize=9.5)
    ax.set_ylim(0, 200)
    ax.grid(axis="y", alpha=0.25)
    ax.set_title("满配 + 光影，夜晚逐格移动", loc="left", fontsize=10.5)
    ax.legend(frameon=False, fontsize=9, loc="upper left")

    ax = axes[1]
    ax.bar([0, 1], [9.246, 0.68], 0.45, color=[GRAY, CGC])
    for i, v in enumerate([9.246, 0.68]):
        ax.text(i, v + 0.25, f"{v:.2f} ms", ha="center", fontsize=10)
    ax.set_xticks([0, 1])
    ax.set_xticklabels(["解耦前", "解耦后"], fontsize=10, color="black")
    ax.set_ylabel("一次光照变化 (ms)", fontsize=9.5)
    ax.set_ylim(0, 10.8)
    ax.grid(axis="y", alpha=0.25)
    ax.set_title("跨过一次亮度边界时的单帧耗时", loc="left", fontsize=10.5)
    ax.text(1, 3.6, "−93%", ha="center", fontsize=12, color=CGC, weight="bold")

    fig.suptitle("光照变化对帧率的影响（照度改变不改变平均帧，只压低 1% low）",
                 fontsize=12.5, x=0.01, ha="left")
    fig.text(0.01, 0.015, "场景：夜晚、脚下火把、每跨一格方块光 −1，每秒约 2.9 次光照变化；"
             "解耦前 n=8，解耦后 n=1", fontsize=8.5, color="#555555")
    fig.tight_layout(rect=[0, 0.07, 1, 0.93])
    save(fig, "光照影响测量.png")


# ---------------------------------------------------------------- 图 4
def fig_dirty():
    setup()
    rows = [
        ("静止待机", 0.0),
        ("ak47 换弹", 44.0),
        ("m4a1 裸配 换弹", 20.7),
        ("m4a1 满配 换弹", 9.3),
    ]
    fig, ax = plt.subplots(figsize=(11.6, 3.9))
    for y, (label, dirty) in enumerate(rows):
        ax.barh(y, dirty, 0.5, color=CGC)
        ax.barh(y, 100 - dirty, 0.5, left=dirty, color=SKIP)
        if dirty > 0:
            ax.text(dirty / 2, y, f"{dirty:.0f}%", ha="center", va="center",
                    fontsize=9.5, color="white")
        ax.text(101.5, y, f"可跳过 {100 - dirty:.0f}%", va="center", fontsize=9.5,
                color="#2f6db3")
    ax.set_yticks(range(len(rows)))
    ax.set_yticklabels([r[0] for r in rows], fontsize=10.5)
    ax.invert_yaxis()
    ax.set_xlim(0, 122)
    ax.set_xticks([0, 25, 50, 75, 100])
    ax.set_xlabel("占全枪 cube 的比例 (%)", fontsize=9.5)
    ax.grid(axis="x", alpha=0.25)
    ax.set_title("换弹动画期间仍需重新计算的 cube 占比（p99，按 cube 计）",
                 loc="left", fontsize=12)
    fig.legend(handles=[Patch(color=CGC, label="需要重新计算"),
                        Patch(color=SKIP, label="走静态烘焙、不重新计算")],
               loc="lower center", bbox_to_anchor=(0.5, 0.085), ncol=2,
               frameon=False, fontsize=9)
    fig.text(0.01, 0.015, "静止时只有根节点在动，根变换由绘制矩阵承担，不触发重新计算；"
             "换弹是动态场景的上界", fontsize=8.5, color="#555555")
    fig.tight_layout(rect=[0, 0.17, 1, 1])
    save(fig, "脏子树测量.png")


# ---------------------------------------------------------------- 图 5
def fig_perf():
    setup()
    fig, axes = plt.subplots(1, 2, figsize=(12.2, 5.0))
    ax = axes[0]
    groups = [
        ("空手（基准）", 1283.0, 1268.5),
        ("第一人称持枪 ak47", 1017.7, 630.8),
        ("1% low 帧", 472.0, 334.0),
    ]
    for i, (label, c, t) in enumerate(groups):
        ax.barh(i - 0.19, c, 0.34, color=CGC)
        ax.barh(i + 0.19, t, 0.34, color=TACZ)
        ax.text(c + 14, i - 0.19, f"{c:.1f}", va="center", fontsize=9)
        ax.text(t + 14, i + 0.19, f"{t:.1f}", va="center", fontsize=9)
    ax.set_yticks(range(len(groups)))
    ax.set_yticklabels([g[0] for g in groups], fontsize=10)
    ax.invert_yaxis()
    ax.set_xlim(0, 1620)
    ax.set_xlabel("帧数 (FPS)", fontsize=9.5)
    ax.grid(axis="x", alpha=0.25)
    ax.set_title("帧数（越高越好）", loc="left", fontsize=11)

    ax = axes[1]
    groups = [
        ("空手（基准）", 0.780, 0.788, "{:.3f}"),
        ("第一人称持枪 ak47", 0.983, 1.586, "{:.3f}"),
        ("单枪渲染帧时间（增量）", 0.203, 0.798, "{:.3f}"),
        ("1% low 帧时间", 2.119, 2.994, "{:.3f}"),
    ]
    for i, (label, c, t, fmt) in enumerate(groups):
        ax.barh(i - 0.19, c, 0.34, color=CGC)
        ax.barh(i + 0.19, t, 0.34, color=TACZ)
        ax.text(c + 0.04, i - 0.19, fmt.format(c), va="center", fontsize=9)
        ax.text(t + 0.04, i + 0.19, fmt.format(t), va="center", fontsize=9)
    ax.set_yticks(range(len(groups)))
    ax.set_yticklabels([g[0] for g in groups], fontsize=10)
    ax.invert_yaxis()
    ax.set_xlim(0, 3.6)
    ax.set_xlabel("帧时间 (ms)", fontsize=9.5)
    ax.grid(axis="x", alpha=0.25)
    ax.set_title("帧时间（越低越好）——单枪渲染 −74.5%", loc="left", fontsize=11)

    fig.suptitle("CGC 26.11.36 vs TaCZ 1.2.0（第一人称 ak47，不开光影，n=4）",
                 fontsize=12.5, x=0.01, ha="left")
    fig.legend(handles=[Patch(color=CGC, label="CGC 26.11.36"),
                        Patch(color=TACZ, label="TaCZ 1.2.0")],
               loc="upper right", ncol=2, frameon=False, fontsize=10)
    fig.text(0.01, 0.015, "空手基线两端相等（0.8σ）；单枪渲染 = 持枪帧时间 − 空手帧时间；"
             "单枪整帧 1.61×、1% low 1.41×", fontsize=8.5, color="#555555")
    fig.tight_layout(rect=[0, 0.05, 1, 0.93])
    save(fig, "26.11.36性能测试.png")


if __name__ == "__main__":
    fig_cost_breakdown()
    fig_static_bake()
    fig_light()
    fig_dirty()
    fig_perf()
