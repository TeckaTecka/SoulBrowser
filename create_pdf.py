#!/usr/bin/env python3
"""Creates a professionally formatted PDF from the Czech DOCX document using ReportLab."""

import re
from docx import Document
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import cm, mm
from reportlab.lib import colors
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, PageBreak, HRFlowable, KeepTogether
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_JUSTIFY, TA_CENTER, TA_LEFT
from reportlab.platypus import BaseDocTemplate, Frame, PageTemplate

# ── Register fonts ────────────────────────────────────────────────────────────
FONT_DIR = '/usr/share/fonts/truetype/liberation/'
pdfmetrics.registerFont(TTFont('LiberSerif',      FONT_DIR + 'LiberationSerif-Regular.ttf'))
pdfmetrics.registerFont(TTFont('LiberSerif-Bold', FONT_DIR + 'LiberationSerif-Bold.ttf'))
pdfmetrics.registerFont(TTFont('LiberSerif-Ital', FONT_DIR + 'LiberationSerif-Italic.ttf'))
pdfmetrics.registerFont(TTFont('LiberSerif-BI',   FONT_DIR + 'LiberationSerif-BoldItalic.ttf'))

# ── Typo corrections ──────────────────────────────────────────────────────────
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
    """Escape characters that ReportLab's Paragraph treats as markup."""
    text = text.replace('&', '&amp;')
    text = text.replace('<', '&lt;')
    text = text.replace('>', '&gt;')
    return text

# ── Page layout with header ───────────────────────────────────────────────────
PAGE_W, PAGE_H = A4
MARGIN_L = 3.0 * cm
MARGIN_R = 2.5 * cm
MARGIN_T = 2.5 * cm
MARGIN_B = 2.5 * cm
HEADER_H = 1.0 * cm

NAVY = colors.HexColor('#1A1A5C')
DARK_GRAY = colors.HexColor('#444444')
LIGHT_GRAY = colors.HexColor('#888888')

current_chapter = ['']  # mutable for closure

def draw_header_footer(canvas_obj, doc):
    canvas_obj.saveState()
    page_num = doc.page
    if page_num > 1:
        # Header line
        canvas_obj.setStrokeColor(NAVY)
        canvas_obj.setLineWidth(0.5)
        y_line = PAGE_H - MARGIN_T + 5 * mm
        canvas_obj.line(MARGIN_L, y_line, PAGE_W - MARGIN_R, y_line)
        # Header text
        canvas_obj.setFont('LiberSerif-Ital', 9)
        canvas_obj.setFillColor(DARK_GRAY)
        canvas_obj.drawString(MARGIN_L, y_line + 2 * mm, 'Dveře ve zdi')
        canvas_obj.drawRightString(PAGE_W - MARGIN_R, y_line + 2 * mm, current_chapter[0])
        # Footer
        canvas_obj.setFont('LiberSerif', 9)
        canvas_obj.setFillColor(LIGHT_GRAY)
        canvas_obj.drawCentredString(PAGE_W / 2, MARGIN_B - 7 * mm, str(page_num))
    canvas_obj.restoreState()

# ── Styles ────────────────────────────────────────────────────────────────────
def make_styles():
    s = {}

    s['body'] = ParagraphStyle(
        'body',
        fontName='LiberSerif', fontSize=12, leading=18,
        alignment=TA_JUSTIFY,
        firstLineIndent=1.25 * cm,
        spaceBefore=0, spaceAfter=6,
        leftIndent=0, rightIndent=0,
    )
    s['body_first'] = ParagraphStyle(
        'body_first',
        parent=s['body'],
        firstLineIndent=0,
        spaceAfter=6,
    )
    s['chapter'] = ParagraphStyle(
        'chapter',
        fontName='LiberSerif-Bold', fontSize=18, leading=24,
        alignment=TA_LEFT,
        textColor=NAVY,
        spaceBefore=0, spaceAfter=14,
        leftIndent=0,
    )
    s['subtitle'] = ParagraphStyle(
        'subtitle',
        fontName='LiberSerif-BI', fontSize=12, leading=16,
        alignment=TA_LEFT,
        textColor=DARK_GRAY,
        spaceBefore=0, spaceAfter=10,
    )
    s['translator'] = ParagraphStyle(
        'translator',
        fontName='LiberSerif-Ital', fontSize=10, leading=14,
        alignment=TA_LEFT,
        textColor=DARK_GRAY,
        spaceBefore=0, spaceAfter=12,
    )
    s['footnote'] = ParagraphStyle(
        'footnote',
        fontName='LiberSerif-Ital', fontSize=10, leading=14,
        alignment=TA_JUSTIFY,
        textColor=DARK_GRAY,
        spaceBefore=6, spaceAfter=6,
        leftIndent=1 * cm,
    )
    s['separator'] = ParagraphStyle(
        'separator',
        fontName='LiberSerif', fontSize=11, leading=16,
        alignment=TA_CENTER,
        spaceBefore=8, spaceAfter=8,
    )
    # Title page
    s['title_main'] = ParagraphStyle(
        'title_main',
        fontName='LiberSerif-Bold', fontSize=36, leading=44,
        alignment=TA_CENTER,
        textColor=NAVY,
        spaceBefore=0, spaceAfter=16,
    )
    s['title_sub'] = ParagraphStyle(
        'title_sub',
        fontName='LiberSerif-Ital', fontSize=14, leading=20,
        alignment=TA_CENTER,
        textColor=DARK_GRAY,
        spaceBefore=0, spaceAfter=0,
    )
    return s

