package com.pangw;

import com.pangw.holder.TenantContextHolder;
import com.pangw.mapper.TenantDataMapper;
import com.pangw.pojo.TenantData;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TenantQueryTest {

    private static SqlSessionFactory sqlSessionFactory;

    static final String configPath = "mybatis-config.xml";

    @BeforeAll
    static void initSqlSessionFactory() throws IOException {
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader(configPath));
        initDb();
    }

    static void initDb() throws IOException {
        //创建数据库
        try(SqlSession session = sqlSessionFactory.openSession()){
            Connection conn = session.getConnection();
            Reader reader = Resources.getResourceAsReader("init.sql");
            ScriptRunner runner = new ScriptRunner(conn);
            runner.runScript(reader);
            reader.close();
        }
    }


    @Test
    void testQueryTenantData(){
        try(SqlSession session = sqlSessionFactory.openSession()){
            TenantDataMapper mapper = session.getMapper(TenantDataMapper.class);

            TenantContextHolder.set(1L);

            List<TenantData> list = mapper.list();

            // total:  6,  3 rows where tid = 1
            assertEquals(3, list.size(), "");
        }

    }

    @Test
    void testDisableTenantIntercept(){
        try(SqlSession session = sqlSessionFactory.openSession()) {
            TenantDataMapper mapper = session.getMapper(TenantDataMapper.class);

            TenantContextHolder.disableTenantIntercept();

            List<TenantData> list = mapper.list();

            // total:  6
            assertEquals(6, list.size(), "");
        }
    }
}
