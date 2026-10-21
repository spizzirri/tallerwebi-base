package com.valhalla.config;

import jakarta.persistence.EntityManagerFactory;
import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/** Shared JPA base between production and tests: only the DataSource and the dialect differ. */
@Configuration
@org.springframework.context.annotation.Import(
  com.valhalla.infrastructure.email.EmailConfiguration.class
)
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.valhalla.infrastructure")
@ComponentScan("com.valhalla.infrastructure")
public abstract class BaseJpaConfig {
  static {
    // Runs for every subclass (production and test) before any DataSource is built.
    EnvironmentConfig.pinTimeZoneToUtc();
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
    LocalContainerEntityManagerFactoryBean entityManagerFactory =
      new LocalContainerEntityManagerFactoryBean();
    entityManagerFactory.setDataSource(dataSource);
    entityManagerFactory.setPackagesToScan("com.valhalla.domain");
    entityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    entityManagerFactory.setJpaProperties(jpaProperties());
    return entityManagerFactory;
  }

  @Bean
  public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }

  protected abstract Properties jpaProperties();
}
