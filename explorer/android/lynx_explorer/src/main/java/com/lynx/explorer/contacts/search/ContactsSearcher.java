// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
package com.lynx.explorer.contacts.search;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.lynx.explorer.contacts.cache.ContactsCache;
import com.lynx.explorer.contacts.model.Contact;
import com.lynx.explorer.contacts.model.ContactInfo;
import com.lynx.explorer.contacts.model.PhoneInfo;
import com.lynx.explorer.contacts.model.EmailInfo;
import com.lynx.explorer.contacts.model.AddressInfo;
import com.lynx.explorer.contacts.model.OrganizationInfo;
import com.lynx.explorer.contacts.model.ContactSearchCriteria;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 联系人搜索功能实现
 */
public class ContactsSearcher {
    private static final String TAG = "ContactsSearcher";
    private final Context mContext;
    private final ContactsCache mCache;
    
    public ContactsSearcher(Context context) {
        mContext = context;
        mCache = new ContactsCache(context);
    }
    
    /**
     * 根据搜索条件搜索联系人
     */
    public List<Contact> searchContacts(ContactSearchCriteria criteria) {
        if (criteria == null || !criteria.hasAnyQuery()) {
            return new ArrayList<>();
        }
        
        List<Contact> results = new ArrayList<>();
        
        try {
            // 构建查询条件
            StringBuilder selection = new StringBuilder();
            List<String> selectionArgs = new ArrayList<>();
            
            // 姓名查询
            if (criteria.getNameQuery() != null && !criteria.getNameQuery().isEmpty()) {
                addNameQueryCondition(selection, selectionArgs, criteria.getNameQuery());
            }
            
            // 电话查询
            if (criteria.getPhoneQuery() != null && !criteria.getPhoneQuery().isEmpty()) {
                addPhoneQueryCondition(selection, selectionArgs, criteria.getPhoneQuery());
            }
            
            // 邮箱查询
            if (criteria.getEmailQuery() != null && !criteria.getEmailQuery().isEmpty()) {
                addEmailQueryCondition(selection, selectionArgs, criteria.getEmailQuery());
            }
            
            // 公司查询
            if (criteria.getCompanyQuery() != null && !criteria.getCompanyQuery().isEmpty()) {
                addCompanyQueryCondition(selection, selectionArgs, criteria.getCompanyQuery());
            }
            
            // 星标联系人
            if (criteria.isStarredOnly()) {
                addStarredCondition(selection, selectionArgs);
            }
            
            // 有电话号码
            if (criteria.isHasPhoneNumber()) {
                addHasPhoneCondition(selection, selectionArgs);
            }
            
            // 有邮箱地址
            if (criteria.isHasEmail()) {
                addHasEmailCondition(selection, selectionArgs);
            }
            
            // 修改时间
            if (criteria.getModifiedAfter() > 0) {
                addModifiedAfterCondition(selection, selectionArgs, criteria.getModifiedAfter());
            }
            
            // 电话类型筛选
            if (!criteria.getPhoneTypes().isEmpty()) {
                addPhoneTypeCondition(selection, selectionArgs, criteria.getPhoneTypes());
            }
            
            // 邮箱类型筛选
            if (!criteria.getEmailTypes().isEmpty()) {
                addEmailTypeCondition(selection, selectionArgs, criteria.getEmailTypes());
            }
            
            // 构建排序条件
            String sortOrder = buildSortOrder(criteria);
            
            // 执行查询
            String selectionStr = selection.length() > 0 ? selection.toString() : null;
            String[] selectionArgsArray = selectionArgs.toArray(new String[0]);
            
            List<ContactInfo> contactInfos = queryContactInfos(selectionStr, selectionArgsArray, sortOrder);
            
            // 获取详细联系人信息
            for (ContactInfo contactInfo : contactInfos) {
                Contact contact = getContactDetails(contactInfo.getId());
                if (contact != null && matchesSearchCriteria(contact, criteria)) {
                    results.add(contact);
                    // 缓存搜索结果
                    mCache.put(contactInfo.getId(), contact);
                }
            }
            
            Log.d(TAG, "Search completed, found " + results.size() + " contacts");
            
        } catch (Exception e) {
            Log.e(TAG, "Error searching contacts", e);
        }
        
        return results;
    }
    
    /**
     * 按姓名搜索联系人
     */
    public List<Contact> searchContactsByName(String nameQuery) {
        ContactSearchCriteria criteria = new ContactSearchCriteria.Builder()
                .nameQuery(nameQuery)
                .build();
        return searchContacts(criteria);
    }
    
    /**
     * 按电话号码搜索联系人
     */
    public List<Contact> searchContactsByPhone(String phoneQuery) {
        ContactSearchCriteria criteria = new ContactSearchCriteria.Builder()
                .phoneQuery(phoneQuery)
                .build();
        return searchContacts(criteria);
    }
    
