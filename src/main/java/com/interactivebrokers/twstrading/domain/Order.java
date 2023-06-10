package com.interactivebrokers.twstrading.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "orders")
public class Order {

	public Order() {
	
	}
	
	
	public Order(Long tickerId, String orderAction, Integer quantity, String orderType, Double limitPrice, Double stopPrice, String timeInForce, String barTimeDate) {
		super();
		this.tickerId = tickerId;
		this.orderAction = orderAction;
		this.quantity = quantity;
		this.orderType = orderType;
		this.limitPrice = limitPrice;
		this.stopPrice = stopPrice;
		this.timeInForce = timeInForce;
		this.barTimeDate = barTimeDate;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long orderId;
	private Long conId;
	private Long tickerId;
	private String orderAction;
	private Integer quantity;
	private String orderType;
	private Double limitPrice;
	private Double stopPrice;
	private String timeInForce;
	private String orderStatus;
	private String commission;
	
	private String barTimeDate;
	
	public Long getOrderId() {
		return orderId;
	}


	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}


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


	public String getOrderAction() {
		return orderAction;
	}


	public void setOrderAction(String orderAction) {
		this.orderAction = orderAction;
	}


	public Integer getQuantity() {
		return quantity;
	}


	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}


	public String getOrderType() {
		return orderType;
	}


	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}


	public Double getLimitPrice() {
		return limitPrice;
	}


	public void setLimitPrice(Double limitPrice) {
		this.limitPrice = limitPrice;
	}


	public Double getStopPrice() {
		return stopPrice;
	}


	public void setStopPrice(Double stopPrice) {
		this.stopPrice = stopPrice;
	}


	public String getTimeInForce() {
		return timeInForce;
	}


	public void setTimeInForce(String timeInForce) {
		this.timeInForce = timeInForce;
	}


	public String getOrderStatus() {
		return orderStatus;
	}


	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}


	public String getCommission() {
		return commission;
	}


	public void setCommission(String commission) {
		this.commission = commission;
	}


	public String getBarTimeDate() {
		return barTimeDate;
	}


	public void setBarTimeDate(String barTimeDate) {
		this.barTimeDate = barTimeDate;
	}


	@Override
	public String toString() {
		return "Order [orderId=" + orderId + ", conId=" + conId + ", tickerId=" + tickerId + ", orderAction="
				+ orderAction + ", quantity=" + quantity + ", orderType=" + orderType + ", limitPrice=" + limitPrice
				+ ", stopPrice=" + stopPrice + ", timeInForce=" + timeInForce + ", orderStatus=" + orderStatus
				+ ", commission=" + commission + "]";
	}
	
	
}
