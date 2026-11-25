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
    public void fixManualMatchDirect() throws Exception {
        File f = new File("S:/Host_Badminton-main/Host_Badminton-main/app/src/main/java/com/example/hostbadminton/MainActivity.java");
        if (!f.exists()) f = new File("app/src/main/java/com/example/hostbadminton/MainActivity.java");

        if (f.exists()) {
            String content = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            content = content.replace("\r\n", "\n");

            int idx = content.indexOf("displayMatchesOnCourts(matches, availablePlayers.size(), restingCount);");
            if (idx != -1) {
                content = content.substring(0, idx) + "displaySingleMatchOnCourt(match, p1, p2, p3, p4);" + content.substring(idx + "displayMatchesOnCourts(matches, availablePlayers.size(), restingCount);".length());
                Files.write(f.toPath(), content.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
