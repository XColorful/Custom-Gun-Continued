# -*- coding: utf-8 -*-
"""解析本批次的扩展探针（upload/draw 拆分、GPU 忙时、分配、draw 数），只打印不绘图。

用法：把本目录里所有 `*.txt` 采样文件放在同目录，运行 `python analyze_extra.py`。
每个文件里的每个采集区块输出一行；缺失字段打印 "-"。
"""
import os
import re
import sys

sys.stdout.reconfigure(encoding="utf-8")

BASE = os.path.dirname(os.path.abspath(__file__))
HEAD = re.compile(
    r"(\d{2}-\d{2}-\d{4}, \d{2}:\d{2}:\d{2}) customgun render profile completed, (\d+) frames rendered in ([\d.]+) s")
LINE = re.compile(r"^\s*(.+?)\s+:\s+(.+?)\s*$", re.M)
NUM = r"[+-]?\d+(?:\.\d+)?"

PROBES = ["endBatch flush", "endBatch upload", "endBatch draw",
          "Geometry traverse", "Model render total", "Model flush", "Model submit",
          "Delegate render", "State update"]
COUNTERS = ["endBatch calls", "Draw calls", "Geometry batches", "Cubes submitted",
            "Upload bytes", "Allocated bytes", "Animation channels", "State changes",
            "First-person submissions", "World-item submissions",
            "Geometry submits", "Flush callbacks"]


def ms(fields, key):
    v = fields.get(key)
    if not v:
        return None
    m = re.search(rf"({NUM})\s*ms", v)
    return float(m.group(1)) if m else None


def per_frame(fields, key):
    v = fields.get(key)
    if not v:
        return None
    m = re.search(rf"({NUM})\s*/\s*frame", v)
    return float(m.group(1)) if m else None


def share(fields, key):
    v = fields.get(key)
    if not v:
        return None
    found = re.findall(rf"({NUM})\s*%", v)
    # GPU 行有两个百分比：先「of frames」（覆盖率），后「of frame」（占帧比），取后者
    return float(found[-1]) if found else None


def fmt(v, digits=3):
    return "-" if v is None else f"{v:.{digits}f}"


def main():
    files = sorted(f for f in os.listdir(BASE) if f.endswith(".txt") and f != "标准.txt")
    if not files:
        print("本目录没有采样文件")
        return

    print("文件 | 区块 | 帧时间ms | CGCms | flush | upload | draw | 上传占比 | draws | 上传MB/帧 | 分配MB/帧 | GPUms | GPU占帧% | cubes | channels | state变化")
    for name in files:
        txt = open(os.path.join(BASE, name), encoding="utf-8", errors="replace").read()
        heads = list(HEAD.finditer(txt))
        for i, h in enumerate(heads):
            end = heads[i + 1].start() if i + 1 < len(heads) else len(txt)
            fields = {m.group(1).strip(): m.group(2).strip()
                      for m in LINE.finditer(txt[h.end():end])}

            frame_ms = ms(fields, "Average frame time")
            cgc = ms(fields, "CGC render per frame")
            flush = ms(fields, "endBatch flush")
            if flush is None:
                flush = ms(fields, "Model flush")
            upload = ms(fields, "endBatch upload")
            draw = ms(fields, "endBatch draw")
            upload_share = (upload / flush * 100.0) if (upload is not None and flush) else None

            draws = per_frame(fields, "Draw calls")
            upload_bytes = per_frame(fields, "Upload bytes")
            alloc = per_frame(fields, "Allocated bytes")
            channels = per_frame(fields, "Animation channels")
            state_changes = per_frame(fields, "State changes")
            cubes = per_frame(fields, "Cubes submitted")
            gpu = ms(fields, "GPU frame busy")
            gpu_share = share(fields, "GPU frame busy")

            print(f"{name} | {h.group(1)} | {fmt(frame_ms)} | {fmt(cgc, 4)} | {fmt(flush)} | "
                  f"{fmt(upload)} | {fmt(draw)} | {fmt(upload_share, 1)} | {fmt(draws, 2)} | "
                  f"{fmt(upload_bytes / 1e6 if upload_bytes else None, 2)} | "
                  f"{fmt(alloc / 1e6 if alloc else None, 2)} | {fmt(gpu)} | {fmt(gpu_share, 1)} | "
                  f"{fmt(cubes, 0)} | {fmt(channels, 1)} | {fmt(state_changes, 2)}")

    print()
    print("说明：")
    print("- upload + draw 是 endBatch flush 的子区间，两者相加小于 flush 属正常（还有状态设置）。")
    print("- Upload bytes / 帧 来自 draw 的 DrawState（顶点+索引），可直接与 384 KB/枪 对照。")
    print("- Allocated bytes 是渲染线程本帧分配量（HotSpot ThreadMXBean），用于解释 1% low。")
    print("- State changes 是「本次提交的节点状态指纹与上次不同」的次数；静态展示首帧后应为 0。")
    print("- GPU frame busy 是两次帧事件之间的 GL 命令耗时；F3 打开时该值为空。")


if __name__ == "__main__":
    main()
