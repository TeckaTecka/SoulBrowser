# Síťová segmentace ISP — Ostravia 40
## Návrh implementace: Fáze 1 + Fáze 2

**Verze:** 1.0  
**Datum:** 2026-06-11  
**Síť:** Budova Ostravia 40 — cca 32 nájemců, CCR2116 + 2× CSS326  

---

## 1. Aktuální stav sítě

### 1.1 Topologie

```
Internet (GTS/Lumen)  194.108.250.73/30
           │
       ether1 (WAN)
           │
   CCR2116-12G-4S+    RouterOS 7.23.1
       10.131.161.0/24 gateway
           │
       ether2 ──────────────────────────────── (jeden velký L2 segment)
           │
   Eagle switch (.254, RouterOS)
           │
   CSS326 MASTER "Ostravia 40-1"   10.131.161.251
   │  P1:  místnost 10         │  P13: místnost 130
   │  P2:  místnost 42         │  P14: místnost 131
   │  P3:  místnost 43         │  P15: místnost 132
   │  P4:  místnost 45         │  P16: místnost 133/1
   │  P5:  místnost 49         │  P17: místnost 133/2
   │  P6:  místnost 51         │  P18: místnost 201
   │  P7:  místnost 52         │  P19: Bistro
   │  P8:  místnost 89         │  P20: Výměník
   │  P9:  místnost 91         │  P21: Malá zasedačka
   │  P10: místnost 93         │  P22: Velký sál
   │  P11: místnost 94         │  P23: LinkSwitch2 (→ SLAVE)
   │  P12: místnost 97         │  P24: UPLINK Eagle
           │
           P23 → P24
           │
   CSS326 SLAVE  "Ostravia 40-2"   10.131.161.250
   │  P1:  místnost 229        │  P10: místnost 430/1
   │  P2:  místnost 233        │  P11: místnost 430/1
   │  P3:  místnost 235        │  P12: místnost 504
   │  P4:  místnost 301        │  P13–P23: nepojmenované
   │  P5:  místnost 373        │  P24: LinkSwitch1 (→ MASTER)
   │  P6:  místnost 402
   │  P7:  místnost 403
   │  P8:  místnost 404
   │  P9:  místnost 416

   Speciální klienti (VIP — NEDOTÝKAT SE):
   ether10, ether11, ether12 na CCR2116 → vlastní /29 podsítě
```

### 1.2 Stávající izolační mechanismus

- **DHCP MAC-lock:** Router přiděluje IP z konkrétní /29 podsítě podle MAC adresy
- **Blackhole pool:** Neznámá MAC → IP z 10.131.190.x bez gateway (nemůže surfovat)
- **Dalkia:** Vlastní /29 na 10.131.162.x

### 1.3 Kritické problémy (potvrzeno analýzou zálohy)

| # | Problém | Závažnost | Kde |
|---|---------|-----------|-----|
| 1 | **Port isolation NENÍ zapnuta** na CSS326 MASTER ani SLAVE — všechny porty se navzájem vidí na L2 | KRITICKÁ | CSS326 |
| 2 | **Žádné VLANy** — `vlan.b:[]` prázdné na obou CSS326 | KRITICKÁ | CSS326 |
| 3 | **Chybí forward DROP** pravidla na CCR2116 — cross-tenant provoz explicitně ACCEPT | KRITICKÁ | CCR2116 |
| 4 | **L2 bypass routeru** — tenant A může ARP-ovat na IP tenanta B a komunikovat bez průchodu firewallem | KRITICKÁ | L2 |
| 5 | DHCP Snooping — všechny porty trusted → rogue DHCP server možný | VYSOKÁ | CSS326 |
| 6 | SNMP community `public` na obou CSS326 | STŘEDNÍ | CSS326 |
| 7 | IPSec šifrování `3DES` (Sweet32, RFC 8996) | STŘEDNÍ | CCR2116 |
| 8 | PPTP VPN aktivní (prolomitelné šifrování) | STŘEDNÍ | CCR2116 |
| 9 | SSH `forwarding-enabled=remote` — tunelování mimo firewall | STŘEDNÍ | CCR2116 |
| 10 | DNS open resolver pro nájemníky | NÍZKÁ | CCR2116 |

---

## 2. Cílová architektura

