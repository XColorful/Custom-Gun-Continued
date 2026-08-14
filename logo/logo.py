import os
import struct

import background
import constraint
import fonts
import grid
import harness
import isomorphic
import palette
import title


def save_bmp(canvas, filename="logo.bmp"):
    """保存64x64画布为24位BMP文件"""
    width, height = 64, 64
    row_bytes = width * 3  # 192字节，已是4字节对齐
    image_size = row_bytes * height
    file_size = 54 + image_size

    # BMP文件头 (14字节)
    bmp_header = struct.pack("<2sIHHI", b"BM", file_size, 0, 0, 54)
    # DIB头 (40字节)，负高度表示顶向下排列
    dib_header = struct.pack("<IiiHHIIiiII", 40, width, -height, 1, 24, 0, image_size, 0, 0, 0, 0)

    pixel_data = bytearray()
    for y in range(height):
        for x in range(width):
            r, g, b = canvas[y][x]
            pixel_data.extend([b, g, r])

    with open(filename, "wb") as f:
        f.write(bmp_header)
        f.write(dib_header)
        f.write(pixel_data)


def main():
    # 设置工作目录为脚本所在目录
    os.chdir(os.path.dirname(os.path.abspath(__file__)))

    # 加载数据
    fonts.load_fonts("./fonts")
    colors = palette.load_palette("palette.txt")

    # 初始化 64x64 画布
    canvas = [[(0, 0, 0) for _ in range(64)] for _ in range(64)]

    # 依序绘制
    background.draw_background(canvas, colors.get("background", (0, 0, 0)))
    grid.draw_grid(canvas, colors.get("grid", (128, 128, 128)))
    constraint.draw_constraint(canvas, colors.get("constraint", (255, 0, 0)))
    isomorphic.draw_isomorphic(canvas, colors.get("isomorphic", (0, 255, 0)))
    harness.draw_harness(canvas, colors.get("harness", (0, 0, 255)))
    title.draw_title(
        canvas,
        title_path="title.txt",
        highlight_color=colors.get("highlight", (255, 255, 0)),
        default_text_color=(255, 255, 255),
        shadow_color=colors.get("shadow", (0, 0, 0)),
    )

    save_bmp(canvas, "logo.bmp")


if __name__ == "__main__":
    main()