// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
package com.lynx.explorer.contacts.model;

/**
 * 联系人分组类型枚举
 */
public enum ContactGroupType {
    ALPHABETICAL("alphabetical", "按字母分组"),
    FREQUENCY("frequency", "按联系频率"),
    STARRED("starred", "星标联系人"),
    COMPANY("company", "按公司分组"),
    CUSTOM("custom", "自定义分组");

    private final String code;
    private final String displayName;

    ContactGroupType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 根据代码获取分组类型
     */
    public static ContactGroupType fromCode(String code) {
        if (code == null) {
            return ALPHABETICAL;
        }

        for (ContactGroupType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }

        return ALPHABETICAL; // 默认返回按字母分组
    }

    @Override
    public String toString() {
        return displayName;
    }
}