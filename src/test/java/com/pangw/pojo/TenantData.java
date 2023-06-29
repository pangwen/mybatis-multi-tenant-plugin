package com.pangw.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * Description: 租户数据 pojo
 * <p>
 * Created on 2023/6/28.
 *
 * @author <a href="mailto:smile.pangwen@gmail.com">pangwen</a>
 * @version 0.1
 */
public class TenantData implements Serializable {
    private static final long serialVersionUID = 2747025159805576938L;

    /**
     * 数据id
     */
    private Long id;

    /**
     * 租户id
     */
    private Long tid;

    /**
     * 租户数据
     */
    private String somedata;

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

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public String getSomedata() {
        return somedata;
    }

    public void setSomedata(String somedata) {
        this.somedata = somedata;
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
