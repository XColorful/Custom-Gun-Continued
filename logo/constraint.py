def draw_constraint(canvas, color):
    """在左上、右上、左下三个网格交叉点四周像素填充指定颜色"""
    crosses = [(7, 7), (55, 7), (7, 55)]
    for cx, cy in crosses:
        offsets = [(-1, -1), (1, -1), (-1, 1), (1, 1)]
        for dx, dy in offsets:
            x, y = cx + dx, cy + dy
            if 0 <= x < 64 and 0 <= y < 64:
                canvas[y][x] = color