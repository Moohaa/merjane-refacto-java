package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.productsProcessor.SeasonalProductProcessor;
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

@ExtendWith(MockitoExtension.class)
public class SeasonalPrProcessorTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SeasonalProductProcessor seasonalProductProcessor;

    @Test
    public void shouldDecrementStockWhenInSeasonAndAvailable() {
        Product product = new Product(
                null, 2, 3, "SEASONAL", "Watermelon", null,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)
        );

        seasonalProductProcessor.processProduct(product);

        assertEquals(2, product.getAvailable());
        Mockito.verify(productRepository).save(product);
        Mockito.verify(notificationService, never()).sendDelayNotification(Mockito.anyInt(), Mockito.anyString());
        Mockito.verify(notificationService, never()).sendOutOfStockNotification(Mockito.anyString());
    }

    @Test
    public void shouldNotifyDelayWhenInSeasonOutOfStockAndLeadTimeWithinSeason() {
        Product product = new Product(
                null, 2, 0, "SEASONAL", "Grapes", null,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(5)
        );

        seasonalProductProcessor.processProduct(product);

        assertEquals(0, product.getAvailable());
        Mockito.verify(productRepository).save(product);
        Mockito.verify(notificationService).sendDelayNotification(2, "Grapes");
        Mockito.verify(notificationService, never()).sendOutOfStockNotification(Mockito.anyString());
    }

    @Test
    public void shouldNotifyUnavailableWhenInSeasonOutOfStockAndLeadTimeBeyondSeason() {
        Product product = new Product(
                null, 10, 0, "SEASONAL", "Mango", null,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(2)
        );

        seasonalProductProcessor.processProduct(product);

        assertEquals(0, product.getAvailable());
        Mockito.verify(productRepository).save(product);
        Mockito.verify(notificationService).sendOutOfStockNotification("Mango");
        Mockito.verify(notificationService, never()).sendDelayNotification(Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void shouldNotifyUnavailableWhenOutsideSeasonEvenIfStockExists() {
        Product product = new Product(
                null, 2, 5, "SEASONAL", "Peach", null,
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(10)
        );

        seasonalProductProcessor.processProduct(product);

        assertEquals(5, product.getAvailable());
        Mockito.verify(productRepository).save(product);
        Mockito.verify(notificationService).sendOutOfStockNotification("Peach");
        Mockito.verify(notificationService, never()).sendDelayNotification(Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void shouldTreatSeasonStartDateAsSellable() {
        Product product = new Product(
                null, 2, 1, "SEASONAL", "Apricot", null,
                LocalDate.now(), LocalDate.now().plusDays(5)
        );

        seasonalProductProcessor.processProduct(product);

        assertEquals(0, product.getAvailable());
        Mockito.verify(productRepository).save(product);
    }

    @Test
    public void shouldTreatSeasonEndDateAsSellable() {
        Product product = new Product(
                null, 2, 1, "SEASONAL", "Cherry", null,
                LocalDate.now().minusDays(5), LocalDate.now()
        );

        seasonalProductProcessor.processProduct(product);

        assertEquals(0, product.getAvailable());
        Mockito.verify(productRepository).save(product);
    }

    @Test
    public void shouldThrowDeterministicErrorWhenRequiredDatesAreMissing() {
        Product product = new Product(null, 2, 1, "SEASONAL", "Plum", null, null, LocalDate.now());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> seasonalProductProcessor.processProduct(product)
        );

        assertEquals("seasonStartDate is required", exception.getMessage());
    }
}
