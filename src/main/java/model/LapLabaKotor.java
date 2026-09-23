package model;

import java.time.LocalDateTime;

public class LapLabaKotor {
    private String noNota;
    private LocalDateTime tanggal;
    private String kodeBuku;
    private String judul;
    private int qty;
    private double hargaBeli;
    private double hargaJual;
    private double totalModal;
    private double totalOmset;
    private double labaKotor;
    private double marginPct;

    public LapLabaKotor() {}

    public LapLabaKotor(String noNota, LocalDateTime tanggal, String kodeBuku, String judul,
                        int qty, double hargaBeli, double hargaJual, double totalModal,
                        double totalOmset, double labaKotor, double marginPct) {
        this.noNota = noNota;
        this.tanggal = tanggal;
        this.kodeBuku = kodeBuku;
        this.judul = judul;
        this.qty = qty;
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaJual;
        this.totalModal = totalModal;
        this.totalOmset = totalOmset;
        this.labaKotor = labaKotor;
        this.marginPct = marginPct;
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

    public double getTotalModal() {
        return totalModal;
    }

    public void setTotalModal(double totalModal) {
        this.totalModal = totalModal;
    }

    public double getTotalOmset() {
        return totalOmset;
    }

    public void setTotalOmset(double totalOmset) {
        this.totalOmset = totalOmset;
    }

    public double getLabaKotor() {
        return labaKotor;
    }

    public void setLabaKotor(double labaKotor) {
        this.labaKotor = labaKotor;
    }

    public double getMarginPct() {
        return marginPct;
    }

    public void setMarginPct(double marginPct) {
        this.marginPct = marginPct;
    }
}
