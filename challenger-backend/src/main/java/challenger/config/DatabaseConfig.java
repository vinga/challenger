package challenger.config;

import com.challenger.eviauth.utils.odb.AnyDAO;
import org.jinq.jpa.JinqJPAStreamProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

/**
 * Created by kmyczkowska on 2016-08-30.
 */
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
