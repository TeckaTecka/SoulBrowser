# KREZBO – nový web

Jednoduchý web postavený na **PHP 8.4 + HTML** (žádná databáze). Celý veřejný web je
jedna stránka `index.php` se sekcemi **O nás / Poptávka / Galerie / Kontakt**.

## Nasazení na hosting (Webglobe)

Nahrajte **obsah složky `krezbo-new/`** do kořene webu (např. `public_html/www/`, tam
kam míří doména `krezbo.cz`). Struktura:

```
index.php          – hlavní stránka (vše na jedné stránce)
admin.php          – správa oznámení (chráněno heslem)
config.php         – nastavení (e-mail, heslo, API klíč)
oznameni.txt       – text drobného oznámení na úvodu (spravuje se přes admin.php)
.htaccess          – základní nastavení
robots.txt
css/ js/ img/ data/ – styly, skripty, obrázky, galerie, loga firem
```

Ověřte, že hosting má nastavené **PHP 8.4** a že soubor `oznameni.txt` je **zapisovatelný**
(obvykle práva 644, adresář 755 – pod stejným uživatelem jako PHP to funguje bez úprav).

## Poptávkový formulář

Odesílá e-mail na adresu v `config.php` → `KREZBO_MAIL_TO` (výchozí `krezbo@krezbo.cz`)
pomocí PHP funkce `mail()`. Odesílatel je `krezbo@krezbo.cz`, odpověď (Reply-To) míří na
adresu tazatele. Chcete-li adresáta změnit, upravte `KREZBO_MAIL_TO`.

> Pokud by e-maily nechodily nebo padaly do spamu, dá se přejít na odesílání přes SMTP
> (přihlašovací údaje schránky). Stačí říct a doplním.

## Drobná oznámení na úvodní stránce („Zítra zavřeno" apod.)

1. Otevřete `https://www.krezbo.cz/admin.php`
2. Přihlaste se heslem (výchozí: **`krezbo2026`** – viz níže, změňte si ho!)
3. Napište text a **Uložit**. Oznámení se hned zobrazí nahoře na úvodní stránce.
4. Zrušení oznámení: pole vymažte a **Uložit**.

Alternativně jde obsah zapsat přímo do souboru `oznameni.txt` (přes FTP). Prázdný soubor =
žádné oznámení.

## Prodejní doba (Kontakt)

Otevírací dobu lze upravovat ve stejné administraci `admin.php` – sekce **Prodejní doba**.
Pro každý den vyplňte hodiny (např. `8:30–12:00, 13:00–16:00`) nebo `Zavřeno` a uložte.
Ukládá se do souboru `prodejni-doba.txt` (musí být zapisovatelný, stejně jako `oznameni.txt`).

### Změna hesla do administrace

1. Otevřete `https://www.krezbo.cz/admin.php?heslo=VASE_NOVE_HESLO`
   – stránka vypíše zašifrovaný řetězec (`$2y$...`).
2. Tento řetězec vložte v `config.php` do `KREZBO_ADMIN_HASH`.

## Mapa a Street View (Kontakt)

Mapa se zobrazuje automaticky (bez klíče). Tlačítko „Zobrazit Street View" otevře náhled
na Google mapách. Chcete-li **vloženou** panoramu Street View přímo na stránce, doplňte do
`config.php` → `KREZBO_MAPS_API_KEY` bezplatný Google Maps Embed API klíč.

## Zastoupení firem

Seznam se upravuje v `index.php` v poli `$partners` (logo v `data/logos/`, název, odkaz).
Je zde připravené místo pro **3 nové firmy** (viz komentář v kódu).
