#!/usr/bin/env python3
"""Reformats and proofreads the Czech DOCX document, then saves as new DOCX."""

import re
from docx import Document
from docx.shared import Pt, Inches, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

# ── Typo corrections ──────────────────────────────────────────────────────────
CORRECTIONS = [
    # Missing diacritics / misspellings
    ("demogafických",    "demografických"),
    ("mebyl",            "nebyl"),
    ("výjímkou",         "výjimkou"),
    ("francouzkého",     "francouzského"),
    ("Marcus Harvey",    "Marcus Garvey"),
    # \xa0 (non-breaking space) → regular space inside sentences
    # handled separately below
    # Capitalization: 'Únoru' mid-sentence
    # handled in per-paragraph logic below
]

def fix_text(text: str) -> str:
    """Apply all corrections to a paragraph text."""
    # Replace non-breaking spaces with regular spaces
    text = text.replace("\xa0", " ")
    # Apply corrections
    for wrong, right in CORRECTIONS:
        text = text.replace(wrong, right)
    # Fix 'v Únoru letošního roku' → 'v únoru letošního roku'
    text = re.sub(r'\bv Únoru\b', 'v únoru', text)
    # Collapse multiple spaces
    text = re.sub(r'  +', ' ', text)
    return text.strip()

def normalize_chapter_title(text: str) -> str:
    """Normalise chapter heading to 'Dveře ve zdi – N [subtitle]'."""
    text = text.replace("\xa0", " ").strip()
    # Match patterns like "Dveře ve zdi 25", "Dveře ve zdi – 25", "Dveře ve zdi –6"
    m = re.match(
        r'Dveře ve zdi\s*[-–—]?\s*(\d+)(.*)',
        text, re.IGNORECASE
    )
    if m:
        num = m.group(1)
        rest = m.group(2).strip()
        # rest may start with ", neboli …" or be empty
        if rest and not rest.startswith(',') and not rest.startswith('–') and not rest.startswith('-'):
            rest = ', ' + rest
        return f"Dveře ve zdi – {num}{rest}"
    # Fallback: just clean non-breaking spaces
    return text

def set_paragraph_format(para, first_line_indent=False, space_before=0, space_after=6):
    """Apply paragraph formatting."""
    pf = para.paragraph_format
    pf.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    pf.space_before = Pt(space_before)
    pf.space_after = Pt(space_after)
    if first_line_indent:
        pf.first_line_indent = Cm(1.25)
    else:
        pf.first_line_indent = Pt(0)

def add_page_break(doc):
    para = doc.add_paragraph()
    run = para.add_run()
    br = OxmlElement('w:br')
    br.set(qn('w:type'), 'page')
    run._r.append(br)
    para.paragraph_format.space_before = Pt(0)
    para.paragraph_format.space_after = Pt(0)


# ── Build new document ────────────────────────────────────────────────────────
orig = Document('/root/.claude/uploads/b6ddfa66-7ca7-5cf7-a068-28b452764c46/bedb246b-Dve_e_ve_zdi.docx')
doc  = Document()

# ── Page margins ──────────────────────────────────────────────────────────────
section = doc.sections[0]
section.top_margin    = Cm(2.5)
section.bottom_margin = Cm(2.5)
section.left_margin   = Cm(3.0)
section.right_margin  = Cm(2.5)

# ── Style defaults ────────────────────────────────────────────────────────────
normal_style = doc.styles['Normal']
normal_style.font.name = 'Times New Roman'
normal_style.font.size = Pt(12)

h1_style = doc.styles['Heading 1']
h1_style.font.name = 'Times New Roman'
h1_style.font.size = Pt(16)
h1_style.font.bold = True
h1_style.font.color.rgb = RGBColor(0x1A, 0x1A, 0x5C)
h1_style.paragraph_format.space_before = Pt(18)
h1_style.paragraph_format.space_after  = Pt(10)
h1_style.paragraph_format.alignment    = WD_ALIGN_PARAGRAPH.LEFT

# ── Title page ────────────────────────────────────────────────────────────────
tp = doc.add_paragraph()
tp.alignment = WD_ALIGN_PARAGRAPH.CENTER
tp.paragraph_format.space_before = Pt(120)
tp.paragraph_format.space_after  = Pt(20)
run = tp.add_run("Dveře ve zdi")
run.font.name = 'Times New Roman'
run.font.size = Pt(32)
run.font.bold = True
run.font.color.rgb = RGBColor(0x1A, 0x1A, 0x5C)

sub = doc.add_paragraph()
sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
sub.paragraph_format.space_before = Pt(0)
sub.paragraph_format.space_after  = Pt(0)
run2 = sub.add_run("Překlad a komentáře: Hamilbar")
run2.font.name = 'Times New Roman'
run2.font.size = Pt(13)
run2.font.italic = True
run2.font.color.rgb = RGBColor(0x44, 0x44, 0x44)

add_page_break(doc)

# ── Process original paragraphs ───────────────────────────────────────────────
paras = list(orig.paragraphs)
# Skip paragraph 0 (plain title "Dveře ve zdi") – already on title page
start = 1

