package au.com.innovativecoder.tradecustomerservicewebflux.service;

import au.com.innovativecoder.tradecustomerservicewebflux.dto.CustomerInformation;
import au.com.innovativecoder.tradecustomerservicewebflux.entity.Customer;
import au.com.innovativecoder.tradecustomerservicewebflux.exceptions.ApplicationExceptions;
import au.com.innovativecoder.tradecustomerservicewebflux.mapper.EntityDtoMapper;
import au.com.innovativecoder.tradecustomerservicewebflux.repository.CustomerRepository;
import au.com.innovativecoder.tradecustomerservicewebflux.repository.PortfolioItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PortfolioItemRepository portfolioItemRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, PortfolioItemRepository portfolioItemRepository) {
        this.customerRepository = customerRepository;
        this.portfolioItemRepository = portfolioItemRepository;
    }

    public Mono<CustomerInformation> getCustomerInformation(Integer customerId) {
        return this.customerRepository.findById(customerId)
                .switchIfEmpty(ApplicationExceptions.customerNotFound(customerId))
                .flatMap(this::buildCustomerInformation);
    }

    private Mono<CustomerInformation> buildCustomerInformation(Customer customer) {
        return this.portfolioItemRepository.findAllByCustomerId(customer.getId())
                .collectList()
                .map(list -> EntityDtoMapper.toCustomerInformation(customer, list));
    }
}
