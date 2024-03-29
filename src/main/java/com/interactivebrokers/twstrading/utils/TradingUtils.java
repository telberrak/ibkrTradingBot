package com.interactivebrokers.twstrading.utils;

import java.util.LinkedList;
import java.util.List;

import com.interactivebrokers.twstrading.domain.Bar;

public class TradingUtils {
	/**
	 * 
	 * @param bars
	 * @return
	 */
	public static double vwap(List<Bar> bars) {

		return 0.0;
	}

	/**
	 * 
	 * @param previousBar
	 * @param currentBar
	 * @param window
	 * @return
	 */
	public static double rsi(LinkedList<Bar> bars, int window)
	{		
		double avgGain = bars.stream().map(Bar::change).filter(b -> b > 0.0).reduce(0.0,TradingUtils::add)/window;
		double avgLoss = bars.stream().map(Bar::change).filter(b -> b < 0.0).reduce(0.0,TradingUtils::add)/window;
		
		return 100 * (1 - 1./(1 + Math.abs(avgGain/avgLoss))) ;
	}
	/**
	 * 
	 * @param bars
	 * @param window
	 * @return
	 */
	public static double ema(double prevEma, double barClose, int window) {

		double smootingfactor = 2.0/(window +1);
		
		return ((barClose * smootingfactor) + (1 - smootingfactor) * prevEma);
	}

	/**
	 * 
	 * @param bars
	 * @param window
	 * @return
	 */
	public static double sma(LinkedList<Bar> bars, int window) {
		return bars.stream().map(Bar::getBarClose).reduce(0.0,TradingUtils::add)/window;
	}
	
	private static double add(double a, double b)
	{
		return a + b;
	}
	
}
