package com.lopez.guillen.dogfeeder.model

/**
 * Interfaz RefreshInteraction
 * Se encarga de definir un metodo para el refresco de los layouts
 */
interface RefreshInteraction {
    /**
     * Método handleRefreshLayout
     * Define como se manejan los errores de conexión con el servidor TCP
     */
    fun handleRefreshLayout()


}