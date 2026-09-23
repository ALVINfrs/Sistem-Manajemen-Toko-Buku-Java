package model;

public class LapGrafik {
    private String label;
    private double nilai;

    public LapGrafik() {}

    public LapGrafik(String label, double nilai) {
        this.label = label;
        this.nilai = nilai;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getNilai() {
        return nilai;
    }

    public void setNilai(double nilai) {
        this.nilai = nilai;
    }
}
