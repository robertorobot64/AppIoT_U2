package cl.inacap.appiot

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class Acciones : AppCompatActivity() {
    private lateinit var usu: Usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acciones)
        usu = intent.getSerializableExtra("usu", Usuario::class.java)!!
        findViewById<TextView>(R.id.tvUsuAcc).text = "${usu.nombre} ${usu.apellido}"

        val lv = findViewById<ListView>(R.id.lvAcciones)
        FirebaseDatabase.getInstance().getReference("Accion")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lista = mutableListOf<String>()
                    for (x in snapshot.children) {
                        val a = x.getValue(Accion::class.java) ?: continue
                        lista.add(0, "Fecha: ${a.fecha}  Hora: ${a.hora}\nUsuario: ${a.usuario}\nAcción: ${a.accion}\nDistancia: ${a.distancia} cm\n")
                    }
                    lv.adapter = ArrayAdapter(this@Acciones, android.R.layout.simple_list_item_1, lista)
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        findViewById<Button>(R.id.btnVolver).setOnClickListener {
            val i = Intent(this, Menu::class.java)
            i.putExtra("usu", usu)
            startActivity(i)
        }
    }

    @Deprecated("Desactiva flecha de retroceso")
    override fun onBackPressed() { }
}