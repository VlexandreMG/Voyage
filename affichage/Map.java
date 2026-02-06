package affichage;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import modele.Obstacles;
import modele.Route;
import modele.Ville;

public class Map extends JPanel {

    private List<Ville> villes;
    private List<Route> routes;
    private List<Obstacles> obstacles;

    // Filtrage
    private Route routeFiltree = null;
    private List<Obstacles> obstaclesFiltres = new ArrayList<>();
    private List<Ville> villesSurRoute = new ArrayList<>();
    private Ville villeDepart = null;
    private Ville villeArrivee = null;
    private double distanceDebutRecherche = 0.0;
    private double distanceFinRecherche = Double.MAX_VALUE;
    private boolean afficherInfoObstacles = true;

    // Limites géographiques de Madagascar
    private double minLon = 43.0;
    private double maxLon = 51.0;
    private double minLat = -26.0;
    private double maxLat = -11.0;

    // Marges
    private final int marginLeft = 30;
    private final int marginRight = 30;
    private final int marginTop = 30;
    private final int marginBottom = 150;

    // État chargement
    private volatile boolean dataLoaded = false;
    private String loadingMessage = "Chargement des données de Madagascar...";

    public Map() {
        villes = new ArrayList<>();
        routes = new ArrayList<>();
        obstacles = new ArrayList<>();

        setPreferredSize(new Dimension(1200, 800));
        setBackground(new Color(200, 220, 255));

        // Lancement immédiat du chargement en thread séparé
        loadDataInBackground();
    }

