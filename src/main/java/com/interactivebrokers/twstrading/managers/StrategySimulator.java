 package com.interactivebrokers.twstrading.managers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.interactivebrokers.twstrading.domain.Bar;
import com.interactivebrokers.twstrading.domain.Contract;
import com.interactivebrokers.twstrading.kafka.producers.BarProducer;

public class StrategySimulator {

	
	private static final Logger logger = Logger.getLogger(StrategySimulator.class);
	
	private BarManager barManager;

	private ContractManager contracManager;
	
	@Autowired
	private BarProducer barProducer;
	
	public StrategySimulator(ContractManager contracManager , BarManager barManager) {
		this.contracManager = contracManager;
		this.barManager = barManager;
	}
	
	/**
	 * 
	 */
	public void startSimulation()
	{
		
		List<Contract> contracts = contracManager.getActiveContracts();
		
		if(contracts == null || contracts.isEmpty())
			return;
		
		
		Calendar cal = Calendar.getInstance();
		
		cal.add(Calendar.DAY_OF_MONTH, -4);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
		
		for (Contract contract : contracts) {
			
			
			List<Bar> bars = barManager.getBarsByBarTime(contract.getTickerId(), sdf.format(cal.getTime()));
			
			for(Bar bar : bars)
			{
				
				bar.setRealTime(true);
				barProducer.send(bar);
				
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					
				}
			}
		}
	
		/*
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
		ConcurrentHashMap<Contract,List<Bar>> contractBars = new ConcurrentHashMap<Contract,List<Bar>>();
		
		for (Contract contract : contracts) {
			
			
			//Bar bar = barManager.findLastBar(contract.getTickerId(), sdf.format(cal.getTime()), "1D");
			
			//logger.info("Previous day bar : "+bar.toString());
			
			List<Bar> bars = barManager.findBarsByTickerAndDate(contract.getTickerId(), Calendar.getInstance().getTime());		
			if(bars == null)
				continue;
			
			contractBars.put(contract, bars);
		}
		
		for (Entry<Contract, List<Bar>> entry : contractBars.entrySet()) {
			
			logger.info("Simulating contract "+entry.getKey().toString() );
			
			List<Bar> bars = entry.getValue();
			
			Stream<Bar> stream = bars.stream().filter(b -> b.getBarTime().endsWith("00"));
			
			List<Bar> reducedBar = stream.collect(Collectors.toList());
			
			for(Bar bar : bars)
				tickerBarProducer.send(bar, "5S");			
			
			logger.info(reducedBar.toString());
		}

		*/
		
		
		
		
	}
}
