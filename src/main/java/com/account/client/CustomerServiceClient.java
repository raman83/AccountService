package com.account.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.account.dto.CustomerResponse;

@FeignClient(name = "customer-service", url = "${customer.service.url}")


public interface CustomerServiceClient {
    @GetMapping("/api/v1/customers/external-id/{externalId}")
    ResponseEntity<CustomerResponse> getCustomerByExternalId(@PathVariable("externalId") String externalId);
    
    @GetMapping("/api/v1/customers/{externalId}/exists")
    public boolean exists(@PathVariable("externalId") String externalId);

}