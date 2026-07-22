<?php
/**
 * Nastavení webu KREZBO
 * ---------------------------------------------------------------------------
 * Tento soubor obsahuje jen konfiguraci. Nic se z něj přímo nezobrazuje.
 */

declare(strict_types=1);

/* -----------------------------------------------------------------------
 *  E-MAIL – kam se odesílají poptávky z formuláře
 * --------------------------------------------------------------------- */
const KREZBO_MAIL_TO   = 'krezbo@krezbo.cz';
// Odesílatel musí být adresa na doméně krezbo.cz (kvůli SPF/antispamu).
const KREZBO_MAIL_FROM = 'krezbo@krezbo.cz';

/* -----------------------------------------------------------------------
 *  ADMINISTRACE OZNÁMENÍ (admin.php)
 * -----------------------------------------------------------------------
 *  Výchozí heslo je:  krezbo2026
 *
 *  ZMĚŇTE SI HO! Nové heslo vygenerujete takto:
 *    1) otevřete v prohlížeči  https://www.krezbo.cz/admin.php?heslo=VASE_NOVE_HESLO
 *       (stránka vypíše hash) – NEBO příkazem:
 *         php -r 'echo password_hash("VASE_NOVE_HESLO", PASSWORD_DEFAULT);'
 *    2) vygenerovaný řetězec ($2y$...) vložte níže místo stávajícího.
 * --------------------------------------------------------------------- */
const KREZBO_ADMIN_HASH = '$2y$12$WpMW6enLX2qceKEIf9MEIeCZxohBmD2k4HTXAIWtn4ivoTmgMxjTO';

/* -----------------------------------------------------------------------
 *  Soubor, do kterého se ukládá text oznámení na úvodní stránce
 * --------------------------------------------------------------------- */
const KREZBO_NOTICE_FILE = __DIR__ . '/oznameni.txt';

/* -----------------------------------------------------------------------
 *  Prodejní doba (editovatelná přes admin.php) – ukládá se do souboru,
 *  jeden řádek na den ve stejném pořadí jako dny níže.
 * --------------------------------------------------------------------- */
const KREZBO_HOURS_FILE = __DIR__ . '/prodejni-doba.txt';
const KREZBO_HOURS_DAYS = ['Pondělí', 'Úterý', 'Středa', 'Čtvrtek', 'Pátek', 'Sobota', 'Neděle'];
const KREZBO_HOURS_DEFAULT = [
    '8:30–12:00, 13:00–16:00',
    '8:30–12:00, 13:00–16:00',
    '8:30–12:00, 13:00–16:00',
    '8:30–12:00, 13:00–16:00',
    '8:30–12:00, 13:00–16:00',
    'Zavřeno',
    'Zavřeno',
];

/**
 * Načte prodejní dobu (7 hodnot v pořadí dle KREZBO_HOURS_DAYS).
 * Když soubor neexistuje, vrátí výchozí hodnoty.
 */
function krezbo_load_hours(): array {
    $hours = KREZBO_HOURS_DEFAULT;
    if (is_file(KREZBO_HOURS_FILE)) {
        $lines = file(KREZBO_HOURS_FILE, FILE_IGNORE_NEW_LINES);
        if ($lines !== false) {
            foreach (KREZBO_HOURS_DAYS as $i => $_) {
                if (array_key_exists($i, $lines)) {
                    $hours[$i] = trim($lines[$i]);
                }
            }
        }
    }
    return $hours;
}

/* -----------------------------------------------------------------------
 *  (Volitelné) Google Maps Embed API klíč pro vložený Street View.
 *  Mapa funguje i bez klíče. Pokud klíč vyplníte, zobrazí se Street View
 *  přímo na stránce. Klíč zdarma: https://console.cloud.google.com/
 * --------------------------------------------------------------------- */
const KREZBO_MAPS_API_KEY = '';