```
Internet
    │
CCR2116  ether1 (WAN)
    │
    bridge-isp (VLAN filtering=yes)
    │   ├── vlan101 10.131.161.1/29    ← Tenant místnost 10
    │   ├── vlan102 10.131.161.9/29    ← Tenant místnost 42
    │   ├── vlan103 10.131.161.17/29   ← Tenant místnost 43
    │   │   ... (vlan101–vlan132)
    │   └── vlan199 10.131.162.1/29   ← Dalkia
    │
    ether2 (tagged trunk — všechny VLANy)
    │
Eagle switch (VLAN trunk)
    │
CSS326 MASTER (trunk P24; access porty P1–P22 s PVID)
    ├── P1  PVID=101  → místnost 10
    ├── P2  PVID=102  → místnost 42
    ...
    ├── P23 trunk → CSS326 SLAVE
    └── P24 trunk → Eagle / CCR2116

CSS326 SLAVE (trunk P24; access porty P1–P23 s PVID)
    ├── P1  PVID=123  → místnost 229
    ...
    └── P24 trunk → MASTER CSS326
```

**Výsledek:** Každý nájemce je v samostatné broadcast doméně (VLAN).
Klient nevidí žádný VLAN tag — CSS326 přidá/odstraní tag transparentně.
Žádná rekonfigurace na straně nájemce není nutná.

---

## 3. Fáze 1 — Okamžité opravy (bez výpadku, ~15 min)

### 3.1 CSS326 MASTER a SLAVE — Port Isolation

**Co:** V záložce **Ports** → sloupec **Isolation** zaškrtnout porty P1–P23.
Port P24 (UPLINK/LinkSwitch) nechat **nezaškrtnutý**.

**Proč:** Po zapnutí port isolation smí každý port komunikovat **výhradně přes P24** (uplink).
Tenant A (P1) nemůže posílat L2 rámce přímo na port tenanta B (P2).
Veškerý provoz jde přes Eagle → CCR2116, kde ho firewall může zachytit.

**Dopad:** Okamžitý efekt, nulový výpadek pro klienty (jejich provoz na internet funguje stejně).

```
Postup (SwOS GUI přes DNAT 8081/8082):
1. System → Ports → Isolation: ✓ P1–P23, □ P24
2. Použít vše
3. Opakovat na SLAVE (DNAT port 8082)
```

### 3.2 CSS326 MASTER a SLAVE — DHCP Snooping oprava

**Co:** System → DHCP & PPPoE Snooping → Důvěryhodné přístavy:
odškrtnout P1–P23, ponechat zaškrtnutý **pouze P24**.

**Proč:** Aktuálně jsou všechny porty trusted → jakýkoliv nájemce může spustit
vlastní DHCP server a přidělovat IP adresy ostatním.

### 3.3 CSS326 MASTER a SLAVE — SNMP

**Co:** System → SNMP → Community string: změnit z `public` na bezpečný řetězec.

### 3.4 CCR2116 — Forward DROP pravidla

Soubor: `phase1-immediate-fixes.rsc`

**Co se přidává:**

```routeros
# DROP cross-tenant provoz (10.131.161.x ↔ 10.131.161.x)
/ip firewall filter
add chain=forward src-address=10.131.161.0/24 dst-address=10.131.161.0/24 \
    action=drop comment="SECURITY: blokovat cross-tenant provoz" place-before=0

# DROP cross-tenant Dalkia ↔ tenant
add chain=forward src-address=10.131.162.0/24 dst-address=10.131.161.0/24 \
    action=drop comment="SECURITY: Dalkia nesmí vidět nájemce" place-before=1
add chain=forward src-address=10.131.161.0/24 dst-address=10.131.162.0/24 \
    action=drop comment="SECURITY: nájemce nesmí vidět Dalkia" place-before=2

# DROP blackhole pool
add chain=forward src-address=10.131.190.0/24 \
    action=drop comment="SECURITY: blackhole pool" place-before=3
add chain=forward dst-address=10.131.190.0/24 \
    action=drop comment="SECURITY: blackhole pool" place-before=4
```

**Proč:** CCR2116 aktuálně v forward chain explicitně ACCEPTUJE veškerý provoz
z/do 10.131.161.0/24. Toto pravidlo musí být **vloženo před** stávající ACCEPT pravidla.
Bez port isolation by toto pravidlo samo nestačilo (L2 bypass),
ale jako druhá vrstva obrany (defense-in-depth) je nutné.

### 3.5 CCR2116 — Bezpečnostní opravy

```routeros
# IPSec: 3DES → AES-256-GCM
/ip ipsec proposal set [find] enc-algorithms=aes-256-gcm

# Zakázat PPTP server
/interface pptp-server server set enabled=no

# Zakázat SSH remote forwarding
/ip ssh set forwarding-enabled=no

# DNS: zakázat přístup z nájemníků (ponechat jen management)
/ip dns set allow-remote-requests=no
```

---

## 4. Fáze 2 — VLAN segmentace (maintenance window ~30 min)

### 4.1 VLAN tabulka

