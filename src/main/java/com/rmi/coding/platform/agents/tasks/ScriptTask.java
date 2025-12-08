package com.rmi.coding.platform.agents.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmi.coding.platform.agents.result.ScriptResult;
import com.rmi.coding.platform.common.AgentCallback;
import com.rmi.coding.platform.model.TestCase;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ScriptTask(String code, String language, List<TestCase> testCases) implements Serializable {

    private static final ObjectMapper mapper = new ObjectMapper();

    public void run(AgentCallback callback) throws RemoteException {
        File temp = null;

        try {
            String interpreter = switch (language.toLowerCase()) {
                case "python" -> "python";
                case "javascript" -> "node";
                default -> throw new IllegalArgumentException("Unsupported language: " + language);
            };

            long totalStart = System.currentTimeMillis();

            int passedCount = 0;
            int total = testCases.size();
            StringBuilder finalOutput = new StringBuilder();

            for (TestCase tc : testCases) {
                // Tạo file code tạm
                temp = File.createTempFile("script_", language.equals("python") ? ".py" : ".js");
                try (BufferedWriter writer =
                             new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temp), StandardCharsets.UTF_8))) {
                    writer.write(code);
                }

                ProcessBuilder pb = new ProcessBuilder(interpreter, temp.getAbsolutePath());
                pb.redirectErrorStream(true);
                Process process = pb.start();

                // gửi input
                if (tc.getInput() != null && !tc.getInput().isEmpty()) {
                    Map<String, Object> jsonMap = new HashMap<>();
                    jsonMap.put("input", mapper.readValue(tc.getInput(), Object.class));
                    jsonMap.put("expected",
                            tc.getExpectedOutput() != null
                                    ? mapper.readValue(tc.getExpectedOutput(), Object.class)
                                    : null);

                    String jsonInput = mapper.writeValueAsString(jsonMap);
                    try (BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                        pw.write(jsonInput);
                        pw.newLine();
                        pw.flush();
                    }
                } else {
                    process.getOutputStream().close();
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    output.append(line).append("\n");

                process.waitFor();

                String actualRaw = output.toString().trim();
                String expectedRaw = tc.getExpectedOutput() != null ? tc.getExpectedOutput().trim() : "";

                boolean pass;
                try {
                    Object actualObj = mapper.readValue(actualRaw, Object.class);
                    Object expectedObj = mapper.readValue(expectedRaw, Object.class);
                    pass = actualObj.equals(expectedObj);
                } catch (Exception e) {
                    pass = actualRaw.equals(expectedRaw);
                }

                if (pass) passedCount++;

                finalOutput.append("TestCase ").append(tc.getId())
                        .append(": ").append(pass ? "PASS" : "FAIL")
                        .append("\nExpected: ").append(expectedRaw)
                        .append("\nActual: ").append(actualRaw)
                        .append("\n\n");
            }

            long totalTime = System.currentTimeMillis() - totalStart;

            ScriptResult result = new ScriptResult(
                    passedCount == total,
                    passedCount,
                    total,
                    totalTime,
                    finalOutput.toString()
            );

            callback.onResult(result);

        } catch (Exception e) {
            callback.onError(e.getMessage());
        } finally {
            if (temp != null && temp.exists()) temp.delete();
        }
    }

}
