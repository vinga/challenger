package challenger.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;


@TestConfiguration
@Import(DatabaseConfig.class)
public class DatabaseTestConfig {



  /*
    @Inject
    DataSource dataSource;

  @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan(new String[]{UserODB.class.getPackage().getName()});


        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        em.setJpaProperties(properties);
        return em;
    }*/

}
