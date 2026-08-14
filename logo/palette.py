def parse_color(color_str):
    """解析6位十六进制颜色字符串（如 FF0000 或 #FF0000）"""
    hex_str = color_str.strip().lstrip("#")
    if len(hex_str) != 6:
        raise ValueError(f"Invalid color format: {color_str}")
    r = int(hex_str[0:2], 16)
    g = int(hex_str[2:4], 16)
    b = int(hex_str[4:6], 16)
    return (r, g, b)


def load_palette(palette_path="palette.txt"):
    """读取palette.txt并返回颜色字典"""
    colors = {}
    with open(palette_path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or ":" not in line:
                continue
            key, val = line.split(":", 1)
            colors[key.strip()] = parse_color(val)
    return colors