package com.interactivebrokers.twstrading.managers;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.interactivebrokers.twstrading.domain.Bar;

@Component
public interface BarManager {
	
	public void saveBar(int reqId, long time, double open, double high, double low, double close, long volume, int count, double wap);
	public void saveHistoBar(int reqId, String time, double open, double high, double low, double close, long volume, int count, double wap);
	public List<Bar> findBarsByTickerAndDate(Long tickerId, Date date);
	
	
	public Bar findLastBar(Long tickerId, String barTime, String timeFrame);

}
