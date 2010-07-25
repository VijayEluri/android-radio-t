package org.dandelion.radiot.accepttest;

import org.dandelion.radiot.PodcastList.IModel;
import org.dandelion.radiot.PodcastListActivity;
import org.dandelion.radiot.helpers.ApplicationDriver;
import org.dandelion.radiot.helpers.PodcastListAcceptanceTestCase;
import org.dandelion.radiot.helpers.TestModel;

import android.content.pm.ActivityInfo;

public class InterruptPodcastLoading extends PodcastListAcceptanceTestCase {

	private ApplicationDriver appDriver;
	private TestModel model;

	public void testCancelRssLoadingWhenPressingBack() throws Exception {
		appDriver.visitMainShowPage();
		appDriver.goBack();
		testPresenter.assertTaskIsCancelled();
		appDriver.assertOnHomeScreen();
	}

	public void testChangeOrientationContinuesBackgroundLoading()
			throws Exception {
		PodcastListActivity activity = appDriver.visitMainShowPage();
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		appDriver.waitSomeTime();
		allowPodcastRetrievalToFinish();
		testPresenter.assertStartedBackgroundTasksCount(1);
		appDriver.assertShowingPodcastList();
	}

	public void testDestroyingActivityWhileLoading() throws Exception {
		PodcastListActivity activity = appDriver.visitMainShowPage();
		activity.finish();
		appDriver.assertOnHomeScreen();
		testPresenter.assertTaskIsCancelled();
	}

	protected void allowPodcastRetrievalToFinish() {
		model.returnsEmptyPodcastList();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		appDriver = createApplicationDriver();
	}

	protected IModel createTestModel(String url) {
		model = new TestModel();
		return model;
	}
}
