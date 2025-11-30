package ui;


import models.Utilisateur;
import services.AuthService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginFrame() {
        setTitle("Connexion - Gestion Scolarité");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);

        panel.add(new JLabel("Mot de passe:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        loginButton = new JButton("Se connecter");
        panel.add(new JLabel()); // empty space
        panel.add(loginButton);

        add(panel);
        loginButton.addActionListener(e -> loginAction());

        setVisible(true);
    }

    private void loginAction() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        Utilisateur user = AuthService.login(email, password);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Email ou mot de passe incorrect", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Bienvenue " + user.getNom() + " (" + user.getRole() + ")");

        dispose(); // close login

        switch (user.getRole()) {
            case "etudiant" -> new EtudiantDashboard(user);
            case "enseignant" -> new EnseignantDashboard(user);
            case "secretaire" -> new SecretaireDashboard(user);
            case "chef_programme" -> new ChefProgrammeDashboard(user);
            case "administrateur" -> new AdminDashboard(user);
            default -> JOptionPane.showMessageDialog(null, "Rôle inconnu !");
        }
    }
}
