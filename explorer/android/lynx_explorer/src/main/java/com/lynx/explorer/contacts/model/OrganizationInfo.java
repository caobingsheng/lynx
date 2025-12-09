// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
package com.lynx.explorer.contacts.model;

/**
 * 组织信息类
 */
public class OrganizationInfo {
    // 组织类型常量，对应Android ContactsContract.CommonDataKinds.Organization
    public static final int TYPE_WORK = 1;
    public static final int TYPE_OTHER = 2;
    public static final int TYPE_CUSTOM = 3;

    private String company;             // 公司
    private String title;               // 职位
    private String department;            // 部门
    private String jobDescription;        // 职位描述
    private String officeLocation;        // 办公地点
    private int type;                  // 类型
    private String label;               // 自定义标签

    public OrganizationInfo() {
    }

    public OrganizationInfo(String company, String title) {
        this.company = company;
        this.title = title;
    }

    // Getters and Setters
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
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
     * 获取完整的职位信息
     */
    public String getFullTitle() {
        StringBuilder sb = new StringBuilder();
        
        if (title != null && !title.isEmpty()) {
            sb.append(title);
        }
        
        if (department != null && !department.isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(department);
        }
        
        if (company != null && !company.isEmpty()) {
            if (sb.length() > 0) sb.append(" @ ");
            sb.append(company);
        }
        
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrganizationInfo that = (OrganizationInfo) o;

        if (type != that.type) return false;
        
        if (company != null ? !company.equals(that.company) : that.company != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        
        return true;
    }

    @Override
    public int hashCode() {
        int result = company != null ? company.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + type;
        return result;
    }

    @Override
    public String toString() {
        return "OrganizationInfo{" +
                "company='" + company + '\'' +
                ", title='" + title + '\'' +
                ", department='" + department + '\'' +
                ", jobDescription='" + jobDescription + '\'' +
                ", officeLocation='" + officeLocation + '\'' +
                ", type=" + type +
                ", label='" + label + '\'' +
                '}';
    }
}