package conectacampus;

import java.util.Arrays;
import java.util.List;

/** Pruebas sin librerias externas. Cualquier fallo termina con codigo distinto de cero. */
public final class RedSocialTest {
    private static int comprobaciones;
    private static void verificar(boolean condicion, String mensaje) {
        comprobaciones++;
        if (!condicion) throw new AssertionError(mensaje);
    }
    private static void rechaza(Runnable accion) {
        boolean rechazado = false;
        try { accion.run(); } catch (IllegalArgumentException e) { rechazado = true; }
        verificar(rechazado, "Se esperaba un rechazo");
    }
    public static void main(String[] args) {
        RedSocial r = RedSocial.ejemplo();
        verificar(r.numeroUsuarios() == 6 && r.numeroAmistades() == 5, "Conteos del ejemplo");
        verificar(r.grado(" c1 ") == 3 && r.grado("C6") == 0, "Grados");
        verificar(r.componentes().size() == 2, "Componentes");
        verificar(r.buscarCiclo().size() == 5, "Ciclo de cuatro vertices");
        verificar(Math.abs(r.densidad() - 1.0 / 3) < 1e-12, "Densidad");
        verificar(r.sumaGrados() == 10, "Lema del apreton de manos");
        verificar(r.existeRelacion("C1", "C2") && r.existeRelacion("C2", "C1"), "Reciprocidad");
        verificar(!r.existeRelacion("C1", "C3"), "No confundir camino con amistad directa");
        verificar(r.caminoMinimo("C5", "C3").size() == 4, "Camino minimo de 3 saltos");
        verificar(r.caminoMinimo("C1", "C6").isEmpty(), "Aislado inalcanzable");
        verificar(r.caminoMinimo("C6", "C6").size() == 1, "Camino de longitud cero");
        int[][] antes = r.matrizAdyacencia();
        rechaza(() -> r.proponerAmistad("C1", "C1"));
        rechaza(() -> r.proponerAmistad("C2", "C1"));
        rechaza(() -> r.proponerAmistad("C1", "C9"));
        rechaza(() -> r.registrarPersona("c1", "Otro"));
        rechaza(() -> r.registrarPersona("C7", "   "));
        rechaza(() -> r.registrarPersona(null, "Otro"));
        rechaza(() -> r.registrarPersona("", "Otro"));
        rechaza(() -> r.registrarPersona("C 7", "Otro"));
        rechaza(() -> r.registrarPersona("C7", null));
        rechaza(() -> r.registrarPersona("C7", "A\nB"));
        rechaza(() -> r.vecinos("C9"));
        rechaza(() -> r.grado("C9"));
        rechaza(() -> r.existeRelacion("C9", "C1"));
        rechaza(() -> r.caminoMinimo("C1", "C9"));
        verificar(Arrays.deepEquals(antes, r.matrizAdyacencia()) && r.numeroUsuarios() == 6 && r.numeroAmistades() == 5,
                "Rechazos no deben mutar el grafo");
        antes[0][1] = 0;
        verificar(r.existeRelacion("C1", "C2"), "La matriz devuelta no expone estado interno");
        r.registrarPersona(" c7 ", "Bruno");
        r.proponerAmistad("C7", "C6");
        verificar(r.grado("C6") == 1 && r.numeroAmistades() == 6, "Alta valida");
        verificar(r.vecinos("C6").get(0).id().equals("C7"), "Homonymos identificados por ID");
        RedSocial vacia = new RedSocial();
        verificar(vacia.gradoMaximo() == 0 && vacia.masConectados().isEmpty(), "Maximo en vacio");
        verificar(vacia.componentes().isEmpty() && vacia.buscarCiclo().isEmpty(), "Recorridos en vacio");
        verificar(vacia.densidad() == 0 && vacia.matrizAdyacencia().length == 0, "Matriz y densidad vacias");
        vacia.registrarPersona("X", "Uno");
        verificar(vacia.densidad() == 0 && vacia.componentes().size() == 1, "Un vertice");
        int casos = exhaustivo();
        System.out.println("OK: " + comprobaciones + " comprobaciones; " + casos + " grafos de 5 vertices revisados.");
    }
    private static int exhaustivo() {
        int n = 5, casos = 1 << (n * (n - 1) / 2);
        for (int mascara = 0; mascara < casos; mascara++) {
            RedSocial r = new RedSocial();
            boolean[][] ady = new boolean[n][n];
            int[][] dist = new int[n][n];
            for (int i = 0; i < n; i++) {
                r.registrarPersona("V" + i, "Persona " + i);
                Arrays.fill(dist[i], 99);
                dist[i][i] = 0;
            }
            int bit = 0, aristas = 0;
            for (int i = 0; i < n; i++) for (int j = i + 1; j < n; j++, bit++) {
                if ((mascara & (1 << bit)) != 0) {
                    r.proponerAmistad("V" + i, "V" + j);
                    ady[i][j] = ady[j][i] = true;
                    dist[i][j] = dist[j][i] = 1;
                    aristas++;
                }
            }
            // Oraculo independiente: Floyd-Warshall sobre la matriz de entrada.
            for (int k = 0; k < n; k++) for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) {
                dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
            }
            int componentes = 0;
            for (int i = 0; i < n; i++) {
                boolean nuevo = true;
                for (int j = 0; j < i; j++) if (dist[i][j] < 99) nuevo = false;
                if (nuevo) componentes++;
            }
            verificar(r.componentes().size() == componentes, "Componentes contra oraculo");
            List<RedSocial.Usuario> ciclo = r.buscarCiclo();
            verificar(!ciclo.isEmpty() == (aristas > n - componentes), "Existencia de ciclo");
            if (!ciclo.isEmpty()) {
                verificar(ciclo.size() >= 4 && ciclo.get(0).equals(ciclo.get(ciclo.size() - 1)), "Ciclo cerrado");
                verificar(ciclo.subList(0, ciclo.size() - 1).stream().distinct().count() == ciclo.size() - 1, "Ciclo simple");
                for (int i = 1; i < ciclo.size(); i++) verificar(r.existeRelacion(ciclo.get(i-1).id(), ciclo.get(i).id()), "Arista del ciclo");
            }
            int[][] matriz = r.matrizAdyacencia();
            verificar(r.numeroAmistades() == aristas && r.sumaGrados() == 2 * aristas, "Conteos");
            for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) {
                verificar(matriz[i][j] == (ady[i][j] ? 1 : 0), "Matriz contra entrada");
                var camino = r.caminoMinimo("V" + i, "V" + j);
                verificar(dist[i][j] == 99 ? camino.isEmpty() : camino.size() == dist[i][j] + 1, "Distancia minima");
                if (!camino.isEmpty()) {
                    verificar(camino.get(0).id().equals("V" + i) && camino.get(camino.size()-1).id().equals("V" + j), "Extremos");
                    for (int k = 1; k < camino.size(); k++) verificar(r.existeRelacion(camino.get(k-1).id(), camino.get(k).id()), "Arista del camino");
                }
            }
        }
        return casos;
    }
}
