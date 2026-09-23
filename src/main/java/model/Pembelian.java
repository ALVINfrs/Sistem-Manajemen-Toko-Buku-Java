package model;

import java.time.LocalDateTime;

public class Pembelian {
    private int idPembelian;
    private String noFaktur;
    private LocalDateTime tanggal;
    private int idSupplier;
    private String namaSupplier;
    private int idUser;
    private String username;
    private double total;

    public Pembelian() {}

    public Pembelian(int idPembelian, String noFaktur, LocalDateTime tanggal,
                     int idSupplier, String namaSupplier,
                     int idUser, String username, double total) {
        this.idPembelian = idPembelian;
        this.noFaktur = noFaktur;
        this.tanggal = tanggal;
        this.idSupplier = idSupplier;
        this.namaSupplier = namaSupplier;
        this.idUser = idUser;
        this.username = username;
        this.total = total;
    }

    public int getIdPembelian() {
        return idPembelian;
    }

    public void setIdPembelian(int idPembelian) {
        this.idPembelian = idPembelian;
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

    public int getIdSupplier() {
        return idSupplier;
    }

    public void setIdSupplier(int idSupplier) {
        this.idSupplier = idSupplier;
    }

    public String getNamaSupplier() {
        return namaSupplier;
    }

    public void setNamaSupplier(String namaSupplier) {
        this.namaSupplier = namaSupplier;
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
}
