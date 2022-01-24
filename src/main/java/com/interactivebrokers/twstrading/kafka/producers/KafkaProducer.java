package com.interactivebrokers.twstrading.kafka.producers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import com.interactivebrokers.twstrading.domain.Bar;
import com.interactivebrokers.twstrading.domain.Order;

public class KafkaProducer{

	@Autowired
	protected KafkaTemplate<String, Bar> barKafkaTemplate;

	@Autowired
	protected KafkaTemplate<String, Order> orderKafkaTemplate;
	
	
	protected void send(String topic, Bar bar)
	{
		barKafkaTemplate.send   (topic, bar);
	}
	
	protected void send(String topic, Order order)
	{
		orderKafkaTemplate.send(topic, order);
	}

}
