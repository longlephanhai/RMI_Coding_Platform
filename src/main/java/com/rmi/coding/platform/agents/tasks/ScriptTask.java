package com.rmi.coding.platform.agents.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmi.coding.platform.common.AgentCallback;
import com.rmi.coding.platform.model.TestCase;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ScriptTask(String code, String language, List<TestCase> testCases) implements Serializable {

    private static final ObjectMapper mapper = new ObjectMapper();

    public void run(AgentCallback callback) {
        File temp = null;
        try {
            // Chọn interpreter
            String interpreter;
            switch (language.toLowerCase()) {
                case "python":
                    interpreter = "python";
                    break;
                case "javascript":
                    interpreter = "node";
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported language: " + language);
            }

            StringBuilder finalOutput = new StringBuilder();

            for (TestCase tc : testCases) {
                // Tạo file tạm cho script
                temp = File.createTempFile("script_", language.equals("python") ? ".py" : ".js");
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(temp), StandardCharsets.UTF_8))) {
                    writer.write(code);
                }

                // Debug lệnh chạy
                System.out.println("Running command: " + interpreter + " " + temp.getAbsolutePath());
                System.out.println("TestCase Input JSON: " + tc.getInput());

                ProcessBuilder pb = new ProcessBuilder(interpreter, temp.getAbsolutePath());
                pb.redirectErrorStream(true);
                Process process = pb.start();

                // Gửi input JSON đến Python/JS
                if (tc.getInput() != null && !tc.getInput().trim().isEmpty()) {
                    Map<String, Object> jsonMap = new HashMap<>();
                    jsonMap.put("input", mapper.readValue(tc.getInput(), Object.class)); // input có thể list, dict, số
                    jsonMap.put("expected", tc.getExpectedOutput() != null ? mapper.readValue(tc.getExpectedOutput(), Object.class) : null);
                    String jsonInput = mapper.writeValueAsString(jsonMap);

                    try (BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                        writer.write(jsonInput);
                        writer.newLine();
                        writer.flush();
                        writer.close(); // ⚡ Đóng stream để Python/JS nhận EOF
                    }
                } else {
                    process.getOutputStream().close();
                }

                // Đọc output
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) output.append(line).append("\n");
                process.waitFor();

                String actualRaw = output.toString().trim();
                String expectedRaw = tc.getExpectedOutput() != null ? tc.getExpectedOutput().trim() : "";

                // Debug
                System.out.println("Expected Output JSON: " + expectedRaw);
                System.out.println("Actual Output JSON:   " + actualRaw);

                // Parse JSON để so sánh, bỏ qua dấu cách
                boolean pass;
                try {
                    Object actualObj = mapper.readValue(actualRaw, Object.class);
                    Object expectedObj = mapper.readValue(expectedRaw, Object.class);
                    pass = actualObj.equals(expectedObj);
                } catch (Exception e) {
                    // fallback nếu không parse được JSON
                    pass = actualRaw.equals(expectedRaw);
                }

                finalOutput.append("TestCase ").append(tc.getId())
                        .append(": ").append(pass ? "PASS" : "FAIL")
                        .append("\nExpected: ").append(expectedRaw)
                        .append("\nActual: ").append(actualRaw)
                        .append("\n\n");
            }

            callback.onResult(finalOutput.toString());

        } catch (Exception e) {
            try {
                callback.onError(e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            if (temp != null && temp.exists()) temp.delete();
        }
    }
}
