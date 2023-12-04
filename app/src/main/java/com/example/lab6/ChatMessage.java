package com.example.lab6;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ChatMessage {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "message")
    private String message;

    @ColumnInfo(name = "timeSent")
    private String timeSent;

    @ColumnInfo(name = "SendorReceive")
    private boolean SendorReceive;

    // Empty constructor required by Room
    public ChatMessage() {}

    // Constructor
    public ChatMessage(String message, String timeSent, boolean SendorReceive) {
        this.message = message;
        this.timeSent = timeSent;
        this.SendorReceive = SendorReceive;
    }

    // Getter methods...

    public String getMessage() {
        return message;
    }

    public String getTimeSent() {
        return timeSent;
    }

    public boolean isSendorReceive() {
        return SendorReceive;
    }

    // Setter methods...

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimeSent(String timeSent) {
        this.timeSent = timeSent;
    }

    public void setSendorReceive(boolean SendorReceive) {
        this.SendorReceive = SendorReceive;
    }
}
