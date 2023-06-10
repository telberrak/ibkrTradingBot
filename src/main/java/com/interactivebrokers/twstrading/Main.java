package com.interactivebrokers.twstrading;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScans;

import com.interactivebrokers.twstrading.managers.Processor;
import com.interactivebrokers.twstrading.managers.StrategySimulator;

@SpringBootApplication
public class Main {

	
	@Autowired private Processor processor;
	
	@Autowired private StrategySimulator strategySimulator;
	
	@Value("${application.simulation}")
	private String simulation;

	
	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

		 if(!Boolean.parseBoolean(simulation))
		 {
			 processor.start();
		 }else		
		 {
			 strategySimulator.startSimulation();
		 }
		 
		};
	}
	
	
	private static final String DEFAULT_DDE_SERVICE_NAME = "Stwsserver";
    private static final String DEFAULT_TWS_HOST = "127.0.0.1";
    private static final int DEFAULT_TWS_PORT = 7496;
    private static final int DEFAULT_TWS_CLIENT_ID = 0;

    private static String m_ddeServiceName = DEFAULT_DDE_SERVICE_NAME;
    private static String m_host = DEFAULT_TWS_HOST;
    private static int m_port = DEFAULT_TWS_PORT;
    private static int m_clientId = DEFAULT_TWS_CLIENT_ID;

    /** Method parses command line arguments */
    private static boolean parseCommandLineArguments(String[] args) {
        boolean ret = true;
        for (int i = 0; i < args.length; i++) {
            
            if (args[i].length() > 2 && args[i].charAt(0) == '-') {
                switch(args[i].charAt(1)) {
                    case 'h':
                        m_host = args[i].substring(2);
                        break;
                    case 'p':
                        m_port = Integer.parseInt(args[i].substring(2));
                        break;
                    case 'c':
                        m_clientId = Integer.parseInt(args[i].substring(2));
                        break;
                    case 's':
                        m_ddeServiceName = "S" + args[i].substring(2);
                        break;
                    default:
                        ret = false;
                }
            } else {
                ret = false;
            }
        }
        if (!ret) {
            System.out.println("Invalid command line arguments");
            showArgs();
        }
        
        return ret;
    }    

    /** Method shows command line arguments */
    private static void showArgs() {
        System.out.println("Valid command line arguments: -h<host> -p<port> -c<clientId> -s<servicename>, e.g. -h127.0.0.1 -p7496 -c0 -stwsserver");
    }
}
