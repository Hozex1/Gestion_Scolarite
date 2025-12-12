package ui;

import models.Utilisateur;
import services.EtudiantService;

import javax.swing.*;
import java.awt.*;

public class EtudiantDashboard extends JFrame {

    public EtudiantDashboard(Utilisateur etudiant) {

        // Colors (same as Login / Admin)
        Color primaryBlue = new Color(52, 152, 219);
        Color primaryDarkBlue = new Color(41, 128, 185);
        Color sidebarBg = primaryBlue;
        Color hover = primaryDarkBlue;
        Color selected = primaryDarkBlue;
        Color textColor = Color.WHITE;

        setTitle("Espace Étudiant - " + etudiant.getNom());
        setSize(950, 600);
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

        JLabel title = new JLabel("ÉTUDIANT", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridy = 0;
        sidebar.add(title, gbc);

        gbc.gridy = 1;
        sidebar.add(Box.createVerticalStrut(15), gbc);

        // Buttons
        JButton btnNotes = createButton("Voir mes notes", sidebarBg, textColor, hover, selected);
        JButton btnMoyenne = createButton("Voir ma moyenne", sidebarBg, textColor, hover, selected);
        JButton btnStatut = createButton("Voir mon statut", sidebarBg, textColor, hover, selected);
        JButton btnInfos = createButton("Mes informations", sidebarBg, textColor, hover, selected);
        JButton btnBulletin = createButton("Afficher bulletin", sidebarBg, textColor, hover, selected);
        JButton btnLogout = createButton("Déconnexion",
                new Color(231, 76, 60), textColor, new Color(192, 57, 43), new Color(192, 57, 43));

        JButton[] allButtons = {
                btnNotes, btnMoyenne, btnStatut, btnInfos, btnBulletin, btnLogout
        };

        int row = 2;
        for (JButton btn : allButtons) {
            gbc.gridy = row++;
            sidebar.add(btn, gbc);
        }

        // ================= Content Panel =================
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(245, 245, 245));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcome = new JLabel(
                "<html><h2>Bienvenue, " + etudiant.getNom() + "</h2>"
                        + "<p>Sélectionnez une action à gauche.</p></html>"
        );
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 17));

        content.add(welcome, BorderLayout.NORTH);

        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        // ================= Button Actions =================
        btnNotes.addActionListener(e -> EtudiantService.afficherNotes(etudiant));
        btnMoyenne.addActionListener(e -> EtudiantService.afficherMoyenne(etudiant));
        btnStatut.addActionListener(e -> EtudiantService.afficherStatut(etudiant));
        btnInfos.addActionListener(e -> EtudiantService.afficherInfosPerso(etudiant));
        btnBulletin.addActionListener(e -> EtudiantService.afficherBulletin(etudiant));

        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        setVisible(true);
    }

    // Reusable button creator (same as Admin)
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
