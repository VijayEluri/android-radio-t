package org.dandelion.radiot.test;

import org.dandelion.radiot.PodcastItem;
import org.dandelion.radiot.PodcastListActivity;
import org.dandelion.radiot.RssFeedModel;

import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.test.ActivityUnitTestCase;
import android.test.UiThreadTest;
import android.view.View;
import android.widget.ListView;

public class PodcastRssDisplayAcceptanceTest extends
		ActivityUnitTestCase<PodcastListActivity> {
	private static final String RSS_FILENAME = "podcast_rss.xml";

	private PodcastListActivity activity;

	public PodcastRssDisplayAcceptanceTest() {
		super(PodcastListActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		AssetManager assets = getInstrumentation().getTargetContext()
				.getAssets();

		PodcastListActivity.useModel(new RssFeedModel(
				new RssFeedModel.AssetFeedSource(assets, RSS_FILENAME)));

		activity = startActivity(new Intent(), null, null);
	}

	@UiThreadTest
	public void testRetrieveAndDisplayPodcast() throws Exception {
		ListView list = activity.getListView();

		assertEquals(17, list.getCount());
	}

	@UiThreadTest
	public void testStartPlayingPodcastOnClick() throws Exception {
		PodcastItem item = (PodcastItem) activity.getListAdapter().getItem(0);

		clickOnListItem(0);

		assertStartedPlaying(item.getAudioUri());
	}

	private void assertStartedPlaying(Uri audioUri) {
		Intent intent = getStartedActivityIntent();

		assertNotNull(intent);
		assertEquals("audio/mpeg", intent.getType());
		assertEquals(audioUri, intent.getData());
		assertEquals(Intent.ACTION_VIEW, intent.getAction());
	}

	private void clickOnListItem(int index) {
		View view = activity.getListAdapter().getView(index, null, null);
		activity.getListView().performItemClick(view, index, 0);
	}
}