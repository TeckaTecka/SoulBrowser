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

    if (($_POST['website'] ?? '') !== '') {           // honeypot proti spamu
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

$notice = '';
if (is_file(KREZBO_NOTICE_FILE)) {
    $notice = trim((string)file_get_contents(KREZBO_NOTICE_FILE));
}

function e(string $s): string {
    return htmlspecialchars($s, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}

/* Zastoupení firem – logo v /data/logos/ (nebo prázdné = zobrazí se název), název a odkaz.
   Až budete mít logo nové firmy, nahrajte ho do /data/logos/ a doplňte název souboru
   na první pozici řádku – místo textu se pak zobrazí obrázek. */
$partners = [
    ['sigma.png',       'Sigmagroup',        'https://www.sigmagroup.cz/'],
    ['grundfos.png',    'Grundfos',          'https://www.grundfos.com/'],
    ['espa.png',        'Espa',              'https://www.espa.com/'],
    ['kopro.png',       'Kopro',             'https://www.kopro.cz/'],
    ['wilo.png',        'Wilo Praha s.r.o.', 'https://www.wilo.cz/'],
    ['alfapumpy.png',   'Alfapumpy',         'https://www.alfapumpy.cz/'],
    ['aquatrading.png', 'Aquatrading',       'https://www.aquatradingpumps.cz/'],
    ['aqua-cup.png',    'Aquacup',           'https://www.aquacup.cz/'],
    ['flygt.png',       'Flygt',             'https://www.flygt.com/'],
    ['pedrollo.png',    'Pedrollo',          'https://www.pedrollocz.cz'],
    ['pumpa.png',       'Pumpa a.s.',        'https://www.pumpa.eu/cs/'],
    ['mave.png',        'Mave',              'http://www.mave-nymburk.cz'],
    ['kh.svg',          'K+H čerpadla',      'https://www.k-h.cz'],
    ['lk.png',          'LK pumpservice',    'https://www.lk-group.eu'],
    ['termolux.png',    'Termolux',          'https://www.termolux.cz'],
];

$mapQuery  = 'Partyzánské nám. 5, 702 00 Ostrava';
$mapEmbed  = 'https://maps.google.com/maps?q=' . rawurlencode($mapQuery) . '&t=&z=17&hl=cs&ie=UTF8&iwloc=&output=embed';
// Street View – výchozí pozice provozovny (odkaz z Google map)
$streetView = 'https://maps.app.goo.gl/qrbChQAjdSHbSK6A6';
?>
<!DOCTYPE html>
<html lang="cs">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>KREZBO – prodej čerpadel, domácích vodáren a teplovodní techniky | Ostrava</title>
<meta name="description" content="KREZBO Ostrava – maloobchodní i velkoobchodní prodej čerpadel, domácích vodáren, ručních pump, zahradních hadic a teplovodní techniky. Montáže, servis a poradenství.">
<link rel="icon" href="img/krezbo.ico">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&family=Poppins:wght@500;600;700&display=swap" rel="stylesheet">
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
                <li><a href="#galerie">Galerie</a></li>
                <li><a href="#kontakt">Kontakt</a></li>
                <li><a href="#poptavka" class="nav-cta">Poptávka</a></li>
            </ul>
        </nav>
    </div>
</header>

<?php if ($notice !== ''): ?>
<div class="notice-bar">
    <div class="container">
        <div class="inner">
            <span class="notice-ico" aria-hidden="true">!</span>
            <span class="notice-text"><?= nl2br(e($notice)) ?></span>
        </div>
    </div>
</div>
<?php endif; ?>

<!-- ====================== HERO / ÚVOD ====================== -->
<section id="uvod" class="hero">
    <div class="container">
        <span class="eyebrow">KREZBO Ostrava</span>
        <h1>Čerpadla, domácí vodárny<br>a teplovodní technika</h1>
        <p>Maloobchodní i velkoobchodní prodej, montáže domácích vodáren, servis a odborné poradenství.</p>
        <div class="hero-btns">
            <a href="#poptavka" class="btn btn-light">Poslat poptávku</a>
            <a href="#kontakt" class="btn btn-ghost">Kontakt &amp; otevírací doba</a>
        </div>
    </div>
</section>

<!-- ====================== O NÁS ====================== -->
<section class="section about">
    <div class="container">
        <div class="section-head">
            <span class="eyebrow">O nás</span>
            <h2>Co u nás pořídíte</h2>
            <p>Kompletní sortiment čerpací a teplovodní techniky pro domácnost i firmy.</p>
        </div>
        <div class="about-grid">
            <div class="about-card">
                <h3>
                    <span class="ico" aria-hidden="true">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2v6"/><path d="M5 8h14l-1 12H6L5 8z"/><path d="M9 12h6"/></svg>
                    </span>
                    Nabízíme prodej
                </h3>
                <ul class="ticks">
                    <li>čerpadel</li>
                    <li>domácích vodáren</li>
                    <li>ručních pump</li>
                    <li>zahradních hadic</li>
                    <li>náhradních dílů k čerpadlům i vodárnám</li>
                </ul>
                <p class="note">v rámci maloobchodu i velkoobchodu</p>
            </div>
            <div class="about-card">
                <h3>
                    <span class="ico" aria-hidden="true">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 3l-2 5h4l-6 10 2-7H8l3-8z"/></svg>
                    </span>
                    Dále nabízíme
                </h3>
                <ul class="ticks">
                    <li>teplovodní techniku včetně regulace a spojovacího materiálu (fitinky)</li>
                    <li>montáže domácích vodáren</li>
                    <li>poradenskou službu a servis</li>
                </ul>
                <p class="note">Součástí naší nabídky je i poradenská služba a servis.</p>
            </div>
        </div>
    </div>
</section>

<!-- ====================== ZASTOUPENÍ FIREM ====================== -->
<section class="section partners">
    <div class="container">
        <div class="section-head">
            <span class="eyebrow">Zastoupení firem</span>
            <h2>Značky, které u nás najdete</h2>
        </div>
        <ul class="partner-grid">
            <?php foreach ($partners as [$logo, $name, $url]): ?>
            <li>
                <a href="<?= e($url) ?>" title="<?= e($name) ?>" target="_blank" rel="nofollow noopener">
                    <?php if ($logo !== '' && is_file(__DIR__ . '/data/logos/' . $logo)): ?>
                        <img src="data/logos/<?= e($logo) ?>" alt="<?= e($name) ?>" loading="lazy">
                    <?php else: ?>
                        <span class="partner-name"><?= e($name) ?></span>
                    <?php endif; ?>
                </a>
            </li>
            <?php endforeach; ?>
        </ul>
    </div>
</section>

<!-- ====================== POPTÁVKA ====================== -->
<section id="poptavka" class="section poptavka">
    <div class="container">
        <div class="section-head">
            <span class="eyebrow">Poptávka</span>
            <h2>Poslat poptávku</h2>
            <p>Rádi Vám zašleme materiály poštou nebo navrhneme termín schůzky pro upřesnění Vašich požadavků.</p>
        </div>
        <div class="form-card">
            <?php if ($sent): ?>
                <div class="alert alert-ok">
                    <strong>Poptávka byla úspěšně odeslána.</strong><br>
                    Děkujeme Vám, obratem Vás budeme kontaktovat.
                </div>
            <?php else: ?>
                <?php if (!empty($errors['_send'])): ?>
                    <div class="alert alert-err"><?= e($errors['_send']) ?></div>
                <?php endif; ?>
                <form method="post" action="#poptavka" novalidate>
                    <input type="hidden" name="form" value="poptavka">
                    <div class="hp"><label>Nevyplňujte<input type="text" name="website" tabindex="-1" autocomplete="off"></label></div>
                    <div class="form-grid">
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
                        <div class="field full <?= isset($errors['message']) ? 'has-error' : '' ?>">
                            <label for="f-message">Zpráva <span class="req">*</span></label>
                            <textarea id="f-message" name="message" rows="6" required><?= e($form['message']) ?></textarea>
                            <?php if (isset($errors['message'])): ?><span class="err"><?= e($errors['message']) ?></span><?php endif; ?>
                        </div>
                        <div class="field full">
                            <button type="submit" class="btn">Odeslat poptávku</button>
                        </div>
                    </div>
                </form>
            <?php endif; ?>
        </div>
    </div>
</section>

<!-- ====================== GALERIE ====================== -->
<section id="galerie" class="section gallery">
    <div class="container">
        <div class="section-head">
            <span class="eyebrow">Galerie</span>
            <h2>Naše provozovna</h2>
        </div>
        <ul class="gallery-grid">
            <?php for ($i = 1; $i <= 6; $i++): ?>
            <li>
                <a href="data/gallery/800x600/gallery001_<?= $i ?>.jpg" class="lightbox">
                    <img src="data/gallery/800x600/gallery001_<?= $i ?>.jpg" alt="Provozovna KREZBO <?= $i ?>" loading="lazy">
                </a>
            </li>
            <?php endfor; ?>
        </ul>
    </div>
</section>

<!-- ====================== KONTAKT ====================== -->
<section id="kontakt" class="section contact">
    <div class="container">
        <div class="section-head">
            <span class="eyebrow">Kontakt</span>
            <h2>Kde nás najdete</h2>
        </div>
        <div class="contact-grid">
            <div class="contact-card">
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
                </ul>
                <h3>Zdeněk Zbořil</h3>
                <ul class="contact-list">
                    <li><span>Email:</span> <a href="mailto:krezbo@krezbo.cz">krezbo@krezbo.cz</a></li>
                </ul>
                <h3>Prodejní doba</h3>
                <table class="hours">
                    <?php $hours = krezbo_load_hours(); ?>
                    <?php foreach (KREZBO_HOURS_DAYS as $i => $day): ?>
                        <tr><th><?= e($day) ?></th><td><?= e($hours[$i]) ?></td></tr>
                    <?php endforeach; ?>
                </table>
            </div>
            <div class="map-wrap">
                <div class="map-embed">
                    <iframe src="<?= e($mapEmbed) ?>" loading="lazy"
                        referrerpolicy="no-referrer-when-downgrade" allowfullscreen
                        title="Mapa – KREZBO, Partyzánské nám. 5, Ostrava"></iframe>
                </div>
                <a class="btn btn-outline" href="<?= e($streetView) ?>" target="_blank" rel="noopener">
                    Zobrazit Street View naší provozovny
                </a>
            </div>
        </div>
    </div>
</section>

<!-- ====================== FOOTER ====================== -->
<footer class="site-footer">
    <div class="container">
        <div class="footer-grid">
            <div>
                <img class="footer-logo" src="img/krezbo.png" alt="KREZBO">
                <p>Prodej čerpadel, domácích vodáren a teplovodní techniky. Maloobchod, velkoobchod, montáže a servis.</p>
            </div>
            <div>
                <h4>Kontakt</h4>
                <ul>
                    <li>Partyzánské nám. 5, 702 00 Ostrava</li>
                    <li>Tel.: <a href="tel:+420596122101">596 122 101</a></li>
                    <li>Mob.: <a href="tel:+420602795007">602 795 007</a></li>
                    <li><a href="mailto:krezbo@krezbo.cz">krezbo@krezbo.cz</a></li>
                </ul>
            </div>
            <div>
                <h4>Menu</h4>
                <ul>
                    <li><a href="#uvod">O nás</a></li>
                    <li><a href="#galerie">Galerie</a></li>
                    <li><a href="#poptavka">Poptávka</a></li>
                    <li><a href="#kontakt">Kontakt</a></li>
                </ul>
            </div>
        </div>
        <div class="footer-bottom">
            KREZBO &copy; <?= date('Y') ?> &middot; Všechna práva vyhrazena
        </div>
    </div>
</footer>

<div class="lb-overlay" id="lb" aria-hidden="true">
    <button class="lb-close" aria-label="Zavřít">&times;</button>
    <button class="lb-prev" aria-label="Předchozí">&#10094;</button>
    <img class="lb-img" src="" alt="">
    <button class="lb-next" aria-label="Další">&#10095;</button>
</div>

<script src="js/main.js"></script>
</body>
</html>
