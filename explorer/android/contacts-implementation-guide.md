# 通讯录模块重新实现指南

## 1. 项目结构

```
explorer/android/lynx_explorer/src/main/java/com/lynx/explorer/contacts/
├── model/                          # 数据模型
│   ├── ContactInfo.java
│   ├── PhoneInfo.java
│   ├── EmailInfo.java
│   ├── AddressInfo.java
│   ├── OrganizationInfo.java
│   ├── Contact.java
│   ├── ContactSearchCriteria.java
│   ├── ContactGroup.java
│   ├── ContactsPage.java
│   └── ContactStatistics.java
├── provider/                       # 数据提供者
│   ├── ContactsProvider.java
│   └── ContactsContractHelper.java
├── cache/                          # 缓存管理
│   ├── ContactsCache.java
│   ├── MemoryCache.java
│   └── DiskCache.java
├── database/                       # 本地数据库
│   ├── ContactsDatabase.java
│   ├── ContactsDao.java
│   └── DatabaseSchema.java
├── observer/                       # 数据变更监听
│   ├── ContactsChangeObserver.java
│   └── ContactsChangeListener.java
├── search/                         # 搜索功能
│   ├── ContactsSearcher.java
│   └── SearchFilter.java
├── group/                          # 分组功能
│   ├── ContactGrouper.java
│   └── GroupStrategy.java
├── sync/                           # 同步功能
│   ├── ContactsSyncManager.java
│   └── IncrementalSyncer.java
└── ContactsManager.java            # 主管理类
```

## 2. 核心实现类

### 2.1 ContactsProvider - 原生通讯录访问

```java
public class ContactsProvider {
    private static final String TAG = "ContactsProvider";
    private final Context mContext;
    
    // 联系人URI
    private static final Uri CONTACTS_URI = ContactsContract.Contacts.CONTENT_URI;
    private static final Uri PHONES_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    private static final Uri EMAILS_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
    private static final Uri ADDRESSES_URI = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI;
    private static final Uri ORGANIZATIONS_URI = ContactsContract.CommonDataKinds.Organization.CONTENT_URI;
    
    // 查询投影
    private static final String[] CONTACTS_PROJECTION = {
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.Contacts.PHOTO_URI,
        ContactsContract.Contacts.HAS_PHONE_NUMBER,
        ContactsContract.Contacts.STARRED,
        ContactsContract.Contacts.LAST_TIME_CONTACTED,
        ContactsContract.Contacts.TIMES_CONTACTED,
        ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP
    };
    
    public ContactsProvider(Context context) {
        mContext = context;
    }
    
    // 获取联系人基本信息
    public List<ContactInfo> getContactInfos(int offset, int limit) {
        List<ContactInfo> contacts = new ArrayList<>();
        
        try (Cursor cursor = mContext.getContentResolver().query(
                CONTACTS_URI,
                CONTACTS_PROJECTION,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC LIMIT " + limit + " OFFSET " + offset)) {
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ContactInfo contact = new ContactInfo();
                    contact.setId(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)));
                    contact.setDisplayName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                    contact.setPhotoUri(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)));
                    contact.setStarred(cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.STARRED)) == 1);
                    contact.setTimesContacted(cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED)));
                    contact.setLastTimeContacted(cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts.LAST_TIME_CONTACTED)));
                    contact.setLastModifiedTime(cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)));
                    contacts.add(contact);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting contacts", e);
        }
        
        return contacts;
    }
    
    // 获取联系人详细信息
    public Contact getContactDetails(String contactId) {
        ContactInfo contactInfo = getContactInfo(contactId);
        if (contactInfo == null) {
            return null;
        }
        
        Contact contact = new Contact();
        contact.setContactInfo(contactInfo);
        contact.setPhones(getPhoneNumbers(contactId));
        contact.setEmails(getEmailAddresses(contactId));
        contact.setAddresses(getAddresses(contactId));
        contact.setOrganizations(getOrganizations(contactId));
        
        return contact;
    }
    
    // 获取电话号码
    public List<PhoneInfo> getPhoneNumbers(String contactId) {
        List<PhoneInfo> phones = new ArrayList<>();
        
        String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
        String[] selectionArgs = {contactId};
        
        try (Cursor cursor = mContext.getContentResolver().query(
                PHONES_URI,
                null,
                selection,
                selectionArgs,
                null)) {
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    PhoneInfo phone = new PhoneInfo();
                    phone.setNumber(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    phone.setType(cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)));
                    phone.setLabel(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)));
                    phone.setPrimary(cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY)) == 1);
                    phones.add(phone);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting phone numbers", e);
        }
        
        return phones;
    }
    
    // 类似方法实现getEmailAddresses, getAddresses, getOrganizations...
}
```

### 2.2 ContactsCache - 缓存管理

