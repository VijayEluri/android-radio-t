package org.dandelion.radiot.common.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class TrackedActivity extends FragmentActivity {
    private ActivityTracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tracker = ActivityTracker.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        tracker.activityStarted(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        tracker.activityStopped(this);
    }

    public void trackEvent(String action, String label) {
        tracker.trackEvent(action, label);
    }
}
