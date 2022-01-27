/**
 * 
 */
package com.interactivebrokers.twstrading.kafka.listeners.orders;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.interactivebrokers.twstrading.domain.Order;
import com.interactivebrokers.twstrading.managers.BarManager;
import com.interactivebrokers.twstrading.managers.ContractManager;
import com.interactivebrokers.twstrading.managers.OrderManager;

/**
 * @author telberrak
 *
 */
@Service
public class OrderListener {

	
	private static final Logger logger = Logger.getLogger(OrderListener.class);
	
	private static StringBuilder stats = new StringBuilder(">>>>>>>>>>>>>>>>>>>>>>>>>>>> CURRENT STATS <<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
	
	private Map<String,Order> orders = new HashMap<String,Order>();
	
	private OrderManager orderManager;
	
	
	@Autowired
	private ContractManager contractManager;
	
	@Autowired
	private BarManager barManager;
	
	private Double pnl = 0.0;
	private Double totalPnl = 0.0;
	
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
		
		order.setOrderId(Math.round(Math.random()*1000000));
		
		logger.info("Processing order : "+order.toString());
		
		switch (order.getOrderAction()) {
		
			case "BUY":
				
				stats.append("barTime : "+order.getBarTimeDate() +", OrderId : "+order.getOrderId()+", TickerId : "+order.getTickerId()+", BUY "+order.getQuantity()+" limit@"+order.getLimitPrice() +", stop@"+order.getStopPrice()).append("\n");
				
				logger.info("barTime : "+order.getBarTimeDate() +", OrderId : "+order.getOrderId()+", TickerId : "+order.getTickerId()+", BUY "+order.getQuantity()+" limit@"+order.getLimitPrice() +", stop@"+order.getStopPrice());
				
				orders.put(String.valueOf(order.getTickerId())+"_CLOSE_BUY", order);
				
				break;
	
				
			case "SELL":
				
				stats.append("barTime : "+order.getBarTimeDate() +", OrderId : "+order.getOrderId()+", TickerId : "+order.getTickerId()+", SELL "+order.getQuantity()+" limit@"+order.getLimitPrice() +", stop@"+order.getStopPrice()).append("\n");
				
				logger.info("barTime : "+order.getBarTimeDate() +", OrderId : "+order.getOrderId()+", TickerId : "+order.getTickerId()+", SELL "+order.getQuantity()+" limit@"+order.getLimitPrice() +", stop@"+order.getStopPrice());
				
				orders.put(String.valueOf(order.getTickerId())+"_CLOSE_SELL", order);
				
				break;
		
			case "CLOSE":
				
				Order previousOrder = orders.get(String.valueOf(order.getTickerId()+"_CLOSE_"+order.getOrderType()));		
	
				if("BUY".equalsIgnoreCase(order.getOrderType()))
				{
					pnl = (order.getLimitPrice() - previousOrder.getLimitPrice()) * order.getQuantity();
				}
				else {
					pnl = (previousOrder.getLimitPrice() - order.getLimitPrice() ) * order.getQuantity();;
				}
					
				
					
					//IGNORE ORDER IF PNL < 0 it will stop out
				totalPnl += pnl;
				
				stats.append("barTime : "+order.getBarTimeDate() +", OrderId : "+previousOrder.getOrderId()+", TickerId : "+order.getTickerId()+", CLOSE "+previousOrder.getOrderAction()+" POSITION with PNL="+pnl+" and TOTAL PNL="+totalPnl).append("\n");;
				
				logger.info("barTime : "+order.getBarTimeDate() +", OrderId : "+previousOrder.getOrderId()+", TickerId : "+order.getTickerId()+", CLOSE "+previousOrder.getOrderAction()+" POSITION with PNL="+pnl+" and TOTAL PNL="+totalPnl);
				
				break;
				
			case "STOP_BUY":
			case "STOP_SELL":
				
				Order stpBuyOrder = orders.get(String.valueOf(order.getTickerId())+"_CLOSE_"+order.getOrderAction().substring(5));		
	
				//when stopped, it's always negative, unless using trailing stop
				pnl = -Math.abs((stpBuyOrder.getStopPrice() - stpBuyOrder.getLimitPrice()) * order.getQuantity());
				
				totalPnl += pnl;
				
				stats.append("barTime : "+order.getBarTimeDate() +", OrderId : "+stpBuyOrder.getOrderId()+", TickerId : "+stpBuyOrder.getTickerId()+", "+stpBuyOrder.getOrderAction()+" POSITION STOPPED OUT with PNL="+pnl+" and TOTAL PNL="+totalPnl).append("\n");
				
				logger.info("barTime : "+order.getBarTimeDate() +", OrderId : "+stpBuyOrder.getOrderId()+", TickerId : "+stpBuyOrder.getTickerId()+", "+stpBuyOrder.getOrderAction()+" POSITION STOPPED OUT with PNL="+pnl+" and TOTAL PNL="+totalPnl);
				
				break;
				
	
			default:
				break;

			
		}
		logger.info(stats);
	}

}
