<?php
declare(strict_types=1);
require __DIR__ . '/config.php';
$page = 'o-nas';
$pageTitle = 'O nás';
require __DIR__ . '/inc/header.php';

$timeline = [
    ['1990', 'Majitel p. Petr Škuta zakládá firmu a začíná jezdit s jedním vozem zn. Liaz.'],
    ['1991', 'Zakoupen první tahač VOLVO F12.'],
    ['1992 - 1995', 'Pronájem prostorů v Orlové v areálu bývalé STS.'],
    ['1995', 'Stali jsme se majiteli tohoto objektu a do r. 1999 v něm měla naše firma i sídlo.'],
    ['1997', 'Přikoupili jsme bývalé garáže ČSAD v Bohumíně.'],
    ['1997 - 1999', 'Rekonstrukce těchto prostor a následné stěhování do nového objektu.'],
    ['1999', 'Zakoupení čerpací stanice pohonných hmot (dříve Benzina) v těsném sousedství sídla firmy v Bohumíně.'],
    ['duben - srpen 2000', 'Rekonstrukce čerp. stanice, stavba vrátnice.'],
    ['2001', 'Uvedena do provozu myčka vozidel všech kategorií.'],
    ['2004', 'Zakoupení autobusu MERCEDES-BENZ / SPRINTER.'],
    ['2006', 'Zakoupení skříňového automobilu VOLKSWAGEN / CRAFTER.'],
    ['2007', 'Zakoupení speciálního vyprošťovacího automobilu SCANIA R500.'],
    ['2007', 'Zakoupení nákladního návěsu ADR - cisternový SCHWARZMÜLLER.'],
    ['2009', 'Spustili jsme pro Vás nové internetové stránky.'],
    ['2010', 'Duben - ukončen prodej benzínu Speciál 91.'],
    ['2010', 'Květen - zahájen prodej nafty UltraDiesel.'],
    ['2011', 'Na čerpací stanici přijímáme platební karty EUROCARD-MASTERCARD, VISA, MAESTRO.'],
    ['2018', 'Možnost nočního tankování pomocí tankovacího automatu.'],
];
?>

<div class="page-head">
    <div class="bg" style="background-image:url('img/onas-pano.jpg')"></div>
    <div class="centeris"><h1>O nás</h1></div>
</div>
<div class="breadcrumb"><div class="centeris"><a href="index.php">Úvod</a> &rsaquo; O nás</div></div>

<section class="section">
    <div class="centeris">
        <div class="prose">
            <h2>Vítáme Vás na stránkách TRANSEUROPE</h2>
            <p>Rádi bychom Vám nabídli služby v oblasti mezinárodní kamionové dopravy <strong>po celé Evropě</strong>.
               Na našich stránkách najdete několik základních informací o společnosti. Ochotně Vám poskytneme
               informace o <strong>službách, které nabízíme</strong>.</p>
            <p>Firma TRANSEUROPE se zabývá <strong>mezinárodní dopravou a spedicí</strong>. Dopravujeme zboží téměř
               po <strong>celé Evropě</strong> mimo Ruska.</p>
            <p>Jsme pojištěni v rámci odpovědnosti mezinárodního silničního dopravce podle <strong>Úmluvy CMR</strong>.
               Všichni naši řidiči jsou proškoleni na MKD v rámci Úmluvy CMR a TIR a disponují dostatečnými
               jazykovými znalostmi.</p>

            <h2>Vývoj naší společnosti</h2>
            <ul class="timeline">
                <?php foreach ($timeline as [$year, $text]): ?>
                    <li><strong><?= e($year) ?></strong> &ndash; <?= e($text) ?></li>
                <?php endforeach; ?>
            </ul>
        </div>

        <div class="gallery-grid">
            <?php for ($i = 1; $i <= 5; $i++): ?>
                <a href="img/gallery/g<?= $i ?>.jpg" class="lightbox" target="_blank" rel="noopener">
                    <img src="img/gallery/g<?= $i ?>.jpg" alt="Provozovna TRANSEUROPE <?= $i ?>" loading="lazy">
                </a>
            <?php endfor; ?>
        </div>
    </div>
</section>

<?php require __DIR__ . '/inc/footer.php';