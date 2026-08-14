def draw_background(canvas, color):
    """填充背景颜色"""
    for y in range(64):
        for x in range(64):
            canvas[y][x] = color