package org.dandelion.radiot.podcasts;

import android.app.DownloadManager;
import android.content.Context;
import android.os.Environment;
import org.dandelion.radiot.podcasts.download.PodcastDownloader;
import org.dandelion.radiot.podcasts.core.PodcastPlayer;
import org.dandelion.radiot.podcasts.download.PodcastDownloadManager;
import org.dandelion.radiot.podcasts.download.SystemDownloadManager;
import org.dandelion.radiot.podcasts.ui.ExternalPlayer;

import java.io.File;

public class PodcastsApp {
    private static PodcastsApp instance;
    private PodcastPlayer player;
    private Context context;

    public static void initialize(Context context) {
        if (null == instance) {
            instance = new PodcastsApp(context);
        }
    }
    
    public static void release() {
        instance.releaseInstance();
        instance = null;
    }

    public static PodcastsApp getInstance() {
        return instance;
    }

    public static void setTestingInstance(PodcastsApp newInstance) {
        instance = newInstance;
    }

    public PodcastPlayer getPlayer() {
        return player;
    }

    protected PodcastsApp(Context context) {
        this.context = context;
        player = new ExternalPlayer();
    }

    private void releaseInstance() {
        context = null;
    }

    public PodcastDownloader createDownloader() {
        return new PodcastDownloader(createDownloadManager(), getPodcastDownloadFolder());
    }

    protected File getPodcastDownloadFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    protected PodcastDownloadManager createDownloadManager() {
        return new SystemDownloadManager((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE));
    }
}
