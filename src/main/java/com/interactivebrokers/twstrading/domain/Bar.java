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
	private double ema0;
	@Transient
	private double ema1;
	@Transient
	private double ema2;
	@Transient
	private double vma;
	

	public String getTimeFrame() {
		return timeFrame;
	}

	public void setTimeFrame(String timeFrame) {
		this.timeFrame = timeFrame;
	}

	public double getVma() {
		return vma;
	}

	public void setVma(double vma) {
		this.vma = vma;
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

	public double getEma0() {
		return ema0;
	}

	public void setEma0(double ema0) {
		this.ema0 = ema0;
	}

	public double getEma1() {
		return ema1;
	}

	public void setEma1(double ema1) {
		this.ema1 = ema1;
	}

	public double getEma2() {
		return ema2;
	}

	public void setEma2(double ema2) {
		this.ema2 = ema2;
	}

	@Override
	public int hashCode() {
		return 17 + this.tickerId.hashCode() + this.barTime.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		
		if(!(obj instanceof Bar)) return false;

		return this.tickerId.equals(((Bar)obj).tickerId) && this.barTime.equals(((Bar)obj).barTime);
	}

	@Override
	public String toString() {
		return "Bar [barId=" + barId + ", tickerId=" + tickerId + ", createdOn=" + createdOn + ", barTime=" + barTime
				+ ", barOpen=" + barOpen + ", barHigh=" + barHigh + ", barLow=" + barLow + ", barClose=" + barClose
				+ ", barVolume=" + barVolume + ", barCount=" + barCount + ", barWap=" + barWap + ", timeframe="
				+ timeFrame +", ema0=" + ema0 + ", ema1=" + ema1 + ", ema2=" + ema2
				+ ", vma=" + vma + "]";
	}


}
