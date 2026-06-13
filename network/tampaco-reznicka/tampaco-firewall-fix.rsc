# =============================================================================
# TAMPACO Reznicka – Opravy firewallu
# Router: MikroTik RB951Ui-2HnD, RouterOS 7.x
#
# POUZITI:
#   /import file-name=tampaco-firewall-fix.rsc
#
# CO TENTO SKRIPT DELA:
#   1. Opravuje bug "drop - izolace lan" (bylo fasttrack, ma byt drop)
#   2. Pridava explicitni inter-VLAN DROP (nájemci se nevidí navzájem)
#   3. Blokuje GUEST (VLAN179) od management sítě 192.168.170.0/24
#   4. Omezuje DNS pro GUEST (hosté nemůžou používat DNS routeru)
#
# DOPAD NA KLIENTY:
#   - Žádný – klienti mají stále přístup na internet
#   - Firmy se nadále nevidí (pravidlo nyní explicitní místo implicit-drop)
#   - Hosté (VLAN179) jsou izolováni od správy sítě
# =============================================================================

# --- 1. Oprava bugu: "drop - izolace lan" byl fasttrack, ne drop -----------
# Najdeme pravidlo podle komentáře a opravíme akci i parametry
/ip firewall filter {
  :local ruleId [find comment="drop - izolace lan"]
  :if ([:len $ruleId] > 0) do={
    set $ruleId \
      action=drop \
      chain=forward \
      in-interface-list=LAN \
      out-interface-list=LAN \
      connection-state=""
    :log info "TAMPACO: Opraven bug - drop izolace lan"
  } else={
    :log warning "TAMPACO: Pravidlo 'drop - izolace lan' nenalezeno - preskoceno"
  }
}

# --- 2. Explicitni inter-VLAN DROP ----------------------------------------
# Blokuje provoz mezi libovolnými LAN VLANy (vlan171-179, bridge-trunk).
# Pridáváme PŘED final DROP pravidlo.
# Klienti mají stále přístup na internet (out-interface-list=WAN je povoleno výše).
/ip firewall filter {
  # Preskocit pokud jiz existuje
  :if ([:len [find comment="SECURITY: inter-VLAN drop"]] = 0) do={
    :local finalDrop [find action=drop chain=forward comment=""]
    :if ([:len $finalDrop] > 0) do={
      add chain=forward \
        action=drop \
        in-interface-list=LAN \
        out-interface-list=LAN \
        comment="SECURITY: inter-VLAN drop" \
        place-before=$finalDrop
    } else={
      # Záloha: přidáme na konec chain forward
      add chain=forward \
        action=drop \
        in-interface-list=LAN \
        out-interface-list=LAN \
        comment="SECURITY: inter-VLAN drop"
    }
    :log info "TAMPACO: Pridano inter-VLAN DROP pravidlo"
  } else={
    :log info "TAMPACO: inter-VLAN DROP pravidlo uz existuje"
  }
}

# --- 3. Blokovat GUEST (VLAN179) → management 192.168.170.0/24 -------------
# Hosté nesmí přistupovat na switch (192.168.170.200) ani na management IP routeru.
/ip firewall filter {
  :if ([:len [find comment="SECURITY: GUEST blok management forward"]] = 0) do={
    add chain=forward \
      action=drop \
      in-interface=vlan179 \
      dst-address=192.168.170.0/24 \
      comment="SECURITY: GUEST blok management forward" \
      place-before=[find comment="SECURITY: inter-VLAN drop"]
    :log info "TAMPACO: Pridano GUEST blok management (forward)"
  }
  :if ([:len [find comment="SECURITY: GUEST blok management input"]] = 0) do={
    add chain=input \
      action=drop \
      in-interface=vlan179 \
      dst-address=192.168.170.0/24 \
      comment="SECURITY: GUEST blok management input"
    :log info "TAMPACO: Pridano GUEST blok management (input)"
  }
  # Blokovat přistup na router sám (WinBox, SSH, webfig) z GUEST
  :if ([:len [find comment="SECURITY: GUEST blok router services"]] = 0) do={
    add chain=input \
      action=drop \
      in-interface=vlan179 \
      protocol=tcp \
      dst-port=8291,22,443 \
      comment="SECURITY: GUEST blok router services"
    :log info "TAMPACO: Pridano GUEST blok router services"
  }
}

# --- 4. Blokovat DNS pro GUEST (VLAN179) -----------------------------------
# Hosté nesmí používat DNS resolver routeru.
# Jejich DHCP by měl poskytovat veřejné DNS (8.8.8.8).
# Viz tampaco-queues.rsc – DHCP network pro vlan179 dostane dns-server=8.8.8.8
/ip firewall filter {
  :if ([:len [find comment="SECURITY: GUEST blok DNS input"]] = 0) do={
    add chain=input \
      action=drop \
      in-interface=vlan179 \
      protocol=udp \
      dst-port=53 \
      comment="SECURITY: GUEST blok DNS input"
    add chain=input \
      action=drop \
      in-interface=vlan179 \
      protocol=tcp \
      dst-port=53 \
      comment="SECURITY: GUEST blok DNS input tcp"
    :log info "TAMPACO: Pridano GUEST blok DNS"
  }
}

# --- Hotovo ----------------------------------------------------------------
:log info "TAMPACO: tampaco-firewall-fix.rsc dokoncen"
:put "Hotovo. Zkontrolujte /ip firewall filter print"