```java
public class ContactsCache {
    private static final String TAG = "ContactsCache";
    private static final int MAX_MEMORY_CACHE_SIZE = 100; // 最多缓存100个联系人
    private static final long CACHE_EXPIRY_TIME = 30 * 60 * 1000; // 30分钟过期
    
    private final LruCache<String, Contact> mMemoryCache;
    private final DiskCache mDiskCache;
    private final Context mContext;
    
    public ContactsCache(Context context) {
        mContext = context;
        mMemoryCache = new LruCache<String, Contact>(MAX_MEMORY_CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, Contact value) {
                return 1; // 简单计算，每个联系人算作1个单位
            }
            
            @Override
            protected void entryRemoved(boolean evicted, String key, Contact oldValue, Contact newValue) {
                if (evicted) {
                    // 被淘汰时保存到磁盘缓存
                    mDiskCache.put(key, oldValue);
                }
            }
        };
        mDiskCache = new DiskCache(context);
    }
    
    public Contact get(String contactId) {
        // 先从内存缓存获取
        Contact contact = mMemoryCache.get(contactId);
        if (contact != null) {
            return contact;
        }
        
        // 再从磁盘缓存获取
        contact = mDiskCache.get(contactId);
        if (contact != null) {
            // 重新放入内存缓存
            mMemoryCache.put(contactId, contact);
            return contact;
        }
        
        return null;
    }
    
    public void put(String contactId, Contact contact) {
        if (contact == null) return;
        
        mMemoryCache.put(contactId, contact);
        mDiskCache.put(contactId, contact);
    }
    
    public void invalidate(String contactId) {
        mMemoryCache.remove(contactId);
        mDiskCache.remove(contactId);
    }
    
    public void clear() {
        mMemoryCache.evictAll();
        mDiskCache.clear();
    }
}
```

### 2.3 ContactsSearcher - 搜索功能

```java
public class ContactsSearcher {
    private static final String TAG = "ContactsSearcher";
    private final Context mContext;
    private final ContactsProvider mProvider;
    
    public ContactsSearcher(Context context) {
        mContext = context;
        mProvider = new ContactsProvider(context);
    }
    
    public List<Contact> searchContacts(ContactSearchCriteria criteria) {
        List<Contact> results = new ArrayList<>();
        
        // 构建查询条件
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();
        
        if (criteria.getNameQuery() != null && !criteria.getNameQuery().isEmpty()) {
            selection.append("(")
                    .append(ContactsContract.Contacts.DISPLAY_NAME)
                    .append(" LIKE ? OR ")
                    .append(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                    .append(" LIKE ?)");
            selectionArgs.add("%" + criteria.getNameQuery() + "%");
            selectionArgs.add("%" + criteria.getNameQuery() + "%");
        }
        
        if (criteria.isStarredOnly()) {
            if (selection.length() > 0) {
                selection.append(" AND ");
            }
            selection.append(ContactsContract.Contacts.STARRED).append(" = 1");
        }
        
        // 执行查询
        try (Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                selection.toString(),
                selectionArgs.toArray(new String[0]),
                ContactsContract.Contacts.DISPLAY_NAME + " ASC")) {
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    Contact contact = mProvider.getContactDetails(contactId);
                    
                    // 应用额外的过滤条件
                    if (matchesAdditionalCriteria(contact, criteria)) {
                        results.add(contact);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching contacts", e);
        }
        
        return results;
    }
    
    private boolean matchesAdditionalCriteria(Contact contact, ContactSearchCriteria criteria) {
        // 检查电话号码条件
        if (criteria.getPhoneQuery() != null && !criteria.getPhoneQuery().isEmpty()) {
            boolean hasMatchingPhone = false;
            for (PhoneInfo phone : contact.getPhones()) {
                if (phone.getNumber().contains(criteria.getPhoneQuery())) {
                    hasMatchingPhone = true;
                    break;
                }
            }
            if (!hasMatchingPhone) {
                return false;
            }
        }
        
        // 检查邮箱条件
        if (criteria.getEmailQuery() != null && !criteria.getEmailQuery().isEmpty()) {
            boolean hasMatchingEmail = false;
            for (EmailInfo email : contact.getEmails()) {
                if (email.getAddress().contains(criteria.getEmailQuery())) {
                    hasMatchingEmail = true;
                    break;
                }
            }
            if (!hasMatchingEmail) {
                return false;
            }
        }
        
        // 检查公司条件
        if (criteria.getCompanyQuery() != null && !criteria.getCompanyQuery().isEmpty()) {
            boolean hasMatchingCompany = false;
            for (OrganizationInfo org : contact.getOrganizations()) {
                if (org.getCompany() != null && org.getCompany().contains(criteria.getCompanyQuery())) {
                    hasMatchingCompany = true;
                    break;
                }
            }
            if (!hasMatchingCompany) {
                return false;
            }
        }
        
        return true;
    }
}
```

