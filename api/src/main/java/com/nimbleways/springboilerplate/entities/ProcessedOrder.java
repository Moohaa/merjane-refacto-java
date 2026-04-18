package com.nimbleways.springboilerplate.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "processed_orders")
public class ProcessedOrder {
    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private Long orderId;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
}
