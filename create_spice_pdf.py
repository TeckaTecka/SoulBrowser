#!/usr/bin/env python3
"""Vytvoří profesionálně formátovaný PDF z DOC dokumentu Spice must flow."""

import re, os, glob
from io import BytesIO
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import cm, mm
from reportlab.lib import colors
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, PageBreak, Image, HRFlowable
)
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.enums import TA_JUSTIFY, TA_CENTER, TA_LEFT

# ── Fonty ─────────────────────────────────────────────────────────────────────
FONT_DIR = '/usr/share/fonts/truetype/liberation/'
pdfmetrics.registerFont(TTFont('LiberSerif',      FONT_DIR + 'LiberationSerif-Regular.ttf'))
pdfmetrics.registerFont(TTFont('LiberSerif-Bold', FONT_DIR + 'LiberationSerif-Bold.ttf'))
pdfmetrics.registerFont(TTFont('LiberSerif-Ital', FONT_DIR + 'LiberationSerif-Italic.ttf'))
pdfmetrics.registerFont(TTFont('LiberSerif-BI',   FONT_DIR + 'LiberationSerif-BoldItalic.ttf'))

PAGE_W, PAGE_H = A4
MARGIN_L = 3.0 * cm
MARGIN_R = 2.5 * cm
MARGIN_T = 2.5 * cm
MARGIN_B = 2.5 * cm
USABLE_W = PAGE_W - MARGIN_L - MARGIN_R

NAVY      = colors.HexColor('#1A1A5C')
DARK_GRAY = colors.HexColor('#444444')
LIGHT_GRAY= colors.HexColor('#888888')
GOLD      = colors.HexColor('#8B6914')

current_chapter = ['']

def draw_header_footer(canvas_obj, doc):
    canvas_obj.saveState()
    if doc.page > 1:
        y = PAGE_H - MARGIN_T + 5 * mm
        canvas_obj.setStrokeColor(NAVY)
        canvas_obj.setLineWidth(0.5)
        canvas_obj.line(MARGIN_L, y, PAGE_W - MARGIN_R, y)
        canvas_obj.setFont('LiberSerif-Ital', 9)
        canvas_obj.setFillColor(DARK_GRAY)
        canvas_obj.drawString(MARGIN_L, y + 2*mm, 'Spice must flow')
        canvas_obj.drawRightString(PAGE_W - MARGIN_R, y + 2*mm, current_chapter[0])
        canvas_obj.setFont('LiberSerif', 9)
        canvas_obj.setFillColor(LIGHT_GRAY)
        canvas_obj.drawCentredString(PAGE_W / 2, MARGIN_B - 7*mm, str(doc.page))
    canvas_obj.restoreState()

def make_styles():
    s = {}
    s['body'] = ParagraphStyle(
        'body', fontName='LiberSerif', fontSize=11.5, leading=17,
        alignment=TA_JUSTIFY, firstLineIndent=1.25*cm,
        spaceBefore=0, spaceAfter=5,
    )
    s['body_first'] = ParagraphStyle('body_first', parent=s['body'], firstLineIndent=0)
    s['chapter'] = ParagraphStyle(
        'chapter', fontName='LiberSerif-Bold', fontSize=17, leading=22,
        alignment=TA_LEFT, textColor=NAVY, spaceBefore=0, spaceAfter=4,
    )
    s['chapter_sub'] = ParagraphStyle(
        'chapter_sub', fontName='LiberSerif-Ital', fontSize=11, leading=15,
        alignment=TA_LEFT, textColor=DARK_GRAY, spaceBefore=0, spaceAfter=14,
    )
    s['quote'] = ParagraphStyle(
        'quote', fontName='LiberSerif-Ital', fontSize=12, leading=18,
        alignment=TA_CENTER, textColor=DARK_GRAY,
        spaceBefore=4, spaceAfter=4,
        leftIndent=2*cm, rightIndent=2*cm,
    )
    s['quote_attr'] = ParagraphStyle(
        'quote_attr', fontName='LiberSerif', fontSize=10, leading=14,
        alignment=TA_CENTER, textColor=LIGHT_GRAY,
        spaceBefore=0, spaceAfter=10,
    )
    s['caption'] = ParagraphStyle(
        'caption', fontName='LiberSerif-Ital', fontSize=9.5, leading=13,
        alignment=TA_CENTER, textColor=DARK_GRAY,
        spaceBefore=2, spaceAfter=10,
    )
    s['title_main'] = ParagraphStyle(
        'title_main', fontName='LiberSerif-Bold', fontSize=30, leading=38,
        alignment=TA_CENTER, textColor=NAVY, spaceBefore=0, spaceAfter=6,
    )
    s['title_sub'] = ParagraphStyle(
        'title_sub', fontName='LiberSerif-Ital', fontSize=13, leading=18,
        alignment=TA_CENTER, textColor=DARK_GRAY, spaceBefore=0, spaceAfter=4,
    )
    s['url'] = ParagraphStyle(
        'url', fontName='LiberSerif-Ital', fontSize=8.5, leading=12,
        alignment=TA_LEFT, textColor=LIGHT_GRAY,
        spaceBefore=0, spaceAfter=3,
    )
    s['translator'] = ParagraphStyle(
        'translator', fontName='LiberSerif-Ital', fontSize=10, leading=14,
        alignment=TA_LEFT, textColor=DARK_GRAY, spaceBefore=0, spaceAfter=12,
    )
    return s

