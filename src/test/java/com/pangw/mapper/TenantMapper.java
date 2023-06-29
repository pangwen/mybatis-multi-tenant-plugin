package com.pangw.mapper;

import com.pangw.pojo.Tenant;

import java.util.List;

/**
 * Description: 租户 mapper
 * <p>
 * Created on 2023/6/28.
 *
 * @author <a href="mailto:smile.pangwen@gmail.com">pangwen</a>
 * @version 0.1
 */
public interface TenantMapper {

    void insert(Tenant tenant);

    void update(Tenant tenant);

    void deleteById(Long tid);

    Tenant selectById(Long id);

    List<Tenant> list();
}
