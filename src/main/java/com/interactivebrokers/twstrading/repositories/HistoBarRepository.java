/**
 * 
 */
package com.interactivebrokers.twstrading.repositories;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.interactivebrokers.twstrading.domain.Bar;

/**
 * @author telberrak
 *
 */
public interface HistoBarRepository  extends CrudRepository<Bar, Long> {

List<Bar> findByTickerId(Long conId);
	
	@Query(value = "select b from bars b where b.tickerId =:tickerId and b.barTime =:barTime and b.timeFrame =:timeFrame")
	Bar findLastBar(@Param("tickerId")Long tickerId, @Param("barTime") String barTime,  @Param("timeFrame") String timeFrame);
	
	@Query(value = "select b from bars b where b.tickerId =:tickerId and b.barTime =:barTime and b.timeFrame =:timeFrame")
	Bar findYesterdaytBar(@Param("tickerId")Long tickerId, @Param("barTime") String barTime);
	
	@Query(value = "select b from bars b where b.tickerId =:tickerId and b.barTime like %:barTime% order by b.barTime")
	List<Bar> findBarsByBarTime(@Param("tickerId") Long tickerId, @Param("barTime") String barTime);
	
	@Transactional
	@Modifying
	@Query(value = "update bars b set b.ema10 = :ema10, b.ema20 = :ema20, b.vwap = :vwap where b.barId = :barId")
	void updatBar(@Param("barId") Long barId, @Param("ema10") double ema10, @Param("ema20") double ema20, @Param("vwap") double vwap);
}
