package com.interactivebrokers.twstrading.kafka.listeners;

import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.interactivebrokers.twstrading.domain.Bar;

public abstract class AbstractKafkaListener{

	public abstract void processData(Bar bar);


	
}
