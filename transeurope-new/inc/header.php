<?php
/** Společná hlavička. Před includem nastav: $page (klíč aktivní položky), $pageTitle, $pageDesc. */
if (!isset($page))      { $page = ''; }
if (!isset($pageTitle)) { $pageTitle = 'Mezinárodní kamionová doprava'; }
if (!isset($pageDesc))  { $pageDesc = 'Mezinárodní kamionová doprava a spedice. Kamionová doprava, čerpací stanice, servis návěsů a kariéra řidičů – TRANSEUROPE Bohumín.'; }

$nav = [
    'index'    => ['Úvod',     'index.php'],
    'sluzby'   => ['Služby',   'sluzby.php'],
    'o-nas'    => ['O nás',    'o-nas.php'],
    'kontakty' => ['Kontakty', 'kontakty.php'],
];
?><!DOCTYPE html>
<html lang="cs">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title><?= e($pageTitle) ?> | Transeurope</title>
<meta name="description" content="<?= e($pageDesc) ?>">
<link rel="icon" href="img/favicon.png">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,400;0,600;0,700;0,800;1,600&display=swap" rel="stylesheet">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
<link rel="stylesheet" href="css/style.css">
</head>
<body>

<header class="site-header">
    <div class="header-inner">
        <a href="index.php" class="logo"><img src="img/logo.png" alt="TRANSEUROPE"></a>
        <a class="header-phone" href="tel:+420596012451"><i class="fas fa-phone"></i> +420 596 012 451-2</a>
        <button class="nav-toggle" aria-label="Menu" aria-expanded="false"><span></span><span></span><span></span></button>
        <nav class="top-nav">
            <ul>
                <?php foreach ($nav as $key => [$label, $href]): ?>
                    <li><a href="<?= $href ?>"<?= $page === $key ? ' class="active"' : '' ?>><?= e($label) ?></a></li>
                <?php endforeach; ?>
            </ul>
        </nav>
    </div>
</header>

<div class="layout">
    <nav class="side-nav" aria-label="Rychlé menu">
        <button class="side-item js-nav-toggle" type="button"><span class="ico"><i class="fas fa-bars"></i></span><span>Menu</span></button>
        <a class="side-item" href="kariera.php"><span class="ico"><i class="fas fa-users"></i></span><span>Nábor</span></a>
        <a class="side-item" href="index.php#cerpaci-stanice"><span class="ico"><i class="fas fa-gas-pump"></i></span><span>Čerpací<br>stanice</span></a>
        <a class="side-item" href="o-nas.php"><span class="ico"><i class="fas fa-circle-info"></i></span><span>O nás</span></a>
    </nav>

    <main class="main">
