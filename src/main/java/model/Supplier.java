package model;

public class Supplier {
    private int idSupplier;
    private String namaSupplier;
    private String alamat;
    private String noTelp;

    public Supplier() {}

    public Supplier(int idSupplier, String namaSupplier, String alamat, String noTelp) {
        this.idSupplier = idSupplier;
        this.namaSupplier = namaSupplier;
        this.alamat = alamat;
        this.noTelp = noTelp;
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

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getNoTelp() {
        return noTelp;
    }

    public void setNoTelp(String noTelp) {
        this.noTelp = noTelp;
    }
}