def escape_rl(t):
    return t.replace('&','&amp;').replace('<','&lt;').replace('>','&gt;')

def make_image_flowable(path):
    try:
        img = Image(path)
        w, h = img.imageWidth, img.imageHeight
        max_w = USABLE_W
        max_h = 16 * cm
        scale = min(max_w / w, max_h / h, 1.0)
        img.drawWidth  = w * scale
        img.drawHeight = h * scale
        img.hAlign = 'CENTER'
        return img
    except Exception as e:
        print(f'  Varování: {path}: {e}')
        return None

# ── Načíst text a obrázky ─────────────────────────────────────────────────────
with open('/tmp/spice_text.txt', encoding='utf-8') as f:
    raw = f.read()

# Obrázky seřazené podle pořadí v dokumentu
images = sorted(glob.glob('/tmp/spice_ordered/*.jpg') + glob.glob('/tmp/spice_ordered/*.png'),
                key=lambda p: int(os.path.basename(p).split('.')[0]))
print(f'Dostupné obrázky: {len(images)}')

# Rozdělit text na segmenty oddělené [pic]
segments = raw.split('[pic]')
print(f'Segmentů textu: {len(segments)}, [pic] značek: {len(segments)-1}')

# ── Zpracovat text do řádků ───────────────────────────────────────────────────
CHAPTER_RE = re.compile(
    r'^(Spice must flow|Jaderná zápalka|Zuřivost|Noc je kratší než den|'
    r'Bude ti dáno znamení|Tento provázaný.*svět|Vládce, zrozený vládnout světu|'
    r'Kanadští sloni a australská želva|Vrata tlamy jeho).*\(NUC\d+\)$'
)
URL_RE    = re.compile(r'^https?://')
QUOTE_RE  = re.compile(r'^[—–]\s*(Princess|Baron|Irulan|Harkonnen)')
CAPTION_RE= re.compile(r'^(Náhled|Vypadá|Dříve|Symbol|Zde je|Na snímku|'
                         r'Toto je|Takový|Schéma|Zde jsou|A vůbec)')

def parse_segment(text):
    """Vrátí seznam (typ, obsah) pro segment textu."""
    items = []
    buf = []

    def flush_buf():
        joined = ' '.join(buf).strip()
        if joined:
            items.append(('body', joined))
        buf.clear()

    lines = text.split('\n')
    i = 0
    while i < len(lines):
        line = lines[i].rstrip()

        if not line:
            flush_buf()
            i += 1
            continue

        # Kapitola
        if CHAPTER_RE.match(line):
            flush_buf()
            items.append(('chapter', line))
            i += 1
            # Autor/překladatel – příští neprázdné řádky
            while i < len(lines) and not lines[i].strip():
                i += 1
            trans_lines = []
            while i < len(lines) and lines[i].strip() and not CHAPTER_RE.match(lines[i]):
                l = lines[i].strip()
                if URL_RE.match(l):
                    if trans_lines:
                        items.append(('translator', ' '.join(trans_lines)))
                        trans_lines = []
                    items.append(('url', l))
                else:
                    trans_lines.append(l)
                i += 1
            if trans_lines:
                items.append(('translator', ' '.join(trans_lines)))
            continue

        # URL
        if URL_RE.match(line.strip()):
            flush_buf()
            items.append(('url', line.strip()))
            i += 1
            continue

        # Citát (— Princess / Baron)
        if QUOTE_RE.match(line):
            flush_buf()
            items.append(('quote_attr', line))
            i += 1
            continue

        # Popisek obrázku
        if CAPTION_RE.match(line) and len(line) < 100:
            flush_buf()
            items.append(('caption', line))
            i += 1
            continue

        # Antiword zalamuje řádky – sloučit je do odstavce
        # Nový odstavec začíná prázdným řádkem nebo velkým písmenem po tečce
        buf.append(line)
        i += 1

    flush_buf()
    return items

