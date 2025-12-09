// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
package com.lynx.explorer.contacts.provider;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.lynx.explorer.contacts.model.Contact;
import com.lynx.explorer.contacts.model.ContactInfo;
import com.lynx.explorer.contacts.model.PhoneInfo;
import com.lynx.explorer.contacts.model.EmailInfo;
import com.lynx.explorer.contacts.model.AddressInfo;
import com.lynx.explorer.contacts.model.OrganizationInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 原生Android通讯录数据提供者
 */
public class ContactsProvider {
    private static final String TAG = "ContactsProvider";
    private final Context mContext;

    public ContactsProvider(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * 查询联系人基本信息列表
     */
    public List<ContactInfo> queryContactInfos(String selection, String sortOrder) {
        List<ContactInfo> contacts = new ArrayList<>();
        
        try (Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE,
                        ContactsContract.Contacts.PHOTO_URI,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER,
                        ContactsContract.Contacts.STARRED,
                        ContactsContract.Contacts.LAST_TIME_CONTACTED,
                        ContactsContract.Contacts.TIMES_CONTACTED,
                        ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP
                },
                selection,
                null,
                sortOrder != null ? sortOrder : ContactsContract.Contacts.DISPLAY_NAME + " ASC")) {
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ContactInfo contact = createContactInfoFromCursor(cursor);
                    if (contact != null) {
                        contacts.add(contact);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying contact infos", e);
        }
        
        return contacts;
    }

    /**
     * 获取联系人基本信息列表
     */
    public List<ContactInfo> getContactInfos(int offset, int limit) {
        List<ContactInfo> contacts = new ArrayList<>();
        
        try (Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE,
                        ContactsContract.Contacts.PHOTO_URI,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER,
                        ContactsContract.Contacts.STARRED,
                        ContactsContract.Contacts.LAST_TIME_CONTACTED,
                        ContactsContract.Contacts.TIMES_CONTACTED,
                        ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP
                },
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC LIMIT " + limit + " OFFSET " + offset)) {
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ContactInfo contact = createContactInfoFromCursor(cursor);
                    if (contact != null) {
                        contacts.add(contact);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting contact infos", e);
        }
        
        return contacts;
    }

    /**
     * 获取联系人详细信息
     */
    public Contact getContactDetails(String contactId) {
        ContactInfo contactInfo = getContactInfo(contactId);
        if (contactInfo == null) {
            return null;
        }

        Contact contact = new Contact(contactInfo);
        contact.setPhones(getPhoneNumbers(contactId));
        contact.setEmails(getEmailAddresses(contactId));
        contact.setAddresses(getAddresses(contactId));
        contact.setOrganizations(getOrganizations(contactId));
        
        return contact;
    }

