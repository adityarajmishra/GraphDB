# GraphDB: A Simplified Neo4j-like Graph Database with Java

Welcome to **GraphDB**, a simplified version of a graph database inspired by Neo4j, designed to store, manage, and query graph data efficiently. This project showcases an in-depth use of Java features, including concurrency, thread safety, Java streams, and advanced algorithms for graph traversal and data handling.

## Table of Contents
- [Introduction](#introduction)
- [Features](#features)
- [Data Model](#data-model)
- [Installation](#installation)
- [Usage](#usage)
    - [Command Syntax](#command-syntax)
    - [Examples](#examples)
- [Advanced Graph Algorithms](#advanced-graph-algorithms)
- [Concurrency and Thread Safety](#concurrency-and-thread-safety)
- [File-Based Data Storage](#file-based-data-storage)
- [Limitations and Future Work](#limitations-and-future-work)

## Introduction
GraphDB is a project that simulates a graph database, allowing users to store, retrieve, and manipulate nodes and relationships using a command-line interface (CLI). GraphDB supports basic CRUD operations and complex queries, such as finding relationships and traversing graphs, with thread-safe operations leveraging Java concurrency utilities.

## Features
- **Nodes and Relationships**: Create, retrieve, and delete nodes and relationships with properties.
- **Command-Line Interface**: Perform operations through simple, text-based commands.
- **Graph Traversal and Queries**: Find connections between nodes and explore the graph using built-in traversal techniques.
- **Concurrency**: Thread-safe operations ensure that multiple queries can run simultaneously.
- **File-Based Storage**: Persistent data storage using serialization.
- **Optimizations**: Graph data caching and traversal optimization techniques.
- **Support for Algorithms**: Implementation of advanced algorithms such as A* Search, PageRank, and community detection.

## Data Model
### Nodes
Nodes represent entities in the graph. Each node has:
- **ID**: Unique identifier (integer).
- **Label**: Categorizes the node (e.g., "Person", "Product").
- **Properties**: Key-value pairs (e.g., `{"name": "Alice", "age": 25}`).

**Example:**
```json
{
  "_id": 1,
  "label": "Person",
  "name": "John Doe",
  "age": 40,
  "location": "San Francisco"
}
```

### Relationships

Relationships connect nodes and include:

-   **ID**: Unique identifier (integer).
-   **Type**: Nature of the relationship (e.g., "FRIENDS_WITH").
-   **Source/Target**: IDs of connected nodes.
-   **Properties**: Key-value pairs (e.g., `{"since": "2020-01-01"}`).

**Example:**
```json
{
  "_id": 101,
  "type": "FRIENDS_WITH",
  "source": 1,
  "target": 2,
  "since": "2020-01-01"
}
```

**Installation**
```
git clone https://github.com/adityarajmishra/GraphDB.git
cd graphdb
```

Usage
-----

### Command Syntax

#### Creating a Node
```
CREATE NODE {"_id": <id>, "label": <label>, <key>: <value>, ...}
```
* **Required fields:**
    * `_id` (integer)
    * `label` (string)

* **Output:**
    * `ID_CONFLICT` if the `_id` already exists
    * `SUCCESS` on successful creation

**Creating a Relationship**
```
CREATE RELATIONSHIP {"_id": 101, "type": <type>, "node_label_source": <label>, "source": 1, "node_label_target": <label>, "target": 2, "edge": <edge>, <key>: <value>, ...}
```
* **Required fields:**
    * `_id`
    * `type`
    * `node_label_source`
    * `node_label_target`
    * `source`
    * `target`
    * `edge` (can be `DIRECTED` or `UNDIRECTED`)

* **Output:**
    * `SUCCESS` or `UNSUCCESSFUL`

**Finding Nodes**
```
FIND NODE { <key>: <value>, <key>: <value>, ... }
```
* **Output:**
    * `label->id::label->id` for each matching node
    * `NO NODE AVAILABLE` if no matches

**Deleting Relationships**
```
DELETE RELATIONSHIP { <key>: <value>, <key>: <value>, ... }
```
* **Output:**
    * `<count> Relationship(s) deleted.` on success

**Finding Common Relations**
```
FIND COMMON RELATIONS { "node1": 6, "label1": <label1>, "node2": 4, "label2": <label2>, "type": <relationship_type>, <key>: <value>, ... }
```
* **Output:**
    * Finds relationships between the specified nodes and of the given type

**Advanced Graph Algorithms**

* A* Search: Pathfinding between nodes
* PageRank: Node importance ranking
* Community Detection: Identifies clusters of related nodes

**Concurrency and Thread Safety**

* Uses Java's synchronized blocks and ConcurrentHashMap for safe multi-threaded access
* Leverages Java streams and lambdas for performance and readability

**File-Based Data Storage**

* Data is serialized and stored in files for persistence
* Paths and configuration are customizable

**Limitations and Future Work**

* **Current Limitations:**
    * Basic indexing
    * No advanced query optimization

* **Future Enhancements:**
    * More sophisticated indexing
    * Enhanced query performance
    * GUI for easier interaction

We hope this project helps you understand the power of graph data modeling and Java programming. Contributions and suggestions are always welcome!


