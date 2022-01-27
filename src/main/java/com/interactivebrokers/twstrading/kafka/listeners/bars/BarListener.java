package com.interactivebrokers.twstrading.kafka.listeners.bars;

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
import com.interactivebrokers.twstrading.kafka.producers.OrderProducer;
import com.interactivebrokers.twstrading.managers.BarManager;
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

	//@Value("${spring.kafka.realtime.listener.price.tickerid.1}")
	//private String tickerIdStr;
	
	@Value("${application.bartime.steps}")
	private List<String> timeFrameSteps;

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
	
	private int noTradingPeriodOpen = 0 * SESSION_TIME_MULTIPLIER; //first hour of trading
	private int noTradingPeriodClose = 0 * SESSION_TIME_MULTIPLIER; //last hour of trading
	
	private int counter = 1;
	
	private transient boolean stopTrading = false;

	private LinkedList<Bar> bars10 = new LinkedList<Bar>();
	private LinkedList<Bar> bars20 = new LinkedList<Bar>();
	private LinkedList<Bar> bars30 = new LinkedList<Bar>();
	
	//used for analysis of price action. find candlestick patterns
	private LinkedList<Bar> allbars = new LinkedList<Bar>();
	
	private Bar previousBar = null;
	
	private boolean openLongPosition = false;
	private boolean closeLongPosition = false;
	private boolean longPositionOpened = false;
	private boolean openShortPosition = false;
	private boolean closeShortPosition = false;
	private boolean shortPositionOpened = false;
	
	private Double shortStopPrice = 0.0;
	private Double longStopPrice = 0.0;
	
	private Double acceptablePercentLoss = 0.20;
	private Double acceptablePercentWin = 0.5;
	
	private Double limitPrice = 0.0;
	private String openPositionTime = null;
	private String closePositionTime = null;

	private Double pnl = 0.0;
	private Double totalPnl = 0.0;
	
	private Integer qty = 1;
	
	
	private double tradingSessionHigh = 0.0;
	private double tradingSessionLow = 0.0;
	
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
		
		if(isToDiscard(bar))
			return;
		processData(bar);
	}

	
	/**
	 * 
	 * @param currentBar
	 */
	public void processData(Bar currentBar) {

		logger.info("Processing : bar "+counter+" : ");

		currentBar.setPreviousBar(previousBar);
		
		setIndicators(currentBar);
		
		
		// we don't have enough bars to calculate indicators
		if(counter <= window20)
		{
			logger.info("No trading started yet, no enough bars to calculate indicators");
			counter++;
			previousBar = currentBar;
			return;
		}

		//we decide not to trade for noTradingPeriodOpen minutes
		if(counter <= noTradingPeriodOpen)
		{
			logger.info("No trading in the first "+noTradingPeriodOpen+" minutes after the session opens.");
			counter++;
			previousBar = currentBar;
			return;
		}
		
		//No trading noTradingPeriodClose before the close
		if(counter > (TOTAL_TRADING_SESSION_MINUTES - noTradingPeriodClose))
		{
			logger.info("No trading "+noTradingPeriodClose+" minutes before the session closes");
			
			counter++;
			return;
		}		
		
		if(stopTrading)
		{
			if(longPositionOpened || shortPositionOpened)
			{
				logger.info("Stop trading signal received, close open postions ");
			}
			return;
		}

		if(!longPositionOpened && openLongPositionSignal(currentBar.getPreviousBar(), currentBar))
		{
			openLongPosition = true; //open position in the next bar
			
			//longStopPrice = currentBar.getPreviousBar().getPreviousBar().getBarLow();
			
			
			
			logger.warn("==> Buy signal detected @ "+currentBar.getBarTime()+", Open position at next bar");
		}
		
		
		if(!shortPositionOpened && openShortPositionSignal(currentBar.getPreviousBar(), currentBar))
		{
			openShortPosition = true; //open position in the next bar

			//openShortPosition = false;
			
			//shortStopPrice =  currentBar.getPreviousBar().getPreviousBar().getBarHigh();
			logger.warn("==> Sell signal detected @ "+currentBar.getBarTime()+", Open short position");
		}
		
		// we close a long position if it was already opened
		if(longPositionOpened && ( longProfitTargetReached(currentBar) || closeLongPositionSignal(currentBar.getPreviousBar(), currentBar)))
		{
			closeLongPosition = true;
			logger.warn("==> Close long position signal detected @ "+currentBar.getBarTime());
			
		}
		
		//we close a short position if it was already opened
		if(shortPositionOpened && (shortProfitTargetReached(currentBar) || closeShortPositionSignal(currentBar.getPreviousBar(), currentBar)))
		{
			closeShortPosition = true; 
			logger.warn("==> Close short position signal detected @ "+currentBar.getBarTime()+", close position at next bar");
		}
		
		//log position stopped out
		if(longPositionOpened && stoppedOut(currentBar))
		{			
			orderProducer.send(new Order(currentBar.getTickerId(),"STOP_BUY", 1, null, null, longStopPrice, null, currentBar.getBarTime()));
			
			initAll(); //init All
		}
		
		//short position stopped out		
		if(shortPositionOpened && stoppedOut(currentBar))
		{
			orderProducer.send(new Order(currentBar.getTickerId(),"STOP_SELL", 1, null, null, shortStopPrice, null, currentBar.getBarTime()));
			
			initAll(); //init All
		}
		
		if(openLongPosition && openShortPosition)
		{
			logger.info("Mixed signals received, IGNORE");
			initAll();
		}
		
		//we don't open a poistion if we have a short position
		if(openLongPosition && !shortPositionOpened)
		{
			limitPrice = currentBar.getBarClose();
			openLongPosition = false;
			closeLongPosition = false;
			longPositionOpened = true;
			openPositionTime = currentBar.getBarTime();
			
			longStopPrice = getLongStopPrice(limitPrice);

			orderProducer.send(new Order(currentBar.getTickerId(),"BUY", 1, "LMT", limitPrice, longStopPrice, "DAY", currentBar.getBarTime()));		
		}
		
		if(openShortPosition && !longPositionOpened)
		{
			limitPrice = currentBar.getBarClose();
			openShortPosition = false;
			closeShortPosition = false;
			shortPositionOpened = true;
			openPositionTime = currentBar.getBarTime();
			
			shortStopPrice = getShortStopPrice(limitPrice);

			orderProducer.send(new Order(currentBar.getTickerId(), "SELL", 1, "LMT", limitPrice, shortStopPrice, "DAY", currentBar.getBarTime()));
		}
		
		if(closeLongPosition)
		{
			closePositionTime = currentBar.getBarTime();

			orderProducer.send(new Order(currentBar.getTickerId(),"CLOSE", qty, "BUY", currentBar.getBarClose(), null, null, currentBar.getBarTime()));
			
			initAll();
		}
		
		if(closeShortPosition)
		{
			closePositionTime = currentBar.getBarTime();
			
			orderProducer.send(new Order(currentBar.getTickerId(),"CLOSE", qty, "SELL", currentBar.getBarClose(), null, null, currentBar.getBarTime()));

			initAll();
			
		}	
		
		barManager.updateBar(currentBar);
		
		previousBar = currentBar;
				
		logger.info("End processing bar : "+currentBar.toString());
		
		counter++;
	}
	
	
	private Double getLongStopPrice(Double limitPrice)
	{
		//the min of the last 10 bars
		Double stopPrice = bars10.stream().mapToDouble(b -> b.getBarLow()).min().getAsDouble();
		
		if(stopPrice < limitPrice)
			return stopPrice;
		else
			return limitPrice * (1 - acceptablePercentLoss/100);
	}

	private Double getShortStopPrice(Double limitPrice)
	{
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
			bars10.pop();
		
		}
		
		if (bars20.size() == window20) {
			currentBar.setSma20(TradingUtils.sma(bars20, window20));
			bars20.pop();
		}
		
		if (bars30.size() == (window30 + 1)) {

			bars30.pop();
		}
		
		if(currentBar.getPreviousBar() !=null)
		{
			currentBar.setEma10(TradingUtils.ema(currentBar.getPreviousBar().getEma10() == 0.0 ?currentBar.getPreviousBar().getBarClose() : currentBar.getPreviousBar().getEma10(), currentBar.getBarClose(), window10));
			currentBar.setEma20(TradingUtils.ema(currentBar.getPreviousBar().getEma20() == 0.0 ?currentBar.getPreviousBar().getBarClose()  : currentBar.getPreviousBar().getEma20(), currentBar.getBarClose(), window20));
		}
					
		
		if(counter > window10)
		{
						
		}
		
		if(counter > window20)
		{
			
		}	
		
		// set session low and high
		if(currentBar.getPreviousBar() == null || currentBar.getPreviousBar().getBarLow() == 0.0)
		{
			tradingSessionLow = currentBar.getBarLow();
		}else if(currentBar.getBarLow() < currentBar.getPreviousBar().getBarLow())
		{
			tradingSessionLow = currentBar.getBarLow();
		}else
		{
			tradingSessionLow = currentBar.getPreviousBar().getBarLow();
		}
		
		if(currentBar.getPreviousBar() == null  || currentBar.getPreviousBar().getBarLow() == 0.0)
		{
			tradingSessionHigh = currentBar.getBarHigh();
		}else if(currentBar.getBarHigh() > currentBar.getPreviousBar().getBarHigh())
		{
			tradingSessionHigh = currentBar.getBarHigh();
		}else
		{
			tradingSessionHigh = currentBar.getPreviousBar().getBarHigh();
		}
		
		//add the bar to all bars list for analysis
		allbars.add(currentBar);
	}

	
	/**
	 * 
	 */
	private void initAll()
	{
		longStopPrice = null;
		shortStopPrice = null;
		limitPrice = null;
		
		openLongPosition = false;
		longPositionOpened =false;
		closeLongPosition = false;		
		
		openShortPosition = false;
		shortPositionOpened =false;
		closeShortPosition = false;
		
		openPositionTime = null;
		closePositionTime = null;

	}
	
	
	private double lookAheadEma20(int lookAheadBarIndex, Bar previousBar, Bar currentBar)
	{
		return 0.0;
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

	/**
	 * 
	 * @param previousBar
	 * @param bar
	 * @return
	 */
	private boolean engulfingBearish(Bar previousBar, Bar bar) {
		logger.info("Bearish engulfing");
		return bar.isBearish() && previousBar.isBullish()
				&& bar.getBarOpen() > previousBar.getBarClose()
				&& bar.getBarClose() < previousBar.getBarOpen();
	}
	
	private boolean longProfitTargetReached(Bar currentBar)
	{
		return currentBar.getBarClose() >= limitPrice * (1 + acceptablePercentWin/100);
	}
	
	private boolean shortProfitTargetReached(Bar currentBar)
	{
		return currentBar.getBarClose() <= limitPrice * (1 - acceptablePercentWin/100);
	}

	/**
	 * 
	 * @param previousBar
	 * @param bar
	 * @return
	 */
	private boolean openLongPositionSignal(Bar previousBar, Bar bar)
	{
		return longOpenSignal_ema(previousBar, bar) && downTrend(bars30) ;
	}
	
	
	/**
	 * 
	 * @param previousBar
	 * @param bar
	 * @return
	 */
	private boolean closeLongPositionSignal(Bar previousBar, Bar bar)
	{
		return longCloseSignal_ema(previousBar, bar);
	}
	
	/**
	 * 
	 * @param previousBar
	 * @param currentBar
	 * @return
	 */
	private boolean openShortPositionSignal(Bar previousBar, Bar currentBar)
	{
		return shortOpenSignal_ema(previousBar, currentBar) && upTrend(bars30) ;
	}
	
	/**
	 * 
	 * @param previousBar
	 * @param currentBar
	 * @return
	 */
	private boolean closeShortPositionSignal(Bar previousBar, Bar currentBar)
	{
		return shortCloseSignal_ema(previousBar, currentBar);
	}
	
	/**
	 * 
	 * @param previousBar
	 * @param currentBar
	 * @return
	 */
	private boolean longOpenSignal_ema(Bar previousBar, Bar currentBar)
	{
		
		
		boolean signal = downTrend(bars30) && longOpenSignal_ema_confirm(previousBar, currentBar);
		
		/*
		if(previousBar.isBullish() && previousBar.body() > avgBarBody 
				&& previousBar.getBarVolume() > avgVolume)
		{
			logger.info("maybe strong signal, long bullish candle with high volume");
		}
		
		if(Math.abs(currentBar.getEma10() - currentBar.getEma20()) > avgEMADistance)
		{
			logger.info("distance between EMA10 and EMA20 is getting wider");
		}
		
		signal =   previousBar.getEma20() - previousBar.getEma10() > avgEMADistance;
		signal &=   currentBar.getEma20() - currentBar.getEma10() < previousBar.getEma20() - previousBar.getEma10(); //distance narrowing
		signal &=   previousBar.getBarOpen() <  previousBar.getEma10();
		signal &= 	previousBar.getBarClose() > previousBar.getEma10();
		signal &= 	currentBar.getBarOpen() > currentBar.getEma10();
		signal &= 	currentBar.getBarClose() > currentBar.getEma20();

		 */
		return signal; 
		
	}
	
	
	/**
	 * 
	 * @param previousBar
	 * @param currentBar
	 * @return
	 */
	private boolean longOpenSignal_ema_confirm
	(Bar previousBar, Bar currentBar)
	{
		//ema10 was below ema20 in the previous bar and now gets above it
		return previousBar.getEma10()>0 && previousBar.getEma20() > 0
				&& previousBar.getEma10() < previousBar.getEma20() 
				&& currentBar.getEma10() > currentBar.getEma20();
	}
	
	/**
	 * 
	 * @param previousBar
	 * @param currentBar
	 * @return
	 */
	private boolean longCloseSignal_ema(Bar previousBar, Bar currentBar)
	{
		//ema10 was above ema20 in the previous bar and now gets below it
		return previousBar.getEma10() > 0 && previousBar.getEma20() > 0
				&& previousBar.getEma10() > previousBar.getEma20() 
				&& currentBar.getEma10() < currentBar.getEma20();
	}
	
	/**
	 * 
	 * @param previousBar
	 * @param currentBar
	 * @return
	 */
	private boolean shortOpenSignal_ema(Bar previousBar, Bar currentBar)
	{
		//ema10 was above ema20 in the previous bar and now gets below it
		return (previousBar.getEma10() > 0 && previousBar.getEma20() > 0
				&& previousBar.getEma10() > previousBar.getEma20() 
				&& currentBar.getEma10() < currentBar.getEma20());
	}
	
	/**
	 * 
	 * @param previousBar
	 * @param currentBar
	 * @return
	 */
	private boolean shortCloseSignal_ema(Bar previousBar, Bar currentBar)
	{
		//ema10 was below ema20 in the previous bar and now gets above it
		return previousBar.getEma10() > 0 && previousBar.getEma20() > 0
				&& previousBar.getEma10() < previousBar.getEma20() 
				&& currentBar.getEma10() > currentBar.getEma20();
	}
	
	
	private boolean downTrend(LinkedList<Bar> bars30)
	{	
	
		return true;
		/*
		boolean downTrend = bars30.size() == window30 && bars30.getLast().getBarClose() - bars30.getFirst().getBarClose() < 0;
		
		if(downTrend)
		{
			logger.info("DOWNTREND : "+(bars30.getLast().getBarClose() - bars30.getFirst().getBarClose()));
		}
		
		return downTrend;
		*/
	}
	
	private boolean upTrend(LinkedList<Bar> bars30)
	{
		return true;
		/*
		boolean upTrend = bars30.size() == window30 && bars30.getLast().getBarClose() - bars30.getFirst().getBarClose() > 0;
		if(upTrend)
		{
			logger.info("UPTREND : "+(bars30.getLast().getBarClose() - bars30.getFirst().getBarClose()));
		}
		
		return upTrend;
		
		*/
	}
	/*
	private boolean downTrend(int index)
	{		
	
		boolean downTrend = allbars.size() > index &&  allbars.get(index).getBarClose() - allbars.getLast().getBarClose() > 1;
		
		if(allbars.size() > index)
		{
			logger.info("DOWNTREND : "+(allbars.getLast().getBarClose() - allbars.get(index).getBarClose()));
		}
		
		return downTrend;
	}
	
	private boolean upTrend(int index)
	{
		
		boolean upTrend = allbars.size() > index && allbars.get(index).getBarClose() - allbars.getLast().getBarClose() > 1 ;
		
		if(allbars.size() > index)
		{
			logger.info("UPTREND : "+(allbars.get(index).getBarClose() - allbars.getLast().getBarClose()));
		}
		
		return upTrend;
	}
	*/
	public void stopTrading()
	{
		stopTrading = true;
	}
}
