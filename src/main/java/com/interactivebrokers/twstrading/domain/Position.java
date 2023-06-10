package com.interactivebrokers.twstrading.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "positions")
public class Position {

	public Position() {
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long posId;
	private Long conId;
	private String accountId;
	private Integer position;
	private Double marketPrice;
	private Double marketValue;
	private Double avgCost;
	private Double unrealPnl;
	private Double realPnl;
	
	public Long getPosId() {
		return posId;
	}
	public void setPosId(Long posId) {
		this.posId = posId;
	}
	public Long getConId() {
		return conId;
	}
	public void setConId(Long conId) {
		this.conId = conId;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public Integer getPosition() {
		return position;
	}
	public void setPosition(Integer position) {
		this.position = position;
	}
	public Double getMarketPrice() {
		return marketPrice;
	}
	public void setMarketPrice(Double marketPrice) {
		this.marketPrice = marketPrice;
	}
	public Double getMarketValue() {
		return marketValue;
	}
	public void setMarketValue(Double marketValue) {
		this.marketValue = marketValue;
	}
	public Double getAvgCost() {
		return avgCost;
	}
	public void setAvgCost(Double avgCost) {
		this.avgCost = avgCost;
	}
	public Double getUnrealPnl() {
		return unrealPnl;
	}
	public void setUnrealPnl(Double unrealPnl) {
		this.unrealPnl = unrealPnl;
	}
	public Double getRealPnl() {
		return realPnl;
	}
	public void setRealPnl(Double realPnl) {
		this.realPnl = realPnl;
	}
	@Override
	public String toString() {
		return "Position [posId=" + posId + ", conId=" + conId + ", accountId=" + accountId + ", position=" + position
				+ ", marketPrice=" + marketPrice + ", marketValue=" + marketValue + ", avgCost=" + avgCost
				+ ", unrealPnl=" + unrealPnl + ", realPnl=" + realPnl + ", getPosId()=" + getPosId() + ", getConId()="
				+ getConId() + ", getAccountId()=" + getAccountId() + ", getPosition()=" + getPosition()
				+ ", getMarketPrice()=" + getMarketPrice() + ", getMarketValue()=" + getMarketValue()
				+ ", getAvgCost()=" + getAvgCost() + ", getUnrealPnl()=" + getUnrealPnl() + ", getRealPnl()="
				+ getRealPnl() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}
}
