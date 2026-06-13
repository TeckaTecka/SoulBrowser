# =============================================================================
# TAMPACO Reznicka – WireGuard VPN (nahrada za PPTP)
#
# PPTP pouziva MS-CHAPv2, ktery je kryptograficky prolomeny.
# WireGuard je modern, rychly, nativni v RouterOS 7.
#
# Port 13231/udp uz je ve firewallu povolen (aktualne pro WireGuard/OVPN).
#
# POSTUP:
#   1. Importujte tento skript: /import file-name=tampaco-wireguard.rsc
#   2. Skript vytvori WireGuard interface a vypise verejny klic
#   3. Pro kazdeho klienta vytvorte peer (viz sekce PEER PRIKLAD nize)
#   4. Klientovi predejte konfiguraci (viz sekce CLIENT CONFIG nize)
#   5. Po overeni funkce PPTP zakaze (viz konec skriptu)
#
# DOPAD NA STAVAJICI PPTP KLIENTA (Harper, 192.168.172.222):
#   - Harper bude potrebovat novy VPN klient konfig (WireGuard app)
#   - Jinak zadna zmena na jeho PC/site
# =============================================================================

# --- Vytvorit WireGuard interface -------------------------------------------
/interface wireguard {
  :if ([:len [find name=wg-tampaco]] = 0) do={
    add name=wg-tampaco listen-port=13231 mtu=1420 \
      comment="WireGuard VPN - nahrada PPTP"
    :log info "TAMPACO: WireGuard interface wg-tampaco vytvoren"
  } else={
    :log info "TAMPACO: WireGuard interface uz existuje"
  }
}

# Vypsat verejny klic (potrebny pro peer konfiguraci klientu)
:local pubkey [/interface wireguard get wg-tampaco public-key]
:put "=== ROUTER VEREJNY KLIC (zkopirujte pro klientske konfigurace) ==="
:put $pubkey
:put "==================================================================="

# --- IP adresa pro WireGuard tunel ------------------------------------------
/ip address {
  :if ([:len [find interface=wg-tampaco]] = 0) do={
    add address=10.10.10.1/24 interface=wg-tampaco \
      comment="WireGuard VPN subnet"
    :log info "TAMPACO: IP 10.10.10.1/24 prirazena wg-tampaco"
  }
}

# --- Firewall: WireGuard port (13231/udp) uz je povolen --------------------
# Overeni ze pravidlo existuje, pokud ne - pridame
/ip firewall filter {
  :if ([:len [find dst-port=13231 protocol=udp]] = 0) do={
    add chain=input action=accept protocol=udp dst-port=13231 \
      in-interface-list=WAN comment="WireGuard VPN" \
      place-before=[find action=drop chain=input comment~"drop all"]
    :log info "TAMPACO: WireGuard firewall pravidlo pridano"
  }
}

# --- Firewall: VPN klienti mohou na internet (přes WAN) --------------------
/ip firewall nat {
  :if ([:len [find comment="WireGuard masquerade"]] = 0) do={
    add chain=srcnat action=masquerade \
      out-interface-list=WAN src-address=10.10.10.0/24 \
      comment="WireGuard masquerade"
  }
}

# =============================================================================
# PEER PRIKLAD – spustit manualne pro kazdeho VPN klienta
# Upravte: public-key (z klientske aplikace), allowed-address, comment
# =============================================================================
# /interface wireguard peers
# add interface=wg-tampaco \
#     public-key="KLIENTUV-VEREJNY-KLIC-ZDE==" \
#     allowed-address=10.10.10.2/32 \
#     comment="Harper - VPN pristup VLAN172"
#
# Pro Harper (nahrada PPTP, remote-address byl 192.168.172.222):
# add interface=wg-tampaco \
#     public-key="HARPERUV-KLIC==" \
#     allowed-address=10.10.10.2/32 \
#     comment="Harper VPN"

# =============================================================================
# KLIENTSKA KONFIGURACE (WireGuard app na PC/mobilu)
# Nahradte ROUTER_VEREJNY_KLIC a ROUTER_VEREJNA_IP
# =============================================================================
# [Interface]
# PrivateKey = <vygenerovat v klientske aplikaci>
# Address = 10.10.10.2/24
# DNS = 192.168.172.1
#
# [Peer]
# PublicKey = <ROUTER_VEREJNY_KLIC>
# Endpoint = <ROUTER_VEREJNA_IP>:13231
# AllowedIPs = 192.168.172.0/24, 192.168.170.0/24
# PersistentKeepalive = 25

# =============================================================================
# PO OVERENI FUNKCE – zakaz PPTP (odkomentirejte az WireGuard funguje)
# =============================================================================
# /interface ovpn-server server
#   set [find] disabled=yes
# /ppp secret
#   set [find name=Harper] disabled=yes
# :log info "TAMPACO: PPTP zakazan"

:log info "TAMPACO: tampaco-wireguard.rsc dokoncen"
:put ""
:put "Dalsi kroky:"
:put "1. Zkopirujte verejny klic routeru (vyse)"
:put "2. Na klientskem PC nainstalujte WireGuard (wireguard.com)"
:put "3. V klientske aplikaci vygenerujte klic a pridejte peer na routeru"
:put "4. Po overeni VPN zakaz PPTP (viz konec tohoto skriptu)"
