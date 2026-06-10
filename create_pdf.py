#!/usr/bin/env python3
"""Vytvoří profesionálně formátovaný PDF z českého DOCX dokumentu včetně obrázků."""

import re, os, zipfile, tempfile
from docx import Document
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import cm, mm
from reportlab.lib import colors
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, PageBreak, Image
)
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.enums import TA_JUSTIFY, TA_CENTER, TA_LEFT

# ── Registrace fontů ──────────────────────────────────────────────────────────
FONT_DIR = '/usr/share/fonts/truetype/liberation/'
pdfmetrics.registerFont(TTFont('LiberSerif',      FONT_DIR + 'LiberationSerif-Regular.ttf'))
pdfmetrics.registerFont(TTFont('LiberSerif-Bold', FONT_DIR + 'LiberationSerif-Bold.ttf'))
pdfmetrics.registerFont(TTFont('LiberSerif-Ital', FONT_DIR + 'LiberationSerif-Italic.ttf'))
pdfmetrics.registerFont(TTFont('LiberSerif-BI',   FONT_DIR + 'LiberationSerif-BoldItalic.ttf'))

# ── Opravy překlepů ───────────────────────────────────────────────────────────
CORRECTIONS = [
    ("demogafických",  "demografických"),
    ("mebyl",          "nebyl"),
    ("výjímkou",       "výjimkou"),
    ("francouzkého",   "francouzského"),
    ("Marcus Harvey",  "Marcus Garvey"),
]

def fix_text(text: str) -> str:
    text = text.replace("\xa0", " ")
    for wrong, right in CORRECTIONS:
        text = text.replace(wrong, right)
    text = re.sub(r'\bv Únoru\b', 'v únoru', text)
    text = re.sub(r'  +', ' ', text)
    return text.strip()

def normalize_chapter_title(text: str) -> str:
    text = text.replace("\xa0", " ").strip()
    m = re.match(r'Dve[rř]e ve zdi\s*[-–—]?\s*(\d+)(.*)', text, re.IGNORECASE)
    if m:
        num = m.group(1)
        rest = m.group(2).strip()
        if rest and not rest.startswith(',') and not rest.startswith('–') and not rest.startswith('-'):
            rest = ', ' + rest
        return f"Dveře ve zdi – {num}{rest}"
    return text

def escape_rl(text: str) -> str:
    text = text.replace('&', '&amp;')
    text = text.replace('<', '&lt;')
    text = text.replace('>', '&gt;')
    return text

# ── Rozvržení stránky ─────────────────────────────────────────────────────────
PAGE_W, PAGE_H = A4
MARGIN_L = 3.0 * cm
MARGIN_R = 2.5 * cm
MARGIN_T = 2.5 * cm
MARGIN_B = 2.5 * cm
USABLE_W = PAGE_W - MARGIN_L - MARGIN_R   # šířka textového sloupce

NAVY      = colors.HexColor('#1A1A5C')
DARK_GRAY = colors.HexColor('#444444')
LIGHT_GRAY= colors.HexColor('#888888')

current_chapter = ['']

def draw_header_footer(canvas_obj, doc):
    canvas_obj.saveState()
    page_num = doc.page
    if page_num > 1:
        y_line = PAGE_H - MARGIN_T + 5 * mm
        canvas_obj.setStrokeColor(NAVY)
        canvas_obj.setLineWidth(0.5)
        canvas_obj.line(MARGIN_L, y_line, PAGE_W - MARGIN_R, y_line)
        canvas_obj.setFont('LiberSerif-Ital', 9)
        canvas_obj.setFillColor(DARK_GRAY)
        canvas_obj.drawString(MARGIN_L, y_line + 2 * mm, 'Dveře ve zdi')
        canvas_obj.drawRightString(PAGE_W - MARGIN_R, y_line + 2 * mm, current_chapter[0])
        canvas_obj.setFont('LiberSerif', 9)
        canvas_obj.setFillColor(LIGHT_GRAY)
        canvas_obj.drawCentredString(PAGE_W / 2, MARGIN_B - 7 * mm, str(page_num))
    canvas_obj.restoreState()

