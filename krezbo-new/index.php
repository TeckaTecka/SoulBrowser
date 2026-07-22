<?php
declare(strict_types=1);
require __DIR__ . '/config.php';

/* ===========================================================================
 *  Zpracování formuláře POPTÁVKA (odeslání e-mailu na krezbo@krezbo.cz)
 * ========================================================================= */
$sent   = false;
$errors = [];
$form   = ['name' => '', 'email' => '', 'company' => '', 'phone' => '', 'message' => ''];

if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['form'] ?? '') === 'poptavka') {

    // Honeypot proti spamu – toto pole musí zůstat prázdné (skryté v CSS).
    if (($_POST['website'] ?? '') !== '') {
        // Vypadá to jako spam – tváříme se, že vše proběhlo v pořádku.
        $sent = true;
    } else {
        foreach ($form as $k => $_) {
            $form[$k] = trim((string)($_POST[$k] ?? ''));
        }

        if ($form['name'] === '')  { $errors['name']  = 'Jméno musí být vyplněno.'; }
        if ($form['email'] === '') {
            $errors['email'] = 'Email musí být vyplněn.';
        } elseif (!filter_var($form['email'], FILTER_VALIDATE_EMAIL)) {
            $errors['email'] = 'Zadejte prosím platnou e-mailovou adresu.';
        }
        if ($form['phone'] === '')   { $errors['phone']   = 'Telefon musí být vyplněn.'; }
        if ($form['message'] === '') { $errors['message'] = 'Zpráva musí být vyplněna.'; }

        if (!$errors) {
            $subject = 'Poptávka z webu KREZBO';
            $body =
                "Nová poptávka z webu www.krezbo.cz\r\n" .
                "----------------------------------------\r\n\r\n" .
                "Jméno:       {$form['name']}\r\n" .
                "Email:       {$form['email']}\r\n" .
                "Společnost:  {$form['company']}\r\n" .
                "Telefon:     {$form['phone']}\r\n\r\n" .
                "Zpráva:\r\n{$form['message']}\r\n";

            // Hlavičky – odesílatel je adresa na doméně, odpověď míří na tazatele.
            $fromName = '=?UTF-8?B?' . base64_encode('Web KREZBO') . '?=';
            $headers  = [
                'From: ' . $fromName . ' <' . KREZBO_MAIL_FROM . '>',
                'Reply-To: ' . $form['email'],
                'Content-Type: text/plain; charset=UTF-8',
                'MIME-Version: 1.0',
                'X-Mailer: PHP/' . phpversion(),
            ];
            $encodedSubject = '=?UTF-8?B?' . base64_encode($subject) . '?=';

            if (@mail(KREZBO_MAIL_TO, $encodedSubject, $body, implode("\r\n", $headers))) {
                $sent = true;
                $form = ['name' => '', 'email' => '', 'company' => '', 'phone' => '', 'message' => ''];
            } else {
                $errors['_send'] = 'Zprávu se nepodařilo odeslat. Zkuste to prosím později, '
                    . 'nebo nám napište přímo na krezbo@krezbo.cz.';
            }
        }
    }
}

/* ===========================================================================
 *  Načtení drobného oznámení na úvodní stránce (z oznameni.txt)
 * ========================================================================= */
$notice = '';
if (is_file(KREZBO_NOTICE_FILE)) {
    $notice = trim((string)file_get_contents(KREZBO_NOTICE_FILE));
}

