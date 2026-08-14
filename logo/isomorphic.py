def draw_isomorphic(canvas, color):
    """在网格右下角格子内边缘填充指定颜色"""
    # 右下角格子范围为 (56, 56) 到 (62, 62)
    for x in range(56, 63):
        canvas[56][x] = color
        canvas[62][x] = color
    for y in range(56, 63):
        canvas[y][56] = color
        canvas[y][62] = color