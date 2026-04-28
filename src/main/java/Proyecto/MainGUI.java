package Proyecto;

import Proyecto.View.ViewGUI;

public class MainGUI {

    public static void main(String[] args) {

        javax.swing.SwingUtilities.invokeLater(() -> {
            ViewGUI vista = new ViewGUI();
            vista.setVisible(true);
        });

    }
}