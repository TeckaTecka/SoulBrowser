#!/usr/bin/env python3
"""Reformatuje a opravuje český DOCX dokument včetně obrázků."""

import re, zipfile, os
from io import BytesIO
from docx import Document
from docx.shared import Pt, Cm, RGBColor, Inches
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

DOCX_PATH = '/root/.claude/uploads/b6ddfa66-7ca7-5cf7-a068-28b452764c46/bedb246b-Dve_e_ve_zdi.docx'

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
        num  = m.group(1)
        rest = m.group(2).strip()
        if rest and not rest.startswith(',') and not rest.startswith('–'):
            rest = ', ' + rest
        return f"Dveře ve zdi – {num}{rest}"
    return text

def add_page_break(doc):
    para = doc.add_paragraph()
    run  = para.add_run()
    br   = OxmlElement('w:br')
    br.set(qn('w:type'), 'page')
    run._r.append(br)
    para.paragraph_format.space_before = Pt(0)
    para.paragraph_format.space_after  = Pt(0)

# ── Mapování obrázků: index odstavce → (jméno souboru, binární data) ─────────
def get_image_map(docx_path):
    orig = Document(docx_path)

    # rId → binární data obrázku
    rid_to_data = {}
    with zipfile.ZipFile(docx_path) as z:
        for rid, rel in orig.part.rels.items():
            if 'image' in rel.reltype:
                fname = 'word/' + rel.target_ref.lstrip('/')
                try:
                    rid_to_data[rid] = (os.path.basename(fname), z.read(fname))
                except Exception:
                    pass

    # index odstavce → (fname, data)
    para_to_img = {}
    for i, para in enumerate(orig.paragraphs):
        blips = para._element.findall(
            './/{http://schemas.openxmlformats.org/drawingml/2006/main}blip'
        )
        for blip in blips:
            rid = blip.get(
                '{http://schemas.openxmlformats.org/officeDocument/2006/relationships}embed'
            )
            if rid and rid in rid_to_data:
                para_to_img[i] = rid_to_data[rid]
                break
    return para_to_img, orig

def add_image_paragraph(doc, img_data, fname):
    """Vloží obrázek do dokumentu na střed, max šířka = 14 cm."""
    try:
        # GIF → PNG přes Pillow
        if fname.lower().endswith('.gif'):
            from PIL import Image as PILImage
            buf_in  = BytesIO(img_data)
            buf_out = BytesIO()
            PILImage.open(buf_in).convert('RGB').save(buf_out, 'PNG')
            buf_out.seek(0)
            stream = buf_out
        else:
            stream = BytesIO(img_data)

        p   = doc.add_paragraph()
        p.paragraph_format.alignment    = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_before = Pt(8)
        p.paragraph_format.space_after  = Pt(8)
        run = p.add_run()
        run.add_picture(stream, width=Cm(14))
    except Exception as e:
        print(f'  Varování: nelze vložit obrázek {fname}: {e}')

# ── Sestavení nového dokumentu ────────────────────────────────────────────────
para_to_img, orig = get_image_map(DOCX_PATH)
doc = Document()

# Okraje
section = doc.sections[0]
section.top_margin    = Cm(2.5)
section.bottom_margin = Cm(2.5)
section.left_margin   = Cm(3.0)
section.right_margin  = Cm(2.5)

# Výchozí styly
normal_style = doc.styles['Normal']
normal_style.font.name = 'Times New Roman'
normal_style.font.size = Pt(12)

h1_style = doc.styles['Heading 1']
h1_style.font.name  = 'Times New Roman'
h1_style.font.size  = Pt(16)
h1_style.font.bold  = True
h1_style.font.color.rgb = RGBColor(0x1A, 0x1A, 0x5C)
h1_style.paragraph_format.space_before = Pt(18)
h1_style.paragraph_format.space_after  = Pt(10)
h1_style.paragraph_format.alignment    = WD_ALIGN_PARAGRAPH.LEFT

# Titulní strana
tp = doc.add_paragraph()
tp.alignment = WD_ALIGN_PARAGRAPH.CENTER
tp.paragraph_format.space_before = Pt(120)
tp.paragraph_format.space_after  = Pt(20)
r = tp.add_run("Dveře ve zdi")
r.font.name = 'Times New Roman'; r.font.size = Pt(32)
r.font.bold = True; r.font.color.rgb = RGBColor(0x1A, 0x1A, 0x5C)

sub = doc.add_paragraph()
sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
sub.paragraph_format.space_before = Pt(0)
sub.paragraph_format.space_after  = Pt(0)
r2 = sub.add_run("Překlad a komentáře: Hamilbar")
r2.font.name = 'Times New Roman'; r2.font.size = Pt(13)
r2.font.italic = True; r2.font.color.rgb = RGBColor(0x44, 0x44, 0x44)

add_page_break(doc)

# ── Průchod odstavci ──────────────────────────────────────────────────────────
SEPARATOR_RE   = re.compile(r'^[-—*\s]+$')
is_first_in_chapter = True
chapter_count  = 0
paras = list(orig.paragraphs)

