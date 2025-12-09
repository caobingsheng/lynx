// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
package com.lynx.explorer.contacts.cache;

import android.content.Context;
import android.util.Log;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.lynx.explorer.contacts.model.Contact;
import com.lynx.explorer.contacts.model.ContactInfo;
import com.lynx.explorer.contacts.model.PhoneInfo;
import com.lynx.explorer.contacts.model.EmailInfo;
import com.lynx.explorer.contacts.model.AddressInfo;
import com.lynx.explorer.contacts.model.OrganizationInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 磁盘缓存实现类
 */
public class DiskCache {
    private static final String TAG = "DiskCache";
    private static final String CACHE_DIR_NAME = "contacts_cache";
    private static final String CACHE_FILE_PREFIX = "contact_";
    private static final String CACHE_FILE_SUFFIX = ".json";
    
    private final File mCacheDir;
    private final ConcurrentHashMap<String, Contact> mMemoryMap;
    private long mCacheSize;
    
    public DiskCache(Context context) {
        mCacheDir = new File(context.getCacheDir(), CACHE_DIR_NAME);
        if (!mCacheDir.exists()) {
            mCacheDir.mkdirs();
        }
        mMemoryMap = new ConcurrentHashMap<>();
        mCacheSize = 0;
        calculateCacheSize();
    }
    
    /**
     * 从磁盘缓存获取联系人
     */
    public Contact get(String contactId) {
        if (contactId == null) {
            return null;
        }
        
        // 先检查内存中的缓存
        Contact contact = mMemoryMap.get(contactId);
        if (contact != null) {
            return contact;
        }
        
        File cacheFile = getCacheFile(contactId);
        if (!cacheFile.exists()) {
            return null;
        }
        
        try {
            Contact cachedContact = readContactFromFile(cacheFile);
            if (cachedContact != null) {
                // 放入内存缓存
                mMemoryMap.put(contactId, cachedContact);
                Log.d(TAG, "Contact loaded from disk cache: " + contactId);
            }
            return cachedContact;
        } catch (Exception e) {
            Log.e(TAG, "Error reading contact from disk cache: " + contactId, e);
            // 删除损坏的缓存文件
            cacheFile.delete();
        }
        
        return null;
    }
    
    /**
     * 将联系人保存到磁盘缓存
     */
    public void put(String contactId, Contact contact) {
        if (contactId == null || contact == null) {
            return;
        }
        
        try {
            File cacheFile = getCacheFile(contactId);
            writeContactToFile(contact, cacheFile);
            
            // 更新内存缓存
            mMemoryMap.put(contactId, contact);
            
            // 更新缓存大小
            long fileSize = cacheFile.length();
            mCacheSize += fileSize;
            
            Log.d(TAG, "Contact saved to disk cache: " + contactId + ", size: " + fileSize);
        } catch (Exception e) {
            Log.e(TAG, "Error writing contact to disk cache: " + contactId, e);
        }
    }
    
    /**
     * 移除指定联系人的缓存
     */
    public void remove(String contactId) {
        if (contactId == null) {
            return;
        }
        
        // 从内存缓存移除
        Contact removedContact = mMemoryMap.remove(contactId);
        
        // 删除磁盘缓存文件
        File cacheFile = getCacheFile(contactId);
        if (cacheFile.exists()) {
            long fileSize = cacheFile.length();
            if (cacheFile.delete()) {
                mCacheSize -= fileSize;
                Log.d(TAG, "Contact removed from disk cache: " + contactId);
            } else {
                Log.w(TAG, "Failed to delete cache file: " + contactId);
            }
        }
    }
    
    /**
     * 清空所有缓存
     */
    public void clear() {
        // 清空内存缓存
        mMemoryMap.clear();
        
        // 删除所有磁盘缓存文件
        File[] cacheFiles = mCacheDir.listFiles();
        if (cacheFiles != null) {
            for (File file : cacheFiles) {
                if (file.delete()) {
                    Log.d(TAG, "Cache file deleted: " + file.getName());
                } else {
                    Log.w(TAG, "Failed to delete cache file: " + file.getName());
                }
            }
        }
        
        mCacheSize = 0;
        Log.d(TAG, "All disk cache cleared");
    }
    
    /**
     * 获取缓存大小
     */
    public long getSize() {
        return mCacheSize;
    }
    
    /**
     * 获取缓存文件数量
     */
    public int getFileCount() {
        File[] cacheFiles = mCacheDir.listFiles();
        return cacheFiles != null ? cacheFiles.length : 0;
    }
    
    /**
     * 获取缓存文件
     */
    private File getCacheFile(String contactId) {
        return new File(mCacheDir, CACHE_FILE_PREFIX + contactId + CACHE_FILE_SUFFIX);
    }
    
