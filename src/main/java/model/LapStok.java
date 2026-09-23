package model;

public class LapStok {
    private String kodeBuku;
    private String judul;
    private String kategori;
    private String penerbit;
    private double hargaBeli;
    private double hargaJual;
    private int stok;

    public LapStok() {}

    public LapStok(String kodeBuku, String judul, String kategori, String penerbit,
                   double hargaBeli, double hargaJual, int stok) {
        this.kodeBuku = kodeBuku;
        this.judul = judul;
        this.kategori = kategori;
        this.penerbit = penerbit;
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaJual;
        this.stok = stok;
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

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getPenerbit() {
        return penerbit;
    }

    public void setPenerbit(String penerbit) {
        this.penerbit = penerbit;
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

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }
}
