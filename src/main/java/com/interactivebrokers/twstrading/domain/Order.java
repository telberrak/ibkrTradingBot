package com.interactivebrokers.twstrading.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "orders")
public class Order {

	public Order() {
	
	}
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long orderId;
	private Long conId;
	private String orderAction;
	private Integer quantity;
	private String orderType;
	private Double price;
	private String timeInForce;
	private String orderStatus;
	private String commission;
	
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
	
	public String getOrderAction() {
		return orderAction;
	}
	public void setOrderAction(String orderAction) {
		this.orderAction = orderAction;
	}
	public String getOrderStatus() {
		return orderStatus;
	}
	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
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
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public String getTimeInForce() {
		return timeInForce;
	}
	public void setTimeInForce(String timeInForce) {
		this.timeInForce = timeInForce;
	}
	
	public String getCommission() {
		return commission;
	}
	public void setCommission(String commission) {
		this.commission = commission;
	}
	@Override
	public String toString() {
		return "Order [orderId=" + orderId + ", conId=" + conId + ", orderAction=" + orderAction + ", quantity="
				+ quantity + ", orderType=" + orderType + ", price=" + price + ", timeInForce=" + timeInForce
				+ ", orderStatus=" + orderStatus + ", commission=" + commission + ", getOrderId()=" + getOrderId()
				+ ", getConId()=" + getConId() + ", getOrderAction()=" + getOrderAction() + ", getOrderStatus()="
				+ getOrderStatus() + ", getQuantity()=" + getQuantity() + ", getOrderType()=" + getOrderType()
				+ ", getPrice()=" + getPrice() + ", getTimeInForce()=" + getTimeInForce() + ", getCommission()="
				+ getCommission() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}
	
}
