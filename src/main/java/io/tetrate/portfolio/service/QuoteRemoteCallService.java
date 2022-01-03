package io.tetrate.portfolio.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import io.tetrate.portfolio.domain.Quote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Retrieves quotes from the quote service. Uses hystrix to manage failure.
 * 
 * @author Adam Zwickey
 *
 */
@Service
public class QuoteRemoteCallService {

	private static final Logger LOG = LoggerFactory.getLogger(QuoteRemoteCallService.class);

	@Value("${tetrate.quoteServiceUrl}")
	private String quotesServiceUrl;

    @Bean
    RestTemplate restTemplate(){
      return new RestTemplate();
    }

	/**
	 * Retrieve up to date quotes.
	 * 
	 * @param symbol the symbol of the quote to fetch.
	 * @return The quote
	 */
	public Quote getQuote(String symbol) {
		LOG.info("retrieving quotes: {}", symbol);
		Quote quote = restTemplate().getForObject(quotesServiceUrl + "/v1/quotes?q={symbol}", Quote.class, symbol);
		LOG.info("Received quotes: {}",quote);
		return quote;
	}

	/**
	 * Retrieve multiple quotes.
	 * 
	 * @param symbols comma separated list of symbols.
	 * @return
	 */
	public List<Quote> getMultipleQuotes(String symbols) {
		LOG.info("retrieving multiple quotes: {}", symbols);
		Quote[] quotesArr = restTemplate().getForObject(quotesServiceUrl + "/v1/quotes?q={symbols}", Quote[].class, symbols);
		List<Quote> quotes = Arrays.asList(quotesArr);
		LOG.info("Received quotes: {}",quotes);
		return quotes;
	}
	/**
	 * Retrieve multiple quotes.
	 * 
	 * @param symbols
	 * @return
	 */
	public List<Quote> getMultipleQuotes(Collection<String> symbols) {
		LOG.info("Fetching multiple quotes array: {} ",symbols);
		StringBuilder builder = new StringBuilder();
		for (Iterator<String> i = symbols.iterator(); i.hasNext();) {
			builder.append(i.next());
			if (i.hasNext()) {
				builder.append(",");
			}
		}
		return getMultipleQuotes(builder.toString());
	}
}
