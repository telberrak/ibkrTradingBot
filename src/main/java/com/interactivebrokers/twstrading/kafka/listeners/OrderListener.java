/**
 * 
 */
package com.interactivebrokers.twstrading.kafka.listeners;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.interactivebrokers.twstrading.domain.Order;
import com.interactivebrokers.twstrading.managers.OrderManager;

/**
 * @author telberrak
 *
 */
@Service
public class OrderListener {

	
	private static final Logger logger = Logger.getLogger(OrderListener.class);
	
	@Autowired
	private OrderManager orderManager;
	
	/**
	 * 
	 */
	public OrderListener() {
		
	}
	
	@KafkaListener(topics = "${spring.kafka.realtime.topic.order}", groupId = "${spring.kafka.realtime.order.group.id}", containerFactory = "kafkaOrderListenerContainerFactory")
	public void consume(Order order)
	{
		processOrder(order);
	}

	/**
	 * 
	 * @param order
	 */
	private void processOrder(Order order) {
		
		logger.info("Processing order : "+order.toString());
	}

}
