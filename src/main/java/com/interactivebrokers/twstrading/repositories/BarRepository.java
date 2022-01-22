package com.interactivebrokers.twstrading.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.interactivebrokers.twstrading.domain.Bar;

public interface BarRepository extends CrudRepository<Bar, Long> {

	
	List<Bar> findByTickerId(Long conId);
	
	//@Query("select b from bars b where b.tickerId =: tickerId and creastedOn =:createdOn")
	//List<Bar> findByTickerIdAndCreatedOn(@Param("tickerId")Long tickerId, @Param("createOn") Date createOn);

	//List<Bar> findByTickerId(Long tickerId);
	
	@Query(value = "select b from bars b where b.tickerId =:tickerId and b.barTime =:barTime and b.timeFrame =:timeFrame")
	Bar findLastBar(@Param("tickerId")Long tickerId, @Param("barTime") String barTime,  @Param("timeFrame") String timeFrame);
	
	@Query(value = "select b from bars b where b.tickerId =:tickerId and b.barTime like %:barTime% order by b.barTime")
	List<Bar> findBarsByBarTime(@Param("tickerId") Long tickerId, @Param("barTime") String barTime);
}