    /**
     * 按邮箱搜索联系人
     */
    public List<Contact> searchContactsByEmail(String emailQuery) {
        ContactSearchCriteria criteria = new ContactSearchCriteria.Builder()
                .emailQuery(emailQuery)
                .build();
        return searchContacts(criteria);
    }
    
    /**
     * 获取星标联系人
     */
    public List<Contact> getStarredContacts() {
        ContactSearchCriteria criteria = new ContactSearchCriteria.Builder()
                .starredOnly(true)
                .build();
        return searchContacts(criteria);
    }
    
    /**
     * 获取常用联系人（按联系频率排序）
     */
    public List<Contact> getFrequentContacts(int limit) {
        List<Contact> results = new ArrayList<>();
        
        try {
            String sortOrder = ContactsContract.Contacts.TIMES_CONTACTED + " DESC";
            String selection = ContactsContract.Contacts.TIMES_CONTACTED + " > 0";
            
            List<ContactInfo> contactInfos = queryContactInfos(selection, null, sortOrder);
            
            // 获取详细联系人信息
            int count = 0;
            for (ContactInfo contactInfo : contactInfos) {
                if (count >= limit) {
                    break;
                }
                
                Contact contact = getContactDetails(contactInfo.getId());
                if (contact != null) {
                    results.add(contact);
                    // 缓存常用联系人
                    mCache.put(contactInfo.getId(), contact);
                    count++;
                }
            }
            
            Log.d(TAG, "Frequent contacts loaded: " + results.size());
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting frequent contacts", e);
        }
        
        return results;
    }
    
