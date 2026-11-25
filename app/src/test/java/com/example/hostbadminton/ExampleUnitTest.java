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
    public void updateMembersTabAndSearch() throws Exception {
        String resDir = "src/main/res";
        if (!new File(resDir).exists()) resDir = "app/src/main/res";
        String layoutPath = resDir + "/layout/layout_tab_members.xml";

        if (new File(layoutPath).exists()) {
            String xml = new String(Files.readAllBytes(Paths.get(layoutPath)), StandardCharsets.UTF_8);
            xml = xml.replace("\r\n", "\n");

            String searchBar = "<EditText\n" +
                    "            android:id=\"@+id/etSearchPlayer\"\n" +
                    "            android:layout_width=\"match_parent\"\n" +
                    "            android:layout_height=\"50dp\"\n" +
                    "            android:layout_marginBottom=\"16dp\"\n" +
                    "            android:background=\"@drawable/bg_input_dark\"\n" +
                    "            android:drawableStart=\"@android:drawable/ic_menu_search\"\n" +
                    "            android:drawablePadding=\"12dp\"\n" +
                    "            android:hint=\"Tìm tên cầu thủ...\"\n" +
                    "            android:padding=\"12dp\"\n" +
                    "            android:textColor=\"@color/text_white\"\n" +
                    "            android:textColorHint=\"@color/text_gray\"\n" +
                    "            android:textSize=\"14sp\" />\n";

            String headerTarget = "<LinearLayout android:layout_width=\"match_parent\" android:layout_height=\"wrap_content\" android:orientation=\"horizontal\" android:gravity=\"center_vertical\" android:layout_marginBottom=\"16dp\">";
            if (xml.contains(headerTarget) && !xml.contains("etSearchPlayer")) {
                xml = xml.replace(headerTarget, headerTarget + "\n        " + searchBar);
                Files.write(Paths.get(layoutPath), xml.getBytes(StandardCharsets.UTF_8));
            }
        }

        String javaPath = "src/main/java/com/example/hostbadminton/MainActivity.java";
        if (!new File(javaPath).exists()) javaPath = "app/src/main/java/com/example/hostbadminton/MainActivity.java";

        if (new File(javaPath).exists()) {
            String jContent = new String(Files.readAllBytes(Paths.get(javaPath)), StandardCharsets.UTF_8);
            jContent = jContent.replace("\r\n", "\n");

            if (!jContent.contains("private EditText etSearchPlayer;")) {
                jContent = jContent.replace("private EditText etCourtFee,", "private EditText etSearchPlayer;\n    private EditText etCourtFee,");
            }

            if (!jContent.contains("etSearchPlayer = findViewById")) {
                String targetInit = "btnAddPlayer = findViewById(R.id.btnAddPlayer);";
                String initCode = targetInit + "\n        etSearchPlayer = findViewById(R.id.etSearchPlayer);\n" +
                        "        if (etSearchPlayer != null) {\n" +
                        "            etSearchPlayer.addTextChangedListener(new android.text.TextWatcher() {\n" +
                        "                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}\n" +
                        "                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {\n" +
                        "                    updatePlayerListUI();\n" +
                        "                }\n" +
                        "                @Override public void afterTextChanged(android.text.Editable s) {}\n" +
                        "            });\n" +
                        "        }";
                if (jContent.contains(targetInit)) {
                    jContent = jContent.replace(targetInit, initCode);
                }
            }

            if (!jContent.contains("private String getInitials")) {
                String initialsMethod = "\n    private String getInitials(String name) {\n" +
                        "        if (name == null || name.trim().isEmpty()) return \"PL\";\n" +
                        "        String[] parts = name.trim().split(\"\\\\s+\");\n" +
                        "        if (parts.length >= 2) {\n" +
                        "            String first = parts[0].substring(0, 1);\n" +
                        "            String last = parts[parts.length - 1].substring(0, 1);\n" +
                        "            return (first + last).toUpperCase();\n" +
                        "        } else if (parts[0].length() >= 2) {\n" +
                        "            return parts[0].substring(0, 2).toUpperCase();\n" +
                        "        }\n" +
                        "        return parts[0].toUpperCase();\n" +
                        "    }\n";
                int lastBrace = jContent.lastIndexOf('}');
                if (lastBrace != -1) {
                    jContent = jContent.substring(0, lastBrace) + initialsMethod + "\n}\n";
                }
            }

            int startUI = jContent.indexOf("private void updatePlayerListUI() {");
            int endUI = -1;
            if (startUI != -1) {
                endUI = jContent.indexOf("private void showAvoidPlayerDialog", startUI);
            }
            if (startUI != -1 && endUI != -1) {
                String newUI = "private void updatePlayerListUI() {\n" +
                        "        if (layoutPlayerListContainer == null) return;\n" +
                        "        layoutPlayerListContainer.removeAllViews();\n\n" +
                        "        String keyword = etSearchPlayer != null ? etSearchPlayer.getText().toString().toLowerCase().trim() : \"\";\n\n" +
                        "        for (int i = 0; i < playerList.size(); i++) {\n" +
                        "            Player player = playerList.get(i);\n" +
                        "            int position = i;\n\n" +
                        "            if (!keyword.isEmpty() && !player.getName().toLowerCase().contains(keyword)) {\n" +
                        "                continue;\n" +
                        "            }\n\n" +
                        "            View view = getLayoutInflater().inflate(R.layout.item_player, layoutPlayerListContainer, false);\n" +
                        "            TextView tvInfo = view.findViewById(R.id.tvPlayerInfo);\n" +
                        "            TextView tvCount = view.findViewById(R.id.tvMatchCount);\n" +
                        "            TextView tvInitials = view.findViewById(R.id.tvPlayerInitials);\n" +
                        "            android.widget.Switch swStatus = view.findViewById(R.id.swPlayerStatus);\n\n" +
                        "            if (player != null) {\n" +
                        "                if (tvInitials != null) {\n" +
                        "                    tvInitials.setText(getInitials(player.getName()));\n" +
                        "                }\n" +
                        "                if (swStatus != null) {\n" +
                        "                    swStatus.setOnCheckedChangeListener(null);\n" +
                        "                    swStatus.setChecked(!player.isResting());\n" +
                        "                    swStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {\n" +
                        "                        player.setResting(!isChecked);\n" +
                        "                        savePlayersToPrefs();\n" +
                        "                        updatePlayerListUI();\n" +
                        "                    });\n" +
                        "                }\n" +
                        "                String statusText = player.isResting() ? \" [ĐANG NGHỈ]\" : \"\";\n" +
                        "                String prefText = \"\";\n" +
                        "                if (player.getPreferredTeammateId() != null) {\n" +
                        "                    Player partner = getPlayerById(player.getPreferredTeammateId());\n" +
                        "                    if (partner != null) {\n" +
                        "                        prefText = \" [Cặp: \" + partner.getName() + \"]\";\n" +
                        "                    }\n" +
                        "                }\n" +
                        "                String avoidText = \"\";\n" +
                        "                if (player.getAvoidPlayerId() != null) {\n" +
                        "                    Player avoidP = getPlayerById(player.getAvoidPlayerId());\n" +
                        "                    if (avoidP != null) {\n" +
                        "                        avoidText = \" [Né: \" + avoidP.getName() + \"]\";\n" +
                        "                    }\n" +
                        "                }\n" +
                        "                String styleText = player.getPlayStyle() != PlayStyle.BALANCED ? \" [\" + player.getPlayStyle().getDisplayName() + \"]\" : \"\";\n" +
                        "                String discountText = player.getDiscountAmount() > 0 ? \" [Giảm \" + formatMoney(player.getDiscountAmount()) + \" VNĐ]\" : \"\";\n" +
                        "                int stt = position + 1;\n" +
                        "                tvInfo.setText(stt + \". \" + player.getName() + \" (\" + player.getGender().getDisplayName() + \" - \" + player.getSkill().getDisplayName() + \")\" + prefText + avoidText + styleText + discountText + statusText);\n" +
                        "                tvCount.setText(player.getMatchesPlayed() + \"/6 set\");\n" +
                        "            }\n\n" +
                        "            view.setOnClickListener(v -> showPlayerOptionsDialog(player, position));\n" +
                        "            layoutPlayerListContainer.addView(view);\n" +
                        "        }\n" +
                        "    }\n\n    ";

                jContent = jContent.substring(0, startUI) + newUI + jContent.substring(endUI);
            }

            Files.write(Paths.get(javaPath), jContent.getBytes(StandardCharsets.UTF_8));
        }
    }
}
