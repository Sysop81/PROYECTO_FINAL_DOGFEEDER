package com.lopez.guillen.dogfeeder.model

/**
 * Interfaz OnFragmentInteractionListener
 * Se encarga de definir un metodo para la comunicacion entre fragments y de esta forma delegamos la carga del fragment
 * a la activity en lugar del fragment
 *
 */
interface FragmentInteractionListener {
    fun onHandleRegisterUser()
    fun onHandleRecoveryPassword()
}