# Track whether current para is first in chapter (no first-line indent)
is_first_in_chapter = True
# For separator lines (—— or ***) use centered style
SEPARATOR_RE = re.compile(r'^[-—*\s]+$')

chapter_count = 0

for para in paras[start:]:
    style_name = para.style.name
    text = fix_text(para.text)

    # Skip blank paragraphs (keep only one blank between sections)
    if not text:
        continue

    # ── Heading 1 → chapter heading ──────────────────────────────────────────
    if style_name == 'Heading 1':
        if chapter_count > 0:
            add_page_break(doc)
        chapter_count += 1
        heading_text = normalize_chapter_title(text)
        h = doc.add_paragraph(style='Heading 1')
        h.clear()
        run = h.add_run(heading_text)
        run.font.name = 'Times New Roman'
        run.font.size = Pt(16)
        run.font.bold = True
        is_first_in_chapter = True
        continue

    # ── Translator note (first "Přeložil…" in each chapter) ──────────────────
    if text.startswith('Přeložil Hamilbar') or text.startswith('Vzato odtud') or \
       text.startswith('Převzato odtud') or text == 'Vzato odtud.' or \
       text.startswith('Přeložil Hamilbar, vzato') or text.startswith('Přeložil Hamilbar, převzato'):
        # Render as small italic note
        note = doc.add_paragraph()
        note.paragraph_format.space_before = Pt(0)
        note.paragraph_format.space_after  = Pt(10)
        note.paragraph_format.alignment    = WD_ALIGN_PARAGRAPH.LEFT
        r = note.add_run(text)
        r.font.name   = 'Times New Roman'
        r.font.size   = Pt(10)
        r.font.italic = True
        r.font.color.rgb = RGBColor(0x55, 0x55, 0x55)
        is_first_in_chapter = True
        continue

    # ── HTTP links – skip ─────────────────────────────────────────────────────
    if text.startswith('http://') or text.startswith('https://'):
        continue

    # ── Separator lines (—————, ***) ─────────────────────────────────────────
    if SEPARATOR_RE.match(text) and len(text) >= 3:
        sep = doc.add_paragraph()
        sep.alignment = WD_ALIGN_PARAGRAPH.CENTER
        sep.paragraph_format.space_before = Pt(6)
        sep.paragraph_format.space_after  = Pt(6)
        r = sep.add_run('* * *')
        r.font.size = Pt(11)
        r.font.name = 'Times New Roman'
        is_first_in_chapter = False
        continue

    # ── Inline headings / subchapter titles (short lines in "Normal (Web)" that ──
    # look like titles – e.g. "Archa, Noe a žirafa.", "Cui prodest?" etc.) ────
    # Heuristic: text shorter than 80 chars, ends with '.', '?' or '!'  and
    # the previous paragraph was also short (chapter-opening style)
    # We treat them as bold sub-headings only if <= 60 chars and no comma-heavy text
    is_subheading = (
        len(text) <= 80 and
        not text[0].islower() and
        (text.endswith('.') or text.endswith('?') or text.endswith('!') or
         text.endswith('"') or text.endswith('"')) and
        ',' not in text[:40] and
        not text.startswith('(') and
        # some known sub-headings
        any(kw in text for kw in ['Archa', 'Cui prodest', 'Bolševici', 'Dohoda',
                                   '"Čestný', 'Virtuózní', 'Poprava', 'Náčelník',
                                   'Angličané', 'Je možné'])
    )
    if is_subheading:
        sh = doc.add_paragraph()
        sh.paragraph_format.space_before = Pt(10)
        sh.paragraph_format.space_after  = Pt(4)
        sh.paragraph_format.alignment    = WD_ALIGN_PARAGRAPH.LEFT
        r = sh.add_run(text)
        r.font.name = 'Times New Roman'
        r.font.size = Pt(12)
        r.font.bold = True
        is_first_in_chapter = False
        continue

    # ── Footnote / endnote marker paragraphs (start with '(*)') ──────────────
    if text.startswith('(*)'):
        fn = doc.add_paragraph()
        fn.paragraph_format.space_before = Pt(6)
        fn.paragraph_format.space_after  = Pt(6)
        fn.paragraph_format.alignment    = WD_ALIGN_PARAGRAPH.JUSTIFY
        r = fn.add_run(text)
        r.font.name   = 'Times New Roman'
        r.font.size   = Pt(10)
        r.font.italic = True
        is_first_in_chapter = False
        continue

    # ── Regular body paragraph ────────────────────────────────────────────────
    p = doc.add_paragraph()
    p.paragraph_format.alignment    = WD_ALIGN_PARAGRAPH.JUSTIFY
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after  = Pt(6)
    if is_first_in_chapter:
        p.paragraph_format.first_line_indent = Pt(0)
    else:
        p.paragraph_format.first_line_indent = Cm(1.25)

    r = p.add_run(text)
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    is_first_in_chapter = False

# ── Save ──────────────────────────────────────────────────────────────────────
out_docx = '/home/user/SoulBrowser/Dvere_ve_zdi_reformatovano.docx'
doc.save(out_docx)
print(f"Saved: {out_docx}")
