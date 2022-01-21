package com.interactivebrokers.twstrading;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.interactivebrokers.twstrading.managers.Processor;
import com.interactivebrokers.twstrading.managers.StrategySimulator;

@SpringBootApplication
public class Main {

	
	@Autowired private Processor processor;
	
	@Autowired private StrategySimulator strategySimulator;
	
	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			//processor.start();
			
			strategySimulator.startSimulation();

		};
	}
}
