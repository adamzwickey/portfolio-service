package io.tetrate.portfolio.service;

import io.tetrate.portfolio.domain.*;
import io.tetrate.portfolio.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Manages a portfolio of holdings of stock/shares.
 * 
 * @author Adam Zwickey
 *
 */
@Service
public class PortfolioService {
	private static final Logger LOG = LoggerFactory.getLogger(PortfolioService.class);

	/**
	 * The service than handles the calls to get quotes.
	 */
	@Autowired
	QuoteRemoteCallService quoteService;

	@Autowired
	private OrderRepository orderRepository;

	@Value("${tetrate.accountServiceUrl}")
    private String accountServiceUrl;

    @Bean
    RestTemplate restTemplate(){
      return new RestTemplate();
    }

	/**
	 * Retrieves the portfolio for the given accountId.
	 *
	 *            The account id to retrieve for.
	 * @return The portfolio.
	 */
	public Portfolio getPortfolio() {
		/*
		 * Retrieve all orders for accounts id and build portfolio. - for each
		 * order create holding. - for each holding find current price.
		 */
		LOG.debug("Getting portfolio for accountId: " );
		List<Order> orders = orderRepository.getOrders();
		Portfolio folio = new Portfolio();
		return createPortfolio(folio, orders);
	}

	/**
	 * Builds a portfolio object with the list of orders.
	 * 
	 * @param portfolio
	 *            the portfolio object to build.
	 * @param orders
	 *            the list of orders.
	 * @return the portfolio object
	 */
	private Portfolio createPortfolio(Portfolio portfolio, List<Order> orders) {
		Set<String> symbols = new HashSet<>();
		Holding holding = null;
		for (Order order : orders) {
			holding = portfolio.getHolding(order.getSymbol());
			if (holding == null) {
				holding = new Holding();
				holding.setSymbol(order.getSymbol());
				holding.setCurrency(order.getCurrency());
				portfolio.addHolding(holding);
				symbols.add(order.getSymbol());
			}
			holding.addOrder(order);
		}
		List<Quote> quotes = new ArrayList<>();
		
		if (symbols.size() > 0) {
			quotes = quoteService.getMultipleQuotes(symbols);
		}

		for (Quote quote : quotes) {
			portfolio.getHolding(quote.getSymbol()).setCurrentValue(new BigDecimal(quote.getLastPrice()));
		}
		portfolio.refreshTotalValue();
		LOG.debug("Portfolio: " + portfolio);
		return portfolio;
	}

	/**
	 * Add an order to the repository and modify account balance.
	 * 
	 * @param order the order to add.
	 * @return the saved order.
	 */
	@Transactional
	public Order addOrder(Order order, String bearerToken) {
		LOG.debug("Adding order: " + order);
		if (order.getOrderFee() == null) {
			order.setOrderFee(Order.DEFAULT_ORDER_FEE);
			LOG.debug("Adding Fee to order: " + order);
		}
		Transaction transaction = new Transaction();
		
		if (order.getOrderType().equals(OrderType.BUY)) {
			double amount = order.getQuantity()
					* order.getPrice().doubleValue()
					+ order.getOrderFee().doubleValue();
			
			transaction.setAccountId(order.getAccountId());
			transaction.setAmount(BigDecimal.valueOf(amount));
			transaction.setCurrency(order.getCurrency());
			transaction.setDate(order.getCompletionDate());
			transaction.setDescription(order.toString());
			transaction.setType(TransactionType.DEBIT);
			
		} else if (order.getOrderType().equals(OrderType.SELL)){
			double amount = order.getQuantity()
					* order.getPrice().doubleValue()
					- order.getOrderFee().doubleValue();
			
			transaction.setAccountId(order.getAccountId());
			transaction.setAmount(BigDecimal.valueOf(amount));
			transaction.setCurrency(order.getCurrency());
			transaction.setDate(order.getCompletionDate());
			transaction.setDescription(order.toString());
			transaction.setType(TransactionType.CREDIT);
			
		}

		HttpHeaders headers = new HttpHeaders();
		HttpEntity<Transaction> entity = new HttpEntity<Transaction>(transaction, headers);
		headers.add("Authorization", "Bearer " + bearerToken);
		ResponseEntity<String> resp = restTemplate().exchange(accountServiceUrl + "/accounts/transaction", 
							  HttpMethod.POST, 
							  entity, 
							  String.class);
		LOG.debug("accounts transaction response: {}", resp);    
		if (resp.getStatusCode() == HttpStatus.OK) {
			LOG.info(String.format("Account funds updated successfully for account: %s and new funds are: %s",
							order.getAccountId(), resp.getBody()));
			return orderRepository.save(order);
			
		} else {
			// TODO: throw exception - not enough funds!
			// SK - Whats the expected behaviour?
			LOG.warn("PortfolioService:addOrder - decresing balance HTTP not ok: ");
			return null;
		}

	}
}
