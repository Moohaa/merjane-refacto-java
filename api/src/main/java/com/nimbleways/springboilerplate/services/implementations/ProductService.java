package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.spec.ProductProcessor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductService {
    public static final String NORMAL_TYPE = "NORMAL";

    private final ProductRepository pr;

    private final NotificationService ns;

    private final Map<String, ProductProcessor> productProcessorByType;

    public ProductService(ProductRepository pr, NotificationService ns, List<ProductProcessor> productProcessors) {
        this.pr = pr;
        this.ns = ns;
        this.productProcessorByType = productProcessors.stream()
                .collect(Collectors.toMap(
                        processor -> normalizeType(processor.supportedType()),
                        Function.identity()
                ));
    }

    public void notifyDelay(int leadTime, Product p) {
        p.setLeadTime(leadTime);
        pr.save(p);
        ns.sendDelayNotification(leadTime, p.getName());
    }

    public void save(Product p) {
        pr.save(p);
    }

    void processProduct(Product product) {
        ProductProcessor orderProcessor = this.getProductProcessor(product.getType());
        orderProcessor.processProduct(product);
    }

    ProductProcessor getProductProcessor(String productType) {
        ProductProcessor normalProcessor = productProcessorByType.get(NORMAL_TYPE);
        return productProcessorByType.getOrDefault(normalizeType(productType), normalProcessor);
    }

    public boolean isProductAvailable(Product product) {
        return product.getAvailable() > 0;
    }

    private String normalizeType(String productType) {
        if (productType == null) {
            return NORMAL_TYPE;
        }
        return productType.trim().toUpperCase(Locale.ROOT);
    }
}
