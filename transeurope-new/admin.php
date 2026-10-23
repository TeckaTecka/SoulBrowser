<?php
declare(strict_types=1);
session_start();
require __DIR__ . '/config.php';

/* Pomůcka: admin.php?heslo=NOVE vypíše hash pro config.php */
if (isset($_GET['heslo'])) {
    header('Content-Type: text/plain; charset=UTF-8');
    echo "Hash pro config.php (TE_ADMIN_HASH):\n\n" . password_hash((string)$_GET['heslo'], PASSWORD_DEFAULT) . "\n";
    exit;
}

$msg = ''; $err = '';

if (isset($_GET['odhlasit'])) { $_SESSION = []; session_destroy(); header('Location: admin.php'); exit; }

if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['akce'] ?? '') === 'login') {
    if (password_verify((string)($_POST['heslo'] ?? ''), te_admin_hash())) {
        session_regenerate_id(true); $_SESSION['te_admin'] = true;
    } else { $err = 'Nesprávné heslo.'; }
}
$logged = !empty($_SESSION['te_admin']);

/* Uložení cen PHM */
if ($logged && $_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['akce'] ?? '') === 'save_prices') {
    $vals = [];
    foreach (TE_FUELS as $i => $_) {
        $v = trim((string)($_POST['cena'][$i] ?? ''));
        $v = str_replace([',', ' '], ['.', ''], $v);           // 44,94 -> 44.94
        $vals[] = $v;
    }
    if (@file_put_contents(TE_PRICES_FILE, implode("\n", $vals) . "\n") !== false) {
        $msg = 'Ceny PHM byly uloženy.';
    } else { $err = 'Soubor ceny-phm.txt se nepodařilo zapsat. Zkontrolujte práva k zápisu.'; }
}

/* Změna hesla */
if ($logged && $_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['akce'] ?? '') === 'change_password') {
    $stare = (string)($_POST['stare'] ?? ''); $nove = (string)($_POST['nove'] ?? ''); $nove2 = (string)($_POST['nove2'] ?? '');
    if (!password_verify($stare, te_admin_hash())) { $err = 'Stávající heslo není správné.'; }
    elseif (mb_strlen($nove) < 6) { $err = 'Nové heslo musí mít alespoň 6 znaků.'; }
    elseif ($nove !== $nove2) { $err = 'Nové heslo a jeho potvrzení se neshodují.'; }
    elseif (@file_put_contents(TE_ADMIN_HASH_FILE, password_hash($nove, PASSWORD_DEFAULT) . "\n") === false) { $err = 'Heslo se nepodařilo uložit.'; }
    else { $msg = 'Heslo bylo změněno.'; }
}

$prices = te_load_prices();
?>
<!DOCTYPE html>
<html lang="cs">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>TRANSEUROPE – správa cen PHM</title>
<link rel="icon" href="img/favicon.png">
<style>
    body{font-family:"Segoe UI",Roboto,Arial,sans-serif;background:#f4f5f9;color:#2a2d3a;margin:0;padding:40px 16px;}
    .box{max-width:640px;margin:0 auto;background:#fff;border:1px solid #e6e8f0;border-radius:10px;box-shadow:0 10px 30px rgba(27,30,77,.1);padding:32px;}
    h1{color:#1b1e4d;margin:0 0 .3em;font-size:1.5rem;}h2{color:#1b1e4d;font-size:1.15rem;margin:0 0 .3em;}
    p.sub{color:#6b7280;margin:0 0 1.3em;}
    label{display:block;font-weight:600;color:#1b1e4d;margin-bottom:6px;}
    input[type=text],input[type=password]{width:100%;padding:12px 14px;border:1px solid #d6dae6;border-radius:8px;font:inherit;box-sizing:border-box;}
    .btn{display:inline-block;background:#e2231a;color:#fff;border:0;cursor:pointer;padding:12px 26px;border-radius:8px;font:inherit;font-weight:700;margin-top:16px;}
    .btn:hover{background:#c21d14;}
    .btn-link{background:none;color:#e2231a;padding:0;font-weight:700;text-decoration:none;}
    .msg{background:#e7f6ec;border:1px solid #b6e2c4;color:#1c7a3e;padding:12px 16px;border-radius:8px;margin-bottom:18px;}
    .err{background:#fdecea;border:1px solid #f3c2bd;color:#b53225;padding:12px 16px;border-radius:8px;margin-bottom:18px;}
    .hint{color:#6b7280;font-size:.9rem;margin-top:8px;}
    .top{display:flex;justify-content:space-between;align-items:center;margin-bottom:1.3em;}
    .divider{border:0;border-top:1px solid #e6e8f0;margin:30px 0 24px;}
    .price-row{display:flex;align-items:center;gap:12px;margin-bottom:12px;}
    .price-row label{flex:0 0 150px;margin:0;}
    .price-row input{flex:1;}
    .price-row .unit{color:#6b7280;}
</style>
</head>
<body>
<div class="box">
<?php if ($msg): ?><div class="msg"><?= e($msg) ?></div><?php endif; ?>
<?php if ($err): ?><div class="err"><?= e($err) ?></div><?php endif; ?>

<?php if (!$logged): ?>
    <h1>Správa cen PHM</h1>
    <p class="sub">Zadejte heslo pro přístup ke správě cen na čerpací stanici.</p>
    <form method="post">
        <input type="hidden" name="akce" value="login">
        <label for="heslo">Heslo</label>
        <input type="password" id="heslo" name="heslo" autofocus required>
        <button type="submit" class="btn">Přihlásit</button>
    </form>
<?php else: ?>
    <div class="top"><h1>Správa cen PHM</h1><a class="btn-link" href="admin.php?odhlasit=1">Odhlásit</a></div>
    <p class="sub">Upravte ceny paliv zobrazené na úvodní stránce v sekci „Čerpací stanice". Uložením se ihned projeví.</p>
    <form method="post">
        <input type="hidden" name="akce" value="save_prices">
        <?php foreach (TE_FUELS as $i => $f): ?>
            <div class="price-row">
                <label for="c<?= $i ?>"><?= e(trim($f['brand'] . ' ' . ($f['sub'] === 'diesel' ? '' : $f['sub']))) ?></label>
                <input type="text" id="c<?= $i ?>" name="cena[<?= $i ?>]" value="<?= e($prices[$i]) ?>" inputmode="decimal">
                <span class="unit">Kč / litr</span>
            </div>
        <?php endforeach; ?>
        <p class="hint">Zadávejte číslo, např. <code>44.94</code> (čárka se převede automaticky).</p>
        <button type="submit" class="btn">Uložit ceny</button>
    </form>

    <hr class="divider">
    <h2>Změna hesla</h2>
    <p class="sub">Heslo pro přihlášení do této administrace.</p>
    <form method="post" autocomplete="off">
        <input type="hidden" name="akce" value="change_password">
        <label for="stare">Stávající heslo</label>
        <input type="password" id="stare" name="stare" required>
        <label for="nove" style="margin-top:14px;">Nové heslo</label>
        <input type="password" id="nove" name="nove" required>
        <label for="nove2" style="margin-top:14px;">Nové heslo znovu</label>
        <input type="password" id="nove2" name="nove2" required>
        <p class="hint">Nové heslo musí mít alespoň 6 znaků.</p>
        <button type="submit" class="btn">Změnit heslo</button>
    </form>
<?php endif; ?>
</div>
</body>
</html>
