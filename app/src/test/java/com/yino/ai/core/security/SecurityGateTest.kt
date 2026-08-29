package com.yino.ai.core.security

import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    fun `non-interactive mode denies MEDIUM/HIGH risk (fail-closed)`() = runBlockingTest {
        val gate = SecurityGate().apply { interactive = false }
        val approvedMedium = gate.authorize("test_tool", ActionRisk.MEDIUM, "test reason")
        val approvedHigh = gate.authorize("test_tool", ActionRisk.HIGH, "test reason")
        assertFalse("MEDIUM risk should be denied in non-interactive mode", approvedMedium)
        assertFalse("HIGH risk should be denied in non-interactive mode", approvedHigh)
    }

    @Test
    fun `approval resolves pending deferred`() = runBlockingTest {
        val gate = SecurityGate().apply { interactive = true }
        
        // Start authorization in background
        val authJob = launch {
            gate.authorize("test_tool", ActionRisk.MEDIUM, "test reason")
        }

        // Give time for the pending approval to be emitted
        delay(10)

        // Collect the pending approval
        val pendingList = mutableListOf<SecurityGate.PendingApproval>()
        val collectJob = launch {
            gate.pendingApprovals.collect { pendingList.add(it) }
        }
        delay(10)
        collectJob.cancel()

        assertEquals(1, pendingList.size)
        val pending = pendingList[0]

        // Approve it
        gate.respond(pending.requestId, true)

        // The authorize call should complete with true
        val result = authJob.join()
        assertTrue("Authorization should succeed after approval", result)
    }

    @Test
    fun `deny resolves pending deferred with false`() = runBlockingTest {
        val gate = SecurityGate().apply { interactive = true }
        
        val authJob = launch {
            gate.authorize("test_tool", ActionRisk.HIGH, "test reason")
        }

        delay(10)

        val pendingList = mutableListOf<SecurityGate.PendingApproval>()
        val collectJob = launch {
            gate.pendingApprovals.collect { pendingList.add(it) }
        }
        delay(10)
        collectJob.cancel()

        val pending = pendingList[0]
        gate.respond(pending.requestId, false)

        val result = authJob.join()
        assertFalse("Authorization should fail after denial", result)
    }

    @Test
    fun `timeout denies after 120 seconds`() = runBlockingTest {
        // Use a short timeout for testing by creating a gate with a shorter timeout
        // We test the timeout mechanism by using a custom dispatcher that we can control
        val testDispatcher = UnconfinedTestDispatcher()
        val gate = SecurityGate().apply { interactive = true }
        
        val authJob = launch(testDispatcher) {
            gate.authorize("test_tool", ActionRisk.MEDIUM, "test reason")
        }

        // Advance time past the timeout (120 seconds)
        testDispatcher.advanceTimeBy(121_000)

        val result = authJob.join()
        assertFalse("Authorization should timeout and deny after 120 seconds", result)
    }

    @Test
    fun `multiple pending approvals handled correctly`() = runBlockingTest {
        val gate = SecurityGate().apply { interactive = true }
        
        val authJob1 = launch { gate.authorize("tool1", ActionRisk.MEDIUM, "reason1") }
        val authJob2 = launch { gate.authorize("tool2", ActionRisk.HIGH, "reason2") }

        delay(10)

        val pendingList = mutableListOf<SecurityGate.PendingApproval>()
        val collectJob = launch {
            gate.pendingApprovals.collect { pendingList.add(it) }
        }
        delay(10)
        collectJob.cancel()

        assertEquals(2, pendingList.size)

        // Approve first, deny second
        gate.respond(pendingList[0].requestId, true)
        gate.respond(pendingList[1].requestId, false)

        assertTrue(authJob1.join())
        assertFalse(authJob2.join())
    }
}