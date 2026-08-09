def draw_harness(canvas, color):
    """填充经过 (63, 63) 的两条线条"""
    for i in range(64):
        canvas[63][i] = color
        canvas[i][63] = color