package model;

import java.time.LocalDateTime;

public class LapPendapatan {
    private String noNota;
    private LocalDateTime tanggal;
    private String judul;
    private int qty;
    private double hargaBeli;
    private double hargaJual;
    private double laba;

    public LapPendapatan() {}

    public LapPendapatan(String noNota, LocalDateTime tanggal, String judul, int qty,
                         double hargaBeli, double hargaJual, double laba) {
        this.noNota = noNota;
        this.tanggal = tanggal;
        this.judul = judul;
        this.qty = qty;
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaJual;
        this.laba = laba;
    }

    public String getNoNota() {
        return noNota;
    }

    public void setNoNota(String noNota) {
        this.noNota = noNota;
    }

    public LocalDateTime getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDateTime tanggal) {
        this.tanggal = tanggal;
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

    public double getHargaBeli() {
        return hargaBeli;
    }

    public void setHargaBeli(double hargaBeli) {
        this.hargaBeli = hargaBeli;
    }

    public double getHargaJual() {
        return hargaJual;
    }

    public void setHargaJual(double hargaJual) {
        this.hargaJual = hargaJual;
    }

    public double getLaba() {
        return laba;
    }

    public void setLaba(double laba) {
        this.laba = laba;
    }
}
