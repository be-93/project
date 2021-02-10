package com.backend.project.repository;

import com.backend.project.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapRepository extends JpaRepository<Scrap, String> {
}
