package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.config.exceptions.AppException;
import com.nimbleways.springboilerplate.config.logging.Log;
import com.nimbleways.springboilerplate.config.logging.LogFactory;
import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProcessedOrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.spec.OrderServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class OrderService implements OrderServiceI {

    private final Log log= LogFactory.get(OrderService.class);
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProcessedOrderRepository processedOrderRepository;
    private final  ProductService productService;

    @Autowired
    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            ProcessedOrderRepository processedOrderRepository,
            ProductService productService
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.processedOrderRepository = processedOrderRepository;
        this.productService = productService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessOrderResponse processOrder(Long orderId) throws AppException {
        log.info("Processing order with id " + orderId);

        Order order = orderRepository.findById(orderId).orElseThrow(
                ()-> new AppException("Can't find the order.", HttpStatus.NOT_FOUND)
        );

        try {
            processedOrderRepository.insertProcessedOrder(orderId, LocalDateTime.now());
        } catch (DataIntegrityViolationException ex) {
            throw new AppException("Order was already processed.", HttpStatus.CONFLICT);
        }

        List<Long> productIds = order.getItems().stream()
                .map(Product::getId)
                .sorted()
                .collect(Collectors.toList());

        if (!productIds.isEmpty()) {
            Map<Long, Product> lockedProducts = productRepository.findAllByIdInOrderByIdForUpdate(productIds).stream()
                    .collect(Collectors.toMap(Product::getId, Function.identity()));

            for (Long productId : productIds) {
                Product lockedProduct = lockedProducts.get(productId);
                if (lockedProduct != null) {
                    productService.processProduct(lockedProduct);
                }
            }
        }

        return new ProcessOrderResponse(order.getId());
    }
}
