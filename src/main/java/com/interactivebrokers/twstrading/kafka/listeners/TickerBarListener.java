package com.interactivebrokers.twstrading.kafka.listeners;

import java.util.LinkedList;

import javax.annotation.PostConstruct;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.interactivebrokers.twstrading.domain.Bar;
import com.interactivebrokers.twstrading.utils.TradingUtils;

@Service
public class TickerBarListener extends AbstractKafkaListener {

	private static final int TOTAL_TRADING_SESSION_MINUTES = 390;

	private static final Logger logger = Logger.getLogger(TickerBarListener.class);

	@Value("${spring.kafka.realtime.topic.prefix}")
	private String topicPrefix;

	@Value("${spring.kafka.realtime.group.id}")
	private String groupId;

	private String topic;

	@Value("${spring.kafka.realtime.listener.1.ticker.id}")
	private String tickerIdStr;

	private Long tickerId;

	private long cumVolume;
	private double cumPV;
	private long avgVolume;
	
	private double cumBarBody;
	private double avgBarBody;
	
	private double cumSMADistance;
	private double avgSMADistance;
	
	private int window10 = 10;
	private int window20 = 20;
	
	private int noTradingPeriodOpen = 20; //first hour of trading
	private int noTradingPeriodClose = 20; //last hour of trading
	
	private int counter = 1;
	private int positionCounter = 0;
	
	private transient boolean stopTrading = false;
	private transient boolean StartTrading;

	private LinkedList<Bar> bars10 = new LinkedList<Bar>();
	private LinkedList<Bar> bars20 = new LinkedList<Bar>();
	
	private Bar previousBar = null;
	
	private boolean openLongPosition = false;
	private boolean closeLongPosition = false;
	private boolean longPositionOpened = false;
	private boolean openShortPosition = false;
	private boolean closeShortPosition = false;
	private boolean shortPositionOpened = false;
	
	private Double shortStopPrice = 0.0;
	private Double longStopPrice = 0.0;
	
	private Double limitPrice = 0.0;
	private String openPositionTime = null;
	private String closePositionTime = null;

	private Double pnl = 0.0;
	private Double totalPnl = 0.0;
	
	private Integer qty = 1;
	
	public TickerBarListener() {

	}

	@PostConstruct
	void init() {
		tickerId = Long.parseLong(tickerIdStr);
		logger.info("barTime, bar.isBullish(), SMA10 - SMA20, barClose - SMA10, barOpen - SMA10, barClose - SMA20,barOpen - SMA20, avgSMADistance ");
	}

	/**
	 * 
	 * @param tickerId
	 */
	public TickerBarListener(Long tickerId) {
		this.tickerId = Long.parseLong(tickerIdStr);
		StringBuilder sb = new StringBuilder(topicPrefix);
		sb.append(".").append(tickerId);
		topic = sb.toString();
	}

	/**
	 * 
	 * @param bar
	 */
	@KafkaListener(topics = "${spring.kafka.realtime.topic.prefix}")
	public void consume(Bar bar) {
		if (tickerId.equals(bar.getTickerId()))
			processData(bar);
		else
			logger.warn("tickerId=" + bar.getTickerId() + " was ignored");
	}

