package ui;

import models.Utilisateur;
import services.AdministrateurService;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    private final Utilisateur admin;
    private final JPanel contentPanel;

    public AdminDashboard(Utilisateur admin) {
        this.admin = admin;

        // Colors from LoginFrame
        Color primaryBlue = new Color(52, 152, 219);
        Color primaryDarkBlue = new Color(41, 128, 185);
        Color sidebarBg = primaryBlue;
        Color hover = primaryDarkBlue;
        Color selected = primaryDarkBlue;
        Color textColor = Color.WHITE;

        setTitle("Espace Administrateur - " + admin.getNom());
        setSize(1100, 750);
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

        JLabel title = new JLabel("ADMINISTRATEUR", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridy = 0;
        sidebar.add(title, gbc);

        gbc.gridy = 1;
        sidebar.add(Box.createVerticalStrut(15), gbc);

        // Fixed icons (no emojis)
        JButton btnAfficher = createButton("Afficher utilisateurs", sidebarBg, textColor, hover, selected);
        JButton btnAdd = createButton("Ajouter utilisateur", sidebarBg, textColor, hover, selected);
        JButton btnEdit = createButton("Modifier utilisateur", sidebarBg, textColor, hover, selected);
        JButton btnDelete = createButton("Supprimer utilisateur", sidebarBg, textColor, hover, selected);
        JButton btnDroits = createButton("Modifier droits d'accès", sidebarBg, textColor, hover, selected);
        JButton btnSauvegarde = createButton("Sauvegarder données", sidebarBg, textColor, hover, selected);
        JButton btnRestaurer = createButton("Restaurer données", sidebarBg, textColor, hover, selected);
        JButton btnStats = createButton("Rapports & statistiques", sidebarBg, textColor, hover, selected);
        JButton btnLogout = createButton("Déconnexion", new Color(231, 76, 60), textColor, new Color(192, 57, 43), new Color(192, 57, 43));

        JButton[] allButtons = {
                btnAfficher, btnAdd, btnEdit, btnDelete, btnDroits,
                btnSauvegarde, btnRestaurer, btnStats, btnLogout
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
                "<html><h2>Bienvenue, " + admin.getNom() + "</h2>"
                        + "<p>Veuillez choisir une action dans le menu.</p></html>"
        );
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        contentPanel.add(welcome, BorderLayout.NORTH);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // ================= Button Actions =================
        btnAfficher.addActionListener(e -> AdministrateurService.afficherUtilisateurs());
        btnAdd.addActionListener(e -> AdministrateurService.ajouterUtilisateur());
        btnEdit.addActionListener(e -> AdministrateurService.modifierUtilisateur());
        btnDelete.addActionListener(e -> AdministrateurService.supprimerUtilisateur());
        btnDroits.addActionListener(e -> AdministrateurService.modifierDroitsAcces(admin));
        btnSauvegarde.addActionListener(e -> AdministrateurService.sauvegarderDonnees());

        btnRestaurer.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                AdministrateurService.restaurerDonnees(fc.getSelectedFile().getAbsolutePath());
            }
        });

        btnStats.addActionListener(e -> AdministrateurService.genererRapportsEtStatistiques());

        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        setVisible(true);
    }

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
