package com.kameo.challenger.config;

//import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.kameo.challenger.utils.odb.AnyDAO;
import com.kameo.challenger.utils.odb.AnyDAONew;
import org.jinq.jpa.JinqJPAStreamProvider;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.web.servlet.DispatcherServlet;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.sql.DataSource;

import static org.reflections.util.ConfigurationBuilder.build;


@Configuration
class DatabaseConfig {
    @Inject
    Provider<EntityManager> em;

    @Bean
    @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
    public JinqJPAStreamProvider jinqJPAStreamProvider() {
        return new JinqJPAStreamProvider(anyDao().getEm().getMetamodel());
    }




    @Bean
    public AnyDAO anyDao() {
        return new AnyDAO();
    }


    @Bean
    public AnyDAONew anyDaoNew() {
        return new AnyDAONew(em.get());
    }

/*    @Bean
    public ServletRegistrationBean dispatcherServlet() {
        ServletRegistrationBean registration = new ServletRegistrationBean(
                new DispatcherServlet() {

                }, "/");
        registration.setAsyncSupported(false);
        return registration;
    }*/
    @Bean
    public DataSource dataSource() {

        // no need shutdown, EmbeddedDatabaseFactoryBean will take care of this
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();

        return builder
                .setType(EmbeddedDatabaseType.HSQL) //.H2 or .DERBY
                .ignoreFailedDrops(true)
                .generateUniqueName(true)
                // .addScript("db/sql/create-db.sql")
                // .addScript("db/sql/insert-data.sql")
                .build();
    }
}
