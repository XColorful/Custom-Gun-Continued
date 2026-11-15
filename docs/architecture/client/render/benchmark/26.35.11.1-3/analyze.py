# -*- coding: utf-8 -*-
"""解析 26.35.11.1-3（满配枪械批次）的 CGC 双端基准数据。

数据来源（每个端 3 次重复）：
- `cgc1 (N).txt`                Afterburner，1.20.1 标准 3 步（空快捷栏 / 裸配 / 满配）
- `cgc1-builtin (N).txt`        内置探针，同上
- `cgc1-multi (N).txt`          Afterburner，1.20.1 multi 3 段（9 满配展示框；+裸配手持；+满配手持）
- `cgc1-multi-builtin (N).txt`  内置探针，同上
- `cgc2*`                       同上，NeoForge 26.3

与 26.35.11.1-2 的差异：
- 本批次枪械换成 tacz:m4a1，并新增“满配”（瞄具 + 激光 + 握把 + 枪口 + 枪托）场景；
  multi 的 9 个展示框也是满配，因此 multi 不能与上一批的 9 把裸枪直接比较。
- 本批次不测“切换槽位 / 清空”，多出的 1 段给了 multi 的满配。

分组口径：
- multi 三段按“手持帧 + 立方体数”区分：无手持帧 = 仅 9 展示框；
  手持帧中 `Cubes submitted` 小的是裸配、大的是满配。
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
NODES = ["cgc1", "cgc2"]
NODE_LABEL = {"cgc1": "1.20.1 (Forge)", "cgc2": "26.3 (NeoForge)"}
NODE_COLOR = {"cgc1": "#b8442f", "cgc2": "#2f6db3"}
STEPS = ["空快捷栏", "第一人称裸配", "第一人称满配"]

AF_REC = re.compile(
    r"(\d{2}-\d{2}-\d{4}, \d{2}:\d{2}:\d{2}) (\S+) benchmark completed, (\d+) frames rendered in ([\d.]+) s\s*\n"
    r"\s*Average framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Minimum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*Maximum framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*1% low framerate\s*:\s*([\d.]+) FPS\s*\n"
    r"\s*0\.1% low framerate\s*:\s*([\d.]+) FPS")

BT_HEAD = re.compile(
    r"(\d{2}-\d{2}-\d{4}, \d{2}:\d{2}:\d{2}) customgun render profile completed, (\d+) frames rendered in ([\d.]+) s")
BT_LINE = re.compile(r"^\s*(.+?)\s+:\s+(.+?)\s*$", re.M)
NUM = r"[+-]?\d+(?:\.\d+)?"

PROBE_KEYS = [
    "Hand event entry", "First-person submit", "World-item submit", "State update",
    "Data refresh", "Scope render", "Model render total", "Node snapshot",
    "Functional nodes", "Geometry traverse", "endBatch flush", "Delegate render",
    "Muzzle cache", "Stencil clear", "GUI slot icon", "Camera angles",
    "Hand camera", "FOV", "Model submit", "Model flush",
    "endBatch upload", "endBatch draw",
]
COUNTER_KEYS = [
    "First-person submissions", "World-item submissions", "endBatch calls",
    "Cubes submitted", "Nodes captured", "Geometry batches", "Stencil windows", "LOD hits",
    "Geometry submits", "Flush callbacks", "Draw calls", "Upload bytes", "Allocated bytes",
    "Animation channels", "State changes",
]
PROBE_CN = {
    "Hand event entry": "入口事件", "First-person submit": "第一人称提交",
    "World-item submit": "展示框提交", "State update": "状态更新",
    "Data refresh": "数据刷新", "Scope render": "瞄具模板",
    "Model render total": "模型渲染总计", "Node snapshot": "节点快照",
    "Functional nodes": "功能节点", "Geometry traverse": "几何遍历",
    "endBatch flush": "模型 flush", "Delegate render": "delegate",
    "Muzzle cache": "枪口缓存", "Stencil clear": "模板清空",
    "GUI slot icon": "GUI 图标", "Camera angles": "相机角度",
    "Hand camera": "手部相机", "FOV": "FOV",
    "endBatch upload": "flush 上传", "endBatch draw": "flush draw",
}
COUNTER_CN = {
    "First-person submissions": "第一人称提交", "World-item submissions": "展示框提交",
    "endBatch calls": "几何提交/endBatch", "Cubes submitted": "立方体数",
    "Nodes captured": "节点数", "Geometry batches": "几何批次",
    "Stencil windows": "模板窗口", "LOD hits": "LOD 命中",
    "Draw calls": "draw 数", "Upload bytes": "上传字节",
    "Allocated bytes": "分配字节", "Animation channels": "动画通道",
    "State changes": "状态变化",
}


def parse_afterburner(path):
    txt = open(path, encoding="utf-8", errors="replace").read()
    out = []
    for m in AF_REC.finditer(txt):
        ts, proc, frames, secs, avg, mn, mx, low1, low01 = m.groups()
        if proc.lower() != "javaw.exe":
            continue
        out.append(dict(ts=ts, frames=int(frames), seconds=float(secs),
                        avg=float(avg), mn=float(mn), mx=float(mx),
                        low1=float(low1), frame_ms=1000.0 / float(avg)))
    return out


def parse_builtin(path):
    txt = open(path, encoding="utf-8", errors="replace").read()
    heads = list(BT_HEAD.finditer(txt))
    blocks = []
    for i, h in enumerate(heads):
        end = heads[i + 1].start() if i + 1 < len(heads) else len(txt)
        fields = {m.group(1).strip(): m.group(2).strip() for m in BT_LINE.finditer(txt[h.end():end])}
        blk = dict(ts=h.group(1), frames=int(h.group(2)), seconds=float(h.group(3)))

        def ms(key):
            v = fields.get(key)
            if not v:
                return None
            m2 = re.search(rf"({NUM})\s*ms", v)
            return float(m2.group(1)) if m2 else None

        def per_frame(key):
            v = fields.get(key)
            if not v:
                return None
            m2 = re.search(rf"({NUM})\s*/\s*frame", v)
            return float(m2.group(1)) if m2 else None

        blk["avg_ms"] = ms("Average frame time")
        fps_m = re.search(rf"({NUM})\s*FPS", fields.get("Average frame time") or "")
        blk["avg_fps"] = float(fps_m.group(1)) if fps_m else None
        blk["p99_ms"] = ms("1% high frame time")
        blk["cgc_ms"] = ms("CGC render per frame")
        m3 = re.search(rf"({NUM})\s*%", fields.get("CGC render per frame") or "")
        blk["cgc_share"] = float(m3.group(1)) if m3 else None
        blk["counters"] = {k: per_frame(k) for k in COUNTER_KEYS}
        if blk["counters"].get("endBatch calls") is None:
            blk["counters"]["endBatch calls"] = blk["counters"].get("Geometry submits")
        blk["probes"] = {k: ms(k) for k in PROBE_KEYS}
        if blk["probes"].get("Model render total") is None:
            blk["probes"]["Model render total"] = blk["probes"].get("Model submit")
        if blk["probes"].get("endBatch flush") is None:
            blk["probes"]["endBatch flush"] = blk["probes"].get("Model flush")
        blk["fields"] = fields
        blocks.append(blk)
    return blocks


def classify_multi(af_list, bt_list):
    """把 multi 3 段分成：仅展示框 / 手持裸配 / 手持满配。

    multi 的立方体数包含 9 个展示框，不能用手持帧的绝对值判断裸配 / 满配；
    同一文件内三段的立方体数严格递增，按秩区分即可。
    """
    pairs = [dict(af=a, bt=b) for a, b in zip(af_list, bt_list)]
    for p in pairs:
        c = p["bt"]["counters"]
        p["fp"] = c.get("First-person submissions") or 0.0
        p["cubes"] = c.get("Cubes submitted") or 0.0
    with_hand = sorted([p for p in pairs if p["fp"] >= 0.5], key=lambda p: p["cubes"])
    assert len(with_hand) == 2, [p["fp"] for p in pairs]
    with_hand[0]["kind"] = "9 个展示框 + 手持裸配"
    with_hand[1]["kind"] = "9 个展示框 + 手持满配"
    for p in pairs:
        if p["fp"] < 0.5:
            p["kind"] = "仅 9 个展示框"
    order = ["仅 9 个展示框", "9 个展示框 + 手持裸配", "9 个展示框 + 手持满配"]
    return {k: [p for p in pairs if p["kind"] == k] for k in order}


def mean_sd(vals):
    vals = [v for v in vals if v is not None]
    if not vals:
        return (None, None)
    return (statistics.mean(vals), statistics.stdev(vals) if len(vals) > 1 else 0.0)


def classify_std(af_list, bt_list):
    """标准 3 步按立方体数区分：0 = 空快捷栏，约 750 = 裸配，>1200 = 满配。"""
    pairs = [dict(af=a, bt=b) for a, b in zip(af_list, bt_list)]
    for p in pairs:
        cubes = p["bt"]["counters"].get("Cubes submitted") or 0.0
        p["step"] = STEPS[0] if cubes < 1 else (STEPS[1] if cubes < 1200 else STEPS[2])
    # 记录顺序必须与步骤一致，否则说明采集有错位
    assert [p["step"] for p in pairs] == STEPS[:len(pairs)], [p["step"] for p in pairs]
    return pairs


def load():
    data = {}
    for node in NODES:
        std, multi = [], []
        for rep in range(1, 4):
            af = parse_afterburner(os.path.join(BASE, f"{node} ({rep}).txt"))
            bt = parse_builtin(os.path.join(BASE, f"{node}-builtin ({rep}).txt"))
            assert len(af) == len(bt) and 2 <= len(af) <= 3, (node, rep, len(af), len(bt))
            std.append(dict(rep=rep, pairs=classify_std(af, bt)))

            afm = parse_afterburner(os.path.join(BASE, f"{node}-multi ({rep}).txt"))
            btm = parse_builtin(os.path.join(BASE, f"{node}-multi-builtin ({rep}).txt"))
            assert len(afm) == 3 and len(btm) == 3, (node, rep, len(afm), len(btm))
            multi.append(dict(rep=rep, groups=classify_multi(afm, btm)))
        data[node] = dict(std=std, multi=multi)
    return data


def summarize(data):
    out = {}
    for node in NODES:
        std, multi = data[node]["std"], data[node]["multi"]
        by_step = {st: [p for s in std for p in s["pairs"] if p["step"] == st] for st in STEPS}
        rows = []
        for st in STEPS:
            ps = by_step[st]
            af = [p["af"] for p in ps]
            bt = [p["bt"] for p in ps]
            af_ft = mean_sd([v["frame_ms"] for v in af])
            bt_ft = mean_sd([v["avg_ms"] for v in bt])
            rows.append(dict(
                n=len(ps),
                af_fps=mean_sd([v["avg"] for v in af]),
                af_ft=af_ft,
                af_low1=mean_sd([v["low1"] for v in af]),
                bt_ft=bt_ft,
                bt_p99=mean_sd([v["p99_ms"] for v in bt]),
                bt_cgc=mean_sd([v["cgc_ms"] for v in bt]),
                bt_share=mean_sd([v["cgc_share"] for v in bt]),
                overhead=(bt_ft[0] - af_ft[0]) if (bt_ft[0] is not None and af_ft[0] is not None) else None,
            ))
        base_af = rows[0]["af_ft"][0]
        for r in rows:
            r["af_dft"] = r["af_ft"][0] - base_af

        mg = {}
        for kind in ["仅 9 个展示框", "9 个展示框 + 手持裸配", "9 个展示框 + 手持满配"]:
            ps = [p for s in multi for p in s["groups"][kind]]
            mg[kind] = dict(
                n=len(ps),
                af_fps=mean_sd([p["af"]["avg"] for p in ps]),
                af_ft=mean_sd([p["af"]["frame_ms"] for p in ps]),
                af_low1=mean_sd([p["af"]["low1"] for p in ps]),
                bt_ft=mean_sd([p["bt"]["avg_ms"] for p in ps]),
                bt_cgc=mean_sd([p["bt"]["cgc_ms"] for p in ps]),
                bt_share=mean_sd([p["bt"]["cgc_share"] for p in ps]),
                counters={k: mean_sd([p["bt"]["counters"].get(k) for p in ps]) for k in COUNTER_KEYS},
                probes={k: mean_sd([p["bt"]["probes"].get(k) for p in ps]) for k in PROBE_KEYS},
            )

        probes = {k: dict(
            std_bare=mean_sd([p["bt"]["probes"].get(k) for p in by_step[STEPS[1]]]),
            std_full=mean_sd([p["bt"]["probes"].get(k) for p in by_step[STEPS[2]]]),
            multi_frame=mg["仅 9 个展示框"]["probes"].get(k),
            multi_full_hand=mg["9 个展示框 + 手持满配"]["probes"].get(k),
        ) for k in PROBE_KEYS}
        counters = {k: dict(
            std_bare=mean_sd([p["bt"]["counters"].get(k) for p in by_step[STEPS[1]]]),
            std_full=mean_sd([p["bt"]["counters"].get(k) for p in by_step[STEPS[2]]]),
            multi_frame=mg["仅 9 个展示框"]["counters"].get(k),
            multi_bare_hand=mg["9 个展示框 + 手持裸配"]["counters"].get(k),
            multi_full_hand=mg["9 个展示框 + 手持满配"]["counters"].get(k),
        ) for k in COUNTER_KEYS}
        out[node] = dict(rows=rows, mg=mg, probes=probes, counters=counters, std=std)
    return out


def f2(t, n=4):
    return "-" if t is None or t[0] is None else f"{t[0]:.{n}f}±{t[1]:.{n}f}"


def f1(t, n=2):
    return "-" if t is None or t[0] is None else f"{t[0]:.{n}f}"


def print_summary(s):
    for node in NODES:
        d = s[node]
        print(f"\n===== {NODE_LABEL[node]} · 标准 3 步（最多 3 次重复）=====")
        print("步骤 | n | AF FPS | AF 帧时间 | 内置帧时间 | 采集开销 | CGC | 占比 | Δft(AF) | 1%low | p99")
        for i, r in enumerate(d["rows"]):
            print(f"{STEPS[i]} | {r['n']} | {f1(r['af_fps'],1)} | {f2(r['af_ft'])} | {f2(r['bt_ft'])} | "
                  f"{r['overhead']:+.4f} | {f1(r['bt_cgc'])} | {f1(r['bt_share'],1)}% | "
                  f"{r['af_dft']:+.4f} | {f1(r['af_low1'],1)} | {f2(r['bt_p99'])}")

        print(f"\n----- {NODE_LABEL[node]} · 持枪段探针（裸配 / 满配）-----")
        for k in PROBE_KEYS:
            if k not in PROBE_CN:
                continue
            a = d["probes"][k]["std_bare"]
            b = d["probes"][k]["std_full"]
            if (a and a[0] is not None) or (b and b[0] is not None):
                print(f"  {PROBE_CN[k]:16s} {f2(a):>16s} {f2(b):>16s}  Δ {(b[0]-a[0]):+.4f}"
                      if a and b and a[0] is not None and b[0] is not None
                      else f"  {PROBE_CN[k]:16s} {f2(a):>16s} {f2(b):>16s}")

        print(f"\n----- {NODE_LABEL[node]} · 计数器 -----")
        for k in COUNTER_KEYS:
            if k not in COUNTER_CN:
                continue
            c = d["counters"][k]
            print(f"  {COUNTER_CN[k]:14s} 标准裸配 {f1(c['std_bare'],2):>10s} | 标准满配 {f1(c['std_full'],2):>10s} | "
                  f"multi 9 框 {f1(c['multi_frame'],2):>10s} | multi+裸配 {f1(c['multi_bare_hand'],2):>10s} | "
                  f"multi+满配 {f1(c['multi_full_hand'],2):>10s}")

        print(f"\n----- {NODE_LABEL[node]} · multi -----")
        for kind, g in d["mg"].items():
            print(f"  {kind}: n={g['n']} AF {f1(g['af_fps'],1)} FPS ({f2(g['af_ft'])}) "
                  f"内置 {f2(g['bt_ft'])} CGC {f2(g['bt_cgc'])} ({f1(g['bt_share'],1)}%)")
        base = d["mg"]["仅 9 个展示框"]
        for kind in ("9 个展示框 + 手持裸配", "9 个展示框 + 手持满配"):
            g = d["mg"][kind]
            print(f"  {kind} − 仅 9 框：帧时间 {g['af_ft'][0]-base['af_ft'][0]:+.3f} ms, "
                  f"CGC {g['bt_cgc'][0]-base['bt_cgc'][0]:+.3f} ms")

    # 跨端：满配 / 裸配 的增量
    print("\n===== 满配相对裸配的增量（标准持枪，ms）=====")
    print(f"{'探针':16s} {'1.20.1':>10s} {'26.3':>10s}")
    for k in PROBE_KEYS:
        a = s["cgc1"]["probes"][k]
        b = s["cgc2"]["probes"][k]
        if a["std_full"][0] is None or a["std_bare"][0] is None:
            continue
        da = a["std_full"][0] - a["std_bare"][0]
        db = (b["std_full"][0] - b["std_bare"][0]) if (b["std_full"][0] is not None and b["std_bare"][0] is not None) else None
        print(f"{PROBE_CN[k]:16s} {da:+10.4f} {('%+10.4f' % db) if db is not None else '         -'}")


def setup_font():
    for f in (r"C:\Windows\Fonts\msyh.ttc", r"C:\Windows\Fonts\simhei.ttf"):
        if os.path.exists(f):
            font_manager.fontManager.addfont(f)
    plt.rcParams["font.sans-serif"] = ["Microsoft YaHei", "SimHei", "DejaVu Sans"]
    plt.rcParams["axes.unicode_minus"] = False
    plt.rcParams["axes.spines.top"] = False
    plt.rcParams["axes.spines.right"] = False


def fig_standard(s, out):
    setup_font()
    fig, axes = plt.subplots(1, 2, figsize=(14, 5.4))
    fig.suptitle("标准场景（3 次重复）：空手 / 裸配 / 满配", fontsize=14, y=0.99)
    x = np.arange(3)
    w = 0.36
    ax = axes[0]
    for k, node in enumerate(NODES):
        vals = [s[node]["rows"][i]["af_ft"][0] for i in range(3)]
        errs = [s[node]["rows"][i]["af_ft"][1] for i in range(3)]
        ax.bar(x + (k - 0.5) * w, vals, w, label=NODE_LABEL[node], color=NODE_COLOR[node],
               yerr=errs, capsize=2, error_kw=dict(lw=0.8))
        for xi, v in zip(x + (k - 0.5) * w, vals):
            ax.annotate(f"{v:.2f}", (xi, v), textcoords="offset points", xytext=(0, 3), ha="center", fontsize=8)
    ax.set_xticks(x); ax.set_xticklabels(STEPS); ax.set_ylabel("平均帧时间 (ms)")
    ax.set_title("A. 帧时间（±1 样本标准差）", fontsize=11); ax.legend(fontsize=9); ax.grid(axis="y", alpha=0.25)

    ax = axes[1]
    for k, node in enumerate(NODES):
        vals = [s[node]["rows"][i]["bt_cgc"][0] for i in range(3)]
        ax.bar(x + (k - 0.5) * w, vals, w, label=NODE_LABEL[node], color=NODE_COLOR[node])
        for xi, v in zip(x + (k - 0.5) * w, vals):
            ax.annotate(f"{v:.2f}", (xi, v), textcoords="offset points", xytext=(0, 3), ha="center", fontsize=8)
    ax.set_xticks(x); ax.set_xticklabels(STEPS); ax.set_ylabel("CGC 每帧耗时 (ms)")
    ax.set_title("B. CGC 探针", fontsize=11); ax.legend(fontsize=9); ax.grid(axis="y", alpha=0.25)
    fig.tight_layout(rect=[0, 0, 1, 0.94]); fig.savefig(out, dpi=130); plt.close(fig)


def fig_multi(s, out):
    setup_font()
    kinds = ["仅 9 个展示框", "9 个展示框 + 手持裸配", "9 个展示框 + 手持满配"]
    labels = ["仅 9 框（满配）", "9 框 + 手持裸配", "9 框 + 手持满配"]
    fig, axes = plt.subplots(1, 2, figsize=(14, 5.4))
    fig.suptitle("multi：9 个满配展示框（± 手持枪）", fontsize=14, y=0.99)
    x = np.arange(3); w = 0.36
    ax = axes[0]
    for k, node in enumerate(NODES):
        vals = [s[node]["mg"][g]["af_ft"][0] for g in kinds]
        errs = [s[node]["mg"][g]["af_ft"][1] for g in kinds]
        ax.bar(x + (k - 0.5) * w, vals, w, label=NODE_LABEL[node], color=NODE_COLOR[node],
               yerr=errs, capsize=2, error_kw=dict(lw=0.8))
        for xi, v in zip(x + (k - 0.5) * w, vals):
            ax.annotate(f"{v:.1f}", (xi, v), textcoords="offset points", xytext=(0, 3), ha="center", fontsize=8)
    ax.set_xticks(x); ax.set_xticklabels(labels, fontsize=9); ax.set_ylabel("平均帧时间 (ms)")
    ax.set_title("A. 帧时间（±1 样本标准差）", fontsize=11); ax.legend(fontsize=9); ax.grid(axis="y", alpha=0.25)

    ax = axes[1]
    for k, node in enumerate(NODES):
        vals = [s[node]["mg"][g]["bt_cgc"][0] for g in kinds]
        ax.bar(x + (k - 0.5) * w, vals, w, label=NODE_LABEL[node], color=NODE_COLOR[node])
        for xi, v in zip(x + (k - 0.5) * w, vals):
            ax.annotate(f"{v:.1f}", (xi, v), textcoords="offset points", xytext=(0, 3), ha="center", fontsize=8)
    ax.set_xticks(x); ax.set_xticklabels(labels, fontsize=9); ax.set_ylabel("CGC 每帧耗时 (ms)")
    ax.set_title("B. CGC 探针", fontsize=11); ax.legend(fontsize=9); ax.grid(axis="y", alpha=0.25)
    fig.tight_layout(rect=[0, 0, 1, 0.94]); fig.savefig(out, dpi=130); plt.close(fig)


def fig_premium(s, out):
    setup_font()
    keys = ["Scope render", "Geometry traverse", "endBatch flush", "Delegate render",
            "Node snapshot", "State update", "Stencil clear", "Data refresh"]
    fig, ax = plt.subplots(figsize=(10, 5.4))
    x = np.arange(len(keys)); w = 0.36
    for k, node in enumerate(NODES):
        vals = []
        for key in keys:
            a = s[node]["probes"][key]["std_bare"][0]
            b = s[node]["probes"][key]["std_full"][0]
            vals.append((b or 0) - (a or 0))
        ax.bar(x + (k - 0.5) * w, vals, w, label=NODE_LABEL[node], color=NODE_COLOR[node])
        for xi, v in zip(x + (k - 0.5) * w, vals):
            ax.annotate(f"{v:+.3f}", (xi, v), textcoords="offset points",
                        xytext=(0, 3 if v >= 0 else -10), ha="center", fontsize=7)
    ax.axhline(0, color="black", lw=0.8)
    ax.set_xticks(x); ax.set_xticklabels([PROBE_CN[k] for k in keys], fontsize=9)
    ax.set_ylabel("满配 − 裸配 (ms)")
    ax.set_title("满配带来的逐项增量（标准持枪；探针互相包含，不可相加）", fontsize=12)
    ax.legend(fontsize=9); ax.grid(axis="y", alpha=0.25)
    fig.tight_layout(); fig.savefig(out, dpi=130); plt.close(fig)


def main():
    data = load()
    s = summarize(data)
    print_summary(s)
    fig_standard(s, os.path.join(BASE, "测试数据总览-1-标准.png"))
    fig_multi(s, os.path.join(BASE, "测试数据总览-2-multi.png"))
    fig_premium(s, os.path.join(BASE, "测试数据总览-3-满配增量.png"))
    print("\n图已保存：测试数据总览-1-标准.png / -2-multi.png / -3-满配增量.png")


if __name__ == "__main__":
    main()
