package com.interactivebrokers.twstrading.managers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.interactivebrokers.twstrading.domain.Bar;
import com.interactivebrokers.twstrading.domain.HistoBar;
import com.interactivebrokers.twstrading.kafka.producers.BarProducer;
import com.interactivebrokers.twstrading.repositories.BarRepository;
import com.interactivebrokers.twstrading.repositories.HistoBarRepository;

public class BarManagerImpl implements BarManager {

	private static final Logger logger = Logger.getLogger(BarManagerImpl.class);
	
	@Autowired
	private BarProducer barProducer;
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");
	
	private static final SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");
	
	@Value("${application.simulation}")
	private String simulation;
	
	
	@Autowired 
	private BarRepository barRepository;
	
	@Autowired 
	private HistoBarRepository histoBarRepository;

	public BarManagerImpl() {
		
	}

	@Override
	public void saveBar(int tickerId, long time, double open, double high, double low, double close, long volume, int count, double wap, boolean realTime) {

		Bar bar = new Bar(Calendar.getInstance().getTime(), Long.valueOf(tickerId) ,sdf.format(new Date(time*1000)), open, high, low, close, volume, count, wap, "5mins");
		bar.setRealTime(realTime);
		logger.info("saving bar "+bar.toString());
		try {
			barRepository.save(bar);
		} catch (Exception e) {
			logger.info("Could'nt save bar "+bar.toString(), e);
		}
		barProducer.send(bar);
	}
	

	@Override
	public void updateBar(Bar bar) {		
		
		if(Boolean.parseBoolean(simulation))
		{
			histoBarRepository.updatBar(bar.getBarId(), bar.getEma10(), bar.getEma20(), bar.getVwap(), bar.getRsi());	
		}else
		{
			barRepository.updatBar(bar.getBarId(), bar.getEma10(), bar.getEma20(), bar.getVwap(), bar.getRsi());	
		}

	}

	@Override
	public void saveHistoBar(int tickerId, String time, double open, double high, double low, double close, long volume,
			int count, double wap, boolean realTime) 
	{
		HistoBar bar = new HistoBar(Calendar.getInstance().getTime(), Long.valueOf(tickerId) ,time, open, high, low, close, volume, count, wap, "5mins");
		bar.setRealTime(realTime);
		try {		
			logger.info("saving bar "+bar.toString());
			histoBarRepository.save(bar);
		} catch (Exception e) {
			logger.info("Could'nt save histo bar "+bar.toString(), e);
		}		
		barProducer.send(bar);
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
	

	@Override
	public List<HistoBar> getHistoBarsByBarTime(Long tickerId, String barTime) {
		return histoBarRepository.findByTickerIdAndBarTime(tickerId, barTime);
	}
	

	@Override
	public Bar findYesterdaytBar(Long tickerId, String barTime) {
		return barRepository.findYesterdaytBar(tickerId, barTime);
	}

	@Override
	public List<HistoBar> getHistoBarsByTickerAndTimeframe(Long tickerId, String timeframe) {
		return histoBarRepository.findByTickerIdAndBarTime(tickerId, timeframe);
	}

}