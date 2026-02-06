package modele;
public class Voiture {
    String id;
    double vitesseMax;
    String type;
    double longueur;
    double largeur;

    public Voiture(String id, double vitesseMax, String type, double longueur, double largeur){
        this.id = id;
        this.vitesseMax = vitesseMax;
        this.type = type;
        this.longueur = longueur;
        this.largeur = largeur;
    }
    public Voiture(double vitesseMax, String type, double longueur, double largeur){
        this.vitesseMax = vitesseMax;
        this.type = type;
        this.longueur = longueur;
        this.largeur = largeur;
    }
    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }
    public double getVitesseMax(){
        return vitesseMax;
    }
    public void setVitesseMax(double vitesseMax){
        this.vitesseMax = vitesseMax;
    }
    public String getType(){
        return type;
    }
    public void setType(String type){
        this.type = type;
    }
    public double getLongueur(){
        return longueur;
    }
    public void setLongueur(double longueur){
        this.longueur = longueur;
    }
    public double getLargeur(){
        return largeur;
    }
    public void setLargeur(double largeur){
        this.largeur = largeur;
    }
}
