package com.pangw.holder;

public class TenantContext {

    private final boolean enable;
    private final Long tenantId;

    TenantContext(Long tenantId) {
        this.enable = true;
        this.tenantId = tenantId;
    }

    TenantContext(boolean enable, Long tenantId) {
        this.enable = enable;
        this.tenantId = tenantId;
    }

    public boolean isEnable() {
        return enable;
    }

    public Long getTenantId() {
        return tenantId;
    }
}
