package com.solverlabs.worldcraft.srv.domain;

public class RoomUploadStatus {
    private static final String STATUS_STRING_UPLOADED = "uploaded";
    private static final String STATUS_STRING_UPLOAD_FAILED = "upload_failed";
    public static final int STATUS_UPLOAD_FAILED = 2;
    public static final int STATUS_UPLOAD_SUCCESS = 1;
    private long id;
    private long roomId;
    private int status;

    public RoomUploadStatus(long j, String str, long j2) {
        this.id = j;
        setStatus(str);
        this.roomId = j2;
    }

    public long getId() {
        return this.id;
    }

    public long getRoomId() {
        return this.roomId;
    }

    public int getStatus() {
        return this.status;
    }

    public boolean isSuccess() {
        return this.status == 1;
    }

    public void setId(long j) {
        this.id = j;
    }

    public void setRoomId(long j) {
        this.roomId = j;
    }

    public void setStatus(String str) {
        if (STATUS_STRING_UPLOADED.equals(str)) {
            this.status = 1;
        } else if (STATUS_STRING_UPLOAD_FAILED.equals(str)) {
            this.status = 2;
        }
    }
}
