package model;

import java.time.LocalDateTime;

public class Retur {
    private int idRetur;
    private String noRetur;
    private LocalDateTime tanggal;
    private int idPenjualan;
    private String noNota;
    private int idBuku;
    private String kodeBuku;
    private String judul;
    private int qty;
    private String alasan;

    public Retur() {}

    public Retur(int idRetur, String noRetur, LocalDateTime tanggal,
                 int idPenjualan, String noNota, int idBuku,
                 String kodeBuku, String judul, int qty, String alasan) {
        this.idRetur = idRetur;
        this.noRetur = noRetur;
        this.tanggal = tanggal;
        this.idPenjualan = idPenjualan;
        this.noNota = noNota;
        this.idBuku = idBuku;
        this.kodeBuku = kodeBuku;
        this.judul = judul;
        this.qty = qty;
        this.alasan = alasan;
    }

    public int getIdRetur() {
        return idRetur;
    }

    public void setIdRetur(int idRetur) {
        this.idRetur = idRetur;
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

    public int getIdBuku() {
        return idBuku;
    }

    public void setIdBuku(int idBuku) {
        this.idBuku = idBuku;
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

    public String getAlasan() {
        return alasan;
    }

    public void setAlasan(String alasan) {
        this.alasan = alasan;
    }
}
