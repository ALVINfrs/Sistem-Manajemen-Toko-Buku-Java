package util;
import javax.swing.*;
public class Validasi {
    public static boolean wajib(JTextField... fs) {
        for (JTextField f : fs)
            if (f.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Field wajib tidak boleh kosong: " + f.getName()); f.requestFocus(); return false; }
        return true;
    }
    public static boolean angka(JTextField f) {
        try { Double.parseDouble(f.getText().trim()); return true; }
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(null, "Harus angka: " + f.getName()); f.requestFocus(); return false; }
    }
    public static boolean integer(JTextField f) {
        try { Integer.parseInt(f.getText().trim()); return true; }
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(null, "Harus bilangan bulat: " + f.getName()); f.requestFocus(); return false; }
    }
}
