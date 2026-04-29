package Proyecto;


import Proyecto.View.usuario.LoginView;

public class MainGUI {

    public static void main(String[] args) {

        javax.swing.SwingUtilities.invokeLater(() -> {
            LoginView login = new LoginView();
            login.setVisible(true);
        });

    }
}