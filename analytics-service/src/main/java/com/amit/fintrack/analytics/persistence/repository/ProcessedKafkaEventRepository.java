package com.amit.fintrack.analytics.persistence.repository;

import com.amit.fintrack.analytics.persistence.entity.ProcessedKafkaEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedKafkaEventRepository extends JpaRepository<ProcessedKafkaEvent, UUID> {
}