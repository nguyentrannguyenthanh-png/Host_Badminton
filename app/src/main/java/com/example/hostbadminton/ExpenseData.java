package com.example.hostbadminton;

public class ExpenseData {
    private double courtFee;
    private double shuttleFee;
    private double otherFee;
    private String note;
    private boolean isFixedFeeMode;
    private double fixedFeePerPlayer;
    private boolean isSeparateGenderFee;
    private double maleFee;
    private double femaleFee;
    private int shuttlesUsedCount;
    private boolean useInventoryShuttle;

    public ExpenseData(double courtFee, double shuttleFee, double otherFee, String note) {
        this(courtFee, shuttleFee, otherFee, note, false, 0, false, 0, 0, 0, false);
    }

    public ExpenseData(double courtFee, double shuttleFee, double otherFee, String note,
                       boolean isFixedFeeMode, double fixedFeePerPlayer,
                       boolean isSeparateGenderFee, double maleFee, double femaleFee) {
        this(courtFee, shuttleFee, otherFee, note, isFixedFeeMode, fixedFeePerPlayer, isSeparateGenderFee, maleFee, femaleFee, 0, false);
    }

    public ExpenseData(double courtFee, double shuttleFee, double otherFee, String note,
                       boolean isFixedFeeMode, double fixedFeePerPlayer,
                       boolean isSeparateGenderFee, double maleFee, double femaleFee,
                       int shuttlesUsedCount, boolean useInventoryShuttle) {
        this.courtFee = courtFee;
        this.shuttleFee = shuttleFee;
        this.otherFee = otherFee;
        this.note = note != null ? note : "";
        this.isFixedFeeMode = isFixedFeeMode;
        this.fixedFeePerPlayer = fixedFeePerPlayer;
        this.isSeparateGenderFee = isSeparateGenderFee;
        this.maleFee = maleFee;
        this.femaleFee = femaleFee;
        this.shuttlesUsedCount = shuttlesUsedCount;
        this.useInventoryShuttle = useInventoryShuttle;
    }

    public double getCourtFee() { return courtFee; }
    public void setCourtFee(double courtFee) { this.courtFee = courtFee; }

    public double getShuttleFee() { return shuttleFee; }
    public void setShuttleFee(double shuttleFee) { this.shuttleFee = shuttleFee; }

    public double getOtherFee() { return otherFee; }
    public void setOtherFee(double otherFee) { this.otherFee = otherFee; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public boolean isFixedFeeMode() { return isFixedFeeMode; }
    public void setFixedFeeMode(boolean fixedFeeMode) { isFixedFeeMode = fixedFeeMode; }

    public double getFixedFeePerPlayer() { return fixedFeePerPlayer; }
    public void setFixedFeePerPlayer(double fixedFeePerPlayer) { this.fixedFeePerPlayer = fixedFeePerPlayer; }

    public boolean isSeparateGenderFee() { return isSeparateGenderFee; }
    public void setSeparateGenderFee(boolean separateGenderFee) { isSeparateGenderFee = separateGenderFee; }

    public double getMaleFee() { return maleFee; }
    public void setMaleFee(double maleFee) { this.maleFee = maleFee; }

    public double getFemaleFee() { return femaleFee; }
    public void setFemaleFee(double femaleFee) { this.femaleFee = femaleFee; }

    public int getShuttlesUsedCount() { return shuttlesUsedCount; }
    public void setShuttlesUsedCount(int shuttlesUsedCount) { this.shuttlesUsedCount = shuttlesUsedCount; }

    public boolean isUseInventoryShuttle() { return useInventoryShuttle; }
    public void setUseInventoryShuttle(boolean useInventoryShuttle) { this.useInventoryShuttle = useInventoryShuttle; }

    public double getTotalExpense() {
        return courtFee + shuttleFee + otherFee;
    }
}