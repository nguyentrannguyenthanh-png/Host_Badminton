package com.example.hostbadminton;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "HostBadminton.db";
    private static final int DATABASE_VERSION = 7;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_PLAYERS = "players";
    public static final String TABLE_HISTORY = "match_history";
    public static final String TABLE_EXPENSES = "expenses";
    public static final String TABLE_SESSION_REPORTS = "session_reports";
    public static final String TABLE_SHUTTLE_INVENTORY = "shuttle_inventory";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                "username TEXT PRIMARY KEY, " +
                "password TEXT, " +
                "phone TEXT)";

        String CREATE_PLAYERS_TABLE = "CREATE TABLE " + TABLE_PLAYERS + " (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "skill TEXT, " +
                "gender TEXT, " +
                "matchesPlayed INTEGER, " +
                "isResting INTEGER, " +
                "preferredTeammateId TEXT, " +
                "avoidPlayerId TEXT, " +
                "discountAmount REAL DEFAULT 0, " +
                "playStyle TEXT DEFAULT 'BALANCED')";

        String CREATE_HISTORY_TABLE = "CREATE TABLE " + TABLE_HISTORY + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "roundNumber INTEGER, " +
                "courtNumber INTEGER, " +
                "p1_id TEXT, p1_name TEXT, p1_skill TEXT, p1_gender TEXT, " +
                "p2_id TEXT, p2_name TEXT, p2_skill TEXT, p2_gender TEXT, " +
                "p3_id TEXT, p3_name TEXT, p3_skill TEXT, p3_gender TEXT, " +
                "p4_id TEXT, p4_name TEXT, p4_skill TEXT, p4_gender TEXT, " +
                "skillDiff REAL, " +
                "score1 INTEGER, " +
                "score2 INTEGER)";

        String CREATE_EXPENSES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_EXPENSES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "courtFee REAL, " +
                "shuttleFee REAL, " +
                "otherFee REAL, " +
                "note TEXT, " +
                "isFixedFeeMode INTEGER DEFAULT 0, " +
                "fixedFeePerPlayer REAL DEFAULT 0, " +
                "isSeparateGenderFee INTEGER DEFAULT 0, " +
                "maleFee REAL DEFAULT 0, " +
                "femaleFee REAL DEFAULT 0, " +
                "shuttlesUsedCount INTEGER DEFAULT 0, " +
                "useInventoryShuttle INTEGER DEFAULT 0)";

        String CREATE_REPORTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SESSION_REPORTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "dateStr TEXT, " +
                "monthStr TEXT, " +
                "yearStr TEXT, " +
                "totalExpense REAL, " +
                "totalCollected REAL, " +
                "profitOrLoss REAL, " +
                "playerCount INTEGER, " +
                "maleCount INTEGER, " +
                "femaleCount INTEGER)";

        String CREATE_SHUTTLE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SHUTTLE_INVENTORY + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "brandName TEXT, " +
                "tubeCount INTEGER, " +
                "shuttlesPerTube INTEGER, " +
                "totalPrice REAL, " +
                "pricePerShuttle REAL, " +
                "remainingShuttles INTEGER)";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_PLAYERS_TABLE);
        db.execSQL(CREATE_HISTORY_TABLE);
        db.execSQL(CREATE_EXPENSES_TABLE);
        db.execSQL(CREATE_REPORTS_TABLE);
        db.execSQL(CREATE_SHUTTLE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 7) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_PLAYERS + " ADD COLUMN avoidPlayerId TEXT");
                db.execSQL("ALTER TABLE " + TABLE_PLAYERS + " ADD COLUMN playStyle TEXT DEFAULT 'BALANCED'");
            } catch (Exception ignored) {}
        }
    }

    // --- USER METHODS ---
    public boolean registerUser(String username, String password, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        values.put("phone", phone);
        long result = db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{"username"},
                "username = ? AND password = ?", new String[]{username, password},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean isUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{"username"},
                "username = ?", new String[]{username},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public void updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);
        db.update(TABLE_USERS, values, "username = ?", new String[]{username});
    }

    public boolean checkUserPhone(String username, String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{"username"},
                "username = ? AND phone = ?", new String[]{username, phone},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // --- PLAYER METHODS ---
    public void savePlayers(List<Player> players) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYERS, null, null);
        for (Player p : players) {
            ContentValues values = new ContentValues();
            values.put("id", p.getId());
            values.put("name", p.getName());
            values.put("skill", p.getSkill().name());
            values.put("gender", p.getGender().name());
            values.put("matchesPlayed", p.getMatchesPlayed());
            values.put("isResting", p.isResting() ? 1 : 0);
            values.put("preferredTeammateId", p.getPreferredTeammateId() != null ? p.getPreferredTeammateId() : "");
            values.put("avoidPlayerId", p.getAvoidPlayerId() != null ? p.getAvoidPlayerId() : "");
            values.put("discountAmount", p.getDiscountAmount());
            values.put("playStyle", p.getPlayStyle() != null ? p.getPlayStyle().name() : PlayStyle.BALANCED.name());
            db.insert(TABLE_PLAYERS, null, values);
        }
    }

    public List<Player> loadPlayers() {
        List<Player> players = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLAYERS, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                PlayerSkill skill = PlayerSkill.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("skill")));
                Gender gender = Gender.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                int matchesPlayed = cursor.getInt(cursor.getColumnIndexOrThrow("matchesPlayed"));
                boolean isResting = cursor.getInt(cursor.getColumnIndexOrThrow("isResting")) == 1;
                String prefId = cursor.getString(cursor.getColumnIndexOrThrow("preferredTeammateId"));

                int avoidIdx = cursor.getColumnIndex("avoidPlayerId");
                int discountIdx = cursor.getColumnIndex("discountAmount");
                int styleIdx = cursor.getColumnIndex("playStyle");

                String avoidId = (avoidIdx != -1 && !cursor.isNull(avoidIdx)) ? cursor.getString(avoidIdx) : "";
                double discountAmount = (discountIdx != -1 && !cursor.isNull(discountIdx)) ? cursor.getDouble(discountIdx) : 0;
                String styleStr = (styleIdx != -1 && !cursor.isNull(styleIdx)) ? cursor.getString(styleIdx) : PlayStyle.BALANCED.name();

                Player p = new Player(id, name, skill, gender);
                p.setMatchesPlayed(matchesPlayed);
                p.setResting(isResting);
                if (prefId != null && !prefId.isEmpty()) p.setPreferredTeammateId(prefId);
                if (avoidId != null && !avoidId.isEmpty()) p.setAvoidPlayerId(avoidId);
                p.setDiscountAmount(discountAmount);

                try {
                    p.setPlayStyle(PlayStyle.valueOf(styleStr));
                } catch (Exception e) {
                    p.setPlayStyle(PlayStyle.BALANCED);
                }

                players.add(p);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return players;
    }

    // --- HISTORY METHODS ---
    public void saveHistory(List<HistoryManager.SetRecord> records) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, null, null);
        for (HistoryManager.SetRecord rec : records) {
            ContentValues values = new ContentValues();
            values.put("roundNumber", rec.getRoundNumber());
            values.put("courtNumber", rec.getCourtNumber());

            values.put("p1_id", rec.getP1().getId());
            values.put("p1_name", rec.getP1().getName());
            values.put("p1_skill", rec.getP1().getSkill().name());
            values.put("p1_gender", rec.getP1().getGender().name());

            values.put("p2_id", rec.getP2().getId());
            values.put("p2_name", rec.getP2().getName());
            values.put("p2_skill", rec.getP2().getSkill().name());
            values.put("p2_gender", rec.getP2().getGender().name());

            values.put("p3_id", rec.getP3().getId());
            values.put("p3_name", rec.getP3().getName());
            values.put("p3_skill", rec.getP3().getSkill().name());
            values.put("p3_gender", rec.getP3().getGender().name());

            values.put("p4_id", rec.getP4().getId());
            values.put("p4_name", rec.getP4().getName());
            values.put("p4_skill", rec.getP4().getSkill().name());
            values.put("p4_gender", rec.getP4().getGender().name());

            values.put("skillDiff", rec.getSkillDiff());
            if (rec.getScore1() != null) values.put("score1", rec.getScore1());
            if (rec.getScore2() != null) values.put("score2", rec.getScore2());

            db.insert(TABLE_HISTORY, null, values);
        }
    }

    public List<HistoryManager.SetRecord> loadHistory(List<Player> allPlayers) {
        List<HistoryManager.SetRecord> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HISTORY, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int roundNum = cursor.getInt(cursor.getColumnIndexOrThrow("roundNumber"));
                int courtNum = cursor.getInt(cursor.getColumnIndexOrThrow("courtNumber"));
                double skillDiff = cursor.getDouble(cursor.getColumnIndexOrThrow("skillDiff"));

                Player p1 = getOrCreatePlayer(allPlayers, cursor.getString(cursor.getColumnIndexOrThrow("p1_id")), cursor.getString(cursor.getColumnIndexOrThrow("p1_name")), cursor.getString(cursor.getColumnIndexOrThrow("p1_skill")), cursor.getString(cursor.getColumnIndexOrThrow("p1_gender")));
                Player p2 = getOrCreatePlayer(allPlayers, cursor.getString(cursor.getColumnIndexOrThrow("p2_id")), cursor.getString(cursor.getColumnIndexOrThrow("p2_name")), cursor.getString(cursor.getColumnIndexOrThrow("p2_skill")), cursor.getString(cursor.getColumnIndexOrThrow("p2_gender")));
                Player p3 = getOrCreatePlayer(allPlayers, cursor.getString(cursor.getColumnIndexOrThrow("p3_id")), cursor.getString(cursor.getColumnIndexOrThrow("p3_name")), cursor.getString(cursor.getColumnIndexOrThrow("p3_skill")), cursor.getString(cursor.getColumnIndexOrThrow("p3_gender")));
                Player p4 = getOrCreatePlayer(allPlayers, cursor.getString(cursor.getColumnIndexOrThrow("p4_id")), cursor.getString(cursor.getColumnIndexOrThrow("p4_name")), cursor.getString(cursor.getColumnIndexOrThrow("p4_skill")), cursor.getString(cursor.getColumnIndexOrThrow("p4_gender")));

                HistoryManager.SetRecord rec = new HistoryManager.SetRecord(roundNum, courtNum, p1, p2, p3, p4, skillDiff);

                int s1Index = cursor.getColumnIndex("score1");
                int s2Index = cursor.getColumnIndex("score2");
                if (!cursor.isNull(s1Index) && !cursor.isNull(s2Index)) {
                    rec.setScore(cursor.getInt(s1Index), cursor.getInt(s2Index));
                }

                records.add(rec);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return records;
    }

    private Player getOrCreatePlayer(List<Player> allPlayers, String id, String name, String skillStr, String genderStr) {
        for (Player p : allPlayers) {
            if (p.getId().equals(id)) return p;
        }
        return new Player(id, name, PlayerSkill.valueOf(skillStr), Gender.valueOf(genderStr));
    }

    // --- EXPENSE METHODS ---
    public void saveExpense(double courtFee, double shuttleFee, double otherFee, String note) {
        saveExpense(courtFee, shuttleFee, otherFee, note, false, 0, false, 0, 0, 0, false);
    }

    public void saveExpense(double courtFee, double shuttleFee, double otherFee, String note,
                            boolean isFixedFeeMode, double fixedFeePerPlayer,
                            boolean isSeparateGenderFee, double maleFee, double femaleFee) {
        saveExpense(courtFee, shuttleFee, otherFee, note, isFixedFeeMode, fixedFeePerPlayer, isSeparateGenderFee, maleFee, femaleFee, 0, false);
    }

    public void saveExpense(double courtFee, double shuttleFee, double otherFee, String note,
                            boolean isFixedFeeMode, double fixedFeePerPlayer,
                            boolean isSeparateGenderFee, double maleFee, double femaleFee,
                            int shuttlesUsedCount, boolean useInventoryShuttle) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPENSES, null, null);
        ContentValues values = new ContentValues();
        values.put("courtFee", courtFee);
        values.put("shuttleFee", shuttleFee);
        values.put("otherFee", otherFee);
        values.put("note", note != null ? note : "");
        values.put("isFixedFeeMode", isFixedFeeMode ? 1 : 0);
        values.put("fixedFeePerPlayer", fixedFeePerPlayer);
        values.put("isSeparateGenderFee", isSeparateGenderFee ? 1 : 0);
        values.put("maleFee", maleFee);
        values.put("femaleFee", femaleFee);
        values.put("shuttlesUsedCount", shuttlesUsedCount);
        values.put("useInventoryShuttle", useInventoryShuttle ? 1 : 0);
        db.insert(TABLE_EXPENSES, null, values);
    }

    public ExpenseData loadExpense() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXPENSES, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            double courtFee = cursor.getDouble(cursor.getColumnIndexOrThrow("courtFee"));
            double shuttleFee = cursor.getDouble(cursor.getColumnIndexOrThrow("shuttleFee"));
            double otherFee = cursor.getDouble(cursor.getColumnIndexOrThrow("otherFee"));
            String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));

            int isFixedIdx = cursor.getColumnIndex("isFixedFeeMode");
            int fixedFeeIdx = cursor.getColumnIndex("fixedFeePerPlayer");
            int isSepGenderIdx = cursor.getColumnIndex("isSeparateGenderFee");
            int maleFeeIdx = cursor.getColumnIndex("maleFee");
            int femaleFeeIdx = cursor.getColumnIndex("femaleFee");
            int shuttlesUsedIdx = cursor.getColumnIndex("shuttlesUsedCount");
            int useInvShuttleIdx = cursor.getColumnIndex("useInventoryShuttle");

            boolean isFixedFeeMode = (isFixedIdx != -1 && !cursor.isNull(isFixedIdx)) ? cursor.getInt(isFixedIdx) == 1 : false;
            double fixedFeePerPlayer = (fixedFeeIdx != -1 && !cursor.isNull(fixedFeeIdx)) ? cursor.getDouble(fixedFeeIdx) : 0;
            boolean isSeparateGenderFee = (isSepGenderIdx != -1 && !cursor.isNull(isSepGenderIdx)) ? cursor.getInt(isSepGenderIdx) == 1 : false;
            double maleFee = (maleFeeIdx != -1 && !cursor.isNull(maleFeeIdx)) ? cursor.getDouble(maleFeeIdx) : 0;
            double femaleFee = (femaleFeeIdx != -1 && !cursor.isNull(femaleFeeIdx)) ? cursor.getDouble(femaleFeeIdx) : 0;

            int shuttlesUsedCount = (shuttlesUsedIdx != -1 && !cursor.isNull(shuttlesUsedIdx)) ? cursor.getInt(shuttlesUsedIdx) : 0;
            boolean useInventoryShuttle = (useInvShuttleIdx != -1 && !cursor.isNull(useInvShuttleIdx)) ? cursor.getInt(useInvShuttleIdx) == 1 : false;

            cursor.close();
            return new ExpenseData(courtFee, shuttleFee, otherFee, note, isFixedFeeMode, fixedFeePerPlayer, isSeparateGenderFee, maleFee, femaleFee, shuttlesUsedCount, useInventoryShuttle);
        }
        if (cursor != null) cursor.close();
        return new ExpenseData(0, 0, 0, "", false, 0, false, 0, 0, 0, false);
    }

    // --- SHUTTLE INVENTORY CRUD METHODS ---
    public void addShuttleStock(String brandName, int tubeCount, int shuttlesPerTube, double totalPrice) {
        SQLiteDatabase db = this.getWritableDatabase();
        int totalShuttles = tubeCount * shuttlesPerTube;
        double pricePerShuttle = totalShuttles > 0 ? (totalPrice / totalShuttles) : 0;

        ContentValues values = new ContentValues();
        values.put("brandName", brandName != null && !brandName.isEmpty() ? brandName : "Cầu Mới");
        values.put("tubeCount", tubeCount);
        values.put("shuttlesPerTube", shuttlesPerTube);
        values.put("totalPrice", totalPrice);
        values.put("pricePerShuttle", pricePerShuttle);
        values.put("remainingShuttles", totalShuttles);
        db.insert(TABLE_SHUTTLE_INVENTORY, null, values);
    }

    public void updateShuttleStock(int id, String brandName, int tubeCount, int shuttlesPerTube, double totalPrice, int remainingShuttles) {
        SQLiteDatabase db = this.getWritableDatabase();
        int totalShuttles = tubeCount * shuttlesPerTube;
        double pricePerShuttle = totalShuttles > 0 ? (totalPrice / totalShuttles) : 0;

        ContentValues values = new ContentValues();
        values.put("brandName", brandName);
        values.put("tubeCount", tubeCount);
        values.put("shuttlesPerTube", shuttlesPerTube);
        values.put("totalPrice", totalPrice);
        values.put("pricePerShuttle", pricePerShuttle);
        values.put("remainingShuttles", remainingShuttles);
        db.update(TABLE_SHUTTLE_INVENTORY, values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteShuttleStock(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SHUTTLE_INVENTORY, "id = ?", new String[]{String.valueOf(id)});
    }

    public List<ShuttleStockData> getAllShuttleStocks() {
        List<ShuttleStockData> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SHUTTLE_INVENTORY, null, null, null, null, null, "id DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String brand = cursor.getString(cursor.getColumnIndexOrThrow("brandName"));
                int tubeCount = cursor.getInt(cursor.getColumnIndexOrThrow("tubeCount"));
                int perTube = cursor.getInt(cursor.getColumnIndexOrThrow("shuttlesPerTube"));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow("totalPrice"));
                double perShuttle = cursor.getDouble(cursor.getColumnIndexOrThrow("pricePerShuttle"));
                int remaining = cursor.getInt(cursor.getColumnIndexOrThrow("remainingShuttles"));

                list.add(new ShuttleStockData(id, brand, tubeCount, perTube, total, perShuttle, remaining));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public ShuttleStockData loadShuttleStock() {
        List<ShuttleStockData> list = getAllShuttleStocks();
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return new ShuttleStockData(-1, "Chưa nhập kho", 0, 12, 0, 0, 0);
    }

    public void deductShuttleStock(int count) {
        ShuttleStockData stock = loadShuttleStock();
        if (stock != null && stock.id != -1 && stock.remainingShuttles > 0) {
            int newRemaining = Math.max(0, stock.remainingShuttles - count);
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("remainingShuttles", newRemaining);
            db.update(TABLE_SHUTTLE_INVENTORY, values, "id = ?", new String[]{String.valueOf(stock.id)});
        }
    }

    public static class ShuttleStockData {
        public int id;
        public String brandName;
        public int tubeCount;
        public int shuttlesPerTube;
        public double totalPrice;
        public double pricePerShuttle;
        public int remainingShuttles;

        public ShuttleStockData(int id, String brandName, int tubeCount, int shuttlesPerTube, double totalPrice, double pricePerShuttle, int remainingShuttles) {
            this.id = id;
            this.brandName = brandName;
            this.tubeCount = tubeCount;
            this.shuttlesPerTube = shuttlesPerTube;
            this.totalPrice = totalPrice;
            this.pricePerShuttle = pricePerShuttle;
            this.remainingShuttles = remainingShuttles;
        }
    }

    // --- REVENUE REPORT METHODS ---
    public void saveSessionReport(double totalExpense, double totalCollected, int playerCount, int maleCount, int femaleCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        Date now = new Date();

        String dateStr = dateFormat.format(now);
        String monthStr = monthFormat.format(now);
        String yearStr = yearFormat.format(now);
        double profitOrLoss = totalCollected - totalExpense;

        ContentValues values = new ContentValues();
        values.put("dateStr", dateStr);
        values.put("monthStr", monthStr);
        values.put("yearStr", yearStr);
        values.put("totalExpense", totalExpense);
        values.put("totalCollected", totalCollected);
        values.put("profitOrLoss", profitOrLoss);
        values.put("playerCount", playerCount);
        values.put("maleCount", maleCount);
        values.put("femaleCount", femaleCount);

        db.insert(TABLE_SESSION_REPORTS, null, values);
    }

    public List<SessionReportItem> getAllSessionReportsList() {
        List<SessionReportItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SESSION_REPORTS, null, null, null, null, null, "id DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("dateStr"));
                String monthStr = cursor.getString(cursor.getColumnIndexOrThrow("monthStr"));
                String yearStr = cursor.getString(cursor.getColumnIndexOrThrow("yearStr"));
                double totalExpense = cursor.getDouble(cursor.getColumnIndexOrThrow("totalExpense"));
                double totalCollected = cursor.getDouble(cursor.getColumnIndexOrThrow("totalCollected"));
                double profitOrLoss = cursor.getDouble(cursor.getColumnIndexOrThrow("profitOrLoss"));
                int playerCount = cursor.getInt(cursor.getColumnIndexOrThrow("playerCount"));
                int maleCount = cursor.getInt(cursor.getColumnIndexOrThrow("maleCount"));
                int femaleCount = cursor.getInt(cursor.getColumnIndexOrThrow("femaleCount"));

                list.add(new SessionReportItem(id, dateStr, monthStr, yearStr, totalExpense, totalCollected, profitOrLoss, playerCount, maleCount, femaleCount));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public void updateSessionReport(int id, double totalExpense, double totalCollected) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("totalExpense", totalExpense);
        values.put("totalCollected", totalCollected);
        values.put("profitOrLoss", totalCollected - totalExpense);
        db.update(TABLE_SESSION_REPORTS, values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteSessionReport(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SESSION_REPORTS, "id = ?", new String[]{String.valueOf(id)});
    }

    public ReportSummary getDailyReport(String dateStr) {
        return getReportByQuery("dateStr = ?", new String[]{dateStr});
    }

    public ReportSummary getMonthlyReport(String monthStr) {
        return getReportByQuery("monthStr = ?", new String[]{monthStr});
    }

    public ReportSummary getYearlyReport(String yearStr) {
        return getReportByQuery("yearStr = ?", new String[]{yearStr});
    }

    private ReportSummary getReportByQuery(String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SESSION_REPORTS,
                new String[]{"SUM(totalExpense)", "SUM(totalCollected)", "SUM(profitOrLoss)", "COUNT(id)"},
                selection, selectionArgs, null, null, null);

        double totalExp = 0, totalColl = 0, profitLoss = 0;
        int sessionCount = 0;

        if (cursor != null && cursor.moveToFirst()) {
            totalExp = cursor.getDouble(0);
            totalColl = cursor.getDouble(1);
            profitLoss = cursor.getDouble(2);
            sessionCount = cursor.getInt(3);
            cursor.close();
        }
        return new ReportSummary(totalExp, totalColl, profitLoss, sessionCount);
    }

    public static class SessionReportItem {
        public int id;
        public String dateStr;
        public String monthStr;
        public String yearStr;
        public double totalExpense;
        public double totalCollected;
        public double profitOrLoss;
        public int playerCount;
        public int maleCount;
        public int femaleCount;

        public SessionReportItem(int id, String dateStr, String monthStr, String yearStr,
                                 double totalExpense, double totalCollected, double profitOrLoss,
                                 int playerCount, int maleCount, int femaleCount) {
            this.id = id;
            this.dateStr = dateStr;
            this.monthStr = monthStr;
            this.yearStr = yearStr;
            this.totalExpense = totalExpense;
            this.totalCollected = totalCollected;
            this.profitOrLoss = profitOrLoss;
            this.playerCount = playerCount;
            this.maleCount = maleCount;
            this.femaleCount = femaleCount;
        }
    }

    public static class ReportSummary {
        public double totalExpense;
        public double totalCollected;
        public double profitOrLoss;
        public int sessionCount;

        public ReportSummary(double totalExpense, double totalCollected, double profitOrLoss, int sessionCount) {
            this.totalExpense = totalExpense;
            this.totalCollected = totalCollected;
            this.profitOrLoss = profitOrLoss;
            this.sessionCount = sessionCount;
        }
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYERS, null, null);
        db.delete(TABLE_HISTORY, null, null);
        db.delete(TABLE_EXPENSES, null, null);
        db.delete(TABLE_SESSION_REPORTS, null, null);
        db.delete(TABLE_SHUTTLE_INVENTORY, null, null);
    }
}