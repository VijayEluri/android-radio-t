package org.dandelion.radiot.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;

import org.dandelion.radiot.PodcastItem;
import org.dandelion.radiot.PodcastList;
import org.dandelion.radiot.PodcastListActivity;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.UiThreadTest;
import android.view.View;
import android.widget.TextView;

public class PodcastListActivityTestCase extends
		ActivityUnitTestCase<PodcastListActivity> {

	// This date is 19.06.2010
	private static final Date SAMPLE_DATE = new Date(110, 05, 19);

	private PodcastListActivity activity;

	public PodcastListActivityTestCase() {
		super(PodcastListActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		new ArrayList<PodcastItem>();
		PodcastListActivity.setDefaultPresenter(PodcastList.nullPresenter());
		activity = startActivity(new Intent(), null, null);
	}

	@Override
	protected void tearDown() throws Exception {
		PodcastListActivity.resetDefaultPresenter();
		super.tearDown();
	}

	@UiThreadTest
	public void testDisplayPodcastItem() throws Exception {
		ArrayList<PodcastItem> items = new ArrayList<PodcastItem>();
		items.add(new PodcastItem(121, SAMPLE_DATE, "Show notes", ""));

		activity.updatePodcasts(items);
		View listItem = activity.getListAdapter().getView(0, null, null);

		assertTextEquals(listItem, "podcast_item_view_number", "#121");
		assertTextEquals(listItem, "podcast_item_view_date", "19.06.2010");
		assertTextEquals(listItem, "podcast_item_view_shownotes", "Show notes");
	}

	public void assertTextEquals(View listItem, String elementName, String value)
			throws Exception {
		int id = getIdByName(elementName);
		TextView textView = (TextView) listItem.findViewById(id);
		assertEquals(value, textView.getText());
	}

	public int getIdByName(String elementName) throws NoSuchFieldException,
			IllegalAccessException {
		Class cls = org.dandelion.radiot.R.id.class;
		Field field = cls.getDeclaredField(elementName);
		int i = field.getInt(cls);
		return i;
	}
}
