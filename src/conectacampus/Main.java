package conectacampus;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/** Interfaz de consola; el modelo no imprime ni depende de Scanner. */
public final class Main {
    private Main() { }
    public static void main(String[] args) {
        RedSocial red = RedSocial.ejemplo();
        if (args.length == 1 && args[0].equals("--demo")) { informe(red); return; }
        if (args.length > 0) {
            System.err.println("Uso: java -cp bin conectacampus.Main [--demo]");
            System.exit(2);
        }
        try (Scanner entrada = new Scanner(System.in)) {
            System.out.println("CONECTA CAMPUS | Laboratorio de redes de amistad");
            while (true) {
                System.out.println("\n1 Registrar usuario  | 2 Crear amistad | 3 Consultar amistad");
                System.out.println("4 Vecinos y grado   | 5 Informe       | 6 Camino minimo");
                System.out.println("7 Restablecer demo  | 8 Red vacia     | 0 Salir");
                String opcion = leer(entrada, "Opcion: ");
                if (opcion == null || opcion.equals("0")) break;
                try {
                    switch (opcion) {
                        case "1" -> {
                            red.registrarPersona(requerido(entrada, "ID: "), requerido(entrada, "Nombre: "));
                            System.out.println("Usuario registrado.");
                        }
                        case "2" -> {
                            red.proponerAmistad(requerido(entrada, "Primer ID: "), requerido(entrada, "Segundo ID: "));
                            System.out.println("Amistad creada en ambos sentidos.");
                        }
                        case "3" -> System.out.println(red.existeRelacion(requerido(entrada, "Primer ID: "), requerido(entrada, "Segundo ID: "))
                                ? "Si existe amistad directa." : "No existe amistad directa.");
                        case "4" -> {
                            String id = requerido(entrada, "ID: ");
                            System.out.println("Vecinos: " + red.vecinos(id) + " | Grado: " + red.grado(id));
                        }
                        case "5" -> informe(red);
                        case "6" -> {
                            List<RedSocial.Usuario> camino = red.caminoMinimo(requerido(entrada, "Origen: "), requerido(entrada, "Destino: "));
                            System.out.println(camino.isEmpty() ? "No existe camino entre estos usuarios." : recorrido(camino) + " | Saltos: " + (camino.size() - 1));
                        }
                        case "7" -> { red = RedSocial.ejemplo(); System.out.println("Ejemplo restablecido."); }
                        case "8" -> { red = new RedSocial(); System.out.println("Red vacia iniciada."); }
                        default -> System.out.println("Opcion no valida. Use un numero de 0 a 8.");
                    }
                } catch (FinEntrada e) {
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("Validacion: " + e.getMessage());
                }
            }
            System.out.println("Sesion finalizada. Los datos solo se conservan durante la ejecucion.");
        }
    }
    private static String leer(Scanner sc, String texto) {
        System.out.print(texto);
        return sc.hasNextLine() ? sc.nextLine().strip() : null;
    }
    private static String requerido(Scanner sc, String texto) {
        String valor = leer(sc, texto);
        if (valor == null) throw new FinEntrada();
        return valor;
    }
    private static final class FinEntrada extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
    private static String recorrido(List<RedSocial.Usuario> lista) {
        return String.join(" -> ", lista.stream().map(u -> u.id() + " " + u.nombre()).toList());
    }
    public static void informe(RedSocial red) {
        System.out.println("\n=== CONECTA CAMPUS: INFORME DE LA RED ACTUAL ===");
        System.out.println("1. V = " + red.usuarios());
        System.out.println("   E = " + red.amistades().stream().map(a -> "{" + a.a().id() + ", " + a.b().id() + "}").toList());
        System.out.println("   Usuarios: " + red.numeroUsuarios() + " | Amistades: " + red.numeroAmistades());
        System.out.println("2. Grafo simple, no dirigido y no ponderado.");
        int[][] matriz = red.matrizAdyacencia();
        List<RedSocial.Usuario> usuarios = red.usuarios();
        int ancho = usuarios.stream().mapToInt(u -> u.id().length()).max().orElse(2) + 2;
        System.out.printf("%" + ancho + "s", "");
        usuarios.forEach(u -> System.out.printf("%" + ancho + "s", u.id()));
        System.out.println();
        for (int i = 0; i < matriz.length; i++) {
            System.out.printf("%" + ancho + "s", usuarios.get(i).id());
            for (int valor : matriz[i]) System.out.printf("%" + ancho + "d", valor);
            System.out.println();
        }
        System.out.printf(Locale.ROOT, "   Densidad: %.2f %%%n", red.densidad() * 100);
        System.out.println("3. Grados y listas de adyacencia:");
        for (var u : usuarios) System.out.println("   " + u + " | grado " + red.grado(u.id()) + " | " + red.vecinos(u.id()));
        System.out.println("   Aislados: " + red.aislados());
        System.out.println("   Mayor grado: " + red.gradoMaximo() + " | Usuarios: " + red.masConectados());
        System.out.println("   Suma de grados: " + red.sumaGrados() + " = 2 x " + red.numeroAmistades());
        var ciclo = red.buscarCiclo();
        System.out.println("4. Ciclo: " + (ciclo.isEmpty() ? "ninguno" : recorrido(ciclo)));
        var grupos = red.componentes();
        System.out.println("   Componentes (" + grupos.size() + "): " + grupos);
        System.out.println(usuarios.isEmpty() ? "   Red vacia: no hay usuarios que conectar." : grupos.size() == 1 ? "   Red conexa." : "   Red desconectada.");
        System.out.println("5. Consulte amistad, vecinos y caminos en las opciones 3, 4 y 6.");
        System.out.println("6. Validaciones: extremos existentes, sin lazos, sin duplicados, ID unico y nombre valido.");
    }
}
