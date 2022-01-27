package com.interactivebrokers.twstrading.managers;

import java.util.List;

import com.interactivebrokers.twstrading.domain.Contract;

public interface ContractManager {
	
	public List<Contract> getContracts();
	
	public List<Contract> getActiveContracts();
	
	public Contract saveOrUpdate(int reqId, com.ib.client.Contract contract);
	
	public Contract getContractToTrade(Long tickerId);
	
}
