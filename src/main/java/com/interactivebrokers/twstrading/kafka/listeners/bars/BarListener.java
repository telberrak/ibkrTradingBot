package com.interactivebrokers.twstrading.kafka.listeners.bars;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.interactivebrokers.twstrading.domain.Bar;
import com.interactivebrokers.twstrading.domain.Order;
import com.interactivebrokers.twstrading.domain.TradingAction;
import com.interactivebrokers.twstrading.kafka.producers.OrderProducer;
import com.interactivebrokers.twstrading.managers.BarManager;
import com.interactivebrokers.twstrading.signals.TradingSignal;
import com.interactivebrokers.twstrading.utils.TradingUtils;

@Service
public class BarListener {
	
	private static final Logger logger = Logger.getLogger(BarListener.class);
	
	private static int SESSION_TIME_MULTIPLIER = 1; //1min = 12 x 5s
	
	private static final int TOTAL_TRADING_SESSION_MINUTES = 390 * SESSION_TIME_MULTIPLIER;
	
	@Autowired
	private OrderProducer orderProducer;
	@Autowired
	private BarManager barManager;
	
	@Autowired
	private TradingSignal tradingSignal;

	//@Value("${spring.kafka.realtime.listener.price.tickerid.1}")
	//private String tickerIdStr;
	
	@Value("${application.bartime.steps}")
	private List<String> timeFrameSteps;
	
	@Value("${application.simulation}")
	private String simulation;

	private long cumVolume;
	private double cumPV;
	private long avgVolume;
	
	private double cumBarBody;
	private double avgBarBody;

	private double avgEMADistance;
	private double cumEMADistance;
	
	private int window10 = 10 * SESSION_TIME_MULTIPLIER;
	private int window20 = 20 * SESSION_TIME_MULTIPLIER;
	private int window30 = 30 * SESSION_TIME_MULTIPLIER;
	
	private static Calendar TRADING_SESSION_END_TIME = Calendar.getInstance();
	static
	{
		TRADING_SESSION_END_TIME.set(Calendar.HOUR_OF_DAY, 20);
		TRADING_SESSION_END_TIME.set(Calendar.MINUTE, 54);
		
		logger.info(TRADING_SESSION_END_TIME.getTime());
		
	}
	
	private int counter = 1;
	
	private transient boolean stopTrading = false;

	private LinkedList<Bar> bars10 = new LinkedList<Bar>();
	private LinkedList<Bar> bars20 = new LinkedList<Bar>();
	private LinkedList<Bar> bars30 = new LinkedList<Bar>();
	
	//used for analysis of price action. find candlestick patterns
	private LinkedList<Bar> processed_bars = new LinkedList<Bar>();
	
	private LinkedList<Bar> all_bars = new LinkedList<Bar>();
	
	private Bar previousBar = null;
	
	private boolean longPositionOpened = false;
	private boolean shortPositionOpened = false;
	
	private Double shortStopPrice = 0.0;
	private Double longStopPrice = 0.0;
	
	private Double limitPrice = 0.0;

	private Integer qty = 1;
	
	double acceptablePercentLoss = 0.2;
	
	
	private double tradingSessionHigh = Double.MIN_VALUE; //we start at the min value and will be set to currentBar.getBarHigh
	private double tradingSessionLow = Double.MAX_VALUE;  //we start at the max value and will be set to currentBar.getBarLow
	
	public BarListener() {

	}

	@PostConstruct
	void init() {
		//tickerId = Long.parseLong(tickerIdStr);
	}

