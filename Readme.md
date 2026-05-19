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



### 3. Instrucciones claras para compilar y ejecutar cada módulo

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

Protocolo para la Demo Local (Secuencia de Arranque):

Paso 1: Inicie el servidor local de Apache ActiveMQ y asegúrese de que esté escuchando en tcp://localhost:61616.

Paso 2: Ejecute los módulos de persistencia y procesamiento (Event Store Builder y Business Unit). Esto garantiza que las colas y tópicos estén listos para recibir y procesar datos, e inicializa el servicio web en el puerto 7000.

Paso 3: Ejecute los flujos emisores (Football Feeder y Weather Feeder) para comenzar con la ingesta dinámica de datos y comprobar el procesamiento en tiempo real durante la presentación.


### 4. Ejemplos de uso 
Una vez que el módulo Business Unit se encuentra en ejecución, inicializa un servicio web ligero en el puerto 7000 impulsado por el framework Javalin. Este servicio expone los datos procesados del Datamart en formato estructurado JSON

Pedir la recomendación para un equipo: * GET http://localhost:7000/recommend/Real Madrid CF

Te devolverá un JSON como este:

JSON
{
  "match": "Real Madrid CF vs FC Barcelona",
  "date": "2026-10-24 20:00:00",
  "recommendation": " 🌤️ TIEMPO PERFECTO: Manga corta o sudadera fina. Disfruta del fútbol.",
  "weather": "20.5°C, clear sky"
}
Ver todos los partidos guardados:

GET http://localhost:7000/matches

Ver los partidos de una ciudad concreta:

GET http://localhost:7000/weather/Madrid

Una vez que la Business Unit está en marcha, expone un servicio web en el puerto 7000 (usando el framework Javalin) que devuelve las respuestas en formato limpio JSON.
Una vez que el módulo Business Unit se encuentra en ejecución, inicializa un servicio web ligero en el puerto 7000 impulsado por el framework Javalin. Este servicio expone los datos procesados del Datamart en formato estructurado JSON. Las consultas se pueden realizar directamente desde cualquier navegador web, herramientas como Postman, o mediante comandos curl.1. Inferencia Predictiva sobre Partidos FuturosEndpoint: GET /recommend/{nombre_del_equipo}Lógica interna: El sistema filtra los eventos planificados basándose en la marca temporal actual ($match\_date \ge DATETIME('now')$) y recupera la recomendación climática calculada para el encuentro más próximo cronológicamente del equipo solicitado.Ejemplo de Petición: http://localhost:7000/recommend/Real%20Madrid%20CFRespuesta del sistema (JSON):JSON{
  "match": "Real Madrid CF vs Club Atlético de Madrid",
  "date": "2026-05-24 21:00:00",
  "recommendation": " 🌤️ TIEMPO PERFECTO: Sudadera fina o manga corta. Ideal para disfrutar del partido.",
  "weather": "20.5°C, clear sky"
}
2. Consulta de Estado Meteorológico por UbicaciónEndpoint: GET /weather/{ciudad}Lógica interna:
Recupera las últimas métricas atmosféricas e interpolaciones predictivas registradas en el Datamart asociadas a una localización geográfica específica.
Ejemplo de Petición: http://localhost:7000/weather/Madrid
3. Catálogo Global: GET /matches
Lógica interna: Devuelve el conjunto completo de registros integrados en el Datamart ordenados cronológicamente. 
Ejemplo de Petición: http://localhost:7000/matches
### 5. Arquitectura de Sistema

<img width="545" height="968" alt="image" src="https://github.com/user-attachments/assets/259d4f50-1cde-4e9a-a31c-7acecda685d9" />



### 6. Principios y patrones de diseño aplicados
Para que el código sea limpio y fácil de mantener, hemos seguido varias reglas de diseño:

Principio de Responsabilidad Única (SRP): Cada componente asume un único rol estrictamente acotado dentro del sistema. RestApi se limita a la gestión de rutas HTTP, EventConsumer a la escucha aislada del broker JMS, HistoryLoader a la lectura secuencial de los eventos en frío y Datamart a la encapsulación de transacciones SQL sobre SQLite.

Patrón Publicador / Suscriptor: Gracias a ActiveMQ, los feeders envían datos sin saber quién los va a recibir, logrando que si un módulo se apaga, los demás sigan funcionando sin enterarse.

Inyección de Dependencias: No usamos variables globales. La base de datos (Datamart) se crea una sola vez en el Main y se le pasa por el constructor a RestApi, HistoryLoader y EventConsumer para que todos compartan la misma conexión de forma segura.

Separación por Capas (MVC): El código está ordenado limpiamente en paquetes separados según su función: model (los datos), view (la API web de Javalin) y control (la lógica que procesa la información).Model (estructuras de datos y entidades), control (lógica analítica, procesamiento de tópicos y lógica de upsert) y la capa de exposición del servicio REST impulsada por Javalin.
