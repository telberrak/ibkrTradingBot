package com.interactivebrokers.twstrading.managers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.interactivebrokers.twstrading.domain.Bar;
import com.interactivebrokers.twstrading.kafka.producers.TickerBarProducer;
import com.interactivebrokers.twstrading.repositories.BarRepository;

public class BarManagerImpl implements BarManager {

	private static final Logger logger = LoggerFactory.getLogger(BarManagerImpl.class);
	
	@Autowired
	private TickerBarProducer tickerBarProducer;
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");
	@Autowired 
	private BarRepository barRepository;

	public BarManagerImpl() {
		
	}

	@Override
	public void saveBar(int tickerId, long time, double open, double high, double low, double close, long volume, int count, double wap) {

		Bar bar = new Bar(Calendar.getInstance().getTime(), Long.valueOf(tickerId) ,sdf.format(new Date(time*1000)), open, high, low, close, volume, count, wap, "5S");
		logger.info("saving bar "+bar.toString());
		barRepository.save(bar);
		tickerBarProducer.send(bar, "5S");
	}

	@Override
	public void saveHistoBar(int tickerId, String time, double open, double high, double low, double close, long volume,
			int count, double wap) {
			Bar bar = new Bar(Calendar.getInstance().getTime(), Long.valueOf(tickerId) ,time, open, high, low, close, volume, count, wap, "1MIN");
			logger.info("saving bar "+bar.toString());
			barRepository.save(bar);
			tickerBarProducer.send(bar, "1MIN");
			
	}

	@Override
	public List<Bar> findBarsByTickerAndDate(Long tickerId, Date date) {
		
		return barRepository.findByTickerId(Long.valueOf(tickerId));
	}

	@Override
	public Bar findLastBar(Long tickerId, String barTime, String timeFrame) {
		
		return barRepository.findLastBar(tickerId, barTime, timeFrame);
	}

	@Override
	public List<Bar> getBarsByBarTime(Long tickerId, String barTime) {
		return barRepository.findBarsByBarTime(tickerId, barTime);
	}
}