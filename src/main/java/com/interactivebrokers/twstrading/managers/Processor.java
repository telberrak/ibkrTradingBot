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

	@Value("${spring.kafka.realtime.listener.price.tickerid}")
	private String tickerIdStr;

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
		m_client.eConnect(host, port, 2);
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

			//contractDetails(m_client, contracts);
			
			reqRealTimeBars(m_client,contracts);
			
			int[] offsets = { 0 };

			/*
			for (int offset : offsets) {

				historicalData5mins(m_client, contracts, offset);

				Thread.sleep(60000);

				logger.info("Cancelling 5min historical data ");
				cancelHistoricalData(m_client, contracts);
			}
			
			 * 
			 * 
			 * logger.info("Requesting 5min historical data ");
			 * 
			 * historicalData5min(m_client, contracts);
			 * 
			 * Thread.sleep(10000);
			 * 
			 * logger.info("Cancelling 5min historical data ");
			 * cancelHistoricalData(m_client, contracts);
			 * 
			 * 
			 * //int[] offsets = {-4,-5,-6,-7,-8,-9,-10,-11,-12,-13,-14,-15,-16,-17,-18,
			 * -19,-20,-21,-22,-23,-24,-25,-26,-27,-28,-29,-30,-31,-32,-33};
			 * 
			 * //int[] offsets = {0};
			 * 
			 * 
			 * 
			 * 
			 * 
			 * 
			 * 
			 * logger.info("Requesting realtime data "); reqRealTimeBars(m_client,
			 * contracts);
			 * 
			 * Thread.sleep(7*60*60*1000);
			 * 
			 * logger.info("Cancelling realtime data "); cancelRealTimeBars(m_client,
			 * contracts);
			 * 
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

		// cal.add(Calendar.DAY_OF_MONTH, -15);
		String formatted = form.format(cal.getTime());

		historicalDataRequests(client, formatted, "5 D", "1 min", contracts);
	}

	/**
	 * 
	 * @param client
	 * @throws InterruptedException
	 */
	private void historicalData5mins(EClientSocket client, List<Contract> contracts, int offset)
			throws InterruptedException {

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat form = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		cal.add(Calendar.DAY_OF_MONTH, offset);
		String formatted = form.format(cal.getTime());
		historicalDataRequests(client, formatted, "1 D", "5 mins", contracts);

	}

	private void historicalData1min(EClientSocket client, List<Contract> contracts, int offset)
			throws InterruptedException {

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat form = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		cal.add(Calendar.DAY_OF_MONTH, offset);
		String formatted = form.format(cal.getTime());
		historicalDataRequests(client, formatted, "1 D", "1 min", contracts);

	}
	
	private void historicalDailyData(EClientSocket client, List<Contract> contracts, int offset)
			throws InterruptedException {

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat form = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		cal.add(Calendar.DAY_OF_MONTH, offset);
		String formatted = form.format(cal.getTime());
		historicalDataRequests(client, formatted, "1 Y", "1 day", contracts);

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

		//String[] tickers = "CPB,K,SYY,MDLZ,GIS,KHC,CAG,MKC,HRL,SJM,EL,XRAY,MA,MSFT,V,DLTR,VMC,HSY,ISRG,ATVI,CRL,ETN,RMD,WM,AAPL,KDP,KR,KO,IRM,ABBV,DLR,TGT,XYL,FAST,CSCO,KMB,COST,LOW,AMD,GEN,TSN,SPG,MRNA,TAP,MRK,GOOG,LW,WYNN,PSX,T,JNJ,VTRS,UPS,TMUS,GOOGL,APTV,PEP,WMT,AVGO,CL,FIS,D,CE,GILD,JNPR,MCD,PFE,ADSK,ALGN,HAS,TRV,APA,ACN,IPG,BBWI,WBD,BLK,MNST,EXC,LIN,AMGN,EBAY,AMAT,ABT,BIIB,ED,DUK,WBA,RTX,MAR,AEP,NEE,NKE,CEG,DE,FTNT,SO,HON,TXN,GLW,APH,IBM,ORCL,NWL,ADM,LYV,AMZN,TER,PARA,CNP,DG,LLY,BA,TMO,INTC,BMY,GPN,ALB,CPRT,NEM,LVS,CMCSA,MO,VZ,BBY,BKR,DXCM,MMC,VICI,BRK.B,INTU,QCOM,TTWO,CVS,MGM,PG,FDX,AFL,ADBE,HD,PCAR,GS,KMI,ZTS,RCL,META,CARR,JPM,HPE,STX,BAC,PYPL,NDAQ,VTR,CFG,PXD,DVN,NOW,O,OXY,DAL,GM,WY,XOM,SWKS,ALK,MPC,USB,CSX,MS,WELL,LRCX,F,LEN,HPQ,MMM,WMB,OKE,BLDR,DFS,MCHP,SBUX,ADI,COF,BEN,COP,IP,AXP,UNH,XEL,HES,CVX,DIS,FCX,PCG,LUV,ULTA,AAL,ICE,C,STT,MDT,CTRA,SYF,NVDA,SCHW,AAP,CRM,PSA,VLO,WFC,FANG,JCI,MPWR,SLB,CZR,HBAN,EOG,MRO,UAL,HST,VNO,HAL,CCL,BF.B,ON,CF,WRK,MOS,NCLH,EXPE,FITB,EQT,CAT,GE,URI,EPAM,LNC,MTB,VFC,DISH,PNC,ENPH,MTCH,TFC,AES,WDC,MU,LKQ,ANET,ZION,KEY,CCI,CMA,RF,LUMN,INVH,TSLA,NFLX,ETSY,SIVB,RE,ACGL,ELV,TRGP,FDS,GMS,EQIX,FRC,ILMN,MKTX,HUM,CLX,MOH,TFX,ODFL,CHD,ORLY,MSI,BAX,MCK,CDNS,BSX,SNPS,BDX,IDXX,IT,VRTX,UNP,SEDG,WAT,INCY,TYL,CME,MTD,NSC,YUM,FE,CNC,EW,VRSK,LYB,DGX,ETR,CMG,EXR,AWK,A,DD,MSCI,ES,AZO,STZ,AKAM,OGN,NRG,SYK,CAH,AEE,APD,AJG,HCA,NTAP,RSG,REGN,VRSN,COO,WEC,TJX,PAYX,ROP,ABC,SBAC,CDW,ZBH,PPL,EIX,ANSS,ECL,GRMN,TECH,DPZ,CHRW,AMCR,AON,HOLX,KEYS,CDAY,SRE,BWA,LNT,LH,CTAS,IEX,EA,ROK,JKHY,DVA,HSIC,CB,BRO,FFIV,CI,ESS,DTE,GPC,TDY,ADP,WRB,SHW,MLM,AMT,WTW,L,AOS,SPGI,EMN,NDSN,PEG,AME,ZBRA,CSGP,AMP,EMR,ALL,PAYC,BR,TXT,ATO,MAA,CMS,FTV,ROL,ROST,NOC,MCO,CINF,BIO,PGR,STE,KLAC,WAB,DHR,NI,IQV,GL,GD,EXPD,WST,EQR,UDR,LDOS,DOV,AVB,LMT,AVY,TDG,BALL,HIG,PPG,PLD,UHS,QRVO,HII,PRU,RJF,HWM,CPT,FOXA,LHX,OTIS,JBHT,FOX,PTC,DRI,SNA,PWR,IR,KIM,TT,SEE,BKNG,MET,HLT,CTSH,AIZ,ITW,GWW,NWS,IFF,ALLE,TROW,NTRS,NWSA,PM,J,PNR,NUE,TPR,REG,PKG,OMC,RL,CBRE,BK,CMI,DOW,PNW,TRMB,MHK,NVR,DXC,FLT,PFG,ARE,TEL,CTVA,FMC,IVZ,EFX,NXPI,CTLT,RHI,POOL,PH,WHR,FRT,PEAK,AIG,SWK,DHI,PHM,GNRC,MAS,TSCO,BXP,KMX,SBNY,PKI,FISV,CHTR".split(",");

		/*
		String[] tickers = "SPY,QQQ,IWM" .split(",");

		for (String ticker : tickers) {
			Contract contract = new Contract();

			contract.symbol(ticker);
			contract.secType("STK");
			contract.currency("USD");
			contract.exchange("SMART");
			contract.primaryExch("");

			ibcontracts.add(contract);
		}

		return ibcontracts;
	*/
		
		return getOptionContracts("SPY");
	}
	
	/**
	 * 
	 * @param ticker
	 * @return
	 * @throws InterruptedException
	 */
	private List<Contract> getOptionContracts(String ticker) throws InterruptedException {

		List<Contract> ibcontracts = new ArrayList<Contract>();

		//String[] tickers = "CPB,K,SYY,MDLZ,GIS,KHC,CAG,MKC,HRL,SJM,EL,XRAY,MA,MSFT,V,DLTR,VMC,HSY,ISRG,ATVI,CRL,ETN,RMD,WM,AAPL,KDP,KR,KO,IRM,ABBV,DLR,TGT,XYL,FAST,CSCO,KMB,COST,LOW,AMD,GEN,TSN,SPG,MRNA,TAP,MRK,GOOG,LW,WYNN,PSX,T,JNJ,VTRS,UPS,TMUS,GOOGL,APTV,PEP,WMT,AVGO,CL,FIS,D,CE,GILD,JNPR,MCD,PFE,ADSK,ALGN,HAS,TRV,APA,ACN,IPG,BBWI,WBD,BLK,MNST,EXC,LIN,AMGN,EBAY,AMAT,ABT,BIIB,ED,DUK,WBA,RTX,MAR,AEP,NEE,NKE,CEG,DE,FTNT,SO,HON,TXN,GLW,APH,IBM,ORCL,NWL,ADM,LYV,AMZN,TER,PARA,CNP,DG,LLY,BA,TMO,INTC,BMY,GPN,ALB,CPRT,NEM,LVS,CMCSA,MO,VZ,BBY,BKR,DXCM,MMC,VICI,BRK.B,INTU,QCOM,TTWO,CVS,MGM,PG,FDX,AFL,ADBE,HD,PCAR,GS,KMI,ZTS,RCL,META,CARR,JPM,HPE,STX,BAC,PYPL,NDAQ,VTR,CFG,PXD,DVN,NOW,O,OXY,DAL,GM,WY,XOM,SWKS,ALK,MPC,USB,CSX,MS,WELL,LRCX,F,LEN,HPQ,MMM,WMB,OKE,BLDR,DFS,MCHP,SBUX,ADI,COF,BEN,COP,IP,AXP,UNH,XEL,HES,CVX,DIS,FCX,PCG,LUV,ULTA,AAL,ICE,C,STT,MDT,CTRA,SYF,NVDA,SCHW,AAP,CRM,PSA,VLO,WFC,FANG,JCI,MPWR,SLB,CZR,HBAN,EOG,MRO,UAL,HST,VNO,HAL,CCL,BF.B,ON,CF,WRK,MOS,NCLH,EXPE,FITB,EQT,CAT,GE,URI,EPAM,LNC,MTB,VFC,DISH,PNC,ENPH,MTCH,TFC,AES,WDC,MU,LKQ,ANET,ZION,KEY,CCI,CMA,RF,LUMN,INVH,TSLA,NFLX,ETSY,SIVB,RE,ACGL,ELV,TRGP,FDS,GMS,EQIX,FRC,ILMN,MKTX,HUM,CLX,MOH,TFX,ODFL,CHD,ORLY,MSI,BAX,MCK,CDNS,BSX,SNPS,BDX,IDXX,IT,VRTX,UNP,SEDG,WAT,INCY,TYL,CME,MTD,NSC,YUM,FE,CNC,EW,VRSK,LYB,DGX,ETR,CMG,EXR,AWK,A,DD,MSCI,ES,AZO,STZ,AKAM,OGN,NRG,SYK,CAH,AEE,APD,AJG,HCA,NTAP,RSG,REGN,VRSN,COO,WEC,TJX,PAYX,ROP,ABC,SBAC,CDW,ZBH,PPL,EIX,ANSS,ECL,GRMN,TECH,DPZ,CHRW,AMCR,AON,HOLX,KEYS,CDAY,SRE,BWA,LNT,LH,CTAS,IEX,EA,ROK,JKHY,DVA,HSIC,CB,BRO,FFIV,CI,ESS,DTE,GPC,TDY,ADP,WRB,SHW,MLM,AMT,WTW,L,AOS,SPGI,EMN,NDSN,PEG,AME,ZBRA,CSGP,AMP,EMR,ALL,PAYC,BR,TXT,ATO,MAA,CMS,FTV,ROL,ROST,NOC,MCO,CINF,BIO,PGR,STE,KLAC,WAB,DHR,NI,IQV,GL,GD,EXPD,WST,EQR,UDR,LDOS,DOV,AVB,LMT,AVY,TDG,BALL,HIG,PPG,PLD,UHS,QRVO,HII,PRU,RJF,HWM,CPT,FOXA,LHX,OTIS,JBHT,FOX,PTC,DRI,SNA,PWR,IR,KIM,TT,SEE,BKNG,MET,HLT,CTSH,AIZ,ITW,GWW,NWS,IFF,ALLE,TROW,NTRS,NWSA,PM,J,PNR,NUE,TPR,REG,PKG,OMC,RL,CBRE,BK,CMI,DOW,PNW,TRMB,MHK,NVR,DXC,FLT,PFG,ARE,TEL,CTVA,FMC,IVZ,EFX,NXPI,CTLT,RHI,POOL,PH,WHR,FRT,PEAK,AIG,SWK,DHI,PHM,GNRC,MAS,TSCO,BXP,KMX,SBNY,PKI,FISV,CHTR".split(",");
		int[] strikes = {425,426,427,428,429,430};
		
		String[] rights = {"C","P"};
		
		for (int strike : strikes) {
			
			Contract contract;

			for (String optRight : rights) {
				contract = new Contract();
				contract.symbol(ticker);
				contract.secType("OPT");
				contract.currency("USD");
				contract.exchange("SMART");
				contract.secType("OPT");
				contract.lastTradeDateOrContractMonth("20230609");
				contract.right(optRight);
				contract.strike(strike);
				contract.multiplier("100");
				
				ibcontracts.add(contract);
			}
			
		}

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

		List<com.interactivebrokers.twstrading.domain.Contract> contracts = new ArrayList<com.interactivebrokers.twstrading.domain.Contract>();

		if (tickerIdStr != null && !tickerIdStr.trim().equalsIgnoreCase("")) {
			com.interactivebrokers.twstrading.domain.Contract contract = contractManager
					.getContractToTrade(Long.valueOf(tickerIdStr));

			if (contract != null) {
				contracts.add(contract);
			}
		}

		if (contracts.isEmpty()) {
			contracts = contractManager.getActiveContracts();
		}

		if (contracts == null || contracts.isEmpty()) {
			return getContracts();
		}

		List<Contract> ibcontracts = new ArrayList<Contract>(contracts.size());

		for (com.interactivebrokers.twstrading.domain.Contract contract : contracts) {

			if (!contract.isHistoReceived()) {
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
		}

		return ibcontracts;
	}

}
