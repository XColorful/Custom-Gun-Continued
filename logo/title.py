import fonts
import highlight
import palette


def draw_title(
    canvas,
    title_path="title.txt",
    highlight_color=(0, 0, 0),
    default_text_color=(255, 255, 255),
    shadow_color=(0, 0, 0),
):
    """绘制两行文本及高亮背景，包含阴影效果"""
    with open(title_path, "r", encoding="utf-8") as f:
        lines = [line.rstrip("\r\n") for line in f.readlines()]

    line1 = lines[0] if len(lines) > 0 else ""
    line2 = lines[1] if len(lines) > 1 else ""

    text_rows = [
        (line1, 24),  # 中线往上8像素 (32 - 8)
        (line2, 32),  # 中线往下8像素 (32)
    ]

    is_highlight = True
    current_text_color = default_text_color

    # 预定义的颜色前缀控制符
    CONTROL_PREFIXES = ("§")

    for text, start_y in text_rows:
        parsed_chars = []
        i = 0
        while i < len(text):
            char = text[i]
            # 检查控制符 (控制字符后跟6位十六进制数字)
            if char in CONTROL_PREFIXES and i + 6 < len(text):
                color_code = text[i + 1 : i + 7]
                if all(c in "0123456789abcdefABCDEF" for c in color_code):
                    current_text_color = palette.parse_color(color_code)
                    i += 7
                    continue
            parsed_chars.append((char, current_text_color))
            i += 1

        char_count = len(parsed_chars)
        start_x = (64 - char_count * 6) // 2

        # 第一遍循环：填充高亮背景
        for idx, (char, text_color) in enumerate(parsed_chars):
            cur_x = start_x + idx * 6
            if is_highlight:
                highlight.draw_highlight(canvas, cur_x, start_y, highlight_color)
            is_highlight = not is_highlight

        # 第二遍循环：先绘制所有字符的阴影像素（右下偏移 1 像素）
        for idx, (char, text_color) in enumerate(parsed_chars):
            cur_x = start_x + idx * 6
            matrix = fonts.get_char_matrix(ord(char))
            for row_idx in range(8):
                for col_idx in range(6):
                    if matrix[row_idx][col_idx] != " ":
                        px, py = cur_x + col_idx + 1, start_y + row_idx + 1
                        if 0 <= px < 64 and 0 <= py < 64:
                            canvas[py][px] = shadow_color

        # 第三遍循环：绘制实体文字像素，覆盖可能存在的重叠阴影
        for idx, (char, text_color) in enumerate(parsed_chars):
            cur_x = start_x + idx * 6
            matrix = fonts.get_char_matrix(ord(char))
            for row_idx in range(8):
                for col_idx in range(6):
                    if matrix[row_idx][col_idx] != " ":
                        px, py = cur_x + col_idx, start_y + row_idx
                        if 0 <= px < 64 and 0 <= py < 64:
                            canvas[py][px] = text_color