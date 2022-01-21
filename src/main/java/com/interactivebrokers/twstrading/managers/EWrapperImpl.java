package com.interactivebrokers.twstrading.managers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ib.client.Bar;
import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDescription;
import com.ib.client.ContractDetails;
import com.ib.client.DeltaNeutralContract;
import com.ib.client.DepthMktDataDescription;
import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReaderSignal;
import com.ib.client.EWrapper;
import com.ib.client.EWrapperMsgGenerator;
import com.ib.client.Execution;
import com.ib.client.FamilyCode;
import com.ib.client.HistogramEntry;
import com.ib.client.HistoricalTick;
import com.ib.client.HistoricalTickBidAsk;
import com.ib.client.HistoricalTickLast;
import com.ib.client.NewsProvider;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.PriceIncrement;
import com.ib.client.SoftDollarTier;
import com.ib.client.TickAttrib;
import com.ib.client.TickAttribBidAsk;
import com.ib.client.TickAttribLast;

@Component
public class EWrapperImpl implements EWrapper {

	private static final Logger logger = LoggerFactory.getLogger(EWrapperImpl.class);
	
	private 
	@Autowired ContractManager contractManager;
	
	@Autowired BarManager barManager;
	
	// ! [socket_declare]
	private EReaderSignal readerSignal;
	private EClientSocket clientSocket;
	protected int currentOrderId = -1;
	// ! [socket_declare]

	// ! [socket_init]
	public EWrapperImpl( ContractManager contractManager, BarManager barManager) {
		this.contractManager = contractManager;
		this.barManager = barManager;
		this.readerSignal = new EJavaSignal();
		this.clientSocket = new EClientSocket(this, readerSignal);
	}

	// ! [socket_init]
	public EClientSocket getClient() {
		return clientSocket;
	}