| VLAN ID | Místnost/nájemce | CSS326 port | Podsíť |
|---------|-----------------|-------------|--------|
| 101 | Místnost 10 | MASTER P1 | 10.131.161.x/29 |
| 102 | Místnost 42 | MASTER P2 | 10.131.161.x/29 |
| 103 | Místnost 43 | MASTER P3 | 10.131.161.x/29 |
| 104 | Místnost 45 | MASTER P4 | 10.131.161.x/29 |
| 105 | Místnost 49 | MASTER P5 | 10.131.161.x/29 |
| 106 | Místnost 51 | MASTER P6 | 10.131.161.x/29 |
| 107 | Místnost 52 | MASTER P7 | 10.131.161.x/29 |
| 108 | Místnost 89 | MASTER P8 | 10.131.161.x/29 |
| 109 | Místnost 91 | MASTER P9 | 10.131.161.x/29 |
| 110 | Místnost 93 | MASTER P10 | 10.131.161.x/29 |
| 111 | Místnost 94 | MASTER P11 | 10.131.161.x/29 |
| 112 | Místnost 97 | MASTER P12 | 10.131.161.x/29 |
| 113 | Místnost 130 | MASTER P13 | 10.131.161.x/29 |
| 114 | Místnost 131 | MASTER P14 | 10.131.161.x/29 |
| 115 | Místnost 132 | MASTER P15 | 10.131.161.x/29 |
| 116 | Místnost 133/1 | MASTER P16 | 10.131.161.x/29 |
| 117 | Místnost 133/2 | MASTER P17 | 10.131.161.x/29 |
| 118 | Místnost 201 | MASTER P18 | 10.131.161.x/29 |
| 119 | Bistro | MASTER P19 | 10.131.161.x/29 |
| 120 | Výměník | MASTER P20 | 10.131.161.x/29 |
| 121 | Malá zasedačka | MASTER P21 | 10.131.161.x/29 |
| 122 | Velký sál | MASTER P22 | 10.131.161.x/29 |
| 123 | Místnost 229 | SLAVE P1 | 10.131.161.x/29 |
| 124 | Místnost 233 | SLAVE P2 | 10.131.161.x/29 |
| 125 | Místnost 235 | SLAVE P3 | 10.131.161.x/29 |
| 126 | Místnost 301 | SLAVE P4 | 10.131.161.x/29 |
| 127 | Místnost 373 | SLAVE P5 | 10.131.161.x/29 |
| 128 | Místnost 402 | SLAVE P6 | 10.131.161.x/29 |
| 129 | Místnost 403 | SLAVE P7 | 10.131.161.x/29 |
| 130 | Místnost 404 | SLAVE P8 | 10.131.161.x/29 |
| 131 | Místnost 416 | SLAVE P9 | 10.131.161.x/29 |
| 132 | Místnost 430 | SLAVE P10/P11 | 10.131.161.x/29 |
| 133 | Místnost 504 | SLAVE P12 | 10.131.161.x/29 |
| 134–144 | Nepojmenované | SLAVE P13–P23 | rezerva |
| 199 | Dalkia | (vlastní port/trunk) | 10.131.162.x/29 |

*Poznámka: Podsítě (x/29) doplnit podle aktuální DHCP tabulky před implementací.*

### 4.2 CCR2116 konfigurace

Soubor: `phase2-vlan-ccr2116.rsc`

```routeros
# 1. Vytvořit bridge s VLAN filtering
/interface bridge
add name=bridge-isp vlan-filtering=yes frame-types=admit-only-vlan-tagged \
    comment="ISP tenant bridge s VLAN izolací"

# 2. ether2 přidat do bridge jako tagged trunk
/interface bridge port
add bridge=bridge-isp interface=ether2 frame-types=admit-only-vlan-tagged

# 3. Vytvořit VLAN interfacy na bridge
/interface vlan
add interface=bridge-isp name=vlan101 vlan-id=101 comment="Místnost 10"
add interface=bridge-isp name=vlan102 vlan-id=102 comment="Místnost 42"
... (vlan103 – vlan144, vlan199)

# 4. Přiřadit IP adresy na VLAN interfacy (přesunout z ether2)
/ip address
remove [find interface=ether2]
add address=10.131.161.X/29 interface=vlan101 comment="Tenant místnost 10"
...

# 5. DHCP servery přemigrovat na VLAN interfacy
/ip dhcp-server
set [find name=ISP] interface=vlan101  # nebo vytvořit nové per-VLAN
```

### 4.3 CSS326 konfigurace (SwOS VLAN)

Soubor: `phase2-vlan-crs326-template.rsc`

