# FieldWeather
Gabriel Pérez Doreste y Gael Hernández Brito

### 1. Descripción del proyecto y su propuesta de valor
Este proyecto es un sistema distribuido que recolecta, guarda y muestra datos combinados de partidos de fútbol y el clima.

Propuesta de valor: El sistema cruza las fechas y lugares de los partidos con el pronóstico del tiempo en tiempo real para generar recomendaciones inteligentes de forma automática (por ejemplo, si va a nevar, si hay alertas por calor extremo o si va a llover). Esto ayuda a los aficionados a saber exactamente qué ropa llevar o qué precauciones tomar antes de ir al estadio.

### 2. Justificación de la elección de APIs y estructura del Datamart
Elección de APIs
API de Fútbol: Nos da de forma fiable los partidos, los equipos que juegan, las fechas y los resultados en formatos limpios (JSON) para poder extraerlos fácilmente.

API del Clima: Nos permite conocer la temperatura, humedad y el pronóstico exacto de la ciudad en la que se juega cada partido justo en las horas cercanas al encuentro.

Estructura del Datamart
Hemos elegido SQLite para nuestra base de datos por una razón muy sencilla: funciona directamente sobre un archivo local (datamart.db). No hace falta instalar ni configurar ningún servidor de bases de datos externo en el ordenador, lo que hace que todo sea mucho más rápido de montar.

Además, toda la información se guarda de forma desnormalizada en una única tabla llamada match_weather. Al tener los datos del partido, del tiempo y la recomendación calculada en la misma fila, la API web (Javalin) puede leerlos al instante para mostrárselos al usuario sin ralentizar el sistema con búsquedas complejas.

### 3. Instrucciones de ejecución
Para arrancar el proyecto en tu ordenador, no necesitas usar líneas de comandos ni terminales complejas. Lo hacemos todo directamente desde el entorno de desarrollo (como IntelliJ IDEA) siguiendo estos pasos:

Requisitos previos
Tener Java instalado (Versión 17 o superior).

Descargar y arrancar Apache ActiveMQ (el broker de mensajería que usamos para conectar los módulos).

Tener las claves (API Keys) de las plataformas de fútbol y clima.

Pasos para arrancar el sistema
Paso 1: Encender ActiveMQ
Antes de abrir el código, ve a la carpeta donde descargaste ActiveMQ y arráncalo. Esto hará que el puerto tcp://localhost:61616 se quede escuchando los mensajes.

Paso 2: Configurar los parámetros (Arguments) en IntelliJ
Como el proyecto tiene varios módulos independientes, cada uno tiene su propio archivo Main. Para que funcionen bien, hay que ir a las opciones de ejecución de IntelliJ (arriba a la derecha, en Edit Configurations...), buscar la casilla Program arguments y escribir los datos que necesita cada uno separados por un espacio:

Event Store Builder: Necesita la URL de ActiveMQ y la carpeta donde va a guardar los ficheros de texto.

Ejemplo de argumentos: tcp://localhost:61616 ./eventstore

Football Feeder: Necesita la clave de la API y la URL de ActiveMQ.

Ejemplo de argumentos: tu_api_key_futbol tcp://localhost:61616

Weather Feeder: Necesita la clave de la API y la URL de ActiveMQ.

Ejemplo de argumentos: tu_api_key_clima tcp://localhost:61616

Business Unit: Necesita la URL de ActiveMQ, la ruta de la carpeta de eventos y el puerto de la web.

Ejemplo de argumentos: tcp://localhost:61616 ./eventstore 8080

Paso 3: Darle al botón de Play
Simplemente abre el archivo Main de cada módulo y dale al botón verde de Run / Play en tu entorno de desarrollo. Al estar todos encendidos a la vez, el sistema empezará a funcionar en tiempo real.



### 4. Ejemplos de uso

Requisitos del Entorno
Java Development Kit (JDK): Versión 17 o superior correctamente configurada en el IDE.

