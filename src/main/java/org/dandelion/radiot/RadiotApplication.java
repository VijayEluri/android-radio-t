package org.dandelion.radiot;

import android.app.Application;
import android.os.Handler;
import org.dandelion.radiot.common.Scheduler;
import org.dandelion.radiot.http.DataMonitor;
import org.dandelion.radiot.http.DisabledProvider;
import org.dandelion.radiot.http.HttpDataMonitor;
import org.dandelion.radiot.http.Provider;
import org.dandelion.radiot.live.chat.Message;
import org.dandelion.radiot.live.topics.CurrentTopic;
import org.dandelion.radiot.live.ui.ChatTranslationFragment;
import org.dandelion.radiot.live.ui.CurrentTopicFragment;
import org.dandelion.radiot.podcasts.main.PodcastsApp;
import org.dandelion.radiot.util.ProgrammerError;

import java.util.List;

public class RadiotApplication extends Application {
    private static final String CHAT_URL = "http://chat.radio-t.com";
    private static final String TOPIC_TRACKER_BASE_URL = "http://radiot.tindandelion.com:8080";
    // private static final String TOPIC_TRACKER_BASE_URL = "http://192.168.0.11:8080";
    // private static final String CHAT_URL = "http://10.0.1.2:4567";

    public static DataMonitor createChatTranslation(String chatUrl, Scheduler scheduler) {
//      Provider<List<Message>> client = HttpChatClient.create(chatUrl);
        Provider<List<Message>> client = new DisabledProvider<>();
        return new HttpDataMonitor<>(client, scheduler);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PodcastsApp.initialize(this);
        setupChatTranslation();
        setupTopicTracker();
    }

    private void setupTopicTracker() {
        final int updateDelayMillis = 60000;
        CurrentTopicFragment.trackerFactory = new DataMonitor.Factory<CurrentTopic>() {
            @Override
            public DataMonitor<CurrentTopic> create() {
//                Provider<CurrentTopic> provider = new HttpTopicProvider(TOPIC_TRACKER_BASE_URL);
                Provider<CurrentTopic> provider = new DisabledProvider<>();
                return new HttpDataMonitor<>(provider, new HandlerScheduler(updateDelayMillis));
            }
        };
    }

    private void setupChatTranslation() {
        final int updateDelayMillis = 5000;
        ChatTranslationFragment.chatFactory = new DataMonitor.Factory() {
            @Override
            public DataMonitor create() {
                return createChatTranslation(CHAT_URL, new HandlerScheduler(updateDelayMillis));
            }
        };
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        PodcastsApp.release();
    }

    private static class HandlerScheduler implements Scheduler {
        private final int delayMillis;
        private Handler handler = new Handler();
        private Performer performer;
        private boolean isScheduled = false;
        private Runnable action = new Runnable() {
            @Override
            public void run() {
                performer.performAction();
                isScheduled = false;
            }
        };

        private HandlerScheduler(int delayMillis) {
            this.delayMillis = delayMillis;
        }

        @Override
        public void setPerformer(Performer performer) {
            this.performer = performer;
        }

        @Override
        public void scheduleNext() {
            if (isScheduled) {
                throw new ProgrammerError("The previous action hasn't finished yet");
            }

            isScheduled = true;
            handler.postDelayed(action, delayMillis);
        }

        @Override
        public void cancel() {
            handler.removeCallbacks(action);
            isScheduled = false;
        }
    }
}
