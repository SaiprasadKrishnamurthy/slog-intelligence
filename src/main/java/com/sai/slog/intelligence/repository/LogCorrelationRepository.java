package com.sai.slog.intelligence.repository;

import com.sai.slog.intelligence.domain.LogCorrelation;
import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LogCorrelationRepository extends GraphRepository<LogCorrelation> {

    @Depth(value = -1)
    List<LogCorrelation> findByComponent(String component);

    @Query("MATCH p=(a)-[r:DIRECTLY_CAUSED_BY*]->(b) WITH b,LENGTH(p) AS depth WHERE a.logMessage CONTAINS {message} AND depth={level} RETURN b")
    List<LogCorrelation> findByLevel(@Param("message") final String message, @Param("level") final int level);

    @Query("MATCH ()-[r:DIRECTLY_CAUSED_BY]->(n)\n" +
            "WITH n, count(r) as rel_cnt\n" +
            "WHERE rel_cnt >= 2\n" +
            "RETURN n\n" +
            "order by rel_cnt desc")
    List<LogCorrelation> detrimentalExceptions();
}