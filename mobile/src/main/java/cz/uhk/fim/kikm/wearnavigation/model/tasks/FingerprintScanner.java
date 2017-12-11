package cz.uhk.fim.kikm.wearnavigation.model.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.altbeacon.beacon.Beacon;

import java.util.Collection;
import java.util.List;

import cz.uhk.fim.kikm.wearnavigation.model.database.BeaconEntry;
import cz.uhk.fim.kikm.wearnavigation.model.database.Fingerprint;
import cz.uhk.fim.kikm.wearnavigation.model.database.WirelessEntry;
import cz.uhk.fim.kikm.wearnavigation.model.database.helpers.DatabaseCRUD;

// TODO: github issue #19
// TODO: add cellular scanner
// TODO: add sensor scanner
public class FingerprintScanner extends AsyncTask<Void, Void, Fingerprint> implements BluetoothLEScannerInterface {

    private BluetoothLEScanner mBluetoothLEScanner;
    private DatabaseCRUD mDatabase;
    private final int TIME = 30000;
    private long startTime;
    private boolean mServiceBound = false;
    private Fingerprint mFingerprint;
    private WifiManager wifiManager;
    private Context mContext;
    private WifiScanReceiver wifiScanReceiver;

    public FingerprintScanner(Context context, Fingerprint fingerprint) {
        mContext = context;
        mFingerprint = fingerprint;
        mBluetoothLEScanner = new BluetoothLEScanner(context, this);
        mDatabase = new DatabaseCRUD(context);

        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiScanReceiver = new WifiScanReceiver();
        context.registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    @Override
    protected Fingerprint doInBackground(Void... voids) {
        Log.d("SCANNER", "Executing scan");
        // Checking if ble scanner service was bound or not (3 tries)
        int connectionTry = 3;
        while(!mServiceBound && connectionTry > 0) {
            try {
                Log.d("SCANNER", "waiting for LE connection");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connectionTry--;
        }

        // Service is not bound
        if(!mServiceBound) {
            return null;
        }

        // Until the scan time is up the thread will be put to sleep
        while (System.currentTimeMillis() < startTime + TIME) {
            if (!isCancelled()) {
                try {
                    Log.d("SCANNER", "Scanning");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                mBluetoothLEScanner.cancelScan();
                return null;
            }
            //publishProgress();
        }

        Log.d("SCANNER", "Scan finished");

        // Return calculated fingerprint
        return mFingerprint;
    }

    @Override
    protected void onPostExecute(Fingerprint fingerprint) {
        if(fingerprint != null) {
            //mDatabase.saveFingerprint(fingerprint, null);

            Log.d("Scanner", "Beacon count: " + fingerprint.getBeaconEntries().size());
            Log.d("Scanner", "Wireless count: " + fingerprint.getWirelessEntries().size());
        }

        // Unbinding the scanner service
        mContext.unregisterReceiver(wifiScanReceiver);
        mBluetoothLEScanner.handleDestroy();
    }

    @Override
    public void serviceConnected() {
        mBluetoothLEScanner.setScanPeriods(100L, 1000L);
        if(mBluetoothLEScanner.startScan(TIME)) {
            mServiceBound = true;
            // Set start time after service was connected
            startTime = System.currentTimeMillis();
        }
    }

    @Override
    public void foundBeacon(Beacon beacon) {
        List<BeaconEntry> beaconEntries = mFingerprint.getBeaconEntries();           // Load list of current beacons
        beaconEntries.add( createBeaconEntry(beacon, System.currentTimeMillis()) );   // Add new calculated BeaconEntry
    }

    @Override
    public void foundMultipleBeacons(Collection<Beacon> beacons) {
        List<BeaconEntry> beaconEntries = mFingerprint.getBeaconEntries();    // Load list of current beacons
        long currentMillis = System.currentTimeMillis();                      // Get current milliseconds for calculation of times

        // Create BeaconEntries from found beacons and add them to the list
        for (Beacon beacon : beacons) {
            beaconEntries.add( createBeaconEntry(beacon, currentMillis) );
        }
    }

    /**
     * Create BeaconEntry from beacon.
     *
     * @param beacon found beacon to get data from
     * @param currentMillis to calculate scanTime
     * @return BeaconEntry
     */
    private BeaconEntry createBeaconEntry(Beacon beacon, long currentMillis) {
        // Calculate time variables
        String bssid = beacon.getBluetoothAddress();
        long scanTime = currentMillis - startTime;
        long scanDifference = calculateBeaconScanDifference(scanTime, bssid);

        // Create new BeaconEntry and set data into it
        BeaconEntry newBeacon = new BeaconEntry();
        newBeacon.setBssid(bssid);
        newBeacon.setDistance((float) beacon.getDistance());
        newBeacon.setRssi(beacon.getRssi());
        newBeacon.setTimestamp(currentMillis);
        newBeacon.setScanTime(scanTime);
        newBeacon.setScanDifference(scanDifference);

        return newBeacon;
    }

    /**
     * Calculates beacon scanDifference based on max scanTime and current scan time.
     *
     * @param scanTime actual scan time
     * @param bssid beacon bssid
     * @return long scanDifference
     */
    private long calculateBeaconScanDifference(long scanTime, String bssid) {
        List<BeaconEntry> beacons = mFingerprint.getBeaconEntries();        // Load list of current beacons
        BeaconEntry tempBeacon = new BeaconEntry(bssid);                    // Create beacon with bssid
        long scanDifference = 0;                                            // Scan time that will be calculated

        // We get data only from a specific beacon
        if(beacons != null && beacons.contains(tempBeacon)) {
            long beaconScanTime = 0;

            for(BeaconEntry beacon : beacons) {
                if(!beacon.equals(tempBeacon)) continue;                // Ignore different beacons
                if(beacon.getScanTime() > beaconScanTime) beaconScanTime = beacon.getScanTime();    // Higher scanTime gets saved
            }

            // Calculate actual scanDifference
            scanDifference = scanTime - beaconScanTime;
        }
        return scanDifference;
    }

    /**
     * Receiver that handles Wifi scanner.
     * Parse Wifi networks into WirelessEntries.
     */
    class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            List<WirelessEntry> wirelessEntries = mFingerprint.getWirelessEntries();        // Load list of current wireless entries
            long currentMillis = System.currentTimeMillis();
            long scanTime = currentMillis - startTime;

            for (ScanResult scanResult : wifiManager.getScanResults()) {
                // Create new WirelessEntry and set its data
                WirelessEntry wirelessEntry = new WirelessEntry();
                wirelessEntry.setSsid(scanResult.SSID);                 // Set wireless SSID
                wirelessEntry.setBssid(scanResult.BSSID);               // Set wireless BSSID
                wirelessEntry.setRssi(scanResult.level);                // Set wireless RSSI
                wirelessEntry.setFrequency(scanResult.frequency);       // Set wireless Frequency
                // Set wireless channel width (api 23+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    wirelessEntry.setChannel(scanResult.channelWidth);
                }

                // Calculated distance and times
                wirelessEntry.setDistance((float) (Math.pow(10.0d, (27.55d - 40d * Math.log10(scanResult.frequency) + 6.7d - scanResult.level) / 20.0d) * 1000.0));
                wirelessEntry.setTimestamp(currentMillis);      // Current timestamp set to entry
                wirelessEntry.setScanTime(scanTime);            // Current scan time
                wirelessEntry.setScanDifference(calculateWirelessScanDifference(scanTime, wirelessEntry));  // Calculated scan difference for the entry

                // Add new WirelessEntry to the list
                wirelessEntries.add(wirelessEntry);
            }

            // Start the scan again. Disabled at onPostExecute.
            wifiManager.startScan();
        }
    }

    /**
     * Calculates wireless scanDifference based on max scanTime and current scan time.
     *
     * @param scanTime actual scan time
     * @param wirelessEntry current wireless entry to calculate difference for
     * @return long scanDifference
     */
    private long calculateWirelessScanDifference(long scanTime, WirelessEntry wirelessEntry) {
        List<WirelessEntry> wirelessEntries = mFingerprint.getWirelessEntries();        // Load list of current wireless entries
        long scanDifference = 0;                                                        // Scan time that will be calculated

        // We get data only from a specific wireless entries
        if(wirelessEntries != null && wirelessEntries.contains(wirelessEntry)) {
            long wirelessScanTime = 0;

            for(WirelessEntry tempWirelessEntry : wirelessEntries) {
                if(!tempWirelessEntry.equals(wirelessEntry)) continue;                // Ignore different entries
                if(tempWirelessEntry.getScanTime() > wirelessScanTime) wirelessScanTime = tempWirelessEntry.getScanTime();    // Higher scanTime gets saved
            }

            // Calculate actual scanDifference
            scanDifference = scanTime - wirelessScanTime;
        }
        return scanDifference;
    }
}
