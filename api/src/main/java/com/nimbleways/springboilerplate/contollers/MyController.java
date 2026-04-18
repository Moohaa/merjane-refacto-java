package com.nimbleways.springboilerplate.contollers;

import com.nimbleways.springboilerplate.config.exceptions.AppException;
import com.nimbleways.springboilerplate.config.logging.Log;
import com.nimbleways.springboilerplate.config.logging.LogFactory;
import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;

import com.nimbleways.springboilerplate.services.spec.OrderServiceI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class MyController {

    private final Log log= LogFactory.get(MyController.class);


    private final OrderServiceI orderService;

    public MyController(OrderServiceI orderService) {
        this.orderService = orderService;
    }

    @PostMapping("{orderId}/processOrder")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ProcessOrderResponse> processOrder(@PathVariable Long orderId) {
        log.info("Received order processing request [ id= " + orderId +"]");
        try {
            return new ResponseEntity<ProcessOrderResponse>(orderService.processOrder(orderId),HttpStatus.OK);
        }
        catch (AppException e) {
            return new ResponseEntity<ProcessOrderResponse>(e.getHttpCode());
        }
    }
}
