package com.interactivebrokers.twstrading.kafka.listeners;

import java.util.LinkedList;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.interactivebrokers.twstrading.domain.Bar;
import com.interactivebrokers.twstrading.domain.Order;
import com.interactivebrokers.twstrading.kafka.producers.OrderProducer;
import com.interactivebrokers.twstrading.utils.TradingUtils;

@Service
public class BarListener {
	
	
	@Autowired
	private OrderProducer orderProducer;

	private static final int TOTAL_TRADING_SESSION_MINUTES = 390;

	private static final Logger logger = Logger.getLogger(BarListener.class);

	@Value("${spring.kafka.realtime.listener.price.tickerid.1}")
	private String tickerIdStr;

	private Long tickerId;

	private long cumVolume;
	private double cumPV;
	private long avgVolume;
	
	private double cumBarBody;
	private double avgBarBody;

	private double cumEMADistance;
	private double avgEMADistance;
	
	private int window10 = 10;
	private int window20 = 20;
	private int window30 = 30;
	
	private int noTradingPeriodOpen = 20; //first hour of trading
	private int noTradingPeriodClose = 20; //last hour of trading
	
	private int counter = 1;
	private int positionCounter = 0;
	
	private transient boolean stopTrading = false;
	private transient boolean StartTrading;

	private LinkedList<Bar> bars10 = new LinkedList<Bar>();
	private LinkedList<Bar> bars20 = new LinkedList<Bar>();
	private LinkedList<Bar> bars30 = new LinkedList<Bar>();
	
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
	
	private Double limitPrice = 0.0;
	private String openPositionTime = null;
	private String closePositionTime = null;

	private Double pnl = 0.0;
	private Double totalPnl = 0.0;
	
	private Integer qty = 1;
	
	
	private double tradingSessionHigh = 0.0;
	private double tradingSessionLow = 0.0;
	
	private StringBuilder sbf = new StringBuilder("\n");
	
	public BarListener() {

	}

	@PostConstruct
	void init() {
		tickerId = Long.parseLong(tickerIdStr);
		//logger.info("barId, barTime, bar.isBullish(), Ema10, Ema20, EMA10 - EMA20, barClose - EMA10, barOpen - EMA10, barClose - EMA20,barOpen - EMA20, avgEMADistance, openLongPosition, closeLongPosition, openShortPosition, closeShortPosition");
	}

	/**
	 * 
	 * @param bar
	 */
	@KafkaListener(topics = "${spring.kafka.realtime.topic.price}", groupId = "${spring.kafka.realtime.price.group.id}", containerFactory = "kafkaBarListenerContainerFactory")
	public void consume(Bar bar) {
		if (tickerId.equals(bar.getTickerId()))
			processData(bar);
		else
			logger.warn("tickerId=" + bar.getTickerId() + " was ignored");
	}