	public EReaderSignal getSignal() {
		return readerSignal;
	}
	

	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
		logger.info(EWrapperMsgGenerator.contractDetails(reqId, contractDetails));
		
		
		logger.info("Saving contract details "+contractManager);
		contractManager.saveOrUpdate(reqId, contractDetails.contract());
		
	}


	@Override
	public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume,
			double wap, int count) {

			logger.info(EWrapperMsgGenerator.realtimeBar(reqId, time, open, high, low, close, volume, wap, count));
			barManager.saveBar(reqId, time, open, high, low, close, volume, count, wap);
	}


	@Override
	public void historicalData(int reqId, Bar bar) {
		logger.info(EWrapperMsgGenerator.historicalData(reqId, bar.time(), bar.open(), bar.high(), bar.low(), bar.close(), bar.volume(), bar.count(), bar.wap()));
		
		barManager.saveHistoBar(reqId, bar.time(), bar.open(), bar.high(), bar.low(), bar.close(), bar.volume(), bar.count(), bar.wap());
	}

	

	@Override
	public void tickPrice(int tickerId, int field, double price, TickAttrib attrib) {
		
		logger.info(EWrapperMsgGenerator.tickPrice(tickerId, field, price, attrib));
	}

	@Override
	public void tickSize(int tickerId, int field, int size) {
		logger.info(EWrapperMsgGenerator.tickSize(tickerId, field, size));
	}

	@Override
	public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice,
			double pvDividend, double gamma, double vega, double theta, double undPrice) {
		logger.info(EWrapperMsgGenerator.tickOptionComputation(tickerId, field, impliedVol, delta, optPrice, pvDividend, gamma, vega, theta, undPrice));
	}

	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		logger.info(EWrapperMsgGenerator.tickGeneric(tickerId, tickType, value));
	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
		logger.info(EWrapperMsgGenerator.tickString(tickerId, tickType, value));
	}

	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints,
			double impliedFuture, int holdDays, String futureLastTradeDate, double dividendImpact,
			double dividendsToLastTradeDate) {
		logger.info(EWrapperMsgGenerator.tickEFP(tickerId, tickType, basisPoints, formattedBasisPoints, impliedFuture, holdDays, futureLastTradeDate, dividendImpact, dividendsToLastTradeDate));

	}

	@Override
	public void orderStatus(int orderId, String status, double filled, double remaining, double avgFillPrice,
			int permId, int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
		logger.info(EWrapperMsgGenerator.orderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld, mktCapPrice));
	}

	@Override
	public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
		logger.info(EWrapperMsgGenerator.openOrder(orderId, contract, order, orderState));
	}

	@Override
	public void openOrderEnd() {
		logger.info(EWrapperMsgGenerator.openOrderEnd());
	}

	@Override
	public void updateAccountValue(String key, String value, String currency, String accountName) {
		logger.info(EWrapperMsgGenerator.updateAccountValue(key, value, currency, accountName));
	}

	@Override
	public void updatePortfolio(Contract contract, double position, double marketPrice, double marketValue,
			double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
		logger.info(EWrapperMsgGenerator.updatePortfolio(contract, position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL, accountName));
	}

	@Override
	public void updateAccountTime(String timeStamp) {
		logger.info(EWrapperMsgGenerator.updateAccountTime(timeStamp));
	}

	@Override
	public void accountDownloadEnd(String accountName) {
		logger.info(EWrapperMsgGenerator.accountDownloadEnd(accountName));
	}

	@Override
	public void nextValidId(int orderId) {
		logger.info(EWrapperMsgGenerator.nextValidId(orderId));
	}

	@Override
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		logger.info(EWrapperMsgGenerator.bondContractDetails(reqId, contractDetails));
	}

	@Override
	public void contractDetailsEnd(int reqId) {
		logger.info(EWrapperMsgGenerator.contractDetailsEnd(reqId));
	}

	@Override
	public void execDetails(int reqId, Contract contract, Execution execution) {
		logger.info(EWrapperMsgGenerator.execDetails(reqId, contract, execution));

	}

	@Override
	public void execDetailsEnd(int reqId) {
		logger.info(EWrapperMsgGenerator.execDetailsEnd(reqId));
	}

	@Override
	public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
		logger.info(EWrapperMsgGenerator.updateMktDepth(tickerId, position, operation, side, price, size));
	}

	@Override
	public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price,
			int size, boolean isSmartDepth) {
		logger.info(EWrapperMsgGenerator.updateMktDepthL2(tickerId, position, marketMaker, operation, side, price, size, isSmartDepth));

	}

	@Override
	public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
		logger.info(EWrapperMsgGenerator.updateNewsBulletin(msgId, msgType, message, origExchange));
	}

	@Override
	public void managedAccounts(String accountsList) {
		logger.info(EWrapperMsgGenerator.managedAccounts(accountsList));
	}

	@Override
	public void receiveFA(int faDataType, String xml) {
		logger.info(EWrapperMsgGenerator.receiveFA(faDataType, xml));
	}

	@Override
	public void scannerParameters(String xml) {
		logger.info(EWrapperMsgGenerator.scannerParameters(xml));
	}

	@Override
	public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark,
			String projection, String legsStr) {
		logger.info(EWrapperMsgGenerator.scannerData(reqId, rank, contractDetails, distance, benchmark, projection, legsStr));
	}

	@Override
	public void scannerDataEnd(int reqId) {
		logger.info(EWrapperMsgGenerator.scannerDataEnd(reqId));
	}

	@Override
	public void currentTime(long time) {
		logger.info(EWrapperMsgGenerator.currentTime(time));
	}

	@Override
	public void fundamentalData(int reqId, String data) {
		logger.info(EWrapperMsgGenerator.fundamentalData(reqId, data));
	}

	@Override
	public void deltaNeutralValidation(int reqId, DeltaNeutralContract deltaNeutralContract) {
		logger.info(EWrapperMsgGenerator.deltaNeutralValidation(reqId, deltaNeutralContract));
	}

	@Override
	public void tickSnapshotEnd(int reqId) {
		logger.info(EWrapperMsgGenerator.tickSnapshotEnd(reqId));
	}

	@Override
	public void marketDataType(int reqId, int marketDataType) {
		logger.info(EWrapperMsgGenerator.marketDataType(reqId, marketDataType));
	}

	@Override
	public void commissionReport(CommissionReport commissionReport) {
		logger.info(EWrapperMsgGenerator.commissionReport(commissionReport));
	}

	@Override
	public void position(String account, Contract contract, double pos, double avgCost) {
		logger.info(EWrapperMsgGenerator.position(account, contract, pos, avgCost));
	}

	@Override
	public void positionEnd() {
		logger.info(EWrapperMsgGenerator.positionEnd());
	}

	@Override
	public void accountSummary(int reqId, String account, String tag, String value, String currency) {
		logger.info(EWrapperMsgGenerator.accountSummary(reqId, account, tag, value, currency));
	}

	@Override
	public void accountSummaryEnd(int reqId) {
		logger.info(EWrapperMsgGenerator.accountSummaryEnd(reqId));
	}

	@Override
	public void verifyMessageAPI(String apiData) {
		logger.info("verifyMessageAPI => apiData "+apiData);
	}

	@Override
	public void verifyCompleted(boolean isSuccessful, String errorText) {
		logger.info("verifyCompleted => isSuccessful "+isSuccessful+", errorText : "+errorText);
	}

	@Override
	public void verifyAndAuthMessageAPI(String apiData, String xyzChallenge) {
		logger.info("verifyAndAuthMessageAPI => apiData "+apiData+", xyzChallenge : "+xyzChallenge);
	}

	@Override
	public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {
		logger.info("verifyAndAuthCompleted => isSuccessful "+isSuccessful+", errorText : "+errorText);
	}

	@Override
	public void displayGroupList(int reqId, String groups) {
		logger.info("displayGroupList => reqId "+reqId+", groups : "+groups);
	}
		

	@Override
	public void displayGroupUpdated(int reqId, String contractInfo) {
		logger.info("displayGroupUpdated => reqId "+reqId+", contractInfo : "+contractInfo);
	}

	@Override
	public void error(Exception e) {
		logger.info(EWrapperMsgGenerator.error(e));				
	}

	@Override
	public void error(String str) {
		logger.info(EWrapperMsgGenerator.error(str));
	}

	@Override
	public void error(int id, int errorCode, String errorMsg) {
		logger.info(EWrapperMsgGenerator.error(id, errorCode, errorMsg));
	}

	@Override
	public void connectionClosed() {
		logger.info(EWrapperMsgGenerator.connectionClosed());
	}

	@Override
	public void connectAck() {
		
	}

	@Override
	public void positionMulti(int reqId, String account, String modelCode, Contract contract, double pos,
			double avgCost) {
		logger.info(EWrapperMsgGenerator.positionMulti(reqId, account, modelCode, contract, pos, avgCost));	}

	@Override
	public void positionMultiEnd(int reqId) {
		logger.info(EWrapperMsgGenerator.positionEnd());
	}

	@Override
	public void accountUpdateMulti(int reqId, String account, String modelCode, String key, String value,
			String currency) {
		logger.info(EWrapperMsgGenerator.accountUpdateMulti(reqId, account, modelCode, key, value, currency));
	}

	@Override
	public void accountUpdateMultiEnd(int reqId) {
		logger.info(EWrapperMsgGenerator.accountUpdateMultiEnd(reqId));
	}

	@Override
	public void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId,
			String tradingClass, String multiplier, Set<String> expirations, Set<Double> strikes) {
		logger.info(EWrapperMsgGenerator.securityDefinitionOptionalParameter(reqId, exchange, underlyingConId, tradingClass, multiplier, expirations, strikes));
	}

	@Override
	public void securityDefinitionOptionalParameterEnd(int reqId) {
		logger.info(EWrapperMsgGenerator.securityDefinitionOptionalParameterEnd(reqId));
	}

	@Override
	public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {
		logger.info(EWrapperMsgGenerator.softDollarTiers(tiers));
	}

	@Override
	public void familyCodes(FamilyCode[] familyCodes) {
		logger.info(EWrapperMsgGenerator.familyCodes(familyCodes));
	}

	@Override
	public void symbolSamples(int reqId, ContractDescription[] contractDescriptions) {
		logger.info(EWrapperMsgGenerator.symbolSamples(reqId, contractDescriptions));
	}

	@Override
	public void historicalDataEnd(int reqId, String startDateStr, String endDateStr) {
		logger.info(EWrapperMsgGenerator.historicalDataEnd(reqId, startDateStr, endDateStr));
	}

	@Override
	public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {
		logger.info(EWrapperMsgGenerator.mktDepthExchanges(depthMktDataDescriptions));
	}

	@Override
	public void tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline,
			String extraData) {
		logger.info(EWrapperMsgGenerator.tickNews(tickerId, timeStamp, providerCode, articleId, headline, extraData));
	}

	@Override
	public void smartComponents(int reqId, Map<Integer, Entry<String, Character>> theMap) {
		logger.info(EWrapperMsgGenerator.smartComponents(reqId, theMap));
	}

	@Override
	public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {
		logger.info(EWrapperMsgGenerator.tickReqParams(tickerId, minTick, bboExchange, snapshotPermissions));
	}

	@Override
	public void newsProviders(NewsProvider[] newsProviders) {
		logger.info(EWrapperMsgGenerator.newsProviders(newsProviders));
	}

	@Override
	public void newsArticle(int requestId, int articleType, String articleText) {
		logger.info(EWrapperMsgGenerator.newsArticle(requestId, articleType, articleText));
	}

	@Override
	public void historicalNews(int requestId, String time, String providerCode, String articleId, String headline) {
		logger.info(EWrapperMsgGenerator.historicalNews(requestId, time, providerCode, articleId, headline));
	}

	@Override
	public void historicalNewsEnd(int requestId, boolean hasMore) {
		logger.info(EWrapperMsgGenerator.historicalNewsEnd(requestId, hasMore));
	}

	@Override
	public void headTimestamp(int reqId, String headTimestamp) {
		logger.info(EWrapperMsgGenerator.headTimestamp(reqId, headTimestamp));
	}

	@Override
	public void histogramData(int reqId, List<HistogramEntry> items) {
		logger.info(EWrapperMsgGenerator.histogramData(reqId, items));
	}

	@Override
	public void historicalDataUpdate(int reqId, Bar bar) {
		logger.info(EWrapperMsgGenerator.historicalData(reqId, bar.time(), bar.open(), bar.high(), bar.low(), bar.close(), bar.volume(), bar.count(), bar.wap()));
	}

	@Override
	public void rerouteMktDataReq(int reqId, int conId, String exchange) {
		logger.info(EWrapperMsgGenerator.rerouteMktDataReq(reqId, conId, exchange));
	}

	@Override
	public void rerouteMktDepthReq(int reqId, int conId, String exchange) {
		logger.info(EWrapperMsgGenerator.rerouteMktDepthReq(reqId, conId, exchange));
	}

	@Override
	public void marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {
		logger.info(EWrapperMsgGenerator.marketRule(marketRuleId, priceIncrements));
	}

	@Override
	public void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {
		
		logger.info(EWrapperMsgGenerator.pnl(reqId, dailyPnL, unrealizedPnL, realizedPnL));

	}

	@Override
	public void pnlSingle(int reqId, int pos, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {
		logger.info(EWrapperMsgGenerator.pnlSingle(reqId, pos, dailyPnL, unrealizedPnL, realizedPnL, value));
	}

	@Override
	public void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean done) {
		
		if(ticks == null) return;
		
		for(HistoricalTick tick : ticks)
		{
			logger.info(EWrapperMsgGenerator.historicalTick(reqId, tick.time(), tick.price(), tick.size())); 
		}
	}

	@Override
	public void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {
		
		if(ticks == null) return;
		
		for(HistoricalTickBidAsk tick : ticks)
		{
			logger.info(EWrapperMsgGenerator.historicalTickBidAsk(reqId, tick.time(), tick.tickAttribBidAsk(), tick.priceBid(), tick.priceAsk(), tick.sizeBid(), tick.sizeAsk()));
		}
	}

	@Override
	public void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done) {
		if(ticks == null) return;
		
		for(HistoricalTickLast tick : ticks)
		{
			logger.info(EWrapperMsgGenerator.historicalTickLast(reqId, tick.time(), tick.tickAttribLast(), tick.price(), tick.size(), tick.exchange(), tick.specialConditions()));
		}

	}

	@Override
	public void tickByTickAllLast(int reqId, int tickType, long time, double price, int size,
			TickAttribLast tickAttribLast, String exchange, String specialConditions) {
		
		logger.info(EWrapperMsgGenerator.tickByTickAllLast(reqId, tickType, time, price, size, tickAttribLast, exchange, specialConditions));

	}

	@Override
	public void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize, int askSize,
			TickAttribBidAsk tickAttribBidAsk) {
		logger.info(EWrapperMsgGenerator.tickByTickBidAsk(reqId, time, bidPrice, askPrice, bidSize, askSize, tickAttribBidAsk));
	}

	@Override
	public void tickByTickMidPoint(int reqId, long time, double midPoint) {
		logger.info(EWrapperMsgGenerator.tickByTickMidPoint(reqId, time, midPoint));
	}

	@Override
	public void orderBound(long orderId, int apiClientId, int apiOrderId) {
		logger.info(EWrapperMsgGenerator.orderBound(orderId, apiClientId, apiOrderId));
	}

	@Override
	public void completedOrder(Contract contract, Order order, OrderState orderState) {
		logger.info(EWrapperMsgGenerator.completedOrder(contract, order, orderState));
	}

	@Override
	public void completedOrdersEnd() {
		logger.info(EWrapperMsgGenerator.completedOrdersEnd());
	}

}
