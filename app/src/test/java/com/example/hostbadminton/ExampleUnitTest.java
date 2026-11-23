package com.example.hostbadminton;

import org.junit.Test;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void removeBOMFromLoginActivityFinal() throws Exception {
        String javaPath = "src/main/java/com/example/hostbadminton/LoginActivity.java";
        if (!new File(javaPath).exists()) javaPath = "app/src/main/java/com/example/hostbadminton/LoginActivity.java";

        if (new File(javaPath).exists()) {
            byte[] bytes = Files.readAllBytes(Paths.get(javaPath));
            // Ensure no BOM
            if (bytes.length >= 3 && bytes[0] == (byte)0xEF && bytes[1] == (byte)0xBB && bytes[2] == (byte)0xBF) {
                byte[] newBytes = new byte[bytes.length - 3];
                System.arraycopy(bytes, 3, newBytes, 0, newBytes.length);
                Files.write(Paths.get(javaPath), newBytes);
            }
            String content = new String(Files.readAllBytes(Paths.get(javaPath)), StandardCharsets.UTF_8);
            content = content.replace("\uFEFF", "");
            Files.write(Paths.get(javaPath), content.getBytes(StandardCharsets.UTF_8));
        }
    }
}
