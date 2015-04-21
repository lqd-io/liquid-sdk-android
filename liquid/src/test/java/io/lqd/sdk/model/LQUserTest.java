package io.lqd.sdk.model;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import io.lqd.sdk.factory.FactoryGirl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;


@Config(manifest = "../AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class LQUserTest {

    private LQUser user;
    private HashMap<String, Object> attrs;

    @Before
    public void setUp() {
        user = FactoryGirl.createUser();
        attrs = new HashMap<String, Object>();

        attrs.put("teste1", 1);
        attrs.put("teste2", 1);
        attrs.put("teste3", 1);
    }

    // User with unique_id only
    @Test
    public void testToJSON() {
        JSONObject json = user.toJSON();

        assertTrue(json.has("unique_id"));
    }

    // User with custom attrs
    @Test
    public void testToJSONAttrs() {
        user = new LQUser("123", attrs);
        JSONObject json = user.toJSON();

        assertTrue(json.has("unique_id"));
        assertTrue(json.has("teste1"));
        assertTrue(json.has("teste2"));
        assertTrue(json.has("teste3"));
    }

    @Test
    public void testLoadFail() {
        Assert.assertNotNull(Robolectric.application);
        LQUser prevUser = new LQUser("le_id");
        prevUser.save(Robolectric.application, "le_user");
        Robolectric.application.deleteFile("le_user.user");
        LQUser savedUser = LQUser.load(Robolectric.application,"le_user");

        assertNotSame(savedUser.getIdentifier(), prevUser.getIdentifier());
    }

    @Test
    public void testSaveSuccess() {
        user.save(Robolectric.application, "le_user");
        LQUser savedUser = LQUser.load(Robolectric.application, "le_user");

        assertTrue(user.equals(savedUser));

    }

    @Test
    public void attributesNotNull() {
        user.setAttributes(null);
        assertNotNull(user.getAttributes());
    }

    @Test
    public void attributesNotNullLoad() {
        user.setAttributes(null);
        user.save(Robolectric.application, "le_user");
        LQUser loaded = user.load(Robolectric.application, "le_user");
        assertNotNull(loaded.getAttributes());
    }

}
