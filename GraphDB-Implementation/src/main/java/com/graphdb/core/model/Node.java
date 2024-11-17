package com.graphdb.core.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record Node(long id, String label, Map<String, Object> properties) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public Node(long id, String label, Map<String, Object> properties) {
        this.id = id;
        this.label = label;
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
        Node node = (Node) o;
        return id == node.id && Objects.equals(label, node.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label);
    }
}