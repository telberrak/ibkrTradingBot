package com.interactivebrokers.twstrading.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.interactivebrokers.twstrading.domain.Contract;

@Repository
public interface ContractRepository extends CrudRepository<Contract, Long> {

	List<Contract> findByConExchange(String conExchange);
	List<Contract> findBySecType(String secType);
	List<Contract> findBySymbol(String symbol);
	
	@Override
	@Transactional(timeout = 10)
	List<Contract> findAll();
}
