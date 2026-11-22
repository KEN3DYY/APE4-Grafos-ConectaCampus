public final class ConexionAmbato {
    private ConexionAmbato() {}

    static RedSocial inicializar() {
        RedSocial red = new RedSocial();
        String[] nombres = {"Sofía", "Mateo", "Valeria", "Andrés", "Camila", "Diego"};
        for (int i = 0; i < nombres.length; i++) {
            exigir(red.registrarPersona("A0" + (i + 1), nombres[i]), "registro inicial");
        }
        String[][] amistades = {{"A01", "A02"}, {"A02", "A03"}, {"A03", "A04"},
                {"A04", "A01"}, {"A04", "A05"}};
        for (String[] par : amistades) {
            exigir(red.agregarAmistad(par[0], par[1]), "amistad inicial");
        }
        return red;
    }

    private static void exigir(boolean condicion, String operacion) {
        if (!condicion) throw new IllegalStateException("Falló " + operacion);
    }

    public static void main(String[] args) {
        RedSocial red = inicializar();
        System.out.println("CONEXIÓN AMBATO | APE 4 FASE 2");
        System.out.println("1. registrarPersona A01–A06 y mostrarRed");
        System.out.println(red.mostrarRed());
        System.out.print("Grados A01–A06:");
        for (int i = 1; i <= 6; i++) System.out.print(" " + red.contarAmigos("A0" + i));
        System.out.println();
        System.out.println(red.resumen());
        exigir(red.seguir("A02", "A01"), "seguir(A02,A01)");
        System.out.println("2. seguir(A02,A01) -> true");
        System.out.println("Seguidores de Sofía: " + red.listarSeguidores("A01"));
        System.out.println(red.resumen());
        exigir(red.seguir("A06", "A01"), "seguir(A06,A01)");
        System.out.println("3. seguir(A06,A01) -> true");
        System.out.println("Seguidos de Diego: " + red.listarSeguidos("A06"));
        System.out.println("Seguidores de Sofía: " + red.listarSeguidores("A01"));
        System.out.println("Amigos de Diego: " + red.contarAmigos("A06"));
        System.out.println(red.resumen());
        System.out.println("4. listarAmigos(A04) -> " + red.listarAmigos("A04"));
        System.out.println(red.resumen());
        exigir(red.eliminarAmistad("A04", "A05"), "eliminarAmistad(A04,A05)");
        System.out.println("5. eliminarAmistad(A04,A05) -> true");
        System.out.println("Amigos de Andrés: " + red.listarAmigos("A04"));
        System.out.println("Amigos de Camila: " + red.contarAmigos("A05"));
        System.out.println(red.resumen());
        exigir(red.eliminarPersona("A01"), "eliminarPersona(A01)");
        System.out.println("6. eliminarPersona(A01) -> true");
        System.out.println(red.mostrarRed());
        System.out.println(red.resumen());
        boolean duplicado = red.registrarPersona("A02", "Otro nombre");
        exigir(!duplicado, "rechazo del duplicado");
        System.out.println("7. registrarPersona(A02, Otro nombre) -> " + duplicado);
        System.out.println(red.resumen());
        boolean lazo = red.agregarAmistad("A03", "A03");
        exigir(!lazo, "rechazo del lazo");
        System.out.println("8. agregarAmistad(A03,A03) -> " + lazo);
        System.out.println(red.resumen());
    }
}
