package com.cybex.provider.http.entity;

public class AssetRecord {

    private int count;
    private GroupInfo groupInfo;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public GroupInfo getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
    }

    public class GroupInfo {
        private String asset;

        public String getAsset() {
            return asset;
        }

        public void setAsset(String asset) {
            this.asset = asset;
        }
    }
}
