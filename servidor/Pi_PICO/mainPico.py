# ********************************************************************
# * [Hardware : Raspberry pi PICO]                                   * 
# * Script para el control de tira led situada en el compartimento   *
# * de la electrónica de DOG-FEEDER                                  *
# * José Ramón López Guillén                                         *
# ********************************************************************  

# Imports de librerías
import machine 
import neopixel
import time

# Configurar el pin y el número de LEDs
# Crear una instancia de NeoPixel para el manejo de la tira de leds
# *******************************************************************
pin = machine.Pin(16)  # Usa el pin GPIO 16
num_leds = 8           # Número de LEDs en la tira
strip = neopixel.NeoPixel(pin, num_leds)

# Configurar un botón para realizar el swap del modo de iluminación
#********************************************************************
button_pin = machine.Pin(17, machine.Pin.IN, machine.Pin.PULL_UP)
mode = 1
debounce_time = 200
last_press_time = 0

# Función encargada de apagar todos los LEDs
#*********************************************************************
def clear_strip():
    for i in range(num_leds):
        strip[i] = (0, 0, 0)
    strip.write()

# Función encargada establecer el color de todos los LEDs
#*********************************************************************
def set_color(color):
    for i in range(num_leds):
        strip[i] = color
    strip.write()

# Función encargada de realizar un efecto de arcoíris
#**********************************************************************
def rainbow_cycle(delay):
    for j in range(255):
        for i in range(num_leds):
            pixel_index = (i * 256 // num_leds) + j
            strip[i] = wheel(pixel_index & 255)
        strip.write()
        time.sleep_ms(delay)

# Función supletoria para generar los colores del arcoíris
#**********************************************************************
def wheel(pos):
    if pos < 85:
        return (pos * 3, 255 - pos * 3, 0)
    elif pos < 170:
        pos -= 85
        return (255 - pos * 3, 0, pos * 3)
    else:
        pos -= 170
        return (0, pos * 3, 255 - pos * 3)
    

# Manejador de eventos para el botón. Se encarga de alternar entre los 
# distintos modos de colores especificados en el script.
#***********************************************************************
def button_pressed(pin):
    global mode, last_press_time, debounce_time
    current_time = time.ticks_ms()
    if time.ticks_diff(current_time, last_press_time) > debounce_time:
        if mode < 6:
            mode += 1
        else:
            mode = 1
        
        last_press_time = current_time
        setMode()


def setMode():
    global mode
    if mode == 1:
        print("modo 1")
        set_color((255, 0, 0))  # Rojo
    elif mode == 2:
        print("modo 2")
        set_color((0, 255, 0))  # Verde
    elif mode == 3:
        print("modo 3")
        set_color((0, 0, 255))  # Azul
    elif mode == 4:
        print("modo 4")
        set_color((255, 0, 255))  # Magenta
    elif mode == 5:
        print("modo 5")
        set_color((255, 255, 255))  # Blanco
    else:
        print("modo arcoiris")
        rainbow_cycle(20)
    
    
# ************** Inicio del programa *****************************

# Se define el manejador de eventos para el boton de cambio de modo
button_pin.irq(trigger=machine.Pin.IRQ_RISING, handler=button_pressed)
# Se realiza la llamada a la funcion encargada de establecer al inicio el modo 1
setMode()

    