    package com.graphdb.core;

    import com.fasterxml.jackson.core.type.TypeReference;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.graphdb.core.model.Node;
    import com.graphdb.core.model.Relationship;
    import java.io.IOException;
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
            if (command.equalsIgnoreCase("STOP")) {
                return "ADIOS!";
            }

            String[] parts = command.split("\\s+", 2);
            if (parts.length < 2) {
                return "INVALID_COMMAND";
            }

            try {
                return switch (parts[0].toUpperCase()) {
                    case "CREATE_NODE" -> processCreateNode(parts[1]);
                    case "CREATE_RELATIONSHIP" -> processCreateRelationship(parts[1]);
                    case "FIND_NODE" -> processFindNode(parts[1]);
                    case "DELETE_RELATIONSHIP" -> processDeleteRelationship(parts[1]);
                    case "FIND_COMMON_RELATIONS" -> processFindCommonRelations(parts[1]);
                    case "FIND_RELATED_NODES" -> processFindRelatedNodes(parts[1]);
                    case "FIND_MIN_HOPS" -> processFindMinHops(parts[1]);
                    default -> "INVALID_COMMAND";
                };
            } catch (Exception e) {
                return "INVALID_COMMAND";
            }
        }

        private String processCreateNode(String json) {
            try {
                Map<String, Object> data = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

                long id = Long.parseLong(String.valueOf(data.remove("_id")));
                String label = String.valueOf(data.remove("label"));

                Node node = new Node(id, label, data);
                return graphDb.createNode(node) ? "SUCCESS" : "ID_CONFLICT";
            } catch (IOException e) {
                return "INVALID_COMMAND";
            }
        }

        private String processCreateRelationship(String json) {
            try {
                Map<String, Object> data = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

                long id = Long.parseLong(String.valueOf(data.remove("_id")));
                String type = String.valueOf(data.remove("type"));
                long source = Long.parseLong(String.valueOf(data.remove("source")));
                long target = Long.parseLong(String.valueOf(data.remove("target")));
                String sourceLabel = String.valueOf(data.remove("node_label_source"));
                String targetLabel = String.valueOf(data.remove("node_label_target"));
                boolean directed = "DIRECTED".equals(data.remove("edge"));

                Relationship relationship = new Relationship(id, type, source, target,
                        sourceLabel, targetLabel, directed, data);

                return graphDb.createRelationship(relationship) ? "SUCCESS" : "UNSUCCESSFUL";
            } catch (IOException e) {
                return "INVALID_COMMAND";
            }
        }

        private String processFindNode(String json) {
            try {
                Map<String, Object> criteria = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

                var nodes = graphDb.findNodes(criteria);
                if (nodes.isEmpty()) {
                    return "NO NODE AVAILABLE";
                }

                return nodes.stream()
                        .map(n -> n.label() + "->" + n.id())
                        .collect(Collectors.joining("::"));
            } catch (IOException e) {
                return "INVALID_COMMAND";
            }
        }

        private String processDeleteRelationship(String json) {
            try {
                Map<String, Object> criteria = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
                int count = graphDb.deleteRelationships(criteria);
                return count + " Relationship(s) deleted";
            } catch (IOException e) {
                return "INVALID_COMMAND";
            }
        }

        private String processFindCommonRelations(String json) {
            try {
                Map<String, Object> data = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

                long node1Id = Long.parseLong(String.valueOf(data.remove("node1")));
                long node2Id = Long.parseLong(String.valueOf(data.remove("node2")));
                String label1 = String.valueOf(data.remove("label1"));
                String label2 = String.valueOf(data.remove("label2"));
                String type = String.valueOf(data.remove("type"));

                List<Node> commonNodes = graphDb.findCommonRelations(node1Id, label1, node2Id, label2, type, data);
                if (commonNodes.isEmpty()) {
                    return "NO COMMON RELATION FOUND";
                }

                return commonNodes.stream()
                        .map(n -> n.label() + "->" + n.id())
                        .collect(Collectors.joining("::"));
            } catch (IOException e) {
                return "INVALID_COMMAND";
            }
        }

        private String processFindRelatedNodes(String json) {
            try {
                Map<String, Object> data = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

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
            } catch (IOException e) {
                return "INVALID_COMMAND";
            }
        }

        private String processFindMinHops(String json) {
            try {
                Map<String, Object> data = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

                long sourceId = Long.parseLong(String.valueOf(data.remove("source")));
                long targetId = Long.parseLong(String.valueOf(data.remove("target")));
                String sourceLabel = String.valueOf(data.remove("source_label"));
                String targetLabel = String.valueOf(data.remove("target_label"));

                int hops = graphDb.findMinimumHops(sourceId, sourceLabel, targetId, targetLabel, data);
                return hops + " Hop(s) Required";
            } catch (IOException e) {
                return "INVALID_COMMAND";
            }
        }
    }