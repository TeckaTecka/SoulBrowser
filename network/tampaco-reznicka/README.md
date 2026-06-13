# TAMPACO Řeznicka – Síťová konfigurace

MikroTik RB951Ui-2HnD + CRS326-24G-2S+, RouterOS 7.x  
Izolace nájemců v budově pomocí VLAN 171–179.

## Aktuální topologie

```
ISP (100 Mbps) → ether1 (10.0.0.120/24, GW 10.0.0.138)
                       │
                 RB951Ui-2HnD
                       │ ether5 (802.1Q trunk)
                 CRS326-24G-2S+
                       │
     Port1  Port2  Port3  Port4  Port5  Port9
     VLAN171 172   173    174    175    179
     Kader  TAMP  KAIM   F2F   Advo   Guest
```

## VLANy a nájemci

| VLAN | Podsíť             | Nájemce              | Switch port |
|------|--------------------|----------------------|-------------|
| 171  | 192.168.171.0/24   | Kadeřnictví          | Port 1      |
| 172  | 192.168.172.0/24   | TAMPACO house        | Port 2      |
| 173  | 192.168.173.0/24   | KAIMAN               | Port 3      |
| 174  | 192.168.174.0/24   | Face to Face (F2F)   | Port 4      |
| 175  | 192.168.175.0/24   | Advokátní kancelář   | Port 5      |
| 176  | 192.168.176.0/24   | Volný slot           | Port 6      |
| 177  | 192.168.177.0/24   | Volný slot           | Port 7      |
| 178  | 192.168.178.0/24   | Volný slot           | Port 8      |
| 179  | 192.168.179.0/24   | GUEST                | Port 9      |

Management: 192.168.170.0/24 (bridge-trunk), switch = 192.168.170.200

## Skripty k importu

Importovat na router: `Tools → Terminal → /import file-name=<soubor>.rsc`

### Povinné (bezpečnostní opravy)

| Skript | Popis | Priorita |
|--------|-------|----------|
| `tampaco-firewall-fix.rsc` | Oprava inter-VLAN firewallu, izolace GUESTu | **KRITICKÉ** |
| `tampaco-guest-dhcp-fix.rsc` | DNS pro GUEST → 8.8.8.8 (nutné po firewall-fix) | Povinné |
| `tampaco-snmp-security.rsc` | SNMP community, zakaz write-access | Doporučené |

### Volitelné

| Skript | Popis |
|--------|-------|
| `tampaco-queues.rsc` | Zapnutí rate limitingu per VLAN – upravit limity dle smlouvy |
| `tampaco-wireguard.rsc` | Nahrazení PPTP za WireGuard VPN |

## Pořadí importu

```
1. tampaco-firewall-fix.rsc     # nejprve firewall
2. tampaco-guest-dhcp-fix.rsc   # pak DHCP pro GUEST
3. tampaco-snmp-security.rsc    # SNMP
4. tampaco-queues.rsc           # rate limiting (upravit limity)
5. tampaco-wireguard.rsc        # VPN (až po dohodě s uživatelem PPTP)
```

## Ověření po importu

```routeros
# 1. Inter-VLAN blokace – z PC v VLAN172 NESMÍ fungovat:
ping 192.168.173.1        # odpoved = CHYBA v konfiguraci

# 2. Internet z VLAN172 MUSI fungovat:
ping 8.8.8.8              # musi odpovedat

# 3. GUEST blokace managementu – z PC v VLAN179 NESMÍ:
ping 192.168.170.200      # switch – musi byt blokovan
ping 192.168.170.1        # router – musi byt blokovan

# 4. GUEST internet MUSI fungovat:
ping 8.8.8.8              # musi odpovedat
```

## Plánovaný upgrade na RB4011

Při výměně RB951 → RB4011 je nutné:

1. **Přejít na bridge VLAN filtering** (místo VLAN subinterfaces na ether5):
   - Vytvořit bridge s `vlan-filtering=yes`
   - Přidat ether5 jako tagged port
   - VLAN subinterfaces definovat na bridge, ne na fyzickém portu
   - CSS326 trunk port zůstává beze změny

2. **Přejít na Gbit trunk** – RB4011 má 10× Gbit, ether5 100Mbps bottleneck odpadne

3. **Firewall a queue pravidla** přenést beze změny (jsou na L3, HW nezávislé)

4. **DHCP leases** exportovat a importovat – klienti dostanou stejné IP

## CSS326 – doporučené manuální změny (webGUI)

- `http://192.168.170.200` → System → Password → změnit z `L95GA`
- System → SNMP → Community → změnit z `public` na stejný řetězec jako na routeru
