package org.dandelion.radiot.live.chat;

import android.os.AsyncTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

public class HttpChatTranslation implements ChatTranslation {
    private MessageConsumer consumer;
    private final HttpChatClient chatClient;

    public HttpChatTranslation(String baseUrl) {
        this.chatClient = new HttpChatClient(baseUrl);
    }

    @Override
    public void start(MessageConsumer consumer) {
        this.consumer = consumer;
        requestLastRecords();
    }

    private void requestLastRecords() {
        new LastRecordsRequest(chatClient, consumer).execute();
    }

    @Override
    public void refresh() {
        new NextRecordsRequest(chatClient, consumer).execute();
    }

    @Override
    public void stop() {
        // TODO: Properly close connections
    }

    private static abstract class ConnectTask extends AsyncTask<Void, Void, List<Message>> {
        protected final MessageConsumer consumer;
        private Exception error;
        private final HttpChatClient chatClient;

        public ConnectTask(HttpChatClient chatClient, MessageConsumer consumer) {
            this.consumer = consumer;
            this.chatClient = chatClient;
        }

        @Override
        protected List<Message> doInBackground(Void... params) {
            try {
                return parseMessages(chatClient.requestMessages(mode()));
            } catch (Exception e) {
                error = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Message> messages) {
            if (error != null) {
                consumeError(error);
            } else {
                consumeMessages(messages);
            }
        }

        protected abstract void consumeMessages(List<Message> messages);
        protected abstract String mode();

        private void consumeError(Exception e) {

        }

        private List<Message> parseMessages(String json) {
            return ResponseParser.parse(json);
        }
    }

    private static class LastRecordsRequest extends ConnectTask {

        public LastRecordsRequest(HttpChatClient chatClient, MessageConsumer consumer) {
            super(chatClient, consumer);
        }

        @Override
        protected void consumeMessages(List<Message> messages) {
            consumer.initWithMessages(messages);
        }

        @Override
        protected String mode() {
            return "last";
        }
    }

    private static class NextRecordsRequest extends ConnectTask {

        private NextRecordsRequest(HttpChatClient chatClient, MessageConsumer consumer) {
            super(chatClient, consumer);
        }

        @Override
        protected void consumeMessages(List<Message> messages) {
            consumer.appendMessages(messages);
        }

        @Override
        protected String mode() {
            return "next";
        }
    }

    private static class HttpChatClient {
        private final String baseUrl;
        private final DefaultHttpClient httpClient;

        public HttpChatClient(String baseUrl) {
            this.baseUrl = baseUrl;
            this.httpClient = new DefaultHttpClient();
        }

        public String requestMessages(String mode) throws IOException {
            HttpResponse response = httpClient.execute(new HttpGet(chatStreamUrl(mode)));
            return EntityUtils.toString(response.getEntity());
        }

        private String chatStreamUrl(String mode) {
            return baseUrl + "/data/jsonp?mode=" + mode + "&recs=10";
        }
    }
}
