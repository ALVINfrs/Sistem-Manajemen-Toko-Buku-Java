package model;

public class LapSupplier {
    private int idSupplier;
    private String namaSupplier;
    private String noTelp;
    private String alamat;
    private int totalFaktur;
    private int totalPcs;
    private double totalBiaya;

    public LapSupplier() {}

    public LapSupplier(int idSupplier, String namaSupplier, String noTelp, String alamat,
                       int totalFaktur, int totalPcs, double totalBiaya) {
        this.idSupplier = idSupplier;
        this.namaSupplier = namaSupplier;
        this.noTelp = noTelp;
        this.alamat = alamat;
        this.totalFaktur = totalFaktur;
        this.totalPcs = totalPcs;
        this.totalBiaya = totalBiaya;
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

    public String getNoTelp() {
        return noTelp;
    }

    public void setNoTelp(String noTelp) {
        this.noTelp = noTelp;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public int getTotalFaktur() {
        return totalFaktur;
    }

    public void setTotalFaktur(int totalFaktur) {
        this.totalFaktur = totalFaktur;
    }

    public int getTotalPcs() {
        return totalPcs;
    }

    public void setTotalPcs(int totalPcs) {
        this.totalPcs = totalPcs;
    }

    public double getTotalBiaya() {
        return totalBiaya;
    }

    public void setTotalBiaya(double totalBiaya) {
        this.totalBiaya = totalBiaya;
    }
}
