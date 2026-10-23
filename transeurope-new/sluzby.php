<?php
declare(strict_types=1);
require __DIR__ . '/config.php';
$page = 'sluzby';
$pageTitle = 'Služby';
require __DIR__ . '/inc/header.php';
?>

<div class="page-head">
    <div class="bg" style="background-image:url('img/hero.jpg')"></div>
    <div class="centeris"><h1>Služby</h1></div>
</div>
<div class="breadcrumb"><div class="centeris"><a href="index.php">Úvod</a> &rsaquo; Služby</div></div>

<section class="section">
    <div class="centeris">
        <div class="prose">
            <h2>Poskytujeme následující služby</h2>
        </div>
        <ul class="svc-list">
            <li><i class="fas fa-truck"></i>&nbsp; Mezinárodní kamionová doprava</li>
            <li><i class="fas fa-gas-pump"></i>&nbsp; Čerpací stanice 5,00&ndash;22,00 hod, nonstop provoz tankovacího automatu</li>
            <li><i class="fas fa-wrench"></i>&nbsp; Autorizovaný servis návěsů Schwarzmüller v areálu firmy</li>
        </ul>
        <p style="margin-top:34px;">
            <a href="kariera.php" class="btn">Kariéra &ndash; nábor řidičů</a>
        </p>
    </div>
</section>

<?php require __DIR__ . '/inc/footer.php';