Apache ActiveMQ: Broker de mensajes compatible con JMS, inicializado localmente en su puerto nativo (tcp://localhost:61616).

Configuración de Parámetros de Ejecución (Program Arguments)
Para simular la topología distribuida dentro del IDE (como IntelliJ IDEA), configure los siguientes argumentos de programa en las opciones de ejecución de cada clase Main (Run ➡️ Edit Configurations... ➡️ Program arguments):

Event Store Builder: * Program arguments: tcp://localhost:61616 ../eventstore

(Registra la secuencia exacta e inmutable de eventos en la ruta relativa especificada).

Business Unit: * Program arguments: tcp://localhost:61616 ../eventstore 7000

(El núcleo analítico. Lee el histórico en frío desde la ruta relativa, procesa el tiempo real y levanta la API web en el puerto 7000).

Football Feeder: * Program arguments: tcp://localhost:61616 https://api.football-data.org/v4/matches?X-Auth-Token=TU_API_KEY

(Conecta al broker local e ingesta los datos deportivos mediante la URL base y su correspondiente API Key).

Weather Feeder: * Program arguments: tcp://localhost:61616 https://api.openweathermap.org/data/2.5/forecast?appid=TU_API_KEY

(Conecta al broker local e ingesta las predicciones meteorológicas enviando los datos en tiempo real).


Paso 1: Inicie el servidor local de Apache ActiveMQ y asegúrese de que esté escuchando en tcp://localhost:61616.

Paso 2: Ejecute los módulos de persistencia y procesamiento (Event Store Builder y Business Unit). Esto garantiza que las colas y tópicos estén listos para recibir y procesar datos, e inicializa el servicio web en el puerto 7000.

Paso 3: Ejecute los flujos emisores (Football Feeder y Weather Feeder) para comenzar con la ingesta dinámica de datos y comprobar el procesamiento en tiempo real durante la presentación.

Una vez que business-unit esté en ejecución, levanta una API REST en el puerto 7000. Puedes acceder a los datos mediante las siguientes rutas:

Interfaz Gráfica: http://localhost:7000/ (Muestra el menú interactivo con los partidos futuros y predicciones).

Endpoint de Partidos (JSON): Petición GET a http://localhost:7000/matches para obtener la lista completa de encuentros cruzados con su respectivo clima y recomendación.


Cuando ejecute el business-unit y abra el (http://localhost:7000)
Se encontrará con el siguiente menú.
<img width="1900" height="902" alt="Captura de pantalla 2026-05-19 223251" src="https://github.com/user-attachments/assets/48feae15-ee04-4b28-b61c-08a3a7877bb5" />
En el verá todos los partidos jugados, los partidos futuros que incluyen la predicción del tiempo y todos los partidos.

Aquí una demostración al abrir la página de los próximos partidos.
<img width="1897" height="913" alt="Captura de pantalla 2026-05-19 223303" src="https://github.com/user-attachments/assets/4c1afb7a-8e6a-415b-b69b-4041b0e27c94" />



### 5. Arquitectura de Sistema

<img width="545" height="968" alt="image" src="https://github.com/user-attachments/assets/259d4f50-1cde-4e9a-a31c-7acecda685d9" />


#### Diagrama de clases — Weather Feeder
<img width="1902" height="748" alt="image" src="https://github.com/user-attachments/assets/f818b89e-a5d7-4468-87f7-3b464e626885" />

El módulo weather-feeder sigue una arquitectura en capas basada en interfaces para desacoplar las responsabilidades de cada componente.

La clase Main actúa como punto de entrada y delega toda la lógica al Controller, que orquesta el flujo principal: obtiene los datos meteorológicos a través de la interfaz WeatherFeeder y los persiste a través de la interfaz WeatherStore.

OpenWeatherMapFeeder es la implementación concreta de WeatherFeeder. Se encarga de consultar la API de OpenWeatherMap, parsear la respuesta JSON usando WeatherMapper y devolver una lista de objetos WeatherEvent, que representan el modelo de datos interno del módulo con campos como temperatura, humedad, descripción y timestamps.

En cuanto a la persistencia, CompositeWeatherStore implementa WeatherStore y actúa como un almacén compuesto que delega en dos implementaciones simultáneamente: DatabaseWeatherStore, que guarda los eventos en una base de datos SQLite, y WeatherEventStore, que los publica en un topic de ActiveMQ para que otros módulos puedan consumirlos.


#### Diagrama de clases — Football Feeder

<img width="1918" height="687" alt="image" src="https://github.com/user-attachments/assets/ebac65fa-f67e-461b-8717-13863a8da68c" />

El módulo football-feeder sigue la misma arquitectura en capas basada en interfaces que el módulo de weather, manteniendo el mismo patrón de diseño para garantizar la coherencia del sistema.

La clase Main actúa como punto de entrada y delega toda la lógica al Controller, que orquesta el flujo principal: obtiene los datos de partidos a través de la interfaz FootballFeeder y los persiste a través de la interfaz MatchStore.

FootballDataFeeder es la implementación concreta de FootballFeeder. Se encarga de consultar la API de Football-Data.org, parsear la respuesta JSON usando MatchMapper y devolver una lista de objetos Match, que representan el modelo de datos interno del módulo con campos como equipos, marcador, estado del partido, competición, fecha y ciudad.

En cuanto a la persistencia, CompositeMatchStore implementa MatchStore y actúa como un almacén compuesto que delega en dos implementaciones simultáneamente: DatabaseMatchStore, que guarda los partidos en una base de datos SQLite, y MatchEventStore, que los publica en un topic de ActiveMQ para que otros módulos puedan consumirlos.

#### Diagrama de clases — Event Store builder
<img width="770" height="467" alt="image" src="https://github.com/user-attachments/assets/f23d7627-9eb1-431e-8924-9a281b230cd6" />

El módulo event-store-builder es el componente suscriptor del sistema, responsable de consumir los eventos publicados en ActiveMQ y persistirlos de forma organizada en el sistema de ficheros.

La clase Main actúa como punto de entrada. Crea una instancia de EventStore y se la inyecta al EventStoreListener, suscribiéndolo a los topics Weather y Football. Una vez iniciado, el programa permanece en ejecución a la espera de nuevos eventos.

EventStoreListener es el componente que se conecta al broker ActiveMQ y se suscribe de forma duradera a los topics indicados. Por cada mensaje recibido, extrae los campos ts y ss del evento JSON y delega el almacenamiento en EventStore.

EventStore es el componente encargado de persistir los eventos en el sistema de ficheros siguiendo la estructura eventstore/{topic}/{ss}/{YYYYMMDD}.events, añadiendo cada evento en formato JSON Lines al fichero correspondiente según la fecha del timestamp del evento.



### 6. Principios y patrones de diseño aplicados
Para que el código sea limpio y fácil de mantener, hemos seguido varias reglas de diseño:

Principio de Responsabilidad Única (SRP): Cada componente asume un único rol estrictamente acotado dentro del sistema. RestApi se limita a la gestión de rutas HTTP, EventConsumer a la escucha aislada del broker JMS, HistoryLoader a la lectura secuencial de los eventos en frío y Datamart a la encapsulación de transacciones SQL sobre SQLite.

Patrón Publicador / Suscriptor: Gracias a ActiveMQ, los feeders envían datos sin saber quién los va a recibir, logrando que si un módulo se apaga, los demás sigan funcionando sin enterarse.

Inyección de Dependencias: No usamos variables globales. La base de datos (Datamart) se crea una sola vez en el Main y se le pasa por el constructor a RestApi, HistoryLoader y EventConsumer para que todos compartan la misma conexión de forma segura.

Separación por Capas (MVC): El código está ordenado limpiamente en paquetes separados según su función: model (los datos), view (la API web de Javalin) y control (la lógica que procesa la información).Model (estructuras de datos y entidades), control (lógica analítica, procesamiento de tópicos y lógica de upsert) y la capa de exposición del servicio REST impulsada por Javalin.
