package io.enotes.sdk.repository.card;

import android.support.annotation.NonNull;

import org.ethereum.util.ByteUtil;

/**
 * A wrapper class for raw ISO-DEP data.
 */
public class Command {
    private String desc;
    private byte[] cmdByte;
    private String cmdStr;

    public static Command newCmd() {
        return new Command();
    }

    @NonNull
    public byte[] getCmdByte() {
        if (cmdByte == null && cmdStr != null) cmdByte = ByteUtil.hexStringToBytes(cmdStr);
        return cmdByte;
    }

    @NonNull
    public Command setCmdByte(@NonNull byte[] cmdByte) {
        this.cmdByte = cmdByte;
        this.cmdStr = null;
        return this;
    }

    @NonNull
    public String getCmdStr() {
        if (cmdStr == null && cmdByte != null) cmdStr = ByteUtil.toHexString(cmdByte);
        return cmdStr;
    }

    @NonNull
    public Command setCmdStr(@NonNull String cmdStr) {
        this.cmdStr = cmdStr;
        this.cmdByte = null;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public Command setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    @Override
    public String toString() {
        return "Command[" + desc + "]{" + getCmdStr() + "}";
    }
}
