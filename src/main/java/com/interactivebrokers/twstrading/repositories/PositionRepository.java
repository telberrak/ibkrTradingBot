package com.interactivebrokers.twstrading.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.interactivebrokers.twstrading.domain.Position;

public interface PositionRepository extends CrudRepository<Position, Long> {

	List<Position> findByConId(Long conId);
	List<Position> findByAccountId(Long accountId);

}
