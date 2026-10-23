<?php
declare(strict_types=1);
require __DIR__ . '/config.php';
$page = 'index';
$pageTitle = 'Mezinárodní kamionová doprava';
$prices = te_load_prices();
require __DIR__ . '/inc/header.php';
?>

<!-- ===== HERO ===== -->
<section class="hero">
    <div class="bg"></div>
    <div class="centeris">
        <h1>Mezinárodní<br>kamionová doprava</h1>
        <a href="sluzby.php" class="btn large">Naše služby</a>
    </div>
    <div class="hero-tabs">
        <div class="centeris">
            <span class="active"><b>01</b> Kariéra</span>
            <span><b>02</b> Naše služby</span>
        </div>
    </div>
</section>

<!-- ===== NÁBOR ===== -->
<section class="section recruitment" id="nabor">
    <div class="centeris">
        <div class="photo"><img src="img/nabor.jpg" alt="Nábor řidičů"></div>
        <div class="content">
            <h2>Nábor nových řidičů</h2>
            <p>Práce řidiče je <strong>náročné povolání</strong>, které ovšem <strong>není stereotypní</strong>.
               Nabízí každodenní změnu, řidič pozná mnoho <strong>nových krajů a zvyků</strong>. Naučí se
               <strong>komunikovat</strong> s cizími lidmi a řešit s nimi pracovní problémy i přes jazykovou barieru.</p>
            <a href="kariera.php" class="btn">Více informací</a>
        </div>
    </div>
</section>

<!-- ===== ČERPACÍ STANICE – CENY PHM ===== -->
<section class="section soft gas-station" id="cerpaci-stanice">
    <div class="centeris">
        <h2>Čerpací stanice <span>Transeurope</span></h2>
        <div class="fuel-grid">
            <?php foreach (TE_FUELS as $i => $f): ?>
                <div class="fuel-box">
                    <div class="fuel-brand"><?= e($f['brand']) ?></div>
                    <?php if ($f['sub'] === '98' || $f['sub'] === '95'): ?>
                        <div class="fuel-sub"><?= e($f['sub']) ?></div>
                    <?php elseif ($f['sub'] === 'diesel'): ?>
                        <div class="fuel-sub badge">Diesel</div>
                    <?php else: ?>
                        <div class="fuel-sub">&nbsp;</div>
                    <?php endif; ?>
                    <div class="fuel-price"><?= e($prices[$i]) ?><small>Kč / litr</small></div>
                </div>
            <?php endforeach; ?>
        </div>
    </div>
</section>

<!-- ===== O NÁS ===== -->
<section class="section index-about" id="o-nas">
    <div class="centeris">
        <div class="ia-box">
            <h2>O <span class="r">nás</span></h2>
            <p>Rádi bychom Vám nabídli služby v oblasti mezinárodní kamionové dopravy <strong>po celé Evropě</strong>.
               Na našich stránkách najdete <strong>několik základních informací</strong> o společnosti. Ochotně Vám
               poskytneme informace o službách, které nabízíme.</p>
            <p>Firma <strong>TRANSEUROPE se zabývá mezinárodní dopravou a spedicí</strong>. Dopravujeme zboží téměř
               po celé Evropě mimo Ruska.</p>
            <p>Jsme pojištěni v rámci odpovědnosti mezinárodního silničního dopravce podle <strong>Úmluvy CMR</strong>.</p>
            <a href="o-nas.php" class="btn">Více informací</a>
        </div>
        <div class="ia-photo"></div>
    </div>
</section>

<!-- ===== PARTNEŘI ===== -->
<section class="section partners">
    <div class="centeris">
        <h2>Spolupracujeme <i class="fas fa-arrow-down"></i></h2>
        <div class="partner-row">
            <a href="http://www.lanex.cz/" target="_blank" rel="nofollow noopener"><img src="img/partners/partner_lanex.png" alt="Lanex"></a>
            <a href="https://www.bekaert.com/" target="_blank" rel="nofollow noopener"><img src="img/partners/partner_bekaert.png" alt="Bekaert"></a>
            <a href="https://www.cargoprague.cz/" target="_blank" rel="nofollow noopener"><img src="img/partners/partner_cargo-prague.png" alt="Cargo Prague"></a>
            <a href="http://www.mtmtransport.cz/" target="_blank" rel="nofollow noopener"><img src="img/partners/partner_mtm.png" alt="MTM"></a>
            <a href="http://www.cz.rhenus.com/cs/" target="_blank" rel="nofollow noopener"><img src="img/partners/partner_rhenus.png" alt="Rhenus"></a>
            <a href="http://www.conrop.cz/" target="_blank" rel="nofollow noopener"><img src="img/partners/partner_conrop.png" alt="Conrop"></a>
        </div>
    </div>
</section>

<?php require __DIR__ . '/inc/footer.php';