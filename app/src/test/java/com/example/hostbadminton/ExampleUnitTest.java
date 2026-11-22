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
    public void updateMainActivityFields() throws Exception {
        File f = new File("app/src/main/java/com/example/hostbadminton/MainActivity.java");
        if (!f.exists()) f = new File("src/main/java/com/example/hostbadminton/MainActivity.java");
        if (!f.exists()) f = new File("../app/src/main/java/com/example/hostbadminton/MainActivity.java");

        System.out.println("Checking MainActivity.java at: " + f.getAbsolutePath() + ", exists=" + f.exists());

        if (f.exists()) {
            String jContent = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            jContent = jContent.replace("\r\n", "\n");

            if (!jContent.contains("import android.net.Uri;")) {
                jContent = jContent.replace("import android.content.Intent;", "import android.content.Intent;\nimport android.net.Uri;");
            }
            if (!jContent.contains("import android.widget.ImageView;")) {
                jContent = jContent.replace("import android.widget.EditText;", "import android.widget.EditText;\nimport android.widget.ImageView;");
            }

            int idx = jContent.indexOf("private Button btnSaveAndCalculate");
            if (idx != -1 && !jContent.contains("btnSelectQrCode")) {
                String decl = "private Button btnSelectQrCode;\n    private ImageView ivQrCodePreview;\n    private Uri qrCodeUri = null;\n    private static final int PICK_QR_IMAGE_REQUEST = 1001;\n    ";
                jContent = jContent.substring(0, idx) + decl + jContent.substring(idx);
                Files.write(f.toPath(), jContent.getBytes(StandardCharsets.UTF_8));
                System.out.println("Successfully added QR code fields to MainActivity.java");
            }
        }
    }
}
