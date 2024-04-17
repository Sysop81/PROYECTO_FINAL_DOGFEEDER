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
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.core.animation.doOnEnd
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.lopez.guillen.dogfeeder.model.Session
import com.lopez.guillen.dogfeeder.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val that = this
        GlobalScope.launch(Dispatchers.Main) {
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
                // Se establece el layout para nuestro mainACTIVITY
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
                val callback = object : OnBackPressedCallback(true /* enabled by default */) {
                    override fun handleOnBackPressed()
                    {
                        // Maneja el evento de ir hacia atrás aquí
                        if (drawerLayout.isDrawerOpen(GravityCompat.START))
                        {
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }
                        else
                        {
                            isEnabled = false
                            finish()
                        }
                    }
                }
                onBackPressedDispatcher.addCallback(that,callback)

                //Step 8. Obtenemos las sharedPreferences
                sharedPreferences = that.getSharedPreferences("preferences", Context.MODE_PRIVATE)
                // Step 9. Recuperamos los elementos del header del drawerMenu y seteamos al usuario que tenemos en nuestras
                //         sharedPreferences.
                val headerView = drawerMenu.getHeaderView(0)
                //val tvUsuario = headerView.findViewById<TextView>(R.id.tvUserName)
                val tvEmail = headerView.findViewById<TextView>(R.id.tvEmail)
                val user_ = Tools.getUserInSharedPreferences(sharedPreferences)
                tvEmail.setText(user_?.email)
            }
        }
    }

    /**
     * Método onCreateOptionsMenu
     * Se encarga de inflar la vista de la Toolbar cargando el xml del menu en la vista principal
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Step 1. Inflamos la vista de la Toolbar
        menuInflater.inflate(R.menu.top_app_bar, menu)
        // Step 2. Inicializamos nuestra searchView que se encuentra contenida en el menu
        //searchItem = menu?.findItem(R.id.action_search)
        //searchView = searchItem?.actionView as SearchView

        // Step 3. Inicializamos la ordenacion tambien contenida en el menu
        //orderView = menu?.findItem(R.id.action_order)

        return true
    }

    /**
     * Método onOptionsItemSelected
     * Metodo encargado del manejo de eventos sobre el menu de opciones de la Toolbar. En este caso, solo maneja la opción
     * de cierre de sesión y vuelta a la pantalla de login.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.opCloseSession->{
                // ***** Cerramos sesion
                // Step 1 Limpiamos el usuario de las sharedPreferences
                Tools.cheanSharedPreferences(sharedPreferences)

                /*val user = FirebaseAuth.getInstance().currentUser
                if(user != null) FirebaseAuth.getInstance().signOut()*/

                val session = Session.getInstance()
                //session.close()

                val handler = Handler(Looper.getMainLooper())

                GlobalScope.launch(Dispatchers.IO) {
                    val stream: OutputStream =  session.clientSocket.getOutputStream()
                    val streamOut = DataOutputStream(stream)
                    streamOut.writeUTF("CLOSE")

                    val inStream: InputStream = session.clientSocket.getInputStream()
                    val data = DataInputStream(inStream)
                    val statusCode = data.readUTF();

                    handler.post{
                        if(statusCode != null && statusCode.equals("1")){
                            session.close()
                            Tools.cheanSharedPreferences(sharedPreferences)
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        }else{
                            Tools.showAlertDialog(this@MainActivity,"Error al cerrar sesión")
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
     */
    fun manageOptionMenu(item: MenuItem): Boolean{
        return when (item.itemId) {
            R.id.menuHome -> {
                loadFragment(FragmentFood(this))
                true
            }
            R.id.menuList -> {
                //loadFragment(FragmentRv(this))
                true
            }
            R.id.menuFav -> {
                //loadFragment(FragmentRvFav(this))
                true
            }
            R.id.menuLoc -> {
                //loadFragment(FragmentLoc(this))
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