    private void loadDataInBackground() {
        new Thread(() -> {
            try {
                // Chargement réel
                villes = Ville.chargerVilles();
                routes = Route.chargerRoutes();
                obstacles = Obstacles.chargerObstacles(routes);

                dataLoaded = true;

                SwingUtilities.invokeLater(this::repaint);

                System.out.println("Chargement terminé → " +
                        villes.size() + " villes | " +
                        routes.size() + " routes | " +
                        obstacles.size() + " obstacles");
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    loadingMessage = "Erreur lors du chargement : " + e.getMessage();
                    repaint();
                });
            }
        }, "DataLoader-Map").start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!dataLoaded) {
            drawLoadingScreen(g2d);
            drawLegend(g2d);
            return;
        }

        // ───────────── Mode normal ─────────────

        // Routes
        g2d.setColor(new Color(200, 100, 50));
        g2d.setStroke(new BasicStroke(1.5f));

        if (routeFiltree != null) {
            drawRoute(g2d, routeFiltree);
        } else {
            for (Route r : routes) {
                drawRoute(g2d, r);
            }
        }

        // Obstacles
        List<Obstacles> obstaclesToDraw = (routeFiltree != null) ? obstaclesFiltres : obstacles;
        for (Obstacles obs : obstaclesToDraw) {
            drawObstacle(g2d, obs);
        }

        // Villes
        if (routeFiltree == null) {
            for (Ville v : villes) {
                drawVille(g2d, v);
            }
        } else {
            for (Ville v : villesSurRoute) {
                drawVilleSurRoute(g2d, v);
            }
        }

        // Infos obstacles
        if (afficherInfoObstacles && !obstaclesToDraw.isEmpty()) {
            drawObstaclesInfo(g2d, obstaclesToDraw);
        }

        // Légende (toujours en dernier)
        drawLegend(g2d);
    }

    private void drawLoadingScreen(Graphics2D g2d) {
        // Message principal
        g2d.setColor(new Color(30, 60, 120));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 36));
        FontMetrics fm = g2d.getFontMetrics();
        int w = fm.stringWidth(loadingMessage);
        int x = (getWidth() - w) / 2;
        int y = getHeight() / 2 - 40;
        g2d.drawString(loadingMessage, x, y);

        // Sous-titre
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        fm = g2d.getFontMetrics();
        String sub = "Veuillez patienter quelques instants...";
        w = fm.stringWidth(sub);
        x = (getWidth() - w) / 2;
        g2d.drawString(sub, x, y + 50);

        // Barre de progression animée (style simple)
        int barW = 480;
        int barH = 24;
        int barX = (getWidth() - barW) / 2;
        int barY = y + 140;

        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(barX, barY, barW, barH, 12, 12);

        g2d.setColor(new Color(180, 220, 255));
        g2d.fillRoundRect(barX + 3, barY + 3, barW - 6, barH - 6, 9, 9);

        // Animation sinusoidale
        long time = System.currentTimeMillis();
        double phase = (time % 2800) / 2800.0;
        int progress = (int) (barW * 0.7 * (0.5 + 0.5 * Math.sin(phase * Math.PI * 4)));

        g2d.setColor(new Color(0, 120, 255));
        g2d.fillRoundRect(barX + 6, barY + 6, progress, barH - 12, 6, 6);
    }

    private void drawRoute(Graphics2D g2d, Route route) {
        List<double[]> coords = route.getCoordinates();
        if (coords.size() < 2) return;

        for (int i = 0; i < coords.size() - 1; i++) {
            double[] p1 = coords.get(i);
            double[] p2 = coords.get(i + 1);

            int x1 = lonToX(p1[0]);
            int y1 = latToY(p1[1]);
            int x2 = lonToX(p2[0]);
            int y2 = latToY(p2[1]);

            double dist = Math.hypot(x2 - x1, y2 - y1);
            if (dist < 60) {
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
    }

    private void drawVille(Graphics2D g2d, Ville ville) {
        int x = lonToX(ville.getLongitude());
        int y = latToY(ville.getLatitude());

        String place = ville.getPlace();
        if ("city".equals(place)) {
            g2d.setColor(Color.RED);
            g2d.fillOval(x - 5, y - 5, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawString(ville.getName(), x + 7, y + 4);
        } else if ("town".equals(place)) {
            g2d.setColor(new Color(255, 100, 0));
            g2d.fillOval(x - 4, y - 4, 8, 8);
        } else {
            g2d.setColor(new Color(100, 100, 255));
            g2d.fillOval(x - 3, y - 3, 6, 6);
        }
    }

    private void drawVilleSurRoute(Graphics2D g2d, Ville ville) {
        int x = lonToX(ville.getLongitude());
        int y = latToY(ville.getLatitude());

        if (ville.equals(villeDepart) || ville.equals(villeArrivee)) {
            g2d.setColor(new Color(255, 255, 0, 120));
            g2d.fillOval(x - 12, y - 12, 24, 24);
            g2d.setColor(new Color(255, 215, 0));
            g2d.setStroke(new BasicStroke(2.5f));
            g2d.drawOval(x - 12, y - 12, 24, 24);
        }

        String place = ville.getPlace();
        if ("city".equals(place)) {
            g2d.setColor(Color.RED);
            g2d.fillOval(x - 6, y - 6, 12, 12);
        } else if ("town".equals(place)) {
            g2d.setColor(new Color(255, 100, 0));
            g2d.fillOval(x - 5, y - 5, 10, 10);
        } else {
            g2d.setColor(new Color(100, 100, 255));
            g2d.fillOval(x - 4, y - 4, 8, 8);
        }

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.drawString(ville.getName(), x + 8, y + 4);
    }

    private void drawObstacle(Graphics2D g2d, Obstacles obstacle) {
        List<double[]> seg = obstacle.extraireSegmentObstacle();
        if (seg.size() < 2) return;

        for (int i = 0; i < seg.size() - 1; i++) {
            double[] p1 = seg.get(i);
            double[] p2 = seg.get(i + 1);

            int x1 = lonToX(p1[0]);
            int y1 = latToY(p1[1]);
            int x2 = lonToX(p2[0]);
            int y2 = latToY(p2[1]);

            double dist = Math.hypot(x2 - x1, y2 - y1);
            if (dist < 60) {
                // Contour noir épais
                g2d.setColor(new Color(0, 0, 0, 220));
                g2d.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(x1, y1, x2, y2);

                // Rouge vif
                g2d.setColor(new Color(255, 0, 0));
                g2d.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
    }

    private void drawObstaclesInfo(Graphics2D g2d, List<Obstacles> obsList) {
        int x = 20;
        int y = 20;
        int w = 360;
        int h = Math.min(40 + obsList.size() * 65, 420);

        g2d.setColor(new Color(255, 255, 255, 235));
        g2d.fillRoundRect(x, y, w, h, 16, 16);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(x, y, w, h, 16, 16);

        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        String title = routeFiltree != null
                ? "Obstacles sur " + routeFiltree.getName()
                : "Obstacles (" + obsList.size() + ")";
        g2d.drawString(title, x + 12, y + 24);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        int cy = y + 50;

        for (int i = 0; i < obsList.size() && cy < y + h - 30; i++) {
            Obstacles o = obsList.get(i);

            g2d.setColor(Color.RED);
            g2d.fillOval(x + 12, cy - 10, 14, 14);
            g2d.setColor(Color.BLACK);
            g2d.drawString((i + 1) + ".", x + 14, cy + 4);

            String rInfo = (routeFiltree == null && o.getRoute() != null)
                    ? " (" + o.getRoute().getName() + ")"
                    : "";
            g2d.drawString("De " + String.format("%.1f", o.getDistanceDebut()) + " km" + rInfo,
                    x + 34, cy + 4);
            g2d.drawString("à " + String.format("%.1f", o.getDistanceFin()) + " km",
                    x + 34, cy + 20);

            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Arial", Font.ITALIC, 10));
            g2d.drawString("Longueur : " + String.format("%.1f", o.getDistanceFin() - o.getDistanceDebut()) + " km",
                    x + 34, cy + 36);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.setColor(Color.BLACK);

            cy += 65;
        }

        if (obsList.isEmpty()) {
            g2d.setColor(Color.GRAY);
            g2d.drawString(routeFiltree != null ? "Aucun obstacle sur cette route" : "Aucun obstacle",
                    x + 12, cy + 10);
        }
    }

    private void drawLegend(Graphics2D g2d) {
        int lx = getWidth() - 190;
        int ly = getHeight() - 140;

        g2d.setColor(new Color(255, 255, 255, 210));
        g2d.fillRoundRect(lx - 8, ly - 18, 180, 140, 12, 12);

        g2d.setColor(Color.BLACK);
        g2d.drawString("Légende", lx, ly);

        ly += 22;
        g2d.setColor(Color.RED);
        g2d.fillOval(lx, ly - 6, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Villes principales", lx + 16, ly + 4);

        ly += 22;
        g2d.setColor(new Color(255, 100, 0));
        g2d.fillOval(lx, ly - 5, 8, 8);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Villes secondaires", lx + 16, ly + 4);

        ly += 22;
        g2d.setColor(new Color(100, 100, 255));
        g2d.fillOval(lx, ly - 4, 6, 6);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Autres localités", lx + 16, ly + 4);

        ly += 22;
        g2d.setColor(new Color(200, 100, 50));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(lx, ly, lx + 12, ly);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Routes", lx + 16, ly + 4);

        ly += 22;
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(5f));
        g2d.drawLine(lx, ly, lx + 12, ly);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Obstacles", lx + 16, ly + 4);
    }

    // ────────────────────────────────────────────────
    // Méthodes utilitaires (inchangées)
    // ────────────────────────────────────────────────

    private int lonToX(double lon) {
        int w = getWidth() - marginLeft - marginRight;
        return marginLeft + (int) ((lon - minLon) / (maxLon - minLon) * w);
    }

    private int latToY(double lat) {
        int h = getHeight() - marginTop - marginBottom;
        return marginTop + (int) ((maxLat - lat) / (maxLat - minLat) * h);
    }

    public void ajouterObstacle(Obstacles obstacle) {
        obstacles.add(obstacle);
        if (routeFiltree != null && obstacle.getRoute() != null &&
                obstacle.getRoute().getOgcFid() == routeFiltree.getOgcFid()) {
            obstaclesFiltres.add(obstacle);
        }
        repaint();
    }

    public void filtrerParRoute(Route route) {
        if (route == null) {
            afficherTout();
            return;
        }

        this.routeFiltree = route;
        obstaclesFiltres.clear();
        villesSurRoute.clear();
        villeDepart = null;
        villeArrivee = null;
        distanceDebutRecherche = 0.0;
        distanceFinRecherche = Double.MAX_VALUE;

        // Obstacles de cette route
        for (Obstacles o : obstacles) {
            if (o.getRoute() != null && o.getRoute().getOgcFid() == route.getOgcFid()) {
                obstaclesFiltres.add(o);
            }
        }

        // Villes proches
        double seuil = 0.1;
        for (Ville v : villes) {
            boolean proche = false;
            for (double[] c : route.getCoordinates()) {
                double d = Math.hypot(v.getLongitude() - c[0], v.getLatitude() - c[1]);
                if (d < seuil) {
                    proche = true;
                    break;
                }
            }
            if (proche) villesSurRoute.add(v);
        }

        // Zoom sur la route
        zoomOnRoute(route);

        repaint();
    }

    public void filtrerParRouteAvecVilles(Route route, Ville depart, Ville arrivee,
                                          double distDebut, double distFin) {
        if (route == null) {
            afficherTout();
            return;
        }

        this.routeFiltree = route;
        this.villeDepart = depart;
        this.villeArrivee = arrivee;
        this.distanceDebutRecherche = distDebut;
        this.distanceFinRecherche = distFin;

        obstaclesFiltres.clear();
        villesSurRoute.clear();

        // Obstacles dans l'intervalle
        for (Obstacles o : obstacles) {
            if (o.getRoute() != null && o.getRoute().getOgcFid() == route.getOgcFid()) {
                if (o.getDistanceDebut() <= distFin && o.getDistanceFin() >= distDebut) {
                    obstaclesFiltres.add(o);
                }
            }
        }

        // Villes (même filtre que précédemment)
        double seuil = 0.1;
        for (Ville v : villes) {
            boolean proche = false;
            for (double[] c : route.getCoordinates()) {
                double d = Math.hypot(v.getLongitude() - c[0], v.getLatitude() - c[1]);
                if (d < seuil) {
                    proche = true;
                    break;
                }
            }
            if (proche) villesSurRoute.add(v);
        }

        zoomOnRoute(route);

        repaint();
    }

    private void zoomOnRoute(Route route) {
        double ml = Double.MAX_VALUE, Ml = Double.MIN_VALUE;
        double ma = Double.MAX_VALUE, Ma = Double.MIN_VALUE;

        for (double[] c : route.getCoordinates()) {
            ml = Math.min(ml, c[0]);
            Ml = Math.max(Ml, c[0]);
            ma = Math.min(ma, c[1]);
            Ma = Math.max(Ma, c[1]);
        }

        double margeLon = (Ml - ml) * 0.15;
        double margeLat = (Ma - ma) * 0.15;

        this.minLon = ml - margeLon;
        this.maxLon = Ml + margeLon;
        this.minLat = ma - margeLat;
        this.maxLat = Ma + margeLat;
    }

    public void afficherTout() {
        routeFiltree = null;
        obstaclesFiltres.clear();
        villesSurRoute.clear();
        villeDepart = null;
        villeArrivee = null;
        distanceDebutRecherche = 0.0;
        distanceFinRecherche = Double.MAX_VALUE;

        // Reset bornes
        minLon = 43.0;
        maxLon = 51.0;
        minLat = -26.0;
        maxLat = -11.0;

        repaint();
    }

    public void toggleInfoObstacles() {
        afficherInfoObstacles = !afficherInfoObstacles;
        repaint();
    }

    public List<Ville> getVilles() {
        return villes;
    }

    public List<Route> getRoutes() {
        return routes;
    }
}