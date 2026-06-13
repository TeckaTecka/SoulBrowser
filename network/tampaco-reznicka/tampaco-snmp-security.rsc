# =============================================================================
# TAMPACO Reznicka – Bezpecnost SNMP
#
# Aktualni stav:
#   - SNMP community = "public" (universalne znama, nebezpecna)
#   - write-access=yes na SNMP community routeru
#   - SNMP dostupny ze vsech IP adres
#
# Tento skript:
#   - Zmeni community string na silnejsi
#   - Omezi SNMP na management subnet (RUBICON + 192.168.170.0/24)
#   - Zakaze write-access
#
# POZNAMKA CSS326:
#   SNMP community "public" na switchi CSS326 zmenit rucne pres webGUI:
#   http://192.168.170.200 → System → SNMP → Community = <novy retezec>
#   (nevyzaduje fyzicky pristup, jen webovy prohlizec na management PC)
# =============================================================================

# Upravte tento retezec! Pouzijte min. 16 znaku, velka+mala+cisla
:local newCommunity "TAMPACO-monitor-2024x"

/snmp community {
  # Zmenime default community
  set [find default=yes] \
    name=$newCommunity \
    addresses=194.212.26.172/32,194.108.250.74/32,193.165.237.0/24,192.168.170.0/24 \
    write-access=no
  :log info "TAMPACO: SNMP community zmenena a write-access zakazana"
}

/snmp {
  # SNMP ponechame zapnuty (pouzit pro monitoring)
  set enabled=yes
}

:put "SNMP community zmenena na: $newCommunity"
:put "SNMP write-access: zakazan"
:put "SNMP adresy: jen RUBICON + 192.168.170.0/24"
:put ""
:put "NEZAPOMENOUT: Zmenit SNMP community na CSS326 switchi!"
:put "  -> http://192.168.170.200 -> System -> SNMP"
