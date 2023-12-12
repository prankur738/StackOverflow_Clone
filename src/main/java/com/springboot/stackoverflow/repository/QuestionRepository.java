package com.springboot.stackoverflow.repository;

import com.springboot.stackoverflow.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question,Integer> {
    @Query("""
            SELECT q FROM Question q
            WHERE (LOWER(q.title) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(q.content) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(q.author) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR EXISTS (SELECT t FROM q.tags t
                              WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))))
            """)
    Page<Question> searchQuestionsPaginated(String search, Pageable pageable);
}
