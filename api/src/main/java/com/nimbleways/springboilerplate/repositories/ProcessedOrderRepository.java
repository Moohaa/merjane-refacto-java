package com.nimbleways.springboilerplate.repositories;

import com.nimbleways.springboilerplate.entities.ProcessedOrder;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ProcessedOrderRepository extends JpaRepository<ProcessedOrder, Long> {
    @Modifying
    @Query(
            value = "insert into processed_orders (order_id, processed_at) values (:orderId, :processedAt)",
            nativeQuery = true
    )
    void insertProcessedOrder(@Param("orderId") Long orderId, @Param("processedAt") LocalDateTime processedAt);
}
