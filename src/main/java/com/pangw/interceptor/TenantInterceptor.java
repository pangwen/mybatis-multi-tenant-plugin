package com.pangw.interceptor;

import com.pangw.holder.TenantContext;
import com.pangw.holder.TenantContextHolder;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Description: 多租户插件
 * <p>
 * Created on 2023/6/28.
 *
 * @author <a href="mailto:smile.pangwen@gmail.com">pangwen</a>
 * @version 0.1
 */
/* 根据方法签名拦截指定方法， 以下拦截 Executor 接口中的两个 query 方法*/
@Intercepts({
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})}
)
public class TenantInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(TenantInterceptor.class);

    private List<String> ignoredMappers;

    private String tenantColumn = "tenant_id";


    public TenantInterceptor() {
        log.info("Instantiate multi tenant plugin...");
    }


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        /* 方法参数列表 */
        Object[] args = invocation.getArgs();
        int len = args.length;
        MappedStatement ms = (MappedStatement) args[0];
        log.debug("Handle [{}] method..", ms.getId());
        TenantContext tenantContext = TenantContextHolder.get();
        // 无需添加租户查询，直接返回
        if (Objects.isNull(tenantContext)
                || !tenantContext.isEnable()
                || isIgnored(ignoredMappers, ms.getId())) {
            log.debug("Ignore process boundSql. ");
            return invocation.proceed();
        }

        Executor executor = (Executor) invocation.getTarget();
        Object paramObj = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler<?> resultHandler = (ResultHandler<?>) args[3];
        BoundSql boundSql = ms.getBoundSql(paramObj);

        CacheKey cacheKey;
        if (len == 4) {
            cacheKey = executor.createCacheKey(ms, paramObj, rowBounds, boundSql);
            boundSql = processTenantBoundSql(ms, boundSql, paramObj);
        } else {
            // len == 6
            cacheKey = (CacheKey) args[4];
            boundSql = processTenantBoundSql(ms, (BoundSql) args[5], paramObj);
        }

        return executor.query(ms, paramObj, rowBounds, resultHandler, cacheKey, boundSql);
    }

    private BoundSql processTenantBoundSql(MappedStatement ms, BoundSql boundSql, Object paramObj) throws JSQLParserException, NoSuchFieldException, IllegalAccessException {
        // 解析原始SQL
        String originalSql = boundSql.getSql();
        Select select = (Select) CCJSqlParserUtil.parse(originalSql);
        SelectBody selectBody = select.getSelectBody();
        // 不处理复杂查询
        if (!(selectBody instanceof PlainSelect)) {
            log.warn("Tenant plugin is only available for plaint query. method: {}, original sql: {}, ", ms.getId(), originalSql);
            return boundSql;
        }
        PlainSelect plainSelect = (PlainSelect) selectBody;
        // 不处理 联表查询
        List<Join> joins = plainSelect.getJoins();
        if (Objects.nonNull(joins) && !joins.isEmpty()) {
            log.warn("Cannot parse join tables.  method: {},  original sql: {}", ms.getId(), originalSql);
            return boundSql;
        }
        // 不处理 from 子查询
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem instanceof SubSelect) {
            log.warn("Cannot parse sub select sql. method: {},  original sql: {}", ms.getId(), originalSql);
            return boundSql;
        }

        String columnName = tenantColumn;
        // 解析表别名
        Alias alias = fromItem.getAlias();
        if (Objects.nonNull(alias)) {
            String tableAlias = alias.getName();
            if (Objects.nonNull(tableAlias) && !tableAlias.isEmpty()) {
                columnName = tableAlias + "." + columnName;
            }
        }

        EqualsTo tenantEquals = new EqualsTo(new Column(columnName), new LongValue(TenantContextHolder.get().getTenantId()));

        Expression where = plainSelect.getWhere();
        if (Objects.isNull(where)) {
            plainSelect.setWhere(tenantEquals);
        } else {
            plainSelect.setWhere(new AndExpression(tenantEquals, where));
        }

        BoundSql tenantBoundSql = new BoundSql(ms.getConfiguration(), plainSelect.toString(), boundSql.getParameterMappings(), paramObj);
        fillAdditionalParameters(tenantBoundSql, boundSql);

        return tenantBoundSql;
    }

    /**
     * 解决额外参数绑定， 例如 {@code <bind> 标签定义的参数 }
     *
     * @param tenantBoundSql 添加租户查询条件后的 boundSql
     * @param boundSql       原始 boundSql
     * @throws NoSuchFieldException   反射获取额外参数异常
     * @throws IllegalAccessException 反射访问属性异常
     */
    private void fillAdditionalParameters(BoundSql tenantBoundSql, BoundSql boundSql) throws NoSuchFieldException, IllegalAccessException {
        Field field = boundSql.getClass().getDeclaredField("additionalParameters");
        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> additionalParams = (Map<String, Object>) field.get(boundSql);
        if (Objects.isNull(additionalParams) || additionalParams.isEmpty()) {
            return;
        }
        for (String key : additionalParams.keySet()) {
            tenantBoundSql.setAdditionalParameter(key, additionalParams.get(key));
        }

    }

    /**
     * 判断是否忽略处理多租户sql
     *
     * @param ignoredMappers 配置的需要忽略的mapper接口全限定名
     * @param msId           当前执行的 mapper 方法名
     * @return 是否忽略
     */
    private boolean isIgnored(List<String> ignoredMappers, String msId) {
        if (ignoredMappers.isEmpty()) {
            return false;
        }
        String currentClassName = msId.substring(0, msId.lastIndexOf("."));
        for (String mapper : ignoredMappers) {
            if (mapper.equals(currentClassName)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        if (Objects.isNull(properties) || properties.isEmpty()) {
            this.ignoredMappers = new ArrayList<>();
            return;
        }

        String property = properties.getProperty("ignoredMappers");
        if (Objects.isNull(property) || property.isEmpty()) {
            this.ignoredMappers = new ArrayList<>();
        } else {
            this.ignoredMappers = Arrays.asList(property.split(","));
        }
        String tenantColumnName = properties.getProperty("tenantColumnName");
        if (Objects.nonNull(tenantColumnName) && !tenantColumnName.isEmpty()) {
            this.tenantColumn = tenantColumnName;
        }
    }
}