	@Override
	public void processData(Bar currentBar) {

		logger.info("Processing : bar "+counter+" : "+  currentBar.toString());
		
		StringBuilder sb = new StringBuilder();

		cumVolume += currentBar.getBarVolume();
		cumPV += currentBar.getBarVolume() * (currentBar.getBarHigh() + currentBar.getBarLow() + currentBar.getBarClose()) / 3;
		avgVolume = cumVolume / counter;
		cumBarBody =+ currentBar.body();
		avgBarBody = cumBarBody / counter;	

		
		currentBar.setVwap(cumPV / cumVolume);
		
		bars10.add(currentBar);
		bars20.add(currentBar);

		if (bars10.size() == window10) {
			currentBar.setSma10(TradingUtils.sma(bars10, window10));
			
			bars10.pop();

		}
		if (bars20.size() == window20) {
			currentBar.setSma20(TradingUtils.sma(bars20, window20));
			
			bars20.pop();
		}
		
		if(counter > window10)
		{
			currentBar.setEma10(TradingUtils.ema(previousBar.getEma10() == 0.0 ?previousBar.getSma10() : previousBar.getEma10(), previousBar.getBarClose(), window10));
		}
		if(counter > window20)
		{
			currentBar.setEma20(TradingUtils.ema(previousBar.getEma20() == 0.0 ?previousBar.getSma20() : previousBar.getEma20(), previousBar.getBarClose(), window20));
		}
		
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
			logger.info("COUNTER = "+counter);
			logger.info("TOTAL REALIZED PNL = "+totalPnl);
			
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
		
		cumSMADistance += Math.abs(currentBar.getSma10() - currentBar.getSma20());
		avgSMADistance = (cumSMADistance / counter);
		
		//we don't open a poistion if we have a short position
		if(openLongPosition && !shortPositionOpened)
		{
			limitPrice = currentBar.getBarOpen();
			openLongPosition = false;
			closeLongPosition = false;
			longPositionOpened = true;
			openPositionTime = currentBar.getBarTime();
			
			logger.info(" OOOOOOOOOOOOOOOOOOOOO> Long Position opened at "+openPositionTime+", BUY limit@"+limitPrice +", stop@"+longStopPrice);
			
		}
		
		if(openShortPosition && !longPositionOpened)
		{
			limitPrice = currentBar.getBarOpen();
			openShortPosition = false;
			closeShortPosition = false;
			shortPositionOpened = true;
			
			logger.info(" OOOOOOOOOOOOOOOOOOOOO> Short Position opened at "+openPositionTime+", SELL limit@"+limitPrice +", stop@"+shortStopPrice);
		}
		
		if(closeLongPosition)
		{
			closePositionTime = currentBar.getBarTime();
			
			logger.info("CCCCCCCCCCCCCCCCCCCCCC> Position closed at "+ closePositionTime+", limitPrice@"+currentBar.getBarOpen());

			pnl = (currentBar.getBarOpen() - limitPrice)*qty;
			totalPnl += pnl;
			
			if(pnl < 0.0)
			{
				logger.info("Negative PNL ?!!");
			}
			
			logger.info("############################ Long position Realized pnl from "+openPositionTime+" to "+closePositionTime+" = "+pnl);
			logger.info("############################ Realized totalPnl = "+totalPnl +" points");
					
			initAll();
			
			positionCounter++;
			
			logger.info("########################## "+positionCounter + " positions opened and closed ##########################");
		}
		
		if(closeShortPosition)
		{
			closePositionTime = currentBar.getBarTime();
			
			logger.info("CCCCCCCCCCCCCCCCCCCCCC> Position closed at "+ closePositionTime+", limitPrice@"+currentBar.getBarOpen());
			
			pnl = (limitPrice - currentBar.getBarOpen() ) * qty;
			totalPnl += pnl;
			
			if(pnl < 0.0)
			{
				logger.info("Negative PNL ?!!");
			}
			
			logger.info("############################ Short position Realized pnl from "+openPositionTime+" to "+closePositionTime+" = "+pnl);
			logger.info("############################ Realized totalPnl = "+totalPnl+" points");
			
			initAll();
			
			positionCounter++;
			
			logger.info("########################## "+positionCounter + " positions opened and closed ##########################");
		}
		
		sb.append(currentBar.getBarTime()).append(", ");
		sb.append(currentBar.isBullish() ? "Bullish Bar" : "Bearish bar").append(", ");
		sb.append((currentBar.getSma10() - currentBar.getSma20())).append(", ");
		sb.append((currentBar.getBarOpen() - currentBar.getSma10())).append(", ");
		sb.append((currentBar.getBarClose() - currentBar.getSma20())).append(", ");
		sb.append((currentBar.getBarOpen() - currentBar.getSma20())).append(", ");
		sb.append(avgSMADistance).append(", ");
		sb.append(openLongPosition).append(", ");
		sb.append(closeLongPosition).append(", ");
		
		logger.info(sb.toString());

		/*
		 * no position is opened
		 * the previous bar sma10 is below sma20
		 * the previous bar closes above sma10
		 * the current bar opens above sma10 and closes above sma20
		 * 
		 * open a long position with as toploss equal previous bar low
		 * 
		 */
		
		if(!longPositionOpened && openLongPositionSignal(previousBar, currentBar))
		{
			openLongPosition = true; //open position in the next bar
			longStopPrice = previousBar.getBarLow();
			
			logger.info("===========> Buy signal detected @ "+currentBar.getBarTime()+", Open position at next bar");
		}
		
		//open a short position in the next bar
		if(!shortPositionOpened && openShortPositionSignal(previousBar, currentBar))
		{
			openShortPosition = true; //open position in the next bar
			shortStopPrice = previousBar.getBarHigh();
			
			logger.info("===========> Sell signal detected @ "+currentBar.getBarTime()+", Open position at next bar");
		}
		
		// we close a long position if it was already opened
		if(longPositionOpened 
				&& (engulfingBearish(previousBar, currentBar) || closeLongPositionSignal(previousBar, currentBar)))
		{
			closeLongPosition = true;
			logger.info("===========> Close position signal detected @ "+currentBar.getBarTime());
			
		}
		
				
		// we open a short position
		if(!shortPositionOpened && openShortPositionSignal(previousBar, currentBar))
		{
			openShortPosition = true;
			shortStopPrice = previousBar.getBarHigh(); 
			logger.info("===========> Close position signal detected @ "+currentBar.getBarTime()+" , Open position at next bar");
		}
		
		//we close a short position if it was already opened
		if(shortPositionOpened && (closeShortPositionSignal(previousBar, currentBar)))
		{
			closeShortPosition = true; 
			logger.info("===========> Close short position signal detected @ "+currentBar.getBarTime()+", close position at next bar");
		}
		
		//log position stopped out
		if(longPositionOpened && stoppedOut(currentBar))
		{
			pnl = (longStopPrice - limitPrice) *qty;
			totalPnl += pnl;
			
			logger.info("===========> Long postion stopped out @ "+currentBar.getBarTime()+", pnl="+pnl+", totalPnl="+totalPnl);
			
			initAll(); //init All
		}
		
		//short position stopped out		
		if(shortPositionOpened && stoppedOut(currentBar))
		{
			pnl = (limitPrice - shortStopPrice) *qty;
			totalPnl += pnl;			
			logger.info("===========> Short postion stopped out @ "+currentBar.getBarTime()+", pnl="+pnl+", totalPnl="+totalPnl);
			
			initAll(); //init All
		}

		
		if(openLongPosition && openShortPosition)
		{
			logger.info("Mixed signals received, IGNORE");
			initAll();
		}
		previousBar = currentBar;
		
		counter++;
		
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

	/**
	 * 
	 * @param previousBar
	 * @param bar
	 * @return
	 */
	private boolean openLongPositionSignal(Bar previousBar, Bar bar)
	{
		return longOpenSignal_ema(previousBar, bar);
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
		return shortOpenSignal_ema(previousBar, currentBar);
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
	 * @param bar
	 * @return
	 */
	private boolean longOpenSignal_sma(Bar previousBar, Bar bar)
	{
		if(previousBar.isBullish() && previousBar.body() > avgBarBody 
				&& previousBar.getBarVolume() > avgVolume)
		{
			logger.info("maybe strong signal, long bullish candle with high volume");
		}
		
		if(Math.abs(bar.getSma10() - bar.getSma20()) > avgSMADistance)
		{
			logger.info("distance between SMA10 and SMA20 is getting wider");
		}
		return (bar.getSma20() - bar.getSma10()) > avgSMADistance
		 &&  previousBar.getBarOpen() <  previousBar.getSma10() 
		 &&  previousBar.getBarClose() > previousBar.getSma10() 
		 &&  bar.getBarOpen() > bar.getSma10()
		 && bar.getBarClose() > bar.getSma20();
	}
	
	/**
	 * 
	 * @param previousBar
	 * @param bar
	 * @return
	 */
	private boolean longCloseSignal_sma(Bar previousBar, Bar bar)
	{
		return previousBar.isBearish() 
				&& previousBar.getBarClose() <= previousBar.getSma10()
				&& bar.getBarOpen() <= bar.getSma10()
				&& bar.getBarClose() <= bar.getSma20();
	}
	
	/**
	 * 
	 * @param previousBar
	 * @param currentBar
	 * @return
	 */
	private boolean longOpenSignal_ema(Bar previousBar, Bar currentBar)
	{
		//ema10 was below ema20 in the previous bar and now gets above it
		return previousBar.getEma10() < previousBar.getEma20() && currentBar.getEma10() > currentBar.getEma20();
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
		return previousBar.getEma10() > previousBar.getEma20() && currentBar.getEma10() < currentBar.getEma20();
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
		return previousBar.getEma10() > previousBar.getEma20() && currentBar.getEma10() < currentBar.getEma20();
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
		return previousBar.getEma10() < previousBar.getEma20() && currentBar.getEma10() > currentBar.getEma20();
	}
	
	
	public void stopTrading()
	{
		stopTrading = true;
	}
}