#!/usr/bin/env python3
"""
Generate 1080x1920 high-res store screenshots for 034_http-headers (HeaderCraft)
"""
import os
from PIL import Image, ImageDraw, ImageFont

def create_phone_mockup(title, subtitle, badge_text, screen_content_func, width=1080, height=1920):
    img = Image.new("RGBA", (width, height), (15, 23, 42, 255))
    draw = ImageDraw.Draw(img)

    # Top title branding section
    font_badge = ImageFont.truetype("arialbd.ttf", 36) if os.path.exists("C:/Windows/Fonts/arialbd.ttf") else ImageFont.load_default()
    font_title = ImageFont.truetype("arialbd.ttf", 64) if os.path.exists("C:/Windows/Fonts/arialbd.ttf") else ImageFont.load_default()
    font_sub = ImageFont.truetype("arial.ttf", 36) if os.path.exists("C:/Windows/Fonts/arial.ttf") else ImageFont.load_default()

    # Badge
    bbox = draw.textbbox((0, 0), badge_text, font=font_badge)
    bw = bbox[2] - bbox[0] + 40
    bx = (width - bw) // 2
    draw.rounded_rectangle([bx, 80, bx + bw, 136], radius=12, fill=(30, 41, 59, 255), outline=(56, 189, 248, 255), width=2)
    draw.text((bx + 20, 88), badge_text, fill=(56, 189, 248, 255), font=font_badge)

    # Title
    bbox_t = draw.textbbox((0, 0), title, font=font_title)
    draw.text(((width - (bbox_t[2] - bbox_t[0])) // 2, 160), title, fill=(255, 255, 255, 255), font=font_title)

    # Subtitle
    bbox_s = draw.textbbox((0, 0), subtitle, font=font_sub)
    draw.text(((width - (bbox_s[2] - bbox_s[0])) // 2, 245), subtitle, fill=(148, 163, 184, 255), font=font_sub)

    # Phone Frame
    phone_x = 90
    phone_y = 330
    phone_w = 900
    phone_h = 1530

    # Outer bezel
    draw.rounded_rectangle([phone_x, phone_y, phone_x + phone_w, phone_y + phone_h], radius=48, fill=(10, 15, 28, 255), outline=(51, 65, 85, 255), width=4)
    # Inner display
    screen_x = phone_x + 20
    screen_y = phone_y + 20
    screen_w = phone_w - 40
    screen_h = phone_h - 40
    draw.rounded_rectangle([screen_x, screen_y, screen_x + screen_w, screen_y + screen_h], radius=36, fill=(15, 23, 42, 255))

    # Notch / Camera Island
    draw.rounded_rectangle([screen_x + screen_w // 2 - 80, screen_y + 12, screen_x + screen_w // 2 + 80, screen_y + 44], radius=16, fill=(0, 0, 0, 255))

    # Draw specific screen content
    screen_content_func(draw, screen_x, screen_y + 60, screen_w, screen_h - 60)

    return img

def draw_builder_screen(draw, sx, sy, sw, sh):
    font_bold = ImageFont.truetype("arialbd.ttf", 32)
    font_code = ImageFont.truetype("consola.ttf", 26)
    font_sm = ImageFont.truetype("arial.ttf", 24)

    # Top Bar
    draw.text((sx + 30, sy + 20), "HeaderCraft", fill=(255, 255, 255), font=font_bold)
    draw.rounded_rectangle([sx + sw - 230, sy + 15, sx + sw - 30, sy + 65], radius=10, fill=(16, 185, 129, 60), outline=(16, 185, 129, 255), width=2)
    draw.text((sx + sw - 215, sy + 23), "🛡️ Grade A+ (98点)", fill=(16, 185, 129), font=font_sm)

    # Quick action buttons
    by = sy + 90
    draw.rounded_rectangle([sx + 30, by, sx + sw//2 - 10, by + 70], radius=12, fill=(30, 41, 59), outline=(56, 189, 248), width=2)
    draw.text((sx + 60, by + 18), "🌐 CORS ビルダー", fill=(56, 189, 248), font=font_bold)

    draw.rounded_rectangle([sx + sw//2 + 10, by, sx + sw - 30, by + 70], radius=12, fill=(30, 41, 59), outline=(16, 185, 129), width=2)
    draw.text((sx + sw//2 + 40, by + 18), "⚡ Cache 制御", fill=(16, 185, 129), font=font_bold)

    # Headers list
    hy = by + 95
    draw.text((sx + 30, hy), "設定中のHTTPヘッダー (6件)", fill=(241, 245, 249), font=font_bold)

    headers = [
        ("Access-Control-Allow-Origin", "https://app.example.com", (56, 189, 248)),
        ("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS", (56, 189, 248)),
        ("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload", (168, 85, 247)),
        ("Content-Security-Policy", "default-src 'self'; script-src 'self'", (16, 185, 129)),
        ("X-Content-Type-Options", "nosniff", (16, 185, 129)),
        ("Cache-Control", "no-store, no-cache, must-revalidate", (245, 158, 11)),
    ]

    card_y = hy + 45
    for key, val, col in headers:
        draw.rounded_rectangle([sx + 30, card_y, sx + sw - 30, card_y + 110], radius=14, fill=(30, 41, 59))
        draw.ellipse([sx + 50, card_y + 40, sx + 74, card_y + 64], fill=(56, 189, 248))
        draw.text((sx + 90, card_y + 16), key, fill=col, font=font_bold)
        draw.text((sx + 90, card_y + 60), val[:40] + ("..." if len(val) > 40 else ""), fill=(226, 232, 240), font=font_code)
        card_y += 125

    # Bottom Code Output Preview
    draw.rounded_rectangle([sx + 30, card_y + 10, sx + sw - 30, card_y + 260], radius=16, fill=(2, 6, 23), outline=(56, 189, 248), width=2)
    draw.text((sx + 50, card_y + 25), "Nginx 設定出力 (ワンタップコピー可能)", fill=(148, 163, 184), font=font_sm)
    draw.text((sx + 50, card_y + 70), "add_header Access-Control-Allow-Origin \"https://...\";", fill=(56, 189, 248), font=font_code)
    draw.text((sx + 50, card_y + 115), "add_header Strict-Transport-Security \"max-age=...\";", fill=(168, 85, 247), font=font_code)
    draw.text((sx + 50, card_y + 160), "add_header X-Content-Type-Options \"nosniff\";", fill=(16, 185, 129), font=font_code)

def draw_formats_screen(draw, sx, sy, sw, sh):
    font_bold = ImageFont.truetype("arialbd.ttf", 32)
    font_code = ImageFont.truetype("consola.ttf", 25)
    font_sm = ImageFont.truetype("arial.ttf", 24)

    draw.text((sx + 30, sy + 20), "マルチサーバー・フレームワーク対応", fill=(255, 255, 255), font=font_bold)

    formats = [
        ("Nginx", "add_header X-Frame-Options \"DENY\" always;\nadd_header Content-Security-Policy \"...\";", (56, 189, 248)),
        ("Apache (.htaccess)", "<IfModule mod_headers.c>\n  Header set X-Frame-Options \"DENY\"\n</IfModule>", (245, 158, 11)),
        ("Next.js (next.config.js)", "async headers() {\n  return [{ source: '/:path*', headers: [...] }];\n}", (168, 85, 247)),
        ("Cloudflare / Vercel (_headers)", "/*\n  X-Content-Type-Options: nosniff\n  Cache-Control: public, max-age=31536000", (16, 185, 129)),
        ("Node.js (Express)", "app.use((req, res, next) => {\n  res.setHeader('X-Frame-Options', 'DENY');\n  next();\n});", (244, 63, 94))
    ]

    fy = sy + 90
    for name, code, col in formats:
        draw.rounded_rectangle([sx + 30, fy, sx + sw - 30, fy + 220], radius=16, fill=(30, 41, 59), outline=col, width=2)
        draw.text((sx + 50, fy + 16), name, fill=col, font=font_bold)
        draw.rounded_rectangle([sx + 50, fy + 65, sx + sw - 50, fy + 200], radius=8, fill=(2, 6, 23))
        lines = code.split("\n")
        for i, line in enumerate(lines):
            draw.text((sx + 65, fy + 75 + i * 38), line, fill=(226, 232, 240), font=font_code)
        fy += 245

def draw_audit_screen(draw, sx, sy, sw, sh):
    font_bold = ImageFont.truetype("arialbd.ttf", 32)
    font_title = ImageFont.truetype("arialbd.ttf", 44)
    font_sm = ImageFont.truetype("arial.ttf", 24)

    draw.text((sx + 30, sy + 20), "🛡️ HTTPヘッダー・セキュリティ診断", fill=(255, 255, 255), font=font_bold)

    # Score Box
    draw.rounded_rectangle([sx + 30, sy + 80, sx + sw - 30, sy + 250], radius=18, fill=(16, 185, 129, 40), outline=(16, 185, 129), width=3)
    draw.text((sx + 60, sy + 110), "総合セキュリティスコア: A+", fill=(16, 185, 129), font=font_title)
    draw.text((sx + 60, sy + 180), "98 / 100点 - 業界標準のベストプラクティスに適合", fill=(226, 232, 240), font=font_bold)

    # Audit issues list
    ay = sy + 280
    issues = [
        ("✅ 高水準のセキュリティ設定", "HSTS, CSP, nosniff, クリックジャッキング防止が完全構成", (16, 185, 129)),
        ("ℹ️ HSTS Preload 設定済", "次回以降のブラウザ直接暗号化通信を保証", (56, 189, 248)),
        ("🚨 ワイルドカードCORS & Cookie検知機能", "ブラウザによるブロックを未然に警告・自動回避", (244, 63, 94)),
        ("💡 Referrer-Policy 推奨", "strict-origin-when-cross-origin によるURL漏洩防止", (245, 158, 11)),
    ]

    for title, desc, col in issues:
        draw.rounded_rectangle([sx + 30, ay, sx + sw - 30, ay + 150], radius=14, fill=(30, 41, 59))
        draw.text((sx + 50, ay + 20), title, fill=col, font=font_bold)
        draw.text((sx + 50, ay + 75), desc, fill=(203, 213, 225), font=font_sm)
        ay += 175

def draw_presets_screen(draw, sx, sy, sw, sh):
    font_bold = ImageFont.truetype("arialbd.ttf", 32)
    font_sm = ImageFont.truetype("arial.ttf", 24)

    draw.text((sx + 30, sy + 20), "⚡ 目的別プリセット ＆ HTTP辞典", fill=(255, 255, 255), font=font_bold)

    presets = [
        ("RESTful JSON API (セキュア)", "API", "no-store, CORS開放, nosniff, HTTPS強制", (56, 189, 248)),
        ("最高セキュリティ A+ サイト", "Security", "CSP, HSTS Preload, COOP, CORP, XFO", (16, 185, 129)),
        ("静的アセット & CDN (1年 immutable)", "Cache", "ビルドアセットを最速キャッシュ配信", (245, 158, 11)),
        ("SPA / フロントエンドホスティング", "Web", "React / Vue / Next.js 用 HTMLキャッシュ制御", (168, 85, 247)),
        ("Webフォント配信 (WOFF2/TTF)", "Assets", "他ドメインでのフォント描画許可と長期保存", (6, 182, 212)),
    ]

    py = sy + 90
    for name, cat, desc, col in presets:
        draw.rounded_rectangle([sx + 30, py, sx + sw - 30, py + 160], radius=16, fill=(30, 41, 59))
        draw.rounded_rectangle([sx + 50, py + 16, sx + 170, py + 56], radius=8, fill=col)
        draw.text((sx + 65, py + 22), cat, fill=(15, 23, 42), font=font_bold)
        draw.text((sx + 190, py + 20), name, fill=(241, 245, 249), font=font_bold)
        draw.text((sx + 50, py + 80), desc, fill=(148, 163, 184), font=font_sm)
        draw.rounded_rectangle([sx + sw - 230, py + 95, sx + sw - 50, py + 145], radius=8, fill=(56, 189, 248))
        draw.text((sx + sw - 215, py + 105), "⚡ ビルダー適用", fill=(15, 23, 42), font=font_sm)
        py += 185

def main():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    out_dir = os.path.join(base_dir, "store_assets", "play")
    os.makedirs(out_dir, exist_ok=True)

    screens = [
        ("01_builder.png", "CORS & セキュリティヘッダー生成", "直感UIで安全なHTTPヘッダーを瞬時に作成", "HTTP HEADER ENGINE", draw_builder_screen),
        ("02_formats.png", "マルチサーバー出力対応", "Nginx / Apache / Next.js / Express 出力", "MULTI-PLATFORM EXPORT", draw_formats_screen),
        ("03_audit.png", "A+ 脆弱性・セキュリティ診断", "ブラウザ互換性・CORS仕様違反を未然検知", "SECURITY AUDIT", draw_audit_screen),
        ("04_presets.png", "即戦力プリセット & HTTP辞典", "40種以上のHTTPヘッダー詳細解説と構成集", "PRESETS & GLOSSARY", draw_presets_screen),
    ]

    for fname, title, sub, badge, func in screens:
        img = create_phone_mockup(title, sub, badge, func)
        out_path = os.path.join(out_dir, fname)
        img.save(out_path)
        print(f"Generated screenshot: {out_path}")

    print("All screenshots generated successfully!")

if __name__ == "__main__":
    main()
