package com.graphdb.core.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record Relationship(long id, String type, long sourceNodeId, long targetNodeId, String sourceLabel,
                           String targetLabel, boolean directed,
                           Map<String, Object> properties) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public Relationship(long id, String type, long sourceNodeId, long targetNodeId,
                        String sourceLabel, String targetLabel, boolean directed,
                        Map<String, Object> properties) {
        this.id = id;
        this.type = type;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.sourceLabel = sourceLabel;
        this.targetLabel = targetLabel;
        this.directed = directed;
        this.properties = new HashMap<>(properties);
    }

    @Override
    public Map<String, Object> properties() {
        return new HashMap<>(properties);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relationship that = (Relationship) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}