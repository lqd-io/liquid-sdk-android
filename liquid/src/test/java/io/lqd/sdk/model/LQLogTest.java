package io.lqd.sdk.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.lqd.sdk.LQLog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Config(manifest = "../AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class LQLogTest {

    int invalid_below = 0;
    int invalid_above = 10;

    @Test
    public void testBelowinvalidLevelException() {
        try {
            LQLog.setLevel(invalid_below);
            fail("It should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {

        }
    }

    @Test
    public void testAboveinvalidLevelException() {
        try {
            LQLog.setLevel(invalid_above);
            fail("It should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {

        }
    }

    @Test
    public void testCorrectNotThrowingException() {
        try {
            LQLog.setLevel(LQLog.HTTP);
        } catch (IllegalArgumentException e) {
            fail("It shouldn't throw IllegalArgumentException");
        }
    }

    @Test
    public void testSetsLevel() {
        LQLog.setLevel(LQLog.HTTP);
        assertEquals(LQLog.HTTP, LQLog.getLevel());
    }
}
