package com.graphdb.core;

import com.graphdb.core.model.Node;
import com.graphdb.core.model.Relationship;
import com.graphdb.core.algorithms.GraphTraversal;
import com.graphdb.persistence.PersistenceManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class GraphDatabase {
    private final Map<String, Map<Long, Node>> nodesByLabel;
    private final Map<String, Map<Long, Relationship>> relationshipsByType;
    private final ReadWriteLock lock;
    private final PersistenceManager persistenceManager;

    public GraphDatabase() {
        this.nodesByLabel = new ConcurrentHashMap<>();
        this.relationshipsByType = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.persistenceManager = new PersistenceManager();
    }

    public boolean createNode(Node node) {
        lock.writeLock().lock();
        try {
            Map<Long, Node> nodes = nodesByLabel.computeIfAbsent(
                    node.label(), k -> new ConcurrentHashMap<>());

            if (nodes.containsKey(node.id())) {
                return false;
            }

            nodes.put(node.id(), node);
            persistenceManager.saveNode((com.graphdb.core.model.Node) node);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean createRelationship(Relationship relationship) {
        lock.writeLock().lock();
        try {
            // Check if relationship already exists
            if (relationshipsByType.containsKey(relationship.type()) &&
                    relationshipsByType.get(relationship.type()).containsKey(relationship.id())) {
                return false;
            }

            // Verify nodes exist
            if (!nodeExists(relationship.sourceNodeId(), relationship.sourceLabel()) ||
                    !nodeExists(relationship.targetNodeId(), relationship.targetLabel())) {
                return false;
            }

            Map<Long, Relationship> relationships = relationshipsByType.computeIfAbsent(
                    relationship.type(), k -> new ConcurrentHashMap<>());

            relationships.put(relationship.id(), relationship);
            persistenceManager.saveRelationship(relationship);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Node> findNodes(Map<String, Object> criteria) {
        lock.readLock().lock();
        try {
            return nodesByLabel.values().stream()
                    .flatMap(labelMap -> labelMap.values().stream())
                    .filter(node -> matchesCriteria(node.properties(), criteria))
                    .sorted(Comparator.comparing(Node::label).thenComparing(Node::id))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public int deleteRelationships(Map<String, Object> criteria) {
        lock.writeLock().lock();
        try {
            List<Relationship> toDelete = new ArrayList<>();

            // Find all relationships that match the criteria
            for (Map<Long, Relationship> typeMap : relationshipsByType.values()) {
                for (Relationship rel : typeMap.values()) {
                    boolean matches = true;
                    for (Map.Entry<String, Object> entry : criteria.entrySet()) {
                        Object relValue = rel.getProperties().get(entry.getKey());
                        Object criteriaValue = entry.getValue();

                        // Special handling for type field which is not in properties
                        if (entry.getKey().equals("type")) {
                            if (!rel.getType().equals(criteriaValue)) {
                                matches = false;
                                break;
                            }
                            continue;
                        }

                        if (relValue == null || !relValue.equals(criteriaValue)) {
                            matches = false;
                            break;
                        }
                    }
                    if (matches) {
                        toDelete.add(rel);
                    }
                }
            }

            // Delete the matched relationships
            for (Relationship rel : toDelete) {
                relationshipsByType.get(rel.getType()).remove(rel.getId());
                persistenceManager.deleteRelationship(rel);
            }

            return toDelete.size();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private boolean nodeExists(long id, String label) {
        Map<Long, Node> nodes = nodesByLabel.get(label);
        return nodes != null && nodes.containsKey(id);
    }

    private boolean matchesCriteria(Map<String, Object> properties, Map<String, Object> criteria) {
        return criteria.entrySet().stream()
                .allMatch(entry -> Objects.equals(properties.get(entry.getKey()), entry.getValue()));
    }

    public List<Node> findCommonRelations(long node1Id, String label1,
                                          long node2Id, String label2, String relationType,
                                          Map<String, Object> filters) {
        lock.readLock().lock();
        try {
            Node node1 = nodesByLabel.get(label1).get(node1Id);
            Node node2 = nodesByLabel.get(label2).get(node2Id);

            if (node1 == null || node2 == null) {
                return Collections.emptyList();
            }

            return GraphTraversal.findCommonRelations(
                            node1, node2, relationType, nodesByLabel, relationshipsByType, filters)
                    .stream()
                    .sorted(Comparator.comparing(Node::label).thenComparing(Node::id))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Node> findRelatedNodes(long nodeId, String label, int depth,
                                       Map<String, Object> filters) {
        lock.readLock().lock();
        try {
            Map<Long, Node> labelMap = nodesByLabel.get(label);
            if (labelMap == null) return Collections.emptyList();

            Node startNode = labelMap.get(nodeId);
            if (startNode == null) return Collections.emptyList();

            return GraphTraversal.findRelatedNodes(
                            startNode, depth, nodesByLabel, relationshipsByType, filters)
                    .stream()
                    .sorted(Comparator.comparing(Node::label).thenComparing(Node::id))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public int findMinimumHops(long sourceId, String sourceLabel,
                               long targetId, String targetLabel, Map<String, Object> intermediaryFilters) {
        lock.readLock().lock();
        try {
            Node source = nodesByLabel.get(sourceLabel).get(sourceId);
            Node target = nodesByLabel.get(targetLabel).get(targetId);

            if (source == null || target == null) {
                return -1;
            }

            return GraphTraversal.findMinimumHops(
                    source, target, nodesByLabel, relationshipsByType, intermediaryFilters);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean relationshipExists(String type, long id) {
        Map<Long, Relationship> relationships = relationshipsByType.get(type);
        return relationships != null && relationships.containsKey(id);
    }
}