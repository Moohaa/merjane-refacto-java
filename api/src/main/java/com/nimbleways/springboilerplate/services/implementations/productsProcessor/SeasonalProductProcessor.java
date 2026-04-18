package com.nimbleways.springboilerplate.services.implementations.productsProcessor;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import com.nimbleways.springboilerplate.services.spec.ProductProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SeasonalProductProcessor implements ProductProcessor {

    private final ProductRepository productRepository;
    
    private final NotificationService notificationService;

    public SeasonalProductProcessor(ProductRepository productRepository, NotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    @Override
    public String supportedType() {
        return "SEASONAL";
    }

    @Override
    public void processProduct(Product product) {
        validateRequiredFields(product);

        LocalDate today = LocalDate.now();
        if (isInSeason(today, product.getSeasonStartDate(), product.getSeasonEndDate()) && product.getAvailable() > 0) {
            product.setAvailable(product.getAvailable() - 1);
            productRepository.save(product);
            return;
        }

        if (!isInSeason(today, product.getSeasonStartDate(), product.getSeasonEndDate())) {
            notificationService.sendOutOfStockNotification(product.getName());
            productRepository.save(product);
        }

        else if (isDelayAfterSeasonEnd(today, product.getLeadTime(), product.getSeasonEndDate())) {
            notificationService.sendOutOfStockNotification(product.getName());
            productRepository.save(product);
        }
        else {
            productRepository.save(product);
            notificationService.sendDelayNotification(product.getLeadTime(), product.getName());
        }
    }


    private static boolean isInSeason(LocalDate today, LocalDate seasonStartDate, LocalDate seasonEndDate) {
        return !today.isBefore(seasonStartDate) && !today.isAfter(seasonEndDate);
    }

    private static boolean isDelayAfterSeasonEnd(LocalDate today, int leadTime, LocalDate seasonEndDate) {
        return today.plusDays(leadTime).isAfter(seasonEndDate);
    }

    private static void validateRequiredFields(Product product) {
        if (product.getAvailable() == null) {
            throw new IllegalArgumentException("available is required");
        }
        if (product.getLeadTime() == null) {
            throw new IllegalArgumentException("leadTime is required");
        }
        if (product.getSeasonStartDate() == null) {
            throw new IllegalArgumentException("seasonStartDate is required");
        }
        if (product.getSeasonEndDate() == null) {
            throw new IllegalArgumentException("seasonEndDate is required");
        }
    }
}