# ── Styly odstavců ────────────────────────────────────────────────────────────
def make_styles():
    s = {}
    s['body'] = ParagraphStyle(
        'body', fontName='LiberSerif', fontSize=12, leading=18,
        alignment=TA_JUSTIFY, firstLineIndent=1.25*cm,
        spaceBefore=0, spaceAfter=6,
    )
    s['body_first'] = ParagraphStyle(
        'body_first', parent=s['body'], firstLineIndent=0, spaceAfter=6,
    )
    s['chapter'] = ParagraphStyle(
        'chapter', fontName='LiberSerif-Bold', fontSize=18, leading=24,
        alignment=TA_LEFT, textColor=NAVY, spaceBefore=0, spaceAfter=14,
    )
    s['subtitle'] = ParagraphStyle(
        'subtitle', fontName='LiberSerif-BI', fontSize=12, leading=16,
        alignment=TA_LEFT, textColor=DARK_GRAY, spaceBefore=0, spaceAfter=10,
    )
    s['translator'] = ParagraphStyle(
        'translator', fontName='LiberSerif-Ital', fontSize=10, leading=14,
        alignment=TA_LEFT, textColor=DARK_GRAY, spaceBefore=0, spaceAfter=12,
    )
    s['footnote'] = ParagraphStyle(
        'footnote', fontName='LiberSerif-Ital', fontSize=10, leading=14,
        alignment=TA_JUSTIFY, textColor=DARK_GRAY,
        spaceBefore=6, spaceAfter=6, leftIndent=1*cm,
    )
    s['separator'] = ParagraphStyle(
        'separator', fontName='LiberSerif', fontSize=11, leading=16,
        alignment=TA_CENTER, spaceBefore=8, spaceAfter=8,
    )
    s['title_main'] = ParagraphStyle(
        'title_main', fontName='LiberSerif-Bold', fontSize=36, leading=44,
        alignment=TA_CENTER, textColor=NAVY, spaceBefore=0, spaceAfter=16,
    )
    s['title_sub'] = ParagraphStyle(
        'title_sub', fontName='LiberSerif-Ital', fontSize=14, leading=20,
        alignment=TA_CENTER, textColor=DARK_GRAY, spaceBefore=0, spaceAfter=0,
    )
    return s

# ── Extrakce obrázků z DOCX ───────────────────────────────────────────────────
def extract_images(docx_path, out_dir):
    """Vrátí dict: paragraph_index -> cesta k obrázku na disku."""
    os.makedirs(out_dir, exist_ok=True)

    # Extrahovat mediální soubory
    with zipfile.ZipFile(docx_path) as z:
        media_files = [f for f in z.namelist() if f.startswith('word/media/')]
        for mf in media_files:
            z.extract(mf, out_dir)

    # Sestavit mapování rId -> soubor
    doc = Document(docx_path)
    rid_to_file = {}
    for rid, rel in doc.part.rels.items():
        if 'image' in rel.reltype:
            fname = os.path.basename(rel.target_ref)
            rid_to_file[rid] = os.path.join(out_dir, 'word', 'media', fname)

    # Najít pozice obrázků v odstavcích
    para_to_img = {}
    for i, para in enumerate(doc.paragraphs):
        blips = para._element.findall(
            './/{http://schemas.openxmlformats.org/drawingml/2006/main}blip'
        )
        for blip in blips:
            rid = blip.get(
                '{http://schemas.openxmlformats.org/officeDocument/2006/relationships}embed'
            )
            if rid and rid in rid_to_file:
                para_to_img[i] = rid_to_file[rid]
                break  # jeden obrázek na odstavec
    return para_to_img, doc

def make_image_flowable(img_path, max_width=None, max_height=None):
    """Vytvoří ReportLab Image s proporcionálním škálováním."""
    if max_width is None:
        max_width = USABLE_W
    if max_height is None:
        max_height = 18 * cm

    # GIF konverze přes Pillow pokud je třeba
    if img_path.lower().endswith('.gif'):
        try:
            from PIL import Image as PILImage
            gif = PILImage.open(img_path)
            png_path = img_path + '.png'
            gif.convert('RGB').save(png_path, 'PNG')
            img_path = png_path
        except Exception:
            return None

    try:
        img = Image(img_path)
        w, h = img.imageWidth, img.imageHeight
        scale = min(max_width / w, max_height / h, 1.0)
        img.drawWidth  = w * scale
        img.drawHeight = h * scale
        img.hAlign = 'CENTER'
        return img
    except Exception as e:
        print(f'  Varování: nelze načíst obrázek {img_path}: {e}')
        return None

