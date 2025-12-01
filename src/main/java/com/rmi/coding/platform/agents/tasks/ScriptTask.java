package com.rmi.coding.platform.agents.tasks;

import com.rmi.coding.platform.common.AgentCallback;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.io.Serializable;

public record ScriptTask(String code, String language) implements Serializable {

    public ScriptTask(String code, String language) {
        this.code = code;
        this.language = language.toLowerCase();
    }

    public void run(AgentCallback callback) {
        File temp = null;
        try {
            String interpreter;
            switch (language) {
                case "python":
                    interpreter = "python";
                    break;
                case "javascript":
                    interpreter = "node";
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported language: " + language);
            }

            temp = File.createTempFile("script_", language.equals("python") ? ".py" : ".js");
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(temp), StandardCharsets.UTF_8))) {
                writer.write(code);
            }

            ProcessBuilder pb = new ProcessBuilder(interpreter, temp.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();

            callback.onResult(output.toString());

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
