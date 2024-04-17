package com.lopez.guillen.dogfeeder.model

/**
 * Clase Item
 * Esta data class para los registros de pulso que se mostraran en el recyclerView
 * date -> Representa la fecha del registro
 * fmax -> Frecuencia cardiaca maxima
 * fmed -> frecuencia cardiaca media
 * umbral -> Umbral determinado en base a las métricas anteriores
 * isFav  -> Determina si este entreno esta definido como favorito
 */
data class User(val email:String, val password:String) {}