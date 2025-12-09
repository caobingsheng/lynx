# 通讯录模块迁移指南

## 1. 迁移概述

本指南详细说明如何从基于第三方库的通讯录实现迁移到原生Android实现，包括API变更、数据结构差异和迁移步骤。

## 2. API变更对比

### 2.1 旧API vs 新API

| 旧API方法 | 新API方法 | 变更说明 |
|-----------|----------|----------|
| `getContactsPage(int page)` | `getContactsPage(int page, int pageSize)` | 添加pageSize参数，更灵活的分页 |
| `getContactsPageWithQuery(int page, String query)` | `getContactsPageWithCriteria(int page, int pageSize, ContactSearchCriteria criteria)` | 支持多条件搜索 |
| `searchContacts(String query)` | `searchContacts(ContactSearchCriteria criteria)` | 支持多字段搜索 |
| 无 | `getContactGroups(ContactGroupType type)` | 新增分组功能 |
| 无 | `getContactsModifiedSince(long timestamp)` | 新增增量更新 |
| 无 | `registerContactsChangeListener(ContactsChangeListener listener)` | 新增变更监听 |

### 2.2 数据结构变更

#### 旧数据结构
```java
// 简单的Map结构
WritableMap contact = new JavaOnlyMap();
contact.putString("id", "123");
contact.putString("name", "张三");
contact.putString("phone", "13800138000");
contact.putArray("phones", phonesArray);
```

#### 新数据结构
```java
// 强类型对象
Contact contact = new Contact();
ContactInfo info = new ContactInfo();
info.setId("123");
info.setDisplayName("张三");
contact.setContactInfo(info);

List<PhoneInfo> phones = new ArrayList<>();
PhoneInfo phone = new PhoneInfo();
phone.setNumber("13800138000");
phone.setType(Phone.TYPE_MOBILE);
phones.add(phone);
contact.setPhones(phones);
```

## 3. 迁移步骤

### 步骤1：准备工作
1. 备份现有代码
2. 创建新的包结构：`com.lynx.explorer.contacts`
3. 确保所有必要的权限已声明

### 步骤2：移除第三方库依赖
```gradle
// 在build.gradle中删除这行
implementation 'com.github.vestrel00:contacts-android:0.4.0'
```

### 步骤3：实现数据模型
1. 创建新的数据模型类
2. 实现数据转换工具类
3. 添加序列化支持

```java
// 数据转换工具示例
public class ContactDataConverter {
    public static WritableMap convertToWritableMap(Contact contact) {
        WritableMap result = new JavaOnlyMap();
        
        ContactInfo info = contact.getContactInfo();
        result.putString("id", info.getId());
        result.putString("name", info.getDisplayName());
        result.putString("photoUri", info.getPhotoUri());
        result.putBoolean("starred", info.isStarred());
        
        // 转换电话号码
        WritableArray phonesArray = new JavaOnlyArray();
        for (PhoneInfo phone : contact.getPhones()) {
            WritableMap phoneMap = new JavaOnlyMap();
            phoneMap.putString("number", phone.getNumber());
            phoneMap.putInt("type", phone.getType());
            phoneMap.putString("label", phone.getLabel());
            phonesArray.pushMap(phoneMap);
        }
        result.putArray("phones", phonesArray);
        
        // 类似地转换其他字段...
        
        return result;
    }
    
    public static ContactSearchCriteria convertFromLegacyQuery(String query) {
        ContactSearchCriteria criteria = new ContactSearchCriteria();
        criteria.setNameQuery(query);
        return criteria;
    }
}
```

### 步骤4：更新ContactsModule
```java
public class ContactsModule extends LynxModule {
    private static final String TAG = "ContactsModule";
    private ContactsManager mContactsManager;
    
    public ContactsModule(Context context) {
        super(context);
        mContactsManager = new ContactsManager();
        mContactsManager.initialize(context);
    }
    
    @LynxMethod
    public WritableMap checkContactsPermission() {
        WritableMap result = new JavaOnlyMap();
        boolean hasPermission = mContactsManager.hasContactsPermission();
        result.putBoolean("hasPermission", hasPermission);
        return result;
    }
    
    @LynxMethod
    public void requestContactsPermission() {
        if (mContext instanceof Activity) {
            mContactsManager.requestContactsPermission((Activity) mContext);
        }
    }
    
    @LynxMethod
    public WritableMap getContactsPage(int page, int pageSize) {
        ContactsPage contactsPage = mContactsManager.getContactsPage(page, pageSize);
        return convertContactsPageToWritableMap(contactsPage);
    }
    
    @LynxMethod
    public WritableMap getContactsPageWithQuery(int page, int pageSize, String query) {
        ContactSearchCriteria criteria = ContactDataConverter.convertFromLegacyQuery(query);
        ContactsPage contactsPage = mContactsManager.getContactsPageWithCriteria(page, pageSize, criteria);
        return convertContactsPageToWritableMap(contactsPage);
    }
    
    @LynxMethod
    public WritableMap searchContacts(String query) {
        ContactSearchCriteria criteria = ContactDataConverter.convertFromLegacyQuery(query);
        List<Contact> contacts = mContactsManager.searchContacts(criteria);
        return convertContactListToWritableMap(contacts);
    }
    
    // 新增API方法
    @LynxMethod
    public WritableMap getContactGroups(String groupType) {
        ContactGroupType type = ContactGroupType.valueOf(groupType.toUpperCase());
        List<ContactGroup> groups = mContactsManager.getContactGroups(type);
        return convertGroupsToWritableMap(groups);
    }
    
    @LynxMethod
    public WritableMap getFrequentContacts(int limit) {
        List<Contact> contacts = mContactsManager.getFrequentContacts(limit);
        return convertContactListToWritableMap(contacts);
    }
    
    // 转换方法
    private WritableMap convertContactsPageToWritableMap(ContactsPage page) {
        WritableMap result = new JavaOnlyMap();
        result.putBoolean("success", true);
        
        WritableMap data = new JavaOnlyMap();
        WritableArray contactsArray = new JavaOnlyArray();
        
        for (Contact contact : page.getContacts()) {
            contactsArray.pushMap(ContactDataConverter.convertToWritableMap(contact));
        }
        
        data.putArray("contacts", contactsArray);
        data.putInt("totalCount", page.getTotalCount());
        data.putInt("currentPage", page.getCurrentPage());
        data.putInt("pageSize", page.getPageSize());
        data.putBoolean("hasMore", page.isHasMore());
        
        result.putMap("data", data);
        return result;
    }
}
```

