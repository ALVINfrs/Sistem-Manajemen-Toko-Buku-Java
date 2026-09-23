package model;

import java.time.LocalDateTime;

public class LapPenjualan {
    private String noNota;
    private LocalDateTime tanggal;
    private String kasir;
    private String member;
    private double total;
    private String metodeBayar;
    private double diskon;

    public LapPenjualan() {}

    public LapPenjualan(String noNota, LocalDateTime tanggal, String kasir, String member, double total) {
        this.noNota = noNota;
        this.tanggal = tanggal;
        this.kasir = kasir;
        this.member = member;
        this.total = total;
    }

    public LapPenjualan(String noNota, LocalDateTime tanggal, String kasir, String member,
                        double total, String metodeBayar, double diskon) {
        this.noNota = noNota;
        this.tanggal = tanggal;
        this.kasir = kasir;
        this.member = member;
        this.total = total;
        this.metodeBayar = metodeBayar;
        this.diskon = diskon;
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

    public String getMetodeBayar() {
        return metodeBayar;
    }

    public void setMetodeBayar(String metodeBayar) {
        this.metodeBayar = metodeBayar;
    }

    public double getDiskon() {
        return diskon;
    }

    public void setDiskon(double diskon) {
        this.diskon = diskon;
    }
}
