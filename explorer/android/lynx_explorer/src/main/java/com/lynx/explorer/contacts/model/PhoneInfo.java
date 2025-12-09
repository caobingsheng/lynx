// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
package com.lynx.explorer.contacts.model;

/**
 * 电话信息类
 */
public class PhoneInfo {
    // 电话类型常量，对应Android ContactsContract.CommonDataKinds.Phone
    public static final int TYPE_HOME = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_WORK = 3;
    public static final int TYPE_FAX_WORK = 4;
    public static final int TYPE_FAX_HOME = 5;
    public static final int TYPE_PAGER = 6;
    public static final int TYPE_OTHER = 7;
    public static final int TYPE_CALLBACK = 8;
    public static final int TYPE_CAR = 9;
    public static final int TYPE_COMPANY_MAIN = 10;
    public static final int TYPE_ISDN = 11;
    public static final int TYPE_MAIN = 12;
    public static final int TYPE_OTHER_FAX = 13;
    public static final int TYPE_RADIO = 14;
    public static final int TYPE_TELEX = 15;
    public static final int TYPE_TTY_TDD = 16;
    public static final int TYPE_WORK_MOBILE = 17;
    public static final int TYPE_WORK_PAGER = 18;
    public static final int TYPE_ASSISTANT = 19;
    public static final int TYPE_MMS = 20;
    public static final int TYPE_CUSTOM = 21;

    private String number;      // 电话号码
    private int type;           // 类型
    private String label;       // 自定义标签
    private boolean isPrimary;  // 是否为主要号码

    public PhoneInfo() {
    }

    public PhoneInfo(String number, int type) {
        this.number = number;
        this.type = type;
    }

    public PhoneInfo(String number, int type, String label, boolean isPrimary) {
        this.number = number;
        this.type = type;
        this.label = label;
        this.isPrimary = isPrimary;
    }

    // Getters and Setters
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
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
            case TYPE_MOBILE:
                return "手机";
            case TYPE_WORK:
                return "工作";
            case TYPE_FAX_WORK:
                return "工作传真";
            case TYPE_FAX_HOME:
                return "家庭传真";
            case TYPE_PAGER:
                return "寻呼机";
            case TYPE_OTHER:
                return "其他";
            case TYPE_CALLBACK:
                return "回拨";
            case TYPE_CAR:
                return "车载";
            case TYPE_COMPANY_MAIN:
                return "公司总机";
            case TYPE_ISDN:
                return "ISDN";
            case TYPE_MAIN:
                return "主要";
            case TYPE_OTHER_FAX:
                return "其他传真";
            case TYPE_RADIO:
                return "无线电";
            case TYPE_TELEX:
                return "电传";
            case TYPE_TTY_TDD:
                return "TTY_TDD";
            case TYPE_WORK_MOBILE:
                return "工作手机";
            case TYPE_WORK_PAGER:
                return "工作寻呼机";
            case TYPE_ASSISTANT:
                return "助理";
            case TYPE_MMS:
                return "彩信";
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

        PhoneInfo phoneInfo = (PhoneInfo) o;

        if (type != phoneInfo.type) return false;
        if (isPrimary != phoneInfo.isPrimary) return false;
        return number != null ? number.equals(phoneInfo.number) : phoneInfo.number == null;
    }

    @Override
    public int hashCode() {
        int result = number != null ? number.hashCode() : 0;
        result = 31 * result + type;
        result = 31 * result + (isPrimary ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PhoneInfo{" +
                "number='" + number + '\'' +
                ", type=" + type +
                ", label='" + label + '\'' +
                ", isPrimary=" + isPrimary +
                '}';
    }
}