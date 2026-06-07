package com.meetingos.repository;

import com.meetingos.entity.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DecisionRepository extends JpaRepository<Decision, UUID> {

    List<Decision> findByMeetingId(UUID meetingId);

    @Query(value = """
            SELECT * FROM decisions
            WHERE to_tsvector(
                'english',
                coalesce(title,'') || ' ' ||
                coalesce(reason,'') || ' ' ||
                coalesce(CAST(alternatives AS text),'') || ' ' ||
                coalesce(CAST(tradeoffs AS text),'')
            )
            @@ plainto_tsquery('english', :query)
            ORDER BY ts_rank(
                to_tsvector(
                    'english',
                    coalesce(title,'') || ' ' ||
                    coalesce(reason,'') || ' ' ||
                    coalesce(CAST(alternatives AS text),'') || ' ' ||
                    coalesce(CAST(tradeoffs AS text),'')
                ),
                plainto_tsquery('english', :query)
            ) DESC
            """, nativeQuery = true)
    List<Decision> fullTextSearch(@Param("query") String query);
}
