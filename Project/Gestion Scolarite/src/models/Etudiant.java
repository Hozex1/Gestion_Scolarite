package models;

public class Etudiant extends Utilisateur {
    private String origineScolaire;
    private String statut;

    public Etudiant(int id, String nom, String prenom, String email, String motDePasse, String role,
                    String origineScolaire, String statut) {
        super(id, nom, prenom, email, motDePasse, role);
        this.origineScolaire = origineScolaire;
        this.statut = statut;
    }

    public String getOrigineScolaire() { return origineScolaire; }
    public void setOrigineScolaire(String origineScolaire) { this.origineScolaire = origineScolaire; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
