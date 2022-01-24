package com.interactivebrokers.twstrading.kafka.producers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.interactivebrokers.twstrading.domain.Bar;

@Service
public class BarProducer extends KafkaProducer {

	
	@Value("${spring.kafka.realtime.topic.price}")
	private String topic;
	
	/**
	 * 
	 * @param tickerId
	 */
	public BarProducer()
	{
		
	}
	
	/**
	 * 
	 * @param bar
	 */
	public void send(Bar bar) {

		send(topic, bar);
	}
	
}
