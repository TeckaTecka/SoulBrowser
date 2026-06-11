# ============================================================
# FÁZE 2: VLAN konfigurace — CSS326 MASTER + SLAVE (SwOS)
# Síť: Ostravia 40 ISP
# Datum: 2026-06-11
#
# CSS326 "Ostravia 40-1" = MASTER  (10.131.161.251, DNAT port 8081)
# CSS326 "Ostravia 40-2" = SLAVE   (10.131.161.250, DNAT port 8082)
#
# DŮLEŽITÉ: CSS326 běží SwOS (ne RouterOS).
# SwOS nemá plné CLI — VLAN konfigurace se provádí přes webové GUI.
# Tento soubor je PRŮVODCE a šablona hodnot, ne importovatelný script.
#
# SwOS GUI je dostupné přes: http://194.108.250.74:8081 (MASTER)
#                             http://194.108.250.74:8082 (SLAVE)
#
# PREREKVIZITA: Fáze 1 (port isolation) musí být hotova a ověřena.
# ============================================================


# ============================================================
# ČÁST A: FÁZE 1 — Port Isolation (GUI postup)
# ============================================================
# Toto je URGENTNÍ krok — lze provést bez výpadku klientů.
#
# MASTER CSS326 (10.131.161.251):
#   1. Přihlásit se: http://194.108.250.74:8081
#   2. Záložka: Ports
#   3. Sloupec "Isolation":
#      - P1–P23: zaškrtnout (enabled)
#      - P24 (UPLINKEagle): NEZAŠKRTÁVAT (musí zůstat průchozí)
#   4. Kliknout "Použít vše"
#
# SLAVE CSS326 (10.131.161.250):
#   1. Přihlásit se: http://194.108.250.74:8082
#   2. Záložka: Ports
#   3. Sloupec "Isolation":
#      - P1–P23: zaškrtnout (enabled)
#      - P24 (LinkSwitch1/uplink): NEZAŠKRTÁVAT
#   4. Kliknout "Použít vše"
#
# Ověření: Dva klienti na různých portech stejného CSS326
#          nesmí pingnout přes L2 (ARP odpověď nesmí dojít).


# ============================================================
# ČÁST B: FÁZE 1 — DHCP Snooping oprava (GUI postup)
# ============================================================
# Proveďte na MASTER i SLAVE.
#
#   1. Záložka: System
#   2. Sekce "DHCP & PPPoE Snooping"
#   3. "Důvěryhodné přístavy" (Trusted Ports):
#      - P1–P23: ODŠKRTNOUT
#      - P24: ponechat zaškrtnutý (uplink = důvěryhodný)
#   4. Kliknout "Použít vše"


# ============================================================
# ČÁST C: FÁZE 1 — SNMP oprava (GUI postup)
# ============================================================
#   1. Záložka: SNMP
#   2. Community string: změnit z "public" na bezpečný řetězec
#      (minimálně 12 znaků, kombinace písmen/číslic/symbolů)
#   3. Uložit


# ============================================================
# ČÁST D: FÁZE 2 — VLAN konfigurace MASTER CSS326
# ============================================================
# Proveďte až po dokončení VLAN konfigurace na CCR2116!
# Maintenance window doporučena.
#
# VLAN záložka v SwOS → přidat VLANy a přiřadit porty:
#
# Trunk porty (nesou všechny VLANy tagovaně):
#   P23 (LinkSwitch2 → SLAVE): TAGGED pro VLANy 101–144, 199
#   P24 (UPLINKEagle → CCR2116): TAGGED pro VLANy 101–144, 199
#
# Access porty (každý port = jeden VLAN, untagged):
# ┌──────┬──────────────────┬─────────┬──────────────────────────┐
# │ Port │ Místnost         │ PVID    │ VLAN ID (untagged member) │
# ├──────┼──────────────────┼─────────┼──────────────────────────┤
# │  P1  │ Místnost 10      │  101    │  101                     │
# │  P2  │ Místnost 42      │  102    │  102                     │
# │  P3  │ Místnost 43      │  103    │  103                     │
# │  P4  │ Místnost 45      │  104    │  104                     │
# │  P5  │ Místnost 49      │  105    │  105                     │
# │  P6  │ Místnost 51      │  106    │  106                     │
# │  P7  │ Místnost 52      │  107    │  107                     │
# │  P8  │ Místnost 89      │  108    │  108                     │
# │  P9  │ Místnost 91      │  109    │  109                     │
# │ P10  │ Místnost 93      │  110    │  110                     │
# │ P11  │ Místnost 94      │  111    │  111                     │
# │ P12  │ Místnost 97      │  112    │  112                     │
# │ P13  │ Místnost 130     │  113    │  113                     │
# │ P14  │ Místnost 131     │  114    │  114                     │
# │ P15  │ Místnost 132     │  115    │  115                     │
# │ P16  │ Místnost 133/1   │  116    │  116                     │
# │ P17  │ Místnost 133/2   │  117    │  117                     │
# │ P18  │ Místnost 201     │  118    │  118                     │
# │ P19  │ Bistro           │  119    │  119                     │
# │ P20  │ Výměník          │  120    │  120                     │
# │ P21  │ Malá zasedačka   │  121    │  121                     │
# │ P22  │ Velký sál        │  122    │  122                     │
# │ P23  │ LinkSwitch2→SLAVE│ trunk   │  tagged: 101–144, 199    │
# │ P24  │ UPLINK Eagle     │ trunk   │  tagged: 101–144, 199    │
# └──────┴──────────────────┴─────────┴──────────────────────────┘
#
# Postup v SwOS GUI:
#   1. Záložka VLAN
#   2. Pro každý VLAN ID (101–122):
#      - Add VLAN: ID = 101, Name = "Mistnost-10"
#      - Přiřadit P1 jako Untagged, P23 a P24 jako Tagged
#   3. Po nastavení všech VLANů kliknout "Použít vše"
#   4. Záložka Ports → pro každý access port nastavit PVID (Port VLAN ID)
#   5. Kliknout "Použít vše"


