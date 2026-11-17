# -*- coding: utf-8 -*-
"""解析 Afterburner 基准数据，输出统计并绘制单张多子图总览。仅做纯数据计算。"""
import re, os, glob, statistics
import matplotlib
matplotlib.use("Agg")
from matplotlib import font_manager
import matplotlib.pyplot as plt
import numpy as np

BASE = os.path.dirname(os.path.abspath(__file__))
NODES = ["tacz1", "tacz2", "cgc1", "cgc2"]
STEPS = ["空快捷栏", "第一人称 ak47", "切换下一槽位", "清空 /clear"]
COLORS = {"tacz1": "#2f6db3", "tacz2": "#7fb3e6", "cgc1": "#b8442f", "cgc2": "#e8a090"}
REFRESH = 165.0
BUDGET_MS = 1000.0 / REFRESH

REC = re.compile(
    r"(\d+ frames) rendered in ([\d.]+) s\s*\n"
    r"\s*Average framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Minimum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Maximum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*1% low framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*0\.1% low framerate\s*:\s*([\d.]+) FPS")


def parse_file(path):
    txt = open(path, encoding="utf-8", errors="replace").read()
    out = []
    for m in REC.finditer(txt):
        f = [float(x) for x in m.groups()[1:]]
        out.append(dict(seconds=f[0], avg=f[1], mn=f[2], mx=f[3], low1=f[4], low01=f[5]))
    return out


def load():
    std, multi = {n: [] for n in NODES}, {n: None for n in NODES}
    for path in glob.glob(os.path.join(BASE, "*.txt")):
        name = os.path.basename(path)
        m = re.match(r"(tacz1|tacz2|cgc1|cgc2)(-multi)? \((\d)\)\.txt$", name)
        if not m or name == "标准.txt":
            continue
        node, is_multi, rep = m.group(1), bool(m.group(2)), int(m.group(3))
        recs = parse_file(path)
        if is_multi:
            multi[node] = recs
        else:
            std[node].append((rep, recs))
    for n in NODES:
        std[n].sort(key=lambda x: x[0])
        assert len(std[n]) == 3 and all(len(r) == 4 for _, r in std[n]), n
        assert len(multi[n]) == 2, n
    return std, multi


def mean_sd(vals):
    return statistics.mean(vals), (statistics.stdev(vals) if len(vals) > 1 else 0.0)


def summarize(std, multi):
    # per node/step: mean & sd of avg, low1, low01, frame time
    s = {}
    for n in NODES:
        s[n] = []
        for i in range(4):
            avgs = [r[i]["avg"] for _, r in std[n]]
            l1 = [r[i]["low1"] for _, r in std[n]]
            l01 = [r[i]["low01"] for _, r in std[n]]
            m, sd = mean_sd(avgs)
            s[n].append(dict(avg=m, sd=sd, low1=mean_sd(l1)[0], low01=mean_sd(l01)[0],
                             ft=1000.0 / m, rel=(m / mean_sd([r[0]["avg"] for _, r in std[n]])[0] - 1) * 100))
        base_ft = s[n][0]["ft"]
        for d in s[n]:
            d["dft"] = d["ft"] - base_ft
            d["headroom"] = BUDGET_MS / d["ft"]
    m = {}
    for n in NODES:
        g, c = multi[n]
        m[n] = dict(gun=g["avg"], clear=c["avg"], gun_low1=g["low1"], clear_low1=c["low1"],
                    gun_ft=1000.0 / g["avg"], clear_ft=1000.0 / c["avg"],
                    gun_headroom=BUDGET_MS / (1000.0 / g["avg"]),
                    clear_headroom=BUDGET_MS / (1000.0 / c["avg"]),
                    gun_max=g["mx"], clear_max=c["mx"])
    return s, m


def setup_font():
    for f in (r"C:\Windows\Fonts\msyh.ttc", r"C:\Windows\Fonts\simhei.ttf"):
        if os.path.exists(f):
            font_manager.fontManager.addfont(f)
    plt.rcParams["font.sans-serif"] = ["Microsoft YaHei", "SimHei", "DejaVu Sans"]
    plt.rcParams["axes.unicode_minus"] = False
    plt.rcParams["axes.spines.top"] = False
    plt.rcParams["axes.spines.right"] = False


