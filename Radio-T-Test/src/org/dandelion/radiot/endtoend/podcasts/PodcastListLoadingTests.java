package org.dandelion.radiot.endtoend.podcasts;

import android.content.Context;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import org.dandelion.radiot.TestPodcastListLoader;
import org.dandelion.radiot.endtoend.podcasts.helpers.PodcastListRunner;
import org.dandelion.radiot.endtoend.podcasts.helpers.TestRssServer;
import org.dandelion.radiot.podcasts.ui.PodcastListActivity;

import static org.dandelion.radiot.endtoend.podcasts.helpers.RssFeedBuilder.buildFeed;

public class PodcastListLoadingTests
        extends ActivityInstrumentationTestCase2<PodcastListActivity> {
    private TestRssServer backend;
    private PodcastListRunner app;

    public PodcastListLoadingTests() {
        super(PodcastListActivity.class);
    }

    public void testRetrievePodcastListFromRssServerAndDisplayIt() throws Exception {
        String feed = buildFeed()
                .item("<title>Радио-Т 140</title>" +
                        "<pubDate>Sun, 13 Jun 2010 01:37:22 +0000</pubDate>" +
                        "<itunes:summary>Lorem ipsum dolor sit amet</itunes:summary>")
                .item("<title>Радио-Т 141</title>" +
                        "<pubDate>Sun, 19 Jun 2010 01:37:22 +0000</pubDate>" +
                        "<itunes:summary>consectetur adipiscing elit</itunes:summary>")
                .done();

        app = startApplication();
        backend.hasReceivedRequestForRss();
        backend.respondSuccessWith(feed);

        app.showsPodcastItem("#140", "13.06.2010", "Lorem ipsum dolor sit amet");
        app.showsPodcastItem("#141", "19.06.2010", "consectetur adipiscing elit");
    }

    public void testPressingBackButtonWhileLoadingClosesActivity() throws Exception {
        app = startApplication();
        backend.hasReceivedRequestForRss();

        app.pressBack();
        app.wasClosed();
    }

    public void testRetrievesThumbnailsFromTheServer() throws Exception {
        final String thumbnailUrl = "/images/radio-t/rt307.jpg";
        String feed = buildFeed()
                .item("<title>Радио-Т 140</title>" +
                        "<pubDate>Sun, 13 Jun 2010 01:37:22 +0000</pubDate>" +
                        "<description>&lt;p&gt;&lt;img src=\"" +
                        thumbnailUrl +
                        "\" alt=\"\" /&gt;&lt;/p&gt;</description>")
                .done();

        app = startApplication();
        backend.hasReceivedRequestForRss();
        backend.respondSuccessWith(feed);

        backend.hasReceivedRequestForUrl(thumbnailUrl);
    }

    public void testRefreshingPodcastListRetrievesItFromServerAgain() throws Exception {
        String initialFeed = buildFeed().done();
        String updatedFeed = buildFeed()
                .item("<title>Радио-Т 140</title>" +
                        "<pubDate>Sun, 13 Jun 2010 01:37:22 +0000</pubDate>" +
                        "<itunes:summary>Lorem ipsum dolor sit amet</itunes:summary>")
                .done();

        backend.respondSuccessWith(initialFeed);

        app = startApplication();
        app.showsEmptyList();

        app.refreshPodcasts();
        backend.hasReceivedRequestForRss();
        backend.respondSuccessWith(updatedFeed);

        app.showsPodcastItem("#140", "13.06.2010", "Lorem ipsum dolor sit amet");
    }

    public void testShowsErrorIfUnableToRetrieveList() throws Exception {
        backend.respondNotFoundError();
        app = startApplication();
        app.showsErrorMessage();
    }


    @Override
    public void setUp() throws Exception {
        super.setUp();
        backend = new TestRssServer();
        TestPodcastListLoader.resetCache();
    }

    @Override
    public void tearDown() throws Exception {
        backend.stop();
        super.tearDown();
    }

    private PodcastListRunner startApplication() {
        setActivityIntent(startupIntent());
        return new PodcastListRunner(getInstrumentation(), getActivity());
    }

    private Intent startupIntent() {
        Context context = getInstrumentation().getContext();
        return PodcastListActivity.createIntent(context, "Test Activity", "test-show");
    }

}
