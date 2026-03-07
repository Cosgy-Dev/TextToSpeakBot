package dev.cosgy.textToSpeak.utils

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import net.dv8tion.jda.api.OnlineStatus
import org.junit.Test

class OtherUtilTest {
    @Test
    fun isBetaVersion_detectsBetaSuffix() {
        assertTrue(OtherUtil.isBetaVersion("1.2.3-beta1"))
        assertFalse(OtherUtil.isBetaVersion("1.2.3"))
        assertTrue(OtherUtil.isBetaVersion("beta1"))
    }

    @Test
    fun compareVersions_returnsMinusOneWhenLeftIsOlder() {
        assertEquals(-1, OtherUtil.compareVersions("1.2.3", "1.2.4"))
    }

    @Test
    fun compareVersions_returnsOneWhenLeftIsNewer() {
        assertEquals(1, OtherUtil.compareVersions("2.0.0", "1.9.9"))
    }

    @Test
    fun compareVersions_treatsMissingPartsAsZero() {
        assertEquals(0, OtherUtil.compareVersions("1.2", "1.2.0"))
    }

    @Test
    fun parseStatus_returnsOnlineForNullOrUnknownValue() {
        assertEquals(OnlineStatus.ONLINE, OtherUtil.parseStatus(null))
        assertEquals(OnlineStatus.UNKNOWN, OtherUtil.parseStatus("not-a-status"))
    }

    @Test
    fun parseStatus_parsesValidStatusValues() {
        assertEquals(OnlineStatus.IDLE, OtherUtil.parseStatus("idle"))
        assertEquals(OnlineStatus.DO_NOT_DISTURB, OtherUtil.parseStatus("dnd"))
    }
}
