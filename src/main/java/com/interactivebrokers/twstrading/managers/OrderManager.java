/**
 * 
 */
package com.interactivebrokers.twstrading.managers;

import java.util.List;

import org.springframework.stereotype.Component;

import com.interactivebrokers.twstrading.domain.Order;
import com.interactivebrokers.twstrading.domain.OrderCritreria;

/**
 * @author telberrak
 *
 */

@Component
public interface OrderManager {
	
	public List<Order> findOrders(OrderCritreria criteria);
 
}
