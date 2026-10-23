<?php
/**
 * Nastavení webu TRANSEUROPE
 * ---------------------------------------------------------------------------
 */
declare(strict_types=1);

/* E-mail – kam chodí formuláře (kariéra i kontakt) */
const TE_MAIL_TO   = 'transeurope@transeurope.cz';
const TE_MAIL_FROM = 'transeurope@transeurope.cz';

/* Administrace cen PHM (admin.php) – výchozí heslo: transeurope2026
 * Změna hesla přímo v administraci (uloží se do admin.hash). */
const TE_ADMIN_HASH      = '$2y$10$SiI/oYgbyuBAmcKUxnr.7.L1hKEV0OQmhcG6jfkG3tRdphWpKchdW';
const TE_ADMIN_HASH_FILE = __DIR__ . '/admin.hash';

/* Soubor s cenami PHM (4 řádky: Natural 98, Natural 95, Ultra Diesel, Diesel) */
const TE_PRICES_FILE = __DIR__ . '/ceny-phm.txt';

/* Výchozí ceny a popisky paliv */
const TE_FUELS = [
    ['brand' => 'Natural',      'sub' => '98',     'default' => '50.00'],
    ['brand' => 'Natural',      'sub' => '95',     'default' => '44.94'],
    ['brand' => 'Ultra Diesel', 'sub' => 'diesel', 'default' => '49.50'],
    ['brand' => 'Diesel',       'sub' => '',       'default' => '47.50'],
];

/** Vrátí aktuální hash hesla (ze souboru, jinak výchozí). */
function te_admin_hash(): string {
    if (is_file(TE_ADMIN_HASH_FILE)) {
        $h = trim((string)file_get_contents(TE_ADMIN_HASH_FILE));
        if ($h !== '') return $h;
    }
    return TE_ADMIN_HASH;
}

/** Načte ceny PHM (4 hodnoty); když soubor chybí, vrátí výchozí. */
function te_load_prices(): array {
    $prices = array_map(fn($f) => $f['default'], TE_FUELS);
    if (is_file(TE_PRICES_FILE)) {
        $lines = file(TE_PRICES_FILE, FILE_IGNORE_NEW_LINES);
        if ($lines !== false) {
            foreach (TE_FUELS as $i => $_) {
                if (array_key_exists($i, $lines) && trim($lines[$i]) !== '') {
                    $prices[$i] = trim($lines[$i]);
                }
            }
        }
    }
    return $prices;
}

/** Bezpečný výpis do HTML. */
function e(string $s): string {
    return htmlspecialchars($s, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}
