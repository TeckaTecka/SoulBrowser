# ============================================================
# FÁZE 2: VLAN segmentace — CCR2116-12G-4S+
# Síť: Ostravia 40 ISP
# Datum: 2026-06-11
#
# PREREKVIZITY:
#   - Fáze 1 musí být dokončena a ověřena
#   - CSS326 MASTER i SLAVE musí mít VLAN nakonfigurované
#     (viz phase2-vlan-crs326-template.rsc)
#   - Eagle switch musí propouštět VLAN tagy (trunk)
#   - Maintenance window doporučena (klienti ztratí konektivitu
#     na cca 2–5 minut při přesunu IP adres)
#
# Import: /import file=phase2-vlan-ccr2116.rsc
#
# ZÁLOHA PŘED IMPORTEM:
#   /system backup save name=before-phase2
# ============================================================

# ============================================================
# KROK 1: Vytvořit bridge s VLAN filtering
# ============================================================
# Bridge-isp nahradí přímé použití ether2.
# vlan-filtering=yes = bridge rozumí VLAN tagům a izoluje provoz.
# POZOR: frame-types=admit-only-vlan-tagged na bridge portu ether2
#        znamená, že untagged rámce z ether2 budou ZAHOZENY — toto
#        je žádoucí po dokončení VLAN konfigurace na CSS326.

/interface bridge
add name=bridge-isp \
    vlan-filtering=yes \
    protocol-mode=none \
    comment="ISP tenant bridge - VLAN izolace najemcu"

# ============================================================
# KROK 2: Přidat ether2 do bridge jako tagged trunk
# ============================================================
# ether2 → Eagle switch → CSS326 → nájemci
# Veškerý provoz z nájemců přichází tagovaný (CSS326 přidá VLAN tag).

/interface bridge port
add bridge=bridge-isp \
    interface=ether2 \
    frame-types=admit-only-vlan-tagged \
    comment="Trunk do Eagle/CSS326"

# ============================================================
# KROK 3: Registrovat VLANy na bridge (trunk port propagace)
# ============================================================

/interface bridge vlan
# MASTER CSS326 — místnosti 1. budova
add bridge=bridge-isp vlan-ids=101 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=102 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=103 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=104 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=105 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=106 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=107 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=108 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=109 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=110 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=111 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=112 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=113 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=114 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=115 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=116 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=117 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=118 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=119 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=120 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=121 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=122 tagged=bridge-isp,ether2
# SLAVE CSS326 — místnosti 2. budova
add bridge=bridge-isp vlan-ids=123 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=124 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=125 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=126 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=127 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=128 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=129 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=130 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=131 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=132 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=133 tagged=bridge-isp,ether2
# Rezerva pro SLAVE P13–P23 (nepojmenované porty)
add bridge=bridge-isp vlan-ids=134 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=135 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=136 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=137 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=138 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=139 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=140 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=141 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=142 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=143 tagged=bridge-isp,ether2
add bridge=bridge-isp vlan-ids=144 tagged=bridge-isp,ether2
# Dalkia (zvláštní nájemce — vlastní /29 na 10.131.162.x)
add bridge=bridge-isp vlan-ids=199 tagged=bridge-isp,ether2

# ============================================================
# KROK 4: Vytvořit VLAN interfacy (bridge subinterfacy)
# ============================================================
# Každý VLAN interface = jeden L3 gateway pro daný segment.
# IP adresy se přiřadí v Kroku 5.

/interface vlan
# MASTER CSS326 porty
add interface=bridge-isp name=vlan101 vlan-id=101 comment="Místnost 10"
add interface=bridge-isp name=vlan102 vlan-id=102 comment="Místnost 42"
add interface=bridge-isp name=vlan103 vlan-id=103 comment="Místnost 43"
add interface=bridge-isp name=vlan104 vlan-id=104 comment="Místnost 45"
add interface=bridge-isp name=vlan105 vlan-id=105 comment="Místnost 49"
add interface=bridge-isp name=vlan106 vlan-id=106 comment="Místnost 51"
add interface=bridge-isp name=vlan107 vlan-id=107 comment="Místnost 52"
add interface=bridge-isp name=vlan108 vlan-id=108 comment="Místnost 89"
add interface=bridge-isp name=vlan109 vlan-id=109 comment="Místnost 91"
add interface=bridge-isp name=vlan110 vlan-id=110 comment="Místnost 93"
add interface=bridge-isp name=vlan111 vlan-id=111 comment="Místnost 94"
add interface=bridge-isp name=vlan112 vlan-id=112 comment="Místnost 97"
add interface=bridge-isp name=vlan113 vlan-id=113 comment="Místnost 130"
add interface=bridge-isp name=vlan114 vlan-id=114 comment="Místnost 131"
add interface=bridge-isp name=vlan115 vlan-id=115 comment="Místnost 132"
add interface=bridge-isp name=vlan116 vlan-id=116 comment="Místnost 133/1"
add interface=bridge-isp name=vlan117 vlan-id=117 comment="Místnost 133/2"
add interface=bridge-isp name=vlan118 vlan-id=118 comment="Místnost 201"
add interface=bridge-isp name=vlan119 vlan-id=119 comment="Bistro"
add interface=bridge-isp name=vlan120 vlan-id=120 comment="Výměník"
add interface=bridge-isp name=vlan121 vlan-id=121 comment="Malá zasedačka"
add interface=bridge-isp name=vlan122 vlan-id=122 comment="Velký sál"
# SLAVE CSS326 porty
add interface=bridge-isp name=vlan123 vlan-id=123 comment="Místnost 229"
add interface=bridge-isp name=vlan124 vlan-id=124 comment="Místnost 233"
add interface=bridge-isp name=vlan125 vlan-id=125 comment="Místnost 235"
add interface=bridge-isp name=vlan126 vlan-id=126 comment="Místnost 301"
add interface=bridge-isp name=vlan127 vlan-id=127 comment="Místnost 373"
add interface=bridge-isp name=vlan128 vlan-id=128 comment="Místnost 402"
add interface=bridge-isp name=vlan129 vlan-id=129 comment="Místnost 403"
add interface=bridge-isp name=vlan130 vlan-id=130 comment="Místnost 404"
add interface=bridge-isp name=vlan131 vlan-id=131 comment="Místnost 416"
add interface=bridge-isp name=vlan132 vlan-id=132 comment="Místnost 430"
add interface=bridge-isp name=vlan133 vlan-id=133 comment="Místnost 504"
add interface=bridge-isp name=vlan134 vlan-id=134 comment="SLAVE P13 (rezerva)"
add interface=bridge-isp name=vlan135 vlan-id=135 comment="SLAVE P14 (rezerva)"
add interface=bridge-isp name=vlan136 vlan-id=136 comment="SLAVE P15 (rezerva)"
add interface=bridge-isp name=vlan137 vlan-id=137 comment="SLAVE P16 (rezerva)"
add interface=bridge-isp name=vlan138 vlan-id=138 comment="SLAVE P17 (rezerva)"
add interface=bridge-isp name=vlan139 vlan-id=139 comment="SLAVE P18 (rezerva)"
add interface=bridge-isp name=vlan140 vlan-id=140 comment="SLAVE P19 (rezerva)"
add interface=bridge-isp name=vlan141 vlan-id=141 comment="SLAVE P20 (rezerva)"
add interface=bridge-isp name=vlan142 vlan-id=142 comment="SLAVE P21 (rezerva)"
add interface=bridge-isp name=vlan143 vlan-id=143 comment="SLAVE P22 (rezerva)"
add interface=bridge-isp name=vlan144 vlan-id=144 comment="SLAVE P23 (rezerva)"
# Dalkia
add interface=bridge-isp name=vlan199 vlan-id=199 comment="Dalkia"