CSS326 SwOS VLAN konfigurace se provádí **přes GUI** (SwOS nemá plnohodnotné CLI
pro VLAN import), ale logika je:

**MASTER CSS326:**

| Port | Typ | PVID | Tagged VLANy |
|------|-----|------|--------------|
| P1–P22 | Access | 101–122 | — |
| P23 | Trunk | 1 | 101–144, 199 |
| P24 | Trunk | 1 | 101–144, 199 |

**SLAVE CSS326:**

| Port | Typ | PVID | Tagged VLANy |
|------|-----|------|--------------|
| P1–P23 | Access | 123–144 | — |
| P24 | Trunk | 1 | 101–144, 199 |

### 4.4 Postup migrace (maintenance window)

```
Příprava (bez výpadku):
1. Vytvořit bridge-isp a VLAN interfacy na CCR2116 (bez přesunu IP)
2. Testovat VLAN konfiguraci na nepoužívaném portu CSS326
3. Naplánovat maintenance window (doporučeno: víkend nebo noc)

Maintenance window (cca 30 min):
4.  [CCR2116] Přidat ether2 do bridge-isp
5.  [CSS326 MASTER] Nastavit PVID na access portech, trunk na P23+P24
6.  [CSS326 SLAVE]  Nastavit PVID na access portech, trunk na P24
7.  [Eagle switch]  Ověřit VLAN trunk konfiguraci
8.  [CCR2116] Přesunout IP adresy z ether2 na VLAN interfacy
9.  [CCR2116] Restartovat DHCP servery
10. Ověřit konektivitu (viz sekce 6)
11. Pokud OK: hotovo. Pokud problém: rollback (obnovit zálohu)
```

---

## 5. Dopad na klienty

| Scénář | Fáze 1 (port isolation) | Fáze 2 (VLANy) |
|--------|------------------------|-----------------|
| Internet funguje | ✓ beze změny | ✓ beze změny |
| Klientská rekonfigurace | žádná | žádná |
| Cross-tenant komunikace | **BLOKOVÁNA** | **BLOKOVÁNA** |
| Klient za unmanaged switchem (jiný nájemce) | L3 blokován, L2 leak | **Plná izolace** |
| DHCP lease | beze změny | beze změny (stejné IP) |
| VIP klienti ether10/11/12 | nedotčeni | nedotčeni |

---

## 6. Ověření po implementaci

### Fáze 1

```bash
# Test cross-tenant blokování (z PC nájemce A):
ping <IP_nájemce_B>          # musí selhat (DROP)
traceroute <IP_nájemce_B>    # musí selhat na CCR2116

# Test internet stále funguje:
ping 8.8.8.8                  # musí projít
curl -I https://google.com    # musí vrátit HTTP 301

# Test VIP klientů (z administrátorského PC):
ping 10.131.16x.x             # ether10/11/12 klienti — musí fungovat
```

### Fáze 2

```routeros
# Na CCR2116:
/ip dhcp-server lease print   # leasy přiřazeny přes VLAN servery
/interface bridge host print  # MAC adresy izolované po VLANech
/ip route print               # trasy na VLAN interfacy

# Test VLAN tagování:
/tool packet-sniffer interface=bridge-isp protocol=ip duration=10
# Pakety mají VLAN tag odpovídající nájemci
```

---

## 7. Rollback postup

```routeros
# CCR2116: smazat přidané DROP pravidla
/ip firewall filter remove [find comment~"SECURITY:"]

# CCR2116: zakázat bridge-isp (Fáze 2 rollback)
/interface bridge disable bridge-isp
/ip address remove [find interface~"vlan"]
# Obnovit původní IP na ether2 ze zálohy

# CSS326: obnovit zálohu přes SwOS → Systém → Obnovit zálohu
# (zálohy jsou: CSS326_2.18.swb a CSS326_2.18_1.swb)
```

---

## 8. Shrnutí priorit

| Priorita | Akce | Čas | Výpadek |
|----------|------|-----|---------|
| **1. URGENTNÍ** | Port isolation na CSS326 MASTER+SLAVE | 10 min | Ne |
| **2. URGENTNÍ** | DROP forward pravidla na CCR2116 | 10 min | Ne |
| **3. VYSOKÁ** | DHCP Snooping oprava (jen P24 trusted) | 5 min | Ne |
| **4. VYSOKÁ** | SNMP community změna | 5 min | Ne |
| **5. STŘEDNÍ** | IPSec 3DES → AES-256 | 5 min | Krátký reset VPN |
| **6. STŘEDNÍ** | PPTP zakázat, SSH forwarding zakázat | 5 min | Ne |
| **7. PLÁNOVANÁ** | Fáze 2: VLAN migrace | 30 min | Maintenance window |
