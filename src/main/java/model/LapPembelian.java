package model;

import java.time.LocalDateTime;

public class LapPembelian {
    private String noFaktur;
    private LocalDateTime tanggal;
    private String supplier;
    private String kasir;
    private double total;

    public LapPembelian() {}

    public LapPembelian(String noFaktur, LocalDateTime tanggal, String supplier, String kasir, double total) {
        this.noFaktur = noFaktur;
        this.tanggal = tanggal;
        this.supplier = supplier;
        this.kasir = kasir;
        this.total = total;
    }

    public String getNoFaktur() {
        return noFaktur;
    }

    public void setNoFaktur(String noFaktur) {
        this.noFaktur = noFaktur;
    }

    public LocalDateTime getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDateTime tanggal) {
        this.tanggal = tanggal;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getKasir() {
        return kasir;
    }

    public void setKasir(String kasir) {
        this.kasir = kasir;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
