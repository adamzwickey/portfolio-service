package io.tetrate.portfolio.controller;

import io.tetrate.portfolio.domain.Order;
import io.tetrate.portfolio.domain.Portfolio;
import io.tetrate.portfolio.service.PortfolioService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides the REST API for the portfolio service.
 * 
 * Provides the following endpoints:
 * <p><ul>
 * <li>GET <code>/portfolio/{id}</code> retrieves the portfolio with given user id.
 * <li>POST <code>/portfolio{id}</code> adds an order to the portfolio with the given user id.
 * </ul><p>
 * 
 * @author David Ferreira Pinto
 *
 */
@RestController
public class PortfolioController {
	private static final Logger LOG = LoggerFactory.getLogger(PortfolioController.class);

	/**
	 * the service to delegate to.
	 */
	@Autowired
	private PortfolioService service;

	/**
	 * Retrieves the portfolio for the given account.
	 * @return The portfolio with HTTP OK.
	 */
	@PreAuthorize("hasAuthority('ROLE_PORTFOLIO')")
	@GetMapping(value = "/portfolio")
	public ResponseEntity<Portfolio> getPortfolio() {
		LOG.debug("PortfolioController: Retrieving portfolio with user id:" );
		Portfolio folio = service.getPortfolio();
		LOG.debug("PortfolioController: Retrieved portfolio: {}", folio);
		return new ResponseEntity<Portfolio>(folio, getNoCacheHeaders(), HttpStatus.OK);
	}
	
	private HttpHeaders getNoCacheHeaders() {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Cache-Control", "no-cache");
		return responseHeaders;
	}
	/**
	 * Adds an order to the portfolio of the given user.
	 *
	 * @param order The order to add.
	 * @return The order with HTTP CREATED or BAD REQUEST if it couldn't save.
	 */
	@PreAuthorize("hasAuthority('ROLE_TRADE')")
	@PostMapping(value = "/portfolio")
	public ResponseEntity<Order> addOrder(@RequestBody final Order order,
										  @AuthenticationPrincipal Jwt token) {
		LOG.debug("Adding Order: {}", order);
		LOG.debug("User Token {}", token.getTokenValue());
		LOG.debug("User Token id: {}", token.getId());
		LOG.debug("User Token Subject: {}", token.getSubject());
		LOG.debug("User Token Claims: {}", token.getClaims());
		LOG.debug("User Token Headers: {}", token.getHeaders());
		LOG.debug("User Token Eamil: {}", token.getClaimAsString("email"));
		
		order.setUserId(token.getClaimAsString("email"));
		Order savedOrder = service.addOrder(order, token.getTokenValue());

		LOG.debug("Order added: {}", savedOrder);
		if (savedOrder != null && savedOrder.getOrderId() != null) {
			return new ResponseEntity<Order>(savedOrder, getNoCacheHeaders(), HttpStatus.CREATED);
		} else {
			LOG.warn("Order not saved: {}", order);
			return new ResponseEntity<Order>(savedOrder, getNoCacheHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
