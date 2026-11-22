public final class PruebasRedSocial {
    private static int aprobadas;

    private static void verificar(boolean condicion, String descripcion) {
        if (!condicion) throw new AssertionError(descripcion);
        aprobadas++;
    }

    private static void estado(RedSocial red, int u, int a, int s, String etapa) {
        RedSocial.Resumen actual = red.resumen();
        verificar(actual.usuarios == u && actual.amistades == a
                && actual.seguimientos == s && actual.aristas == a * 2 + s, etapa);
    }

    private static void escenarioGuia() {
        RedSocial red = ConexionAmbato.inicializar();
        estado(red, 6, 5, 0, "Estado inicial");
        int[] grados = {2, 2, 2, 3, 1, 0};
        for (int i = 0; i < grados.length; i++) {
            verificar(red.contarAmigos("A0" + (i + 1)) == grados[i], "Grado inicial A0" + (i + 1));
        }
        verificar(red.seguir("A02", "A01"), "Mateo sigue a Sofía");
        verificar("Mateo".equals(red.listarSeguidores("A01")), "Primer seguidor");
        estado(red, 6, 5, 1, "Estado tras primer seguimiento");
        verificar(red.seguir("A06", "A01"), "Diego sigue a Sofía");
        verificar("Diego, Mateo".equals(red.listarSeguidores("A01")), "Seguidores de Sofía");
        verificar("Sofía".equals(red.listarSeguidos("A06")), "Seguidos de Diego");
        verificar(red.listarSeguidores("A06").isEmpty(), "Sin reciprocidad automática");
        estado(red, 6, 5, 2, "Estado tras segundo seguimiento");
        verificar("Camila, Sofía, Valeria".equals(red.listarAmigos("A04")), "Amigos de Andrés");
        verificar(red.eliminarAmistad("A04", "A05"), "Baja de amistad");
        verificar(red.listarAmigos("A05").isEmpty(), "Baja en el sentido inverso");
        estado(red, 6, 4, 2, "Estado tras baja de amistad");
        verificar(red.eliminarPersona("A01"), "Baja de Sofía");
        verificar(red.listarSeguidores("A01") == null, "Nodo ausente");
        verificar(red.listarSeguidos("A02").isEmpty(), "Elimina seguimiento de Mateo");
        verificar(red.listarSeguidos("A06").isEmpty(), "Elimina seguimiento de Diego");
        verificar("Valeria".equals(red.listarAmigos("A02")), "Conserva amistad no relacionada");
        verificar(!red.mostrarRed().contains("Sofía"), "No quedan referencias a Sofía");
        verificar(!red.registrarPersona("a02", "Otro nombre"), "Duplicado sin distinción de caso");
        verificar(!red.agregarAmistad("A03", "a03"), "Lazo de amistad");
        estado(red, 5, 2, 0, "Estado final");
    }

    private static void validaciones() {
        RedSocial red = new RedSocial();
        for (String id : new String[]{null, "", " ", " P", "P ", "P Q", "P,Q", "P\tQ", "P\u00A0Q"}) {
            verificar(!red.registrarPersona(id, "Persona"), "Identificador inválido");
        }
        verificar(!red.registrarPersona("P", null), "Nombre nulo");
        verificar(!red.registrarPersona("P", "  "), "Nombre vacío");
        verificar(red.mostrarRed().isEmpty(), "Red vacía");
        verificar(red.contarAmigos("X") == -1, "Conteo para ausente");
        verificar(red.listarAmigos("X") == null, "Lista de amistades para ausente");
        verificar(red.listarSeguidos("X") == null, "Lista de seguidos para ausente");
        verificar(!red.eliminarPersona("X"), "Eliminar ausente");
        verificar(red.registrarPersona("P", "Paz"), "Primer registro");
        verificar(red.registrarPersona("Q", "Quino"), "Segundo registro");
        verificar(!red.agregarAmistad("P", "X"), "Amistad con ausente");
        verificar(!red.seguir("X", "Q"), "Seguir desde ausente");
        verificar(!red.seguir("P", "P"), "Seguir a sí mismo");
        verificar(!red.eliminarAmistad("P", "Q"), "Amistad inexistente");
        verificar(!red.dejarDeSeguir("P", "Q"), "Seguimiento inexistente");
        verificar(red.agregarAmistad("P", "Q"), "Amistad válida");
        verificar(!red.agregarAmistad("Q", "P"), "Amistad duplicada inversa");
        verificar(red.seguir("P", "Q"), "Coexisten tipos entre los mismos vértices");
        verificar(!red.seguir("P", "Q"), "Seguimiento duplicado");
        verificar(red.seguir("Q", "P"), "Seguimiento inverso permitido");
        verificar(red.dejarDeSeguir("Q", "P"), "Baja de seguimiento");
        verificar(red.contarAmigos("Q") == 1, "La baja conserva la amistad");
        verificar("Quino".equals(red.listarSeguidos("p")), "La baja conserva el sentido contrario");
        verificar(red.eliminarPersona("Q"), "Eliminar la cabeza");
        estado(red, 1, 0, 0, "Retira dos tipos de arista hacia el mismo destino");
        verificar(red.registrarPersona("Q", "Quino"), "Registro tras eliminar");
        verificar(red.listarSeguidores("Q").isEmpty(), "Sin referencias antiguas al registrar de nuevo");
        verificar(red.eliminarPersona("P"), "Eliminar la cola");
        verificar(red.eliminarPersona("Q"), "Eliminar último nodo");
        estado(red, 0, 0, 0, "Red vuelve a estar vacía");
    }

    public static void main(String[] args) {
        escenarioGuia();
        System.out.println("OK | Escenario de los ocho pasos de Conexión Ambato");
        validaciones();
        System.out.println("OK | Validaciones, direccionalidad y limpieza de referencias");
        System.out.println("Comprobaciones aprobadas: " + aprobadas);
    }
}