    /**
     * 根据ID获取联系人基本信息
     */
    public ContactInfo getContactInfo(String contactId) {
        String selection = ContactsContract.Contacts._ID + " = ?";
        String[] selectionArgs = {contactId};
        
        try (Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE,
                        ContactsContract.Contacts.PHOTO_URI,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER,
                        ContactsContract.Contacts.STARRED,
                        ContactsContract.Contacts.LAST_TIME_CONTACTED,
                        ContactsContract.Contacts.TIMES_CONTACTED,
                        ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP
                },
                selection,
                selectionArgs,
                null)) {
            
            if (cursor != null && cursor.moveToFirst()) {
                return createContactInfoFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting contact info for id: " + contactId, e);
        }
        
        return null;
    }

    /**
     * 获取电话号码列表
     */
    public List<PhoneInfo> getPhoneNumbers(String contactId) {
        List<PhoneInfo> phones = new ArrayList<>();
        
        String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
        String[] selectionArgs = {contactId};
        
        try (Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone._ID,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.LABEL,
                        ContactsContract.CommonDataKinds.Phone.IS_PRIMARY
                },
                selection,
                selectionArgs,
                null)) {
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    PhoneInfo phone = createPhoneInfoFromCursor(cursor);
                    if (phone != null) {
                        phones.add(phone);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting phone numbers for contact: " + contactId, e);
        }
        
        return phones;
    }

    /**
     * 获取邮箱地址列表
     */
    public List<EmailInfo> getEmailAddresses(String contactId) {
        List<EmailInfo> emails = new ArrayList<>();
        
        String selection = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?";
        String[] selectionArgs = {contactId};
        
        try (Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Email._ID,
                        ContactsContract.CommonDataKinds.Email.ADDRESS,
                        ContactsContract.CommonDataKinds.Email.TYPE,
                        ContactsContract.CommonDataKinds.Email.LABEL,
                        ContactsContract.CommonDataKinds.Email.IS_PRIMARY
                },
                selection,
                selectionArgs,
                null)) {
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    EmailInfo email = createEmailInfoFromCursor(cursor);
                    if (email != null) {
                        emails.add(email);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting email addresses for contact: " + contactId, e);
        }
        
        return emails;
    }

    /**
     * 获取地址列表
     */
    public List<AddressInfo> getAddresses(String contactId) {
        List<AddressInfo> addresses = new ArrayList<>();
        
        String selection = ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ?";
        String[] selectionArgs = {contactId};
        
        try (Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.StructuredPostal._ID,
                        ContactsContract.CommonDataKinds.StructuredPostal.STREET,
                        ContactsContract.CommonDataKinds.StructuredPostal.CITY,
                        ContactsContract.CommonDataKinds.StructuredPostal.REGION,
                        ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
                        ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
                        ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                        ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                        ContactsContract.CommonDataKinds.StructuredPostal.LABEL
                },
                selection,
                selectionArgs,
                null)) {
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    AddressInfo address = createAddressInfoFromCursor(cursor);
                    if (address != null) {
                        addresses.add(address);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting addresses for contact: " + contactId, e);
        }
        
        return addresses;
    }

    /**
     * 获取组织信息列表
     */
    public List<OrganizationInfo> getOrganizations(String contactId) {
        List<OrganizationInfo> organizations = new ArrayList<>();
        
        String selection = ContactsContract.CommonDataKinds.Organization.CONTACT_ID + " = ?";
        String[] selectionArgs = {contactId};
        
        try (Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{
                        ContactsContract.Data._ID,
                        ContactsContract.CommonDataKinds.Organization.COMPANY,
                        ContactsContract.CommonDataKinds.Organization.TITLE,
                        ContactsContract.CommonDataKinds.Organization.DEPARTMENT,
                        ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION,
                        ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION,
                        ContactsContract.CommonDataKinds.Organization.TYPE,
                        ContactsContract.CommonDataKinds.Organization.LABEL
                },
                selection + " AND " + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE + "'",
                selectionArgs,
                null)) {
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    OrganizationInfo organization = createOrganizationInfoFromCursor(cursor);
                    if (organization != null) {
                        organizations.add(organization);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting organizations for contact: " + contactId, e);
        }
        
        return organizations;
    }

    /**
     * 获取联系人总数
     */
    public int getContactsCount() {
        try (Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts._ID},
                null,
                null,
                null)) {
            
            return cursor != null ? cursor.getCount() : 0;
        } catch (Exception e) {
            Log.e(TAG, "Error getting contacts count", e);
            return 0;
        }
    }

    /**
     * 从Cursor创建ContactInfo
     */
    private ContactInfo createContactInfoFromCursor(Cursor cursor) {
        try {
            ContactInfo contactInfo = new ContactInfo();
            
            int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            if (idIndex >= 0) {
                contactInfo.setId(cursor.getString(idIndex));
            }
            
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
     * 从Cursor创建PhoneInfo
     */
    private PhoneInfo createPhoneInfoFromCursor(Cursor cursor) {
        try {
            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int typeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
            int labelIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL);
            int isPrimaryIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY);
            
            String number = numberIndex >= 0 ? cursor.getString(numberIndex) : "";
            int type = typeIndex >= 0 ? cursor.getInt(typeIndex) : PhoneInfo.TYPE_OTHER;
            String label = labelIndex >= 0 ? cursor.getString(labelIndex) : "";
            boolean isPrimary = isPrimaryIndex >= 0 && cursor.getInt(isPrimaryIndex) == 1;
            
            return new PhoneInfo(number, type, label, isPrimary);
        } catch (Exception e) {
            Log.e(TAG, "Error creating phone info from cursor", e);
            return null;
        }
    }

    /**
     * 从Cursor创建EmailInfo
     */
    private EmailInfo createEmailInfoFromCursor(Cursor cursor) {
        try {
            int addressIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
            int typeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE);
            int labelIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL);
            int isPrimaryIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.IS_PRIMARY);
            
            String address = addressIndex >= 0 ? cursor.getString(addressIndex) : "";
            int type = typeIndex >= 0 ? cursor.getInt(typeIndex) : EmailInfo.TYPE_OTHER;
            String label = labelIndex >= 0 ? cursor.getString(labelIndex) : "";
            boolean isPrimary = isPrimaryIndex >= 0 && cursor.getInt(isPrimaryIndex) == 1;
            
            return new EmailInfo(address, type, label, isPrimary);
        } catch (Exception e) {
            Log.e(TAG, "Error creating email info from cursor", e);
            return null;
        }
    }

    /**
     * 从Cursor创建AddressInfo
     */
    private AddressInfo createAddressInfoFromCursor(Cursor cursor) {
        try {
            int streetIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET);
            int cityIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY);
            int stateIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION);
            int postalCodeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE);
            int countryIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY);
            int formattedAddressIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
            int typeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE);
            int labelIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.LABEL);
            
            String street = streetIndex >= 0 ? cursor.getString(streetIndex) : "";
            String city = cityIndex >= 0 ? cursor.getString(cityIndex) : "";
            String state = stateIndex >= 0 ? cursor.getString(stateIndex) : "";
            String postalCode = postalCodeIndex >= 0 ? cursor.getString(postalCodeIndex) : "";
            String country = countryIndex >= 0 ? cursor.getString(countryIndex) : "";
            String formattedAddress = formattedAddressIndex >= 0 ? cursor.getString(formattedAddressIndex) : "";
            int type = typeIndex >= 0 ? cursor.getInt(typeIndex) : AddressInfo.TYPE_OTHER;
            String label = labelIndex >= 0 ? cursor.getString(labelIndex) : "";
            
            AddressInfo address = new AddressInfo(formattedAddress, type);
            address.setStreet(street);
            address.setCity(city);
            address.setState(state);
            address.setPostalCode(postalCode);
            address.setCountry(country);
            address.setLabel(label);
            
            return address;
        } catch (Exception e) {
            Log.e(TAG, "Error creating address info from cursor", e);
            return null;
        }
    }

    /**
     * 从Cursor创建OrganizationInfo
     */
    private OrganizationInfo createOrganizationInfoFromCursor(Cursor cursor) {
        try {
            int companyIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY);
            int titleIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE);
            int departmentIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT);
            int jobDescriptionIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION);
            int officeLocationIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION);
            int typeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TYPE);
            int labelIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.LABEL);
            
            String company = companyIndex >= 0 ? cursor.getString(companyIndex) : "";
            String title = titleIndex >= 0 ? cursor.getString(titleIndex) : "";
            String department = departmentIndex >= 0 ? cursor.getString(departmentIndex) : "";
            String jobDescription = jobDescriptionIndex >= 0 ? cursor.getString(jobDescriptionIndex) : "";
            String officeLocation = officeLocationIndex >= 0 ? cursor.getString(officeLocationIndex) : "";
            int type = typeIndex >= 0 ? cursor.getInt(typeIndex) : OrganizationInfo.TYPE_OTHER;
            String label = labelIndex >= 0 ? cursor.getString(labelIndex) : "";
            
            OrganizationInfo organization = new OrganizationInfo(company, title);
            organization.setDepartment(department);
            organization.setJobDescription(jobDescription);
            organization.setOfficeLocation(officeLocation);
            organization.setType(type);
            organization.setLabel(label);
            
            return organization;
        } catch (Exception e) {
            Log.e(TAG, "Error creating organization info from cursor", e);
            return null;
        }
    }
}