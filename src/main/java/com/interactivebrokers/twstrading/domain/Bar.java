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
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long barId;
	private Long tickerId;
	private Date createdOn;
	private String barTime;
	private Double barOpen;
	private Double barHigh;
	private Double barLow;
	private Double barClose;
	private Long barVolume;
	private Integer barCount;
	private Double barWap;
	private String timeFrame;
	
	@Transient
	private double ema10;
	@Transient
	private double ema20;
	@Transient
	private double ema9;
	@Transient
	private double ema21;
	@Transient
	private double vwap;
	@Transient
	private double sma10;
	@Transient
	private double sma20;
	@Transient
	private double sma9;
	@Transient
	private double sma21;
	

	
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
		StringBuilder sb = new StringBuilder(this.tickerId.intValue());
		sb.append("_").append(this.barTime);
		return sb.toString();
	}
	
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

	public Date getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	
	public String getBarTime() {
		return barTime;
	}
	public void setBarTime(String barTime) {
		this.barTime = barTime;
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

	public double getEma9() {
		return ema9;
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

	@Override
	public String toString() {
		return "Bar [barId=" + barId + ", tickerId=" + tickerId + ", createdOn=" + createdOn + ", barTime=" + barTime
				+ ", barOpen=" + barOpen + ", barHigh=" + barHigh + ", barLow=" + barLow + ", barClose=" + barClose
				+ ", barVolume=" + barVolume + ", barCount=" + barCount + ", barWap=" + barWap + ", timeFrame="
				+ timeFrame + ", ema10=" + ema10 + ", ema20=" + ema20 + ", ema9=" + ema9 + ", ema21=" + ema21
				+ ", vwap=" + vwap + ", sma10=" + sma10 + ", sma20=" + sma20 + ", sma9=" + sma9 + ", sma21=" + sma21
				+ "]";
	}
}
