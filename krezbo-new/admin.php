<?php
declare(strict_types=1);
session_start();
require __DIR__ . '/config.php';

/* Pomůcka: admin.php?heslo=NOVE  vypíše hash k vložení do config.php */
if (isset($_GET['heslo'])) {
    header('Content-Type: text/plain; charset=UTF-8');
    echo "Hash pro config.php (KREZBO_ADMIN_HASH):\n\n";
    echo password_hash((string)$_GET['heslo'], PASSWORD_DEFAULT) . "\n";
    exit;
}

$msg = '';
$err = '';

/* ---- Odhlášení ---- */
if (isset($_GET['odhlasit'])) {
    $_SESSION = [];
    session_destroy();
    header('Location: admin.php');
    exit;
}

/* ---- Přihlášení ---- */
if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['akce'] ?? '') === 'login') {
    if (password_verify((string)($_POST['heslo'] ?? ''), KREZBO_ADMIN_HASH)) {
        session_regenerate_id(true);
        $_SESSION['krezbo_admin'] = true;
    } else {
        $err = 'Nesprávné heslo.';
    }
}

$logged = !empty($_SESSION['krezbo_admin']);

/* ---- Uložení oznámení ---- */
if ($logged && $_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['akce'] ?? '') === 'save') {
    $text = trim((string)($_POST['oznameni'] ?? ''));
    // Normalizace konců řádků
    $text = str_replace(["\r\n", "\r"], "\n", $text);
    if (@file_put_contents(KREZBO_NOTICE_FILE, $text) !== false) {
        $msg = $text === '' ? 'Oznámení bylo smazáno – na webu se nic nezobrazuje.' : 'Oznámení bylo uloženo.';
    } else {
        $err = 'Soubor oznameni.txt se nepodařilo zapsat. Zkontrolujte prosím práva k zápisu.';
    }
}

$current = is_file(KREZBO_NOTICE_FILE) ? (string)file_get_contents(KREZBO_NOTICE_FILE) : '';

function e(string $s): string {
    return htmlspecialchars($s, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}
?>
<!DOCTYPE html>
<html lang="cs">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>KREZBO – správa oznámení</title>
<link rel="icon" href="img/krezbo.ico">
<style>
    body{font-family:"Segoe UI",Roboto,Arial,sans-serif;background:#f4f6f8;color:#243040;margin:0;padding:40px 16px;}
    .box{max-width:640px;margin:0 auto;background:#fff;border:1px solid #e2e7ec;border-radius:10px;
        box-shadow:0 6px 24px rgba(13,59,102,.08);padding:30px;}
    h1{color:#0d3b66;margin:0 0 .2em;font-size:1.5rem;}
    p.sub{color:#6b7785;margin:0 0 1.4em;}
    label{display:block;font-weight:600;color:#0d3b66;margin-bottom:6px;}
    input[type=password],textarea{width:100%;padding:12px 14px;border:1px solid #e2e7ec;border-radius:8px;
        font:inherit;box-sizing:border-box;}
    textarea{min-height:120px;resize:vertical;}
    .btn{display:inline-block;background:#0d3b66;color:#fff;border:0;cursor:pointer;padding:12px 26px;
        border-radius:8px;font:inherit;font-weight:600;margin-top:14px;}
    .btn:hover{background:#1769aa;}
    .btn-link{background:none;color:#1769aa;padding:0;font-weight:600;text-decoration:none;}
    .msg{background:#e6f5ea;border:1px solid #b7e0c2;color:#1f7a3d;padding:12px 16px;border-radius:8px;margin-bottom:18px;}
    .err{background:#fdecec;border:1px solid #f3c0c0;color:#b32222;padding:12px 16px;border-radius:8px;margin-bottom:18px;}
    .hint{color:#6b7785;font-size:.9rem;margin-top:8px;}
    .top{display:flex;justify-content:space-between;align-items:center;margin-bottom:1.4em;}
    .preview{background:#f4a300;color:#3a2c00;border-radius:8px;padding:12px 16px;margin-top:10px;font-weight:600;white-space:pre-wrap;}
</style>
</head>
<body>
<div class="box">

<?php if ($msg): ?><div class="msg"><?= e($msg) ?></div><?php endif; ?>
<?php if ($err): ?><div class="err"><?= e($err) ?></div><?php endif; ?>

<?php if (!$logged): ?>
    <h1>Správa oznámení</h1>
    <p class="sub">Zadejte heslo pro přístup ke správě oznámení na úvodní stránce.</p>
    <form method="post">
        <input type="hidden" name="akce" value="login">
        <label for="heslo">Heslo</label>
        <input type="password" id="heslo" name="heslo" autofocus required>
        <button type="submit" class="btn">Přihlásit</button>
    </form>
<?php else: ?>
    <div class="top">
        <h1>Správa oznámení</h1>
        <a class="btn-link" href="admin.php?odhlasit=1">Odhlásit</a>
    </div>
    <p class="sub">
        Sem napište krátké oznámení, které se zobrazí nahoře na úvodní stránce
        (např. „Zítra zavřeno" nebo „Ve středu 24.7. otevřeno jen do 12:00").
        Chcete-li oznámení zrušit, pole vymažte a uložte.
    </p>
    <form method="post">
        <input type="hidden" name="akce" value="save">
        <label for="oznameni">Text oznámení</label>
        <textarea id="oznameni" name="oznameni" placeholder="Nechte prázdné = žádné oznámení se nezobrazí"><?= e($current) ?></textarea>
        <p class="hint">Můžete použít i více řádků. Bez formátování (HTML se nezobrazí).</p>
        <?php if (trim($current) !== ''): ?>
            <div>Náhled aktuálního oznámení:</div>
            <div class="preview"><?= e(trim($current)) ?></div>
        <?php endif; ?>
        <button type="submit" class="btn">Uložit</button>
    </form>
<?php endif; ?>

</div>
</body>
</html>
