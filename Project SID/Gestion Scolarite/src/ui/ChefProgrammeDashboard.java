package ui;

import models.Utilisateur;
import services.ChefProgrammeService;

import javax.swing.*;
import java.awt.*;

public class ChefProgrammeDashboard extends JFrame {

    private final Utilisateur chef;
    private final JPanel contentPanel;

    public ChefProgrammeDashboard(Utilisateur chef) {
        this.chef = chef;

        // Colors from LoginFrame + AdminDashboard
        Color primaryBlue = new Color(52, 152, 219);
        Color primaryDarkBlue = new Color(41, 128, 185);
        Color sidebarBg = primaryBlue;
        Color hover = primaryDarkBlue;
        Color pressed = primaryDarkBlue;
        Color textColor = Color.WHITE;

        setTitle("Chef de Programme - " + chef.getNom());
        setSize(1100, 950);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ================= Sidebar =================
        JPanel sidebar = new JPanel();
        sidebar.setBackground(sidebarBg);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.gridx = 0;

        JLabel title = new JLabel("CHEF DE PROGRAMME", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridy = 0;
        sidebar.add(title, gbc);

        gbc.gridy = 1;
        sidebar.add(Box.createVerticalStrut(15), gbc);

        // === Buttons identical style to Admin ===
        JButton btnAjouterProgramme = createButton("Ajouter un programme", sidebarBg, textColor, hover, pressed);
        JButton btnModifierProgramme = createButton("Modifier un programme", sidebarBg, textColor, hover, pressed);
        JButton btnSupprimerProgramme = createButton("Supprimer un programme", sidebarBg, textColor, hover, pressed);

        JButton btnAfficherMatieres = createButton("Voir matières d’un programme", sidebarBg, textColor, hover, pressed);
        JButton btnAjouterMatiere = createButton("Ajouter une matière", sidebarBg, textColor, hover, pressed);
        JButton btnAjouterMatiereExistante = createButton("Ajouter matière existante", sidebarBg, textColor, hover, pressed);
        JButton btnModifierMatiere = createButton("Modifier une matière", sidebarBg, textColor, hover, pressed);
        JButton btnSupprimerMatiere = createButton("Supprimer une matière", sidebarBg, textColor, hover, pressed);

        JButton btnPrerequis = createButton("Définir prérequis programmes", sidebarBg, textColor, hover, pressed);
        JButton btnPonderations = createButton("Définir coefficients matières / épreuves", sidebarBg, textColor, hover, pressed);
        JButton btnAssignerEns = createButton("Assigner / modifier enseignant", sidebarBg, textColor, hover, pressed);
        JButton btnValiderStatuts = createButton("Valider moyennes et statuts", sidebarBg, textColor, hover, pressed);

        JButton btnLogout = createButton("Déconnexion", new Color(231, 76, 60), textColor, new Color(192, 57, 43), new Color(192, 57, 43));

        JButton[] allButtons = {
                btnAjouterProgramme, btnModifierProgramme, btnSupprimerProgramme,
                btnAfficherMatieres, btnAjouterMatiere, btnAjouterMatiereExistante,
                btnModifierMatiere, btnSupprimerMatiere, btnPrerequis, btnPonderations,
                btnAssignerEns, btnValiderStatuts, btnLogout
        };

        int row = 2;
        for (JButton b : allButtons) {
            gbc.gridy = row++;
            sidebar.add(b, gbc);
        }

        // ================= Content Panel =================
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(245, 245, 245));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcome = new JLabel(
                "<html><h2>Bienvenue, " + chef.getNom() + "</h2>"
                        + "<p>Choisissez une action dans le menu.</p></html>"
        );
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        contentPanel.add(welcome, BorderLayout.NORTH);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // ================= Button Actions =================
        btnAjouterProgramme.addActionListener(e -> ChefProgrammeService.ajouterProgramme(chef));
        btnModifierProgramme.addActionListener(e -> ChefProgrammeService.modifierProgramme(chef));
        btnSupprimerProgramme.addActionListener(e -> ChefProgrammeService.supprimerProgramme(chef));

        btnAfficherMatieres.addActionListener(e -> ChefProgrammeService.afficherMatieresParProgramme());
        btnAjouterMatiere.addActionListener(e -> ChefProgrammeService.ajouterMatiere(chef));
        btnAjouterMatiereExistante.addActionListener(e -> ChefProgrammeService.ajouterMatiereExistanteAProgramme());
        btnModifierMatiere.addActionListener(e -> ChefProgrammeService.modifierMatiere(chef));
        btnSupprimerMatiere.addActionListener(e -> ChefProgrammeService.supprimerMatiere(chef));

        btnPrerequis.addActionListener(e -> ChefProgrammeService.definirPrerequisProgramme(chef));
        btnPonderations.addActionListener(e -> ChefProgrammeService.definirPonderationsMatieres(chef));
        btnAssignerEns.addActionListener(e -> ChefProgrammeService.assignerOuModifierEnseignantMatiere(chef));
        btnValiderStatuts.addActionListener(e -> ChefProgrammeService.validerMoyennesEtStatuts(chef));

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
