package com.lopez.guillen.dogfeeder

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.lopez.guillen.dogfeeder.model.Pet
import com.lopez.guillen.dogfeeder.model.Session
import com.lopez.guillen.dogfeeder.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.SocketException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Clase FragmentPet
 * Clase encargada de manejar la lógica de necogio para la vista en la cual se setean los valores de la mascota asociada
 * a DOG-FEEDER
 */
class FragmentPet(val _context: Activity) : Fragment() {
    // Propiedades de clase
    private val session = Session.getInstance()
    private lateinit var btnSavePetSettings: Button
    private lateinit var inName : TextInputLayout
    private lateinit var inBreed : TextInputLayout
    private lateinit var inBirthday : TextView
    private lateinit var inWeight : EditText
    private lateinit var inType : Spinner
    private lateinit var inVacNotify : Switch
    private lateinit var inVacDay : Spinner
    private lateinit var inVacMonth : Spinner
    private lateinit var btnPetBirthday : Button
    private lateinit var pet : Pet
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Step 0. Manejamos la visibilidad de la searchView y comprobamos si en este fragment es necesaria la acción de
        //         refresco.
        (activity as MainActivity).manageSearchView(false)
        Tools.activateMainSwipeRefresh(_context,true)

        // Step 1. Instanciado de controles del formulario
        inName = view.findViewById(R.id.txtPetName)
        inBreed = view.findViewById(R.id.txtPetBreed)
        inBirthday = view.findViewById(R.id.picker)
        inWeight = view.findViewById(R.id.txtPetWeight)
        inType = view.findViewById(R.id.spinner)
        inVacDay = view.findViewById(R.id.spinner_vaccine_day)
        inVacMonth = view.findViewById(R.id.spinner_vaccine_month)
        inVacNotify = view.findViewById(R.id.switchVaccineNotify)
        btnSavePetSettings = view.findViewById(R.id.btnSavePetSettings)
        btnPetBirthday = view.findViewById(R.id.btnPicker)
        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        // Step 2. Cargas de datos spinners
        Tools.loadSpinner(inType,listOf(getString(R.string.spinner_pet_size_small),
                                        getString(R.string.spinner_pet_size_med),
                                        getString(R.string.spinner_pet_size_big)),_context)
        Tools.loadSpinner(inVacDay,getDataDaysOfMonth(1,31),_context)
        Tools.loadSpinner(inVacMonth,getDataMonthOfYear(),_context)

        // Step 3. Se establecen los manejadores de eventos
        btnSavePetSettings.setOnClickListener{ handleBtnSavePetSettings() }
        btnPetBirthday.setOnClickListener{ handleBtnPetBirthday() }
        inName.editText?.doOnTextChanged { text, _, _, _ -> handleFormInput(text, inName) }
        inBreed.editText?.doOnTextChanged { text, _, _, _ -> handleFormInput(text, inBreed) }

