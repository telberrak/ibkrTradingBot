package com.interactivebrokers.twstrading;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.interactivebrokers.twstrading.domain.Bar;
import com.interactivebrokers.twstrading.domain.Order;
import com.interactivebrokers.twstrading.managers.BarManager;
import com.interactivebrokers.twstrading.managers.BarManagerImpl;
import com.interactivebrokers.twstrading.managers.ContractManager;
import com.interactivebrokers.twstrading.managers.ContractManagerImpl;
import com.interactivebrokers.twstrading.managers.OrderManager;
import com.interactivebrokers.twstrading.managers.OrderManagerImpl;
import com.interactivebrokers.twstrading.managers.PositionManager;
import com.interactivebrokers.twstrading.managers.PositionManagerImpl;
import com.interactivebrokers.twstrading.managers.Processor;
import com.interactivebrokers.twstrading.managers.StrategySimulator;
import com.interactivebrokers.twstrading.signals.TradingSignal;

@Configuration
@EnableJpaRepositories
@EnableTransactionManagement
@EnableKafka
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

	@Value("${spring.kafka.server}")
	private String bootstrapServer;
	
	@Value("${spring.kafka.realtime.price.group.id}")
	private String priceGroupId;
	
	@Value("${spring.kafka.realtime.order.group.id}")
	private String orderGroupId;

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

	
//	@Bean
//	public TradingSignal tradingSignal() {
//		return new TradingSignal();
//	}
	@Bean
	public BarManager bartManager() {
		return new BarManagerImpl();
	}
	
	@Bean
	public OrderManager orderManager() {
		return new OrderManagerImpl();
	}
	
	@Bean
	public PositionManager positionManager() {
		return new PositionManagerImpl();
	}

	@Bean
	public Processor processor() {
		return new Processor(contractManager(), bartManager());
	}

	@Bean
	public StrategySimulator strategySimulator() {
		return new StrategySimulator(contractManager(), bartManager());
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setPackagesToScan(new String[] { "com.interactivebrokers.twstrading",
				"com.interactivebrokers.twstrading.domain", "com.interactivebrokers.twstrading.simulators",
				"com.interactivebrokers.twstrading.managers", "com.interactivebrokers.twstrading.repositories" });
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



	/**
	 * 
	 * @return
	 */
	private Properties additionalProperties() {
		Properties properties = new Properties();
		properties.setProperty("spring.jpa.database-platform", platform);
		properties.setProperty("spring.jpa.show-sql", "true");

		return properties;
	}

	/**
	 * 
	 * Kafka config
	 */

	/**
	 * 
	 * @return
	 */
	@Bean
	public ConsumerFactory<String, Bar> barConsumerFactory() {
		Map<String, Object> config = new HashMap<>();

		config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
		config.put(ConsumerConfig.GROUP_ID_CONFIG, priceGroupId);
		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
		return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new JsonDeserializer<>(Bar.class));
	}
	
	/**
	 * 
	 * @return
	 */
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Bar> kafkaBarListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, Bar> factory = new ConcurrentKafkaListenerContainerFactory<String, Bar>();
		factory.setConsumerFactory(barConsumerFactory());
		return factory;
	}
	
	/**
	 * 
	 * @return
	 */
	@Bean
	public ProducerFactory<String, Bar> barProducerFactory() {
		Map<String, Object> config = new HashMap<>();

		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		return new DefaultKafkaProducerFactory<String, Bar>(config);
	}
	
	@Bean
	public KafkaTemplate<String, Bar> barKafkaTemplate() {
		return new KafkaTemplate<String, Bar>(barProducerFactory());
	}
	
	
	
	@Bean
	public ConsumerFactory<String, Order> orderConsumerFactory() {
		Map<String, Object> config = new HashMap<>();

		config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
		config.put(ConsumerConfig.GROUP_ID_CONFIG, orderGroupId);
		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
		return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new JsonDeserializer<>(Order.class));
	}
	

	
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Order> kafkaOrderListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, Order> factory = new ConcurrentKafkaListenerContainerFactory<String, Order>();
		factory.setConsumerFactory(orderConsumerFactory());
		return factory;
	}
		
	
	@Bean
	public ProducerFactory<String, Order> orderProducerFactory() {
		Map<String, Object> config = new HashMap<>();

		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		return new DefaultKafkaProducerFactory<String, Order>(config);
	}

	
	@Bean
	public KafkaTemplate<String, Order> orderKafkaTemplate() {
		return new KafkaTemplate<String, Order>(orderProducerFactory());
	}
	
}