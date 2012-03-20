package org.dandelion.radiot.podcasts.download;

import android.app.IntentService;
import android.content.Intent;
import org.dandelion.radiot.podcasts.PodcastsApp;

public class DownloadService extends IntentService {
    private static String TAG = DownloadService.class.getName();
    public static String START_DOWNLOAD_ACTION = TAG + ".START_DOWNLOAD";
    public static final String DOWNLOAD_COMPLETE_ACTION = TAG + ".DOWNLOAD_COMPLETE";

    public static String URL_EXTRA = TAG + ".URL";
    public static final String TITLE_EXTRA = TAG + ".TITLE";
    public static final String TASK_ID_EXTRA = TAG + ".TASK_ID";

    private DownloadEngine downloader;

    public DownloadService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (START_DOWNLOAD_ACTION.equals(action)) {
            startDownloading(intent);
        }
        if (DOWNLOAD_COMPLETE_ACTION.equals(action)) {
            processCompletedDownload(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createCore();
    }

    private void processCompletedDownload(Intent intent) {
        long taskId = intent.getLongExtra(TASK_ID_EXTRA, 0);
        downloader.finishDownload(taskId);
    }

    private void createCore() {
        PodcastsApp app = PodcastsApp.getInstance();
        DownloadManager downloadManager = app.createDownloadManager();
        DownloadFolder downloadFolder = app.getPodcastDownloadFolder();
        MediaScanner mediaScanner = app.createMediaScanner();
        downloader = new DownloadEngine(downloadManager, downloadFolder, mediaScanner);
    }

    private void startDownloading(Intent intent) {
        DownloadManager.DownloadTask task = new DownloadManager.DownloadTask();
        task.url = intent.getStringExtra(URL_EXTRA);
        task.title = intent.getStringExtra(TITLE_EXTRA);
        downloader.startDownloading(task);
    }
}
