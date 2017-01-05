package org.dandelion.radiot.accepttest.testables;

import android.content.Context;
import junit.framework.Assert;
import org.dandelion.radiot.helpers.SyncValueHolder;
import org.dandelion.radiot.podcasts.core.PodcastAction;
import org.dandelion.radiot.podcasts.core.PodcastItem;

public class FakePodcastPlayer implements PodcastAction {
    private SyncValueHolder<String> podcastToPlay = new SyncValueHolder<String>();

	public void perform(Context context, PodcastItem podcast) {
        podcastToPlay.setValue(podcast.audioUri);
	}

	public void assertIsPlaying(String url) throws Exception {
		Assert.assertEquals(url, podcastToPlay.getValue());
	}
}