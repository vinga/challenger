package com.kameo.challenger.config;

import com.kameo.challenger.utils.odb.AnyDAO;
import com.kameo.challenger.utils.odb.AnyDAONew;
import org.jinq.jpa.JinqJPAStreamProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;


@Configuration
public class DatabaseConfig {


    @Bean
    @Scope(proxyMode= ScopedProxyMode.TARGET_CLASS)
    public JinqJPAStreamProvider jinqJPAStreamProvider() {
        return new JinqJPAStreamProvider(anyDao().getEm().getMetamodel());
    }
    @Bean
    public AnyDAO anyDao() {
        return new AnyDAO();
    }


    @Bean
    public AnyDAONew anyDaoNew() {
        return new AnyDAONew();
    }


    @Bean
    public DataSource dataSource() {

        // no need shutdown, EmbeddedDatabaseFactoryBean will take care of this
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();

        EmbeddedDatabase db = builder
                .setType(EmbeddedDatabaseType.HSQL) //.H2 or .DERBY
               // .addScript("db/sql/create-db.sql")
               // .addScript("db/sql/insert-data.sql")
                .build();
        return db;
    }
}
