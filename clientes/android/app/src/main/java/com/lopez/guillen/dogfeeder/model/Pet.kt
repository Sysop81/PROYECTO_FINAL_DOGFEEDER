package com.lopez.guillen.dogfeeder.model

import java.util.*

/**
 * Clase Pet
 * Clase encargada de modelar a la mascota asociada a DOG-FEEDER
 * @param id Identificador de la mascota
 * @param name Nombre de la mascota
 * @param breed Raza de la mascota
 * @param birthday Fecha de nacimiento de la mascota
 * @param weight Peso de la mascota
 * @param type Tipo de raza de la mascota
 * @param vaccineNotify Activación / desactivación para la notificación de vacunación anual
 * @param vaccineDay Día de vacunación
 * @param vaccineMonth Mes de vacunación
 */
class Pet(val id: Int,val name: String,val breed: String, val birthday : String, val weight : Double,
          val type : String, val vaccineNotify : Boolean, val vaccineDay : Int, val vaccineMonth : Int) {

    /**
     * Método getTypeOfSize
     * Método encargado de devolver un valor entero en función del tipo (raza) asociado a la mascota
     * @return Int Valor entero de la posicón en el listado spinner
     */
    fun getTypeOfSize() : Int{
        return when (type){
            "medium" -> 1
            "big" -> 2
            else -> 0
        }
    }


    /**
     * Método getYearOrDefault
     * Devuelve el año extraido de la fecha de nacimento y en caso de esta vacío o no cumplir con la dimensión, obtener
     * desde Calendar el año en curso.
     * @return Int Año de nacimiento
     */
    fun getYearOrDefault() : Int{
        var aDate = birthday.split("-")

        return  if(aDate.isEmpty() || aDate.size < 3){
            Calendar.getInstance().get(Calendar.YEAR)
        } else  aDate[0].toInt()
    }


    /**
     * Método getFromDate
     * Método encargado de obtener de la fecha de nacimiento en base a la cadena que llega como parametro, la parte
     * correspondiente al split de la fecha. [Si partOfYear == "YEAR" entonces return aDate[0].toInt]
     * @param partOfYear Cadena de caracteres
     * @return Int Valor correspondiente a dia, mes o año de la fecha de nacimiento
     */
    fun getFromDate(partOfYear : String) : Int {
        var aDate = birthday.split("-")

        return if(aDate.isNotEmpty() && aDate.size == 3){
            when(partOfYear){
                "YEAR" -> aDate[0].toInt()
                "MONTH" -> aDate[1].toInt() -1
                else -> aDate[2].toInt()
            }
        }else{
            val calendar = Calendar.getInstance()
            when(partOfYear){
                "YEAR" -> calendar.get(Calendar.YEAR)
                "MONTH" -> calendar.get(Calendar.MONTH)
                else -> calendar.get(Calendar.DAY_OF_MONTH)
            }
        }
    }


    override fun toString(): String {
        return "Pet(id=$id, name='$name', breed='$breed', birthday='$birthday', weight=$weight, type='$type', vaccineNotify=$vaccineNotify, vaccineDay=$vaccineDay, vaccineMonth=$vaccineMonth)"
    }
}