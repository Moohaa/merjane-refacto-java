package com.nimbleways.springboilerplate.services.implementations.productsProcessor;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import com.nimbleways.springboilerplate.services.spec.ProductProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ExpirableProductProcessor implements ProductProcessor {

    private final ProductRepository productRepository;

    private final NotificationService notificationService;


    public ExpirableProductProcessor(ProductRepository productRepository, NotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    @Override
    public String supportedType() {
        return "EXPIRABLE";
    }

    @Override
    public void processProduct(Product product) {
        validateRequiredFields(product);

        LocalDate today = LocalDate.now();
        boolean isExpired = isExpired(today, product.getExpiryDate());

        if (!isExpired) {
            processLikeNormal(product);
            return;
        }

        notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
        product.setAvailable(0);
        productRepository.save(product);
    }

    private void processLikeNormal(Product product) {
        if (product.getAvailable() > 0) {
            product.setAvailable(product.getAvailable() - 1);
            productRepository.save(product);
            return;
        }
        int leadTime = product.getLeadTime();
        if (leadTime > 0) {
            productRepository.save(product);
            notificationService.sendDelayNotification(leadTime, product.getName());
        }
    }

    private static boolean isExpired(LocalDate today, LocalDate expiryDate) {
        return today.isAfter(expiryDate);
    }

    private static void validateRequiredFields(Product product) {
        if (product.getAvailable() == null) {
            throw new IllegalArgumentException("available is required");
        }
        if (product.getLeadTime() == null) {
            throw new IllegalArgumentException("leadTime is required");
        }
        if (product.getExpiryDate() == null) {
            throw new IllegalArgumentException("expiryDate is required");
        }
    }
}
