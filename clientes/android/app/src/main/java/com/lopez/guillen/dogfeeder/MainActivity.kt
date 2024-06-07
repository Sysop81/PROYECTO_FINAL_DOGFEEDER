package com.lopez.guillen.dogfeeder

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.core.animation.doOnEnd
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.lopez.guillen.dogfeeder.model.Session
import com.lopez.guillen.dogfeeder.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Clase MainActivity
 * Activity MAIN o principal, es la actividad que se carga despues de que un usuario realice la acción de LOGIN en la
 * app. Sobre esta activity se establecen los cuatro fragments de gestión para DOG-FEEDER
 */
class MainActivity : AppCompatActivity() {
    // Propiedades de clase
    private lateinit var swipeRefresh : SwipeRefreshLayout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var searchItem:  MenuItem
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 0. Obtenemos el contexto del activity y lanzamos una corrutina para establecer la progressbar
        val that = this
        lifecycleScope.launch(Dispatchers.Main) {
            // Establecemos el layout que contine la progressbar
            setContentView(R.layout.progressbar)

            // Obtenemos la progressbar y establecemos el maximo que puede alcanzar en 1000 y el valor actual en
            // 300. --> Quedarian 700 de prograso por completar en 3 segundos
            val pB = findViewById<ProgressBar>(R.id.progressBar)
            pB.max = 1000
            var currentProgress = 300

            // Construimos una animación para la barra de progreso especificando los valores anteriormente establecidos
            val animation = ObjectAnimator.ofInt(pB, "progress", currentProgress, pB.max)
                .setDuration(3000)
            // Se lanza la animación
            animation.start()

            animation.doOnEnd {

                // Se establece el layout para nuestro mainActivity
                setContentView(R.layout.activity_main)

                // Step 1. Establecemos la Toolbar en lugar de la ActionBar y configuramos el DrawerMenu
                setSupportActionBar(findViewById(R.id.toolbar))

                // Step 2. Instanciamos el drawerLayout y establecemos la accion de toggle open/close en la toolbar
                drawerLayout = findViewById(R.id.mainActivity)
                toggle = ActionBarDrawerToggle(
                    that,
                    drawerLayout,
                    findViewById(R.id.toolbar),
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close
                )
                // Step 3. Se añade un listener para manejar el evento abierto/cerrado y se sincroniza.
                toggle.drawerArrowDrawable.color = getColor(R.color.white)
                drawerLayout.addDrawerListener(toggle)
                toggle.syncState()

                // Step 4. Establecemos un listener para el nav_view del drawermenu
                val drawerMenu = findViewById<NavigationView>(R.id.nav_view)
                drawerMenu.setNavigationItemSelectedListener(that::manageOptionMenu)

                // Step 5. Cargamos por defecto el fragment HOME que es nuestra vista de contacto con el entrenador.
                loadFragment(FragmentFood(that))

                // Step 6. Recuperamos nuestra barra de navegacion inferior y establecemos un manejador de eventos para
                //         proceder a la carga del fragment deseado en nuestro FrameLayout
                val bottonNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
                bottonNavigation.setOnItemSelectedListener(that::manageOptionMenu)

                // Step 7. Declaramos la funcion de callback y establecemos el callback onBackPressed.
                val callback = object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed()
                    {
                        // Unicamente se desea cerrar el drawerMenu. Se deshabilita cualquier acción sobre los fragments
                        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }

                    }
                }
                onBackPressedDispatcher.addCallback(that,callback)

                //Step 8. Obtenemos las sharedPreferences
                sharedPreferences = that.getSharedPreferences("preferences", Context.MODE_PRIVATE)

                // Step 9. Recuperamos los elementos del header del drawerMenu y seteamos al usuario que tenemos en nuestras
                //         sharedPreferences.
                val headerView = drawerMenu.getHeaderView(0)
                val tvEmail = headerView.findViewById<TextView>(R.id.tvEmail)
                val user_ = Tools.getUserInSharedPreferences(sharedPreferences)
                tvEmail.setText(user_?.email)

