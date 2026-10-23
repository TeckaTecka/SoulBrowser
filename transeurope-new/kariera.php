<?php
declare(strict_types=1);
require __DIR__ . '/config.php';
require __DIR__ . '/inc/mail.php';
$page = 'sluzby';
$pageTitle = 'Kariéra';

$sent = false; $errors = [];
$form = ['name' => '', 'email' => '', 'phone' => '', 'message' => ''];
if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['form'] ?? '') === 'kariera') {
    if (($_POST['website'] ?? '') !== '') { $sent = true; }
    else {
        foreach ($form as $k => $_) { $form[$k] = trim((string)($_POST[$k] ?? '')); }
        if ($form['name'] === '')  { $errors['name'] = 'Vyplňte prosím jméno.'; }
        if ($form['email'] === '' || !filter_var($form['email'], FILTER_VALIDATE_EMAIL)) { $errors['email'] = 'Zadejte platný e-mail.'; }

        $attach = null;
        if (!empty($_FILES['cv']['name']) && ($_FILES['cv']['error'] ?? 4) === UPLOAD_ERR_OK) {
            $okExt = ['pdf','doc','docx','odt','jpg','jpeg','png'];
            $ext = strtolower(pathinfo($_FILES['cv']['name'], PATHINFO_EXTENSION));
            if (!in_array($ext, $okExt, true)) { $errors['cv'] = 'Povolené formáty: PDF, DOC, DOCX, ODT, JPG, PNG.'; }
            elseif ($_FILES['cv']['size'] > 5 * 1024 * 1024) { $errors['cv'] = 'Maximální velikost přílohy je 5 MB.'; }
            else { $attach = ['name' => $_FILES['cv']['name'], 'type' => $_FILES['cv']['type'], 'tmp' => $_FILES['cv']['tmp_name']]; }
        }

        if (!$errors) {
            $body = "Reakce na inzerát Řidič - mezinárodní kamionová doprava (www.transeurope.cz)\n\n"
                  . "Jméno:   {$form['name']}\nEmail:   {$form['email']}\nTelefon: {$form['phone']}\n\nZpráva:\n{$form['message']}\n";
            if (te_send_mail('Přihláška řidiče - web TRANSEUROPE', $body, $form['email'], $attach)) {
                $sent = true; $form = ['name'=>'','email'=>'','phone'=>'','message'=>''];
            } else { $errors['_send'] = 'Přihlášku se nepodařilo odeslat. Napište prosím na transeurope@transeurope.cz.'; }
        }
    }
}
require __DIR__ . '/inc/header.php';
?>

<div class="page-head">
    <div class="bg" style="background-image:url('img/nabor.jpg')"></div>
    <div class="centeris"><h1>Kariéra</h1></div>
</div>
<div class="breadcrumb"><div class="centeris"><a href="index.php">Úvod</a> &rsaquo; <a href="sluzby.php">Služby</a> &rsaquo; Kariéra</div></div>

<section class="section">
    <div class="centeris">
        <div class="prose">
            <h2>Řidič &ndash; mezinárodní kamionová doprava</h2>

            <h3 style="color:var(--navy);margin-top:1.2em;">Požadavky na řidiče</h3>
            <ul class="svc-list" style="grid-template-columns:1fr;">
                <li>Řidičský průkaz <strong>C+E</strong></li>
                <li><strong>Kartu</strong> do digitálního tachografu</li>
                <li>Psychologické <strong>vyšetření</strong></li>
                <li><strong>Výpis</strong> z karty řidiče</li>
                <li><strong>Praxi</strong> v řízení vozidel C+E</li>
            </ul>

            <h3 style="color:var(--navy);margin-top:1.4em;">Nabízíme</h3>
            <ul class="svc-list" style="grid-template-columns:1fr 1fr;">
                <li><strong>Fixní</strong> základ mzdy</li>
                <li><strong>Prémie</strong> za úspory PHM a péči o vozidlo</li>
                <li><strong>Příplatky</strong> za nakládky a prostoje</li>
                <li>100% <strong>diety</strong></li>
                <li>Moderní <strong>autopark</strong> (Volvo, euro 5 a 6)</li>
                <li>Přidělené <strong>vozidlo</strong> s návěsem</li>
            </ul>

            <h3 style="color:var(--navy);margin-top:1.4em;">Popis pozice</h3>
            <p>Práce řidiče je <strong>náročné povolání</strong>, které ovšem <strong>není stereotypní</strong>. Nabízí
               každodenní změnu, řidič pozná mnoho <strong>nových krajů a zvyků</strong> a naučí se komunikovat s cizími
               lidmi i přes jazykovou barieru.</p>
            <p>Hlavní směry dopravy &ndash; <strong>Německo, Benelux, Anglie a Švédsko</strong>. Export z 90 %
               z moravskoslezského kraje a z 80 % návrat z každé trasy do místa firmy. <strong>Nejezdíme</strong>
               Rusko, Turecko apod. Řádné <strong>týdenní odpočinky</strong> v místě bydliště, dodržování zákona 561/2006.</p>
            <p>Veškeré mýto a tankování <strong>bezhotovostně</strong> a tankovacími kartami.</p>

            <h2 style="margin-top:1.6em;">Reagovat na inzerát</h2>
            <?php if ($sent): ?>
                <div class="alert alert-ok"><strong>Přihláška byla odeslána.</strong> Děkujeme, ozveme se Vám.</div>
            <?php else: ?>
                <?php if (!empty($errors['_send'])): ?><div class="alert alert-err"><?= e($errors['_send']) ?></div><?php endif; ?>
                <form class="form-card" method="post" action="kariera.php" enctype="multipart/form-data">
                    <input type="hidden" name="form" value="kariera">
                    <div class="hp"><input type="text" name="website" tabindex="-1" autocomplete="off"></div>
                    <div class="field <?= isset($errors['name'])?'has-error':'' ?>"><label>Jméno a příjmení *</label>
                        <input type="text" name="name" value="<?= e($form['name']) ?>" required>
                        <?php if(isset($errors['name'])):?><span class="err"><?= e($errors['name'])?></span><?php endif;?></div>
                    <div class="field <?= isset($errors['email'])?'has-error':'' ?>"><label>E-mail *</label>
                        <input type="email" name="email" value="<?= e($form['email']) ?>" required>
                        <?php if(isset($errors['email'])):?><span class="err"><?= e($errors['email'])?></span><?php endif;?></div>
                    <div class="field"><label>Telefon</label>
                        <input type="text" name="phone" value="<?= e($form['phone']) ?>"></div>
                    <div class="field"><label>Zpráva</label>
                        <textarea name="message" rows="5"><?= e($form['message']) ?></textarea></div>
                    <div class="field <?= isset($errors['cv'])?'has-error':'' ?>"><label>Životopis (příloha)</label>
                        <input type="file" name="cv" accept=".pdf,.doc,.docx,.odt,.jpg,.jpeg,.png">
                        <?php if(isset($errors['cv'])):?><span class="err"><?= e($errors['cv'])?></span><?php endif;?></div>
                    <button type="submit" class="btn">Odeslat přihlášku</button>
                </form>
            <?php endif; ?>
        </div>
    </div>
</section>

<?php require __DIR__ . '/inc/footer.php';