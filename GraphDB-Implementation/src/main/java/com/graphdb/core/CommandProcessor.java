package com.graphdb.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphdb.core.model.Node;
import com.graphdb.core.model.Relationship;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandProcessor {
    private final GraphDatabase graphDb;
    private final ObjectMapper objectMapper;

    public CommandProcessor(GraphDatabase graphDb) {
        this.graphDb = graphDb;
        this.objectMapper = new ObjectMapper();
    }

    public String processCommand(String command) {
        String[] parts = command.split("\\s+", 2);
        if (parts.length < 2 && !command.equalsIgnoreCase("STOP")) {
            return "INVALID_COMMAND";
        }

        if (command.equalsIgnoreCase("STOP")) {
            return "ADIOS!";
        }

        try {
            return switch (parts[0].toUpperCase()) {
                case "CREATE_NODE" -> processCreateNode(parts[1]);
                case "CREATE_RELATIONSHIP" -> processCreateRelationship(parts[1]);
                case "FIND_NODE" -> processFindNode(parts[1]);
                case "DELETE_RELATIONSHIP" -> processDeleteRelationship(parts[1]);
                case "FIND_RELATED_NODES" -> processFindRelatedNodes(parts[1]);
                case "FIND_MIN_HOPS" -> processFindMinHops(parts[1]);
                default -> "INVALID_COMMAND";
            };
        } catch (Exception e) {
            return "INVALID_COMMAND";
        }
    }

    private String processCreateNode(String json) throws IOException {
        Map<String, Object> data = objectMapper.readValue(json, new TypeReference<>() {});

        long id = Long.parseLong(String.valueOf(data.remove("_id")));
        String label = String.valueOf(data.remove("label"));

        Node node = new Node(id, label, data);
        return graphDb.createNode(node) ? "SUCCESS" : "UNSUCCESSFUL";
    }

    private String processCreateRelationship(String json) throws IOException {
        Map<String, Object> data = objectMapper.readValue(json, new TypeReference<>() {});

        // Check for duplicate relationship ID
        String type = String.valueOf(data.get("type"));
        long id = Long.parseLong(String.valueOf(data.get("_id")));

        // Check if relationship with same ID already exists
        if (graphDb.relationshipExists(type, id)) {
            return "INVALID_COMMAND";
        }

        long source = Long.parseLong(String.valueOf(data.remove("source")));
        long target = Long.parseLong(String.valueOf(data.remove("target")));
        String sourceLabel = String.valueOf(data.remove("node_label_source"));
        String targetLabel = String.valueOf(data.remove("node_label_target"));
        boolean directed = "DIRECTED".equals(data.remove("edge"));
        id = Long.parseLong(String.valueOf(data.remove("_id")));
        type = String.valueOf(data.remove("type"));

        Relationship relationship = new Relationship(id, type, source, target,
                sourceLabel, targetLabel, directed, data);
        return graphDb.createRelationship(relationship) ? "SUCCESS" : "UNSUCCESSFUL";
    }

    private String processFindNode(String json) throws IOException {
        Map<String, Object> criteria = objectMapper.readValue(json, new TypeReference<>() {});
        List<Node> nodes = graphDb.findNodes(criteria);

        if (nodes.isEmpty()) {
            return "NO NODE AVAILABLE";
        }

        return nodes.stream()
                .map(n -> n.label() + "->" + n.id())
                .collect(Collectors.joining("::"));
    }

    private String processDeleteRelationship(String json) throws IOException {
        Map<String, Object> criteria = objectMapper.readValue(json, new TypeReference<>() {});

        // If only type is specified, add it directly to criteria
        if (criteria.containsKey("type")) {
            String type = String.valueOf(criteria.get("type"));
            criteria = new HashMap<>(criteria);
        }

        int count = graphDb.deleteRelationships(criteria);
        return count + " Relationship(s) deleted";
    }

    private String processFindRelatedNodes(String json) throws IOException {
        Map<String, Object> data = objectMapper.readValue(json, new TypeReference<>() {});

        long nodeId = Long.parseLong(String.valueOf(data.remove("_id")));
        String label = String.valueOf(data.remove("label"));
        int depth = Integer.parseInt(String.valueOf(data.remove("depth")));

        List<Node> relatedNodes = graphDb.findRelatedNodes(nodeId, label, depth, data);
        if (relatedNodes.isEmpty()) {
            return "NO_NODES_AVAILABLE";
        }

        return relatedNodes.stream()
                .map(n -> n.label() + "->" + n.id())
                .collect(Collectors.joining("::"));
    }

    private String processFindMinHops(String json) throws IOException {
        Map<String, Object> data = objectMapper.readValue(json, new TypeReference<>() {});

        long sourceId = Long.parseLong(String.valueOf(data.remove("source")));
        long targetId = Long.parseLong(String.valueOf(data.remove("target")));
        String sourceLabel = String.valueOf(data.remove("source_label"));
        String targetLabel = String.valueOf(data.remove("target_label"));

        int hops = graphDb.findMinimumHops(sourceId, sourceLabel, targetId, targetLabel, data);
        if (hops == -1) {
            return "NO_PATH_FOUND";
        }
        return hops + " Hop(s) Required";
    }
}