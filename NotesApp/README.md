# Poznámky (Android)

Náhrada aplikace **My Notes (vitalypanov.mynotes.pro)** – bez reklam, bez internetu, se stejnými widgety.

## Co umí (fáze 1)
- Záložky (Obecné / Práce / Domov + vlastní, barvy, řazení), přepínání swipem
- Textové poznámky a seznamy (checklist), převod text ⇄ seznam
- Barva poznámky (stejná paleta), velikost a barva písma, připnutí, režim jen pro čtení
- Hledání, řazení (datum / název), mřížka nebo seznam, koš s obnovou
- Připomínky / kalendář: denně, týdně, 2 a 4 týdny, měsíčně, 2 měsíce, čtvrtletí, pololetí, ročně, jednorázově + notifikace
- Záložka Kalendář (měsíční přehled + poznámky dne)
- Widgety:
  - **Poznámka** – bílá / černá / průhledná; obsah poznámky, odškrtávání položek přímo na ploše, tlačítka „vybrat poznámku“ a „nová“
  - **Poznámky na den** – všechny poznámky s připomínkou na daný den, listování dny
  - **Kalendář – poznámka dne** – jedna poznámka dne, listování dny i poznámkami
- Zálohy `.bak` – **stejný formát jako My Notes** (kopie SQLite `mynotes.db`), funguje oběma směry

## Přechod ze staré aplikace
1. V My Notes: Nastavení → Zálohovat (vznikne soubor `RRRRMMDD_…bak`).
2. Tady: ⋮ → Nastavení a zálohy → Obnovit ze zálohy → vybrat ten `.bak`.

## Zatím chybí (fáze 2)
Přílohy (obrázky, audio – data se ale při importu zachovají), formátování textu (tučné apod.),
PIN / otisk prstu, cloudová synchronizace, vlastní fonty, přetahování položek seznamu.

## Sestavení
APK sestavuje GitHub Actions (workflow „Poznámky – sestavení APK“) – ke stažení v záložce Actions → Artifacts.
Lokálně: `./gradlew assembleRelease` (vyžaduje Android SDK).

Aplikace je podepsaná klíčem `app/debug.keystore` uloženým v repu, aby šly aktualizace instalovat přes sebe.
Kdokoli s přístupem k repu tím klíčem může podepsat APK – pro soukromou appku to stačí, pro zveřejnění je potřeba vlastní klíč.
