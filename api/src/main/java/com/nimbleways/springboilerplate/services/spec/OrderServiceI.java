package com.nimbleways.springboilerplate.services.spec;

import com.nimbleways.springboilerplate.config.exceptions.AppException;
import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import org.springframework.web.bind.annotation.PathVariable;

public interface OrderServiceI {
    public ProcessOrderResponse processOrder(Long orderId) throws AppException;
}