# ============================================================
# KROK 5: Přiřadit IP adresy na VLAN interfacy
# ============================================================
# DOPLŇTE správné IP adresy z aktuální DHCP/IP tabulky CCR2116!
# Vzor: /ip address print where interface=ether2
# Každý nájemce má /29 podsíť, gateway je první IP v bloku.
#
# Příklad struktury (doplňte skutečné hodnoty):

# /ip address
# add address=10.131.161.1/29   interface=vlan101 comment="Místnost 10 GW"
# add address=10.131.161.9/29   interface=vlan102 comment="Místnost 42 GW"
# add address=10.131.161.17/29  interface=vlan103 comment="Místnost 43 GW"
# add address=10.131.161.25/29  interface=vlan104 comment="Místnost 45 GW"
# add address=10.131.161.33/29  interface=vlan105 comment="Místnost 49 GW"
# ... (doplnit všechny /29 bloky)
# add address=10.131.162.1/29   interface=vlan199 comment="Dalkia GW"

# Po přiřazení IP smazat původní IP na ether2:
# /ip address remove [find interface=ether2]

# ============================================================
# KROK 6: DHCP servery — přemigrovat na VLAN interfacy
# ============================================================
# Stávající DHCP server "ISP" běží na ether2.
# Po migraci potřebujeme buď:
#   A) Jeden DHCP server per VLAN interface (čistší, doporučeno)
#   B) Jeden DHCP server s více pool-y na bridge-isp (méně přehledné)
#
# Varianta A (doporučeno) — každý tenant = vlastní DHCP server:
#
# /ip dhcp-server
# add name=dhcp-vlan101 interface=vlan101 address-pool=pool-vlan101 \
#     lease-time=12h authoritative=yes
# add name=dhcp-vlan102 interface=vlan102 address-pool=pool-vlan102 \
#     lease-time=12h authoritative=yes
# ... (opakovat pro všechny VLANy)
#
# /ip pool
# add name=pool-vlan101 ranges=10.131.161.2-10.131.161.6
# add name=pool-vlan102 ranges=10.131.161.10-10.131.161.14
# ... (doplnit správné rozsahy)
#
# DHCP Networks (gateway + DNS per VLAN):
# /ip dhcp-server network
# add address=10.131.161.0/29  gateway=10.131.161.1  dns-server=8.8.8.8,1.1.1.1
# add address=10.131.161.8/29  gateway=10.131.161.9  dns-server=8.8.8.8,1.1.1.1
# ...

# ============================================================
# KROK 7: Aktualizovat NAT pravidla
# ============================================================
# Pokud NAT masquerade/src-nat odkazuje na "out-interface=ether1"
# nebo "src-address-list", žádná změna není nutná.
# Pokud odkazuje na konkrétní interface ether2, aktualizujte:
#
# Zkontrolujte stávající NAT:
# /ip firewall nat print where chain=srcnat
#
# Typicky stačí: masquerade na out-interface=ether1 (WAN) — funguje per VLAN automaticky.

# ============================================================
# ROLLBACK POSTUP (pokud něco selže)
# ============================================================
# 1. Zakázat bridge-isp:
#    /interface bridge disable bridge-isp
#
# 2. Obnovit IP adresy na ether2 ze zálohy:
#    /system backup load name=before-phase2
#
# 3. Na CSS326 obnovit zálohu přes SwOS GUI.

# ============================================================
# OVĚŘENÍ:
#   /interface bridge host print
#   -> MAC adresy musí být vidět pod správnými VLAN ID
#
#   /ip dhcp-server lease print
#   -> Leasy přiřazeny přes dhcp-vlan10x servery
#
#   /interface vlan print
#   -> Všechny vlan10x–vlan199 musí být running
# ============================================================
