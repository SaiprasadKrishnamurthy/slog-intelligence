package com.sai.slog.intelligence.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.springframework.data.neo4j.annotation.*;


import java.util.HashSet;
import java.util.Set;

/**
 * Created by saipkri on 21/02/17.
 */
@Data
@NodeEntity
@EqualsAndHashCode(exclude = {"directlyCausedBy", "potentiallyCausedBy"})
@ToString(of = {"description"})
public class LogCorrelation {

    @GraphId
    private Long graphId;

    private String logMessage;

    private String component;

    @Index(unique = true, primary = true)
    private String description;

    private boolean disabled;

    @Relationship(type = "DIRECTLY_CAUSED_BY")
    private Set<LogCorrelation> directlyCausedBy = new HashSet<>();

    @Relationship(type = "POTENTIALLY_CAUSED_BY")
    private Set<LogCorrelation> potentiallyCausedBy = new HashSet<>();

    public void addDirectCause(final LogCorrelation logCorrelation) {
        this.directlyCausedBy.add(logCorrelation);
    }

    public void addPotentialCause(final LogCorrelation logCorrelation) {
        this.potentiallyCausedBy.add(logCorrelation);
    }
}
