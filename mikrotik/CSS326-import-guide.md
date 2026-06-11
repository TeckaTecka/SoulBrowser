# Průvodce importem zálohy CSS326 — Fáze 1: Port Isolation

## Co soubory mění

| Soubor | Switch | IP | Web přístup |
|--------|--------|----|-------------|
| `CSS326-MASTER-phase1-isolation.swb` | Ostravia 40-1 | 10.131.161.251 | 194.108.250.74:8081 |
| `CSS326-SLAVE-phase1-isolation.swb` | Ostravia 40-2 | 10.131.161.250 | 194.108.250.74:8082 |

### Konkrétní změny v záloze

**Port isolation (fwd.b):**
- P1–P23: každý port smí komunikovat výhradně přes P24 (uplink)
- P24 (uplink): beze změny — může dosáhnout na všechny porty
- SFP1, SFP2: beze změny

**DHCP Snooping (sys.b.dtrp):**
- Pouze P24 je nastavena jako "Důvěryhodný port" (trusted)
- P1–P23 jsou nedůvěryhodné → switch zahodí DHCP odpovědi z přístupových portů

**Co se NEMĚNÍ:**
- Jméno switche (identity)
- IP adresa
- Heslo
- RSTP konfigurace
- Port speeds, duplex, flow control
- SNMP (community "public" zůstává — viz poznámka níže)

---

## Postup importu

### Prerekvizity
- Přístup na web rozhraní SwOS
- Zálohovat aktuální konfiguraci (`Systém → Uložit zálohu`) — pro případ vrácení zpět
- Doporučeno: mít připravený konzolový přístup nebo fyzický přístup pro případ výpadku management přístupu

### Krok 1 — Záloha aktuálního stavu (doporučeno)
1. Otevřít SwOS (`:8081` nebo `:8082`)
2. Záložka **Systém** → tlačítko **Uložit zálohu**
3. Stáhnout a uložit soubor na disk

### Krok 2 — Import nové konfigurace
1. Záložka **Systém** → sekce **Zálohování**
2. Kliknout **Vybrat soubor** → vybrat příslušný `.swb` soubor
3. Kliknout **Obnovit konfiguraci**
4. Switch se restartuje (~15 sekund)

### Krok 3 — Ověření po importu
Po restartu switche:

**a) Ověřit port isolation v GUI:**
- Záložka **Porty** → zkontrolovat sloupec **Izolace přístavů**
- P1–P23: zaškrtnuto ✓
- P24: nezaškrtnuto ✓

**b) Ověřit DHCP snooping:**
- Záložka **Systém** → sekce **DHCP & PPPoE Snooping**
- Pouze P24 zaškrtnuta jako **Důvěryhodný port** ✓
- P1–P23: nezaškrtnuto ✓

**c) Ověřit konektivitu nájemníků:**
- Z libovolného klientského PC: `ping 8.8.8.8` → OK (internet funguje)
- Cross-tenant ping: `ping 10.131.161.10` z IP 10.131.161.2 → Request timeout ✓

### Postup při vrácení zpět (rollback)
1. Záložka **Systém** → **Vybrat soubor** → vybrat originální zálohu
2. **Obnovit konfiguraci** → switch se restartuje

---

## Pořadí importu

1. Nejprve **MASTER** (Ostravia 40-1, `:8081`)
2. Pak **SLAVE** (Ostravia 40-2, `:8082`)

Mezi jednotlivými importy: počkejte ~30 sekund na restart a ověřte konektivitu.

---

## Poznámky

### SNMP community
Záloha zachovává SNMP community `public`. **Doporučeno: změnit na silný řetězec** ihned po importu:
- SwOS → záložka **SNMP** → pole **Community** → zadat nový řetězec → **Apply**

### Dopad na unmanaged switche pod CSS326
Port isolation platí jen pro porty přímo na CSS326. Zařízení zapojená za unmanaged switchem pod jedním portem CSS326 stále sdílejí L2 segment mezi sebou navzájem — ale nemohou se dostat k ostatním nájemníkům díky port isolation na CSS326.

### Fáze 2 — VLAN konfigurace
Port isolation (tato záloha) řeší L2 izolaci na úrovni CSS326. Pro plnou VLAN segmentaci (nutnou pro per-tenant QoS a granulární monitoring) je třeba dokončit Fázi 2 — viz `DESIGN.pdf` sekce 4.
