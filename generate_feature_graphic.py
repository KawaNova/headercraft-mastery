#!/usr/bin/env python3
"""
Generate 1024x500 Feature Graphic for 034_http-headers (HeaderCraft)
"""
import os
import math
from PIL import Image, ImageDraw, ImageFont

def create_feature_graphic():
    w, h = 1024, 500
    img = Image.new("RGBA", (w, h), (15, 23, 42, 255))
    draw = ImageDraw.Draw(img)

    for y in range(h):
        ratio = y / float(h)
        r = int(15 + ratio * 8)
        g = int(23 + ratio * 12)
        b = int(42 + ratio * 20)
        draw.line([(0, y), (w, y)], fill=(r, g, b, 255))

    # Background tech circles on the right
    cx_grid, cy_grid = 780, 250
    for r in [280, 200, 130, 70]:
        draw.ellipse([cx_grid - r, cy_grid - r, cx_grid + r, cy_grid + r], outline=(56, 189, 248, 25), width=1)
    for angle_deg in range(0, 360, 30):
        rad = math.radians(angle_deg)
        x2 = cx_grid + 280 * math.cos(rad)
        y2 = cy_grid + 280 * math.sin(rad)
        draw.line([(cx_grid, cy_grid), (x2, y2)], fill=(16, 185, 129, 20), width=1)

    # Fonts
    font_badge = None
    font_title = None
    font_sub = None
    font_tag = None
    font_code = None
    font_card = None
    try:
        font_badge = ImageFont.truetype("arialbd.ttf", 15)
        font_title = ImageFont.truetype("arialbd.ttf", 44)
        font_sub = ImageFont.truetype("arial.ttf", 20)
        font_tag = ImageFont.truetype("arialbd.ttf", 13)
        font_code = ImageFont.truetype("consola.ttf", 15)
        font_card = ImageFont.truetype("arialbd.ttf", 14)
    except Exception:
        font_badge = ImageFont.load_default()
        font_title = ImageFont.load_default()
        font_sub = ImageFont.load_default()
        font_tag = ImageFont.load_default()
        font_code = ImageFont.load_default()
        font_card = ImageFont.load_default()

    # Left content: Hero branding
    x_left = 60
    y_badge = 65

    # Top Pill Badge
    draw.rounded_rectangle([x_left, y_badge, x_left + 290, y_badge + 34], radius=8, fill=(30, 41, 59, 255), outline=(56, 189, 248, 255), width=1)
    draw.text((x_left + 16, y_badge + 8), "🌐 CORS・SECURITY・CACHE ENGINE", fill=(56, 189, 248, 255), font=font_badge)

    # Main Title
    y_title = y_badge + 50
    draw.text((x_left, y_title), "HeaderCraft", fill=(255, 255, 255, 255), font=font_title)

    # Subtitle
    y_sub = y_title + 58
    draw.text((x_left, y_sub), "HTTP Headers & Security Configurator", fill=(148, 163, 184, 255), font=font_sub)

    # Feature badges
    y_features = y_sub + 50
    features = [
        ("⚡ CORS Preflight Builder", (56, 189, 248)),
        ("🛡️ A+ Security Auditing", (16, 185, 129)),
        ("💾 Multi-Server Export", (245, 158, 11)),
    ]

    cx_f = x_left
    for text, color in features:
        bbox = draw.textbbox((0, 0), text, font=font_tag)
        bw = bbox[2] - bbox[0] + 20
        draw.rounded_rectangle([cx_f, y_features, cx_f + bw, y_features + 30], radius=6, fill=(30, 41, 59, 220), outline=color, width=1)
        draw.text((cx_f + 10, y_features + 6), text, fill=(241, 245, 249, 255), font=font_tag)
        cx_f += bw + 10

    # Privacy / Local badge
    y_priv = y_features + 45
    draw.text((x_left, y_priv), "🔒 100% On-Device / Zero Cloud Dependency / Offline Ready", fill=(100, 116, 139, 255), font=font_tag)

    # Supported servers tag bar
    y_servers = y_priv + 35
    draw.text((x_left, y_servers), "Outputs: Nginx • Apache • Cloudflare • Next.js • Express", fill=(148, 163, 184, 255), font=font_badge)

    # Right side: Mockup Card of Header Console
    card_x = 580
    card_y = 65
    card_w = 380
    card_h = 360

    # Outer border/glow
    draw.rounded_rectangle([card_x - 2, card_y - 2, card_x + card_w + 2, card_y + card_h + 2], radius=14, outline=(56, 189, 248, 140), width=1)
    draw.rounded_rectangle([card_x, card_y, card_x + card_w, card_y + card_h], radius=12, fill=(20, 29, 47, 255))

    # Card Top Header
    draw.rounded_rectangle([card_x, card_y, card_x + card_w, card_y + 40], radius=12, fill=(30, 41, 59, 255))
    draw.rectangle([card_x, card_y + 25, card_x + card_w, card_y + 40], fill=(30, 41, 59, 255))

    for i, col in enumerate([(239, 68, 68), (245, 158, 11), (16, 185, 129)]):
        draw.ellipse([card_x + 16 + i * 16, card_y + 14, card_x + 26 + i * 16, card_y + 24], fill=col)

    draw.text((card_x + 80, card_y + 11), "Server Config (Nginx / Express)", fill=(148, 163, 184, 255), font=font_card)

    # Score Badge
    draw.rounded_rectangle([card_x + card_w - 90, card_y + 8, card_x + card_w - 12, card_y + 32], radius=6, fill=(16, 185, 129, 60), outline=(16, 185, 129, 255), width=1)
    draw.text((card_x + card_w - 82, card_y + 11), "🛡️ Grade A+", fill=(16, 185, 129, 255), font=font_tag)

    # Header Rows Code Preview
    lines = [
        ("# CORS & Security Headers", (100, 116, 139)),
        ("add_header Access-Control-Allow-Origin", (56, 189, 248)),
        ("  \"https://app.example.com\" always;", (241, 245, 249)),
        ("add_header Strict-Transport-Security", (168, 85, 247)),
        ("  \"max-age=31536000; preload\" always;", (241, 245, 249)),
        ("add_header X-Content-Type-Options", (16, 185, 129)),
        ("  \"nosniff\" always;", (241, 245, 249)),
        ("add_header Cache-Control", (245, 158, 11)),
        ("  \"public, max-age=31536000, immutable\";", (241, 245, 249)),
    ]

    y_line = card_y + 56
    for text, color in lines:
        draw.text((card_x + 18, y_line), text, fill=color, font=font_code)
        y_line += 28

    return img

def main():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    img = create_feature_graphic()
    out_path = os.path.join(base_dir, "feature_graphic_1024x500.png")
    img.save(out_path)
    print(f"Generated feature graphic: {out_path}")

    # Also save to Play store asset directory
    store_dir = os.path.join(base_dir, "store_assets", "play")
    os.makedirs(store_dir, exist_ok=True)
    img.save(os.path.join(store_dir, "feature_graphic.png"))
    print(f"Saved to {store_dir}/feature_graphic.png")

if __name__ == "__main__":
    main()
