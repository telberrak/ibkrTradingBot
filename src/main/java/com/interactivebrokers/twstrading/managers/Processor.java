package com.interactivebrokers.twstrading.managers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;

public class Processor {

	private static final Logger logger = LoggerFactory.getLogger(Processor.class);

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

			/*
		
			logger.info("Requesting 5min historical data ");
			// reqRealTimeBars(m_client);
			historicalData5min(m_client, contracts);
			
			Thread.sleep(10000);
			
			logger.info("Cancelling 5min historical data ");
			cancelHistoricalData(m_client, contracts);
			
			*/
			
			logger.info("Requesting 1min historical data ");
			historicalData1min(m_client, contracts);
			
			Thread.sleep(10000);
			
			logger.info("Cancelling 1min historical data ");
			cancelHistoricalData(m_client, contracts);

			/*
			logger.info("Requesting realtime data ");
			reqRealTimeBars(m_client, contracts);
			
			Thread.sleep(300000*12*2);
			
			logger.info("Cancelling realtime data ");
			cancelRealTimeBars(m_client, contracts);
			*/
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
		String formatted = form.format(cal.getTime());

		historicalDataRequests(client, formatted, "1 D", "1 min", contracts);
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

			client.reqHistoricalData(ibcontract.conid(), ibcontract, endDateTime, durationString,
					barSizeString, "TRADES", 1, 1, false, null);
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
	 */
	private List<Contract> getIBContracts() {

		List<com.interactivebrokers.twstrading.domain.Contract> contracts = contractManager.getActiveContracts();
		if (contracts == null || contracts.isEmpty()) {
			logger.info("No contract found");
			return null;
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

			ibcontracts.add(ibcontract);
		}

		return ibcontracts;
	}


}
