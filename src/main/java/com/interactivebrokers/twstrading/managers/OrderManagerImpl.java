/**
 * 
 */
package com.interactivebrokers.twstrading.managers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.interactivebrokers.twstrading.domain.Order;
import com.interactivebrokers.twstrading.domain.OrderCritreria;
import com.interactivebrokers.twstrading.repositories.OrderRepository;

/**
 * @author telberrak
 *
 */
public class OrderManagerImpl implements OrderManager{

	
	@Autowired
	private OrderRepository orderRepository;
	/**
	 * 
	 */
	public OrderManagerImpl() {
		
	}

	@Override
	public List<Order> findOrders(OrderCritreria criteria) {
		
		return null;
	}

}
