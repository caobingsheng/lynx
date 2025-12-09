// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
package com.lynx.explorer.contacts.model;

import java.util.List;
import java.util.ArrayList;

/**
 * 完整联系人对象
 */
public class Contact {
    private ContactInfo contactInfo;
    private List<PhoneInfo> phones;
    private List<EmailInfo> emails;
    private List<AddressInfo> addresses;
    private List<OrganizationInfo> organizations;

    public Contact() {
        this.phones = new ArrayList<>();
        this.emails = new ArrayList<>();
        this.addresses = new ArrayList<>();
        this.organizations = new ArrayList<>();
    }

    public Contact(ContactInfo contactInfo) {
        this();
        this.contactInfo = contactInfo;
    }

    // Getters and Setters
    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public List<PhoneInfo> getPhones() {
        return phones;
    }

    public void setPhones(List<PhoneInfo> phones) {
        this.phones = phones != null ? phones : new ArrayList<PhoneInfo>();
    }

    public List<EmailInfo> getEmails() {
        return emails;
    }

    public void setEmails(List<EmailInfo> emails) {
        this.emails = emails != null ? emails : new ArrayList<EmailInfo>();
    }

    public List<AddressInfo> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressInfo> addresses) {
        this.addresses = addresses != null ? addresses : new ArrayList<AddressInfo>();
    }

    public List<OrganizationInfo> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<OrganizationInfo> organizations) {
        this.organizations = organizations != null ? organizations : new ArrayList<OrganizationInfo>();
    }

    // 便捷方法
    public String getId() {
        return contactInfo != null ? contactInfo.getId() : null;
    }

    public String getDisplayName() {
        return contactInfo != null ? contactInfo.getDisplayName() : null;
    }

    public String getPhotoUri() {
        return contactInfo != null ? contactInfo.getPhotoUri() : null;
    }

    public boolean isStarred() {
        return contactInfo != null && contactInfo.isStarred();
    }

    /**
     * 获取主要电话号码
     */
    public String getPrimaryPhoneNumber() {
        if (phones == null || phones.isEmpty()) {
            return null;
        }

        // 首先查找标记为主要的号码
        for (PhoneInfo phone : phones) {
            if (phone.isPrimary()) {
                return phone.getNumber();
            }
        }

        // 如果没有主要号码，返回第一个
        return phones.get(0).getNumber();
    }

    /**
     * 获取主要邮箱地址
     */
    public String getPrimaryEmailAddress() {
        if (emails == null || emails.isEmpty()) {
            return null;
        }

        // 首先查找标记为主要的邮箱
        for (EmailInfo email : emails) {
            if (email.isPrimary()) {
                return email.getAddress();
            }
        }

        // 如果没有主要邮箱，返回第一个
        return emails.get(0).getAddress();
    }

    /**
     * 获取主要公司信息
     */
    public OrganizationInfo getPrimaryOrganization() {
        if (organizations == null || organizations.isEmpty()) {
            return null;
        }

        // 返回第一个组织信息
        return organizations.get(0);
    }

    /**
     * 添加电话号码
     */
    public void addPhone(PhoneInfo phone) {
        if (phone != null) {
            this.phones.add(phone);
        }
    }

    /**
     * 添加邮箱地址
     */
    public void addEmail(EmailInfo email) {
        if (email != null) {
            this.emails.add(email);
        }
    }

    /**
     * 添加地址
     */
    public void addAddress(AddressInfo address) {
        if (address != null) {
            this.addresses.add(address);
        }
    }

    /**
     * 添加组织信息
     */
    public void addOrganization(OrganizationInfo organization) {
        if (organization != null) {
            this.organizations.add(organization);
        }
    }

    /**
     * 是否有电话号码
     */
    public boolean hasPhoneNumber() {
        return phones != null && !phones.isEmpty();
    }

    /**
     * 是否有邮箱地址
     */
    public boolean hasEmailAddress() {
        return emails != null && !emails.isEmpty();
    }

    /**
     * 是否有地址信息
     */
    public boolean hasAddress() {
        return addresses != null && !addresses.isEmpty();
    }

    /**
     * 是否有组织信息
     */
    public boolean hasOrganization() {
        return organizations != null && !organizations.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Contact contact = (Contact) o;

        return contactInfo != null ? contactInfo.equals(contact.contactInfo) : contact.contactInfo == null;
    }

    @Override
    public int hashCode() {
        return contactInfo != null ? contactInfo.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "contactInfo=" + contactInfo +
                ", phones=" + phones +
                ", emails=" + emails +
                ", addresses=" + addresses +
                ", organizations=" + organizations +
                '}';
    }
}