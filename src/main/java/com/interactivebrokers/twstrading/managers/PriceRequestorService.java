package com.interactivebrokers.twstrading.managers;

import java.util.List;

import com.interactivebrokers.twstrading.domain.Contract;

public interface PriceRequestorService {
		
	void requestRealTimePrice(Contract contract, String timeframe);
	
	void requestRealTimePrice(List<Contract> contracts, String timeframe);
	
	void requestHistoPrice(Contract contract, String timeframe);
	
	void requestHistoPrice(List<Contract> contracts, String timeframe);
	
	void requestContractDetails(Contract contract);
	
	void requestContractDetails(List<Contract> contracts);
}