for i, para in enumerate(paras[1:], start=1):
    sname      = para.style.name
    text       = fix_text(para.text)
    has_image  = i in para_to_img

    # ── Prázdný odstavec s obrázkem ──────────────────────────────────────────
    if has_image and not text:
        fname, data = para_to_img[i]
        add_image_paragraph(doc, data, fname)
        is_first_in_chapter = False
        continue

    # ── Prázdný odstavec bez obrázku ─────────────────────────────────────────
    if not text:
        continue

    # ── Nadpis kapitoly ───────────────────────────────────────────────────────
    if sname == 'Heading 1':
        if chapter_count > 0:
            add_page_break(doc)
        chapter_count += 1
        h = doc.add_paragraph(style='Heading 1')
        h.clear()
        r = h.add_run(normalize_chapter_title(text))
        r.font.name = 'Times New Roman'; r.font.size = Pt(16); r.font.bold = True
        is_first_in_chapter = True
        # obrázek těsně za nadpisem (vzácné)
        if has_image:
            fname, data = para_to_img[i]
            add_image_paragraph(doc, data, fname)
            is_first_in_chapter = False
        continue

    # ── Poznámka překladatele ─────────────────────────────────────────────────
    if (text.startswith('Přeložil Hamilbar') or
            text in ('Vzato odtud.', 'Vzato odtud', 'Převzato odtud', 'Převzato odtud.') or
            re.match(r'^Přeložil Hamilbar[,.]', text)):
        text = re.sub(r'https?://\S+', '', text).strip().rstrip(',').strip()
        if text:
            note = doc.add_paragraph()
            note.paragraph_format.space_before = Pt(0)
            note.paragraph_format.space_after  = Pt(10)
            note.paragraph_format.alignment    = WD_ALIGN_PARAGRAPH.LEFT
            r = note.add_run(text)
            r.font.name = 'Times New Roman'; r.font.size = Pt(10)
            r.font.italic = True; r.font.color.rgb = RGBColor(0x55, 0x55, 0x55)
        is_first_in_chapter = True
        continue

    # ── HTTP odkaz – přeskočit ────────────────────────────────────────────────
    if text.startswith('http://') or text.startswith('https://'):
        continue

    # ── Oddělovač ─────────────────────────────────────────────────────────────
    if SEPARATOR_RE.match(text) and len(text) >= 3:
        sep = doc.add_paragraph()
        sep.alignment = WD_ALIGN_PARAGRAPH.CENTER
        sep.paragraph_format.space_before = Pt(6)
        sep.paragraph_format.space_after  = Pt(6)
        r = sep.add_run('* * *')
        r.font.name = 'Times New Roman'; r.font.size = Pt(11)
        is_first_in_chapter = False
        continue

    # ── Podnadpis kapitoly ────────────────────────────────────────────────────
    is_subheading = (
        len(text) <= 90 and not text[0].islower() and
        any(kw in text for kw in [
            '"Čestný politik"', 'Virtuózní finta', 'Poprava carské', 'Náčelník Mkwawa',
        ]) and is_first_in_chapter
    )
    if is_subheading:
        sh = doc.add_paragraph()
        sh.paragraph_format.space_before = Pt(0)
        sh.paragraph_format.space_after  = Pt(8)
        sh.paragraph_format.alignment    = WD_ALIGN_PARAGRAPH.LEFT
        r = sh.add_run(text)
        r.font.name = 'Times New Roman'; r.font.size = Pt(12)
        r.font.bold = True; r.font.italic = True
        r.font.color.rgb = RGBColor(0x44, 0x44, 0x44)
        continue

    # ── Poznámka pod čarou ────────────────────────────────────────────────────
    if text.startswith('(*)'):
        fn = doc.add_paragraph()
        fn.paragraph_format.space_before = Pt(6)
        fn.paragraph_format.space_after  = Pt(6)
        fn.paragraph_format.alignment    = WD_ALIGN_PARAGRAPH.JUSTIFY
        r = fn.add_run(text)
        r.font.name = 'Times New Roman'; r.font.size = Pt(10); r.font.italic = True
        is_first_in_chapter = False
        continue

    # ── Běžný odstavec (+ případný obrázek za textem) ────────────────────────
    p = doc.add_paragraph()
    p.paragraph_format.alignment    = WD_ALIGN_PARAGRAPH.JUSTIFY
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after  = Pt(6)
    p.paragraph_format.first_line_indent = Pt(0) if is_first_in_chapter else Cm(1.25)
    r = p.add_run(text)
    r.font.name = 'Times New Roman'; r.font.size = Pt(12)
    is_first_in_chapter = False

    if has_image:
        fname, data = para_to_img[i]
        add_image_paragraph(doc, data, fname)

# ── Uložit ────────────────────────────────────────────────────────────────────
out_docx = '/home/user/SoulBrowser/Dvere_ve_zdi_reformatovano.docx'
doc.save(out_docx)
print(f'DOCX uložen: {out_docx}')
