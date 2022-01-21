package com.interactivebrokers.twstrading;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.interactivebrokers.twstrading.managers.BarManager;
import com.interactivebrokers.twstrading.managers.BarManagerImpl;
import com.interactivebrokers.twstrading.managers.ContractManager;
import com.interactivebrokers.twstrading.managers.ContractManagerImpl;
import com.interactivebrokers.twstrading.managers.Processor;
import com.interactivebrokers.twstrading.managers.StrategySimulator;

@Configuration
@EnableJpaRepositories
@EnableTransactionManagement
public class AppConfig {

	@Value("${spring.datasource.driver-class-name}")
	private String driver;

	@Value("${spring.datasource.url}")
	private String url;

	@Value(value = "${spring.datasource.username}")
	private String username;

	@Value("${spring.datasource.password}")
	private String password;
	
	@Value("${spring.jpa.database-platform}")
	private String platform;
	
	@Value("${bar.timeFrame}")
	private String timeFrame;

//	 @Autowired
//	 private KafkaProperties kafkaProperties;
//	

	@Bean
	public DataSource dataSource() {
		DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
		dataSourceBuilder.driverClassName(driver);
		dataSourceBuilder.url(url);
		dataSourceBuilder.username(username);
		dataSourceBuilder.password(password);
		return dataSourceBuilder.build();
	}

	@Bean
	public ContractManager contractManager() {
		return new ContractManagerImpl();
	}

	@Bean
	public BarManager bartManager() {
		return new BarManagerImpl();
	}
	
	
	@Bean
	public Processor processor()
	{
		return new Processor(contractManager(), bartManager());
	}
	
	@Bean
	public StrategySimulator strategySimulator()
	{
		return new StrategySimulator(contractManager(), bartManager());
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setPackagesToScan(new String[] { "com.interactivebrokers.twstrading","com.interactivebrokers.twstrading.domain",
				"com.interactivebrokers.twstrading.simulators", "com.interactivebrokers.twstrading.managers",
				"com.interactivebrokers.twstrading.repositories" });
		factory.setDataSource(dataSource());
		factory.setJpaProperties(additionalProperties());
		return factory;
	}

	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {

		JpaTransactionManager txManager = new JpaTransactionManager();
		txManager.setEntityManagerFactory(entityManagerFactory);
		return txManager;

	}
	
	Properties additionalProperties() {
	    Properties properties = new Properties();
	    properties.setProperty("spring.jpa.database-platform", platform);
	    properties.setProperty("spring.jpa.show-sql","true");
		   
	    return properties;
	}
//	
//	@Bean
//    public Map<String, Object> producerConfigs() {
//        Map<String, Object> props =
//                new HashMap<>(kafkaProperties.buildProducerProperties());
//        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
//                StringSerializer.class);
//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
//                JsonSerializer.class);
//        return props;
//    }
//
//    @Bean
//    public ProducerFactory<String, Object> producerFactory() {
//        return new DefaultKafkaProducerFactory<>(producerConfigs());
//    }
//
//    @Bean
//    public KafkaTemplate<String, Object> kafkaTemplate() {
//        return new KafkaTemplate<>(producerFactory());
//    }
}