package com.interactivebrokers.twstrading.managers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;

public class Processor {

	private static final Logger logger = Logger.getLogger(Processor.class);

	private ContractManager contractManager;

	private BarManager barManager;

	@Value("${connection.host}")
	private String host;

	@Value("${connection.port}")
	private int port;

	public Processor(ContractManager contractManager, BarManager barManager) {
		this.contractManager = contractManager;
		this.barManager = barManager;
	}

	/**
	 * 
	 * @throws InterruptedException
	 */
	public void start() throws InterruptedException {

		// logger.info("Connecting to "+host+":"+port);

		EWrapperImpl wrapper = new EWrapperImpl(contractManager, barManager);

		final EClientSocket m_client = wrapper.getClient();
		final EReaderSignal m_signal = wrapper.getSignal();
		// ! [connect]
		m_client.eConnect("127.0.0.1", 7496, 2);
		// ! [connect]
		// ! [ereader]
		final EReader reader = new EReader(m_client, m_signal);

		reader.start();
		// An additional thread is created in this program design to empty the messaging
		// queue
		new Thread(() -> {
			while (m_client.isConnected()) {
				m_signal.waitForSignal();
				try {
					reader.processMsgs();
				} catch (Exception e) {
					logger.error("Exception: " + e.getMessage(), e);
				}
			}
		}).start();

		Thread.sleep(1000);

		List<Contract> contracts = getIBContracts();

		if (contracts == null || contracts.isEmpty())
			logger.info("No contract found");
		else {

			//contractDetails(m_client,contracts);

			//logger.info("Requesting 5min historical data ");
			// reqRealTimeBars(m_client);
			// historicalData5min(m_client, contracts);

			// Thread.sleep(10000);

			// logger.info("Cancelling 5min historical data ");
			// cancelHistoricalData(m_client, contracts);

			// contractDetails2(m_client);
			/* 
			logger.info("Requesting 1min historical data ");
			historicalData5s(m_client, contracts);

			Thread.sleep(10000);

			logger.info("Cancelling 1min historical data ");
			cancelHistoricalData(m_client, contracts);
			
			*/
			logger.info("Requesting 1min historical data ");
			historicalData5s(m_client, contracts);

			Thread.sleep(60000);

			logger.info("Cancelling 1min historical data ");
			cancelHistoricalData(m_client, contracts);
		
			
			logger.info("Requesting realtime data "); reqRealTimeBars(m_client,
					contracts);
		  
			Thread.sleep(7*60*60*1000);
		  
			logger.info("Cancelling realtime data "); cancelRealTimeBars(m_client,
					contracts);
			
		}
	}

	/**
	 * 
	 * @param client
	 * @throws InterruptedException
	 */
	private void reqRealTimeBars(EClientSocket client, List<Contract> contracts) throws InterruptedException {

		for (Contract contract : contracts) {
			logger.info("Subscribing to realtimebar for [ reqId : " + contract.conid() + "]" + contract.toString());

			client.reqRealTimeBars(contract.conid(), contract, 5, "TRADES", true, null);

		}
	}

	/**
	 * 
	 * @param client
	 * @throws InterruptedException
	 */
	private void historicalData5min(EClientSocket client, List<Contract> contracts) throws InterruptedException {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat form = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		String formatted = form.format(cal.getTime());

		historicalDataRequests(client, formatted, "1 D", "5 mins", contracts);
	}

	/**
	 * 
	 * @param client
	 * @throws InterruptedException
	 */
	private void historicalData1min(EClientSocket client, List<Contract> contracts) throws InterruptedException {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat form = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

		// cal.add(Calendar.DAY_OF_MONTH, -15);
		String formatted = form.format(cal.getTime());

		historicalDataRequests(client, formatted, "5 D", "1 min", contracts);
	}

	/**
	 * 
	 * @param client
	 * @throws InterruptedException
	 */
	private void historicalData5s(EClientSocket client, List<Contract> contracts) throws InterruptedException {

			int offset = 0;
			
			Calendar cal = Calendar.getInstance();
			
			SimpleDateFormat form = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

			cal.add(Calendar.DAY_OF_MONTH, offset);

			String formatted = form.format(cal.getTime());

			historicalDataRequests(client, formatted, "1 D", "5 secs", contracts);

			
	}

	/**
	 * 
	 * @param client
	 * @param contracts
	 */
	private void cancelRealTimeBars(EClientSocket client, List<Contract> contracts) {

		for (Contract ibcontract : contracts) {
			logger.info("Cancelling realtimebar subscription to [ reqId : " + ibcontract.conid() + " ]");
			client.cancelRealTimeBars(ibcontract.conid());
		}
	}

