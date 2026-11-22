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
    public void fixMessengerShareTextDrop() throws Exception {
        String javaPath = "src/main/java/com/example/hostbadminton/MainActivity.java";
        if (!new File(javaPath).exists()) javaPath = "app/src/main/java/com/example/hostbadminton/MainActivity.java";

        if (new File(javaPath).exists()) {
            String content = new String(Files.readAllBytes(Paths.get(javaPath)), StandardCharsets.UTF_8);
            content = content.replace("\r\n", "\n");

            String oldCode = "                                        // ALWAYS auto copy to clipboard just in case the target app ignores EXTRA_TEXT\n" +
                    "                                        ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);\n" +
                    "                                        ClipData cd = ClipData.newPlainText(\"Báo Cáo\", reportText);\n" +
                    "                                        if (cb != null) {\n" +
                    "                                            cb.setPrimaryClip(cd);\n" +
                    "                                        }\n" +
                    "                                        \n" +
                    "                                        Toast.makeText(MainActivity.this, \"Đã tự động sao chép văn bản. Vui lòng DÁN (Paste) để gửi kèm ảnh!\", Toast.LENGTH_LONG).show();";

            String newCode = "                                        // ĐẶC TRỊ MESSENGER & ZALO: \n" +
                    "                                        // Cố tình đẩy thẳng text vào clipboard, không để app tự quyết định\n" +
                    "                                        ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);\n" +
                    "                                        ClipData cd = ClipData.newPlainText(\"Báo Cáo\", reportText);\n" +
                    "                                        if (cb != null) {\n" +
                    "                                            cb.setPrimaryClip(cd);\n" +
                    "                                        }\n" +
                    "                                        \n" +
                    "                                        Toast.makeText(MainActivity.this, \"HÃY BẤM DÁN (PASTE) CHỮ VÀO TIN NHẮN!\", Toast.LENGTH_LONG).show();";

            if (content.contains(oldCode)) {
                content = content.replace(oldCode, newCode);
                Files.write(Paths.get(javaPath), content.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
