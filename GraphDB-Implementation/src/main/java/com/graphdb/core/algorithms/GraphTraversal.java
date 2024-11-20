package com.graphdb.core.algorithms;

import com.graphdb.core.model.Node;
import com.graphdb.core.model.Relationship;

import java.util.*;
import java.util.stream.Collectors;

public class GraphTraversal {
    public static List<Node> findRelatedNodes(Node startNode, int maxDepth,
                                              Map<String, Map<Long, Node>> nodesByLabel,
                                              Map<String, Map<Long, Relationship>> relationshipsByType,
                                              Map<String, Object> filters) {

        Set<Node> visited = new HashSet<>();
        Set<Node> result = new HashSet<>();
        Queue<NodeDepthPair> queue = new LinkedList<>();
        queue.offer(new NodeDepthPair(startNode, 0));

        while (!queue.isEmpty()) {
            NodeDepthPair current = queue.poll();
            Node currentNode = current.node;
            int currentDepth = current.depth;

            if (currentDepth > maxDepth) continue;
            if (!visited.add(currentNode)) continue;

            if (currentDepth > 0 && matchesFilters(currentNode, filters)) {
                result.add(currentNode);
            }

            // Get all relationships for current node
            List<Relationship> relationships = findNodeRelationships(currentNode, relationshipsByType);

            for (Relationship rel : relationships) {
                Node nextNode = null;

                if (rel.sourceNodeId() == currentNode.id()) {
                    nextNode = findNodeById(rel.targetNodeId(), rel.targetLabel(), nodesByLabel);
                } else if (!rel.directed() && rel.targetNodeId() == currentNode.id()) {
                    nextNode = findNodeById(rel.sourceNodeId(), rel.sourceLabel(), nodesByLabel);
                }

                if (nextNode != null && !visited.contains(nextNode)) {
                    queue.offer(new NodeDepthPair(nextNode, currentDepth + 1));
                }
            }
        }

        return new ArrayList<>(result);
    }

    public static List<Node> findCommonRelations(Node node1, Node node2, String relationType,
                                                 Map<String, Map<Long, Node>> nodesByLabel,
                                                 Map<String, Map<Long, Relationship>> relationshipsByType,
                                                 Map<String, Object> filters) {

        Set<Node> node1Relations = getRelatedNodes(node1, relationType, relationshipsByType, nodesByLabel, filters);
        Set<Node> node2Relations = getRelatedNodes(node2, relationType, relationshipsByType, nodesByLabel, filters);

        node1Relations.retainAll(node2Relations);
        return new ArrayList<>(node1Relations);
    }

    public static int findMinimumHops(Node source, Node target,
                                      Map<String, Map<Long, Node>> nodesByLabel,
                                      Map<String, Map<Long, Relationship>> relationshipsByType,
                                      Map<String, Object> intermediaryFilters) {

        Queue<NodeDepthPair> queue = new LinkedList<>();
        Set<Long> visited = new HashSet<>();
        queue.offer(new NodeDepthPair(source, 0));
        visited.add(source.id());

        while (!queue.isEmpty()) {
            NodeDepthPair current = queue.poll();
            Node currentNode = current.node;

            if (currentNode.equals(target)) {
                return current.depth;
            }

            List<Relationship> relationships = findNodeRelationships(currentNode, relationshipsByType);

            for (Relationship rel : relationships) {
                Node nextNode = null;

                if (rel.sourceNodeId() == currentNode.id()) {
                    nextNode = findNodeById(rel.targetNodeId(), rel.targetLabel(), nodesByLabel);
                } else if (!rel.directed() && rel.targetNodeId() == currentNode.id()) {
                    nextNode = findNodeById(rel.sourceNodeId(), rel.sourceLabel(), nodesByLabel);
                }

                if (nextNode != null && !visited.contains(nextNode.id())) {
                    if (nextNode.equals(target) || matchesFilters(nextNode, intermediaryFilters)) {
                        queue.offer(new NodeDepthPair(nextNode, current.depth + 1));
                        visited.add(nextNode.id());
                    }
                }
            }
        }

        return -1;  // No path found
    }

    private static class NodeDepthPair {
        Node node;
        int depth;

        NodeDepthPair(Node node, int depth) {
            this.node = node;
            this.depth = depth;
        }
    }

    private static List<Relationship> findNodeRelationships(Node node,
                                                            Map<String, Map<Long, Relationship>> relationshipsByType) {
        return relationshipsByType.values().stream()
                .flatMap(typeMap -> typeMap.values().stream())
                .filter(rel -> rel.sourceNodeId() == node.id() ||
                        (!rel.directed() && rel.targetNodeId() == node.id()))
                .collect(Collectors.toList());
    }

    private static Node findNodeById(long id, String label,
                                     Map<String, Map<Long, Node>> nodesByLabel) {
        Map<Long, Node> labelMap = nodesByLabel.get(label);
        return labelMap != null ? labelMap.get(id) : null;
    }

    private static Set<Node> getRelatedNodes(Node node, String relationType,
                                             Map<String, Map<Long, Relationship>> relationshipsByType,
                                             Map<String, Map<Long, Node>> nodesByLabel,
                                             Map<String, Object> filters) {

        Set<Node> relatedNodes = new HashSet<>();
        Map<Long, Relationship> relationships = relationshipsByType.getOrDefault(relationType, new HashMap<>());

        for (Relationship rel : relationships.values()) {
            if (rel.sourceNodeId() == node.id()) {
                Node targetNode = findNodeById(rel.targetNodeId(), rel.targetLabel(), nodesByLabel);
                if (targetNode != null && matchesFilters(targetNode, filters)) {
                    relatedNodes.add(targetNode);
                }
            } else if (!rel.directed() && rel.targetNodeId() == node.id()) {
                Node sourceNode = findNodeById(rel.sourceNodeId(), rel.sourceLabel(), nodesByLabel);
                if (sourceNode != null && matchesFilters(sourceNode, filters)) {
                    relatedNodes.add(sourceNode);
                }
            }
        }

        return relatedNodes;
    }

    private static boolean matchesFilters(Node node, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) return true;

        Map<String, Object> nodeProps = node.properties();
        return filters.entrySet().stream()
                .allMatch(entry -> Objects.equals(nodeProps.get(entry.getKey()), entry.getValue()));
    }
}