import java.util.Locale;
import java.util.StringJoiner;

public final class RedSocial {
    private enum Tipo { AMISTAD, SEGUIR }

    private static final class Vertice {
        final String usuario;
        final String nombre;
        Vertice siguiente;
        Arista aristas;

        Vertice(String usuario, String nombre, Vertice siguiente) {
            this.usuario = usuario;
            this.nombre = nombre;
            this.siguiente = siguiente;
        }
    }

    private static final class Arista {
        final Vertice destino;
        final Tipo tipo;
        Arista siguiente;

        Arista(Vertice destino, Tipo tipo, Arista siguiente) {
            this.destino = destino;
            this.tipo = tipo;
            this.siguiente = siguiente;
        }
    }

    private Vertice cabeza;

    private static String clave(String usuario) {
        if (usuario == null || usuario.isEmpty()
                || usuario.indexOf(',') >= 0
                || usuario.codePoints().anyMatch(c -> Character.isWhitespace(c)
                        || Character.isSpaceChar(c))) {
            return null;
        }
        return usuario.toLowerCase(Locale.ROOT);
    }

    private Vertice buscar(String usuario) {
        String id = clave(usuario);
        for (Vertice v = cabeza; id != null && v != null; v = v.siguiente) {
            if (v.usuario.equals(id)) return v;
        }
        return null;
    }

    public boolean registrarPersona(String usuario, String nombre) {
        String id = clave(usuario);
        if (id == null || nombre == null || nombre.isBlank() || buscar(usuario) != null) {
            return false;
        }
        cabeza = new Vertice(id, nombre.strip(), cabeza);
        return true;
    }

    public boolean eliminarPersona(String usuario) {
        Vertice objetivo = buscar(usuario);
        if (objetivo == null) return false;
        Vertice anterior = null;
        for (Vertice v = cabeza; v != null; v = v.siguiente) {
            quitar(v, objetivo, null);
            if (v.siguiente == objetivo) anterior = v;
        }
        if (anterior == null) cabeza = objetivo.siguiente;
        else anterior.siguiente = objetivo.siguiente;
        objetivo.aristas = null;
        objetivo.siguiente = null;
        return true;
    }

    private static boolean existe(Vertice origen, Vertice destino, Tipo tipo) {
        for (Arista a = origen.aristas; a != null; a = a.siguiente) {
            if (a.destino == destino && a.tipo == tipo) return true;
        }
        return false;
    }

    private static boolean quitar(Vertice origen, Vertice destino, Tipo tipo) {
        boolean eliminada = false;
        Arista anterior = null;
        for (Arista a = origen.aristas; a != null; a = a.siguiente) {
            if (a.destino == destino && (tipo == null || tipo == a.tipo)) {
                if (anterior == null) origen.aristas = a.siguiente;
                else anterior.siguiente = a.siguiente;
                eliminada = true;
            } else {
                anterior = a;
            }
        }
        return eliminada;
    }

    private boolean agregar(String a, String b, Tipo tipo) {
        Vertice origen = buscar(a);
        Vertice destino = buscar(b);
        if (origen == null || destino == null || origen == destino
                || existe(origen, destino, tipo)) return false;
        origen.aristas = new Arista(destino, tipo, origen.aristas);
        if (tipo == Tipo.AMISTAD) {
            destino.aristas = new Arista(origen, tipo, destino.aristas);
        }
        return true;
    }

    public boolean agregarAmistad(String a, String b) {
        return agregar(a, b, Tipo.AMISTAD);
    }

    public boolean seguir(String a, String b) {
        return agregar(a, b, Tipo.SEGUIR);
    }

    public boolean eliminarAmistad(String a, String b) {
        Vertice origen = buscar(a);
        Vertice destino = buscar(b);
        if (origen == null || destino == null || !quitar(origen, destino, Tipo.AMISTAD)) {
            return false;
        }
        quitar(destino, origen, Tipo.AMISTAD);
        return true;
    }

    public boolean dejarDeSeguir(String a, String b) {
        Vertice origen = buscar(a);
        Vertice destino = buscar(b);
        return origen != null && destino != null && quitar(origen, destino, Tipo.SEGUIR);
    }

    private static String listar(Vertice v, Tipo tipo) {
        StringJoiner nombres = new StringJoiner(", ");
        for (Arista a = v.aristas; a != null; a = a.siguiente) {
            if (a.tipo == tipo) nombres.add(a.destino.nombre);
        }
        return nombres.toString();
    }

    public String listarAmigos(String usuario) {
        Vertice v = buscar(usuario);
        return v == null ? null : listar(v, Tipo.AMISTAD);
    }

    public String listarSeguidos(String usuario) {
        Vertice v = buscar(usuario);
        return v == null ? null : listar(v, Tipo.SEGUIR);
    }

    public String listarSeguidores(String usuario) {
        Vertice destino = buscar(usuario);
        if (destino == null) return null;
        StringJoiner nombres = new StringJoiner(", ");
        for (Vertice v = cabeza; v != null; v = v.siguiente) {
            if (existe(v, destino, Tipo.SEGUIR)) nombres.add(v.nombre);
        }
        return nombres.toString();
    }

    public int contarAmigos(String usuario) {
        Vertice v = buscar(usuario);
        if (v == null) return -1;
        int total = 0;
        for (Arista a = v.aristas; a != null; a = a.siguiente) {
            if (a.tipo == Tipo.AMISTAD) total++;
        }
        return total;
    }

    public String mostrarRed() {
        StringJoiner salida = new StringJoiner(System.lineSeparator());
        for (Vertice v = cabeza; v != null; v = v.siguiente) {
            salida.add(v.usuario + " " + v.nombre + " | amigos: [" + listar(v, Tipo.AMISTAD)
                    + "] | sigue a: [" + listar(v, Tipo.SEGUIR) + "]");
        }
        return salida.toString();
    }

    public Resumen resumen() {
        int usuarios = 0;
        int aristasAmistad = 0;
        int seguimientos = 0;
        for (Vertice v = cabeza; v != null; v = v.siguiente) {
            usuarios++;
            for (Arista a = v.aristas; a != null; a = a.siguiente) {
                if (a.tipo == Tipo.AMISTAD) aristasAmistad++;
                else seguimientos++;
            }
        }
        return new Resumen(usuarios, aristasAmistad / 2, seguimientos,
                aristasAmistad + seguimientos);
    }

    public static final class Resumen {
        public final int usuarios;
        public final int amistades;
        public final int seguimientos;
        public final int aristas;

        private Resumen(int usuarios, int amistades, int seguimientos, int aristas) {
            this.usuarios = usuarios;
            this.amistades = amistades;
            this.seguimientos = seguimientos;
            this.aristas = aristas;
        }

        @Override
        public String toString() {
            return usuarios + " usuarios | " + amistades + " amistades | "
                    + seguimientos + " seguimientos | " + aristas + " aristas";
        }
    }
}
