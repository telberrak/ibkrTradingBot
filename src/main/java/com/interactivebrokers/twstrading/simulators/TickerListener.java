package com.interactivebrokers.twstrading.simulators;

import com.interactivebrokers.twstrading.domain.Bar;
import com.interactivebrokers.twstrading.domain.Contract;
import com.interactivebrokers.twstrading.listeners.AbstractKafkaListener;

public class TickerListener extends AbstractKafkaListener{

	public TickerListener(Contract contract) {
		
	}

	@Override
	public void processData(Bar bar) {
		
		
	}
	
	
}