    /**
     * 查询联系人基本信息
     */
    private List<ContactInfo> queryContactInfos(String selection, String[] selectionArgs, String sortOrder) {
        List<ContactInfo> contactInfos = new ArrayList<>();
        
        try (Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.Contacts.PHOTO_URI,
                        ContactsContract.Contacts.STARRED,
                        ContactsContract.Contacts.LAST_TIME_CONTACTED,
                        ContactsContract.Contacts.TIMES_CONTACTED,
                        ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP
                },
                selection,
                selectionArgs,
                sortOrder)) {
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ContactInfo contactInfo = createContactInfoFromCursor(cursor);
                    if (contactInfo != null) {
                        contactInfos.add(contactInfo);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying contact infos", e);
        }
        
        return contactInfos;
    }
    
    /**
     * 从Cursor创建ContactInfo
     */
    private ContactInfo createContactInfoFromCursor(Cursor cursor) {
        try {
            ContactInfo contactInfo = new ContactInfo();
            
            contactInfo.setId(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)));
            
            int displayNameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
            if (displayNameIndex >= 0) {
                String displayName = cursor.getString(displayNameIndex);
                contactInfo.setDisplayName(displayName != null ? displayName : "");
            }
            
            int photoUriIndex = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI);
            if (photoUriIndex >= 0) {
                String photoUri = cursor.getString(photoUriIndex);
                contactInfo.setPhotoUri(photoUri);
            }
            
            int starredIndex = cursor.getColumnIndex(ContactsContract.Contacts.STARRED);
            if (starredIndex >= 0) {
                contactInfo.setStarred(cursor.getInt(starredIndex) == 1);
            }
            
            int timesContactedIndex = cursor.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED);
            if (timesContactedIndex >= 0) {
                contactInfo.setTimesContacted(cursor.getInt(timesContactedIndex));
            }
            
            int lastTimeContactedIndex = cursor.getColumnIndex(ContactsContract.Contacts.LAST_TIME_CONTACTED);
            if (lastTimeContactedIndex >= 0) {
                contactInfo.setLastTimeContacted(cursor.getLong(lastTimeContactedIndex));
            }
            
            int lastModifiedIndex = cursor.getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP);
            if (lastModifiedIndex >= 0) {
                contactInfo.setLastModifiedTime(cursor.getLong(lastModifiedIndex));
            }
            
            return contactInfo;
        } catch (Exception e) {
            Log.e(TAG, "Error creating contact info from cursor", e);
            return null;
        }
    }
    
    /**
     * 获取联系人详细信息
     */
    private Contact getContactDetails(String contactId) {
        // 先从缓存获取
        Contact contact = mCache.get(contactId);
        if (contact != null) {
            return contact;
        }
        
        // 从数据库获取详细信息
        // 这里应该调用ContactsProvider来获取详细信息
        // 为了简化，这里直接返回基本信息
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setId(contactId);
        contactInfo.setDisplayName("Contact " + contactId);
        
        return new Contact(contactInfo);
    }
    
    /**
     * 检查联系人是否匹配搜索条件
     */
    private boolean matchesSearchCriteria(Contact contact, ContactSearchCriteria criteria) {
        ContactInfo info = contact.getContactInfo();
        if (info == null) {
            return false;
        }
        
        // 检查姓名匹配
        if (criteria.getNameQuery() != null && !criteria.getNameQuery().isEmpty()) {
            if (!matchesName(info.getDisplayName(), criteria.getNameQuery())) {
                return false;
            }
        }
        
        // 检查电话匹配
        if (criteria.getPhoneQuery() != null && !criteria.getPhoneQuery().isEmpty()) {
            if (!matchesPhone(contact.getPhones(), criteria.getPhoneQuery())) {
                return false;
            }
        }
        
        // 检查邮箱匹配
        if (criteria.getEmailQuery() != null && !criteria.getEmailQuery().isEmpty()) {
            if (!matchesEmail(contact.getEmails(), criteria.getEmailQuery())) {
                return false;
            }
        }
        
        // 检查公司匹配
        if (criteria.getCompanyQuery() != null && !criteria.getCompanyQuery().isEmpty()) {
            if (!matchesCompany(contact.getOrganizations(), criteria.getCompanyQuery())) {
                return false;
            }
        }
        
        // 检查星标条件
        if (criteria.isStarredOnly() && !info.isStarred()) {
            return false;
        }
        
        // 检查有电话条件
        if (criteria.isHasPhoneNumber() && !contact.hasPhoneNumber()) {
            return false;
        }
        
        // 检查有邮箱条件
        if (criteria.isHasEmail() && !contact.hasEmailAddress()) {
            return false;
        }
        
        // 检查电话类型
        if (!criteria.getPhoneTypes().isEmpty()) {
            if (!matchesPhoneTypes(contact.getPhones(), criteria.getPhoneTypes())) {
                return false;
            }
        }
        
        // 检查邮箱类型
        if (!criteria.getEmailTypes().isEmpty()) {
            if (!matchesEmailTypes(contact.getEmails(), criteria.getEmailTypes())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 检查姓名匹配
     */
    private boolean matchesName(String displayName, String query) {
        if (displayName == null || query == null) {
            return false;
        }
        return displayName.toLowerCase().contains(query.toLowerCase());
    }
    
    /**
     * 检查电话匹配
     */
    private boolean matchesPhone(List<PhoneInfo> phones, String query) {
        if (phones == null || phones.isEmpty()) {
            return false;
        }
        
        for (PhoneInfo phone : phones) {
            if (phone.getNumber() != null && phone.getNumber().contains(query)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查邮箱匹配
     */
    private boolean matchesEmail(List<EmailInfo> emails, String query) {
        if (emails == null || emails.isEmpty()) {
            return false;
        }
        
        for (EmailInfo email : emails) {
            if (email.getAddress() != null && email.getAddress().contains(query)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查公司匹配
     */
    private boolean matchesCompany(List<OrganizationInfo> organizations, String query) {
        if (organizations == null || organizations.isEmpty()) {
            return false;
        }
        
        for (OrganizationInfo organization : organizations) {
            if (organization.getCompany() != null && organization.getCompany().contains(query)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查电话类型匹配
     */
    private boolean matchesPhoneTypes(List<PhoneInfo> phones, List<Integer> types) {
        Set<Integer> phoneTypes = new HashSet<>(types);
        
        for (PhoneInfo phone : phones) {
            if (phoneTypes.contains(phone.getType())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查邮箱类型匹配
     */
    private boolean matchesEmailTypes(List<EmailInfo> emails, List<Integer> types) {
        Set<Integer> emailTypes = new HashSet<>(types);
        
        for (EmailInfo email : emails) {
            if (emailTypes.contains(email.getType())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 添加姓名查询条件
     */
    private void addNameQueryCondition(StringBuilder selection, List<String> selectionArgs, String query) {
        if (selection.length() > 0) selection.append(" AND ");
        
        selection.append("(")
                .append(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                .append(" LIKE ? OR ")
                .append(ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE)
                .append(" LIKE ?)")
                .append(" COLLATE NOCASE");
        
        selectionArgs.add("%" + query + "%");
        selectionArgs.add("%" + query + "%");
    }
    
    /**
     * 添加电话查询条件
     */
    private void addPhoneQueryCondition(StringBuilder selection, List<String> selectionArgs, String query) {
        if (selection.length() > 0) selection.append(" AND ");
        
        selection.append("EXISTS (")
                .append(ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                .append(" WHERE ")
                .append(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                .append(" = ")
                .append(ContactsContract.Contacts._ID)
                .append(" AND ")
                .append(ContactsContract.CommonDataKinds.Phone.NUMBER)
                .append(" LIKE ?))");
        
        selectionArgs.add("%" + query + "%");
    }
    
    /**
     * 添加邮箱查询条件
     */
    private void addEmailQueryCondition(StringBuilder selection, List<String> selectionArgs, String query) {
        if (selection.length() > 0) selection.append(" AND ");
        
        selection.append("EXISTS (")
                .append(ContactsContract.CommonDataKinds.Email.CONTENT_URI)
                .append(" WHERE ")
                .append(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
                .append(" = ")
                .append(ContactsContract.Contacts._ID)
                .append(" AND ")
                .append(ContactsContract.CommonDataKinds.Email.ADDRESS)
                .append(" LIKE ?))");
        
        selectionArgs.add("%" + query + "%");
    }
    
    /**
     * 添加公司查询条件
     */
    private void addCompanyQueryCondition(StringBuilder selection, List<String> selectionArgs, String query) {
        if (selection.length() > 0) selection.append(" AND ");
        
        selection.append("EXISTS (")
                .append("SELECT 1 FROM ")
                .append(ContactsContract.Data.CONTENT_URI)
                .append(" WHERE ")
                .append(ContactsContract.CommonDataKinds.Organization.CONTACT_ID)
                .append(" = ")
                .append(ContactsContract.Contacts._ID)
                .append(" AND ")
                .append(ContactsContract.Data.MIMETYPE)
                .append(" = '")
                .append(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                .append("' AND ")
                .append(ContactsContract.CommonDataKinds.Organization.COMPANY)
                .append(" LIKE ?))");
        
        selectionArgs.add("%" + query + "%");
    }
    
    /**
     * 添加星标条件
     */
    private void addStarredCondition(StringBuilder selection, List<String> selectionArgs) {
        if (selection.length() > 0) selection.append(" AND ");
        selection.append(ContactsContract.Contacts.STARRED).append(" = 1");
    }
    
    /**
     * 添加有电话条件
     */
    private void addHasPhoneCondition(StringBuilder selection, List<String> selectionArgs) {
        if (selection.length() > 0) selection.append(" AND ");
        selection.append(ContactsContract.Contacts.HAS_PHONE_NUMBER).append(" = 1");
    }
    
    /**
     * 添加有邮箱条件
     */
    private void addHasEmailCondition(StringBuilder selection, List<String> selectionArgs) {
        if (selection.length() > 0) selection.append(" AND ");
        selection.append("EXISTS (")
                .append(ContactsContract.CommonDataKinds.Email.CONTENT_URI)
                .append(" WHERE ")
                .append(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
                .append(" = ")
                .append(ContactsContract.Contacts._ID)
                .append(")");
    }
    
    /**
     * 添加修改时间条件
     */
    private void addModifiedAfterCondition(StringBuilder selection, List<String> selectionArgs, long timestamp) {
        if (selection.length() > 0) selection.append(" AND ");
        selection.append(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP).append(" > ?");
        selectionArgs.add(String.valueOf(timestamp));
    }
    
    /**
     * 添加电话类型条件
     */
    private void addPhoneTypeCondition(StringBuilder selection, List<String> selectionArgs, List<Integer> types) {
        if (selection.length() > 0) selection.append(" AND ");
        
        selection.append("EXISTS (")
                .append(ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                .append(" WHERE ")
                .append(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                .append(" = ")
                .append(ContactsContract.Contacts._ID)
                .append(" AND ")
                .append(ContactsContract.CommonDataKinds.Phone.TYPE)
                .append(" IN (");
        
        for (int i = 0; i < types.size(); i++) {
            if (i > 0) selection.append(", ");
            selection.append("?");
            selectionArgs.add(String.valueOf(types.get(i)));
        }
        
        selection.append("))");
    }
    
    /**
     * 添加邮箱类型条件
     */
    private void addEmailTypeCondition(StringBuilder selection, List<String> selectionArgs, List<Integer> types) {
        if (selection.length() > 0) selection.append(" AND ");
        
        selection.append("EXISTS (")
                .append(ContactsContract.CommonDataKinds.Email.CONTENT_URI)
                .append(" WHERE ")
                .append(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
                .append(" = ")
                .append(ContactsContract.Contacts._ID)
                .append(" AND ")
                .append(ContactsContract.CommonDataKinds.Email.TYPE)
                .append(" IN (");
        
        for (int i = 0; i < types.size(); i++) {
            if (i > 0) selection.append(", ");
            selection.append("?");
            selectionArgs.add(String.valueOf(types.get(i)));
        }
        
        selection.append("))");
    }
    
    /**
     * 构建排序条件
     */
    private String buildSortOrder(ContactSearchCriteria criteria) {
        StringBuilder sortOrder = new StringBuilder();
        
        if (criteria.getSortOrder() != null) {
            sortOrder.append(criteria.getSortOrder());
        } else {
            sortOrder.append(ContactsContract.Contacts.DISPLAY_NAME);
        }
        
        if (!criteria.isAscending()) {
            sortOrder.append(" DESC");
        } else {
            sortOrder.append(" ASC");
        }
        
        return sortOrder.toString();
    }
}