package cz.uhk.fim.kikm.wearnavigation.model.database;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@JsonIgnoreProperties(value = { "_rev", "user" })
public class Fingerprint implements Parcelable {

    // Database labels for database
    public final static String DB_TABLE = "fingerprint";
    public final static String DB_ID = "dbId";
    public final static String DB_FINGERPRINT_ID = "id";
    public final static String DB_FINGERPRINT_SCAN_ID = "scanID";
    public final static String DB_X = "x";
    public final static String DB_Y = "y";
    public final static String DB_SCAN_LENGTH = "scanLength";
    public final static String DB_SCAN_START = "scanStart";
    public final static String DB_SCAN_END = "scanEnd";
    public final static String DB_UPDATE_TIME = "updateTime";
    public final static String DB_LEVEL = "level";
    public final static String DB_LOCATION_ID = "locationDbId";
    public final static String DB_DEVICE_ID = "deviceDbId";

    // Variables of this class
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Expose(serialize = false)
    private int dbId;                              // Database id (its inner id and it is not exported)
    private UUID id;                                // UUID of this scan
    private UUID scanID;                            // UUID to enable fingerprint grouping
    private int x,y;                                // Calculated X and Y locations
    private long scanLength;                        // Length of the scan in ms
    @SerializedName("timestamp")
    @JsonProperty("timestamp")
    private long scanStart;                         // Timestamps of scan start
    @SerializedName("finish")
    @JsonProperty("finish")
    private long  scanEnd;                          // Timestamps of scan end
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Expose(serialize = false)
    private long updateTime;                        // Sync gateway update time (no need to expose it since gateway already knows it)
    private String level;
    private long locationDbId;
    private LocationEntry locationEntry;            // Location of fingerprint to enable multiple buildings and floors
    private long deviceDbId;
    @SerializedName("deviceRecord")
    @JsonProperty("deviceRecord")
    private DeviceEntry deviceEntry;                // Device that created this fingerprint
    @SerializedName("bluetoothRecords")
    @JsonProperty("bluetoothRecords")
    private List<BeaconEntry> beaconEntries;        // List of beacon entries scanned for this fingerprint
    @SerializedName("wirelessRecords")
    @JsonProperty("wirelessRecords")
    private List<WirelessEntry> wirelessEntries;    // List of wireless entries scanned for this fingerprint
    @SerializedName("cellularRecords")
    @JsonProperty("cellularRecords")
    private List<CellularEntry> cellularEntries;    // List of cellular entries scanned for this fingerprint
    @SerializedName("sensorRecords")
    @JsonProperty("sensorRecords")
    private List<SensorEntry> sensorEntries;        // List of beacon entries scanned for this fingerprint

    // Used for JSON parsing
    public Fingerprint() {
        // Initiate lists
        beaconEntries = new ArrayList<>();
        wirelessEntries = new ArrayList<>();
        cellularEntries = new ArrayList<>();
        sensorEntries = new ArrayList<>();
    }

    public Fingerprint(Context context) {
        // Set id and scan UUID to send into other device
        id = UUID.randomUUID();
        scanID = UUID.randomUUID();

        // Initiate lists
        beaconEntries = new ArrayList<>();
        wirelessEntries = new ArrayList<>();
        cellularEntries = new ArrayList<>();
        sensorEntries = new ArrayList<>();

        // Set device
        deviceEntry = DeviceEntry.createInstance(context);

        // Default scan length is 60s
        scanLength = 60000;
    }

