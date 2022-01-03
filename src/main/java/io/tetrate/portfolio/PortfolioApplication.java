package io.tetrate.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

/**
 * SpringBoot application for the portfolio microservice.
 * 
 * Responsible for managing the portfolio as well as providing the API.
 * 
 * @author Adam Zwickey
 *
 */
@SpringBootApplication
@EnableDiscoveryClient
public class PortfolioApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(PortfolioApplication.class, args);
	}

	static {
		HostnameVerifier allHostsValid = (name, sslSession) -> true;
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}
}
