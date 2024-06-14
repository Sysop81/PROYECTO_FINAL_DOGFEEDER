# <h1><img style="width:90px" src="clientes/android/app/src/main/res/drawable/logobig.png" alt="Logo"><span style='color:#D2B48C;'>DOG</span><span>FEEDER</span></h1>

![License](https://img.shields.io/github/license/Sysop81/PROYECTO_FINAL_DOGFEEDER)


1. [Introducción](#introducción)
2. [Modelo 3D](#modelo3d)
3. [Puesta en Marcha Servicio TCP](#puesta-en-marcha)
    - [Instalación de Raspberry OS](#instalación-de-raspberry-os)
    - [Instalación de MySQL](#instalación-de-mysql)
    - [Servidor TCP](#servidor-tcp)
    - [Definición de Variables de Entorno](#definición-de-variables-de-entorno)
4. [Cliente TCP. Android app](#clienteTCP)    
5. [Microcontroloador Raspberry PICO](#microcontrolador-raspberry-pico)    

## Introducción

 __¿Qué es DOGFEEDER?__
 Es un proyecto bajo la filosofía de IoT para la creación de un dispensador de alimento para perros a través de una aplicación móvil comunicada mediante socket TCP.

 __¿Con qué propósito se realiza?__

<span style='background-color:black;color:lightgray;padding:5px 10px 5px 10px;border-radius:5px;'><b><span style='color:#D2B48C;'>DOG</span>FEEDER</b> se realiza para cubrir el módulo de proyecto del CFGS DAM IoT</span> 


## Modelo 3D
* El modelo 3D estará disponible de forma modular en breve.
* Se añade una versión del modelo completo y seccionado de DOGFEEDER.
* Es necesario destacar, que el sistema de extracción de alimento basado en un tornillo de rosca infinita es propiedad del usuario __HENRYLOC5__, y el diseño se puede descargar desde [aquí](https://cults3d.com/es/modelo-3d/herramientas/raspberry-pi-powered-cat-feeder)

## Puesta en Marcha

En este apartado se realizará una breve descripción de los pasos necesarios para poner en marcha el sistema.

### Instalación de Raspberry OS

<u><em>Pasos a seguir :</em></u>
1. acceder a la url https://www.raspberrypi.com/software/
2. Descargar Software Raspberry Pi Imager.
3. Seguir los pasos para preparar la tarjeta MicroSD.
4. Encender la Raspberry pi con la tarjeta MicroSD insertada, completar el proceso de configuración y configurar la conexión de red.
5. Crear usuario o utilizar el usuario Pi por defecto [Privilegios de root] 
6. Configurar la IP estática para la Raspberry Pi para la interfaz que vamos a utilizar con la aplicación cliente. Por defecto, implícitamente en el proyecto se designa el número de equipo <b>240</b> en una red de clase C.

<u><em>Configuración de la IP estática en Raspberry pi.</em></u>

Editamos el fichero <b>dhcp.conf</b>
`sudo nano /etc/dhcp.conf`

A continuación, se muestra un ejemplo para la interfaz inalámbrica
```
interface wlan0
static ip_address=192.168.1.240/24
static routers=192.168.1.1
static domain_name_servers=192.168.1.1 8.8.8.8
```

### Instalación de MySQL
DOGFEEDER utiliza como sistema de persistencia una base de datos relacional, para ello se emplea MYSQL.

<b>¡¡Importante!!</b>
Debemos estar conectados vía ssh o físicamente para realizar la configuración.
`ssh 192.168.1.240`

<u><em>Para realizar la instalación de MYSQL debemos;</em></u>
1. Actualizar la lista de paquetes
    ```
    sudo apt update
    sudo apt full-upgrade
    ```
2. Realizar la instalación de MYSQL
`sudo apt install mysql-server`
Si en la versión de Raspberry OS no se encuentra el paquete, entonces usted debe instalar mariaDB, esto realizará la instalación de MYSQL.
`sudo apt install mariadb-server`

3. Ejecución del script de seguridad proporcionado por MYSQL
`sudo mysql_secure_installation`
Crearemos el usuario y base de datos por defecto para DOGFEEDER.<b>Usted puede establecer valores diferentes.</b>

   | Usuario    | Contraseña | Base de Datos |
   |------------|-------------|---------------|
   | dogfeeder  | dogfeeder   | dog_feeder     |

   Para ello debemos loguearnos como usuario root y lanzar el contenido del script de inicialización de la carpeta database del proyecto.
   `sudo mysql -uroot -p`

4. Ejecutamos los script contenidos en la carpeta database
Para conectarnos con el usuario que hemos creado haremos los siguiente
`mysql -u dogfeeder -p`
Se nos pedirá la contraseña en nuestro caso <b>dogfeeder</b>.
Ahora conectaremos con la base de datos, para ello;
`connect dog_feeder`
Ya estaremos en disposición de lanzar comandos SQL.

### Servidor TCP.
<u><em>Para la puesta en marcha del servidor TCP existen dos opciones</em></u>
1. Descargar el jar.<b>[Aún no implementada]</b>
2. Descargar la carpeta servidor.

    * Una vez descargada abrir la carpeta servidorTCP con un IDE y proceder a generar el jar incluyendo dependencias.
    * Conectarse a la Raspberry Pi mediante un servidor FTP y proceder a realizar la subida de los siguientes ficheros.
        * JAR compilado del servidor TCP
        * Script manageFoodGate.py contenido en la carpeta servidor/Python_scripts
        * dflogo.png contenido en la carpeta img del código del servidor TCP
    * Conectarse a la Raspberry Pi vía ssh y desplazarse hasta la ubicación del fichero JAR
    * Crear las carpetas logs, reports e img ejecutando estos comandos;
        ```
        mkdir logs
        mkdir reports
        mkdir img
        ```    
    * Mover el fichero dflogo.png a la carpeta img
    * Ejecutar el servidor TCP 
    `java -jar [nombreServidorTCP.jar]`
    * Con esto, el servicio estaría listo para atender a los clientes.

### Definición de variables de entorno.
Para acceder a la configuración de las variables de entorno de Raspberry OS escribiremos en la terminal
`sudo nano /etc/environment`

En el fichero environment debemos establecer todas las variables de la tabla.
| Variable    | Valor por defecto | Descripción |
|------------|-------------|---------------|
| TCP_PORT   | 2000        | Puerto de escucha para el servidor TCP     |
| DB_USER    | dogfeeder   | Usuario [base datos]         |
| DB_PASSWORD| dogfeeder   | Contraseña [base datos] |
| DB_NAME    | dog_feeder  | Nombre de la base de datos |
|MAIL_USER_ACCOUNT| Establecer | Dirección de email desde la que se realiza el envío de notificaciones |
|MAIL_USER_PASSWORD| Establecer | Contraseña del email seteado para el envío de notificaciones|
|LOG_FILE_PATH|logs/log.log| Ruta relativa a la ubicación del servidor TCP para el fichero de log|
|REPORTS_FILE_PATH|reports/report.pdf| Ruta relativa a la ubicación del servidor TCP para el fichero de reportes |
|IMG_LOGO_FILE_PATH|img/dflogo.png| Ruta relativa a la ubicación del servidor TCP para la imagen del logotipo a incluir en el reporte |
|GATE_SCRIPT_NAME| manageFoodGate.py| Nombre del script python encargado de abrir/cerrar el tapón de la tolva|

## Cliente TCP. Android app
<u><em>Para la puesta en marcha del cliente existen dos opciones</em></u>
1. Descargar el apk.<b>[Aún no implementada]</b>
2. Descargar la carpeta clientes/android y proceder a abrir con un IDE. Contruir el apk o lanzarla en el dispositivo físico.

## Microcontrolador Raspberry PICO
El proyecto utiliza como sistema de apoyo el microcontrolador Raspberry PICO combinado con un relé de 1 canal 5V DC.
* Es posible utilizar el sistema propuesto sin hacer uso del microcontrolador, para ello, se debe comentar o eliminar el código en;
    * Sección de iluminación en el apartado settings de la aplicación cliente, tanto para la vista como para el controlador.
    * En principio en el servicio no es necesario hacer cambios.