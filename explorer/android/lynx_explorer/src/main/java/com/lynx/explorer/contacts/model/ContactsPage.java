// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
package com.lynx.explorer.contacts.model;

import java.util.List;
import java.util.ArrayList;

/**
 * 联系人分页结果类
 */
public class ContactsPage {
    private List<Contact> contacts;
    private int totalCount;
    private int currentPage;
    private int pageSize;
    private boolean hasMore;
    private String nextCursor;  // 用于游标分页

    public ContactsPage() {
        this.contacts = new ArrayList<>();
        this.currentPage = 0;
        this.pageSize = 20;
        this.hasMore = false;
    }

    public ContactsPage(List<Contact> contacts, int totalCount, int currentPage, int pageSize) {
        this.contacts = contacts != null ? contacts : new ArrayList<Contact>();
        this.totalCount = totalCount;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.hasMore = (currentPage + 1) * pageSize < totalCount;
    }

    // Getters and Setters
    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts != null ? contacts : new ArrayList<Contact>();
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

    /**
     * 获取总页数
     */
    public int getTotalPages() {
        if (pageSize <= 0) return 0;
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    /**
     * 获取当前页的联系人数量
     */
    public int getCurrentPageSize() {
        return contacts != null ? contacts.size() : 0;
    }

    /**
     * 获取下一页页码
     */
    public int getNextPage() {
        return hasMore ? currentPage + 1 : currentPage;
    }

    /**
     * 获取上一页页码
     */
    public int getPreviousPage() {
        return currentPage > 0 ? currentPage - 1 : 0;
    }

    /**
     * 是否为第一页
     */
    public boolean isFirstPage() {
        return currentPage == 0;
    }

    /**
     * 是否为最后一页
     */
    public boolean isLastPage() {
        return !hasMore;
    }

    @Override
    public String toString() {
        return "ContactsPage{" +
                "contactsCount=" + (contacts != null ? contacts.size() : 0) +
                ", totalCount=" + totalCount +
                ", currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", hasMore=" + hasMore +
                ", nextCursor='" + nextCursor + '\'' +
                '}';
    }
}