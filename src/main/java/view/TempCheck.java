package view;

import javax.swing.JFrame;

public class TempCheck extends JFrame {
    public TempCheck() {
        super("TempCheck");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new TempCheck().setVisible(true));
    }
}
