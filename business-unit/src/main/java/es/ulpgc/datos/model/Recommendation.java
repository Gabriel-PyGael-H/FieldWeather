package es.ulpgc.datos.model;

public class Recommendation {
    private final String text;
    private final String status;

    public Recommendation(String text, String status) {
        this.text = text;
        this.status = status;
    }

    public String getText() { return text; }
    public String getStatus() { return status; }
}