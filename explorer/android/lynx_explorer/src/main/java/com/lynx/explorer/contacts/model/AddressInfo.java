// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
package com.lynx.explorer.contacts.model;

/**
 * 地址信息类
 */
public class AddressInfo {
    // 地址类型常量，对应Android ContactsContract.CommonDataKinds.StructuredPostal
    public static final int TYPE_HOME = 1;
    public static final int TYPE_WORK = 2;
    public static final int TYPE_OTHER = 3;
    public static final int TYPE_CUSTOM = 4;

    private String street;            // 街道
    private String city;              // 城市
    private String state;             // 省份
    private String postalCode;        // 邮编
    private String country;           // 国家
    private String formattedAddress;   // 格式化地址
    private int type;               // 类型
    private String label;            // 自定义标签

    public AddressInfo() {
    }

    public AddressInfo(String formattedAddress, int type) {
        this.formattedAddress = formattedAddress;
        this.type = type;
    }

    // Getters and Setters
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
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
            case TYPE_CUSTOM:
                return label != null ? label : "自定义";
            default:
                return "未知";
        }
    }

    /**
     * 获取完整地址字符串
     */
    public String getFullAddress() {
        if (formattedAddress != null && !formattedAddress.isEmpty()) {
            return formattedAddress;
        }

        StringBuilder sb = new StringBuilder();
        if (street != null && !street.isEmpty()) {
            sb.append(street);
        }
        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        if (state != null && !state.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(state);
        }
        if (postalCode != null && !postalCode.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(postalCode);
        }
        if (country != null && !country.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(country);
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AddressInfo that = (AddressInfo) o;

        if (type != that.type) return false;
        return formattedAddress != null ? formattedAddress.equals(that.formattedAddress) : that.formattedAddress == null;
    }

    @Override
    public int hashCode() {
        int result = formattedAddress != null ? formattedAddress.hashCode() : 0;
        result = 31 * result + type;
        return result;
    }

    @Override
    public String toString() {
        return "AddressInfo{" +
                "street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", country='" + country + '\'' +
                ", formattedAddress='" + formattedAddress + '\'' +
                ", type=" + type +
                ", label='" + label + '\'' +
                '}';
    }
}