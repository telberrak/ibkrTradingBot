package com.interactivebrokers.twstrading.repositories;

import java.util.List;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.interactivebrokers.twstrading.domain.Contract;

@SpringBootTest
public class ContractRepositoryTest {


	private static final Logger logger = Logger.getLogger(ContractRepositoryTest.class);
	
	@Autowired
	private ContractRepository contractRepository;

	@Test
	public void testContractRepositoryCreate()
	{
		Contract contract0 = new Contract();
		
		contract0.setSymbol("SPY");
		contract0.setSecType("STK");
		contract0.setCurrency("USD");
		contract0.setConExchange("SMART");
		contract0.setPrimaryExchange("ISLAND");
			
		Contract saved0 = contractRepository.save(contract0);
		logger.info(saved0.toString());
		
		//contractRepository.delete(saved0);
		
		Contract contract1 = new Contract();
		
		contract1.setSymbol("QQQ");
		contract1.setSecType("STK");
		contract1.setCurrency("USD");
		contract1.setConExchange("SMART");
		contract1.setPrimaryExchange("ISLAND");
			
		Contract saved1 = contractRepository.save(contract1);
		logger.info(saved1.toString());
		
		//contractRepository.delete(saved1);
		
		Contract contract2 = new Contract();
		
		contract2.setSymbol("IWM");
		contract2.setSecType("STK");
		contract2.setCurrency("USD");
		contract2.setConExchange("SMART");
		contract2.setPrimaryExchange("ISLAND");
			
		Contract saved2 = contractRepository.save(contract2);
		logger.info(saved2.toString());
		
		contractRepository.delete(saved2);
		
	}
	
	
	@Test
	public void testContractRepositoryLoad()
	{
		List<Contract> contracts = contractRepository.findAll();
		
		if(contracts == null)
			return;
		
		for (Contract contract : contracts) {
			logger.info(contract.toString());
		}
		
	}
	
}