	/**
	 * discard bars with timeframe not ending with one of the timeframesteps
	 * @param bar
	 * @return
	 */
	private boolean isToDiscard(Bar bar)
	{
		if(timeFrameSteps == null || timeFrameSteps.isEmpty())
		{
			return false;
		}
		
		if(!timeFrameSteps.contains(bar.getBarTime().substring(bar.getBarTime().length()-2)))
		{
			return true;
		}
		
		bar.setTimeFrame(String.valueOf(60/timeFrameSteps.size())+"S");
		return false;
	}
	/**
	 * 
	 * @param bar
	 */
	@KafkaListener(topics = "${spring.kafka.realtime.topic.price}", groupId = "${spring.kafka.realtime.price.group.id}", containerFactory = "kafkaBarListenerContainerFactory")
	public void consume(Bar bar) {
		
		all_bars.add(bar);
		
		if(isToDiscard(bar))
		{	
			return;
		}
		processData(bar);
	}

	
	/**
	 * 
	 * @param currentBar
	 */
	public void processData(Bar currentBar) {
		
		
		//WE NEED TO CHECK THE BAR TIME CORRESPOND TO SYSTEM TIME IN CASE OF A RESTART
		//WHEN WE RESTART WE WILL GETR ALL HISTO DATA BEFORE REALTIEM DATA AND WE SHOULD NOT
		//CREATE ORDER BASED ON HISTORICAL DATA

		if(stopTrading)
		{
			if(longPositionOpened || shortPositionOpened)
			{
				logger.info("Stop trading signal received, close open postions ");
			}
			return;
		}
		
		logger.info("Processing : bar "+counter+" : ");

		currentBar.setPreviousBar(previousBar);
		
		setIndicators(currentBar);

		if (counter >= window20) // to give enough data for ema
		{
			
			Date currentDate = Boolean.parseBoolean(simulation) ? getDate(currentBar.getBarTime()) : Calendar.getInstance().getTime();
			
			TradingAction tradingAction = tradingSignal.getSignal(processed_bars);
			
			if(currentDate.after(TRADING_SESSION_END_TIME.getTime()))
			{
				logger.info("Trading session ending in 5 min, close long positions");
				
				if(longPositionOpened && currentBar.isRealTime())
				{
					orderProducer.send(new Order(currentBar.getTickerId(),"CLOSE", qty, "BUY", currentBar.getBarClose(), null, null, currentBar.getBarTime()));	
					longPositionOpened = false;
				}
			}
			
			if(currentDate.after(TRADING_SESSION_END_TIME.getTime()))
			{
				logger.info("Trading session ending in 5 min, close short positions");
				
				if(shortPositionOpened && currentBar.isRealTime())
				{
					orderProducer.send(new Order(currentBar.getTickerId(),"CLOSE", qty, "SELL", currentBar.getBarClose(), null, null, currentBar.getBarTime()));
					shortPositionOpened = false;
				}
			}
			
			if(longPositionOpened && TradingAction.SELL.equals(tradingAction))
			{
				logger.warn("==>Long position is opened and Sell signal detected @ "+currentBar.getBarTime()+", Close long position");
			
				if(currentBar.isRealTime())
				{
					orderProducer.send(new Order(currentBar.getTickerId(),"CLOSE", qty, "BUY", currentBar.getBarClose(), null, null, currentBar.getBarTime()));
					longPositionOpened = false;
				}

			}
	
			if(shortPositionOpened && TradingAction.BUY.equals(tradingAction))
			{
				logger.warn("==>Short position is opened and Buy signal detected @ "+currentBar.getBarTime()+", Close short position");
				if(currentBar.isRealTime())
				{
					orderProducer.send(new Order(currentBar.getTickerId(),"CLOSE", qty, "SELL", currentBar.getBarClose(), null, null, currentBar.getBarTime()));
					shortPositionOpened = false;
				}
				
			}
			
			if(!longPositionOpened && TradingAction.BUY.equals(tradingAction))
			{			
				logger.warn("==> No long position opened and Buy signal detected @ "+currentBar.getBarTime()+", Open long position");
							
				limitPrice = currentBar.getBarClose();//the close of this bar will be the open price of the next bar	
	
				longStopPrice = getLongStopPrice(currentBar, limitPrice);
				
				if(currentBar.isRealTime())
				{
					orderProducer.send(new Order(currentBar.getTickerId(),"BUY", 1, "LMT", limitPrice, longStopPrice, "DAY", currentBar.getBarTime()));
					longPositionOpened = true;
				}
			}
					
			if(!shortPositionOpened && TradingAction.SELL.equals(tradingAction))
			{			
				logger.warn("==> No short postion opened and Sell signal detected @ "+currentBar.getBarTime()+", Open short position");
				
				limitPrice = currentBar.getBarClose();
				
				shortStopPrice = getShortStopPrice(currentBar, limitPrice);
				
				if(currentBar.isRealTime())
				{
					orderProducer.send(new Order(currentBar.getTickerId(), "SELL", 1, "LMT", limitPrice, shortStopPrice, "DAY", currentBar.getBarTime()));
					shortPositionOpened = true;
				}
			}
			
			if(longPositionOpened && stoppedOut(currentBar))
			{						
				logger.warn("==> Long postion Stopped out @ : "+currentBar.getBarTime()+"stopPrice="+longStopPrice);
				
				if(currentBar.isRealTime())
				{
					orderProducer.send(new Order(currentBar.getTickerId(),"STOP_BUY", 1, null, null, longStopPrice, null, currentBar.getBarTime()));
					longPositionOpened = false;
				}				
			}
				
			if(shortPositionOpened && stoppedOut(currentBar))
			{
				logger.warn("==> Short postion Stopped out @ : "+currentBar.getBarTime()+"stopPrice="+shortStopPrice);
				
				if(currentBar.isRealTime())
				{
					orderProducer.send(new Order(currentBar.getTickerId(),"STOP_SELL", 1, null, null, shortStopPrice, null, currentBar.getBarTime()));
					shortPositionOpened = false;
				}
			}			
		}
		
		barManager.updateBar(currentBar);
		
		previousBar = currentBar;
				
		logger.info("End processing bar : "+currentBar.toString());
		
		counter++;
	}
	
	
	/**
	 * 
	 * @param currentBar
	 * @param limitPrice
	 * @return
	 */
	private Double getLongStopPrice(Bar currentBar, Double limitPrice)
	{
		
		//return currentBar.getPreviousBar().getPreviousBar().getBarLow();
		
		
		//the min of the last 10 bars
		Double stopPrice = bars10.stream().mapToDouble(b -> b.getBarLow()).min().getAsDouble();
		
		if(stopPrice < limitPrice)
			return stopPrice;
		else
			return limitPrice * (1 - acceptablePercentLoss/100);
			
			
	}

