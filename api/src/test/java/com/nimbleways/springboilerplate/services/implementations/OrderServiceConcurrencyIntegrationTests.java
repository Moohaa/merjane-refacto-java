package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.config.exceptions.AppException;
import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProcessedOrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class OrderServiceConcurrencyIntegrationTests {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProcessedOrderRepository processedOrderRepository;

    @MockBean
    private NotificationService notificationService;

    @BeforeEach
    void cleanState() {
        processedOrderRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    public void shouldReturnConflictWhenSameOrderProcessedTwiceSequentially() throws Exception {
        Product product = productRepository.save(new Product(null, 0, 1, "NORMAL", "Cable", null, null, null));
        Order order = orderRepository.save(createOrder(Set.of(product)));

        ProcessOrderResponse first = orderService.processOrder(order.getId());
        AppException second = assertThrows(AppException.class, () -> orderService.processOrder(order.getId()));

        assertEquals(order.getId(), first.id());
        assertEquals(HttpStatus.CONFLICT, second.getHttpCode());
    }

    @Test
    public void shouldAllowOnlyOneSuccessWhenSameOrderProcessedConcurrently() throws Exception {
        Product product = productRepository.save(new Product(null, 0, 1, "NORMAL", "Mouse", null, null, null));
        Order order = orderRepository.save(createOrder(Set.of(product)));

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            Callable<Object> task = () -> {
                start.await();
                try {
                    return orderService.processOrder(order.getId());
                } catch (Exception e) {
                    return e;
                }
            };

            Future<Object> first = executorService.submit(task);
            Future<Object> second = executorService.submit(task);
            start.countDown();

            Object result1 = first.get();
            Object result2 = second.get();

            int successCount = 0;
            int conflictCount = 0;
            for (Object result : List.of(result1, result2)) {
                if (result instanceof ProcessOrderResponse) {
                    successCount++;
                } else if (result instanceof AppException) {
                    AppException appException = (AppException) result;
                    if (appException.getHttpCode() == HttpStatus.CONFLICT) {
                        conflictCount++;
                    }
                }
            }

            assertEquals(1, successCount);
            assertEquals(1, conflictCount);

            Product persisted = productRepository.findById(product.getId()).orElseThrow();
            assertEquals(0, persisted.getAvailable());
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    public void shouldRollbackProductUpdatesAndIdempotencyOnMidProcessingFailure() {
        Product normal = productRepository.save(new Product(null, 0, 2, "NORMAL", "Keyboard", null, null, null));
        Product invalidExpirable = productRepository.save(
                new Product(null, 0, 1, "EXPIRABLE", "Milk", null, null, null)
        );
        Order order = orderRepository.save(createOrder(new HashSet<>(List.of(normal, invalidExpirable))));

        RuntimeException firstFailure = assertThrows(RuntimeException.class, () -> orderService.processOrder(order.getId()));
        Product persistedNormal = productRepository.findById(normal.getId()).orElseThrow();

        assertInstanceOf(IllegalArgumentException.class, firstFailure);
        assertEquals(2, persistedNormal.getAvailable());
        assertFalse(processedOrderRepository.existsById(order.getId()));

        RuntimeException secondFailure = assertThrows(RuntimeException.class, () -> orderService.processOrder(order.getId()));
        assertInstanceOf(IllegalArgumentException.class, secondFailure);
        assertFalse(processedOrderRepository.existsById(order.getId()));
    }

    @Test
    public void shouldKeepSharedProductStockConsistentAcrossConcurrentDifferentOrders() throws Exception {
        Product shared = productRepository.save(new Product(null, 0, 1, "NORMAL", "Webcam", null, null, null));
        Order firstOrder = orderRepository.save(createOrder(Set.of(shared)));
        Order secondOrder = orderRepository.save(createOrder(Set.of(shared)));

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            Callable<Exception> firstTask = () -> {
                start.await();
                try {
                    orderService.processOrder(firstOrder.getId());
                    return null;
                } catch (Exception e) {
                    return e;
                }
            };
            Callable<Exception> secondTask = () -> {
                start.await();
                try {
                    orderService.processOrder(secondOrder.getId());
                    return null;
                } catch (Exception e) {
                    return e;
                }
            };

            Future<Exception> first = executorService.submit(firstTask);
            Future<Exception> second = executorService.submit(secondTask);
            start.countDown();

            Exception firstError = first.get();
            Exception secondError = second.get();

            assertTrue(firstError == null, "first order should succeed");
            assertTrue(secondError == null, "second order should succeed");

            Product persisted = productRepository.findById(shared.getId()).orElseThrow();
            assertEquals(0, persisted.getAvailable());
            assertEquals(2L, processedOrderRepository.count());
        } finally {
            executorService.shutdownNow();
        }
    }

    private static Order createOrder(Set<Product> products) {
        Order order = new Order();
        order.setItems(products);
        return order;
    }
}
