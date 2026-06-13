# =============================================================================
# TAMPACO Reznicka – Rate limiting per VLAN (Simple Queues)
#
# Aktualni stav: vsechna Simple Queue pravidla maji disabled=yes
# Tento skript je aktivuje a nastavuje limity.
#
# UPRAVTE LIMITY DLE SMLOUVY S NÁJEMCI:
#   Promena MAX_DEFAULT = výchozí limit pokud není firma níže definována
#
# ISP linka: cca 100 Mbps download, zachovat headroom pro management
#
# DOPAD NA KLIENTY:
#   - Prvni aktivace muze krátkodobě zvýšit latenci pokud nájemce sytí linku
#   - Burst umožní rychlé stahování malých souborů bez omezení
# =============================================================================

# Limity (upravte dle potřeby, format: download/upload v Mbps)
:local limitKAIMAN    "40M/15M"   # VLAN173 - KAIMAN (nejvice zarizeni)
:local limitTAMPACO   "30M/10M"   # VLAN172 - TAMPACO house
:local limitF2F       "20M/10M"   # VLAN174 - Face to Face
:local limitADVOKAT   "20M/10M"   # VLAN175 - Advokatni kancelar
:local limitKADER     "15M/5M"    # VLAN171 - Kadernictvi
:local limitGUEST     "10M/5M"    # VLAN179 - Guest (sdileno mezi vsemi hosty)
:local limitFREE      "20M/10M"   # VLAN176-178 - volne sloty

# Burst nastaveni: kratke vrcholy povoleny (napr. otevirani webovych stranek)
:local burstLimit     "60M/30M"
:local burstThreshold "30M/15M"
:local burstTime      "8/8"

/queue simple {

  # VLAN172 - TAMPACO house
  :local q172 [find target=192.168.172.0/24]
  :if ([:len $q172] > 0) do={
    set $q172 disabled=no max-limit=$limitTAMPACO \
      burst-limit=$burstLimit burst-threshold=$burstThreshold burst-time=$burstTime
    :log info "TAMPACO: Queue VLAN172 aktivovana: $limitTAMPACO"
  }

  # VLAN173 - KAIMAN
  :local q173 [find target=192.168.173.0/24]
  :if ([:len $q173] > 0) do={
    set $q173 disabled=no max-limit=$limitKAIMAN \
      burst-limit=$burstLimit burst-threshold=$burstThreshold burst-time=$burstTime
    :log info "TAMPACO: Queue VLAN173 aktivovana: $limitKAIMAN"
  }

  # VLAN174 - F2F
  :local q174 [find target=192.168.174.0/24]
  :if ([:len $q174] > 0) do={
    set $q174 disabled=no max-limit=$limitF2F \
      burst-limit=$burstLimit burst-threshold=$burstThreshold burst-time=$burstTime
    :log info "TAMPACO: Queue VLAN174 aktivovana: $limitF2F"
  }

  # VLAN175 - Advokatni kancelar
  :local q175 [find target=192.168.175.0/24]
  :if ([:len $q175] > 0) do={
    set $q175 disabled=no max-limit=$limitADVOKAT \
      burst-limit=$burstLimit burst-threshold=$burstThreshold burst-time=$burstTime
    :log info "TAMPACO: Queue VLAN175 aktivovana: $limitADVOKAT"
  }

  # VLAN176 - volny slot
  :local q176 [find target=192.168.176.0/24]
  :if ([:len $q176] > 0) do={
    set $q176 disabled=no max-limit=$limitFREE
    :log info "TAMPACO: Queue VLAN176 aktivovana: $limitFREE"
  }

  # VLAN171 - Kadernictvi
  # (pro VLAN171 neni Simple Queue, pouzijeme add pokud neni)
  :if ([:len [find target=192.168.171.0/24]] = 0) do={
    add name=192_168_171_0 target=192.168.171.0/24 max-limit=$limitKADER \
      comment="VLAN171 Kadernictvi"
    :log info "TAMPACO: Queue VLAN171 pridana: $limitKADER"
  } else={
    set [find target=192.168.171.0/24] disabled=no max-limit=$limitKADER
  }

  # VLAN179 - Guest
  :if ([:len [find target=192.168.179.0/24]] = 0) do={
    add name=192_168_179_0 target=192.168.179.0/24 max-limit=$limitGUEST \
      comment="VLAN179 Guest"
    :log info "TAMPACO: Queue VLAN179 pridana: $limitGUEST"
  } else={
    set [find target=192.168.179.0/24] disabled=no max-limit=$limitGUEST
  }

}

:log info "TAMPACO: tampaco-queues.rsc dokoncen"
:put "Rate limiting aktivovan. Zkontrolujte: /queue simple print"
:put "Celkovy soucet limitu: overit ze neprekracuje ISP linku (100M down)"