# ── Sestavení obsahu PDF ──────────────────────────────────────────────────────
def build_story(styles, docx_path, img_dir):
    story = []
    SEPARATOR_RE = re.compile(r'^[-—*\s]+$')

    para_to_img, doc = extract_images(docx_path, img_dir)
    paras = list(doc.paragraphs)

    # Titulní strana
    story.append(Spacer(1, 7*cm))
    story.append(Paragraph(escape_rl('Dveře ve zdi'), styles['title_main']))
    story.append(Spacer(1, 0.5*cm))
    story.append(Paragraph(escape_rl('Překlad: Hamilbar'), styles['title_sub']))
    story.append(PageBreak())

    is_first_in_chapter = True

    for i, para in enumerate(paras[1:], start=1):
        sname = para.style.name
        text  = fix_text(para.text)
        has_image = i in para_to_img

        # ── Nadpis kapitoly ──────────────────────────────────────────────────
        if sname == 'Heading 1':
            chapter_label = normalize_chapter_title(text)
            current_chapter[0] = chapter_label
            story.append(PageBreak())
            story.append(Paragraph(escape_rl(chapter_label), styles['chapter']))
            is_first_in_chapter = True
            continue

        # ── Obrázek (odstavec bez textu, nebo s obrázkem + textem) ──────────
        if has_image:
            # Nejdříve případný text odstavce
            if text:
                style = styles['body_first'] if is_first_in_chapter else styles['body']
                story.append(Paragraph(escape_rl(text), style))
                is_first_in_chapter = False
            # Pak obrázek
            img_flow = make_image_flowable(para_to_img[i])
            if img_flow:
                story.append(Spacer(1, 6))
                story.append(img_flow)
                story.append(Spacer(1, 12))
            is_first_in_chapter = False
            continue

        # ── Prázdný odstavec ─────────────────────────────────────────────────
        if not text:
            continue

        # ── Poznámka překladatele ─────────────────────────────────────────────
        if (text.startswith('Přeložil Hamilbar') or
                text in ('Vzato odtud.', 'Vzato odtud', 'Převzato odtud', 'Převzato odtud.') or
                re.match(r'^Přeložil Hamilbar[,.]', text)):
            text = re.sub(r'https?://\S+', '', text).strip().rstrip(',').strip()
            if text:
                story.append(Paragraph(escape_rl(text), styles['translator']))
            is_first_in_chapter = True
            continue

        # ── HTTP odkaz – přeskočit ────────────────────────────────────────────
        if re.match(r'https?://', text):
            continue

        # ── Podnadpis kapitoly ────────────────────────────────────────────────
        if (sname == 'Normal (Web)' and len(text) <= 90 and
                not text[0].islower() and
                any(kw in text for kw in [
                    '"Čestný politik"', 'Virtuózní finta', 'Poprava carské',
                    'Náčelník Mkwawa',
                ]) and is_first_in_chapter):
            story.append(Paragraph(escape_rl(text), styles['subtitle']))
            continue

        # ── Oddělovač (———, ***) ─────────────────────────────────────────────
        if SEPARATOR_RE.match(text) and len(text) >= 3:
            story.append(Paragraph('* * *', styles['separator']))
            is_first_in_chapter = False
            continue

        # ── Poznámka pod čarou (začíná (*)) ──────────────────────────────────
        if text.startswith('(*)'):
            story.append(Paragraph(escape_rl(text), styles['footnote']))
            is_first_in_chapter = False
            continue

        # ── Běžný odstavec ────────────────────────────────────────────────────
        style = styles['body_first'] if is_first_in_chapter else styles['body']
        story.append(Paragraph(escape_rl(text), style))
        is_first_in_chapter = False

    return story

# ── Hlavní funkce ─────────────────────────────────────────────────────────────
def main():
    docx_path = '/root/.claude/uploads/b6ddfa66-7ca7-5cf7-a068-28b452764c46/bedb246b-Dve_e_ve_zdi.docx'
    out_pdf   = '/home/user/SoulBrowser/Dvere_ve_zdi_reformatovano.pdf'
    img_dir   = '/tmp/docx_img_extract'

    styles = make_styles()
    story  = build_story(styles, docx_path, img_dir)

    doc = SimpleDocTemplate(
        out_pdf, pagesize=A4,
        leftMargin=MARGIN_L, rightMargin=MARGIN_R,
        topMargin=MARGIN_T + 1.0*cm, bottomMargin=MARGIN_B,
        title='Dveře ve zdi',
        author='Hamilbar (překlad)',
        subject='Geopolitická esej',
    )
    doc.build(story, onFirstPage=draw_header_footer, onLaterPages=draw_header_footer)
    print(f'PDF uložen: {out_pdf}')

if __name__ == '__main__':
    main()
