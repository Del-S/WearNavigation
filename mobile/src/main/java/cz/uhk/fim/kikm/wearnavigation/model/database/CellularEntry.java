package cz.uhk.fim.kikm.wearnavigation.model.database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(value = { "psc", "user", "type" })
public class CellularEntry {

    // Database labels for database
    public final static String DB_TABLE = "cellular";
    public final static String DB_ID = "id";
    public final static String DB_FINGERPRINT_DB_ID = "fingerprintId";
    public final static String DB_BSIC = "bsic";
    public final static String DB_CID = "cid";
    public final static String DB_LAC = "lac";
    public final static String DB_RSSI = "rssi";
    public final static String DB_DISTANCE = "distance";
    public final static String DB_TIMESTAMP = "timestamp";
    public final static String DB_SCAN_TIME = "scanTime";
    public final static String DB_SCAN_DIFFERENCE = "scanDifference";

    // Variables of this class
    @Expose(serialize = false)
    private int id;             // Database id (its inner id and it is not exported)
    private int fingerprintId;  // If of fingerprint that this entry belongs to
    private int bsic;           // Base Station Identity Code
    private int cid;            // GSM Cell Identity (CID Either 16-bit described in TS 27.007)
    private int lac;            // 16-bit Location Area Code
    private int rssi;           // Signal strength of the access point
    private float distance;     // Distance between access point and device
    private long timestamp;      // Device was found at this timestamp
    @JsonProperty("time")
    private long scanTime;       // Device was found at this time during the scan (seconds)
    /**
     * Difference between scanTime and last scanDifference (device based by bssid).
     * Informs about the time difference between this entry and previous one.
     */
    @JsonProperty("difference")
    private long scanDifference;

    // Default constructor used for Gson
    public CellularEntry() {}

    public CellularEntry(Object object) {
        Map<String, Object> cellularRecord = (HashMap<String, Object>) object;
        this.timestamp = Long.valueOf(cellularRecord.get("timestamp").toString());
        this.cid = Integer.valueOf(cellularRecord.get("cid").toString());
        this.lac = Integer.valueOf(cellularRecord.get("lac").toString());
        this.rssi = Integer.valueOf(cellularRecord.get("rssi").toString());
        this.distance = Float.valueOf(cellularRecord.get("distance").toString());
        this.scanTime = Long.valueOf(cellularRecord.get("time").toString());
        this.scanDifference = Long.valueOf(cellularRecord.get("difference").toString());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFingerprintId() {
        return fingerprintId;
    }

    public void setFingerprintId(int fingerprintId) {
        this.fingerprintId = fingerprintId;
    }

    public int getBsic() {
        return bsic;
    }

    public void setBsic(int bsic) {
        this.bsic = bsic;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getLac() {
        return lac;
    }

    public void setLac(int lac) {
        this.lac = lac;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getScanTime() {
        return scanTime;
    }

    public void setScanTime(long scanTime) {
        this.scanTime = scanTime;
    }

    public long getScanDifference() {
        return scanDifference;
    }

    public void setScanDifference(long scanDifference) {
        this.scanDifference = scanDifference;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CellularEntry cellularEntry = (CellularEntry) o;
        return Objects.equals(this.bsic, cellularEntry.bsic) &&
                Objects.equals(this.cid, cellularEntry.cid) &&
                Objects.equals(this.lac, cellularEntry.lac);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bsic, cid, lac);
    }


    @Override
    public String toString() {
        return "class CellularEntry {\n" +
                "    dbId: " + toIndentedString(id) + "\n" +
                "    bsic: " + toIndentedString(bsic) + "\n" +
                "    cid: " + toIndentedString(cid) + "\n" +
                "    lac: " + toIndentedString(lac) + "\n" +
                "    rssi: " + toIndentedString(rssi) + "\n" +
                "    distance: " + toIndentedString(distance) + "\n" +
                "    timestamp: " + toIndentedString(timestamp) + "\n" +
                "    scanTime: " + toIndentedString(scanTime) + "\n" +
                "    scanDifference: " + toIndentedString(scanDifference) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}