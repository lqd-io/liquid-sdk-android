package io.lqd.sdk.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


@Config(manifest = "../AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class LQModelTest {

    private String dollarkey = "le_invalid_$key";
    private String dotkey = "le.invalid_key";
    private String nullcharkey = "le" + '\0' + "key";

    private String validkey = "le_valid_key";

    private Object[] validAttributes = { null, "string", Integer.valueOf(1), Double.valueOf(2.2), Long.valueOf(3), Boolean.FALSE, new Date()};
    private Object[] invalidAttributes = { new Object(), new ArrayList<String>(), new String[2], new LQUser("123") };

    @Test
    public void testValidAttributes() {
        for(Object o : validAttributes) {
            assertTrue("Should be a valid attribute", LQModel.validValue(o, false));

            try {
                assertTrue("Should be a valid attribute", LQModel.validValue(o, true));
            } catch (IllegalArgumentException e) {
                fail("Should not throw exception because is a valid attribute");
            }
        }
    }

    @Test
    public void testInvalidAttributes() {
        for(Object o : invalidAttributes) {
            assertFalse("Should be a invalid attribute", LQModel.validValue(o, false));

            try {
                assertFalse("Should be a invalid attribute", LQModel.validValue(o, true));
                fail("Should throw exception because is a invalid attribute");
            } catch (IllegalArgumentException e) {

            }
        }
    }

    @Test
    public void testSanitizeAttributesBecauseOfValues() {
        HashMap<String, Object> attributes = new HashMap<String, Object>();
        for(Object o : validAttributes) {
            if(o == null) {
                attributes.put("null", o);
            } else {
                attributes.put(o.hashCode()+"", o);
            }
        }
        for(Object o : invalidAttributes) {
            attributes.put(o.toString(), o);
        }
        attributes = LQModel.sanitizeAttributes(attributes, false);
        assertTrue("Number of attributes should be " + validAttributes.length +", but its " + attributes.size(), validAttributes.length == attributes.size());
    }

    @Test
    public void testSanitizeAttributesBecauseOfKeys() {
        HashMap<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(dollarkey, validAttributes[0]);
        attributes.put(validkey, validAttributes[2]);
        attributes.put(dotkey, validAttributes[3]);
        attributes.put(nullcharkey, validAttributes[4]);

        attributes = LQModel.sanitizeAttributes(attributes, false);
        assertTrue("Number of attributes should be 1, but its " + attributes.size(), 1 == attributes.size());
    }

    @Test
    public void testKeyDollarInvalid() {
        boolean valid = LQModel.validKey(dollarkey, false);

        assertFalse("Should be an invalid key", valid);

        try {
            valid = LQModel.validKey(dotkey, true);
            fail("Should throw exception because is a invalid key");
        } catch (IllegalArgumentException e) {
            assertFalse(valid);
        }
    }

    @Test
    public void testKeyDotInvalid() {
        boolean valid = LQModel.validKey(dotkey, false);

        assertFalse("Should be an invalid key", valid);

        try {
            valid = LQModel.validKey(dotkey, true);
            fail("Should throw exception because is a invalid key");
        } catch (IllegalArgumentException e) {
            assertFalse(valid);
        }
    }

    @Test
    public void testKeyNullCharInvalid() {
        boolean valid = LQModel.validKey(nullcharkey, false);

        assertFalse("Should be an invalid key", valid);

        try {
            valid = LQModel.validKey(nullcharkey, true);
            fail("Should throw exception because is a invalid key");
        } catch (IllegalArgumentException e) {
            assertFalse(valid);
        }
    }

    @Test
    public void testValidKey() {
        boolean valid = LQModel.validKey(validkey, false);

        assertTrue("Should be a valid key", valid);

        try {
            valid = LQModel.validKey(validkey, true);
            assertTrue("Should be a valid key", valid);
        } catch (IllegalArgumentException e) {
            fail("Should not throw exception because is a valid key");
        }
    }

    // public static String newIdentifier();

    @Test
    public void testNewIdentifierLength() {
        assertEquals(47, LQModel.newIdentifier().length());
    }
}