/** Bezpečný výpis do HTML. */
function e(string $s): string {
    return htmlspecialchars($s, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}

/* Zastoupení firem – logo v /data/logos/, název a odkaz. */
$partners = [
    ['sigma.png',       'Sigmagroup',       'https://www.sigmagroup.cz/'],
    ['grundfos.png',    'Grundfos',         'https://www.grundfos.com/'],
    ['espa.png',        'Espa',             'https://www.espa.com/'],
    ['kopro.png',       'Kopro',            'https://www.kopro.cz/'],
    ['wilo.png',        'Wilo Praha s.r.o.', 'https://www.wilo.cz/'],
    ['alfapumpy.png',   'Alfapumpy',        'https://www.alfapumpy.cz/'],
    ['aquatrading.png', 'Aquatrading',      'https://www.aquatradingpumps.cz/'],
    ['aqua-cup.png',    'Aquacup',          'https://www.aquacup.cz/'],
    ['flygt.png',       'Flygt',            'https://www.flygt.com/'],
    // Zde později přibudou 3 nové firmy, např.:
    // ['nazev.png', 'Název firmy', 'https://www.odkaz.cz/'],
];

/* Adresa pro Google mapy */
$mapQuery = 'Partyzánské nám. 5, 702 00 Ostrava';
$mapEmbed = 'https://maps.google.com/maps?q=' . rawurlencode($mapQuery) . '&z=17&hl=cs&output=embed';
$streetLink = 'https://www.google.com/maps?layer=c&q=' . rawurlencode($mapQuery);
?>
<!DOCTYPE html>
<html lang="cs">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>KREZBO – prodej čerpadel, domácích vodáren a teplovodní techniky | Ostrava</title>
<meta name="description" content="KREZBO Ostrava – maloobchodní i velkoobchodní prodej čerpadel, domácích vodáren, ručních pump, zahradních hadic a teplovodní techniky. Montáže, servis a poradenství.">
<link rel="icon" href="img/krezbo.ico">
<link rel="stylesheet" href="css/style.css">
</head>
<body>

<header class="site-header">
    <div class="container header-inner">
        <a href="#uvod" class="logo"><img src="img/krezbo.png" alt="KREZBO"></a>
        <button class="nav-toggle" aria-label="Menu" aria-expanded="false">
            <span></span><span></span><span></span>
        </button>
        <nav class="main-nav">
            <ul>
                <li><a href="#uvod">O nás</a></li>
                <li><a href="#poptavka">Poptávka</a></li>
                <li><a href="#galerie">Galerie</a></li>
                <li><a href="#kontakt">Kontakt</a></li>
            </ul>
        </nav>
    </div>
</header>

<?php if ($notice !== ''): ?>
<div class="notice-bar" role="status">
    <div class="container">
        <span class="notice-ico" aria-hidden="true">!</span>
        <span class="notice-text"><?= nl2br(e($notice)) ?></span>
    </div>
</div>
<?php endif; ?>

<!-- ====================== O NÁS / ÚVOD ====================== -->
<section id="uvod" class="section hero">
    <div class="container">
        <h1>Prodej čerpadel a&nbsp;teplovodní techniky</h1>
        <p class="hero-lead">Maloobchod i&nbsp;velkoobchod &middot; montáže &middot; servis &middot; poradenství</p>
    </div>
</section>

<section class="section about">
    <div class="container">
        <div class="about-grid">
            <div class="about-col">
                <h2>Nabízíme prodej</h2>
                <ul class="ticks">
                    <li>čerpadel</li>
                    <li>domácích vodáren</li>
                    <li>ručních pump</li>
                    <li>zahradních hadic</li>
                    <li>náhradních dílů k čerpadlům i vodárnám</li>
                </ul>
                <p class="muted">v rámci maloobchodu i velkoobchodu</p>
            </div>
            <div class="about-col">
                <h2>Dále nabízíme</h2>
                <ul class="ticks">
                    <li>teplovodní techniku včetně regulace a spojovacího materiálu (fitinky)</li>
                    <li>montáže domácích vodáren</li>
                    <li>poradenskou službu a servis</li>
                </ul>
            </div>
        </div>
    </div>
</section>

<section class="section partners">
    <div class="container">
        <h2 class="center">Zastoupení firem</h2>
        <ul class="partner-grid">
            <?php foreach ($partners as [$logo, $name, $url]): ?>
            <li>
                <a href="<?= e($url) ?>" title="<?= e($name) ?>" target="_blank" rel="nofollow noopener">
                    <img src="data/logos/<?= e($logo) ?>" alt="<?= e($name) ?>" loading="lazy">
                </a>
            </li>
            <?php endforeach; ?>
        </ul>
    </div>
</section>

<!-- ====================== POPTÁVKA ====================== -->
<section id="poptavka" class="section poptavka">
    <div class="container narrow">
        <h2 class="center">Poslat poptávku</h2>
        <p class="center muted">
            Pokud nám chcete zaslat poptávku, využijte prosím tento formulář.<br>
            Na Vaši žádost Vám velmi ochotně zašleme materiály poštou,<br>
            nebo můžete navrhnout termín schůzky pro upřesnění Vašich požadavků.
        </p>

        <?php if ($sent): ?>
            <div class="alert alert-ok">
                <strong>Poptávka byla úspěšně odeslána.</strong><br>
                Děkujeme Vám, obratem Vás budeme kontaktovat.
            </div>
        <?php else: ?>
            <?php if (!empty($errors['_send'])): ?>
                <div class="alert alert-err"><?= e($errors['_send']) ?></div>
            <?php endif; ?>
            <form class="form" method="post" action="#poptavka" novalidate>
                <input type="hidden" name="form" value="poptavka">
                <!-- honeypot proti spamu -->
                <div class="hp"><label>Nevyplňujte<input type="text" name="website" tabindex="-1" autocomplete="off"></label></div>

                <div class="field <?= isset($errors['name']) ? 'has-error' : '' ?>">
                    <label for="f-name">Jméno <span class="req">*</span></label>
                    <input type="text" id="f-name" name="name" value="<?= e($form['name']) ?>" required>
                    <?php if (isset($errors['name'])): ?><span class="err"><?= e($errors['name']) ?></span><?php endif; ?>
                </div>

                <div class="field <?= isset($errors['email']) ? 'has-error' : '' ?>">
                    <label for="f-email">Email <span class="req">*</span></label>
                    <input type="email" id="f-email" name="email" value="<?= e($form['email']) ?>" required>
                    <?php if (isset($errors['email'])): ?><span class="err"><?= e($errors['email']) ?></span><?php endif; ?>
                </div>

                <div class="field">
                    <label for="f-company">Společnost</label>
                    <input type="text" id="f-company" name="company" value="<?= e($form['company']) ?>">
                </div>

                <div class="field <?= isset($errors['phone']) ? 'has-error' : '' ?>">
                    <label for="f-phone">Kontaktní telefon <span class="req">*</span></label>
                    <input type="text" id="f-phone" name="phone" value="<?= e($form['phone']) ?>" required>
                    <?php if (isset($errors['phone'])): ?><span class="err"><?= e($errors['phone']) ?></span><?php endif; ?>
                </div>

                <div class="field <?= isset($errors['message']) ? 'has-error' : '' ?>">
                    <label for="f-message">Zpráva <span class="req">*</span></label>
                    <textarea id="f-message" name="message" rows="8" required><?= e($form['message']) ?></textarea>
                    <?php if (isset($errors['message'])): ?><span class="err"><?= e($errors['message']) ?></span><?php endif; ?>
                </div>

                <div class="field">
                    <button type="submit" class="btn">Odeslat poptávku</button>
                </div>
            </form>
        <?php endif; ?>
    </div>
</section>

<!-- ====================== GALERIE ====================== -->
<section id="galerie" class="section gallery">
    <div class="container">
        <h2 class="center">Galerie</h2>
        <p class="center muted">Naše provozovna</p>
        <ul class="gallery-grid">
            <?php for ($i = 1; $i <= 6; $i++): ?>
            <li>
                <a href="data/gallery/800x600/gallery001_<?= $i ?>.jpg" class="lightbox">
                    <img src="data/gallery/160x120/gallery001_<?= $i ?>.jpg" alt="Provozovna KREZBO <?= $i ?>" loading="lazy">
                </a>
            </li>
            <?php endfor; ?>
        </ul>
    </div>
</section>

<!-- ====================== KONTAKT ====================== -->
<section id="kontakt" class="section contact">
    <div class="container">
        <h2 class="center">Kontakt</h2>
        <div class="contact-grid">
            <div class="contact-info">
                <h3>KREZBO</h3>
                <address>
                    Partyzánské nám. 5<br>
                    702 00 Ostrava<br>
                    Česká republika
                </address>
                <ul class="contact-list">
                    <li><span>IČ:</span> 46534792</li>
                    <li><span>DIČ:</span> CZ6209281859</li>
                    <li><span>Tel.:</span> <a href="tel:+420596122101">596 122 101</a></li>
                    <li><span>Mob.:</span> <a href="tel:+420602795007">602 795 007</a></li>
                    <li><span>Fax:</span> +420 596 122 101</li>
                    <li><span>Email:</span> <a href="mailto:krezbo@krezbo.cz">krezbo@krezbo.cz</a></li>
                    <li><span>Web:</span> www.krezbo.cz</li>
                </ul>

                <h3>Zdeněk Zbořil</h3>
                <ul class="contact-list">
                    <li><span>Email:</span> <a href="mailto:z.zboril@krezbo.cz">z.zboril@krezbo.cz</a></li>
                </ul>

                <h3>Prodejní doba</h3>
                <table class="hours">
                    <tr><th>Pondělí</th><td>8:30–12:00, 13:00–16:00</td></tr>
                    <tr><th>Úterý</th><td>8:30–12:00, 13:00–16:00</td></tr>
                    <tr><th>Středa</th><td>8:30–12:00, 13:00–16:00</td></tr>
                    <tr><th>Čtvrtek</th><td>8:30–12:00, 13:00–16:00</td></tr>
                    <tr><th>Pátek</th><td>8:30–12:00, 13:00–16:00</td></tr>
                    <tr><th>Sobota</th><td>Zavřeno</td></tr>
                    <tr><th>Neděle</th><td>Zavřeno</td></tr>
                </table>
            </div>

            <div class="contact-map">
                <div class="map-embed">
                    <iframe
                        src="<?= e($mapEmbed) ?>"
                        width="100%" height="360" style="border:0;"
                        allowfullscreen loading="lazy" referrerpolicy="no-referrer-when-downgrade"
                        title="Mapa – KREZBO, Partyzánské nám. 5, Ostrava"></iframe>
                </div>

                <?php if (KREZBO_MAPS_API_KEY !== ''): ?>
                <div class="map-embed">
                    <iframe
                        src="https://www.google.com/maps/embed/v1/streetview?key=<?= e(KREZBO_MAPS_API_KEY) ?>&location=49.8380,18.2925&heading=0&pitch=0&fov=90"
                        width="100%" height="360" style="border:0;"
                        allowfullscreen loading="lazy" referrerpolicy="no-referrer-when-downgrade"
                        title="Street View – KREZBO"></iframe>
                </div>
                <?php endif; ?>

                <a class="btn btn-outline" href="<?= e($streetLink) ?>" target="_blank" rel="noopener">
                    Zobrazit Street View na Google mapách
                </a>
            </div>
        </div>
    </div>
</section>

<footer class="site-footer">
    <div class="container">
        <p>KREZBO &copy; <?= date('Y') ?> &middot; Všechna práva vyhrazena</p>
    </div>
</footer>

<!-- Jednoduchý lightbox pro galerii -->
<div class="lb-overlay" id="lb" aria-hidden="true">
    <button class="lb-close" aria-label="Zavřít">&times;</button>
    <button class="lb-prev" aria-label="Předchozí">&#10094;</button>
    <img class="lb-img" src="" alt="">
    <button class="lb-next" aria-label="Další">&#10095;</button>
</div>

<script src="js/main.js"></script>
</body>
</html>
