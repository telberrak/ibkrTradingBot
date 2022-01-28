/**
 * 
 */
package com.interactivebrokers.twstrading.signals;

import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interactivebrokers.twstrading.domain.Bar;
import com.interactivebrokers.twstrading.domain.TradingAction;

/**
 * @author telberrak
 *
 */
@Component
public class TradingSignal {
	
	private static final Logger logger = Logger.getLogger(TradingSignal.class);
	
	/**
	 * 
	 */
	public TradingSignal() {
	
	}
	
	/**
	 * 
	 * @param bars
	 * @return
	 */
	public TradingAction getSignal(LinkedList<Bar> bars)
	{	
		if(bars.getLast().getPreviousBar() == null)
			return TradingAction.NONE;
		if(openLongPositionSignal(bars.getLast().getPreviousBar(), bars.getLast()))
			return TradingAction.BUY;
		else if(openShortPositionSignal(bars.getLast().getPreviousBar(), bars.getLast()))
			return TradingAction.SELL;
		else if (closeLongPositionSignal(bars.getLast().getPreviousBar(), bars.getLast()))
			return TradingAction.SELL;
		else if(closeShortPositionSignal(bars.getLast().getPreviousBar(), bars.getLast())) 
			return TradingAction.BUY;
		return TradingAction.NONE;
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
	
	private boolean openLongPositionSignal2(LinkedList<Bar> bars)
	{
				
		boolean signal = bars.size() > 20*12 && (bars.getLast().getBarClose() -  bars.get(bars.size() - 20*12).getBarClose()) < 0;   
		
		signal &=  bars.getLast().getEma20() - bars.getLast().getEma10() < bars.getLast().getPreviousBar().getEma20() - bars.getLast().getPreviousBar().getEma10(); //distance narrowing
		signal &=   bars.getLast().getPreviousBar().getBarOpen() <  bars.getLast().getPreviousBar().getEma10();
		signal &= 	bars.getLast().getPreviousBar().getBarClose() > bars.getLast().getPreviousBar().getEma10();
		signal &= 	bars.getLast().getPreviousBar().getBarOpen() > bars.getLast().getPreviousBar().getEma10();
		signal &= 	bars.getLast().getPreviousBar().getBarClose() > bars.getLast().getPreviousBar().getEma20();
		
		
		return signal;
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
	private boolean engulfingBearish(Bar previousBar, Bar bar) {
		logger.info("Bearish engulfing");
		return bar.isBearish() && previousBar.isBullish()
				&& bar.getBarOpen() > previousBar.getBarClose()
				&& bar.getBarClose() < previousBar.getBarOpen();
	}
	
	
	
	/**
	 * 
	 * @param previousBar
	 * @param currentBar
	 * @return
	 */
	private boolean longOpenSignal_ema(Bar previousBar, Bar currentBar)
	{
		
		
		return previousBar.getEma10()>0 && previousBar.getEma20() > 0
				&& previousBar.getEma10() < previousBar.getEma20() 
				&& currentBar.getEma10() > currentBar.getEma20();
		
		
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

}
