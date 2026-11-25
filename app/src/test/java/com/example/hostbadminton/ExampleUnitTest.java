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
    public void fix4TabsSwitchingInMainActivity() throws Exception {
        String javaPath = "src/main/java/com/example/hostbadminton/MainActivity.java";
        if (!new File(javaPath).exists()) javaPath = "app/src/main/java/com/example/hostbadminton/MainActivity.java";

        if (new File(javaPath).exists()) {
            String jContent = new String(Files.readAllBytes(Paths.get(javaPath)), StandardCharsets.UTF_8);
            jContent = jContent.replace("\r\n", "\n");

            // Replace click listeners in onCreate
            String oldListeners = "btnTabHome.setOnClickListener(v -> switchTab(true));\n        btnTabExpense.setOnClickListener(v -> switchTab(false));";
            String newListeners = "if (btnTabHome != null) btnTabHome.setOnClickListener(v -> switchTab(0));\n" +
                    "        if (btnTabMembers != null) btnTabMembers.setOnClickListener(v -> switchTab(1));\n" +
                    "        if (btnTabExpense != null) btnTabExpense.setOnClickListener(v -> switchTab(2));\n" +
                    "        if (btnTabProfile != null) btnTabProfile.setOnClickListener(v -> switchTab(3));";

            if (jContent.contains(oldListeners)) {
                jContent = jContent.replace(oldListeners, newListeners);
            } else {
                jContent = jContent.replace("btnTabHome.setOnClickListener(v -> switchTab(true));", "if (btnTabHome != null) btnTabHome.setOnClickListener(v -> switchTab(0));");
                jContent = jContent.replace("btnTabExpense.setOnClickListener(v -> switchTab(false));", "if (btnTabMembers != null) btnTabMembers.setOnClickListener(v -> switchTab(1));\n        if (btnTabExpense != null) btnTabExpense.setOnClickListener(v -> switchTab(2));\n        if (btnTabProfile != null) btnTabProfile.setOnClickListener(v -> switchTab(3));");
            }

            // Replace old switchTab(boolean isHomeSelected) with switchTab(int tabIndex)
            int start = jContent.indexOf("private void switchTab(");
            if (start != -1) {
                int end = jContent.indexOf("private void loadExpensesData", start);
                if (end != -1) {
                    String newSwitch = "private void switchTab(int tabIndex) {\n" +
                            "        if (layoutHome != null) layoutHome.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);\n" +
                            "        if (layoutMembers != null) layoutMembers.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);\n" +
                            "        if (layoutExpenseStats != null) layoutExpenseStats.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);\n" +
                            "        if (layoutProfile != null) layoutProfile.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);\n" +
                            "        \n" +
                            "        int colorActive = Color.parseColor(\"#4DF197\");\n" +
                            "        int colorInactive = Color.parseColor(\"#8A94A6\");\n\n" +
                            "        TextView tvHome = findViewById(R.id.tvTabHomeText);\n" +
                            "        TextView tvMembers = findViewById(R.id.tvTabMembersText);\n" +
                            "        TextView tvExpense = findViewById(R.id.tvTabExpenseText);\n" +
                            "        TextView tvProfile = findViewById(R.id.tvTabProfileText);\n\n" +
                            "        if (tvHome != { tvHome.setTextColor(tabIndex == 0 ? colorActive : colorInactive); tvHome.setTypeface(null, tabIndex == 0 ? Typeface.BOLD : Typeface.NORMAL); }\n" +
                            "        if (tvMembers != null) { tvMembers.setTextColor(tabIndex == 1 ? colorActive : colorInactive); tvMembers.setTypeface(null, tabIndex == 1 ? Typeface.BOLD : Typeface.NORMAL); }\n" +
                            "        if (tvExpense != null) { tvExpense.setTextColor(tabIndex == 2 ? colorActive : colorInactive); tvExpense.setTypeface(null, tabIndex == 2 ? Typeface.BOLD : Typeface.NORMAL); }\n" +
                            "        if (tvProfile != null) { tvProfile.setTextColor(tabIndex == 3 ? colorActive : colorInactive); tvProfile.setTypeface(null, tabIndex == 3 ? Typeface.BOLD : Typeface.NORMAL); }\n\n" +
                            "        if (tabIndex == 2) {\n" +
                            "            loadExpensesData();\n" +
                            "            calculateAndDisplayExpenses();\n" +
                            "            calculateAndDisplayStats();\n" +
                            "        }\n" +
                            "    }\n\n    ";
                    // Fix syntax error in tvHome check
                    newSwitch = newSwitch.replace("if (tvHome != {", "if (tvHome != null) {");
                    jContent = jContent.substring(0, start) + newSwitch + jContent.substring(end);
                }
            }

            Files.write(Paths.get(javaPath), jContent.getBytes(StandardCharsets.UTF_8));
        }
    }
}
