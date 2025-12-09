// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
package com.lynx.explorer.contacts.cache;

import android.content.Context;
import android.util.Log;
import android.util.LruCache;

import com.lynx.explorer.contacts.model.Contact;
import com.lynx.explorer.contacts.model.ContactInfo;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 联系人缓存管理类
 */
public class ContactsCache {
    private static final String TAG = "ContactsCache";
    private static final int MAX_MEMORY_CACHE_SIZE = 100; // 内存缓存最大数量
    private static final long CACHE_EXPIRY_TIME = 30 * 60 * 1000; // 30分钟过期时间
    
    private final LruCache<String, Contact> mMemoryCache;
    private final DiskCache mDiskCache;
    private final ConcurrentHashMap<String, Long> mCacheTimestamps;
    
    public ContactsCache(Context context) {
        mMemoryCache = new LruCache<String, Contact>(MAX_MEMORY_CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, Contact contact) {
                // 简单计算每个联系人的大小
                return 1;
            }
            
            @Override
            protected void entryRemoved(boolean evicted, String key, Contact oldValue, Contact newValue) {
                if (evicted) {
                    Log.d(TAG, "Contact evicted from memory cache: " + key);
                    // 被淘汰时保存到磁盘缓存
                    mDiskCache.put(key, oldValue);
                }
            }
        };
        
        mDiskCache = new DiskCache(context);
        mCacheTimestamps = new ConcurrentHashMap<>();
    }
    
    /**
     * 从缓存获取联系人
     */
    public Contact get(String contactId) {
        if (contactId == null) {
            return null;
        }
        
        // 先检查内存缓存
        Contact contact = mMemoryCache.get(contactId);
        if (contact != null) {
            Long timestamp = mCacheTimestamps.get(contactId);
            if (timestamp != null && !isCacheExpired(timestamp)) {
                Log.d(TAG, "Contact found in memory cache: " + contactId);
                return contact;
            } else {
                // 缓存已过期，移除
                mMemoryCache.remove(contactId);
                mCacheTimestamps.remove(contactId);
            }
        }
        
        // 再检查磁盘缓存
        contact = mDiskCache.get(contactId);
        if (contact != null) {
            Long timestamp = mCacheTimestamps.get(contactId);
            if (timestamp != null && !isCacheExpired(timestamp)) {
                Log.d(TAG, "Contact found in disk cache: " + contactId);
                // 重新放入内存缓存
                mMemoryCache.put(contactId, contact);
                return contact;
            } else {
                // 缓存已过期，移除
                mDiskCache.remove(contactId);
                mCacheTimestamps.remove(contactId);
            }
        }
        
        return null;
    }
    
    /**
     * 将联系人放入缓存
     */
    public void put(String contactId, Contact contact) {
        if (contactId == null || contact == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        // 放入内存缓存
        mMemoryCache.put(contactId, contact);
        
        // 放入磁盘缓存
        mDiskCache.put(contactId, contact);
        
        // 更新时间戳
        mCacheTimestamps.put(contactId, currentTime);
        
        Log.d(TAG, "Contact cached: " + contactId);
    }
    
    /**
     * 移除指定联系人缓存
     */
    public void remove(String contactId) {
        if (contactId == null) {
            return;
        }
        
        mMemoryCache.remove(contactId);
        mDiskCache.remove(contactId);
        mCacheTimestamps.remove(contactId);
        
        Log.d(TAG, "Contact removed from cache: " + contactId);
    }
    
    /**
     * 清空所有缓存
     */
    public void clear() {
        mMemoryCache.evictAll();
        mDiskCache.clear();
        mCacheTimestamps.clear();
        
        Log.d(TAG, "All contacts cache cleared");
    }
    
    /**
     * 获取内存缓存大小
     */
    public int getMemoryCacheSize() {
        return mMemoryCache.size();
    }
    
    /**
     * 获取磁盘缓存大小
     */
    public long getDiskCacheSize() {
        return mDiskCache.getSize();
    }
    
    /**
     * 检查缓存是否过期
     */
    private boolean isCacheExpired(long timestamp) {
        return (System.currentTimeMillis() - timestamp) > CACHE_EXPIRY_TIME;
    }
    
    /**
     * 批量获取联系人
     */
    public void getContacts(List<String> contactIds, ContactsCacheCallback callback) {
        if (contactIds == null || callback == null) {
            return;
        }
        
        for (String contactId : contactIds) {
            Contact contact = get(contactId);
            if (contact != null) {
                callback.onContactLoaded(contactId, contact);
            } else {
                callback.onContactMiss(contactId);
            }
        }
    }
    
    /**
     * 批量缓存联系人
     */
    public void putContacts(List<Contact> contacts) {
        if (contacts == null) {
            return;
        }
        
        for (Contact contact : contacts) {
            if (contact != null && contact.getId() != null) {
                put(contact.getId(), contact);
            }
        }
    }
    
    /**
     * 缓存回调接口
     */
    public interface ContactsCacheCallback {
        void onContactLoaded(String contactId, Contact contact);
        void onContactMiss(String contactId);
    }
    
    /**
     * 获取缓存统计信息
     */
    public CacheStats getCacheStats() {
        return new CacheStats(
                mMemoryCache.size(),
                MAX_MEMORY_CACHE_SIZE,
                mDiskCache.getSize(),
                mCacheTimestamps.size()
        );
    }
    
    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        public final int memoryCacheSize;
        public final int memoryCacheMaxSize;
        public final long diskCacheSize;
        public final int timestampCount;
        
        public CacheStats(int memoryCacheSize, int memoryCacheMaxSize, 
                       long diskCacheSize, int timestampCount) {
            this.memoryCacheSize = memoryCacheSize;
            this.memoryCacheMaxSize = memoryCacheMaxSize;
            this.diskCacheSize = diskCacheSize;
            this.timestampCount = timestampCount;
        }
        
        @Override
        public String toString() {
            return "CacheStats{" +
                    "memoryCacheSize=" + memoryCacheSize +
                    ", memoryCacheMaxSize=" + memoryCacheMaxSize +
                    ", diskCacheSize=" + diskCacheSize +
                    ", timestampCount=" + timestampCount +
                    '}';
        }
    }
}