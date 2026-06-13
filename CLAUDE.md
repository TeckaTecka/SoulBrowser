# CLAUDE.md

Tento soubor poskytuje Claude Code (claude.ai/code) kontext při práci s tímto repozitářem.

## Přehled projektu

**Název:** Network Admin Tools
**Popis:** Profesionální mobilní aplikace pro správce sítě — sjednocuje LAN scanner (Fing-style), WiFi analyzer, RouterOS/MikroTik management, síťovou diagnostiku (ping, traceroute, DNS, BGP, WHOIS), speed test, SNMP monitoring, CIDR/VLSM kalkulátor a Terminal Server Manager do jedné aplikace.
**Stav:** V aktivním vývoji — Fáze 1 MVP

## Tech stack

- **Jazyk:** Kotlin
- **UI:** Android XML Views (žádný Compose)
- **Min SDK / Target SDK:** minSdk 26, targetSdk 35
- **Build system:** Gradle KTS
- **Architektura:** MVVM + Repository pattern; ViewModel drží stav skenů a monitoringu; Kotlin Coroutines + Flow pro async síťové operace

## Build & běžné příkazy

```bash
# Sestavení debug APK
./gradlew assembleDebug

# Instalace na připojené zařízení
./gradlew installDebug

# Spuštění unit testů
./gradlew test

# Spuštění instrumentation testů (potřeba zařízení/emulátor)
./gradlew connectedAndroidTest

# Lint
./gradlew lint

# Vyčištění build outputs
./gradlew clean
```

## Struktura projektu

```
app/
├── src/main/java/com/teckatecka/netadmin/
│   ├── ui/
│   │   ├── main/            # MainActivity, Navigation
│   │   ├── calculator/      # CIDR, VLSM kalkulátor
│   │   ├── scanner/         # LAN scanner, Port scanner
│   │   ├── wifi/            # WiFi Analyzer
│   │   ├── diagnostics/     # Ping, Traceroute, DNS, BGP, WHOIS
│   │   ├── routeros/        # MikroTik management
│   │   ├── speedtest/       # Speed test, iPerf3
│   │   ├── monitoring/      # Host monitoring, SNMP
│   │   └── terminal/        # SSH terminál, RDP/Terminal Server
│   ├── data/
│   │   ├── db/              # Room database — zařízení, history
│   │   ├── model/           # Data třídy (Host, Device, ScanResult...)
│   │   └── repository/      # Repository vrstva
│   ├── network/
│   │   ├── routeros/        # RouterOS API klient (TCP socket port 8728)
│   │   ├── snmp/            # SNMP4J wrapper
│   │   ├── ssh/             # SSH klient (JSch)
│   │   └── scanner/         # ARP/ICMP scanner, port scanner
│   └── utils/               # Pomocné třídy (IpUtils, OuiDatabase...)
└── src/main/res/
    ├── layout/              # XML layouty
    ├── values/              # strings, colors, styles, dimens
    ├── values-cs/           # Česká lokalizace
    └── drawable/
```

## Konvence pro psaní kódu

### Kotlin
- **Kotlin verze:** 1.9+
- **Pojmenování:** `PascalCase` třídy, `camelCase` metody/proměnné, `UPPER_SNAKE_CASE` konstanty.
- **Null-safety:** využívat Kotlin null-safety (`?.`, `?:`, `!!` jen pokud jsi si jistý). Vyhýbat se `!!`.
- **Coroutines:** `viewModelScope.launch` v ViewModel, `withContext(Dispatchers.IO)` pro síťové operace.
- **Flow:** pro živá data (grafy, monitoring, live scan výsledky).
- **Délka řádku:** max **200 znaků**.
- **Zarovnání do sloupců:** u skupin podobných deklarací nebo volání zarovnávat pomocí mezer pro čitelnost:
  ```kotlin
  val address    = prefs.getString("address", "")
  val port       = prefs.getInt("port", 8728)
  val username   = prefs.getString("username", "admin")
  val password   = prefs.getString("password", "")
  ```
  Používej s citem — jen u bloků 2+ souvisejících přiřazení stejné struktury.

