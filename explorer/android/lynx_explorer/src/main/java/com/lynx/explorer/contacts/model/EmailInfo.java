// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
package com.lynx.explorer.contacts.model;

/**
 * 邮箱信息类
 */
public class EmailInfo {
    // 邮箱类型常量，对应Android ContactsContract.CommonDataKinds.Email
    public static final int TYPE_HOME = 1;
    public static final int TYPE_WORK = 2;
    public static final int TYPE_OTHER = 3;
    public static final int TYPE_MOBILE = 4;
    public static final int TYPE_CUSTOM = 5;

    private String address;    // 邮箱地址
    private int type;         // 类型
    private String label;      // 自定义标签
    private boolean isPrimary; // 是否为主要邮箱

    public EmailInfo() {
    }

    public EmailInfo(String address, int type) {
        this.address = address;
        this.type = type;
    }

    public EmailInfo(String address, int type, String label, boolean isPrimary) {
        this.address = address;
        this.type = type;
        this.label = label;
        this.isPrimary = isPrimary;
    }

    // Getters and Setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    /**
     * 获取类型名称
     */
    public String getTypeName() {
        switch (type) {
            case TYPE_HOME:
                return "家庭";
            case TYPE_WORK:
                return "工作";
            case TYPE_OTHER:
                return "其他";
            case TYPE_MOBILE:
                return "移动";
            case TYPE_CUSTOM:
                return label != null ? label : "自定义";
            default:
                return "未知";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmailInfo emailInfo = (EmailInfo) o;

        if (type != emailInfo.type) return false;
        if (isPrimary != emailInfo.isPrimary) return false;
        return address != null ? address.equals(emailInfo.address) : emailInfo.address == null;
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + type;
        result = 31 * result + (isPrimary ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EmailInfo{" +
                "address='" + address + '\'' +
                ", type=" + type +
                ", label='" + label + '\'' +
                ", isPrimary=" + isPrimary +
                '}';
    }
}