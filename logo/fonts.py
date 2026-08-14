import glob
import os

_font_data = {}


def load_fonts(font_dir="./fonts"):
    """读取字体文件并存入字典"""
    global _font_data
    _font_data.clear()
    pattern = os.path.join(font_dir, "*.txt")
    for file_path in glob.glob(pattern):
        file_name = os.path.basename(file_path)
        ascii_str = os.path.splitext(file_name)[0]
        if ascii_str.isdigit():
            ascii_val = int(ascii_str)
            with open(file_path, "r", encoding="utf-8") as f:
                lines = [line.rstrip("\r\n") for line in f.readlines()]
                # 补齐或截断至8行6列
                matrix = []
                for i in range(8):
                    line = lines[i] if i < len(lines) else ""
                    line = line.ljust(6)[:6]
                    matrix.append(line)
                _font_data[ascii_val] = matrix
    return _font_data


def get_char_matrix(ascii_val):
    """获取指定ASCII字符对应的8行6列矩阵"""
    if not _font_data:
        load_fonts()
    return _font_data.get(ascii_val, [" " * 6] * 8)