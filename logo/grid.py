def draw_grid(canvas, color):
    """画8条竖线和8条横线，右下角63填充，0不填充"""
    for i in range(7, 64, 8):
        for j in range(64):
            canvas[i][j] = color  # 横线
            canvas[j][i] = color  # 竖线