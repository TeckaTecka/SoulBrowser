package com.teckatecka.netadmin

import com.teckatecka.netadmin.utils.CidrCalculator
import com.teckatecka.netadmin.utils.VlsmSubnet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CidrCalculatorTest {

    @Test
    fun `cidr /24 calculates correct network`() {
        val r = CidrCalculator.calculate("192.168.1.0/24")
        assertEquals("192.168.1.0",   r.networkAddr)
        assertEquals("192.168.1.255", r.broadcastAddr)
        assertEquals("192.168.1.1",   r.firstHost)
        assertEquals("192.168.1.254", r.lastHost)
        assertEquals(254L,            r.hostCount)
        assertEquals("255.255.255.0", r.subnetMask)
        assertEquals("0.0.0.255",     r.wildcardMask)
    }

    @Test
    fun `cidr /30 calculates correct network`() {
        val r = CidrCalculator.calculate("10.0.0.0/30")
        assertEquals("10.0.0.0",     r.networkAddr)
        assertEquals("10.0.0.3",     r.broadcastAddr)
        assertEquals("10.0.0.1",     r.firstHost)
        assertEquals("10.0.0.2",     r.lastHost)
        assertEquals(2L,             r.hostCount)
    }

    @Test
    fun `cidr host input gets masked to network`() {
        val r = CidrCalculator.calculate("192.168.1.50/24")
        assertEquals("192.168.1.0",   r.networkAddr)   // host bits vynulovány
        assertEquals("192.168.1.255", r.broadcastAddr)
    }

    @Test
    fun `cidr invalid input throws`() {
        assertThrows(Exception::class.java) {
            CidrCalculator.calculate("999.168.1.0/24")
        }
        assertThrows(Exception::class.java) {
            CidrCalculator.calculate("192.168.1.0/33")
        }
    }

    @Test
    fun `vlsm allocates subnets correctly`() {
        val results = CidrCalculator.calculateVlsm(
            "192.168.1.0/24",
            listOf(
                VlsmSubnet("Office", 50),
                VlsmSubnet("Servers", 14),
                VlsmSubnet("Mgmt", 6)
            )
        )
        assertEquals(3, results.size)
        // Největší podsíť první
        assertEquals("Office", results[0].name)
        // Každá podsíť musí mít dostatek hostů
        assert(results[0].usableHosts >= 50)
        assert(results[1].usableHosts >= 14)
        assert(results[2].usableHosts >= 6)
    }

    @Test
    fun `vlsm insufficient space throws`() {
        assertThrows(IllegalStateException::class.java) {
            CidrCalculator.calculateVlsm(
                "192.168.1.0/30",   // jen 2 hosty
                listOf(VlsmSubnet("Big", 200))
            )
        }
    }
}
