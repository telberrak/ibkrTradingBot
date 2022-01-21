package com.interactivebrokers.twstrading.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.interactivebrokers.twstrading.domain.Order;

public interface OrderRepository extends CrudRepository<Order, Long> {

	List<Order> findByConId(Long conId);
}
