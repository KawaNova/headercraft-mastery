#!/usr/bin/env python3
"""
Generate high quality app icons for 034_http-headers (HeaderCraft)
Brand theme: Midnight Slate (#0F172A) + HTTP Protocol / Headers Card + CORS/Security Shield + 'HeaderCraft'
"""
import os
import math
from PIL import Image, ImageDraw, ImageFont

def create_headercraft_icon(size=512):
    img = Image.new("RGBA", (size, size), (15, 23, 42, 255))
    draw = ImageDraw.Draw(img)
    scale = size / 512.0

    cx, cy = size // 2, int(185 * scale)

    # 1. Subtle glowing tech background grid / circles
    for r, width, alpha in [(int(145 * scale), 1, 35), (int(130 * scale), 2, 60)]:
        bbox = (cx - r, cy - r, cx + r, cy + r)
        draw.ellipse(bbox, outline=(56, 189, 248, alpha), width=max(1, int(width * scale)))

    # 2. Main HTTP Header Document / Terminal Card
    card_w = int(220 * scale)
    card_h = int(180 * scale)
    card_x = cx - card_w // 2
    card_y = cy - int(95 * scale)
    card_r = int(16 * scale)

    # Card shadow / outer border
    draw.rounded_rectangle(
        [card_x - int(2 * scale), card_y - int(2 * scale), card_x + card_w + int(2 * scale), card_y + card_h + int(2 * scale)],
        radius=card_r + 2,
        fill=(30, 41, 59, 200),
        outline=(56, 189, 248, 120),
        width=max(1, int(2 * scale))
    )
    # Card surface
    draw.rounded_rectangle(
        [card_x, card_y, card_x + card_w, card_y + card_h],
        radius=card_r,
        fill=(20, 29, 47, 255)
    )

    # Card Header bar
    bar_h = int(32 * scale)
    draw.rounded_rectangle(
        [card_x, card_y, card_x + card_w, card_y + bar_h],
        radius=card_r,
        fill=(30, 41, 59, 255)
    )
    # Square bottom of header bar
    draw.rectangle(
        [card_x, card_y + bar_h - int(8 * scale), card_x + card_w, card_y + bar_h],
        fill=(30, 41, 59, 255)
    )

    # Terminal buttons (Red, Yellow, Green)
    dot_y = card_y + bar_h // 2
    for i, color in enumerate([(239, 68, 68), (245, 158, 11), (16, 185, 129)]):
        dx = card_x + int((16 + i * 14) * scale)
        r = int(4 * scale)
        draw.ellipse([dx - r, dot_y - r, dx + r, dot_y + r], fill=color)

    # HTTP Method / Badge on the right of bar
    try:
        font_badge = ImageFont.truetype("arialbd.ttf", int(11 * scale))
    except:
        font_badge = ImageFont.load_default()

    badge_txt = "HTTP/2"
    bbox = draw.textbbox((0, 0), badge_txt, font=font_badge)
    bw = bbox[2] - bbox[0] + int(10 * scale)
    bh = int(18 * scale)
    bx = card_x + card_w - bw - int(12 * scale)
    by = dot_y - bh // 2
    draw.rounded_rectangle([bx, by, bx + bw, by + bh], radius=int(4 * scale), fill=(14, 165, 233, 200))
    draw.text((bx + int(5 * scale), by + int(2 * scale)), badge_txt, fill=(255, 255, 255, 255), font=font_badge)

    # Simulated Key-Value Header Rows inside Card
    # Row 1: Green/Teal (CORS Origin)
    row1_y = card_y + int(46 * scale)
    draw.rounded_rectangle([card_x + int(14 * scale), row1_y, card_x + int(60 * scale), row1_y + int(14 * scale)], radius=int(3 * scale), fill=(16, 185, 129, 220)) # Key
    draw.rounded_rectangle([card_x + int(66 * scale), row1_y, card_x + int(170 * scale), row1_y + int(14 * scale)], radius=int(3 * scale), fill=(51, 65, 85, 240)) # Val

    # Row 2: Cyan/Sky (Cache-Control)
    row2_y = card_y + int(72 * scale)
    draw.rounded_rectangle([card_x + int(14 * scale), row2_y, card_x + int(85 * scale), row2_y + int(14 * scale)], radius=int(3 * scale), fill=(56, 189, 248, 220)) # Key
    draw.rounded_rectangle([card_x + int(91 * scale), row2_y, card_x + int(195 * scale), row2_y + int(14 * scale)], radius=int(3 * scale), fill=(51, 65, 85, 240)) # Val

    # Row 3: Purple (Security / CSP)
    row3_y = card_y + int(98 * scale)
    draw.rounded_rectangle([card_x + int(14 * scale), row3_y, card_x + int(105 * scale), row3_y + int(14 * scale)], radius=int(3 * scale), fill=(168, 85, 247, 220)) # Key
    draw.rounded_rectangle([card_x + int(111 * scale), row3_y, card_x + int(160 * scale), row3_y + int(14 * scale)], radius=int(3 * scale), fill=(51, 65, 85, 240)) # Val

    # Row 4: Amber (Auth / Options)
    row4_y = card_y + int(124 * scale)
    draw.rounded_rectangle([card_x + int(14 * scale), row4_y, card_x + int(70 * scale), row4_y + int(14 * scale)], radius=int(3 * scale), fill=(245, 158, 11, 220)) # Key
    draw.rounded_rectangle([card_x + int(76 * scale), row4_y, card_x + int(145 * scale), row4_y + int(14 * scale)], radius=int(3 * scale), fill=(51, 65, 85, 240)) # Val

    # 3. Prominent Security Shield Badge (overlapping bottom-right of Card)
    shield_cx = card_x + card_w - int(25 * scale)
    shield_cy = card_y + card_h - int(15 * scale)
    sw = int(46 * scale)
    sh_top = shield_cy - int(48 * scale)
    sh_mid = shield_cy + int(10 * scale)
    sh_bot = shield_cy + int(56 * scale)

    shield_pts = [
        (shield_cx - sw, sh_top),
        (shield_cx + sw, sh_top),
        (shield_cx + sw, sh_mid),
        (shield_cx, sh_bot),
        (shield_cx - sw, sh_mid)
    ]

    # Shield Outer Shadow / Glow
    draw.polygon(shield_pts, fill=(15, 23, 42, 255), outline=(16, 185, 129, 255))
    # Inner gradient/solid emerald
    inner_sw = sw - int(6 * scale)
    inner_top = sh_top + int(6 * scale)
    inner_mid = sh_mid
    inner_bot = sh_bot - int(6 * scale)
    inner_shield_pts = [
        (shield_cx - inner_sw, inner_top),
        (shield_cx + inner_sw, inner_top),
        (shield_cx + inner_sw, inner_mid),
        (shield_cx, inner_bot),
        (shield_cx - inner_sw, inner_mid)
    ]
    draw.polygon(inner_shield_pts, fill=(16, 185, 129, 240))

    # Bold Checkmark inside shield
    chk_w = max(3, int(6 * scale))
    chk_pts = [
        (shield_cx - int(18 * scale), shield_cy - int(2 * scale)),
        (shield_cx - int(5 * scale), shield_cy + int(12 * scale)),
        (shield_cx + int(18 * scale), shield_cy - int(14 * scale))
    ]
    draw.line(chk_pts, fill=(255, 255, 255, 255), width=chk_w, joint="curve")

    # 4. Text Area: "HeaderCraft" + "HTTP & CORS ENGINE"
    font_logo = None
    font_sub = None
    try:
        font_logo = ImageFont.truetype("arialbd.ttf", int(40 * scale))
        font_sub = ImageFont.truetype("arialbd.ttf", int(14 * scale))
    except:
        try:
            font_logo = ImageFont.truetype("DejaVuSans-Bold.ttf", int(40 * scale))
            font_sub = ImageFont.truetype("DejaVuSans.ttf", int(14 * scale))
        except:
            font_logo = ImageFont.load_default()
            font_sub = ImageFont.load_default()

    logo_text = "HeaderCraft"
    bbox_logo = draw.textbbox((0, 0), logo_text, font=font_logo)
    logo_w = bbox_logo[2] - bbox_logo[0]
    draw.text((cx - logo_w // 2, int(370 * scale)), logo_text, fill=(255, 255, 255, 255), font=font_logo)

    # Sub-text badge container
    sub_text = "CORS & SECURITY ENGINE"
    bbox_sub = draw.textbbox((0, 0), sub_text, font=font_sub)
    sub_w = bbox_sub[2] - bbox_sub[0]
    sub_box_w = sub_w + int(24 * scale)
    sub_box_h = int(24 * scale)
    sub_box_x = cx - sub_box_w // 2
    sub_box_y = int(426 * scale)

    draw.rounded_rectangle(
        [sub_box_x, sub_box_y, sub_box_x + sub_box_w, sub_box_y + sub_box_h],
        radius=int(6 * scale),
        fill=(30, 41, 59, 220),
        outline=(56, 189, 248, 120),
        width=max(1, int(1 * scale))
    )
    draw.text((cx - sub_w // 2, sub_box_y + int(4 * scale)), sub_text, fill=(56, 189, 248, 255), font=font_sub)

    return img

def create_round_icon(base_img):
    size = base_img.size[0]
    mask = Image.new("L", (size, size), 0)
    draw_mask = ImageDraw.Draw(mask)
    draw_mask.ellipse((0, 0, size, size), fill=255)

    round_img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    round_img.paste(base_img, (0, 0), mask=mask)
    return round_img

def main():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    res_dir = os.path.join(base_dir, "app", "src", "main", "res")

    icon_512 = create_headercraft_icon(512)
    icon_512.save(os.path.join(base_dir, "app_icon_512.png"))

    artifact_dir = r"C:\Users\Kawamoto\.gemini\antigravity\brain\85f914b0-2e74-4dde-88b5-7aefe5547cc8"
    if os.path.exists(artifact_dir):
        icon_512.save(os.path.join(artifact_dir, "headercraft_icon_test.png"))
        print(f"Saved test artifact to {os.path.join(artifact_dir, 'headercraft_icon_test.png')}")

    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }

    for folder, size in densities.items():
        folder_path = os.path.join(res_dir, folder)
        os.makedirs(folder_path, exist_ok=True)

        scaled_icon = create_headercraft_icon(size)
        scaled_icon.save(os.path.join(folder_path, "ic_launcher.png"))

        round_icon = create_round_icon(scaled_icon)
        round_icon.save(os.path.join(folder_path, "ic_launcher_round.png"))

        print(f"Generated {folder}: {size}x{size}")

    print("HeaderCraft icons successfully generated!")

if __name__ == "__main__":
    main()
