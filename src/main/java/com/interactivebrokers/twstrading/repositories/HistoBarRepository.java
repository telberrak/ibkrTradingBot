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

import com.interactivebrokers.twstrading.domain.HistoBar;

/**
 * @author telberrak
 *
 */
public interface HistoBarRepository  extends CrudRepository<HistoBar, Long> {
	
	@Query(value = "select b from histo_bars b where b.tickerId =:tickerId and b.barTime =:barTime and b.timeFrame =:timeFrame")
	HistoBar findLastBar(@Param("tickerId")Long tickerId, @Param("barTime") String barTime,  @Param("timeFrame") String timeFrame);
	
	@Query(value = "select b from histo_bars b where b.tickerId =:tickerId and b.barTime =:barTime and b.timeFrame =:timeFrame")
	HistoBar findYesterdaytBar(@Param("tickerId")Long tickerId, @Param("barTime") String barTime);
	
	@Query(value = "select b from histo_bars b where b.tickerId =:tickerId and b.barTime like %:barTime% order by b.barTime")
	List<HistoBar> findByTickerIdAndBarTime(@Param("tickerId") Long tickerId, @Param("barTime") String barTime);
	
	@Transactional
	@Modifying
	@Query(value = "update histo_bars b set b.ema10 = :ema10, b.ema20 = :ema20, b.vwap = :vwap, b.rsi = :rsi where b.barId = :barId")
	void updatBar(@Param("barId") Long barId, @Param("ema10") double ema10, @Param("ema20") double ema20, @Param("vwap") double vwap, @Param("rsi") double rsi);
	
	@Query(value = "select b from bars b where b.tickerId =:tickerId and b.timeFrame =:timeFrame order by b.timeFrame desc")
	List<HistoBar> findHistoBarsByTickerAndTimeframe(@Param("tickerId")Long tickerId, @Param("timeFrame") String timeFrame);
}
