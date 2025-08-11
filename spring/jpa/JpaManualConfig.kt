package batch.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
  basePackages = ["batch.adapter.out.persistence"],
  entityManagerFactoryRef = "dataEntityManagerFactory",
  transactionManagerRef = "dataTransactionManager"
)
@EntityScan(basePackages = ["core.entity"])
class JpaConfig {

  @Bean
  @ConfigurationProperties(prefix = "spring.datasource.business")
  fun businessDataSource(): DataSource {
    return DataSourceBuilder.create().build()
  }

  @Bean
  fun dataEntityManagerFactory(): LocalContainerEntityManagerFactoryBean {
    val em = LocalContainerEntityManagerFactoryBean()

    em.dataSource = businessDataSource()
    em.setPackagesToScan("batch.adapter.out.persistence", "core.entity")
    em.jpaVendorAdapter = HibernateJpaVendorAdapter()

     val properties = HashMap<String, Any>()
    properties["hibernate.hbm2ddl.auto"] = "update"
    properties["hibernate.show_sql"] = "true"
    properties["hibernate.physical_naming_strategy"] =
      "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy"
    properties["hibernate.format_sql"] = "true"
    properties["hibernate.dialect"] = "org.hibernate.dialect.MySQL8Dialect"
    em.setJpaPropertyMap(properties)

    return em
  }

  @Bean
  fun dataTransactionManager(): PlatformTransactionManager {
    val transactionManger = JpaTransactionManager()
    transactionManger.entityManagerFactory = dataEntityManagerFactory().getObject()
    return transactionManger
  }
}