	private Double getShortStopPrice(Bar currentBar, Double limitPrice)
	{
		
		//return currentBar.getPreviousBar().getPreviousBar().getBarHigh();
		
		
		//the min of the last 10 bars
		
		//.double[] dbls = bars10.stream().mapToDouble(b -> b.getBarLow()).toArray();
		
		Double stopPrice = bars10.stream().mapToDouble(b -> b.getBarLow()).min().getAsDouble();
		
		if(stopPrice > limitPrice)
			return stopPrice;
		else
			return limitPrice * (1 + acceptablePercentLoss/100);
			
			
	}

	/**
	 * 
	 * @param currentBar
	 */
	private void setIndicators(Bar currentBar) {
		
		cumVolume += currentBar.getBarVolume();
		cumPV += currentBar.getBarVolume() * (currentBar.getBarHigh() + currentBar.getBarLow() + currentBar.getBarClose()) / 3;
		avgVolume = cumVolume / counter;
		cumBarBody =+ currentBar.body();
		avgBarBody = cumBarBody / counter;	


		cumEMADistance += Math.abs(currentBar.getEma10() - currentBar.getEma20());
		avgEMADistance = (cumEMADistance / counter);

		currentBar.setVwap(cumPV / cumVolume);
		
		bars10.add(currentBar);
		bars20.add(currentBar);
		bars30.add(currentBar);
		
		if (bars10.size() == window10) {
			currentBar.setSma10(TradingUtils.sma(bars10, window10));
			currentBar.setEma10(TradingUtils.ema((currentBar.getPreviousBar() !=null && currentBar.getPreviousBar().getEma10() == 0.0) ? currentBar.getSma10() : currentBar.getPreviousBar().getEma10(), currentBar.getBarClose(), window10));
			bars10.pop();
		
		}
		
		if (bars20.size() == window20) {
			currentBar.setSma20(TradingUtils.sma(bars20, window20));
			currentBar.setEma20(TradingUtils.ema((currentBar.getPreviousBar() !=null && currentBar.getPreviousBar().getEma20() == 0.0) ?currentBar.getSma20()  : currentBar.getPreviousBar().getEma20(), currentBar.getBarClose(), window20));
			bars20.pop();
		}
		
		if (bars30.size() == (window30 + 1)) {

			bars30.pop();
		}
		
		tradingSessionHigh = currentBar.getBarHigh() > tradingSessionHigh ? currentBar.getBarHigh() : tradingSessionHigh;
		tradingSessionLow  = currentBar.getBarLow()  < tradingSessionLow ? currentBar.getBarLow() : tradingSessionLow;
		
		//add the bar to all bars list for analysis
		processed_bars.add(currentBar);
	}
	
	
	private Date getDate(String barTime)
	{
		//"20220127  14:50:00"
		Calendar cal = Calendar.getInstance();
		
		cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(barTime.substring(10, 12)));
		cal.set(Calendar.MINUTE, Integer.valueOf(barTime.substring(13, 15)));
		cal.set(Calendar.SECOND, Integer.valueOf(barTime.substring(16, 18)));
		
		
		return cal.getTime();
	}
	
	/**
	 * 
	 * @param currentBar
	 * @return
	 */
	private boolean stoppedOut(Bar currentBar) {
		
		return (shortPositionOpened && (currentBar.getBarHigh() > shortStopPrice))
				||(longPositionOpened && (currentBar.getBarLow() < longStopPrice));
	}

	public void stopTrading()
	{
		stopTrading = true;
	}
}
