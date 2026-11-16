package conectacampus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Grafo simple no dirigido. Adyacencias propias e indices estables en cada vertice. */
public final class RedSocial {
    public record Usuario(String id, String nombre) {
        @Override public String toString() { return id + " " + nombre; }
    }
    public record Amistad(Usuario a, Usuario b) { }
    private static final class Enlace {
        final int destino;
        final Enlace siguiente;
        Enlace(int destino, Enlace siguiente) {
            this.destino = destino;
            this.siguiente = siguiente;
        }
    }
    private static final class Vertice {
        final Usuario usuario;
        Enlace primero;
        int grado;
        Vertice(Usuario usuario) { this.usuario = usuario; }
    }
    private final ArrayList<Vertice> vertices = new ArrayList<>();
    private int aristas;

    private static String normalizar(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("El ID es obligatorio.");
        String valor = id.strip().toUpperCase(Locale.ROOT);
        if (!valor.matches("[A-Z][A-Z0-9_-]{0,19}")) {
            throw new IllegalArgumentException("ID: una letra inicial y hasta 20 letras, numeros, _ o -.");
        }
        return valor;
    }
    private int buscar(String id) {
        String clave = normalizar(id);
        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i).usuario.id().equals(clave)) return i;
        }
        return -1;
    }
    private int exigir(String id) {
        int i = buscar(id);
        if (i < 0) throw new IllegalArgumentException("R1: el usuario " + id + " no existe.");
        return i;
    }
    public void registrarPersona(String id, String nombre) {
        String clave = normalizar(id);
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("R7: el nombre es obligatorio.");
        String limpio = nombre.strip();
        if (limpio.length() > 60 || limpio.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Nombre: maximo 60 caracteres, sin controles.");
        }
        if (buscar(clave) >= 0) throw new IllegalArgumentException("R6: el ID ya esta registrado.");
        vertices.add(new Vertice(new Usuario(clave, limpio)));
    }
    private boolean contiene(int a, int b) {
        for (Enlace e = vertices.get(a).primero; e != null; e = e.siguiente) {
            if (e.destino == b) return true;
        }
        return false;
    }
    public void proponerAmistad(String idA, String idB) {
        int a = exigir(idA), b = exigir(idB);
        if (a == b) throw new IllegalArgumentException("R2: no se permiten amistades consigo mismo.");
        if (contiene(a, b)) throw new IllegalArgumentException("R4: esta amistad ya existe.");
        Vertice va = vertices.get(a), vb = vertices.get(b);
        va.primero = new Enlace(b, va.primero);
        vb.primero = new Enlace(a, vb.primero);
        va.grado++;
        vb.grado++;
        aristas++;
    }
    public boolean existeRelacion(String a, String b) { return contiene(exigir(a), exigir(b)); }
    public int grado(String id) { return vertices.get(exigir(id)).grado; }
    public int numeroUsuarios() { return vertices.size(); }
    public int numeroAmistades() { return aristas; }
    public List<Usuario> usuarios() {
        return vertices.stream().map(v -> v.usuario).toList();
    }
    public List<Usuario> vecinos(String id) {
        ArrayList<Usuario> lista = new ArrayList<>();
        for (Enlace e = vertices.get(exigir(id)).primero; e != null; e = e.siguiente) {
            lista.add(vertices.get(e.destino).usuario);
        }
        return List.copyOf(lista);
    }
    public List<Amistad> amistades() {
        ArrayList<Amistad> lista = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            for (Enlace e = vertices.get(i).primero; e != null; e = e.siguiente) {
                if (i < e.destino) lista.add(new Amistad(vertices.get(i).usuario, vertices.get(e.destino).usuario));
            }
        }
        return List.copyOf(lista);
    }
    public double densidad() {
        int n = vertices.size();
        return n < 2 ? 0.0 : aristas / ((double) n * (n - 1) / 2);
    }
    public int[][] matrizAdyacencia() {
        int[][] matriz = new int[vertices.size()][vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            for (Enlace e = vertices.get(i).primero; e != null; e = e.siguiente) matriz[i][e.destino] = 1;
        }
        return matriz;
    }
    public List<Usuario> aislados() { return vertices.stream().filter(v -> v.grado == 0).map(v -> v.usuario).toList(); }
    public int gradoMaximo() { return vertices.stream().mapToInt(v -> v.grado).max().orElse(0); }
    public List<Usuario> masConectados() {
        int max = gradoMaximo();
        return vertices.stream().filter(v -> v.grado == max).map(v -> v.usuario).toList();
    }
    public int sumaGrados() { return vertices.stream().mapToInt(v -> v.grado).sum(); }

    /** BFS iterativo: cada usuario entra una sola vez en la cola. */
    public List<List<Usuario>> componentes() {
        ArrayList<List<Usuario>> grupos = new ArrayList<>();
        boolean[] visto = new boolean[vertices.size()];
        int[] cola = new int[vertices.size()];
        for (int inicio = 0; inicio < vertices.size(); inicio++) {
            if (visto[inicio]) continue;
            ArrayList<Usuario> grupo = new ArrayList<>();
            int frente = 0, fin = 0;
            cola[fin++] = inicio;
            visto[inicio] = true;
            while (frente < fin) {
                int u = cola[frente++];
                grupo.add(vertices.get(u).usuario);
                for (Enlace e = vertices.get(u).primero; e != null; e = e.siguiente) {
                    if (!visto[e.destino]) {
                        visto[e.destino] = true;
                        cola[fin++] = e.destino;
                    }
                }
            }
            grupos.add(List.copyOf(grupo));
        }
        return List.copyOf(grupos);
    }

    /** DFS con pila explicita. El primer y ultimo usuario del ciclo coinciden. */
    public List<Usuario> buscarCiclo() {
        int n = vertices.size();
        int[] estado = new int[n], padre = new int[n], pila = new int[n];
        Arrays.fill(padre, -1);
        Enlace[] pendiente = new Enlace[n];
        for (int raiz = 0; raiz < n; raiz++) {
            if (estado[raiz] != 0) continue;
            int tope = 0;
            pila[0] = raiz;
            estado[raiz] = 1;
            pendiente[raiz] = vertices.get(raiz).primero;
            while (tope >= 0) {
                int u = pila[tope];
                Enlace e = pendiente[u];
                if (e == null) { estado[u] = 2; tope--; continue; }
                pendiente[u] = e.siguiente;
                int v = e.destino;
                if (v == padre[u]) continue;
                if (estado[v] == 0) {
                    padre[v] = u;
                    estado[v] = 1;
                    pendiente[v] = vertices.get(v).primero;
                    pila[++tope] = v;
                } else if (estado[v] == 1) {
                    ArrayList<Usuario> ciclo = new ArrayList<>();
                    for (int x = u; x != v; x = padre[x]) ciclo.add(vertices.get(x).usuario);
                    ciclo.add(vertices.get(v).usuario);
                    Collections.reverse(ciclo);
                    ciclo.add(vertices.get(v).usuario);
                    return List.copyOf(ciclo);
                }
            }
        }
        return List.of();
    }

    /** Camino con el menor numero de amistades; vacio si no hay conexion. */
    public List<Usuario> caminoMinimo(String origen, String destino) {
        int a = exigir(origen), b = exigir(destino), n = vertices.size();
        int[] padre = new int[n], cola = new int[n];
        Arrays.fill(padre, -1);
        padre[a] = a;
        int frente = 0, fin = 0;
        cola[fin++] = a;
        while (frente < fin && padre[b] == -1) {
            int u = cola[frente++];
            for (Enlace e = vertices.get(u).primero; e != null; e = e.siguiente) {
                if (padre[e.destino] == -1) {
                    padre[e.destino] = u;
                    cola[fin++] = e.destino;
                }
            }
        }
        if (padre[b] == -1) return List.of();
        ArrayList<Usuario> camino = new ArrayList<>();
        for (int x = b; x != a; x = padre[x]) camino.add(vertices.get(x).usuario);
        camino.add(vertices.get(a).usuario);
        Collections.reverse(camino);
        return List.copyOf(camino);
    }
    public static RedSocial ejemplo() {
        RedSocial red = new RedSocial();
        String[] nombres = {"Valeria", "Mateo", "Sofia", "Diego", "Elena", "Bruno"};
        for (int i = 0; i < nombres.length; i++) red.registrarPersona("C" + (i + 1), nombres[i]);
        for (String[] par : new String[][]{{"C1","C2"},{"C2","C3"},{"C3","C4"},{"C4","C1"},{"C1","C5"}}) {
            red.proponerAmistad(par[0], par[1]);
        }
        return red;
    }
}
