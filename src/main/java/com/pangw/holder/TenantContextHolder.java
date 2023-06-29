package com.pangw.holder;

import java.util.Objects;

/**
 * Description: 租户插件上下文
 *
 * <p>
 * Created on 2023/6/29.
 *
 * @author <a href="mailto:smile.pangwen@gmail.com">pangwen</a>
 * @version 0.1
 */
public class TenantContextHolder {

    private static final ThreadLocal<TenantContext> HOLDER = new ThreadLocal<>();

    public static TenantContext get(){
        return HOLDER.get();
    }

    private TenantContextHolder() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static void set(TenantContext context){
        Objects.requireNonNull(context, "TenantContext cannot be null.");
        HOLDER.set(context);
    }

    public static void set(Long tenantId){
        Objects.requireNonNull(tenantId, "tenantId cannot be null.");
        HOLDER.set(new TenantContext(tenantId));
    }

    public static void disableTenantIntercept(){
        HOLDER.set(newDisabledTenantContext());
    }

    public static void clear(){
        HOLDER.remove();
    }

    private static TenantContext newDisabledTenantContext(){
        return new TenantContext(false, null);
    }
}
