package model;

import java.time.LocalDateTime;

public class LapPenjualan {
    private String noNota;
    private LocalDateTime tanggal;
    private String kasir;
    private String member;
    private double total;

    public LapPenjualan() {}

    public LapPenjualan(String noNota, LocalDateTime tanggal, String kasir, String member, double total) {
        this.noNota = noNota;
        this.tanggal = tanggal;
        this.kasir = kasir;
        this.member = member;
        this.total = total;
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

    public String getKasir() {
        return kasir;
    }

    public void setKasir(String kasir) {
        this.kasir = kasir;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
