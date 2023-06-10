package com.interactivebrokers.twstrading.domain;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Entity(name = "contracts")
public class Contract {

	public Contract() {
	}

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long conId;
	private Long tickerId;
	private String symbol;
	private String secType;
	private String conExchange;
	private String primaryExchange;
	private String currency;
	private Double strike;
	private String lastTradedateOrContractMonth;
	private String optRight;
	private String multiplier;
	private String secId;
	private String secIdType;
	private Date updateDate;
	private boolean isActive;
	@Column(name="histo_received")
	private boolean histoReceived;
	
	public Long getConId() {
		return conId;
	}
	public void setConId(Long conId) {
		this.conId = conId;
	}
	
	public Long getTickerId() {
		return tickerId;
	}
	public void setTickerId(Long tickerId) {
		this.tickerId = tickerId;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getSecType() {
		return secType;
	}
	public void setSecType(String secType) {
		this.secType = secType;
	}
	public String getConExchange() {
		return conExchange;
	}
	public void setConExchange(String conExchange) {
		this.conExchange = conExchange;
	}
	public String getPrimaryExchange() {
		return primaryExchange;
	}
	public void setPrimaryExchange(String primaryExchange) {
		this.primaryExchange = primaryExchange;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public Double getStrike() {
		return strike;
	}
	public void setStrike(Double strike) {
		this.strike = strike;
	}
	public String getLastTradedateOrContractMonth() {
		return lastTradedateOrContractMonth;
	}
	public void setLastTradedateOrContractMonth(String lastTradedateOrContractMonth) {
		this.lastTradedateOrContractMonth = lastTradedateOrContractMonth;
	}
	public String getOptRight() {
		return optRight;
	}
	public void setOptRight(String optRight) {
		this.optRight = optRight;
	}
	public String getMultiplier() {
		return multiplier;
	}
	public void setMultiplier(String multiplier) {
		this.multiplier = multiplier;
	}
	public String getSecId() {
		return secId;
	}
	public void setSecId(String secId) {
		this.secId = secId;
	}
	public String getSecIdType() {
		return secIdType;
	}
	public void setSecIdType(String secIdType) {
		this.secIdType = secIdType;
	}

	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	@Override
	public int hashCode() {
		return Objects.hash(tickerId);
	}
	
	
	public boolean isHistoReceived() {
		return histoReceived;
	}
	public void setHistoReceived(boolean histoReceived) {
		this.histoReceived = histoReceived;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Contract other = (Contract) obj;
		return Objects.equals(tickerId, other.tickerId);
	}
	@Override
	public String toString() {
		return "Contract [conId=" + conId + ", tickerId=" + tickerId + ", symbol=" + symbol + ", secType=" + secType
				+ ", conExchange=" + conExchange + ", primaryExchange=" + primaryExchange + ", currency=" + currency
				+ ", strike=" + strike + ", lastTradedateOrContractMonth=" + lastTradedateOrContractMonth
				+ ", optRight=" + optRight + ", multiplier=" + multiplier + ", secId=" + secId + ", secIdType="
				+ secIdType + ", updateDate=" + updateDate + ", isActive=" + isActive + ", histoReceived="
				+ histoReceived + "]";
	}
	
	
}
