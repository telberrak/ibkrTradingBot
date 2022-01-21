package com.interactivebrokers.twstrading.managers;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.interactivebrokers.twstrading.domain.Contract;
import com.interactivebrokers.twstrading.repositories.ContractRepository;

public class ContractManagerImpl implements ContractManager {

	private static final Logger logger = LoggerFactory.getLogger(ContractManagerImpl.class);
	
	@Autowired 
	private ContractRepository contractRepository;
	
	public ContractManagerImpl() {
		
	}

	@Override
	public List<Contract> getContracts() {
		
		return contractRepository.findAll();
	}
	
	@Override
	public List<Contract> getActiveContracts() {
		
		return contractRepository.findAll().stream().filter(c -> c.isActive()).collect(Collectors.toList());
	}
	

	@Override
	public Contract saveOrUpdate(int reqId, com.ib.client.Contract ibContract) {
		
		
		Contract contract = null;
		
		Optional<Contract> optionalContract = contractRepository.findById((long)reqId);
		if(optionalContract.isPresent())
		{
			contract = optionalContract.get();
		}else
		{
			contract = new Contract();
		}
				
		contract.setConId((long)reqId);
		contract.setTickerId(Long.valueOf(ibContract.conid()));
		contract.setSymbol(ibContract.symbol());
		contract.setSecType(ibContract.secType().name());
		contract.setConExchange(ibContract.exchange());
		contract.setPrimaryExchange(ibContract.primaryExch());
		contract.setCurrency(ibContract.currency());
		contract.setStrike(ibContract.strike());
		contract.setLastTradedateOrContractMonth(ibContract.lastTradeDateOrContractMonth());
		contract.setOptRight(ibContract.right().name());
		contract.setMultiplier(ibContract.multiplier());
		contract.setSecId(ibContract.secId());
		contract.setSecIdType(ibContract.secIdType().name());
		contract.setUpdateDate(Calendar.getInstance().getTime());

		logger.info("===> Saving : "+contract.toString());
		
		return contractRepository.save(contract);
	}
}