def grouped_bar(ax, categories, values, errs=None, ylabel="", title="", fmt="{:.0f}",
                label_bbox=False):
    x = np.arange(len(categories))
    w = 0.8 / len(NODES)
    for k, n in enumerate(NODES):
        vals = values[n]
        ax.bar(x + (k - (len(NODES) - 1) / 2) * w, vals, w, label=n, color=COLORS[n],
               yerr=(errs[n] if errs else None), capsize=2, error_kw=dict(lw=0.8))
        if fmt:
            for xi, v in zip(x + (k - (len(NODES) - 1) / 2) * w, vals):
                bb = dict(boxstyle="round,pad=0.08", fc="white", ec="none", alpha=0.75) if label_bbox else None
                ax.annotate(fmt.format(v), (xi, v), textcoords="offset points",
                            xytext=(0, 2 if v >= 0 else -2), ha="center",
                            va="bottom" if v >= 0 else "top",
                            fontsize=5.5, rotation=90, bbox=bb)
    ax.set_xticks(x)
    ax.set_xticklabels(categories, fontsize=9)
    ax.set_ylabel(ylabel)
    ax.set_title(title, fontsize=11)


def plot(std, s, m, out_png):
    setup_font()
    fig, axes = plt.subplots(2, 3, figsize=(18, 10.5))
    fig.suptitle("TaCZ / CGC 四端基准测试纯数据总览（标准测试 3 次重复 + multi 1 次；单位 FPS）",
                 fontsize=15, y=0.98)

    # A. 标准测试平均帧率
    ax = axes[0, 0]
    ordered = {n: [d["avg"] for d in s[n]] for n in NODES}
    errs = {n: [d["sd"] for d in s[n]] for n in NODES}
    grouped_bar(ax, STEPS, ordered, errs, "平均帧率 (FPS)", "A. 标准测试：平均帧率（±1 样本标准差）")
    ax.legend(fontsize=8, ncol=2, loc="upper right")
    ax.set_ylim(0, max(max(v) for v in ordered.values()) * 1.25)
    ax.grid(axis="y", alpha=0.25)

    # B. 全部标准原始重复点
    ax = axes[0, 1]
    rng = np.random.default_rng(0)
    for k, n in enumerate(NODES):
        for i in range(4):
            xs = i + (k - 1.5) * 0.18
            for _, recs in std[n]:
                ax.scatter(xs + rng.uniform(-0.03, 0.03), recs[i]["avg"], s=12,
                           color=COLORS[n], alpha=0.85, zorder=3)
            ax.plot([xs - 0.06, xs + 0.06], [s[n][i]["avg"]] * 2, color="black", lw=1.6, zorder=4)
    ax.set_xticks(range(4))
    ax.set_xticklabels(STEPS, fontsize=9)
    ax.set_ylabel("平均帧率 (FPS)")
    ax.set_title("B. 标准测试全部 48 条原始记录（点=单次，横线=均值）", fontsize=11)
    ax.grid(axis="y", alpha=0.25)
    ax.legend([plt.Line2D([], [], color=COLORS[n], marker="o", ls="") for n in NODES],
              NODES, fontsize=8, ncol=2, loc="lower left")

    # C. 帧时间增量
    ax = axes[0, 2]
    cats = ["持枪", "切换槽位", "清空"]
    dvals = {n: [s[n][i]["dft"] for i in (1, 2, 3)] for n in NODES}
    grouped_bar(ax, cats, dvals, None, "相对空快捷栏的帧时间增量 (ms)",
                "C. 标准测试：每帧额外耗时（0 = 空手基线）", fmt="{:+.2f}", label_bbox=True)
    ax.axhline(0, color="black", lw=0.8)
    ax.legend(fontsize=8, ncol=2, loc="upper right")
    ax.grid(axis="y", alpha=0.25)

    # D. 非-multi 汇总 vs multi
    ax = axes[1, 0]
    cats = NODES
    vals = {k: [0] * 4 for k in ["标准·持枪", "标准·清空", "multi·持枪", "multi·清空"]}
    groups = ["标准·持枪", "标准·清空", "multi·持枪", "multi·清空"]
    xs = np.arange(len(NODES))
    w = 0.8 / 4
    gcolors = ["#4a4a4a", "#9a9a9a", "#2f6db3", "#b8442f"]
    for k, g in enumerate(groups):
        v = [s[n][1]["avg"] if g == "标准·持枪" else s[n][3]["avg"] if g == "标准·清空"
             else m[n]["gun"] if g == "multi·持枪" else m[n]["clear"] for n in NODES]
        bars = ax.bar(xs + (k - 1.5) * w, v, w, label=g, color=gcolors[k])
        for xi, vv in zip(xs + (k - 1.5) * w, v):
            ax.annotate(f"{vv:.0f}", (xi, vv), textcoords="offset points", xytext=(0, 2),
                        ha="center", fontsize=6.5, rotation=90)
    ax.axhline(REFRESH, color="green", ls="--", lw=1, label="165Hz 刷新率")
    ax.set_xticks(xs)
    ax.set_xticklabels(NODES)
    ax.set_ylabel("平均帧率 (FPS)")
    ax.set_title("D. 非-multi 汇总（标准测试）与 multi 对比", fontsize=11)
    ax.legend(fontsize=7.5, ncol=2, loc="upper right")
    ax.grid(axis="y", alpha=0.25)

    # E. 165Hz 预算余量
    ax = axes[1, 1]
    cats = ["标准\n空手", "标准\n持枪", "标准\n切换", "标准\n清空", "multi\n持枪", "multi\n清空"]
    vals = {n: [s[n][i]["headroom"] for i in range(4)] + [m[n]["gun_headroom"], m[n]["clear_headroom"]]
            for n in NODES}
    x = np.arange(len(cats))
    w = 0.8 / len(NODES)
    for k, n in enumerate(NODES):
        ax.bar(x + (k - 1.5) * w, vals[n], w, label=n, color=COLORS[n])
    ax.axhline(1.0, color="green", ls="--", lw=1, label="165Hz 帧预算")
    ax.set_xticks(x)
    ax.set_xticklabels(cats, fontsize=8.5)
    ax.set_ylabel("帧预算余量倍数")
    ax.set_title("E. 相对 165Hz 的帧预算余量（<1 = 低于刷新率）", fontsize=11)
    ax.legend(fontsize=8, ncol=3, loc="upper right")
    ax.grid(axis="y", alpha=0.25)

    # F. 1% low 与稳定性
    ax = axes[1, 2]
    l1 = {n: [d["low1"] for d in s[n]] for n in NODES}
    grouped_bar(ax, STEPS, l1, None, "1% low (FPS)", "F. 标准测试：1% low 平均帧率", fmt="{:.0f}")
    ax.legend(fontsize=8, ncol=2, loc="upper right")
    ax.grid(axis="y", alpha=0.25)
    cvs = []
    for n in NODES:
        for i in range(4):
            v = [r[i]["low01"] for _, r in std[n]]
            cvs.append(statistics.stdev(v) / statistics.mean(v) * 100)
    ax.text(0.02, 0.05, f"0.1% low 重复间 CV = {min(cvs):.0f}%–{max(cvs):.0f}%（不可用于端间比较）",
            transform=ax.transAxes, fontsize=8, color="#b8442f",
            bbox=dict(boxstyle="round,pad=0.2", fc="white", ec="none", alpha=0.85))

    fig.tight_layout(rect=[0, 0, 1, 0.96])
    fig.savefig(out_png, dpi=130)
    plt.close(fig)


