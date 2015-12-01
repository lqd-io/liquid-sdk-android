package io.lqd.sdk;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;


@Config(manifest = "../AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class LiquidToolsTest {


    // public static String tenCharEpoch();

    @Test
    public void testShortEpoch() {
        long epoch = 3600; // 1970/1/1 01:00:00
        assertEquals("0000003600", LiquidTools.tenCharEpoch(epoch));
    }

    @Test
    public void testLongEpoch() {
        long epoch = 1432142122;
        assertEquals("1432142122", LiquidTools.tenCharEpoch(epoch));
    }


}