                // Step 10. Instanciamos el objeto swipe y gestionamos un listener para manejar el evento de refresco.
                swipeRefresh = findViewById(R.id.swipeMain)
                swipeRefresh.setOnRefreshListener {
                    val session = Session.getInstance()
                    val handler = Handler(Looper.getMainLooper())
                    lifecycleScope.launch(Dispatchers.IO) {
                        var isConnected = false
                        try {
                            isConnected = session.checkServerConnection(that)
                        }catch(e : Exception){

                        }finally {
                            handler.post{
                                if(isConnected){
                                    if(Tools.checkSharedPreferences(sharedPreferences)){
                                        val user = Tools.getUserInSharedPreferences(sharedPreferences)
                                        if(user != null)
                                            lifecycleScope.launch(Dispatchers.IO){
                                                user.login(that,true,false)
                                            }
                                    }
                                }else{
                                    Tools.showAlertDialog(that,getString(R.string.info_error_connection),R.drawable.ic_baseline_error)
                                }

                                swipeRefresh.isRefreshing = false
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Método onCreateOptionsMenu
     * Se encarga de inflar la vista de la Toolbar cargando el xml del menu en la vista principal
     * @param menu
     * @return Boolean
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Step 1. Inflamos la vista de la Toolbar
        menuInflater.inflate(R.menu.top_app_bar, menu)

        // Step 2. Inicializamos nuestra searchView que se encuentra contenida en el menu
        searchItem = menu?.findItem(R.id.action_search)!!
        searchView = searchItem.actionView as SearchView

        // Step 3. Establecemos el color para el texto de la searchView
        val searchText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchText.setTextColor(getColor(R.color.white))
        searchText.setHintTextColor(getColor(R.color.white))

        // Step 4. Ocultamos la searchView para que no se visualice al cargar el primer fragment
        manageSearchView(false)

        return true
    }


    /**
     * [Getter] Método getSearchItem
     * Getter para la propiedad searchItem.
     * @return MenuItem
     */
    fun getSearchItem(): MenuItem {
        return searchItem
    }


    /**
     * [Getter] Método getSearchView
     * Getter para la propiedad searchView.
     * @return SearchView
     */
    fun getSearchView(): SearchView{
        return searchView
    }


    /**
     * [Setter] Método manageSearchView
     * @param isVisible boolean que representa el estado visible / invisible para la searchView
     */
    fun manageSearchView(isVisible : Boolean){
        searchView.isVisible = isVisible
        searchItem.isVisible = isVisible
    }


    /**
     * Método onOptionsItemSelected
     * Metodo encargado del manejo de eventos sobre el menu de opciones de la Toolbar. En este caso, solo maneja la opción
     * de cierre de sesión y vuelta a la pantalla de login.
     * @param item Opción de menú seleccionada
     * @return Boolean
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.opCloseSession->{
                // ***** Cerramos sesion

                // Step 1. Se obtiene una instancia del singletone y se define el manejador para el post de la corrutina
                val session = Session.getInstance()
                val handler = Handler(Looper.getMainLooper())
                var statusCode = Session.ServerResponseCodes.ERROR.code
                lifecycleScope.launch(Dispatchers.IO) {

                    // Step 2. Realizamos el envío de datos al servidor
                    session.sendRequestToServer(null, Session.ServerStates.CLOSE)

                    // Step 3. Obtenemos la respuesta del servidor
                    statusCode = session.readResponseFromServer()

                    // Step 4. Manejamos la información obtenida del servidor y operamos en consecuencia, mostrando un
                    //         cuadro de diálogo en caso de error o bien limpiando la sesión y volviendo a la vista de
                    //         LOGIN
                    handler.post{
                        if(statusCode.equals(Session.ServerResponseCodes.OK.code)){
                            session.close()
                            Tools.cleanSharedPreferences(sharedPreferences)
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        }else{
                            Tools.showAlertDialog(this@MainActivity,getString(R.string.info_error_logout),R.drawable.ic_baseline_error)
                        }
                    }
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    /**
     * Método manageOptionMenu. [DrawerMenu && BottonMenu handler options]
     * Se encarga de manejar tanto el drawerMenu como el bottonMenu, llamanado al medoto loadFragment en funcion del
     * elemeto del menu sobre el que se ha recibido el vento. Obviamente tanto para el bottonmenu como para el drwar,
     * se establece el mismo layout
     * @param item Opción de menú seleccionada para su carga
     * @return Boolean
     */
    fun manageOptionMenu(item: MenuItem): Boolean{
        return when (item.itemId) {
            R.id.menuFood -> {
                loadFragment(FragmentFood(this))
                true
            }
            R.id.menuList -> {
                loadFragment(FragmentTab(this))
                true
            }
            R.id.menuPet -> {
                loadFragment(FragmentPet(this))
                true
            }
            R.id.menuSettings -> {
                loadFragment(FragmentSettings(this))
                true
            }
            else -> false
        }
    }


    /**
     * Método loadFragment
     * Este método se encarga de cargar el fragment que llega como parametro en el contenedor FrameLayout de nuestra
     * vista.
     * @param Fragment
     */
    fun loadFragment(fragment: Fragment){
        // Step 1. Instanciamos el objeto encargado de interactuar con los fragments
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        // Step 2. Se realiza el reemplazo del fragment por el deseado
        fragmentTransaction.replace(R.id.frameLayoutMain, fragment)
        // Step 4. Se confirma la transacción y se aplican los cambios
        fragmentTransaction.commit()
    }
}