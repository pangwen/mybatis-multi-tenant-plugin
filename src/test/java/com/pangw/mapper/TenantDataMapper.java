package com.pangw.mapper;

import com.pangw.pojo.TenantData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Description: 租户数据 mapper
 * <p>
 * Created on 2023/6/28.
 *
 * @author <a href="mailto:smile.pangwen@gmail.com">pangwen</a>
 * @version 0.1
 */
public interface TenantDataMapper {

    void insert(TenantData tenantData);

    void update(TenantData tenantData);

    void deleteById(Long id);

    List<TenantData> list();

    List<TenantData> listByData(@Param("text") String text);
}
