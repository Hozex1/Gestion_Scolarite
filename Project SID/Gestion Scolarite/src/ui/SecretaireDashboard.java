package ui;

import models.Utilisateur;
import services.SecretaireService;

import javax.swing.*;
import java.awt.*;

public class SecretaireDashboard extends JFrame {

    private final Utilisateur secretaire;
    private final JPanel contentPanel;

    public SecretaireDashboard(Utilisateur secretaire) {
        this.secretaire = secretaire;

        // Theme colors (same everywhere)
        Color primaryBlue = new Color(52, 152, 219);
        Color primaryDarkBlue = new Color(41, 128, 185);
        Color sidebarBg = primaryBlue;
        Color hover = primaryDarkBlue;
        Color selected = primaryDarkBlue;
        Color textColor = Color.WHITE;

        setTitle("Espace Secrétaire - " + secretaire.getNom());
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= Sidebar =================
        JPanel sidebar = new JPanel();
        sidebar.setBackground(sidebarBg);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.gridx = 0;

        JLabel title = new JLabel("SECRÉTAIRE", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridy = 0;
        sidebar.add(title, gbc);

        gbc.gridy = 1;
        sidebar.add(Box.createVerticalStrut(15), gbc);

        // Buttons (NO emojis)
        JButton btnAddEtudiant = createButton("Ajouter étudiant", sidebarBg, textColor, hover, selected);
        JButton btnModifierUser = createButton("Modifier utilisateur", sidebarBg, textColor, hover, selected);
        JButton btnSupprimerUser = createButton("Supprimer utilisateur", sidebarBg, textColor, hover, selected);
        JButton btnModifierInscription = createButton("Modifier inscription", sidebarBg, textColor, hover, selected);
        JButton btnSupprimerInscription = createButton("Supprimer inscription", sidebarBg, textColor, hover, selected);
        JButton btnAfficher = createButton("Afficher étudiants", sidebarBg, textColor, hover, selected);

        JButton btnLogout = createButton("Déconnexion",
                new Color(231, 76, 60), textColor,
                new Color(192, 57, 43), new Color(192, 57, 43));

        JButton[] allButtons = {
                btnAddEtudiant, btnModifierUser, btnSupprimerUser,
                btnModifierInscription, btnSupprimerInscription,
                btnAfficher, btnLogout
        };

        int row = 2;
        for (JButton btn : allButtons) {
            gbc.gridy = row++;
            sidebar.add(btn, gbc);
        }

        // ================= Content Panel =================
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(245, 245, 245));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcome = new JLabel(
                "<html><h2>Bienvenue, " + secretaire.getNom() + "</h2>"
                        + "<p>Sélectionnez une action dans le menu à gauche.</p></html>"
        );
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 17));

        contentPanel.add(welcome, BorderLayout.NORTH);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // ================= Actions =================
        btnAddEtudiant.addActionListener(e -> SecretaireService.ajouterEtudiant());
        btnModifierUser.addActionListener(e -> SecretaireService.modifierUtilisateur());
        btnSupprimerUser.addActionListener(e -> SecretaireService.supprimerUtilisateur());
        btnModifierInscription.addActionListener(e -> SecretaireService.modifierInscription());
        btnSupprimerInscription.addActionListener(e -> SecretaireService.supprimerInscription());
        btnAfficher.addActionListener(e -> SecretaireService.showUsers());

        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        setVisible(true);
    }

    // Same button builder used in all dashboards
    private JButton createButton(String text, Color bg, Color fg, Color hover, Color pressed) {
        JButton btn = new JButton(text);
        btn.setFocusable(false);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(bg); }
            public void mousePressed(java.awt.event.MouseEvent evt) { btn.setBackground(pressed); }
            public void mouseReleased(java.awt.event.MouseEvent evt) { btn.setBackground(hover); }
        });

        return btn;
    }
}
