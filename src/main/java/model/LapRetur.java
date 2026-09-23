package model;

import java.time.LocalDateTime;

public class LapRetur {
    private String noRetur;
    private LocalDateTime tanggal;
    private String noNota;
    private String kodeBuku;
    private String judul;
    private int qty;
    private double hargaJual;
    private double totalRefund;
    private String alasan;

    public LapRetur() {}

    public LapRetur(String noRetur, LocalDateTime tanggal, String noNota, String kodeBuku,
                    String judul, int qty, double hargaJual, double totalRefund, String alasan) {
        this.noRetur = noRetur;
        this.tanggal = tanggal;
        this.noNota = noNota;
        this.kodeBuku = kodeBuku;
        this.judul = judul;
        this.qty = qty;
        this.hargaJual = hargaJual;
        this.totalRefund = totalRefund;
        this.alasan = alasan;
    }

    public String getNoRetur() {
        return noRetur;
    }

    public void setNoRetur(String noRetur) {
        this.noRetur = noRetur;
    }

    public LocalDateTime getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDateTime tanggal) {
        this.tanggal = tanggal;
    }

    public String getNoNota() {
        return noNota;
    }

    public void setNoNota(String noNota) {
        this.noNota = noNota;
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

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public double getHargaJual() {
        return hargaJual;
    }

    public void setHargaJual(double hargaJual) {
        this.hargaJual = hargaJual;
    }

    public double getTotalRefund() {
        return totalRefund;
    }

    public void setTotalRefund(double totalRefund) {
        this.totalRefund = totalRefund;
    }

    public String getAlasan() {
        return alasan;
    }

    public void setAlasan(String alasan) {
        this.alasan = alasan;
    }
}
