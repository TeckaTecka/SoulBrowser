# ============================================================
# FÁZE 1: Okamžité bezpečnostní opravy — CCR2116-12G-4S+
# Síť: Ostravia 40 ISP
# Datum: 2026-06-11
#
# Tento script lze importovat přes:
#   /import file=phase1-immediate-fixes.rsc
#
# POZOR: Před importem uložte zálohu!
#   /system backup save name=before-phase1
#
# NEDOTÝKÁ SE: ether10, ether11, ether12 (VIP klienti)
#              NAT pravidla, DHCP servery, Dude monitoring
# ============================================================

# ------------------------------------------------------------
# ČÁST 1: Forward firewall — blokovat cross-tenant provoz
# ------------------------------------------------------------
# Problém: Stávající pravidla ACCEPT veškerý provoz z/do 10.131.161.0/24,
# včetně provozu mezi nájemci. Po zapnutí port isolation na CSS326
# bude veškerý provoz procházet tímto routerem — bez těchto DROP pravidel
# by byl cross-tenant provoz stále dostupný přes L3 směrování.
# DROP pravidla se vkládají NA ZAČÁTEK forward chain (place-before=0).

/ip firewall filter

# Blokovat provoz blackhole pool (zařízení bez MAC-lock) — na internet
add chain=forward \
    src-address=10.131.190.0/24 \
    action=drop \
    comment="SECURITY: blackhole pool - src" \
    place-before=0

# Blokovat provoz na blackhole pool
add chain=forward \
    dst-address=10.131.190.0/24 \
    action=drop \
    comment="SECURITY: blackhole pool - dst" \
    place-before=1

# Blokovat vzájemnou komunikaci nájemců (10.131.161.x ↔ 10.131.161.x)
# Výjimka: ether10/11/12 (VIP) jsou jiné podsítě — toto pravidlo se jich nedotkne
add chain=forward \
    src-address=10.131.161.0/24 \
    dst-address=10.131.161.0/24 \
    action=drop \
    comment="SECURITY: blokovat cross-tenant L3 provoz" \
    place-before=2

# Blokovat komunikaci nájemci ↔ Dalkia (jsou oddělené subjekty)
add chain=forward \
    src-address=10.131.162.0/24 \
    dst-address=10.131.161.0/24 \
    action=drop \
    comment="SECURITY: Dalkia nesmi videt najemce" \
    place-before=3

add chain=forward \
    src-address=10.131.161.0/24 \
    dst-address=10.131.162.0/24 \
    action=drop \
    comment="SECURITY: najemce nesmi videt Dalkia" \
    place-before=4

# ------------------------------------------------------------
# ČÁST 2: Zakázat PPTP VPN server
# ------------------------------------------------------------
# PPTP používá MPPE šifrování (RC4/MD4) — prolomitelné, Microsoft
# doporučuje migraci na IKEv2 nebo WireGuard od roku 2012.

/interface pptp-server server
set enabled=no

# ------------------------------------------------------------
# ČÁST 3: SSH remote port forwarding
# ------------------------------------------------------------
# Remote forwarding umožňuje uživateli přesměrovat port z externího
# serveru přes SSH tunel na router — efektivně obchází firewall.
# Lokální forwarding (pro správu) zůstane funkční.

/ip ssh
set forwarding-enabled=local

# ------------------------------------------------------------
# ČÁST 4: IPSec — nahradit 3DES za AES-256-GCM
# ------------------------------------------------------------
# 3DES je zranitelný vůči SWEET32 útoku (RFC 7465, RFC 8996).
# AES-256-GCM je hardware-akcelerovaný na CCR2116 (ASIC).
# POZOR: Tato změna vyžaduje aktualizaci nastavení na druhé straně VPN!

/ip ipsec proposal
set [find enc-algorithms~"3des"] enc-algorithms=aes-256-gcm \
    comment="Upgraded from 3DES - 2026-06-11"

# ------------------------------------------------------------
# ČÁST 5: DNS — zakázat open resolver pro nájemce
# ------------------------------------------------------------
# Aktuální nastavení allow-remote-requests=yes umožňuje jakémukoliv
# nájemci používat tento router jako DNS resolver — zbytečná expozice.
# Nájemci typicky používají 8.8.8.8 nebo svůj vlastní DNS.
#
# POZOR: Pokud nájemci DNS na CCR2116 používají (DHCP jim ho přiděluje),
# NEJDŘÍVE změňte DHCP server DNS na 8.8.8.8/1.1.1.1, pak teprve
# aplikujte tento řádek!
#
# Odkomentujte až po ověření, že nájemci DNS tohoto routeru nepotřebují:
# /ip dns set allow-remote-requests=no

# ------------------------------------------------------------
# ČÁST 6: Logovací pravidla pro monitoring cross-tenant pokusů
# ------------------------------------------------------------
# Přidat log akci před DROP pravidla — vidíte v /log, kdo se snaží
# komunikovat cross-tenant (odkomentujte pokud chcete logovat).

# /ip firewall filter
# add chain=forward \
#     src-address=10.131.161.0/24 \
#     dst-address=10.131.161.0/24 \
#     action=log \
#     log-prefix="CROSS-TENANT: " \
#     place-before=[find comment="SECURITY: blokovat cross-tenant L3 provoz"]

# ============================================================
# OVĚŘENÍ PO IMPORTU:
#
# 1. Zkontrolovat pořadí pravidel:
#    /ip firewall filter print where chain=forward
#    -> DROP pravidla musí být PŘED accept pravidly pro 10.131.161.0/24
#
# 2. Test cross-tenant blokování:
#    Z PC nájemce A: ping <IP nájemce B>  -> musí selhat
#    Z PC nájemce A: ping 8.8.8.8         -> musí projít
#
# 3. Test VIP klientů (ether10/11/12):
#    ping jejich IP                         -> musí projít
#
# 4. Blackhole pool:
#    Zařízení s neznámou MAC              -> ping na internet musí selhat
# ============================================================
