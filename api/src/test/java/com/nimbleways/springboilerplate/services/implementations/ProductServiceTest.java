package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.spec.ProductProcessor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertSame;

public class ProductServiceTest {

    @Test
    public void shouldRouteSeasonalTypeToSeasonalProcessor() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        ProductProcessor normalProcessor = Mockito.mock(ProductProcessor.class);
        ProductProcessor seasonalProcessor = Mockito.mock(ProductProcessor.class);
        Mockito.when(normalProcessor.supportedType()).thenReturn("NORMAL");
        Mockito.when(seasonalProcessor.supportedType()).thenReturn("SEASONAL");

        ProductService productService = new ProductService(
                repository,
                notificationService,
                java.util.List.of(normalProcessor, seasonalProcessor)
        );

        assertSame(seasonalProcessor, productService.getProductProcessor("SEASONAL"));
    }

    @Test
    public void shouldRouteExpirableTypeToExpirableProcessor() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        ProductProcessor normalProcessor = Mockito.mock(ProductProcessor.class);
        ProductProcessor expirableProcessor = Mockito.mock(ProductProcessor.class);
        Mockito.when(normalProcessor.supportedType()).thenReturn("NORMAL");
        Mockito.when(expirableProcessor.supportedType()).thenReturn("EXPIRABLE");

        ProductService productService = new ProductService(
                repository,
                notificationService,
                java.util.List.of(normalProcessor, expirableProcessor)
        );

        assertSame(expirableProcessor, productService.getProductProcessor("EXPIRABLE"));
    }

    @Test
    public void shouldFallbackUnknownTypeToNormalProcessor() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        ProductProcessor normalProcessor = Mockito.mock(ProductProcessor.class);
        ProductProcessor seasonalProcessor = Mockito.mock(ProductProcessor.class);
        Mockito.when(normalProcessor.supportedType()).thenReturn("NORMAL");
        Mockito.when(seasonalProcessor.supportedType()).thenReturn("SEASONAL");

        ProductService productService = new ProductService(
                repository,
                notificationService,
                java.util.List.of(normalProcessor, seasonalProcessor)
        );

        assertSame(normalProcessor, productService.getProductProcessor("SOMETHING_ELSE"));
    }
}