	/**
	 * 
	 * @param currentBar
	 */
	public void processData(Bar currentBar) {

		logger.info("Processing : bar "+counter+" : "+  currentBar.toString());
		
		setSessionHighAndLow(currentBar);

		StringBuilder sb = new StringBuilder();

		cumVolume += currentBar.getBarVolume();
		cumPV += currentBar.getBarVolume() * (currentBar.getBarHigh() + currentBar.getBarLow() + currentBar.getBarClose()) / 3;
		avgVolume = cumVolume / counter;
		cumBarBody =+ currentBar.body();
		avgBarBody = cumBarBody / counter;	

		
		currentBar.setVwap(cumPV / cumVolume);
		
		allbars.add(currentBar);
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
		
		if (bars30.size() == window30) {
			
			logger.info("Trend : "+(bars30.getFirst().getBarClose() - bars30.getLast().getBarClose()));
			
			bars30.pop();
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
		
		if(counter == TOTAL_TRADING_SESSION_MINUTES)
		{
			
			logger.info(sbf.toString());
			
			logger.info("COUNTER = "+counter);
			logger.info("POSITION COUNTER = "+positionCounter);
			logger.info("TOTAL REALIZED PNL = "+totalPnl);
			
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

		cumEMADistance += Math.abs(currentBar.getEma10() - currentBar.getEma20());
		avgEMADistance = (cumEMADistance / counter);
		
//		
//		sb.append(currentBar.getBarId()).append(", ");
//		sb.append(currentBar.getBarTime()).append(", ");
//		sb.append(currentBar.isBullish() ? "Bullish" : "Bearish").append(", ");
//		sb.append(currentBar.getEma10()).append(", ");
//		sb.append(currentBar.getEma20()).append(", ");
//		sb.append((currentBar.getEma10() - currentBar.getEma20())).append(", ");
//		sb.append((currentBar.getBarOpen() - currentBar.getEma10())).append(", ");
//		sb.append((currentBar.getBarClose() - currentBar.getEma20())).append(", ");
//		sb.append((currentBar.getBarOpen() - currentBar.getEma20())).append(", ");
//		sb.append(avgEMADistance).append(", ");
//		sb.append(openLongPosition).append(", ");
//		sb.append(closeLongPosition).append(", ");
//		sb.append(openShortPosition).append(", ");
//		sb.append(closeShortPosition).append(", ");
//		sb.append(tradingSessionHigh).append(", ");
//		sb.append(tradingSessionLow).append(", ");
//		
//		logger.info(sb.toString());

		/*
		 * no position is opened
		 * the previous bar sma10 is below sma20
		 * the previous bar closes above sma10
		 * the current bar opens above sma10 and closes above sma20
		 * 
		 * open a long position with as toploss equal previous bar low
		 * 
		 */
		
		if(!longPositionOpened && downTrend(30) && openLongPositionSignal(previousBar, currentBar))
		{
			openLongPosition = true; //open position in the next bar
			longStopPrice = previousBar.getBarLow();

			logger.info("Down Trend : "+(bars30.get(0).getBarClose() - bars30.get(9).getBarClose()));

			logger.warn("===========> Buy signal detected @ "+currentBar.getBarTime()+", Open position at next bar");
		}
		
		/*
		 * conditions:
		 * 		- No short position is still open
		 * 		- previous uptrend
		 * 		- short position signal
		 */
		if(!shortPositionOpened && upTrend(30) && openShortPositionSignal(previousBar, currentBar))
		{
			openShortPosition = true; //open position in the next bar
			shortStopPrice = previousBar.getBarHigh();
			
			logger.info("Up Trend : "+(bars30.get(0).getBarClose() - bars30.get(9).getBarClose()));
			
			logger.warn("===========> Sell signal detected @ "+currentBar.getBarTime()+", Open short position");
		}
		
		// we close a long position if it was already opened
		if(longPositionOpened && closeLongPositionSignal(previousBar, currentBar))
		{
			closeLongPosition = true;
			logger.warn("===========> Close long position signal detected @ "+currentBar.getBarTime());
			
			//we close long position and open short position 
			if(upTrend(30))
			{
				openShortPosition = true;
			}
			
		}
		
		//we close a short position if it was already opened
		if(shortPositionOpened && (closeShortPositionSignal(previousBar, currentBar)))
		{
			closeShortPosition = true; 
			logger.warn("===========> Close short position signal detected @ "+currentBar.getBarTime()+", close position at next bar");
		}
		
		//log position stopped out
		if(longPositionOpened && stoppedOut(currentBar))
		{
			pnl = (longStopPrice - limitPrice) *qty;
			totalPnl += pnl;
			
			double percent = 100*(longStopPrice - limitPrice)/limitPrice;
			
			logger.warn("===========> Long postion stopped out @ "+currentBar.getBarTime()+", pnl="+pnl+", totalPnl="+totalPnl);
			
			sbf.append("Long postion stopped out @ "+currentBar.getBarTime()+", pnl="+pnl+", totalPnl="+totalPnl+ ", price_change= "+percent+"%" );
			sbf.append("\n");
			
			initAll(); //init All
		}
		
		//short position stopped out		
		if(shortPositionOpened && stoppedOut(currentBar))
		{
			pnl = (limitPrice - shortStopPrice) *qty;
			totalPnl += pnl;			
			
			double percent = 100*(limitPrice - shortStopPrice)/limitPrice;
			
			logger.warn("===========> Short postion stopped out @ "+currentBar.getBarTime()+", pnl="+pnl+", totalPnl="+totalPnl);
			
			sbf.append("Short postion stopped out @ "+currentBar.getBarTime()+", pnl="+pnl+", totalPnl="+totalPnl+ ", price_change= "+percent+"%" );
			sbf.append("\n");
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
			limitPrice = currentBar.getBarOpen();
			openLongPosition = false;
			closeLongPosition = false;
			longPositionOpened = true;
			openPositionTime = currentBar.getBarTime();

			logger.warn(" OOOOOOOOOOOOOOOOOOOOO> Long Position opened at "+openPositionTime+", BUY limit@"+limitPrice +", stop@"+longStopPrice);
			
			orderProducer.send(new Order("BUY", 1, "LMT", limitPrice, "DAY"));
			
		}
		
		if(openShortPosition && !longPositionOpened)
		{
			limitPrice = currentBar.getBarOpen();
			openShortPosition = false;
			closeShortPosition = false;
			shortPositionOpened = true;
			openPositionTime = currentBar.getBarTime();
			
			logger.warn(" OOOOOOOOOOOOOOOOOOOOO> Short Position opened at "+openPositionTime+", SELL limit@"+limitPrice +", stop@"+shortStopPrice);
			
			orderProducer.send(new Order("SELL", 1, "LMT", limitPrice, "DAY"));
		}
		
		if(closeLongPosition)
		{
			closePositionTime = currentBar.getBarTime();
			
			logger.warn("CCCCCCCCCCCCCCCCCCCCCC> Position closed at "+ closePositionTime+", limitPrice@"+currentBar.getBarOpen());

			pnl = (currentBar.getBarOpen() - limitPrice)*qty;
			totalPnl += pnl;
			
			if(pnl < 0.0)
			{
				logger.warn("Negative PNL ?!!");
			}
			
			double percent = 100*(limitPrice - currentBar.getBarOpen())/limitPrice;
			
			sbf.append("Long Position opened on "+ openPositionTime +" and closed on "+closePositionTime+", with realize pnl = "+pnl+", totalPnl="+totalPnl+ ", price_change= "+percent+"%" );
			sbf.append("\n");
			
			logger.warn("############################ Long position Realized pnl from "+openPositionTime+" to "+closePositionTime+" = "+pnl);
			logger.warn("############################ Realized totalPnl = "+totalPnl +" points");
					
			initAll();
			
			positionCounter++;
			
			logger.warn("########################## "+positionCounter + " positions opened and closed ##########################");
		}
		
		if(closeShortPosition)
		{
			closePositionTime = currentBar.getBarTime();
			
			logger.warn("CCCCCCCCCCCCCCCCCCCCCC> Position closed at "+ closePositionTime+", limitPrice@"+currentBar.getBarOpen());
			
			pnl = (limitPrice - currentBar.getBarOpen() ) * qty;
			
			double percent = 100*(limitPrice - currentBar.getBarOpen())/limitPrice;
			
			totalPnl += pnl;
			
			if(pnl < 0.0)
			{
				logger.warn("Negative PNL ?!!");
			}
			
			sbf.append("Short Position opened on "+ openPositionTime +" and closed on "+closePositionTime+", with realize pnl = "+pnl +", totalPnl="+totalPnl+ ", price_change= "+percent+"%");
			sbf.append("\n");
			
			logger.warn("############################ Short position Realized pnl from "+openPositionTime+" to "+closePositionTime+" = "+pnl);
			logger.warn("############################ Realized totalPnl = "+totalPnl+" points");
			
			initAll();
			
			positionCounter++;
			
			logger.warn("########################## "+positionCounter + " positions opened and closed ##########################");
		}
					
		previousBar = currentBar;
		
		counter++;
	}

	/**
	 * 
	 * @param currentBar
	 */
	private void setSessionHighAndLow(Bar currentBar) {
		
		if(previousBar == null || previousBar.getBarLow() == 0.0)
		{
			tradingSessionLow = currentBar.getBarLow();
		}else if(currentBar.getBarLow() < previousBar.getBarLow())
		{
			tradingSessionLow = currentBar.getBarLow();
		}else
		{
			tradingSessionLow = previousBar.getBarLow();
		}
		
		if(previousBar == null  || previousBar.getBarLow() == 0.0)
		{
			tradingSessionHigh = currentBar.getBarHigh();
		}else if(currentBar.getBarHigh() > previousBar.getBarHigh())
		{
			tradingSessionHigh = currentBar.getBarHigh();
		}else
		{
			tradingSessionHigh = previousBar.getBarHigh();
		}
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
		return upTrend(30) && shortOpenSignal_ema(previousBar, currentBar);
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
		
		
		boolean signal = downTrend(30) && longOpenSignal_ema_confirm(previousBar, currentBar);
		
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
	
	
	private boolean downTrend(int index)
	{		
		return allbars.get(index).getBarClose() - allbars.getLast().getBarClose() > 1;
	}
	
	private boolean upTrend(int index)
	{
		return allbars.get(index).getBarClose() - allbars.getLast().getBarClose() < 1 ;
	}
	
	public void stopTrading()
	{
		stopTrading = true;
	}
}
