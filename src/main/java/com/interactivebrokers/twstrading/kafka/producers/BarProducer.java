package com.interactivebrokers.twstrading.kafka.producers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.interactivebrokers.twstrading.domain.Bar;
import com.interactivebrokers.twstrading.domain.HistoBar;

@Service
public class BarProducer extends KafkaProducer {

	
	@Value("${spring.kafka.realtime.topic.price}")
	private String topic;

	@Value("${spring.kafka.histo.topic.price}")
	private String histotopic;

	
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
	
	public void send(HistoBar bar) {

		send(histotopic, bar);
	}
	
}
