 package com.interactivebrokers.twstrading.managers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.interactivebrokers.twstrading.domain.Contract;
import com.interactivebrokers.twstrading.domain.HistoBar;
import com.interactivebrokers.twstrading.kafka.producers.BarProducer;

public class StrategySimulator {

	
	private static final Logger logger = Logger.getLogger(StrategySimulator.class);
	
	private BarManager barManager;

	private ContractManager contractManager;
	
	@Autowired
	private BarProducer barProducer;
	
	@Value("${spring.kafka.realtime.listener.price.tickerid}")
	private String tickerIdStr;
	
	public StrategySimulator(ContractManager contractManager , BarManager barManager) {
		this.contractManager = contractManager;
		this.barManager = barManager;
	}
	
	/**
	 * 
	 */
	public void startSimulation()
	{
		
		List<Contract> contracts = new ArrayList<Contract>();
		
		if(tickerIdStr != null && !tickerIdStr.trim().equalsIgnoreCase(""))
		{
			com.interactivebrokers.twstrading.domain.Contract contract = contractManager.getContractToTrade(Long.valueOf(tickerIdStr));
		
			if(contract != null)
			{
				contracts.add(contract);
			}
		}
		
		if(contracts.isEmpty())
		{
			contracts = contractManager.getActiveContracts();
		}
		
		if(contracts == null || contracts.isEmpty())
			return;
		
		//int[] offsets = {-3,-4,-7,-8,-9,-10,-11,-14,-15,-16,-17,-18, -19,-20,-21,-22,-23,-24,-25,-26,-27,-28,-29,-30,-31,-32,-33};
		
		int[] offsets = {-1};
		
		for (int offset : offsets) 
		{
			Calendar cal = Calendar.getInstance();
			
			cal.add(Calendar.DAY_OF_MONTH, offset);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			
			for (Contract contract : contracts) {
				
				
				List<HistoBar> bars = barManager.getHistoBarsByBarTime(contract.getTickerId(), sdf.format(cal.getTime()));
				
				for(HistoBar bar : bars)
				{					
					bar.setRealTime(true);
					barProducer.send(bar);
					
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						
					}
				}
			}
		}
	}
}
