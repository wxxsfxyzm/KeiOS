package com.miui.permcenter;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class AppPermissionInfo implements Parcelable {
    public static final Parcelable.Creator<AppPermissionInfo> CREATOR = new Parcelable.Creator<AppPermissionInfo>() {
        @Override
        public AppPermissionInfo createFromParcel(Parcel source) {
            return new AppPermissionInfo(source);
        }

        @Override
        public AppPermissionInfo[] newArray(int size) {
            return new AppPermissionInfo[size];
        }
    };

    private int count;
    private boolean isAdaptedRpData;
    private boolean isAllowStartByWakePath;
    private boolean isDisableSociality;
    private boolean isEcmManagement;
    private boolean isRunning;
    private boolean isSystem;
    private String label;
    private boolean noScopedStorage;
    private String packageName;
    private HashMap<Long, Integer> permissionToAction = new HashMap<>();
    private HashMap<Long, Integer> permissionToLevel = new HashMap<>();
    private long requiredPermission;
    private int targetSdkVersion;
    private int uid;
    private String usageEvent;
    private int usageRecentDay;

    public AppPermissionInfo() {
    }

    protected AppPermissionInfo(Parcel parcel) {
        packageName = parcel.readString();
        label = parcel.readString();
        uid = parcel.readInt();
        count = parcel.readInt();
        usageEvent = parcel.readString();
        usageRecentDay = parcel.readInt();
        isAllowStartByWakePath = parcel.readByte() != 0;
        isRunning = parcel.readByte() != 0;
        isSystem = parcel.readByte() != 0;
        isAdaptedRpData = parcel.readByte() != 0;
        isDisableSociality = parcel.readByte() != 0;
        targetSdkVersion = parcel.readInt();
        noScopedStorage = parcel.readByte() != 0;
        requiredPermission = parcel.readLong();
        //noinspection unchecked
        permissionToAction = (HashMap<Long, Integer>) parcel.readHashMap(getClass().getClassLoader());
        //noinspection unchecked
        permissionToLevel = (HashMap<Long, Integer>) parcel.readHashMap(getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(packageName);
        parcel.writeString(label);
        parcel.writeInt(uid);
        parcel.writeInt(count);
        parcel.writeString(usageEvent);
        parcel.writeInt(usageRecentDay);
        parcel.writeByte((byte) (isAllowStartByWakePath ? 1 : 0));
        parcel.writeByte((byte) (isRunning ? 1 : 0));
        parcel.writeByte((byte) (isSystem ? 1 : 0));
        parcel.writeByte((byte) (isAdaptedRpData ? 1 : 0));
        parcel.writeByte((byte) (isDisableSociality ? 1 : 0));
        parcel.writeInt(targetSdkVersion);
        parcel.writeByte((byte) (noScopedStorage ? 1 : 0));
        parcel.writeLong(requiredPermission);
        parcel.writeMap(permissionToAction);
        parcel.writeMap(permissionToLevel);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setPermissionToAction(HashMap<Long, Integer> permissionToAction) {
        this.permissionToAction = permissionToAction;
    }

    public void setPermissionToLevel(HashMap<Long, Integer> permissionToLevel) {
        this.permissionToLevel = permissionToLevel;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public void setTargetSdkVersion(int targetSdkVersion) {
        this.targetSdkVersion = targetSdkVersion;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }
}
