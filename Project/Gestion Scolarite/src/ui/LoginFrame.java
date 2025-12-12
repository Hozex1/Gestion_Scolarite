package ui;

import models.Utilisateur;
import services.AuthService;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginFrame() {

        // FORCE modern look (fixes ugly buttons)
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        setTitle("Connexion - Gestion Scolarité");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 360);
        setLocationRelativeTo(null);
        setResizable(false);

        Color backgroundColor = new Color(240, 240, 240);
        Color cardColor = Color.WHITE;
        Color primaryColor = new Color(52, 152, 219);

        JPanel root = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(backgroundColor);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JPanel card = new RoundedPanel(25);
        card.setBackground(cardColor);
        card.setPreferredSize(new Dimension(380, 260));
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("Connexion");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        card.add(title, gbc);

        // Email
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        card.add(new JLabel("Email :"), gbc);

        gbc.gridx = 1;
        emailField = new JTextField();
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailField.setPreferredSize(new Dimension(200, 35));
        card.add(emailField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        card.add(new JLabel("Mot de passe :"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(200, 35));
        card.add(passwordField, gbc);

        // Button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        loginButton = new JButton("Se connecter");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setBackground(primaryColor);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(200, 40));

        // Rounded effect
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        card.add(loginButton, gbc);

        root.add(card);
        add(root);

        // Actions
        loginButton.addActionListener(e -> loginAction());
        emailField.addActionListener(e -> loginAction());
        passwordField.addActionListener(e -> loginAction());

        setVisible(true);
    }

    // Custom rounded panel
    static class RoundedPanel extends JPanel {
        private int radius;
        RoundedPanel(int radius) { this.radius = radius; }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.dispose();
        }
    }

    private void loginAction() {

        String email = emailField.getText().trim();
        char[] passChars = passwordField.getPassword();
        String password = new String(passChars).trim();
        Arrays.fill(passChars, '\0');

        Utilisateur user = AuthService.login(email, password);

        if (user == null) {
            JOptionPane.showMessageDialog(this,
                    "Email ou mot de passe incorrect",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dispose();

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