def print_summary(s, m):
    print("node  step                avg±sd        rel%    ft(ms)  Δft(ms)  headroom  1%low")
    for n in NODES:
        for i, d in enumerate(s[n]):
            print(f"{n:5s} {STEPS[i]:18s} {d['avg']:7.1f}±{d['sd']:5.1f} {d['rel']:+7.2f} "
                  f"{d['ft']:7.4f} {d['dft']:+8.4f} {d['headroom']:8.2f} {d['low1']:7.1f}")
    print("\nMULTI (n=1)")
    for n in NODES:
        d = m[n]
        print(f"{n}: gun {d['gun']:.1f} ({d['gun_ft']:.3f}ms, {d['gun_headroom']:.2f}x, max {d['gun_max']:.1f}) "
              f"| clear {d['clear']:.1f} ({d['clear_ft']:.3f}ms, {d['clear_headroom']:.2f}x, max {d['clear_max']:.1f}) "
              f"| clear vs gun {d['clear'] / d['gun'] * 100 - 100:+.1f}% "
              f"| gun vs std {d['gun'] / s[n][1]['avg'] * 100 - 100:+.1f}%")


def main():
    std, multi = load()
    s, m = summarize(std, multi)
    print_summary(s, m)
    out = os.path.join(BASE, "测试数据总览.png")
    plot(std, s, m, out)
    print("\n图已保存:", out)


if __name__ == "__main__":
    main()