### XML
- **ID konvence:** `snake_case` (např. `@+id/button_scan`, `@+id/text_ip_address`).
- **String resources:** žádné hardcoded stringy — vždy `@string/...` resp. `getString(R.string.X)`.
- **Dimens & barvy:** přes `@dimen/...` a `@color/...`.
- **Layouty:** `ConstraintLayout` u nových obrazovek.

### Logging
- `android.util.Log` s konstantním `TAG` per třída: `private val TAG = "MyClass"`
- Žádné `println`. Žádné citlivé údaje v logu (hesla, klíče).

## Klíčové závislosti

| Závislost | Verze | Účel |
|---|---|---|
| `SNMP4J` | 3.x | SNMP v1/v2c/v3 |
| `JSch` / `sshd-core` | latest | SSH terminál |
| `OkHttp` | 4.x | HTTP (BGP API, WHOIS, speed test) |
| `Room` | 2.x | SQLite databáze |
| `MPAndroidChart` | 3.x | Grafy (traffic, ping history) |
| `Firebase Messaging` | latest | Push notifikace alertů |

## Pravidla pro Claude

1. **Drž existující styl.** Konzistence > teorie.
2. **Žádné velké refaktoringy bez vyžádání.** „Oprav bug X" = oprav jen X.
3. **Žádné nové dependencies bez ptání.** Zdůvodni proč před přidáním do `build.gradle.kts`.
4. **Kotlin, ne Java.** Tento projekt je čistý Kotlin.
5. **XML Views, ne Compose.** Žádné `@Composable` funkce.
6. **Backward compatibility.** `minSdk 26` — používej `Build.VERSION.SDK_INT >= ...` pro novější API.
7. **Komentáře v češtině jsou OK**, identifikátory v angličtině.
8. **Před commit-ready řešením:** spusť alespoň `./gradlew assembleDebug` a `./gradlew test`.
9. **Když si nejsi jistý, ptej se.**
10. **XML layout varianty.** Před úpravou jakéhokoli layoutu v `res/layout/` zkontroluj varianty (`layout-land/`, `layout-sw600dp/` atd.) a aplikuj změnu konzistentně ve všech.
11. **String resources — jazykové varianty.** Přidání nového stringu = přidej do `values/strings.xml` (default EN) a `values-cs/strings.xml` (čeština). Do ostatních jazyků přidej nebo upozorni na chybějící překlad.

## Síťová specifika — důležité poznámky

- **ICMP ping** na Androidu nevyžaduje root od API 26 přes `InetAddress.isReachable()`, ale je omezeno na TCP echo. Pro skutečný ICMP použij NDK nebo `/proc/net/` parsing.
- **WiFi scanning** vyžaduje `ACCESS_FINE_LOCATION` + `CHANGE_WIFI_STATE`. Od Android 10+ je throttling na 4 scany/2 min.
- **RouterOS API** komunikuje binárním protokolem přes TCP port 8728 (nebo 8729 pro SSL). Implementuj vlastní parser sentence/word protokolu.
- **Port scanning** přes Java `Socket.connect()` — Android povoluje raw TCP connects.
- **SNMP4J** funguje na Androidu bez modifikací; použij `DefaultUdpTransportMapping`.
- **WinRM** pro Terminal Server manager — HTTP/SOAP na portu 5985 (HTTP) nebo 5986 (HTTPS).

## Testování

- **Unit testy:** `app/src/test/` — JUnit 5, MockK
- **Instrumentation testy:** `app/src/androidTest/` — Espresso
- Síťové testy mockovat přes MockWebServer (OkHttp) nebo vlastní mock TCP server.

## Známá úskalí

- Android omezuje UDP broadcast na některých ROM verzích — ARP scan přes UDP může selhat; fallback na TCP connect ping.
- WiFi API vrací throttled výsledky od Android 9+ — neočekávej real-time scan, cache výsledky.
- RouterOS API session má timeout — implementuj keepalive (posílej `/system/identity/print` každých 30s).

## Co nedělat

- Neměnit `applicationId`, `versionCode`, `versionName` v `build.gradle.kts` bez explicitního zadání.
- Necommitovat `local.properties`, `*.keystore`, ani API klíče nebo hesla.
- Negenerovat README, CHANGELOG ani jiné docs, pokud o ně nepožádám.
- **Nespouštět auto-formátovací nástroje** — rozbijí ruční zarovnání do sloupců.
- Nepoužívat IDE „Reformat Code" na celé soubory.
