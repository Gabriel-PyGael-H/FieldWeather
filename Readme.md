# FieldWeather
Gabriel Pérez Doreste y Gael Hernández Brito

### 1. Descripción del Proyecto y Propuesta de Valor

FieldWeather es una plataforma orientada a la unificación de datos deportivos y meteorológicos. Su propuesta de valor radica en centralizar en un único cuadro de mando interactivo el calendario de partidos de fútbol junto con la predicción del tiempo en la ciudad donde se disputa cada encuentro.

Esto permite transformar datos complejos en información práctica de alto valor (por ejemplo, recomendaciones automáticas para los asistentes sobre si deben llevar paraguas o abrigo), optimizando la experiencia del usuario al consultar eventos deportivos próximos sin tener que revisar diferentes aplicaciones.

### 2. Justificación de APIs y Estructura del Datamart

Elección de APIs
Fútbol (football-data.org): Seleccionada por su estabilidad y precisión para la ingesta del calendario completo de encuentros, proporcionando la estructura base de partidos, equipos, fechas y localizaciones de las jornadas de liga.

Meteorología (OpenWeatherMap): Se integra por su cobertura global y fiabilidad para obtener predicciones climáticas en tiempo real indexadas por la ciudad de juego, garantizando la precisión de las condiciones ambientales asociadas a cada partido.

Estructura del Datamart (SQLite)
Se ha seleccionado SQLite por su ligereza, rendimiento y nula necesidad de configuración de servidores externos, lo que facilita el despliegue rápido de la Business Unit. El diseño lógico de la base de datos almacena la información limpia y procesada garantizando la unicidad de las entidades:

Evita la duplicidad de registros mediante el control de claves primarias lógicas al procesar los estados de los eventos.

Estructura tablas optimizadas para lecturas rápidas desde la API, separando la persistencia de partidos de las condiciones climáticas asociadas.

### 3. Instrucciones claras para compilar y ejecutar cada módulo 
3. Instrucciones claras para compilar y ejecutar cada módulo
Requisitos previos
Java 21 o superior

Apache ActiveMQ corriendo en el puerto 61616
API key de OpenWeatherMap

API key de Football-Data.org

1. Compilar el proyecto completo
Desde la raíz del proyecto (donde se encuentra el pom.xml principal), ejecute el siguiente comando para compilar y generar los archivos ejecutables de todos los módulos:  mvn clean install

2. Arrancar Apache ActiveMQ
Descargue ActiveMQ desde https://activemq.apache.org e inicie el servicio en su máquina local:
# En Windows
activemq.bat start

# En Linux/Mac
./activemq start

El broker quedará disponible escuchando en tcp://localhost:61616.

3. Weather Feeder
Módulo encargado de publicar las predicciones meteorológicas de las ciudades de LaLiga en el topic Weather. Requiere la URL base de la API con su correspondiente clave de acceso y la dirección del broker:

java -jar weather-feeder/target/weather-feeder-1.0-SNAPSHOT.jar \
  "http://api.openweathermap.org/data/2.5/forecast?q=%s&appid=TU_API_KEY&units=metric&lang=en" \
  localhost:61616

  4. Football Feeder
Módulo encargado de publicar los partidos de LaLiga en el topic Football. Requiere la URL del endpoint, la clave de la API y la dirección del broker:

java -jar football-feeder/target/football-feeder-1.0-SNAPSHOT.jar \
  "https://api.football-data.org/v4/competitions/PD/matches" \
  TU_API_KEY \
  localhost:61616

5. Event Store Builder
Módulo suscriptor que captura de ActiveMQ todos los eventos entrantes y los almacena de forma inmutable. Requiere la dirección del broker y la ruta relativa de la carpeta de destino:

java -jar event-store-builder/target/event-store-builder-1.0-SNAPSHOT.jar \
  localhost:61616 \
  eventstore
Los eventos se estructurarán automáticamente siguiendo el siguiente patrón de almacenamiento inmutable:
eventstore/
├── Football/
│   └── football-feeder/
│       └── 20250815.events
└── Weather/
    └── weather-feeder-v1/
        └── 20250815.events


6. Business Unit
El componente central de la aplicación. Carga los eventos del pasado almacenados en los ficheros, se conecta al broker para escuchar los eventos del presente en tiempo real y expone el servidor web.

Para ejecutar este módulo de manera correcta (ya sea por terminal o configurando los Program Arguments en su IDE), se deben pasar obligatoriamente tres argumentos separados por espacios:
java -jar business-unit/target/business-unit-1.0-SNAPSHOT.jar \
  localhost:61616 \
  eventstore \
  7000

Argumento 1 (broker-url): localhost:61616Dirección del broker para que el EventConsumer escuche los topics de ActiveMQ en vivo.
Argumento 2 (eventstore-path): eventstoreRuta relativa del directorio de almacenamiento. Al usar una ruta relativa, el HistoryLoader funcionará en cualquier                    entorno local de evaluación para procesar el histórico de ficheros del pasado.
Argumento 3 (port): 7000 Puerto en el que la RestApi (Javalin) desplegará los endpoints y servirá la interfaz de usuario.

### 4. Ejemplos de uso 

Una vez que la Business Unit esté totalmente operativa en el puerto 7000, puede comprobar el correcto funcionamiento de las consultas al Datamart realizando peticiones HTTP directas usando los siguientes endpoints

Obtener todos los partidos procesados en el calendario:
Endpoint: GET http://localhost:7000/matches

Filtrar partidos e información meteorológica por ciudad:
Endpoint: GET http://localhost:7000/weather/Madrid

Obtener la recomendación climática para el partido de un equipo específico:
Endpoint: GET http://localhost:7000/recommend/Sevilla

Probar la interfaz web
Para interactuar con la interfaz visual completa de FieldWeather, simplemente se abre el navegador y acceder a la siguiente dirección:
http://localhost:7000/index.html

### 5. Arquitectura de sistema y arquitectura de la aplicación

### 6. Principios y patrones de diseño aplicados en cada módulo.
Principio de Responsabilidad Única (SRP): Cada clase tiene una tarea única y bien delimitada. RestApi maneja de forma exclusiva la capa HTTP, EventConsumer se enfoca únicamente en la lectura del broker, y Datamart centraliza los accesos a la base de datos SQLite.

Patrón Arquitectónico de Capas (MVC): Estructuración limpia que separa el almacenamiento (Model), la lógica interna de control (Control) y los componentes de visualización y API hacia el usuario (View).

Patrón Publicador-Suscriptor (Pub-Sub): Implementado mediante ActiveMQ a nivel de sistema de datos. Este patrón permite desacoplar por completo los Feeders de recolección de los módulos de almacenamiento y procesamiento de la Business Unit, facilitando que el ecosistema del proyecto sea escalable y tolerante a fallos.

