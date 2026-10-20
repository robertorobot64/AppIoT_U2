package cl.inacap.appiot

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class Estadisticas : AppCompatActivity() {
    private lateinit var usu: Usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadisticas)
        usu = intent.getSerializableExtra("usu", Usuario::class.java)!!
        findViewById<TextView>(R.id.tvUsuEst).text = "${usu.nombre} ${usu.apellido}"

        FirebaseDatabase.getInstance().getReference("Accion")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var act = 0
                    var des = 0
                    var obs = 0
                    var dist = 0
                    for (x in snapshot.children) {
                        when (x.child("accion").value.toString()) {
                            "Activación" -> act++
                            "Desactivación" -> des++
                            "Obstáculo Detectado", "Cerca de Impactar" -> {
                                obs++
                                dist += x.child("distancia").value.toString().toIntOrNull() ?: 0
                            }
                        }
                    }
                    findViewById<TextView>(R.id.tvAct).text = "Activaciones: $act"
                    findViewById<TextView>(R.id.tvDes).text = "Desactivaciones: $des"
                    findViewById<TextView>(R.id.tvObs).text = "Obstáculos Detectados: $obs"
                    findViewById<TextView>(R.id.tvDist).text = "Distancia Total: $dist cm"
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