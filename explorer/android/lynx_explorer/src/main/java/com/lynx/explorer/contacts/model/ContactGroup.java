// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
package com.lynx.explorer.contacts.model;

/**
 * 联系人分组类
 */
public class ContactGroup {
    private String groupId;         // 分组ID
    private String groupName;       // 分组名称
    private ContactGroupType type;  // 分组类型
    private int contactCount;       // 联系人数量
    private String sortOrder;       // 排序字段
    private String groupKey;        // 分组键值（如字母A、B等）

    public ContactGroup() {
    }

    public ContactGroup(String groupId, String groupName, ContactGroupType type) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.type = type;
        this.contactCount = 0;
        this.sortOrder = "display_name";
    }

    public ContactGroup(String groupId, String groupName, ContactGroupType type, int contactCount) {
        this(groupId, groupName, type);
        this.contactCount = contactCount;
    }

    // Getters and Setters
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ContactGroupType getType() {
        return type;
    }

    public void setType(ContactGroupType type) {
        this.type = type;
    }

    public int getContactCount() {
        return contactCount;
    }

    public void setContactCount(int contactCount) {
        this.contactCount = contactCount;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    /**
     * 创建字母分组
     */
    public static ContactGroup createAlphabeticalGroup(String letter, int count) {
        ContactGroup group = new ContactGroup();
        group.setGroupId("alpha_" + letter);
        group.setGroupName(letter.toUpperCase());
        group.setType(ContactGroupType.ALPHABETICAL);
        group.setContactCount(count);
        group.setGroupKey(letter.toUpperCase());
        group.setSortOrder("display_name");
        return group;
    }

    /**
     * 创建频率分组
     */
    public static ContactGroup createFrequencyGroup(String name, int count) {
        ContactGroup group = new ContactGroup();
        group.setGroupId("freq_" + name);
        group.setGroupName(name);
        group.setType(ContactGroupType.FREQUENCY);
        group.setContactCount(count);
        group.setSortOrder("times_contacted DESC");
        return group;
    }

    /**
     * 创建星标分组
     */
    public static ContactGroup createStarredGroup(int count) {
        ContactGroup group = new ContactGroup();
        group.setGroupId("starred");
        group.setGroupName("星标联系人");
        group.setType(ContactGroupType.STARRED);
        group.setContactCount(count);
        group.setSortOrder("display_name");
        return group;
    }

    /**
     * 创建公司分组
     */
    public static ContactGroup createCompanyGroup(String company, int count) {
        ContactGroup group = new ContactGroup();
        group.setGroupId("company_" + company);
        group.setGroupName(company);
        group.setType(ContactGroupType.COMPANY);
        group.setContactCount(count);
        group.setGroupKey(company);
        group.setSortOrder("display_name");
        return group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContactGroup that = (ContactGroup) o;

        if (type != that.type) return false;
        return groupId != null ? groupId.equals(that.groupId) : that.groupId == null;
    }

    @Override
    public int hashCode() {
        int result = groupId != null ? groupId.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ContactGroup{" +
                "groupId='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", type=" + type +
                ", contactCount=" + contactCount +
                ", sortOrder='" + sortOrder + '\'' +
                ", groupKey='" + groupKey + '\'' +
                '}';
    }
}