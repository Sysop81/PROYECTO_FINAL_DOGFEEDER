package com.lopez.guillen.dogfeeder.model

/**
 * Clase AuditFoodItem
 * Clase encargada de modelar los registros de auditoría para el suministro de alimento a la mascota.
 * @param id Identificador del registro de auditoría
 * @param user Usuario que realizó el suministro de alimento
 * @param date Fecha en la que se realizó el suministro de alimento
 * @param time Hora en la que se realizó el suministro de alimento
 * @param weight Cantidad de alimento dispensado.
 */
class AuditFoodItem(val id: Int,val user: String,val date: String,val time: String, val weight: Double) {
}