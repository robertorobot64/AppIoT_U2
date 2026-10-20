package cl.inacap.appiot

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Perfil : AppCompatActivity() {
    private lateinit var usu: Usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)
        usu = intent.getSerializableExtra("usu", Usuario::class.java)!!
        findViewById<TextView>(R.id.tvUsuPer).text = "${usu.nombre} ${usu.apellido}"

        findViewById<TextView>(R.id.tvRut).text = "Rut: ${usu.rut}"
        findViewById<TextView>(R.id.tvNom).text = "Nombre: ${usu.nombre}"
        findViewById<TextView>(R.id.tvApe).text = "Apellido: ${usu.apellido}"
        findViewById<TextView>(R.id.tvCorreo).text = "Correo: ${usu.email}"
        findViewById<TextView>(R.id.tvCargo).text = "Cargo: ${usu.cargo}"
        findViewById<TextView>(R.id.tvCon).text = "Contraseña (hash SHA-256): ${usu.contrasena}"

        findViewById<Button>(R.id.btnVolver).setOnClickListener {
            val i = Intent(this, Menu::class.java)
            i.putExtra("usu", usu)
            startActivity(i)
        }
    }

    @Deprecated("Desactiva flecha de retroceso")
    override fun onBackPressed() { }
}