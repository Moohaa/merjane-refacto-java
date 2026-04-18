package com.nimbleways.springboilerplate.services.spec;

import com.nimbleways.springboilerplate.entities.Product;

public interface ProductProcessor {

    String supportedType();

    void processProduct(Product product);
}
