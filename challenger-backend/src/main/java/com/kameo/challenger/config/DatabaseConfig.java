package com.kameo.challenger.config;
//import com.fasterxml.jackson.module.kotlin.KotlinModule;

import com.kameo.challenger.utils.odb.AnyDAO;
import com.kameo.challenger.utils.odb.AnyDAONew;
import org.jinq.jpa.JinqJPAStreamProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.sql.SQLException;

import static org.bouncycastle.cms.RecipientId.password;

@SuppressWarnings("CanBeFinal")
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

    @Profile("default")
    @Bean
    public DataSource dataSource2() {
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
