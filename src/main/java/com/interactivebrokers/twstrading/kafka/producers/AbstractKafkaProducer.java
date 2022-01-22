package com.interactivebrokers.twstrading.kafka.producers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListenerAnnotationBeanPostProcessor;
import org.springframework.kafka.core.KafkaTemplate;

import com.interactivebrokers.twstrading.domain.Bar;

public abstract class AbstractKafkaProducer{

	@Autowired
	protected KafkaTemplate<String, Bar> kafkaTemplate;
	
	public abstract void send(Bar bar, String timeFrame);
}
