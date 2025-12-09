// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
package com.lynx.explorer.contacts.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 联系人搜索条件类
 */
public class ContactSearchCriteria {
    private String nameQuery;           // 姓名查询
    private String phoneQuery;          // 电话查询
    private String emailQuery;          // 邮箱查询
    private String companyQuery;        // 公司查询
    private List<Integer> phoneTypes;   // 电话类型筛选
    private List<Integer> emailTypes;   // 邮箱类型筛选
    private boolean starredOnly;        // 仅星标联系人
    private boolean hasPhoneNumber;     // 有电话号码
    private boolean hasEmail;           // 有邮箱地址
    private long modifiedAfter;         // 修改时间筛选
    private String sortOrder;           // 排序字段
    private boolean ascending;          // 是否升序

    public ContactSearchCriteria() {
        this.phoneTypes = new ArrayList<>();
        this.emailTypes = new ArrayList<>();
        this.starredOnly = false;
        this.hasPhoneNumber = false;
        this.hasEmail = false;
        this.modifiedAfter = 0;
        this.sortOrder = "display_name";
        this.ascending = true;
    }

    // Builder模式支持
    public static class Builder {
        private ContactSearchCriteria criteria = new ContactSearchCriteria();

        public Builder nameQuery(String nameQuery) {
            criteria.nameQuery = nameQuery;
            return this;
        }

        public Builder phoneQuery(String phoneQuery) {
            criteria.phoneQuery = phoneQuery;
            return this;
        }

        public Builder emailQuery(String emailQuery) {
            criteria.emailQuery = emailQuery;
            return this;
        }

        public Builder companyQuery(String companyQuery) {
            criteria.companyQuery = companyQuery;
            return this;
        }

        public Builder addPhoneType(int phoneType) {
            criteria.phoneTypes.add(phoneType);
            return this;
        }

        public Builder addEmailType(int emailType) {
            criteria.emailTypes.add(emailType);
            return this;
        }

        public Builder starredOnly(boolean starredOnly) {
            criteria.starredOnly = starredOnly;
            return this;
        }

        public Builder hasPhoneNumber(boolean hasPhoneNumber) {
            criteria.hasPhoneNumber = hasPhoneNumber;
            return this;
        }

        public Builder hasEmail(boolean hasEmail) {
            criteria.hasEmail = hasEmail;
            return this;
        }

        public Builder modifiedAfter(long modifiedAfter) {
            criteria.modifiedAfter = modifiedAfter;
            return this;
        }

        public Builder sortOrder(String sortOrder) {
            criteria.sortOrder = sortOrder;
            return this;
        }

        public Builder ascending(boolean ascending) {
            criteria.ascending = ascending;
            return this;
        }

        public ContactSearchCriteria build() {
            return criteria;
        }
    }

    // Getters and Setters
    public String getNameQuery() {
        return nameQuery;
    }

    public void setNameQuery(String nameQuery) {
        this.nameQuery = nameQuery;
    }

    public String getPhoneQuery() {
        return phoneQuery;
    }

    public void setPhoneQuery(String phoneQuery) {
        this.phoneQuery = phoneQuery;
    }

    public String getEmailQuery() {
        return emailQuery;
    }

    public void setEmailQuery(String emailQuery) {
        this.emailQuery = emailQuery;
    }

    public String getCompanyQuery() {
        return companyQuery;
    }

    public void setCompanyQuery(String companyQuery) {
        this.companyQuery = companyQuery;
    }

    public List<Integer> getPhoneTypes() {
        return phoneTypes;
    }

    public void setPhoneTypes(List<Integer> phoneTypes) {
        this.phoneTypes = phoneTypes != null ? phoneTypes : new ArrayList<Integer>();
    }

    public List<Integer> getEmailTypes() {
        return emailTypes;
    }

    public void setEmailTypes(List<Integer> emailTypes) {
        this.emailTypes = emailTypes != null ? emailTypes : new ArrayList<Integer>();
    }

    public boolean isStarredOnly() {
        return starredOnly;
    }

    public void setStarredOnly(boolean starredOnly) {
        this.starredOnly = starredOnly;
    }

    public boolean isHasPhoneNumber() {
        return hasPhoneNumber;
    }

    public void setHasPhoneNumber(boolean hasPhoneNumber) {
        this.hasPhoneNumber = hasPhoneNumber;
    }

    public boolean isHasEmail() {
        return hasEmail;
    }

    public void setHasEmail(boolean hasEmail) {
        this.hasEmail = hasEmail;
    }

    public long getModifiedAfter() {
        return modifiedAfter;
    }

    public void setModifiedAfter(long modifiedAfter) {
        this.modifiedAfter = modifiedAfter;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * 检查是否有任何查询条件
     */
    public boolean hasAnyQuery() {
        return (nameQuery != null && !nameQuery.isEmpty()) ||
               (phoneQuery != null && !phoneQuery.isEmpty()) ||
               (emailQuery != null && !emailQuery.isEmpty()) ||
               (companyQuery != null && !companyQuery.isEmpty()) ||
               starredOnly ||
               hasPhoneNumber ||
               hasEmail ||
               modifiedAfter > 0 ||
               !phoneTypes.isEmpty() ||
               !emailTypes.isEmpty();
    }

    /**
     * 创建简单的姓名搜索条件
     */
    public static ContactSearchCriteria createNameSearch(String name) {
        return new Builder()
                .nameQuery(name)
                .build();
    }

    /**
     * 创建简单的电话搜索条件
     */
    public static ContactSearchCriteria createPhoneSearch(String phone) {
        return new Builder()
                .phoneQuery(phone)
                .build();
    }

    /**
     * 创建简单的邮箱搜索条件
     */
    public static ContactSearchCriteria createEmailSearch(String email) {
        return new Builder()
                .emailQuery(email)
                .build();
    }

    /**
     * 创建星标联系人搜索条件
     */
    public static ContactSearchCriteria createStarredSearch() {
        return new Builder()
                .starredOnly(true)
                .build();
    }

    @Override
    public String toString() {
        return "ContactSearchCriteria{" +
                "nameQuery='" + nameQuery + '\'' +
                ", phoneQuery='" + phoneQuery + '\'' +
                ", emailQuery='" + emailQuery + '\'' +
                ", companyQuery='" + companyQuery + '\'' +
                ", phoneTypes=" + phoneTypes +
                ", emailTypes=" + emailTypes +
                ", starredOnly=" + starredOnly +
                ", hasPhoneNumber=" + hasPhoneNumber +
                ", hasEmail=" + hasEmail +
                ", modifiedAfter=" + modifiedAfter +
                ", sortOrder='" + sortOrder + '\'' +
                ", ascending=" + ascending +
                '}';
    }
}