## 4. 前端代码适配

### 4.1 JavaScript/Lynx前端变更

#### 旧调用方式
```javascript
// 检查权限
const permissionResult = await Lynx.callModule('ContactsModule', 'checkContactsPermission');

// 获取联系人
const contactsResult = await Lynx.callModule('ContactsModule', 'getContactsPage', 0);

// 搜索联系人
const searchResult = await Lynx.callModule('ContactsModule', 'searchContacts', '张三');
```

#### 新调用方式
```javascript
// 检查权限（无变化）
const permissionResult = await Lynx.callModule('ContactsModule', 'checkContactsPermission');

// 获取联系人（添加pageSize参数）
const contactsResult = await Lynx.callModule('ContactsModule', 'getContactsPage', 0, 20);

// 多条件搜索
const searchCriteria = {
    nameQuery: '张三',
    phoneQuery: '138',
    starredOnly: false
};
const searchResult = await Lynx.callModule('ContactsModule', 'searchContacts', searchCriteria);

// 获取分组
const groupsResult = await Lynx.callModule('ContactsModule', 'getContactGroups', 'ALPHABETICAL');

// 获取常用联系人
const frequentResult = await Lynx.callModule('ContactsModule', 'getFrequentContacts', 10);
```

### 4.2 数据结构适配

#### 旧数据结构
```javascript
{
  "success": true,
  "data": {
    "contacts": [
      {
        "id": "1",
        "name": "张三",
        "phone": "13800138000",
        "phones": [
          {"number": "13800138000", "type": "MOBILE"}
        ]
      }
    ],
    "totalCount": 100,
    "currentPage": 0,
    "pageSize": 20,
    "hasMore": true
  }
}
```

#### 新数据结构（向后兼容）
```javascript
{
  "success": true,
  "data": {
    "contacts": [
      {
        "id": "1",
        "name": "张三",
        "phone": "13800138000",
        "phones": [
          {"number": "13800138000", "type": "MOBILE", "label": "", "isPrimary": true}
        ],
        "emails": [...],
        "addresses": [...],
        "organizations": [...],
        "photoUri": "...",
        "starred": false,
        "timesContacted": 5,
        "lastTimeContacted": 1234567890,
        "lastModifiedTime": 1234567890
      }
    ],
    "totalCount": 100,
    "currentPage": 0,
    "pageSize": 20,
    "hasMore": true
  }
}
```

## 5. 渐进式迁移策略

### 阶段1：双轨运行
1. 保留旧API，同时实现新API
2. 新功能使用新API
3. 旧功能保持不变

### 阶段2：逐步替换
1. 将简单功能迁移到新API
2. 测试性能和稳定性
3. 收集用户反馈

### 阶段3：完全替换
1. 移除旧API
2. 清理第三方库依赖
3. 更新所有文档

## 6. 测试计划

### 兼容性测试
- 验证旧API仍能正常工作
- 测试新API的功能完整性
- 确保数据格式向后兼容

### 性能测试
- 对比新旧API的性能差异
- 测试大量联系人场景
- 验证内存使用情况

### 功能测试
- 测试所有新增功能
- 验证搜索准确性
- 测试分组功能

## 7. 风险控制

### 数据丢失风险
- 实施数据备份策略
- 提供回滚机制
- 充分测试数据转换

### 性能风险
- 设置性能监控
- 实施渐进式发布
- 准备性能优化方案

### 用户体验风险
- 保持界面一致性
- 提供使用指南
- 收集用户反馈

## 8. 迁移检查清单

- [ ] 备份现有代码
- [ ] 创建新包结构
- [ ] 实现数据模型
- [ ] 实现核心功能
- [ ] 更新ContactsModule
- [ ] 测试新功能
- [ ] 更新前端代码
- [ ] 性能测试
- [ ] 移除第三方库
- [ ] 更新文档
- [ ] 全面测试
- [ ] 发布新版本