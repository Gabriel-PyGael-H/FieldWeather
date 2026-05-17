# FieldWeather
Gabriel Pérez Doreste y Gael Hernández Brito

### 1. Descripción del proyecto y su propuesta de valor
Este proyecto es un sistema distribuido que recolecta, guarda y muestra datos combinados de partidos de fútbol y el clima.

Propuesta de valor: El sistema cruza las fechas y lugares de los partidos con el pronóstico del tiempo en tiempo real para generar recomendaciones inteligentes de forma automática (por ejemplo, si va a nevar, si hay alertas por calor extremo o si va a llover). Esto ayuda a los aficionados a saber exactamente qué ropa llevar o qué precauciones tomar antes de ir al estadio.

### Justificación de la elección de APIs y estructura del Datamart
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



### 3.  Ejemplos de uso


El sistema está pensado para ejecutarse de forma distribuida desde tu entorno de desarrollo (como IntelliJ IDEA) sin necesidad de usar comandos de consola complejos.

Requisitos previos
Java 17 o superior instalado.

Apache ActiveMQ (el intermediario que permite a los módulos mandarse mensajes entre sí) descargado y encendido en su puerto por defecto (localhost:61616).

Configuración de parámetros en IntelliJ
Cada uno de los 4 módulos tiene su propio archivo Main. Para ejecutarlos, entra en las opciones de configuración de IntelliJ (Edit Configurations...), busca la casilla Program arguments e introduce los datos exactos que necesita tu proyecto separados por un espacio:

Event Store Builder: Guarda una copia exacta de todos los mensajes que viajan por el sistema.

Program arguments: localhost:61616 C:\Users\gabri\IdeaProjects\DacdTrabajo\eventstore

Football Feeder: Descarga los datos de los partidos y los envía al broker.

Program arguments: tu_api_key_futbol localhost:61616

Weather Feeder: Descarga el clima y las predicciones y las envía al broker.

Program arguments: tu_api_key_clima localhost:61616

Business Unit: El cerebro del sistema. Lee los archivos guardados, procesa los nuevos datos en tiempo real, actualiza la base de datos y enciende la web en el puerto 7000.

Program arguments: localhost:61616 C:\Users\gabri\IdeaProjects\DacdTrabajo\eventstore 7000

Orden de arranque: Primero enciende ActiveMQ en tu ordenador. Después, dale al botón de "Play" en tu IDE para ejecutar los 4 módulos (es muy recomendable arrancar la Business Unit e Event Store antes que los Feeders).


### 4. Ejemplos de uso (Consultas REST corregidas)
Cuando la Business Unit esté encendida, puedes abrir el navegador web o usar una herramienta como Postman para hacerle preguntas a los siguientes enlaces (endpoints):

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

1. Consultar Recomendación para el próximo partido de un equipo
Endpoint: GET /recommend/{nombre_del_equipo}

Qué hace por dentro: Busca en la base de datos el partido futuro más cercano de ese equipo (match_date >= tiempo actual) y te da el consejo climático.

Ejemplo de Petición: http://localhost:7000/recommend/Real Madrid CF

Respuesta del sistema:

JSON
{
  "match": "Real Madrid CF vs Club Atlético de Madrid",
  "date": "2026-05-24 21:00:00",
  "recommendation": " 🌤️ TIEMPO PERFECTO: Sudadera fina o manga corta. Ideal para disfrutar del partido.",
  "weather": "20.5°C, clear sky"
}
2. Consultar el Catálogo Global (Historial + Próximos Partidos)
Endpoint: GET /matches

Qué hace por dentro: Te devuelve una lista completa con todos los partidos de la base de datos ordenados por fecha. Aquí puedes ver tanto los resultados de los partidos que ya se han jugado como los datos de los que están por jugar.

Ejemplo de Petición: http://localhost:7000/matches
### 5. Arquitectura de Sistema

<img width="545" height="968" alt="image" src="https://github.com/user-attachments/assets/259d4f50-1cde-4e9a-a31c-7acecda685d9" />



### 6. Principios y patrones de diseño aplicados
Para que el código sea limpio y fácil de mantener, hemos seguido varias reglas de diseño:

Responsabilidad Única: Cada clase hace una sola cosa bien hecha. Por ejemplo, RestApi solo se encarga de las rutas web, EventConsumer solo de hablar con ActiveMQ, e HistoryLoader solo de leer los archivos del disco.

Patrón Publicador / Suscriptor: Gracias a ActiveMQ, los feeders envían datos sin saber quién los va a recibir, logrando que si un módulo se apaga, los demás sigan funcionando sin enterarse.

Inyección de Dependencias: No usamos variables globales. La base de datos (Datamart) se crea una sola vez en el Main y se le pasa por el constructor a RestApi, HistoryLoader y EventConsumer para que todos compartan la misma conexión de forma segura.

Separación por Capas (MVC): El código está ordenado limpiamente en paquetes separados según su función: model (los datos), view (la API web de Javalin) y control (la lógica que procesa la información).