# ── Sestavit story ────────────────────────────────────────────────────────────
def build_story(styles):
    story = []
    img_idx = 0

    # Titulní strana
    story.append(Spacer(1, 5*cm))
    story.append(Paragraph(escape_rl('Spice must flow'), styles['title_main']))
    story.append(Spacer(1, 3*mm))
    story.append(Paragraph(
        escape_rl('He who controls the spice, controls the universe'),
        styles['title_sub']
    ))
    story.append(Spacer(1, 8*mm))
    story.append(HRFlowable(width='60%', color=NAVY, thickness=0.5, spaceAfter=8*mm))
    story.append(Paragraph(escape_rl('Napsal: crustgroup  ·  Přeložil: Hamilbar'), styles['title_sub']))
    story.append(PageBreak())

    # Úvodní citáty (první segment před prvním [pic])
    first_seg = segments[0]
    quote_lines = [l.strip() for l in first_seg.split('\n') if l.strip()]
    in_quotes = True
    for line in quote_lines:
        if CHAPTER_RE.match(line):
            in_quotes = False
        if in_quotes:
            if line.startswith('—') or line.startswith('–'):
                story.append(Paragraph(escape_rl(line), styles['quote_attr']))
            elif line and not URL_RE.match(line):
                story.append(Paragraph(escape_rl(line), styles['quote']))
        # Jakmile narazíme na NUC1, přeskočíme (kapitola přijde při segment parsování)

    # Zpracovat všechny segmenty
    for seg_idx, seg in enumerate(segments):
        is_first_in_chapter = True
        items = parse_segment(seg)

        for typ, content in items:
            if typ == 'chapter':
                # Název kapitoly + oddělovač
                num = re.search(r'NUC(\d+)', content)
                label = f"NUC{num.group(1)}" if num else ''
                current_chapter[0] = label
                if seg_idx > 0:
                    story.append(PageBreak())
                title_text = re.sub(r'\s*\(NUC\d+\)', '', content).strip()
                story.append(Paragraph(escape_rl(title_text), styles['chapter']))
                story.append(Paragraph(escape_rl(label), styles['chapter_sub']))
                story.append(HRFlowable(width='100%', color=NAVY, thickness=0.5,
                                        spaceBefore=2, spaceAfter=10))
                is_first_in_chapter = True

            elif typ == 'translator':
                story.append(Paragraph(escape_rl(content), styles['translator']))
                is_first_in_chapter = True

            elif typ == 'url':
                story.append(Paragraph(escape_rl(content), styles['url']))

            elif typ == 'quote':
                story.append(Paragraph(escape_rl(content), styles['quote']))

            elif typ == 'quote_attr':
                story.append(Paragraph(escape_rl(content), styles['quote_attr']))

            elif typ == 'caption':
                story.append(Paragraph(escape_rl(content), styles['caption']))

            elif typ == 'body':
                st = styles['body_first'] if is_first_in_chapter else styles['body']
                story.append(Paragraph(escape_rl(content), st))
                is_first_in_chapter = False

        # Vložit obrázek za tento segment (pokud existuje)
        if seg_idx < len(segments) - 1:  # za každým [pic] je segment
            if img_idx < len(images):
                img = make_image_flowable(images[img_idx])
                if img:
                    story.append(Spacer(1, 4))
                    story.append(img)
                    story.append(Spacer(1, 6))
                img_idx += 1
            else:
                story.append(Paragraph('[obrázek nedostupný]', styles['caption']))

    print(f'Použito obrázků: {img_idx} z {len(images)}')
    return story

# ── Hlavní funkce ─────────────────────────────────────────────────────────────
def main():
    out_pdf = '/home/user/SoulBrowser/Spice_must_flow_reformatovano.pdf'
    styles  = make_styles()
    story   = build_story(styles)

    doc = SimpleDocTemplate(
        out_pdf, pagesize=A4,
        leftMargin=MARGIN_L, rightMargin=MARGIN_R,
        topMargin=MARGIN_T + 1.0*cm, bottomMargin=MARGIN_B,
        title='Spice must flow',
        author='crustgroup / překlad Hamilbar',
        subject='Jaderná energie – série NUC',
    )
    doc.build(story, onFirstPage=draw_header_footer, onLaterPages=draw_header_footer)
    print(f'PDF uložen: {out_pdf}')

if __name__ == '__main__':
    main()
