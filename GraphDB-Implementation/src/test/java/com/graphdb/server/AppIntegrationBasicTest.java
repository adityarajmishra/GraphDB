package com.graphdb.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppIntegrationBasicTest {

    private void assertAppOutput(String input, String expectedOutput) {
        // Set up input stream
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        // Set up output stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        // Run the main method
        App.main(new String[]{});

        // Capture and process the output
        String actualOutput = out.toString().replaceAll("\\r\\n", "\n").trim();
        String formattedExpectedOutput = expectedOutput.replaceAll("\\r\\n", "\n").trim();

        if (!formattedExpectedOutput.equals(actualOutput)) {
            // Split the expected and actual outputs into lines
            String[] expectedLines = formattedExpectedOutput.split("\n");
            String[] actualLines = actualOutput.split("\n");

            // Determine the maximum length of the expected and actual output lines for proper alignment
            int maxExpectedLength = 0;
            int maxActualLength = 0;
            for (String line : expectedLines) {
                if (line.length() > maxExpectedLength) {
                    maxExpectedLength = line.length();
                }
            }
            for (String line : actualLines) {
                if (line.length() > maxActualLength) {
                    maxActualLength = line.length();
                }
            }

            // Determine the width of the header and format
            int columnWidth = Math.max(maxExpectedLength, maxActualLength) + 5;
            String expectedHeader = "===EXPECTED OUTPUT===";
            String actualHeader = "===ACTUAL OUTPUT===";
            String header = String.format("%-" + columnWidth + "s | %s", expectedHeader, actualHeader);

            // ANSI escape codes for coloring
            String redColor = "\u001B[31m";
//            String blue = "\u001B[34m";
            String magentaColor = "\u001B[35m";
            String resetColor = "\u001B[0m";

            // Build the failure message with aligned columns
            StringBuilder failureMessage = new StringBuilder();
            failureMessage.append(redColor).append("Test Failed For:\n").append(resetColor);
//            failureMessage.append(blue).append("====INPUT SEQUENCE====\n").append(input.trim()).append(resetColor).append("\n");
            failureMessage.append(magentaColor).append("====INPUT SEQUENCE====\n").append(resetColor).append(input.trim()).append("\n");
            failureMessage.append(magentaColor).append(header).append(resetColor).append("\n");

            int maxLines = Math.max(expectedLines.length, actualLines.length);
            for (int i = 0; i < maxLines; i++) {
                String expectedLine = i < expectedLines.length ? expectedLines[i] : "";
                String actualLine = i < actualLines.length ? actualLines[i] : "";

                // Add spaces to the expected line to align with the actual output column
                String formattedExpectedLine = String.format("%-" + columnWidth + "s", expectedLine);

                if (!expectedLine.equals(actualLine)) {
                    failureMessage.append(redColor).append(formattedExpectedLine).append(" | ").append(actualLine).append(resetColor).append("\n");
                } else {
                    failureMessage.append(formattedExpectedLine).append(" | ").append(actualLine).append("\n");
                }
            }

            Assertions.fail(failureMessage.toString());
        }

        // Assert equality with detailed message
        assertEquals(formattedExpectedOutput, actualOutput);
    }

    @Test
    public void testExample1() {
        String input =
                "CREATE_NODE { \"_id\": 1, \"label\": \"Person\", \"name\": \"Alice\", \"age\": 25, \"location\": \"NY\" }\n" +
                        "CREATE_NODE { \"_id\": 2, \"label\": \"Person\", \"name\": \"Bob\", \"age\": 30, \"location\": \"SF\" }\n" +
                        "CREATE_RELATIONSHIP { \"_id\": 101, \"type\": \"FRIENDS_WITH\", \"node_label_source\": \"Person\", \"source\": 1, \"node_label_target\": \"Person\", \"target\": 2, \"edge\": \"UNDIRECTED\", \"since\": \"2021-01-01\" }\n" +
                        "FIND_NODE { \"location\": \"NY\" }\n" +
                        "DELETE_RELATIONSHIP { \"type\": \"FRIENDS_WITH\" }\n" +
                        "STOP";
        String expectedOutput =
                "SUCCESS\n" +
                        "SUCCESS\n" +
                        "SUCCESS\n" +
                        "Person->1\n" +
                        "1 Relationship(s) deleted\n" +
                        "ADIOS!\n";

        assertAppOutput(input, expectedOutput);
    }

    @Test
    public void testExample2() {
        String input =
                "CREATE_NODE { \"_id\": 3, \"label\": \"Product\", \"name\": \"Smartphone\", \"brand\": \"TechBrand\", \"price\": 699 }\n" +
                        "CREATE_NODE { \"_id\": 4, \"label\": \"Category\", \"name\": \"Electronics\" }\n" +
                        "CREATE_RELATIONSHIP { \"_id\": 102, \"type\": \"BELONGS_TO\", \"node_label_source\": \"Product\", \"source\": 3, \"node_label_target\": \"Category\", \"target\": 4, \"edge\": \"DIRECTED\", \"since\": \"2023-08-10\" }\n" +
                        "CREATE_RELATIONSHIP { \"_id\": 102, \"type\": \"BELONGS_TO\", \"node_label_source\": \"Product\", \"source\": 3, \"node_label_target\": \"Category\", \"target\": 4, \"edge\": \"DIRECTED\", \"since\": \"2023-08-10\" }\n" +
                        "STOP";
        String expectedOutput =
                "SUCCESS\n" +
                        "SUCCESS\n" +
                        "SUCCESS\n" +
                        "INVALID_COMMAND\n" +
                        "ADIOS!\n";

        assertAppOutput(input, expectedOutput);
    }

    @Test
    public void testExample3() {
        String input =
                "CREATE_NODE { \"_id\": 5, \"label\": \"Book\", \"title\": \"1984\", \"author\": \"George Orwell\", \"year\": 1949 }\n" +
                        "CREATE_NODE { \"_id\": 6, \"label\": \"Person\", \"name\": \"Sam\", \"location\": \"LA\" }\n" +
                        "CREATE_RELATIONSHIP { \"_id\": 103, \"type\": \"LIKES\", \"node_label_source\": \"Person\", \"source\": 6, \"node_label_target\": \"Book\", \"target\": 5, \"edge\": \"DIRECTED\", \"since\": \"2022-05-15\" }\n" +
                        "FIND_RELATED_NODES { \"_id\": 6, \"depth\": 1, \"label\": \"Person\" }\n" +
                        "DELETE_RELATIONSHIP { \"type\": \"LIKES\", \"since\": \"2022-05-15\" }\n" +
                        "STOP";
        String expectedOutput =
                "SUCCESS\n" +
                        "SUCCESS\n" +
                        "SUCCESS\n" +
                        "Book->5\n" +
                        "1 Relationship(s) deleted\n" +
                        "ADIOS!\n";

        assertAppOutput(input, expectedOutput);
    }

    @Test
    public void testExample4() {
        String input =
                "CREATE_NODE { \"_id\": 7, \"label\": \"User\", \"username\": \"john_doe\", \"email\": \"john@example.com\", \"joined\": \"2020-01-01\" }\n" +
                        "CREATE_NODE { \"_id\": 8, \"label\": \"Post\", \"content\": \"Hello World\", \"posted_at\": \"2020-01-02\" }\n" +
                        "CREATE_NODE { \"_id\": 11, \"label\": \"User\", \"username\": \"jane_doe\", \"email\": \"jane@example.com\", \"joined\": \"2020-01-03\" }\n" +
                        "CREATE_RELATIONSHIP { \"_id\": 104, \"type\": \"POSTED\", \"node_label_source\": \"User\", \"source\": 7, \"node_label_target\": \"Post\", \"target\": 8, \"edge\": \"DIRECTED\" }\n" +
                        "CREATE_RELATIONSHIP { \"_id\": 104, \"type\": \"FRIENDS_WITH\", \"node_label_source\": \"User\", \"source\": 7, \"node_label_target\": \"User\", \"target\": 11, \"edge\": \"UNDIRECTED\" }\n" +
                        "FIND_MIN_HOPS { \"source\": 11, \"source_label\": \"User\", \"target\": 8, \"target_label\": \"Post\" }\n" +
                        "STOP";
        String expectedOutput =
                "SUCCESS\n" +
                        "SUCCESS\n" +
                        "SUCCESS\n" +
                        "SUCCESS\n" +
                        "SUCCESS\n" +
                        "2 Hop(s) Required\n" +
                        "ADIOS!\n";

        assertAppOutput(input, expectedOutput);
    }

    @Test
    public void testExample5() {
        String input =
                "CREATE_NODE { \"_id\": 9, \"label\": \"Movie\", \"title\": \"Inception\", \"director\": \"Christopher Nolan\", \"year\": 2010 }\n" +
                        "CREATE_NODE { \"_id\": 10, \"label\": \"Actor\", \"name\": \"Leonardo DiCaprio\", \"birth_year\": 1974 }\n" +
                        "CREATE_RELATIONSHIP { \"_id\": 105, \"type\": \"ACTED_IN\", \"node_label_source\": \"Actor\", \"source\": 10, \"node_label_target\": \"Movie\", \"target\": 9, \"edge\": \"DIRECTED\" }\n" +
                        "FIND_RELATED_NODES { \"label\": \"Movie\", \"_id\": 9, \"depth\": 1 }\n" +
                        "STOP";
        String expectedOutput =
                "SUCCESS\n" +
                        "SUCCESS\n" +
                        "SUCCESS\n" +
                        "NO_NODES_AVAILABLE\n" +
                        "ADIOS!\n";

        assertAppOutput(input, expectedOutput);
    }

}
