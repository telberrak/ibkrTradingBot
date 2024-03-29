package com.interactivebrokers.twstrading.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;


@Entity(name = "bars")
public class Bar {

	public Bar() {
		
	}

	/**
	 * 
	 * @param createdOn
	 * @param tickerId
	 * @param barTime
	 * @param barOpen
	 * @param barHigh
	 * @param barLow
	 * @param barClose
	 * @param barVolume
	 * @param barCount
	 * @param barWap
	 * @param timeFrame
	 */
	public Bar(Date createdOn, Long tickerId, String barTime, Double barOpen, Double barHigh, Double barLow,
			Double barClose, Long barVolume, Integer barCount, Double barWap, String timeFrame) {
		super();
		this.createdOn = createdOn;
		this.tickerId = tickerId;
		this.barTime = barTime;
		this.barOpen = barOpen;
		this.barHigh = barHigh;
		this.barLow = barLow;
		this.barClose = barClose;
		this.barVolume = barVolume;
		this.barCount = barCount;
		this.barWap = barWap;
		this.timeFrame = timeFrame;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long barId;
	private Long tickerId;
	private String barTime;
	
	private Date createdOn;
	
	private Double barOpen;
	private Double barHigh;
	private Double barLow;
	private Double barClose;
	private Long barVolume;
	private Integer barCount;
	private Double barWap;
	private String timeFrame;
	private double ema10;
	private double ema20;
	private double vwap;
	private double rsi;
	
	@Transient
	private double avgGain;
	
	@Transient
	private double avgLoss;
		
	@Transient 
	private double change;
	
	@Transient
	private boolean realTime;
	@Transient
	private Bar previousBar;
	
	@Transient
	private double ema9;
	@Transient
	private double ema21;
	@Transient
	private double sma10;
	@Transient
	private double sma20;
	@Transient
	private double sma9;
	@Transient
	private double sma21;
	
	@Transient
	private double lookAheadEma10;
	@Transient
	private double lookAheadEma20;

	public Long getBarId() {
		return barId;
	}

	public void setBarId(Long barId) {
		this.barId = barId;
	}

	public Long getTickerId() {
		return tickerId;
	}

	public void setTickerId(Long tickerId) {
		this.tickerId = tickerId;
	}

	public String getBarTime() {
		return barTime;
	}

	public void setBarTime(String barTime) {
		this.barTime = barTime;
	}

	/**
	 * 
	 * @return
	 */
	public Double change()
	{
		if(this.getPreviousBar() != null)
		{
			this.change = this.getBarClose() - this.getPreviousBar().getBarClose();
		}
		
		return this.change;
	}
	
	public double getAvgGain() {
		
		return this.avgGain;
	}

	public void setAvgGain(int window) {
		
		 if(change() > 0)
		 {
			 this.avgGain = ((this.getPreviousBar().getAvgGain() * (window -1) + change()) / window);
		 }else
		 {
			 this.avgGain = this.getPreviousBar() != null ? this.getPreviousBar().getAvgGain()  * (window -1) /window : this.avgGain;
		 }
	}

	public double getAvgLoss() {
		return avgLoss;
	}

	public void setAvgLoss(int window) {
		
		if(change() < 0)
		 {
			 this.avgLoss = (this.getPreviousBar().getAvgLoss() * (window -1) + change()) / window;
		 }else
		 {
			 this.avgLoss = this.getPreviousBar() != null ? this.getPreviousBar().getAvgLoss()  * (window -1) /window  : this.avgLoss;
		 }
	}


	public double average()
	{
		return (barOpen + barLow+ barClose + barHigh)/4.0;
	}
	
	public double pivot()
	{
		return (barHigh + barLow + barClose) / 3.0;
	}
	
	public double r1()
	{
		return 2 * pivot() - barLow;
	}
	
	public double s1()
	{
		return 2 * pivot() - barHigh;
	}
	
	public double r2()
	{
		return pivot() + (barHigh - barLow);
	}
	
	public double s2()
	{
		return pivot() + (barHigh - barLow);
	}
	
	public double body()
	{
		return Math.abs(barOpen - barClose);
	}
	
	public double range()
	{
		return barHigh - barLow;
	}
	
	public double tail()
	{
		return isBearish() ? (barClose - barLow) : (barOpen - barLow); 
	}
	
	public double head()
	{
		return isBearish() ? (barHigh - barOpen) : (barHigh - barClose); 
	}
	/**
	 * 
	 * @return
	 */
	public boolean isBearish()
	{
		return this.getBarClose() < this.getBarOpen();
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isBullish()
	{
		return this.getBarClose() > this.getBarOpen();
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSMA10Breached()
	{
		return (isBullish() && this.getBarOpen() < this.getSma10() && this.getBarClose() > this.getSma10())
				||(isBearish() && this.getBarOpen() > this.getSma10() && this.getBarClose() < this.getSma10());
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSMA200Breached()
	{
		return (isBullish() && this.getBarOpen() < this.getSma20() && this.getBarClose() > this.getSma20())
				||(isBearish() && this.getBarOpen() > this.getSma20() && this.getBarClose() < this.getSma20());
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isEMA10Breached()
	{
		return (isBullish() && this.getBarOpen() < this.getEma10() && this.getBarClose() > this.getEma10())
				||(isBearish() && this.getBarOpen() > this.getEma10() && this.getBarClose() < this.getEma10());
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isEMA200Breached()
	{
		return (isBullish() && this.getBarOpen() < this.getEma20() && this.getBarClose() > this.getEma20())
				||(isBearish() && this.getBarOpen() > this.getEma20() && this.getBarClose() < this.getEma20());
	}

	public boolean isRealTime() {
		return realTime;
	}

	public void setRealTime(boolean realTime) {
		this.realTime = realTime;
	}

	public Bar getPreviousBar() {
		return previousBar;
	}

	public void setPreviousBar(Bar previousBar) {
		this.previousBar = previousBar;
	}

	public String getTimeFrame() {
		return timeFrame;
	}

	public void setTimeFrame(String timeFrame) {
		this.timeFrame = timeFrame;
	}
	
	public double getVwap() {
		return vwap;
	}

	public void setVwap(double vwap) {
		this.vwap = vwap;
	}


	public String getKey()
	{
		StringBuilder sb = new StringBuilder(this.getTickerId().intValue());
		sb.append("_").append(this.getBarTime());
		return sb.toString();
	}
	

	public Date getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	
	public Double getBarOpen() {
		return barOpen;
	}
	public void setBarOpen(Double barOpen) {
		this.barOpen = barOpen;
	}
	public Double getBarHigh() {
		return barHigh;
	}
	public void setBarHigh(Double barHigh) {
		this.barHigh = barHigh;
	}
	public Double getBarLow() {
		return barLow;
	}
	public void setBarLow(Double barLow) {
		this.barLow = barLow;
	}
	public Double getBarClose() {
		return barClose;
	}
	public void setBarClose(Double barClose) {
		this.barClose = barClose;
	}
	public Long getBarVolume() {
		return barVolume;
	}
	public void setBarVolume(Long barVolume) {
		this.barVolume = barVolume;
	}
	public Integer getBarCount() {
		return barCount;
	}
	public void setBarCount(Integer barCount) {
		this.barCount = barCount;
	}
	public Double getBarWap() {
		return barWap;
	}
	public void setBarWap(Double barWap) {
		this.barWap = barWap;
	}

	public double getEma10() {
		return ema10;
	}

	public void setEma10(double ema10) {
		this.ema10 = ema10;
	}

	public double getEma20() {
		return ema20;
	}

	public void setEma20(double ema20) {
		this.ema20 = ema20;
	}

	public void setEma9(double ema9) {
		this.ema9 = ema9;
	}

	public double getEma21() {
		return ema21;
	}

	public void setEma21(double ema21) {
		this.ema21 = ema21;
	}

	public double getSma10() {
		return sma10;
	}

	public void setSma10(double sma10) {
		this.sma10 = sma10;
	}

	public double getSma20() {
		return sma20;
	}

	public void setSma20(double sma20) {
		this.sma20 = sma20;
	}

	public double getSma9() {
		return sma9;
	}

	public void setSma9(double sma9) {
		this.sma9 = sma9;
	}

	public double getSma21() {
		return sma21;
	}

	public void setSma21(double sma21) {
		this.sma21 = sma21;
	}

	public double getLookAheadEma10() {
		return lookAheadEma10;
	}

	public void setLookAheadEma10(double lookAheadEma10) {
		this.lookAheadEma10 = lookAheadEma10;
	}

	public double getLookAheadEma20() {
		return lookAheadEma20;
	}

	public void setLookAheadEma20(double lookAheadEma20) {
		this.lookAheadEma20 = lookAheadEma20;
	}

	public double getRsi() {
		return rsi;
	}

	public void setRsi(double rsi) {
		this.rsi = rsi;
	}

	@Override
	public String toString() {
		return "Bar [barId=" + barId + ", tickerId=" + tickerId + ", barTime=" + barTime + ", createdOn=" + createdOn
				+ ", barOpen=" + barOpen + ", barHigh=" + barHigh + ", barLow=" + barLow + ", barClose=" + barClose
				+ ", barVolume=" + barVolume + ", barCount=" + barCount + ", barWap=" + barWap + ", timeFrame="
				+ timeFrame + ", ema10=" + ema10 + ", ema20=" + ema20 + ", vwap=" + vwap + ", rsi=" + rsi + ", avgGain="
				+ avgGain + ", avgLoss=" + avgLoss + ", change=" + change + ", realTime=" + realTime + "]";
	}

}
