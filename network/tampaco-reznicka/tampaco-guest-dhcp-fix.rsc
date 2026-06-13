# =============================================================================
# TAMPACO Reznicka – Oprava DHCP pro GUEST VLAN179
#
# Aktuálně GUEST dostává DNS server = 192.168.179.1 (router).
# Po zablokování DNS vstupu z VLAN179 (viz firewall-fix) by hosté
# neměli DNS. Tento skript změní DNS pro VLAN179 na veřejné 8.8.8.8.
#
# DOPAD NA KLIENTY:
#   - GUEST nájemci: DNS dotazy půjdou na Google DNS – transparentní
#   - Ostatní VLANy: beze změny
# =============================================================================

/ip dhcp-server network {
  :local guestNet [find address="192.168.179.0/24"]
  :if ([:len $guestNet] > 0) do={
    set $guestNet dns-server=8.8.8.8,1.1.1.1
    :log info "TAMPACO: GUEST VLAN179 DNS nastaveno na 8.8.8.8"
    :put "GUEST DNS opraven: 8.8.8.8, 1.1.1.1"
  } else={
    :log warning "TAMPACO: GUEST network 192.168.179.0/24 nenalezena"
  }
}
