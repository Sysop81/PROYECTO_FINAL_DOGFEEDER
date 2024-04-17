package com.lopez.guillen.dogfeeder

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.lopez.guillen.dogfeeder.model.Session
import com.lopez.guillen.dogfeeder.utils.Tools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

class FragmentFood(val _context: Activity) : Fragment() {

    private val session = Session.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSetFood = view.findViewById<Button>(R.id.btnSetFood)
        btnSetFood.setOnClickListener{
            handleBtnSetFood()
        }
    }

    private fun handleBtnSetFood(){
        //Tools.showAlertDialog(_context,"Alimentamos mascota")
        val handler = Handler(Looper.getMainLooper())
        val that = this
        GlobalScope.launch(Dispatchers.IO) {
            val stream: OutputStream =  that.session.clientSocket.getOutputStream() //that.clientSocket.getOutputStream()
            val streamOut = DataOutputStream(stream)
            streamOut.writeUTF("FOOD")

            val inStream: InputStream = that.session.clientSocket.getInputStream()
            val data = DataInputStream(inStream)
            val statusCode = data.readUTF();

            handler.post{
                if(statusCode.equals("1")){
                    Tools.showAlertDialog(_context,"comida suministrada " + statusCode)
                }else{
                    Tools.showAlertDialog(_context,"comida no suministrada " + statusCode)
                }
            }
        }
    }
}