	/**
	 * 
	 * @param client
	 * @param contracts
	 */
	private void cancelHistoricalData(EClientSocket client, List<Contract> contracts) {

		for (Contract ibcontract : contracts) {
			logger.info("Cancelling historical data subscription to [ reqId : " + ibcontract.conid() + "]");
			client.cancelHistoricalData(ibcontract.conid());
		}
	}

	/**
	 * 
	 * @param client
	 * @throws InterruptedException
	 */
	private void historicalDataRequests(EClientSocket client, String endDateTime, String durationString,
			String barSizeString, List<Contract> contracts) throws InterruptedException {

		/*** Requesting historical data ***/

		for (Contract ibcontract : contracts) {

			client.reqHeadTimestamp(ibcontract.conid(), ibcontract, "TRADES", 0, 1);
			client.cancelHeadTimestamp(ibcontract.conid());

			client.reqHistoricalData(ibcontract.conid(), ibcontract, endDateTime, durationString, barSizeString,
					"TRADES", 1, 1, false, null);
		}
		return;
	}

	/**
	 * 
	 * @param client
	 * @throws InterruptedException
	 */
	private void contractDetails(EClientSocket client) throws InterruptedException {

		List<com.interactivebrokers.twstrading.domain.Contract> contracts = contractManager.getContracts();

		if (contracts == null || contracts.isEmpty())
			logger.info("No contract found");

		for (com.interactivebrokers.twstrading.domain.Contract contract : contracts) {

			Contract contract1 = new Contract();
			contract1.symbol(contract.getSymbol());
			contract1.secType(contract.getSecType());
			contract1.currency(contract.getCurrency());
			contract1.exchange(contract.getConExchange());
			contract1.primaryExch(contract.getPrimaryExchange());

			client.reqContractDetails(contract.getConId().intValue(), contract1);
		}
	}

	private void contractDetails(EClientSocket client, List<Contract> contracts) throws InterruptedException {

		int reqId = 55555;

		if (contracts == null || contracts.isEmpty())
			logger.info("No contract found");

		for (Contract contract : contracts) {

			client.reqContractDetails(reqId++, contract);
		}
	}

	private List<Contract> getContracts() throws InterruptedException {

		List<Contract> ibcontracts = new ArrayList<Contract>();

//		Contract contract1 = new Contract();
//		
//		contract1.symbol("SPY");
//		contract1.secType("STK");
//		contract1.currency("USD");
//		contract1.exchange("SMART");
//		contract1.primaryExch("");
//		
//		ibcontracts.add(contract1);
//		
		Contract contract2 = new Contract();
		contract2.symbol("SPY");
		contract2.secType("OPT");
		contract2.currency("USD");
		contract2.exchange("SMART");
		contract2.primaryExch("");		
		contract2.lastTradeDateOrContractMonth("20220126");
		contract2.strike(426);
		contract2.right("P"); 
		contract2.multiplier("100"); 
		ibcontracts.add(contract2);
		
		 
		/*
		 * Contract contract1 = new Contract();
		 * 
		 * contract1.symbol("SPY"); contract1.secType("OPT"); contract1.currency("USD");
		 * contract1.exchange("SMART");
		 * contract1.lastTradeDateOrContractMonth("20220126"); contract1.strike(437);
		 * contract1.right("C"); contract1.multiplier("100"); contract1.primaryExch("");
		 * 
		 * ibcontracts.add(contract1);
		 * 
		 * Contract contract2 = new Contract(); contract2.symbol("SPY");
		 * contract2.secType("OPT"); contract2.currency("USD");
		 * contract2.exchange("SMART");
		 * contract2.lastTradeDateOrContractMonth("20220126"); contract2.strike(436);
		 * contract2.right("C"); contract2.multiplier("100"); contract2.primaryExch("");
		 * 
		 * ibcontracts.add(contract2);
		 * 
		 * Contract contract3 = new Contract(); contract3.symbol("SPY");
		 * contract3.secType("OPT"); contract3.currency("USD");
		 * contract3.exchange("SMART");
		 * contract3.lastTradeDateOrContractMonth("20220126"); contract3.strike(435);
		 * contract3.right("C"); contract3.multiplier("100"); contract3.primaryExch("");
		 * 
		 * ibcontracts.add(contract3);
		 * 
		 * Contract contract10 = new Contract(); contract10.symbol("SPY");
		 * contract10.secType("OPT"); contract10.currency("USD");
		 * contract10.exchange("SMART");
		 * contract10.lastTradeDateOrContractMonth("20220126"); contract10.strike(434);
		 * contract10.right("C"); contract10.multiplier("100");
		 * contract10.primaryExch("");
		 * 
		 * ibcontracts.add(contract10);
		 * 
		 * Contract contract4 = new Contract(); contract4.symbol("SPY");
		 * contract4.secType("OPT"); contract4.currency("USD");
		 * contract4.exchange("SMART");
		 * contract4.lastTradeDateOrContractMonth("20220126"); contract4.strike(433);
		 * contract4.right("C"); contract4.multiplier("100"); contract4.primaryExch("");
		 * 
		 * ibcontracts.add(contract4);
		 * 
		 * Contract contract9 = new Contract(); contract9.symbol("SPY");
		 * contract9.secType("OPT"); contract9.currency("USD");
		 * contract9.exchange("SMART");
		 * contract9.lastTradeDateOrContractMonth("20220126"); contract9.strike(433);
		 * contract9.right("C"); contract9.multiplier("100"); contract9.primaryExch("");
		 * 
		 * ibcontracts.add(contract9);
		 * 
		 * Contract contract5 = new Contract(); contract5.symbol("SPY");
		 * contract5.secType("OPT"); contract5.currency("USD");
		 * contract5.exchange("SMART");
		 * contract5.lastTradeDateOrContractMonth("20220126"); contract5.strike(433);
		 * contract5.right("P"); contract5.multiplier("100"); contract5.primaryExch("");
		 * 
		 * ibcontracts.add(contract5);
		 * 
		 * Contract contract6 = new Contract(); contract6.symbol("SPY");
		 * contract6.secType("OPT"); contract6.currency("USD");
		 * contract6.exchange("SMART");
		 * contract6.lastTradeDateOrContractMonth("20220126"); contract6.strike(432);
		 * contract6.right("P"); contract6.multiplier("100"); contract6.primaryExch("");
		 * 
		 * ibcontracts.add(contract6);
		 * 
		 * Contract contract7 = new Contract(); contract7.symbol("SPY");
		 * contract7.secType("OPT"); contract7.currency("USD");
		 * contract7.exchange("SMART");
		 * contract7.lastTradeDateOrContractMonth("20220126"); contract7.strike(431);
		 * contract7.right("P"); contract7.multiplier("100"); contract7.primaryExch("");
		 * 
		 * ibcontracts.add(contract7);
		 * 
		 * Contract contract8 = new Contract(); contract8.symbol("SPY");
		 * contract8.secType("OPT"); contract8.currency("USD");
		 * contract8.exchange("SMART");
		 * contract8.lastTradeDateOrContractMonth("20220126"); contract8.strike(430);
		 * contract8.right("P"); contract8.multiplier("100"); contract8.primaryExch("");
		 * 
		 * ibcontracts.add(contract8);
		 * 
		 */
		return ibcontracts;

	}

