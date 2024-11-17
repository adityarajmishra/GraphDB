package com.graphdb.persistence;

import com.graphdb.core.model.Node;
import com.graphdb.core.model.Relationship;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PersistenceManager {
    private static final String BASE_PATH = "src/main/resources/graph_database";
    private static final String NODES_PATH = BASE_PATH + "/nodes";
    private static final String RELATIONSHIPS_PATH = BASE_PATH + "/relationships";
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public PersistenceManager() {
        initializeDirectories();
    }

    private void initializeDirectories() {
        try {
            Files.createDirectories(Paths.get(NODES_PATH));
            Files.createDirectories(Paths.get(RELATIONSHIPS_PATH));
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize database directories", e);
        }
    }

    public void saveNode(Node node) {
        lock.writeLock().lock();
        try {
            String labelPath = NODES_PATH + "/" + node.label();
            Files.createDirectories(Paths.get(labelPath));
            String filePath = labelPath + "/" + node.id() + ".bin";

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(filePath))) {
                oos.writeObject((Serializable) node);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save node", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void saveRelationship(Relationship relationship) {
        lock.writeLock().lock();
        try {
            String typePath = RELATIONSHIPS_PATH + "/" + relationship.type();
            Files.createDirectories(Paths.get(typePath));
            String filePath = typePath + "/" + relationship.id() + ".bin";

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(filePath))) {
                oos.writeObject((Serializable) relationship);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save relationship", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void deleteRelationship(Relationship relationship) {
        lock.writeLock().lock();
        try {
            String filePath = RELATIONSHIPS_PATH + "/" + relationship.type() +
                    "/" + relationship.id() + ".bin";
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete relationship", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
}