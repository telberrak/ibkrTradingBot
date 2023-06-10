package com.interactivebrokers.twstrading.repositories;

import java.util.List;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.interactivebrokers.twstrading.domain.HistoBar;
import com.interactivebrokers.twstrading.managers.BarManager;

@SpringBootTest
public class BarRepositoryTest {

	private static final Logger logger = Logger.getLogger(BarRepositoryTest.class);
	
	@Autowired
	private BarManager barManager;
	
	
	
	
	
	@Test
	public void testRetrieveHistoPrices()
	{
		List<HistoBar> prices = barManager.getHistoBarsByTickerAndTimeframe(265598L, "D");
		
		for(HistoBar price : prices)
		{
			logger.info(price);
		}
	}
	
/*
	@Test
	public void testBarRepositoryCreate()
	{
		
		Contract contract0 = new Contract();
		

		contract0.setSymbol("SPY");
		contract0.setSecType("STK");
		contract0.setCurrency("USD");
		contract0.setConExchange("SMART");
		contract0.setPrimaryExchange("ISLAND");
			
		Contract saved0 = contractRepository.save(contract0);
		
		logger.info(saved0);
		
		Bar bar = new Bar();
		
		bar.setBarClose(460.12);
		bar.setBarHigh(465.23);
		bar.setBarLow(459.23);
		bar.setBarOpen(462.56);
		bar.setBarTime("20220118");
		bar.setBarVolume(256L);
		bar.setBarWap(460.32);
		bar.setBarCount(15);
		bar.setCreatedOn(Calendar.getInstance().getTime());
		bar.setConId(saved0.getConId());
		
		
		Bar saved = barRepository.save(bar);
		
		logger.info(saved);
		
		barRepository.delete(saved);
		
		contractRepository.delete(saved0);
		
	}
	*/
}
