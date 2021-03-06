package cz.uhk.fim.kikm.wearnavigation;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

import cz.uhk.fim.kikm.wearnavigation.model.tasks.FingerprintScanner;

public class WearApplication extends Application {

    private BackgroundPowerSaver backgroundPowerSaver;  // Power saver for BeaconLibrary
    private JobInfo.Builder jobBuilder;                 // Specific job to run via JobScheduler

    @Override
    public void onCreate() {
        super.onCreate();

        //BeaconManager.setDebug(true);     // Remove this after completing all scanning features
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);    // Load beacon manager instance to enable settings change
        // Enable beacon
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        //backgroundPowerSaver = new BackgroundPowerSaver(this);      // This reduces bluetooth power usage by about 60% when application is not visible
        beaconManager.setEnableScheduledScanJobs(false);              // Disable scheduled jobs since scans are not run in the background

        JobScheduler jobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );
        if(jobScheduler != null) {
            jobScheduler.cancel(FingerprintScanner.JOB_ID);
        }

        // Building job to run
        jobBuilder = new JobInfo.Builder(FingerprintScanner.JOB_ID,
                new ComponentName(getPackageName(), FingerprintScanner.class.getName()));
        jobBuilder.setOverrideDeadline(200);            // Set deadline which is the maximum scheduling latency.
        jobBuilder.setPersisted(false);                 // Set whether or not to persist this job across device reboots.

        // Never put wifi to sleep mode
        Settings.System.putInt(getContentResolver(),
                Settings.Global.WIFI_SLEEP_POLICY,
                Settings.Global.WIFI_SLEEP_POLICY_NEVER);
    }

    /**
     * Returns instance og Fingerprint job builder.
     *
     * @return JobInfo.Builder
     */
    public JobInfo.Builder getFingerprintJob() {
        return jobBuilder;
    }
}
