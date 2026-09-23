package model;

public class LapTerlaris {
    private String kodeBuku;
    private String judul;
    private int totalQty;
    private double totalOmzet;

    public LapTerlaris() {}

    public LapTerlaris(String kodeBuku, String judul, int totalQty, double totalOmzet) {
        this.kodeBuku = kodeBuku;
        this.judul = judul;
        this.totalQty = totalQty;
        this.totalOmzet = totalOmzet;
    }

    public String getKodeBuku() {
        return kodeBuku;
    }

    public void setKodeBuku(String kodeBuku) {
        this.kodeBuku = kodeBuku;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public int getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(int totalQty) {
        this.totalQty = totalQty;
    }

    public double getTotalOmzet() {
        return totalOmzet;
    }

    public void setTotalOmzet(double totalOmzet) {
        this.totalOmzet = totalOmzet;
    }
}
