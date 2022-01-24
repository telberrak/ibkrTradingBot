/**
 * 
 */
package com.interactivebrokers.twstrading.kafka.producers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.interactivebrokers.twstrading.domain.Order;

/**
 * @author telberrak
 *
 */

@Service
public class OrderProducer extends KafkaProducer {

	
	@Value("${spring.kafka.realtime.topic.order}")
	private String topic;
	/**
	 * 
	 */
	public OrderProducer() {}
	
	/**
	 * 
	 * @param order
	 */
	public void send(Order order)
	{
		send(topic, order);
	}

}
