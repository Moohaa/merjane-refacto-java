package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.assertEquals;

// import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Specify the controller class you want to test
// This indicates to spring boot to only load UsersController into the context
// Which allows a better performance and needs to do less mocks
@SpringBootTest
@AutoConfigureMockMvc
public class MyControllerIntegrationTests {
        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private NotificationService notificationService;

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private ProductRepository productRepository;

        @Test
        public void processOrderShouldReturn() throws Exception {
                List<Product> allProducts = createProducts();
                Set<Product> orderItems = new HashSet<Product>(allProducts);
                Order order = createOrder(orderItems);
                productRepository.saveAll(allProducts);
                order = orderRepository.save(order);
                mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                                .contentType("application/json"))
                                .andExpect(status().isOk());
                Order resultOrder = orderRepository.findById(order.getId()).get();
                assertEquals(resultOrder.getId(), order.getId());
        }

        @Test
        public void processOrderShouldApplyMixedProductRules() throws Exception {
                List<Product> products = new ArrayList<>();
                products.add(new Product(null, 3, 2, "NORMAL", "Normal In Stock", null, null, null));
                products.add(new Product(null, 4, 0, "NORMAL", "Normal Out Of Stock", null, null, null));
                products.add(new Product(
                                null, 2, 1, "SEASONAL", "Seasonal In Season", null,
                                LocalDate.now().minusDays(1), LocalDate.now().plusDays(2)
                ));
                products.add(new Product(
                                null, 2, 5, "SEASONAL", "Seasonal Outside Season", null,
                                LocalDate.now().plusDays(5), LocalDate.now().plusDays(20)
                ));
                products.add(new Product(null, 1, 1, "EXPIRABLE", "Expirable Valid", LocalDate.now().plusDays(1), null, null));
                products.add(new Product(null, 1, 5, "EXPIRABLE", "Expirable Expired", LocalDate.now().minusDays(1), null, null));

                productRepository.saveAll(products);
                Order order = orderRepository.save(createOrder(new HashSet<Product>(products)));

                mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                                .contentType("application/json"))
                                .andExpect(status().isOk());

                assertEquals(Integer.valueOf(1), productRepository.findFirstByName("Normal In Stock").orElseThrow().getAvailable());
                assertEquals(Integer.valueOf(0), productRepository.findFirstByName("Normal Out Of Stock").orElseThrow().getAvailable());
                assertEquals(Integer.valueOf(0), productRepository.findFirstByName("Seasonal In Season").orElseThrow().getAvailable());
                assertEquals(Integer.valueOf(5), productRepository.findFirstByName("Seasonal Outside Season").orElseThrow().getAvailable());
                assertEquals(Integer.valueOf(0), productRepository.findFirstByName("Expirable Valid").orElseThrow().getAvailable());
                assertEquals(Integer.valueOf(0), productRepository.findFirstByName("Expirable Expired").orElseThrow().getAvailable());
        }

        private static Order createOrder(Set<Product> products) {
                Order order = new Order();
                order.setItems(products);
                return order;
        }

        private static List<Product> createProducts() {
                List<Product> products = new ArrayList<>();
                products.add(new Product(null, 15, 30, "NORMAL", "USB Cable", null, null, null));
                products.add(new Product(null, 10, 0, "NORMAL", "USB Dongle", null, null, null));
                products.add(new Product(null, 15, 30, "EXPIRABLE", "Butter", LocalDate.now().plusDays(26), null,
                                null));
                products.add(new Product(null, 90, 6, "EXPIRABLE", "Milk", LocalDate.now().minusDays(2), null, null));
                products.add(new Product(null, 15, 30, "SEASONAL", "Watermelon", null, LocalDate.now().minusDays(2),
                                LocalDate.now().plusDays(58)));
                products.add(new Product(null, 15, 30, "SEASONAL", "Grapes", null, LocalDate.now().plusDays(180),
                                LocalDate.now().plusDays(240)));
                return products;
        }
}