    /**
     * 从文件读取联系人
     */
    private Contact readContactFromFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             JsonReader reader = new JsonReader(isr)) {
            
            reader.beginObject();
            Contact contact = new Contact();
            
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case "contactInfo":
                        contact.setContactInfo(readContactInfo(reader));
                        break;
                    case "phones":
                        contact.setPhones(readPhoneList(reader));
                        break;
                    case "emails":
                        contact.setEmails(readEmailList(reader));
                        break;
                    case "addresses":
                        contact.setAddresses(readAddressList(reader));
                        break;
                    case "organizations":
                        contact.setOrganizations(readOrganizationList(reader));
                        break;
                }
            }
            reader.endObject();
            
            return contact;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing contact from JSON", e);
            throw new IOException("Failed to parse contact from JSON", e);
        }
    }
    
    /**
     * 将联系人写入文件
     */
    private void writeContactToFile(Contact contact, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             JsonWriter writer = new JsonWriter(osw)) {
            
            writer.beginObject();
            
            // 写入基本信息
            ContactInfo contactInfo = contact.getContactInfo();
            if (contactInfo != null) {
                writer.name("contactInfo");
                writeContactInfo(writer, contactInfo);
            }
            
            // 写入电话号码
            if (contact.getPhones() != null && !contact.getPhones().isEmpty()) {
                writer.name("phones");
                writer.beginArray();
                for (PhoneInfo phone : contact.getPhones()) {
                    writePhoneInfo(writer, phone);
                }
                writer.endArray();
            }
            
            // 写入邮箱地址
            if (contact.getEmails() != null && !contact.getEmails().isEmpty()) {
                writer.name("emails");
                writer.beginArray();
                for (EmailInfo email : contact.getEmails()) {
                    writeEmailInfo(writer, email);
                }
                writer.endArray();
            }
            
            // 写入地址
            if (contact.getAddresses() != null && !contact.getAddresses().isEmpty()) {
                writer.name("addresses");
                writer.beginArray();
                for (AddressInfo address : contact.getAddresses()) {
                    writeAddressInfo(writer, address);
                }
                writer.endArray();
            }
            
            // 写入组织信息
            if (contact.getOrganizations() != null && !contact.getOrganizations().isEmpty()) {
                writer.name("organizations");
                writer.beginArray();
                for (OrganizationInfo organization : contact.getOrganizations()) {
                    writeOrganizationInfo(writer, organization);
                }
                writer.endArray();
            }
            
            writer.endObject();
        }
    }
    
    /**
     * 计算缓存总大小
     */
    private void calculateCacheSize() {
        File[] cacheFiles = mCacheDir.listFiles();
        if (cacheFiles == null) {
            mCacheSize = 0;
            return;
        }
        
        long totalSize = 0;
        for (File file : cacheFiles) {
            totalSize += file.length();
        }
        mCacheSize = totalSize;
    }
    
    // 简化的JSON读写方法（实际项目中可以使用更完整的JSON库）
    private void writeContactInfo(JsonWriter writer, ContactInfo contactInfo) throws IOException {
        // 简化实现，实际应该使用更完整的JSON序列化
        writer.beginObject();
        writer.name("id").value(contactInfo.getId());
        writer.name("displayName").value(contactInfo.getDisplayName());
        writer.name("photoUri").value(contactInfo.getPhotoUri());
        writer.name("starred").value(contactInfo.isStarred());
        writer.name("timesContacted").value(contactInfo.getTimesContacted());
        writer.name("lastTimeContacted").value(contactInfo.getLastTimeContacted());
        writer.name("lastModifiedTime").value(contactInfo.getLastModifiedTime());
        writer.endObject();
    }
    
    private void writePhoneInfo(JsonWriter writer, PhoneInfo phone) throws IOException {
        writer.beginObject();
        writer.name("number").value(phone.getNumber());
        writer.name("type").value(phone.getType());
        writer.name("label").value(phone.getLabel());
        writer.name("isPrimary").value(phone.isPrimary());
        writer.endObject();
    }
    
    private void writeEmailInfo(JsonWriter writer, EmailInfo email) throws IOException {
        writer.beginObject();
        writer.name("address").value(email.getAddress());
        writer.name("type").value(email.getType());
        writer.name("label").value(email.getLabel());
        writer.name("isPrimary").value(email.isPrimary());
        writer.endObject();
    }
    
    private void writeAddressInfo(JsonWriter writer, AddressInfo address) throws IOException {
        writer.beginObject();
        writer.name("street").value(address.getStreet());
        writer.name("city").value(address.getCity());
        writer.name("state").value(address.getState());
        writer.name("postalCode").value(address.getPostalCode());
        writer.name("country").value(address.getCountry());
        writer.name("formattedAddress").value(address.getFormattedAddress());
        writer.name("type").value(address.getType());
        writer.name("label").value(address.getLabel());
        writer.endObject();
    }
    
    private void writeOrganizationInfo(JsonWriter writer, OrganizationInfo organization) throws IOException {
        writer.beginObject();
        writer.name("company").value(organization.getCompany());
        writer.name("title").value(organization.getTitle());
        writer.name("department").value(organization.getDepartment());
        writer.name("jobDescription").value(organization.getJobDescription());
        writer.name("officeLocation").value(organization.getOfficeLocation());
        writer.name("type").value(organization.getType());
        writer.name("label").value(organization.getLabel());
        writer.endObject();
    }
    
    // 简化的解析方法（实际项目中应该使用更完整的JSON解析库）
    private ContactInfo readContactInfo(JsonReader reader) throws IOException {
        ContactInfo contactInfo = new ContactInfo();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "id":
                    contactInfo.setId(reader.nextString());
                    break;
                case "displayName":
                    contactInfo.setDisplayName(reader.nextString());
                    break;
                case "photoUri":
                    contactInfo.setPhotoUri(reader.nextString());
                    break;
                case "starred":
                    contactInfo.setStarred(reader.nextBoolean());
                    break;
                case "timesContacted":
                    contactInfo.setTimesContacted(reader.nextInt());
                    break;
                case "lastTimeContacted":
                    contactInfo.setLastTimeContacted(reader.nextLong());
                    break;
                case "lastModifiedTime":
                    contactInfo.setLastModifiedTime(reader.nextLong());
                    break;
            }
        }
        reader.endObject();
        return contactInfo;
    }
    
    private List<PhoneInfo> readPhoneList(JsonReader reader) throws IOException {
        List<PhoneInfo> phones = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            phones.add(readPhoneInfo(reader));
        }
        reader.endArray();
        return phones;
    }
    
    private PhoneInfo readPhoneInfo(JsonReader reader) throws IOException {
        PhoneInfo phone = new PhoneInfo();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "number":
                    phone.setNumber(reader.nextString());
                    break;
                case "type":
                    phone.setType(reader.nextInt());
                    break;
                case "label":
                    phone.setLabel(reader.nextString());
                    break;
                case "isPrimary":
                    phone.setPrimary(reader.nextBoolean());
                    break;
            }
        }
        reader.endObject();
        return phone;
    }
    
    private List<EmailInfo> readEmailList(JsonReader reader) throws IOException {
        List<EmailInfo> emails = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            emails.add(readEmailInfo(reader));
        }
        reader.endArray();
        return emails;
    }
    
    private EmailInfo readEmailInfo(JsonReader reader) throws IOException {
        EmailInfo email = new EmailInfo();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "address":
                    email.setAddress(reader.nextString());
                    break;
                case "type":
                    email.setType(reader.nextInt());
                    break;
                case "label":
                    email.setLabel(reader.nextString());
                    break;
                case "isPrimary":
                    email.setPrimary(reader.nextBoolean());
                    break;
            }
        }
        reader.endObject();
        return email;
    }
    
    private List<AddressInfo> readAddressList(JsonReader reader) throws IOException {
        List<AddressInfo> addresses = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            addresses.add(readAddressInfo(reader));
        }
        reader.endArray();
        return addresses;
    }
    
    private AddressInfo readAddressInfo(JsonReader reader) throws IOException {
        AddressInfo address = new AddressInfo();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "street":
                    address.setStreet(reader.nextString());
                    break;
                case "city":
                    address.setCity(reader.nextString());
                    break;
                case "state":
                    address.setState(reader.nextString());
                    break;
                case "postalCode":
                    address.setPostalCode(reader.nextString());
                    break;
                case "country":
                    address.setCountry(reader.nextString());
                    break;
                case "formattedAddress":
                    address.setFormattedAddress(reader.nextString());
                    break;
                case "type":
                    address.setType(reader.nextInt());
                    break;
                case "label":
                    address.setLabel(reader.nextString());
                    break;
            }
        }
        reader.endObject();
        return address;
    }
    
    private List<OrganizationInfo> readOrganizationList(JsonReader reader) throws IOException {
        List<OrganizationInfo> organizations = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            organizations.add(readOrganizationInfo(reader));
        }
        reader.endArray();
        return organizations;
    }
    
    private OrganizationInfo readOrganizationInfo(JsonReader reader) throws IOException {
        OrganizationInfo organization = new OrganizationInfo();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "company":
                    organization.setCompany(reader.nextString());
                    break;
                case "title":
                    organization.setTitle(reader.nextString());
                    break;
                case "department":
                    organization.setDepartment(reader.nextString());
                    break;
                case "jobDescription":
                    organization.setJobDescription(reader.nextString());
                    break;
                case "officeLocation":
                    organization.setOfficeLocation(reader.nextString());
                    break;
                case "type":
                    organization.setType(reader.nextInt());
                    break;
                case "label":
                    organization.setLabel(reader.nextString());
                    break;
            }
        }
        reader.endObject();
        return organization;
    }
}