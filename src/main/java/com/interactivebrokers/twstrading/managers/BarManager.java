package com.interactivebrokers.twstrading.managers;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.interactivebrokers.twstrading.domain.Bar;
import com.interactivebrokers.twstrading.domain.HistoBar;

@Component
public interface BarManager {
	
	public void saveBar(int reqId, long time, double open, double high, double low, double close, long volume, int count, double wap, boolean realTime);
	public void updateBar(Bar bar);
	public void saveHistoBar(int reqId, String time, double open, double high, double low, double close, long volume, int count, double wap, boolean realTime);
	public List<Bar> findBarsByTickerAndDate(Long tickerId, Date date);
	
	
	public Bar findLastBar(Long tickerId, String barTime, String timeFrame);
	public Bar findYesterdaytBar(Long tickerId, String barTime);
	public List<Bar> getBarsByBarTime(Long tickerId, String barTime);
	public List<HistoBar> getHistoBarsByBarTime(Long tickerId, String barTime);
	
	public List<HistoBar> getHistoBarsByTickerAndTimeframe(Long tickerId, String timeframe);

}