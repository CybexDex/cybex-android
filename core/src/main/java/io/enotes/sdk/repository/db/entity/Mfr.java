package io.enotes.sdk.repository.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "mfr", indices = {@Index(value = {"vendorName", "batch"},
        unique = true)})
public class Mfr {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String vendorName;
    private String batch;
    private String publicKey;//compressed

    public Mfr(String vendorName, String batch, String publicKey) {
        this.vendorName = vendorName;
        this.batch = batch;
        this.publicKey = publicKey;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "Mfr{" +
                "id=" + id +
                ", vendorName='" + vendorName + '\'' +
                ", batch=" + batch +
                ", publicKey='" + publicKey + '\'' +
                '}';
    }
}
