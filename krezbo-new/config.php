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
 *  (Volitelné) Google Maps Embed API klíč pro vložený Street View.
 *  Mapa funguje i bez klíče. Pokud klíč vyplníte, zobrazí se Street View
 *  přímo na stránce. Klíč zdarma: https://console.cloud.google.com/
 * --------------------------------------------------------------------- */
const KREZBO_MAPS_API_KEY = '';
