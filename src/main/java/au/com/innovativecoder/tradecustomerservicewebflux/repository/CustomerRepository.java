package au.com.innovativecoder.tradecustomerservicewebflux.repository;

import au.com.innovativecoder.tradecustomerservicewebflux.entity.Customer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository  extends ReactiveCrudRepository<Customer, Integer> {
}
