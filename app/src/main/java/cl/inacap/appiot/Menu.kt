package cl.inacap.appiot

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class Menu : AppCompatActivity() {
    private lateinit var usu: Usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        usu = intent.getSerializableExtra("usu", Usuario::class.java)!!
        findViewById<TextView>(R.id.tvUsuMenu).text = "${usu.nombre} ${usu.apellido}"

        ir(R.id.btnManipular, ManipularCircuito::class.java)
        ir(R.id.btnAcciones, Acciones::class.java)
        ir(R.id.btnEstadisticas, Estadisticas::class.java)
        ir(R.id.btnPerfil, Perfil::class.java)
        findViewById<Button>(R.id.btnCerrar).setOnClickListener { cerrarSesion() }
    }

    fun ir(id: Int, destino: Class<*>) {
        findViewById<Button>(id).setOnClickListener {
            val i = Intent(this, destino)
            i.putExtra("usu", usu)
            startActivity(i)
        }
    }

    fun cerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Desea cerrar la sesión actual?")
            .setPositiveButton("Aceptar") { _, _ ->
                val i = Intent(this, Principal::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    @Deprecated("Desactiva flecha de retroceso")
    override fun onBackPressed() { }
}