    private Fingerprint(Parcel in) {
        dbId = in.readInt();
        id = UUID.fromString(in.readString());
        scanID = UUID.fromString(in.readString());
        x = in.readInt();
        y = in.readInt();
        scanLength = in.readLong();
        scanStart = in.readLong();
        scanEnd = in.readLong();
        updateTime = in.readLong();
        level = in.readString();
        locationDbId = in.readLong();
        locationEntry = in.readParcelable(LocationEntry.class.getClassLoader());
        deviceDbId = in.readLong();
        deviceEntry = in.readParcelable(DeviceEntry.class.getClassLoader());
        beaconEntries = in.createTypedArrayList(BeaconEntry.CREATOR);
        wirelessEntries = in.createTypedArrayList(WirelessEntry.CREATOR);
        cellularEntries = in.createTypedArrayList(CellularEntry.CREATOR);
        sensorEntries = in.createTypedArrayList(SensorEntry.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(dbId);
        dest.writeString(id.toString());
        dest.writeString(scanID.toString());
        dest.writeInt(x);
        dest.writeInt(y);
        dest.writeLong(scanLength);
        dest.writeLong(scanStart);
        dest.writeLong(scanEnd);
        dest.writeLong(updateTime);
        dest.writeString(level);
        dest.writeLong(locationDbId);
        dest.writeParcelable(locationEntry, flags);
        dest.writeLong(deviceDbId);
        dest.writeParcelable(deviceEntry, flags);
        dest.writeTypedList(beaconEntries);
        dest.writeTypedList(wirelessEntries);
        dest.writeTypedList(cellularEntries);
        dest.writeTypedList(sensorEntries);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Fingerprint> CREATOR = new Creator<Fingerprint>() {
        @Override
        public Fingerprint createFromParcel(Parcel in) {
            return new Fingerprint(in);
        }

        @Override
        public Fingerprint[] newArray(int size) {
            return new Fingerprint[size];
        }
    };

    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getScanID() {
        return scanID;
    }

    public void setScanID(UUID scanID) {
        this.scanID = scanID;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public long getScanLength() {
        return scanLength;
    }

    public void setScanLength(long scanLength) {
        this.scanLength = scanLength;
    }

    public long getScanStart() {
        return scanStart;
    }

    public void setScanStart(long scanStart) {
        this.scanStart = scanStart;
    }

    public long getScanEnd() {
        return scanEnd;
    }

    public void setScanEnd(long scanEnd) {
        this.scanEnd = scanEnd;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        // Set level to build from legacy
        if(level != null) {
            this.locationEntry = new LocationEntry(level);
        }
        this.level = level;
    }

    public long getLocationDbId() {
        return locationDbId;
    }

    public void setLocationDbId(long locationDbId) {
        this.locationDbId = locationDbId;
    }

    public LocationEntry getLocationEntry() {
        return locationEntry;
    }

    public void setLocationEntry(LocationEntry locationEntry) {
        this.locationEntry = locationEntry;
        // Set level to keep legacy
        this.level = locationEntry.getLevel();
    }

    public long getDeviceDbId() {
        return deviceDbId;
    }

    public void setDeviceDbId(long deviceDbId) {
        this.deviceDbId = deviceDbId;
    }

    public DeviceEntry getDeviceEntry() {
        return deviceEntry;
    }

    public void setDeviceEntry(DeviceEntry deviceEntry) {
        this.deviceEntry = deviceEntry;
    }

    public List<BeaconEntry> getBeaconEntries() {
        if(beaconEntries == null) {
            beaconEntries = new ArrayList<>();
        }
        return beaconEntries;
    }

    public void setBeaconEntries(List<BeaconEntry> beaconEntries) {
        this.beaconEntries = beaconEntries;
    }

    public List<WirelessEntry> getWirelessEntries() {
        if(wirelessEntries == null) {
            wirelessEntries = new ArrayList<>();
        }
        return wirelessEntries;
    }

    public void setWirelessEntries(List<WirelessEntry> wirelessEntries) {
        this.wirelessEntries = wirelessEntries;
    }

    public List<CellularEntry> getCellularEntries() {
        if(cellularEntries == null) {
            cellularEntries = new ArrayList<>();
        }
        return cellularEntries;
    }

    public void setCellularEntries(List<CellularEntry> cellularEntries) {
        this.cellularEntries = cellularEntries;
    }

    public List<SensorEntry> getSensorEntries() {
        if(sensorEntries == null) {
            sensorEntries = new ArrayList<>();
        }
        return sensorEntries;
    }

    public void setSensorEntries(List<SensorEntry> sensorEntries) {
        this.sensorEntries = sensorEntries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Fingerprint fingerprint = (Fingerprint) o;
        return Objects.equals(this.id, fingerprint.id) &&
                Objects.equals(this.scanID, fingerprint.scanID) &&
                Objects.equals(this.x, fingerprint.x) &&
                Objects.equals(this.y, fingerprint.y) &&
                Objects.equals(this.scanLength, fingerprint.scanLength) &&
                Objects.equals(this.scanStart, fingerprint.scanStart) &&
                Objects.equals(this.scanEnd, fingerprint.scanEnd) &&
                Objects.equals(this.updateTime, fingerprint.updateTime) &&
                Objects.equals(this.locationEntry, fingerprint.locationEntry) &&
                Objects.equals(this.deviceEntry, fingerprint.deviceEntry) &&
                Objects.equals(this.beaconEntries, fingerprint.beaconEntries) &&
                Objects.equals(this.wirelessEntries, fingerprint.wirelessEntries) &&
                Objects.equals(this.cellularEntries, fingerprint.cellularEntries) &&
                Objects.equals(this.sensorEntries, fingerprint.sensorEntries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,
                scanID,
                x,
                y,
                scanStart,
                scanEnd,
                updateTime,
                locationEntry,
                deviceEntry,
                beaconEntries,
                wirelessEntries,
                cellularEntries,
                sensorEntries);
    }


    @Override
    public String toString() {
        return "class Fingerprint {\n" +
                "    dbId: " + toIndentedString(dbId) + "\n" +
                "    id: " + toIndentedString(id) + "\n" +
                "    scanID: " + toIndentedString(scanID) + "\n" +
                "    x: " + toIndentedString(x) + "\n" +
                "    y: " + toIndentedString(y) + "\n" +
                "    scanLength: " + toIndentedString(scanLength) + "\n" +
                "    scanStart: " + toIndentedString(scanStart) + "\n" +
                "    scanEnd: " + toIndentedString(scanEnd) + "\n" +
                "    updateTime: " + toIndentedString(updateTime) + "\n" +
                "    level: " + toIndentedString(level) + "\n" +
                "    locationEntry: " + toIndentedString(locationEntry) + "\n" +
                "    deviceEntry: " + toIndentedString(deviceEntry) + "\n" +
                "    beaconEntriesCount: " + toIndentedString(beaconEntries.size()) + "\n" +
                "    wirelessEntriesCount: " + toIndentedString(wirelessEntries.size()) + "\n" +
                "    cellularEntriesCount: " + toIndentedString(cellularEntries.size()) + "\n" +
                "    sensorEntriesCount: " + toIndentedString(sensorEntries.size()) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}