        // Step 4. Obtenemos de la sesión los datos o consultamos al servidor
        session.getSessionPet()?.let { pet ->
            setPetDataInForm(pet)
        } ?: getPetData()
    }

    /**
     * Manejador handleFormInput
     * Manejador de eventos para la validación de los campos del formulario
     * @param text Cadena de entrada del campo de texto
     * @param inputField Campo del formulario que se está evaluando
     */
    private fun handleFormInput(text : CharSequence?, inputField : TextInputLayout){
        var isEnabled = false
        if (text.toString().isNullOrEmpty()){
            inputField.error = getString(R.string.form_pet_empty_error)
        }else if (!Tools.isValidString(text.toString())) {
            inputField.error = getString(R.string.form_pet_not_numeric_or_characters_error)
        } else {
            isEnabled = true
            inputField.error = null
        }

        btnSavePetSettings.isEnabled = isEnabled
    }


    /**
     * Método getPetData
     * Este método se encarga de recuperar los datos almacenados de la mascota asociada al comedero
     */
    private fun getPetData(){
        var response = Session.ServerResponseCodes.EMPTY_DATA.code
        val handler = Handler(Looper.getMainLooper())
        var msg = _context.getString(R.string.info_error_connection)
        var isConError = false
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Step 1.  Realizamos el envío de datos al servidor
                session.sendRequestToServer(null, Session.ServerStates.PETDATA)

                // Step 2. Obtenemos la respuesta del servidor
                response = session.readResponseFromServer()
            }catch (se : SocketException){
                session.clientSocket.close()
                isConError = true
            }finally {
                handler.post {
                    progressBar.visibility = View.GONE
                    if (!isConError) {
                        if(!response.equals(Session.ServerResponseCodes.EMPTY_DATA.code)){

                            // Creamos la instancia JSONObject para manejar los datos obtenidos.
                            // Instanciamos un objeto Pet y lo ssseteamos en el formulario
                            val job = JSONObject(response)
                            pet = Pet(
                                job.getInt("ID"),
                                job.getString("name"),
                                job.getString("breed"),
                                job.getString("birthday"),
                                job.getDouble("weight"),
                                job.getString("type"),
                                job.getBoolean("vaccineNotify"),
                                job.getInt("vaccineDay") -1,
                                job.getInt("vaccineMonth")
                            )

                            setPetDataInForm(pet)
                            return@post
                        }else{
                            msg = getString(R.string.info_error_not_register_pet)
                        }
                    }
                    Tools.showAlertDialog(_context,msg,R.drawable.ic_baseline_error)
                    progressBar.visibility = View.GONE
                }
            }
        }
    }


    /**
     * Método setPetDataInForm
     * Este método se encarga de setear en el formulario las propiedades del objeto mascota
     * @param _pet Mascota asociada a DOGFEEDER
     */
    private fun setPetDataInForm(_pet : Pet){
        inName.editText?.setText(_pet.name)
        inBreed.editText?.setText(_pet.breed)
        inBirthday.setText(Tools.formatDate(_pet.birthday,"/"))
        inWeight.setText(_pet.weight.toString())
        inType.setSelection(_pet.getTypeOfSize())
        inVacDay.setSelection(_pet.vaccineDay)
        inVacMonth.setSelection(_pet.vaccineMonth)
        inVacNotify.isChecked = _pet.vaccineNotify
        progressBar.visibility = View.GONE
    }


    /**
     * Manejador de eventos handleBtnPetBirthday
     * Se encarga de manejar el control datepicker para la fecha de nacimiento de la mascota
     */
    private fun handleBtnPetBirthday(){
        // Step 1. Instanciamos un objeto Calendar para realizar operaciones relacionas con fechas y tiempo.
        var year = pet.getFromDate("YEAR")
        val month = pet.getFromDate("MONTH")
        val day =  pet.getFromDate("DAY_OF_MONTH")


        // Step 2. Construimos el datepickerdialog pasando el constexto actual y definiendo una función anonima para su manejo
        //         ademas del dia, mes año obtenidos inicialmente, para que inicialmente esté situado en dicha fecha
        val datePickerDialog = DatePickerDialog(
            _context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                // Se formatea la fecha en el formato deseado.
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)

                // Mostramos la fecha en el TextView
                inBirthday.text = formattedDate
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }


    /**
     * Manejador de eventos handleBtnSavePetSettings
     * Se anecarga de manejar el guardado de datos de la mascota en la base de datos
     */
    private fun handleBtnSavePetSettings(){
        progressBar.visibility = View.VISIBLE
        val _pet = hashMapOf(
            "id" to 0,
            "name" to inName.editText?.text.toString(),
            "breed" to inBreed.editText?.text.toString(),
            "birthday" to inBirthday.text.toString(),
            "weight" to inWeight.text.toString(),
            "type" to getPetSizeType(inType.selectedItem.toString()),
            "vaccineNotify" to inVacNotify.isChecked.toString(),
            "vaccineDay" to inVacDay.selectedItem.toString(),
            "vaccineMonth" to inVacMonth.selectedItemPosition.toString()
        )

        val petJson = (JSONObject(_pet as Map<*, *>?)).toString()
        var statusCode = Session.ServerResponseCodes.ERROR.code
        var msg = getString(R.string.info_error_connection)
        val handler = Handler(Looper.getMainLooper())
        var isConError = false
        var icon = R.drawable.ic_baseline_error
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Step 1.  Realizamos el envío de datos al servidor
                session.sendRequestToServer(petJson, Session.ServerStates.POSTPET)

                // Step 2. Obtenemos la respuesta del servidor
                statusCode = session.readResponseFromServer()
            } catch (se: SocketException) {
                session.clientSocket.close()
                isConError = true
            } finally {
                handler.post {
                    progressBar.visibility = View.GONE
                    if (!isConError) {
                        if (statusCode.equals(Session.ServerResponseCodes.OK.code)) {
                            session.setSessionPet(null)
                            msg = getString(R.string.info_pet_register_ok)
                            icon = R.drawable.ic_baseline_info
                        } else {
                            msg = getString(R.string.info_error_pet_register)
                        }
                    }
                    // Mostramos la información al usuario
                    Tools.showAlertDialog(_context, msg,icon)
                }
            }
        }
    }


    /**
     * Método getPetSizeType
     * Este método se encarga de formatear el tipo de raza para realizar la inserción en la DB
     * @param _type Caadena que representa el tipo de raza
     * @return String Cadena modificada para ser insertada en la DB
     */
    private fun getPetSizeType(_type : String) : String{
        val input = (_type.lowercase(Locale.ROOT)).replace("raza ", "")
        return when (input){
            "mediana" -> "medium"
            "grande" -> "big"
            else -> "small"
        }
    }


    /**
     * Método getDataMonthOfYear
     * @return List<String> Listado con los meses pertenecientes al año
     */
    private fun getDataMonthOfYear() : List<String>{
        return  resources.getStringArray(R.array.months).asList()
    }


    /**
     * Método getDataDaysOfMonth
     * Método encargado de cargar el spinner con los dias por mes
     * @param firstDay Dia de inicio
     * @param endDay Dia de fin
     * @return List<String> Listado con los dias del mes
     */
    private fun getDataDaysOfMonth(firstDay : Int, endDay : Int) :  List<String>{
        return (firstDay..endDay).map { it.toString() }
    }
}