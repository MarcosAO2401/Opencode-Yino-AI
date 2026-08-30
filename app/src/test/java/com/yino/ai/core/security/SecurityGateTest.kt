package com.yino.ai.core.security

import com.yino.ai.core.tools.ActionRisk
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Test

class SecurityGateTest {

    @Test
    fun `LOW risk actions are auto-approved`() = runBlockingTest {
        val gate = SecurityGate()
        val approved = gate.authorize("test_tool", ActionRisk.LOW, "test reason")
        assertTrue("LOW risk should be auto-approved", approved)
    }

    @Test
    fun `MEDIUM risk actions require interactive approval`() = runBlockingTest {
        val gate = SecurityGate().apply { interactive = true }
        val approved = gate.authorize("test_tool", ActionRisk.MEDIUM, "test reason")
        assertFalse("MEDIUM risk should not be auto-approved without UI response", approved)
    }

    @Test
    fun `HIGH risk actions require interactive approval`() = runBlockingTest {
        val gate = SecurityGate().apply { interactive = true }
        val approved = gate.authorize("test_tool", ActionRisk.HIGH, "test reason")
        assertFalse("HIGH risk should not be auto-approved without UI response", approved)
    }

    @Test
    fun `non-interactive mode denies MEDIUM and HIGH risk (fail-closed)`() = runBlockingTest {
        val gate = SecurityGate().apply { interactive = false }
        val approvedMedium = gate.authorize("test_tool", ActionRisk.MEDIUM, "test reason")
        val approvedHigh = gate.authorize("test_tool", ActionRisk.HIGH, "test reason")
        assertFalse("MEDIUM risk should be denied in non-interactive mode", approvedMedium)
        assertFalse("HIGH risk should be denied in non-interactive mode", approvedHigh)
    }

    @Test
    fun `approval resolves pending deferred`() = runBlockingTest {
        val gate = SecurityGate().apply { interactive = true }

        // Collect pending approvals (must be active before authorize emits)
        val pendingList = mutableListOf<SecurityGate.PendingApproval>()
        val collectJob = launch {
            gate.pendingApprovals.collect { pendingList.add(it) }
        }

        // Start authorization in background
        val authJob = async {
            gate.authorize("test_tool", ActionRisk.MEDIUM, "test reason")
        }

        // Give time for the pending approval to be emitted
        delay(10)

        assertEquals(1, pendingList.size)
        collectJob.cancel()

        val pending = pendingList[0]

        // Approve it
        gate.respond(pending.requestId, true)

        // The authorize call should complete with true
        val result = authJob.await()
        assertTrue("Authorization should succeed after approval", result)
    }

    @Test
    fun `deny resolves pending deferred with false`() = runBlockingTest {
        val gate = SecurityGate().apply { interactive = true }

        val pendingList = mutableListOf<SecurityGate.PendingApproval>()
        val collectJob = launch {
            gate.pendingApprovals.collect { pendingList.add(it) }
        }

        val authJob = async {
            gate.authorize("test_tool", ActionRisk.HIGH, "test reason")
        }

        delay(10)

        collectJob.cancel()

        val pending = pendingList[0]
        gate.respond(pending.requestId, false)

        val result = authJob.await()
        assertFalse("Authorization should fail after denial", result)
    }

    @Test
    fun `timeout denies after 120 seconds`() = runBlockingTest {
        val gate = SecurityGate().apply { interactive = true }

        // Run authorize on the test scope's dispatcher so we can control virtual time
        val authJob = async {
            gate.authorize("test_tool", ActionRisk.MEDIUM, "test reason")
        }

        // Advance virtual time past the 120s timeout
        testScheduler.advanceTimeBy(121_000)

        val result = authJob.await()
        assertFalse("Authorization should timeout and deny after 120 seconds", result)
    }

    @Test
    fun `multiple pending approvals handled correctly`() = runBlockingTest {
        val gate = SecurityGate().apply { interactive = true }

        val pendingList = mutableListOf<SecurityGate.PendingApproval>()
        val collectJob = launch {
            gate.pendingApprovals.collect { pendingList.add(it) }
        }

        val authJob1 = async { gate.authorize("tool1", ActionRisk.MEDIUM, "reason1") }
        val authJob2 = async { gate.authorize("tool2", ActionRisk.HIGH, "reason2") }

        delay(10)

        collectJob.cancel()

        assertEquals(2, pendingList.size)

        // Approve first, deny second
        gate.respond(pendingList[0].requestId, true)
        gate.respond(pendingList[1].requestId, false)

        assertTrue(authJob1.await())
        assertFalse(authJob2.await())
    }
}