# ============================================================
# ČÁST E: FÁZE 2 — VLAN konfigurace SLAVE CSS326
# ============================================================
#
# Trunk port:
#   P24 (LinkSwitch1 → MASTER): TAGGED pro VLANy 101–144, 199
#   (VLAN tagy procházejí přes MASTER P23 → Eagle → CCR2116)
#
# Access porty:
# ┌──────┬──────────────────┬─────────┬──────────────────────────┐
# │ Port │ Místnost         │ PVID    │ VLAN ID (untagged member) │
# ├──────┼──────────────────┼─────────┼──────────────────────────┤
# │  P1  │ Místnost 229     │  123    │  123                     │
# │  P2  │ Místnost 233     │  124    │  124                     │
# │  P3  │ Místnost 235     │  125    │  125                     │
# │  P4  │ Místnost 301     │  126    │  126                     │
# │  P5  │ Místnost 373     │  127    │  127                     │
# │  P6  │ Místnost 402     │  128    │  128                     │
# │  P7  │ Místnost 403     │  129    │  129                     │
# │  P8  │ Místnost 404     │  130    │  130                     │
# │  P9  │ Místnost 416     │  131    │  131                     │
# │ P10  │ Místnost 430/1   │  132    │  132                     │
# │ P11  │ Místnost 430/1   │  132    │  132 (stejná místnost)   │
# │ P12  │ Místnost 504     │  133    │  133                     │
# │ P13  │ (nepojmenovaný)  │  134    │  134                     │
# │ P14  │ (nepojmenovaný)  │  135    │  135                     │
# │ P15  │ (nepojmenovaný)  │  136    │  136                     │
# │ P16  │ (nepojmenovaný)  │  137    │  137                     │
# │ P17  │ (nepojmenovaný)  │  138    │  138                     │
# │ P18  │ (nepojmenovaný)  │  139    │  139                     │
# │ P19  │ (nepojmenovaný)  │  140    │  140                     │
# │ P20  │ (nepojmenovaný)  │  141    │  141                     │
# │ P21  │ (nepojmenovaný)  │  142    │  142                     │
# │ P22  │ (nepojmenovaný)  │  143    │  143                     │
# │ P23  │ (nepojmenovaný)  │  144    │  144                     │
# │ P24  │ LinkSwitch1→MSTR │ trunk   │  tagged: 101–144, 199    │
# └──────┴──────────────────┴─────────┴──────────────────────────┘


# ============================================================
# ČÁST F: Eagle switch — VLAN trunk
# ============================================================
# Eagle switch (RouterOS, 10.131.161.254) musí propouštět
# VLAN tagy na portu směrem k CSS326 MASTER i na portu
# směrem k CCR2116 ether2.
#
# Na Eagle switchi (RouterOS):
#
# /interface bridge vlan
# add bridge=bridge1 vlan-ids=101-144 tagged=<port-k-CCR>,<port-k-CSS326>
# add bridge=bridge1 vlan-ids=199     tagged=<port-k-CCR>,<port-k-CSS326>
#
# Nebo pokud Eagle switch používá simple bridge (bez VLAN filtering):
# Zajistit, že port na straně CSS326 a port na straně CCR2116
# jsou oba trunk porty (admit-all nebo admit-only-tagged).


# ============================================================
# OVĚŘENÍ VLAN konfigurace na CSS326:
#
# 1. Po konfiguraci VLAN na P1 (VLAN 101):
#    - Klient na P1 spustí DHCP → musí dostat IP z rozsahu VLAN 101
#    - Klient na P1 nemůže pingnout klienta na P2 (různé VLANy)
#    - Klient na P1 MŮŽE pingnout gateway (CCR2116 vlan101 interface)
#    - Klient na P1 MŮŽE pingnout internet (src-NAT funguje)
#
# 2. Testovat na jednom portu nejdřív — neměnit všechny najednou!
#
# 3. Paketový zachyt pro ověření tagů:
#    Na CCR2116: /tool packet-sniffer interface=ether2
#    Pakety z P1 CSS326 MASTER musí mít VLAN tag 101.
# ============================================================
