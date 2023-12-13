package com.springboot.stackoverflow.repository;

import com.springboot.stackoverflow.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Integer> {
    List<Badge> findByReputationRequiredLessThanEqual(Integer reputation);
}
