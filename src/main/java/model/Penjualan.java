package model;

import java.time.LocalDateTime;

public class Penjualan {
    private int idPenjualan;
    private String noNota;
    private LocalDateTime tanggal;
    private int idUser;
    private String username;
    private double total;
    private double bayar;
    private double kembalian;
    private String metodeBayar;
    private double diskon;

    public Penjualan() {}

    public Penjualan(int idPenjualan, String noNota, LocalDateTime tanggal,
                     int idUser, String username,
                     double total, double bayar, double kembalian,
                     String metodeBayar, double diskon) {
        this.idPenjualan = idPenjualan;
        this.noNota = noNota;
        this.tanggal = tanggal;
        this.idUser = idUser;
        this.username = username;
        this.total = total;
        this.bayar = bayar;
        this.kembalian = kembalian;
        this.metodeBayar = metodeBayar;
        this.diskon = diskon;
    }

    public int getIdPenjualan() {
        return idPenjualan;
    }

    public void setIdPenjualan(int idPenjualan) {
        this.idPenjualan = idPenjualan;
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

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getBayar() {
        return bayar;
    }

    public void setBayar(double bayar) {
        this.bayar = bayar;
    }

    public double getKembalian() {
        return kembalian;
    }

    public void setKembalian(double kembalian) {
        this.kembalian = kembalian;
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