	private static void pnl(EClientSocket client) throws InterruptedException {
		// ! [reqpnl]
		client.reqPnL(17001, "U4253904", "");
		// ! [reqpnl]
		Thread.sleep(10000);
		// ! [cancelpnl]
		client.cancelPnL(17001);
		// ! [cancelpnl]
	}

	private static void pnlSingle(EClientSocket client) throws InterruptedException {
		// ! [reqpnlsingle]
		client.reqPnLSingle(17001, "U4253904", "", 533062664);
		// ! [reqpnlsingle]
		Thread.sleep(1000);
		// ! [cancelpnlsingle]
		client.cancelPnLSingle(17001);
		// ! [cancelpnlsingle]
	}

	private static void ptfPositions(EClientSocket client) throws InterruptedException {
		client.reqPositions();
	}

	private static void optionChains(EClientSocket client) throws InterruptedException {
		Contract contract1 = new Contract();
		contract1.symbol("SPY");
		contract1.secType("OPT");
		contract1.currency("USD");
		contract1.exchange("SMART");

		client.reqSecDefOptParams(0, "IBM", "", "STK", 8314);
	}

	/**
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	private List<Contract> getIBContracts() throws InterruptedException {

		List<com.interactivebrokers.twstrading.domain.Contract> contracts = contractManager.getActiveContracts();
		if (contracts == null || contracts.isEmpty()) {
			return getContracts();
		}

		List<Contract> ibcontracts = new ArrayList<Contract>(contracts.size());

		for (com.interactivebrokers.twstrading.domain.Contract contract : contracts) {
			Contract ibcontract = new Contract();
			ibcontract.conid(contract.getTickerId().intValue());
			ibcontract.symbol(contract.getSymbol());
			ibcontract.secType(contract.getSecType());
			ibcontract.currency(contract.getCurrency());
			ibcontract.exchange(contract.getConExchange());
			ibcontract.primaryExch(contract.getPrimaryExchange());
			ibcontract.strike(contract.getStrike());
			ibcontract.right(contract.getOptRight());
			ibcontract.lastTradeDateOrContractMonth(contract.getLastTradedateOrContractMonth());

			ibcontracts.add(ibcontract);
		}

		return ibcontracts;
	}

}
