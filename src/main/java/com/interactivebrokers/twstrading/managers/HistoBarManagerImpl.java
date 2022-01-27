package com.interactivebrokers.twstrading.managers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.interactivebrokers.twstrading.domain.Bar;
import com.interactivebrokers.twstrading.kafka.producers.BarProducer;
import com.interactivebrokers.twstrading.repositories.HistoBarRepository;

public class HistoBarManagerImpl implements HistoBarManager {

	private static final Logger logger = Logger.getLogger(HistoBarManagerImpl.class);
	
	@Autowired
	private BarProducer barProducer;
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");
	
	private static final SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");
	
	@Autowired 
	private HistoBarRepository histoBarRepository;

	public HistoBarManagerImpl() {
		
	}

	@Override
	public void saveBar(int tickerId, long time, double open, double high, double low, double close, long volume, int count, double wap) {

		Bar bar = new Bar(Calendar.getInstance().getTime(), Long.valueOf(tickerId) ,sdf.format(new Date(time*1000)), open, high, low, close, volume, count, wap, "5S");
		logger.info("saving bar "+bar.toString());
		histoBarRepository.save(bar);
		barProducer.send(bar);
	}
	

	@Override
	public void updateBar(Bar bar) {		
		histoBarRepository.updatBar(bar.getBarId(), bar.getEma10(), bar.getEma20(), bar.getVwap());
	}

	@Override
	public void saveHistoBar(int tickerId, String time, double open, double high, double low, double close, long volume,
			int count, double wap) {
			Bar bar = new Bar(Calendar.getInstance().getTime(), Long.valueOf(tickerId) ,time, open, high, low, close, volume, count, wap, "1MIN");
			logger.info("saving bar "+bar.toString());
			histoBarRepository.save(bar);
			barProducer.send(bar);
			
	}

	@Override
	public List<Bar> findBarsByTickerAndDate(Long tickerId, Date date) {
		
		return histoBarRepository.findByTickerId(Long.valueOf(tickerId));
	}

	@Override
	public Bar findLastBar(Long tickerId, String barTime, String timeFrame) {
		
		return histoBarRepository.findLastBar(tickerId, barTime, timeFrame);
	}

	@Override
	public List<Bar> getBarsByBarTime(Long tickerId, String barTime) {
		return histoBarRepository.findBarsByBarTime(tickerId, barTime);
	}

	@Override
	public Bar findYesterdaytBar(Long tickerId, String barTime) {
		return histoBarRepository.findYesterdaytBar(tickerId, barTime);
	}
}