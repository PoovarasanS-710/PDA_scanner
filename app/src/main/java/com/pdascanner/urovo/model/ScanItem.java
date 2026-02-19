package com.pdascanner.urovo.model;

import java.util.Date;

/**
 * Represents a single successful barcode/QR scan event.
 * Immutable — created once, never modified.
 */
public class ScanItem {

    private final String barcodeString;
    private final Date   timestamp;
    private final int    sequenceNumber; // 1-based, globally unique within session

    public ScanItem(String barcodeString, Date timestamp, int sequenceNumber) {
        this.barcodeString  = barcodeString;
        this.timestamp      = timestamp;
        this.sequenceNumber = sequenceNumber;
    }

    public String getBarcodeString()  { return barcodeString; }
    public Date   getTimestamp()      { return new Date(timestamp.getTime()); } // defensive copy
    public int    getSequenceNumber() { return sequenceNumber; }
}
