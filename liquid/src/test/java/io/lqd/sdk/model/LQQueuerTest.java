package io.lqd.sdk.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Date;

import io.lqd.sdk.LQQueuer;
import io.lqd.sdk.factory.FactoryGirl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


@Config(manifest = "../AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class LQQueuerTest {

    private ArrayList<LQNetworkRequest> queue = new ArrayList<LQNetworkRequest>();
    private LQUser user = new LQUser("123");
    private LQNetworkRequest q1 = new LQNetworkRequest("url", "get", user.toJSON().toString());
    private LQQueuer queuer;

    @Before
    public void setUp() {
        System.setProperty("dexmaker.dexcache", Robolectric.application.getCacheDir().getPath());
        queue.clear();
        queue.add(q1);
        queuer = new LQQueuer(Robolectric.application, "le_token");
    }

//    @Test
//    public void testLoadFail() {
//        Robolectric.application.deleteFile("filename.queue");
//
//        ArrayList<LQNetworkRequest> savedQueue = LQNetworkRequest.loadQueue(Robolectric.application, "filename");
//        assertTrue(savedQueue.size() == 0);
//    }
//
//    @Test
//    public void testSaveSuccess() {
//        LQNetworkRequest.saveQueue(Robolectric.application, queue, "filename");
//
//        ArrayList<LQNetworkRequest> savedQueue = LQNetworkRequest.loadQueue(Robolectric.application, "filename");
//        Log.e("teste", savedQueue.get(0).getJSON());
//        System.out.println(savedQueue.get(0).getJSON());
//        assertEquals(queue.get(0), savedQueue.get(0));
//    }

    /**
     * Test that queuer removes the successful requests and keep the bad requests to retry
     */
    @Test
    public void testRemoveSuccessRequests() {
        LQNetworkRequest r1 = spy(FactoryGirl.createRequest());
        LQNetworkRequest r2 = spy(FactoryGirl.createRequest());
        LQNetworkRequest r3 = spy(FactoryGirl.createRequest());
        LQNetworkRequest r4 = spy(FactoryGirl.createRequest());
        LQNetworkRequest r5 = spy(FactoryGirl.createRequest());


        when(r1.sendRequest("le_token")).thenReturn(new LQNetworkResponse(200));
        when(r2.sendRequest("le_token")).thenReturn(new LQNetworkResponse(200));
        when(r3.sendRequest("le_token")).thenReturn(new LQNetworkResponse(401));
        when(r4.sendRequest("le_token")).thenReturn(new LQNetworkResponse(403));
        when(r5.sendRequest("le_token")).thenReturn(new LQNetworkResponse(422));

        queuer.addToHttpQueue(r1);
        queuer.addToHttpQueue(r2);
        queuer.addToHttpQueue(r3);
        queuer.addToHttpQueue(r4);
        queuer.addToHttpQueue(r5);
        queuer.flush();
        assertEquals(3, queuer.getQueue().size());

        for(LQNetworkRequest req : queuer.getQueue()) {
            assertEquals(1, req.getNumberOfTries());
        }
    }

    @Test
    public void testremoveByNumberOfTries() {
        LQNetworkRequest r1 = spy( FactoryGirl.createRequest());

        when(r1.sendRequest("le_token")).thenReturn(new LQNetworkResponse(401));

        queuer.addToHttpQueue(r1);

        for(int i = 0 ; i < 10 ; ++i) {
            assertEquals(i, queuer.getQueue().get(0).getNumberOfTries());
            queuer.flush();
            assertEquals(1, queuer.getQueue().size());
        }

        queuer.flush();
        assertEquals(0, queuer.getQueue().size());

    }

    @Test
    public void testSendIfLastTryGraterThan30Minutes() {
        LQNetworkRequest r1 = spy( FactoryGirl.createRequest());
        when(r1.sendRequest("le_token")).thenReturn(new LQNetworkResponse(422));

        r1.setLastTry(new Date(new Date().getTime() - (10 * 60 * 1000)));

        queuer.addToHttpQueue(r1);

        for(int i = 0 ; i < 20 ; ++i) {
            assertEquals(0, queuer.getQueue().get(0).getNumberOfTries());
            queuer.flush();
            assertEquals(1, queuer.getQueue().size());
        }
        when(r1.sendRequest("le_token")).thenReturn(new LQNetworkResponse(200));

        r1.setLastTry(new Date(new Date().getTime() - (40 * 60 * 1000)));
        queuer.flush();
        assertEquals(0, queuer.getQueue().size());
    }



}
