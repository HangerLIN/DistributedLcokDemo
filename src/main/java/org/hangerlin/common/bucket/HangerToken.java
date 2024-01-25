package org.hangerlin.common.bucket;

public class HangerToken {
    private boolean inUse;
    private final String nodeName;
    private byte[] data;

    public HangerToken(String nodeName, byte[] data) {
        this.inUse = false;
        this.nodeName = nodeName;
        this.data = data;
    }

    public synchronized void use() {
        if (inUse) {
            throw new IllegalStateException("Token is already in use.");
        }
        inUse = true;
    }

    public synchronized void release() {
        inUse = false;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getNodeName() {
        return nodeName;
    }
}
