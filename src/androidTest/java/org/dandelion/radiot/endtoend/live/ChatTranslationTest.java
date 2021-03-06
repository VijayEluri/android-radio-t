package org.dandelion.radiot.endtoend.live;

import org.dandelion.radiot.endtoend.live.helpers.ChatTranslationRunner;
import org.dandelion.radiot.helpers.NanoHTTPD;
import org.dandelion.radiot.helpers.ResponsiveHttpServer;
import org.dandelion.radiot.http.DataMonitor;
import org.dandelion.radiot.http.HttpDataMonitor;
import org.dandelion.radiot.http.Provider;
import org.dandelion.radiot.live.chat.HttpChatClient;
import org.dandelion.radiot.live.chat.Message;
import org.dandelion.radiot.live.schedule.DeterministicScheduler;
import org.dandelion.radiot.live.ui.ChatTranslationFragment;

import java.io.IOException;
import java.util.List;

import static org.dandelion.radiot.util.ChatStreamBuilder.chatStream;
import static org.dandelion.radiot.util.ChatStreamBuilder.message;

public class ChatTranslationTest extends LiveShowActivityTestCase {
    private LiveChatTranslationServer backend;
    private DeterministicScheduler scheduler;

    public void testAtStartup_RequestsChatContent() throws Exception {
        ChatTranslationRunner app = openScreen();
        backend.hasReceivedInitialRequest();
        backend.respondWithChatStream(chatStream(
                message(1, "Lorem ipsum"),
                message(2, "Dolor sit amet")));

        app.showsChatMessages("Lorem ipsum", "Dolor sit amet");
    }

    public void testRequestNextMessagesWhenRefreshing() throws Exception {
        final int INITIAL_SEQ = 1;
        final ChatTranslationRunner app = openScreen();

        backend.hasReceivedInitialRequest();
        backend.respondWithChatStream(chatStream(message(INITIAL_SEQ, "Lorem ipsum")));
        app.showsChatMessages("Lorem ipsum");

        app.refreshChat();
        backend.hasReceivedContinuationRequest(INITIAL_SEQ);
        backend.respondWithChatStream(chatStream(message(INITIAL_SEQ + 1, "Dolor sit amet")));
        app.showsChatMessages(
                "Lorem ipsum",
                "Dolor sit amet");

        app.refreshChat();
        backend.hasReceivedContinuationRequest(INITIAL_SEQ + 1);
        backend.respondWithChatStream(chatStream(message(INITIAL_SEQ + 2, "Consectetur adipiscing elit")));
        app.showsChatMessages(
                "Lorem ipsum",
                "Dolor sit amet",
                "Consectetur adipiscing elit");
    }

    public void testDisplayingErrorWhenUnableToGetMessages() throws Exception {
        ChatTranslationRunner app = openScreen();

        backend.hasReceivedInitialRequest();
        backend.respondWithError();
        app.showsErrorMessage();
    }

    private ChatTranslationRunner openScreen() {
        return new ChatTranslationRunner(getInstrumentation(), getActivity(), scheduler);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        backend = new LiveChatTranslationServer();
        scheduler = new DeterministicScheduler();
        ChatTranslationFragment.chatFactory = new DataMonitor.Factory() {
            @Override
            public DataMonitor create() {
                String chatUrl = LiveChatTranslationServer.baseUrl();
                Provider<List<Message>> client = HttpChatClient.create(chatUrl);
                return new HttpDataMonitor<>(client, scheduler);
            }
        };
    }

    @Override
    public void tearDown() throws Exception {
        backend.stop();
        super.tearDown();
    }
}

class LiveChatTranslationServer extends ResponsiveHttpServer {
    public LiveChatTranslationServer() throws IOException {
        super();
    }

    public void respondWithChatStream(String content) {
        respondSuccessWith(content, MIME_JSON);
    }

    public void hasReceivedInitialRequest() throws InterruptedException {
        hasReceivedRequest("/api/last/50", "");
    }

    public void hasReceivedContinuationRequest(int seq) throws InterruptedException {
        hasReceivedRequest("/api/new/" + seq, "");
    }

    public void respondWithError() {
        respondWith(new NanoHTTPD.Response(HTTP_NOTFOUND, MIME_HTML, ""));
    }
}


