# TRANSEUROPE – nový web

Věrný klon původního webu transeurope.cz postavený jako **statické PHP + HTML** (žádná
databáze, žádný CMS). Určeno pro hosting **Wedos** s **PHP 8.x**.

## Nasazení

Nahrajte **obsah složky `transeurope-new/`** do kořene webu (kam míří doména).

```
index.php sluzby.php o-nas.php kontakty.php kariera.php   – stránky
admin.php            – správa cen PHM (chráněno heslem)
config.php           – nastavení (e-mail, heslo, ceny)
ceny-phm.txt         – aktuální ceny PHM (zapisovatelné!)
inc/  css/  js/  img/ – šablony, styl, skripty, obrázky
```

Požadavky na hostingu:
- **PHP 8.x**, povolená funkce `mail()` pro formuláře.
- Soubory **`ceny-phm.txt`** a případně nově vzniklý **`admin.hash`** musí být **zapisovatelné**
  (práva 644), aby šlo ukládat ceny a měnit heslo.

## Ceny PHM (hlavní funkce)

1. Otevřete `https://www.transeurope.cz/admin.php`
2. Přihlaste se (výchozí heslo **`transeurope2026`** – změňte si ho).
3. Upravte 4 ceny (Natural 98, Natural 95, Ultra Diesel, Diesel) a uložte – ihned se projeví
   na úvodní stránce v sekci „Čerpací stanice".

**Změna hesla** je přímo v administraci (sekce Změna hesla), uloží se do `admin.hash`.

## Formuláře

Kontaktní formulář (Kontakty) i přihláška řidiče (Kariéra, včetně přílohy životopisu) posílají
e-mail na adresu v `config.php` → `TE_MAIL_TO` (výchozí `transeurope@transeurope.cz`).

## Obrázky

Fotky (hero, nábor, provozovna, galerie) a loga partnerů jsou převzaté z původního webu ve
webovém rozlišení (bez FTP se stahovaly ze živé verze). Logo TRANSEUROPE je čistá reprodukce.
Pro ostré originály stačí příslušné soubory nahradit v `img/`.
