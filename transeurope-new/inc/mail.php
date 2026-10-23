<?php
/** Odeslání e-mailu (prostý text) s volitelnou jednou přílohou. */
declare(strict_types=1);

/**
 * @param array|null $attachment ['name'=>..., 'type'=>..., 'tmp'=>cesta] nebo null
 */
function te_send_mail(string $subject, string $body, string $replyTo, ?array $attachment = null): bool {
    $fromName = '=?UTF-8?B?' . base64_encode('Web TRANSEUROPE') . '?=';
    $encSubject = '=?UTF-8?B?' . base64_encode($subject) . '?=';
    $eol = "\r\n";

    $headers  = 'From: ' . $fromName . ' <' . TE_MAIL_FROM . '>' . $eol;
    if ($replyTo !== '' && filter_var($replyTo, FILTER_VALIDATE_EMAIL)) {
        $headers .= 'Reply-To: ' . $replyTo . $eol;
    }
    $headers .= 'MIME-Version: 1.0' . $eol;
    $headers .= 'X-Mailer: PHP/' . phpversion() . $eol;

    if ($attachment && is_file($attachment['tmp'])) {
        $content = chunk_split(base64_encode((string)file_get_contents($attachment['tmp'])));
        $fname   = preg_replace('/[^\w.\- ]+/u', '_', (string)$attachment['name']);
        $type    = $attachment['type'] ?: 'application/octet-stream';
        $bnd     = 'np' . md5((string)mt_rand());
        $headers .= 'Content-Type: multipart/mixed; boundary="' . $bnd . '"';
        $msg  = '--' . $bnd . $eol;
        $msg .= 'Content-Type: text/plain; charset=UTF-8' . $eol;
        $msg .= 'Content-Transfer-Encoding: 8bit' . $eol . $eol;
        $msg .= $body . $eol . $eol;
        $msg .= '--' . $bnd . $eol;
        $msg .= 'Content-Type: ' . $type . '; name="' . $fname . '"' . $eol;
        $msg .= 'Content-Transfer-Encoding: base64' . $eol;
        $msg .= 'Content-Disposition: attachment; filename="' . $fname . '"' . $eol . $eol;
        $msg .= $content . $eol;
        $msg .= '--' . $bnd . '--';
        return @mail(TE_MAIL_TO, $encSubject, $msg, $headers);
    }

    $headers .= 'Content-Type: text/plain; charset=UTF-8';
    return @mail(TE_MAIL_TO, $encSubject, $body, $headers);
}
