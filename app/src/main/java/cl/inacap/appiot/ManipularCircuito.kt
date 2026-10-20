package cl.inacap.appiot

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class ManipularCircuito : AppCompatActivity() {
    private lateinit var usu: Usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manipular_circuito)
        usu = intent.getSerializableExtra("usu", Usuario::class.java)!!
        findViewById<TextView>(R.id.tvUsuMan).text = "${usu.nombre} ${usu.apellido}"

        findViewById<Button>(R.id.btnActivar).setOnClickListener {
            registrar("Activación", 0)
            val d = (2..40).random()
            registrar(if (d <= 10) "Cerca de Impactar" else "Obstáculo Detectado", d)
            Toast.makeText(this, "Circuito ACTIVADO - sensor: $d cm", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnDesactivar).setOnClickListener {
            registrar("Desactivación", 0)
            Toast.makeText(this, "Circuito DESACTIVADO", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnVolver).setOnClickListener {
            val i = Intent(this, Menu::class.java)
            i.putExtra("usu", usu)
            startActivity(i)
        }
    }

    fun registrar(accion: String, distancia: Int) {
        FirebaseDatabase.getInstance().getReference("Accion")
            .push()
            .setValue(Accion(fechaHoy(), horaAhora(), usu.rut, accion, distancia))
    }

    @Deprecated("Desactiva flecha de retroceso")
    override fun onBackPressed() { }
}