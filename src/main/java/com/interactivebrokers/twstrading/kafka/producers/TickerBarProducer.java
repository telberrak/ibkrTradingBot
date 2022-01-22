package com.interactivebrokers.twstrading.kafka.producers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.interactivebrokers.twstrading.domain.Bar;

@Service
public class TickerBarProducer extends AbstractKafkaProducer {

	
	@Value("${spring.kafka.realtime.topic.prefix}")
	private String topicPrefix;
	
	private String topic;
	
	@Autowired
    private KafkaTemplate<String, Bar> kafkaTemplate;
	
	
	/**
	 * 
	 * @param tickerId
	 */
	public TickerBarProducer()
	{
		
	}
	
	/**
	 * 
	 * @param tickerId
	 */
	public TickerBarProducer(Long tickerId)
	{
		StringBuilder sb = new StringBuilder(topicPrefix);
		sb.append(".").append(tickerId);
		
		topic = sb.toString();
	}
	@Override
	public void send(Bar bar, String timeFrame) {

		kafkaTemplate.send(topicPrefix,bar);
	}
	
}
