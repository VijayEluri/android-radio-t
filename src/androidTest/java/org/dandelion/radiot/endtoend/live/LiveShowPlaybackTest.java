package org.dandelion.radiot.endtoend.live;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import org.dandelion.radiot.accepttest.testables.LiveNotificationManagerSpy;
import org.dandelion.radiot.endtoend.live.helpers.LiveShowRunner;
import org.dandelion.radiot.endtoend.live.helpers.LiveShowServer;
import org.dandelion.radiot.live.LiveShowApp;
import org.dandelion.radiot.live.MediaPlayerStream;
import org.dandelion.radiot.live.core.AudioStream;
import org.dandelion.radiot.live.service.TimeoutReceiver;
import org.dandelion.radiot.live.ui.LiveNotificationManager;

public class LiveShowPlaybackTest extends LiveShowActivityTestCase {
    private LiveShowRunner runner;
    private LiveShowServer backend;

    public void testStartStopPlayback() throws Exception {
        backend.activateTranslation();

        runner.startTranslation();
        runner.showsTranslationInProgress();

        runner.stopTranslation();
        runner.showsTranslationStopped();

        runner.startTranslation();
        runner.showsTranslationInProgress();
    }

    public void testTryToReconnectContinuouslyInWaitingMode() throws Exception {
        backend.suppressTranslation();
        runner.startTranslation();
        runner.showsWaiting();

        backend.activateTranslation();
        signalWaitTimeout();
        runner.showsTranslationInProgress();
    }

    public void testStopWaiting() throws Exception {
        backend.suppressTranslation();

        runner.startTranslation();
        runner.showsWaiting();

        runner.stopTranslation();
        runner.showsTranslationStopped();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        LiveNotificationManagerSpy notificationManager = new LiveNotificationManagerSpy();
        setupTestingApp(notificationManager);

        runner = new LiveShowRunner(getInstrumentation(), getActivity(), notificationManager);
        backend = new LiveShowServer(getInstrumentation().getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        runner.finish();
        backend.stop();
        super.tearDown();
    }

    private void setupTestingApp(final LiveNotificationManagerSpy notificationManager) {
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                LiveShowApp.setTestingInstance(new LiveShowApp() {
                    @Override
                    public AudioStream createAudioStream() {
                        return new MediaPlayerStream(LiveShowServer.SHOW_URL);
                    }

                    @Override
                    public LiveNotificationManager createNotificationManager(Context context) {
                        return notificationManager;
                    }
                });
            }
        });
    }

    private void signalWaitTimeout() {
        Intent intent = new Intent(TimeoutReceiver.BROADCAST);
        cancelScheduledAlarm(intent);
        fireSimulatedAlarm(intent);
    }

    private void cancelScheduledAlarm(Intent intent) {
        AlarmManager manager = (AlarmManager) context().getSystemService(Context.ALARM_SERVICE);
        manager.cancel(PendingIntent.getBroadcast(context(), 0, intent, 0));
    }

    private void fireSimulatedAlarm(Intent intent) {
        context().sendBroadcast(intent);
    }

    private Context context() {
        return getInstrumentation().getTargetContext().getApplicationContext();
    }

}
