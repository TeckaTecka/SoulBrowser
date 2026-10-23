<?php
declare(strict_types=1);
require __DIR__ . '/config.php';
require __DIR__ . '/inc/mail.php';
$page = 'kontakty';
$pageTitle = 'Kontakty';

$sent = false; $errors = [];
$form = ['name' => '', 'email' => '', 'phone' => '', 'message' => ''];
if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['form'] ?? '') === 'kontakt') {
    if (($_POST['website'] ?? '') !== '') { $sent = true; }
    else {
        foreach ($form as $k => $_) { $form[$k] = trim((string)($_POST[$k] ?? '')); }
        if ($form['name'] === '')  { $errors['name'] = 'Vyplňte prosím jméno.'; }
        if ($form['email'] === '' || !filter_var($form['email'], FILTER_VALIDATE_EMAIL)) { $errors['email'] = 'Zadejte platný e-mail.'; }
        if ($form['message'] === '') { $errors['message'] = 'Napište prosím zprávu.'; }
        if (!$errors) {
            $body = "Zpráva z kontaktního formuláře www.transeurope.cz\n\n"
                  . "Jméno:   {$form['name']}\nEmail:   {$form['email']}\nTelefon: {$form['phone']}\n\nZpráva:\n{$form['message']}\n";
            if (te_send_mail('Kontakt z webu TRANSEUROPE', $body, $form['email'])) {
                $sent = true; $form = ['name'=>'','email'=>'','phone'=>'','message'=>''];
            } else { $errors['_send'] = 'Zprávu se nepodařilo odeslat. Napište nám prosím na transeurope@transeurope.cz.'; }
        }
    }
}

$contacts = [
    ['role' => 'Majitel firmy', 'name' => 'Petr Škuta', 'items' => [
        ['phone','Tel.','+420 596 012 701','+420596012701'], ['mobile-alt','Mob.','+420 602 527 279','+420602527279'],
        ['envelope','E-mail','petr.skuta@transeurope.cz','mail']]],
    ['role' => 'Dispečink', 'name' => 'Dispečink', 'items' => [
        ['phone','Tel.','+420 596 012 452','+420596012452'], ['mobile-alt','Mob.','+420 602 714 493','+420602714493'],
        ['envelope','E-mail','dispecink@transeurope.cz','mail']]],
    ['role' => 'Sekretariát, fakturace', 'name' => 'Ivana Marklová', 'items' => [
        ['phone','Tel.','+420 596 012 451','+420596012451'], ['envelope','E-mail','ivana.marklova@transeurope.cz','mail']]],
    ['role' => 'Personalistka, prodej PHM', 'name' => 'Irena Sedláčková', 'items' => [
        ['phone','Tel.','+420 596 012 451','+420596012451'], ['mobile-alt','Mob.','+420 602 503 866','+420602503866'],
        ['envelope','E-mail','irena.sedlackova@transeurope.cz','mail']]],
];
require __DIR__ . '/inc/header.php';
?>

<div class="page-head">
    <div class="bg" style="background-image:url('img/onas-pano.jpg')"></div>
    <div class="centeris"><h1>Kontakty</h1></div>
</div>
<div class="breadcrumb"><div class="centeris"><a href="index.php">Úvod</a> &rsaquo; Kontakty</div></div>

<section class="section">
    <div class="centeris">
        <div class="prose"><h2>Hlavní kontakty na naši společnost</h2></div>
        <div class="contact-grid">
            <?php foreach ($contacts as $c): ?>
                <div class="contact-card">
                    <div class="role"><?= e($c['role']) ?></div>
                    <h3><?= e($c['name']) ?></h3>
                    <ul>
                        <?php foreach ($c['items'] as [$ico,$lbl,$val,$href]): ?>
                            <li><i class="fas fa-<?= $ico ?>"></i> <strong><?= e($lbl) ?>:</strong>
                                <?php if ($href === 'mail'): ?><a href="mailto:<?= e($val) ?>"><?= e($val) ?></a>
                                <?php else: ?><a href="tel:<?= e($href) ?>"><?= e($val) ?></a><?php endif; ?>
                            </li>
                        <?php endforeach; ?>
                    </ul>
                </div>
            <?php endforeach; ?>
        </div>

        <div class="prose" style="margin-top:50px;">
            <h2>Sídlo</h2>
            <p><strong>Petr Škuta - TRANSEUROPE</strong><br>
               Ostravská 204, 735 81 Bohumín<br>
               <i class="fas fa-envelope"></i> <a href="mailto:transeurope@transeurope.cz">transeurope@transeurope.cz</a><br>
               <i class="fas fa-phone"></i> <a href="tel:+420596012451">+420 596 012 451-2</a><br>
               <i class="fas fa-gas-pump"></i> Čerpací stanice <a href="tel:+420596012601">+420 596 012 601</a></p>
        </div>

        <div class="prose" style="margin-top:50px;">
            <h2>Napište nám</h2>
            <?php if ($sent): ?>
                <div class="alert alert-ok"><strong>Zpráva byla odeslána.</strong> Děkujeme, budeme Vás kontaktovat.</div>
            <?php else: ?>
                <?php if (!empty($errors['_send'])): ?><div class="alert alert-err"><?= e($errors['_send']) ?></div><?php endif; ?>
                <form class="form-card" method="post" action="kontakty.php#napiste">
                    <input type="hidden" name="form" value="kontakt">
                    <div class="hp"><input type="text" name="website" tabindex="-1" autocomplete="off"></div>
                    <div class="field <?= isset($errors['name'])?'has-error':'' ?>"><label>Jméno *</label>
                        <input type="text" name="name" value="<?= e($form['name']) ?>" required>
                        <?php if(isset($errors['name'])):?><span class="err"><?= e($errors['name'])?></span><?php endif;?></div>
                    <div class="field <?= isset($errors['email'])?'has-error':'' ?>"><label>E-mail *</label>
                        <input type="email" name="email" value="<?= e($form['email']) ?>" required>
                        <?php if(isset($errors['email'])):?><span class="err"><?= e($errors['email'])?></span><?php endif;?></div>
                    <div class="field"><label>Telefon</label>
                        <input type="text" name="phone" value="<?= e($form['phone']) ?>"></div>
                    <div class="field <?= isset($errors['message'])?'has-error':'' ?>"><label>Zpráva *</label>
                        <textarea name="message" rows="6" required><?= e($form['message']) ?></textarea>
                        <?php if(isset($errors['message'])):?><span class="err"><?= e($errors['message'])?></span><?php endif;?></div>
                    <button type="submit" class="btn">Odeslat</button>
                </form>
            <?php endif; ?>
        </div>
    </div>
</section>

<?php require __DIR__ . '/inc/footer.php';