### 2.4 ContactsChangeObserver - 变更监听

```java
public class ContactsChangeObserver extends ContentObserver {
    private static final String TAG = "ContactsChangeObserver";
    private final List<ContactsChangeListener> mListeners = new ArrayList<>();
    private final Context mContext;
    private final Handler mHandler;
    
    public ContactsChangeObserver(Context context) {
        super(new Handler(Looper.getMainLooper()));
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
    }
    
    public void register() {
        mContext.getContentResolver().registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI,
                true,
                this);
    }
    
    public void unregister() {
        mContext.getContentResolver().unregisterContentObserver(this);
    }
    
    public void addListener(ContactsChangeListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }
    
    public void removeListener(ContactsChangeListener listener) {
        mListeners.remove(listener);
    }
    
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        Log.d(TAG, "Contacts changed: " + uri);
        
        // 在后台线程处理变更
        new Thread(() -> {
            try {
                handleContactsChange(uri);
            } catch (Exception e) {
                Log.e(TAG, "Error handling contacts change", e);
            }
        }).start();
    }
    
    private void handleContactsChange(Uri uri) {
        // 获取变更的联系人ID
        String contactId = getContactIdFromUri(uri);
        if (contactId == null) {
            // 如果无法获取具体ID，可能是批量变更，需要重新同步
            notifyBatchChange();
            return;
        }
        
        // 检查联系人是否存在
        if (isContactDeleted(contactId)) {
            notifyContactDeleted(contactId);
        } else {
            notifyContactChanged(contactId);
        }
    }
    
    private String getContactIdFromUri(Uri uri) {
        // 从URI中提取联系人ID
        String lastPathSegment = uri.getLastPathSegment();
        if (lastPathSegment != null && TextUtils.isDigitsOnly(lastPathSegment)) {
            return lastPathSegment;
        }
        return null;
    }
    
    private boolean isContactDeleted(String contactId) {
        try (Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts._ID},
                ContactsContract.Contacts._ID + " = ?",
                new String[]{contactId},
                null)) {
            return cursor == null || cursor.getCount() == 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if contact is deleted", e);
            return false;
        }
    }
    
    private void notifyContactChanged(String contactId) {
        mHandler.post(() -> {
            for (ContactsChangeListener listener : mListeners) {
                listener.onContactsChanged(Collections.singletonList(contactId));
            }
        });
    }
    
    private void notifyContactDeleted(String contactId) {
        mHandler.post(() -> {
            for (ContactsChangeListener listener : mListeners) {
                listener.onContactsDeleted(Collections.singletonList(contactId));
            }
        });
    }
    
    private void notifyBatchChange() {
        mHandler.post(() -> {
            for (ContactsChangeListener listener : mListeners) {
                // 通知批量变更，需要重新同步
                listener.onContactsChanged(null);
            }
        });
    }
}
```

## 3. 实现步骤

### 步骤1：创建数据模型类
1. 创建所有数据模型类
2. 实现序列化接口（如果需要）
3. 添加必要的工具方法

### 步骤2：实现ContactsProvider
1. 实现基础查询功能
2. 实现详细信息查询
3. 添加错误处理和日志

### 步骤3：实现缓存系统
1. 实现内存缓存
2. 实现磁盘缓存
3. 添加缓存过期策略

### 步骤4：实现搜索功能
1. 实现基础搜索
2. 实现多条件搜索
3. 优化搜索性能

### 步骤5：实现变更监听
1. 实现ContentObserver
2. 添加监听器管理
3. 处理批量变更

### 步骤6：实现分组功能
1. 实现字母分组
2. 实现频率分组
3. 实现自定义分组

### 步骤7：实现增量同步
1. 实现时间戳比较
2. 实现增量查询
3. 添加同步状态管理

### 步骤8：性能优化
1. 添加查询优化
2. 实现内存管理
3. 添加性能监控

## 4. 测试策略

### 单元测试
- 每个核心类的独立测试
- 模拟数据和边界条件测试
- 性能基准测试

### 集成测试
- 端到端功能测试
- 权限处理测试
- 不同Android版本兼容性测试

### 性能测试
- 大量联系人场景测试
- 内存使用监控
- 查询响应时间测试

## 5. 注意事项

### 权限处理
- 动态权限请求
- 权限状态检查
- 权限拒绝处理

### 兼容性
- Android API级别适配
- 不同厂商ROM兼容性
- 数据格式差异处理

### 性能考虑
- 避免在主线程进行数据库操作
- 合理使用缓存策略
- 及时释放资源

### 安全性
- 敏感数据加密存储
- 权限最小化原则
- 数据访问日志记录