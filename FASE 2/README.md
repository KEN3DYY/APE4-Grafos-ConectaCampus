# Conexión Ambato

Entrega individual de Mateo Chalco para APE 4 Fase 2 de Estructura de Datos. El proyecto representa una red social dinámica con una lista enlazada de vértices y una lista de adyacencia propia por persona.

[Leer el informe en GitHub](docs/Vista_previa_informe.md) · [Descargar PDF](docs/Informe_APE4_Fase2_Mateo_Chalco.pdf) · [Descargar Word editable](docs/Informe_APE4_Fase2_Mateo_Chalco.docx)

## Ejecución

Desde la raíz del repositorio ConectaCampus, con JDK 11 o posterior:

```text
cd "FASE 2"
javac -encoding UTF-8 --release 11 -d build src/RedSocial.java src/ConexionAmbato.java src/PruebasRedSocial.java
java -cp build ConexionAmbato
java -cp build PruebasRedSocial
```

La demostración carga las cinco amistades de la guía y ejecuta los ocho pasos en orden. Tras eliminar a Sofía y rechazar un registro duplicado y un lazo, quedan 5 usuarios, 2 amistades, 0 seguimientos y 4 aristas internas. La suite se detiene con un error si una comprobación falla.

## Modelo

Cada amistad ocupa dos aristas y cada seguimiento ocupa una. Los tipos pueden coexistir entre las mismas personas. La búsqueda ignora mayúsculas y minúsculas, y el registro rechaza identificadores vacíos, con espacios o con comas. Los listados devuelven una cadena vacía para usuarios sin relaciones y `null` para usuarios inexistentes. El conteo de amigos devuelve `-1` si el usuario no existe.

Sean V el número de vértices, A las aristas internas y d el tamaño de una lista de adyacencia. Buscar y registrar cuestan O(V). Agregar relaciones, dejar de seguir y listar salientes cuestan O(V + d). Eliminar una amistad revisa las listas de los dos extremos. Eliminar una persona, listar seguidores y mostrar la red cuestan O(V + A). El almacenamiento del grafo es O(V + A).

El escenario y el contrato de operaciones se basan en la guía APE 4 Fase 2 y en el análisis previo del Grupo 5. Los resultados de esta entrega pueden reproducirse con los comandos anteriores. Las transcripciones verificadas están en `evidencias/`.
