package com.interactivebrokers.twstrading.listeners;

import com.interactivebrokers.twstrading.domain.Bar;

public abstract class AbstractKafkaListener {

	public abstract void processData(Bar bar);
}