# ── Build PDF content ─────────────────────────────────────────────────────────
def build_story(styles):
    story = []
    SEPARATOR_RE = re.compile(r'^[-—*\s]+$')

    orig = Document('/root/.claude/uploads/b6ddfa66-7ca7-5cf7-a068-28b452764c46/bedb246b-Dve_e_ve_zdi.docx')
    paras = list(orig.paragraphs)

    # Title page
    story.append(Spacer(1, 7 * cm))
    story.append(Paragraph(escape_rl('Dveře ve zdi'), styles['title_main']))
    story.append(Spacer(1, 0.5 * cm))
    story.append(Paragraph(escape_rl('Překlad: Hamilbar'), styles['title_sub']))
    story.append(PageBreak())

    is_first_in_chapter = True
    chapter_label = ''

    for para in paras[1:]:  # skip plain title para
        sname = para.style.name
        text = fix_text(para.text)

        if not text:
            continue

        # ── Chapter heading ──────────────────────────────────────────────────
        if sname == 'Heading 1':
            chapter_label = normalize_chapter_title(text)
            current_chapter[0] = chapter_label
            story.append(PageBreak())
            story.append(Paragraph(escape_rl(chapter_label), styles['chapter']))
            is_first_in_chapter = True
            continue

        # ── Translator note ──────────────────────────────────────────────────
        if (text.startswith('Přeložil Hamilbar') or
                text in ('Vzato odtud.', 'Vzato odtud', 'Převzato odtud', 'Převzato odtud.') or
                re.match(r'^Přeložil Hamilbar[,.]', text)):
            # Strip URLs from translator note
            text = re.sub(r'https?://\S+', '', text).strip().rstrip(',').strip()
            if text:
                story.append(Paragraph(escape_rl(text), styles['translator']))
            is_first_in_chapter = True
            continue

        # ── HTTP links – skip ────────────────────────────────────────────────
        if re.match(r'https?://', text):
            continue

        # ── Chapter subtitle (e.g. '"Čestný politik" Woodrow Wilson.') ───────
        if (sname == 'Normal (Web)' and
                len(text) <= 90 and
                not text[0].islower() and
                any(kw in text for kw in [
                    '"Čestný politik"', 'Virtuózní finta', 'Poprava carské',
                    'Náčelník Mkwawa', 'Přeložil Hamilbar',
                ]) and
                is_first_in_chapter):
            story.append(Paragraph(escape_rl(text), styles['subtitle']))
            continue

        # ── Separator (—————, ***) ────────────────────────────────────────────
        if SEPARATOR_RE.match(text) and len(text) >= 3:
            story.append(Paragraph('* * *', styles['separator']))
            is_first_in_chapter = False
            continue

        # ── Footnote (starts with (*)) ────────────────────────────────────────
        if text.startswith('(*)'):
            story.append(Paragraph(escape_rl(text), styles['footnote']))
            is_first_in_chapter = False
            continue

        # ── Regular body paragraph ─────────────────────────────────────────
        style = styles['body_first'] if is_first_in_chapter else styles['body']
        story.append(Paragraph(escape_rl(text), style))
        is_first_in_chapter = False

    return story


# ── Main ──────────────────────────────────────────────────────────────────────
def main():
    out_path = '/home/user/SoulBrowser/Dvere_ve_zdi_reformatovano.pdf'
    styles = make_styles()
    story = build_story(styles)

    doc = SimpleDocTemplate(
        out_path,
        pagesize=A4,
        leftMargin=MARGIN_L,
        rightMargin=MARGIN_R,
        topMargin=MARGIN_T + HEADER_H,
        bottomMargin=MARGIN_B,
        title='Dveře ve zdi',
        author='Hamilbar (překlad)',
        subject='Geopolitická esej',
        creator='Claude Code',
    )
    doc.build(story, onFirstPage=draw_header_footer, onLaterPages=draw_header_footer)
    print(f'PDF saved: {out_path}')

if __name__ == '__main__':
    main()
