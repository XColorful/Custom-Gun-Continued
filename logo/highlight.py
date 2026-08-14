def draw_highlight(canvas, start_x, start_y, color):
    """高亮填充8行6列区域"""
    for row in range(8):
        for col in range(6):
            x, y = start_x + col, start_y + row
            if 0 <= x < 64 and 0 <= y < 64:
                canvas[y][x] = color