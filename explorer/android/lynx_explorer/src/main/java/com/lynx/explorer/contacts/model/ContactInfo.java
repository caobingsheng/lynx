// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
package com.lynx.explorer.contacts.model;

/**
 * 联系人基础信息类
 */
public class ContactInfo {
    private String id;                  // 联系人ID
    private String displayName;           // 显示名称
    private String firstName;             // 名
    private String lastName;              // 姓
    private String middleName;           // 中间名
    private String nickname;             // 昵称
    private String photoUri;             // 头像URI
    private long lastModifiedTime;       // 最后修改时间
    private boolean starred;             // 是否为星标联系人
    private int timesContacted;          // 联系次数
    private long lastTimeContacted;      // 最后联系时间

    public ContactInfo() {
    }

    public ContactInfo(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public int getTimesContacted() {
        return timesContacted;
    }

    public void setTimesContacted(int timesContacted) {
        this.timesContacted = timesContacted;
    }

    public long getLastTimeContacted() {
        return lastTimeContacted;
    }

    public void setLastTimeContacted(long lastTimeContacted) {
        this.lastTimeContacted = lastTimeContacted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContactInfo that = (ContactInfo) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ContactInfo{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", nickname='" + nickname + '\'' +
                ", photoUri='" + photoUri + '\'' +
                ", lastModifiedTime=" + lastModifiedTime +
                ", starred=" + starred +
                ", timesContacted=" + timesContacted +
                ", lastTimeContacted=" + lastTimeContacted +
                '}';
    }
}