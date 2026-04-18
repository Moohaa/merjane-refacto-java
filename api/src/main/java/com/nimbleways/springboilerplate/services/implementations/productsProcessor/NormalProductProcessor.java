package com.nimbleways.springboilerplate.services.implementations.productsProcessor;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import com.nimbleways.springboilerplate.services.implementations.ProductService;
import com.nimbleways.springboilerplate.services.spec.ProductProcessor;
import org.springframework.stereotype.Component;

@Component
public class NormalProductProcessor  implements ProductProcessor {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public NormalProductProcessor(ProductRepository productRepository, NotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    @Override
    public String supportedType() {
        return ProductService.NORMAL_TYPE;
    }

    @Override
    public void processProduct(Product product) {
        validateRequiredFields(product);

        if (product.getAvailable() > 0) {
            product.setAvailable(product.getAvailable() - 1);
            productRepository.save(product);
        } else {
            int leadTime = product.getLeadTime();
            if (leadTime > 0) {
                productRepository.save(product);
                notificationService.sendDelayNotification(leadTime, product.getName());
            }
        }

    }

    private static void validateRequiredFields(Product product) {
        if (product.getAvailable() == null) {
            throw new IllegalArgumentException("available is required");
        }
        if (product.getLeadTime() == null) {
            throw new IllegalArgumentException("leadTime is required");
        }
    }
}
