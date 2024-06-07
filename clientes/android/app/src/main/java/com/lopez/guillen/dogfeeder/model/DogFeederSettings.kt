package com.lopez.guillen.dogfeeder.model

/**
 * Clase DogFeederSettings
 * Clase encargada de modelar los parametros de configuración de DOGFEEDER
 * @param id Identificador
 * @param foodRation Ración maxima de alimento a suminstrar
 * @param ledOn Estado de la iluminación de la tira de led
 * @param notifyHopperLow Estado de la notificación para el estado crítico de la tolva de alimento
 * @param notifyEmptyFeeder Estado de la notificación para el comedero con poco o nada de alimento
 */
class DogFeederSettings(val id: Int,val foodRation : Int, val ledOn : Boolean, val notifyHopperLow : Boolean, val notifyEmptyFeeder : Boolean) {
    override fun toString(): String {
        return "DogFeederSettings(id=$id, foodRation=$foodRation, ledOn=$ledOn, notifyHopperLow=$notifyHopperLow, notifyEmptyFeeder=$notifyEmptyFeeder)"
    }
}