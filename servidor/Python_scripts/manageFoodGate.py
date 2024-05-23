import RPi.GPIO as GPIO
import time
import argparse

# Argumentos
parser = argparse.ArgumentParser(description='Controlador Servomotor SG-90.')
parser.add_argument('duty_cycle', type=float, help='Valor del ciclo de trabajo para ChangeDutyCycle')
args = parser.parse_args()

# Definimos el PIN GPIO
servoPIN = 32
GPIO.setmode(GPIO.BOARD)
GPIO.setup(servoPIN, GPIO.OUT)

p = GPIO.PWM(servoPIN, 50) 
p.start(2.5) 

# Se establece la posición del servo mediante el argumento pasado al script
p.ChangeDutyCycle(args.duty_cycle)
time.sleep(0.5)
GPIO.cleanup()
