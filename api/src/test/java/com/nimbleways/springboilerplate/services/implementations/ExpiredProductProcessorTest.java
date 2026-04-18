package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.productsProcessor.ExpirableProductProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExpiredProductProcessorTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ExpirableProductProcessor expirableProductProcessor;

    @Test
    public void shouldDecrementStockWhenInStockAndNotExpired() {
        Product product = new Product(null, 10, 2, "EXPIRABLE", "Butter", LocalDate.now().plusDays(1), null, null);

        expirableProductProcessor.processProduct(product);

        assertEquals(1, product.getAvailable());
        Mockito.verify(productRepository).save(product);
        Mockito.verify(notificationService, never()).sendDelayNotification(Mockito.anyInt(), Mockito.anyString());
        Mockito.verify(notificationService, never()).sendExpirationNotification(Mockito.anyString(), Mockito.any());
    }

    @Test
    public void shouldSendDelayNotificationWhenOutOfStockNotExpiredAndLeadTimePositive() {
        Product product = new Product(null, 4, 0, "EXPIRABLE", "Cheese", LocalDate.now().plusDays(1), null, null);

        expirableProductProcessor.processProduct(product);

        assertEquals(0, product.getAvailable());
        Mockito.verify(productRepository).save(product);
        Mockito.verify(notificationService).sendDelayNotification(4, "Cheese");
        Mockito.verify(notificationService, never()).sendExpirationNotification(Mockito.anyString(), Mockito.any());

    }

    @Test
    public void shouldNotNotifyWhenOutOfStockNotExpiredAndLeadTimeNotPositive() {
        Product product = new Product(null, 0, 0, "EXPIRABLE", "Yogurt", LocalDate.now().plusDays(1), null, null);

        expirableProductProcessor.processProduct(product);

        assertEquals(0, product.getAvailable());
        Mockito.verify(productRepository, never()).save(product);
        Mockito.verify(notificationService, never()).sendDelayNotification(Mockito.anyInt(), Mockito.anyString());
        Mockito.verify(notificationService, never()).sendExpirationNotification(Mockito.anyString(), Mockito.any());
    }

    @Test
    public void shouldSetStockToZeroAndNotifyWhenExpired() {
        Product product = new Product(null, 5, 7, "EXPIRABLE", "Milk", LocalDate.now().minusDays(1), null, null);

        expirableProductProcessor.processProduct(product);

        assertEquals(0, product.getAvailable());
        Mockito.verify(productRepository).save(product);
        Mockito.verify(notificationService).sendExpirationNotification("Milk", product.getExpiryDate());
        Mockito.verify(notificationService, never()).sendDelayNotification(Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void shouldTreatExpiryDateAsStillSellable() {
        Product product = new Product(null, 5, 2, "EXPIRABLE", "Juice", LocalDate.now(), null, null);

        expirableProductProcessor.processProduct(product);

        assertEquals(1, product.getAvailable());
        Mockito.verify(productRepository).save(product);
        Mockito.verify(notificationService, never()).sendExpirationNotification(Mockito.anyString(), Mockito.any());
    }

    @Test
    public void shouldThrowDeterministicErrorWhenExpiryDateIsMissing() {
        Product product = new Product(null, 5, 2, "EXPIRABLE", "Juice", null, null, null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> expirableProductProcessor.processProduct(product)
        );

        assertEquals("expiryDate is required", exception.getMessage());
    }
}
