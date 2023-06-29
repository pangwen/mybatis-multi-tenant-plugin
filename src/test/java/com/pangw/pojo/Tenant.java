package com.pangw.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * Description: 租户 pojo
 * <p>
 * Created on 2023/6/28.
 *
 * @author <a href="mailto:smile.pangwen@gmail.com">pangwen</a>
 * @version 0.1
 */
public class Tenant implements Serializable {
    private static final long serialVersionUID = 4401422063175454339L;

    /**
     * 租户id
     */
    private Long id;

    /**
     * 租户名称
     */
    private String name;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
