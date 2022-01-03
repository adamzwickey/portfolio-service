package io.tetrate.portfolio.repository;


import java.util.List;

import io.tetrate.portfolio.domain.Order;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * 
 * @author Adam Zwickey
 *
 */
public interface OrderRepository extends CrudRepository<Order,Integer> {

	@Query("from Order where userid = ?#{principal.claims['email']} order by completionDate asc")
	//@Query("from Order order by completionDate asc")
	List<Order> getOrders();

}
