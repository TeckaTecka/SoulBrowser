package com.teckatecka.netadmin.network.snmp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.snmp4j.CommunityTarget
import org.snmp4j.PDU
import org.snmp4j.Snmp
import org.snmp4j.event.ResponseEvent
import org.snmp4j.mp.SnmpConstants
import org.snmp4j.smi.OID
import org.snmp4j.smi.OctetString
import org.snmp4j.smi.UdpAddress
import org.snmp4j.smi.VariableBinding
import org.snmp4j.transport.DefaultUdpTransportMapping

data class SnmpVarBind(val oid: String, val value: String, val type: String)

class SnmpClient {

    private fun buildTarget(host: String, port: Int, community: String, version: Int): CommunityTarget<UdpAddress> {
        val target = CommunityTarget<UdpAddress>()
        target.address   = UdpAddress("$host/$port")
        target.community = OctetString(community)
        target.version   = version
        target.retries   = 1
        target.timeout   = 3000
        return target
    }

    suspend fun get(host: String, port: Int = 161, community: String = "public", oids: List<String>): List<SnmpVarBind> =
        withContext(Dispatchers.IO) {
            val transport = DefaultUdpTransportMapping()
            val snmp = Snmp(transport)
            transport.listen()

            try {
                val pdu = PDU()
                pdu.type = PDU.GET
                oids.forEach { pdu.add(VariableBinding(OID(it))) }

                val target = buildTarget(host, port, community, SnmpConstants.version2c)
                val event: ResponseEvent<*>? = snmp.get(pdu, target)
                val response = event?.response ?: return@withContext emptyList()

                response.variableBindings.map { vb ->
                    SnmpVarBind(vb.oid.toString(), vb.variable.toString(), vb.variable.syntax.toString())
                }
            } finally {
                snmp.close()
            }
        }

    suspend fun walk(host: String, port: Int = 161, community: String = "public", baseOid: String): List<SnmpVarBind> =
        withContext(Dispatchers.IO) {
            val transport = DefaultUdpTransportMapping()
            val snmp = Snmp(transport)
            transport.listen()

            val results = mutableListOf<SnmpVarBind>()
            val target  = buildTarget(host, port, community, SnmpConstants.version2c)
            var currentOid = OID(baseOid)

            try {
                while (true) {
                    val pdu = PDU()
                    pdu.type = PDU.GETNEXT
                    pdu.add(VariableBinding(currentOid))

                    val event: ResponseEvent<*>? = snmp.getNext(pdu, target)
                    val response = event?.response ?: break
                    if (response.errorStatus != PDU.noError) break

                    val vb = response.get(0)
                    if (!vb.oid.startsWith(OID(baseOid))) break
                    if (vb.variable.syntax == org.snmp4j.smi.SMIConstants.EXCEPTION_END_OF_MIB_VIEW) break

                    results.add(SnmpVarBind(vb.oid.toString(), vb.variable.toString(), vb.variable.syntax.toString()))
                    currentOid = vb.oid
                }
            } finally {
                snmp.close()
            }

            results
        }
}
