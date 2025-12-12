package models;

public class ChefProgramme extends Utilisateur {
    private String departement;

    public ChefProgramme(int id, String nom, String prenom, String email, String motDePasse, String role, String departement) {
        super(id, nom, prenom, email, motDePasse, role);
        this.departement = departement;
    }

    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
}
