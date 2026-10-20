package cl.inacap.appiot

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class CrearCuenta : AppCompatActivity() {
    private lateinit var txtRut: EditText
    private lateinit var txtNom: EditText
    private lateinit var txtApe: EditText
    private lateinit var txtCorreo: EditText
    private lateinit var spCargo: Spinner
    private lateinit var txtCon: EditText
    private lateinit var txtCon2: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var btnVolver: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_cuenta)
        txtRut = findViewById(R.id.txtRut)
        txtNom = findViewById(R.id.txtNom)
        txtApe = findViewById(R.id.txtApe)
        txtCorreo = findViewById(R.id.txtCorreo)
        spCargo = findViewById(R.id.spCargo)
        txtCon = findViewById(R.id.txtCon)
        txtCon2 = findViewById(R.id.txtCon2)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        btnVolver = findViewById(R.id.btnVolver)

        val cargos = listOf("Administrador", "Operador", "Supervisor", "Técnico")
        spCargo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cargos)

        botonRegistrar()
        botonVolver()
    }

    fun botonRegistrar() {
        btnRegistrar.setOnClickListener {
            val rut = txtRut.text.toString().trim()
            val nom = txtNom.text.toString().trim()
            val ape = txtApe.text.toString().trim()
            val correo = txtCorreo.text.toString().trim()
            val con = txtCon.text.toString().trim()
            val con2 = txtCon2.text.toString().trim()

            if (listOf(rut, nom, ape, correo, con, con2).any { it.isEmpty() }) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_LONG).show()
            } else if (con != con2) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show()
            } else {
                val usu = Usuario(rut, nom, ape, correo, spCargo.selectedItem.toString(), hashSha256(con))
                FirebaseDatabase.getInstance().getReference("Usuario").push().setValue(usu)
                    .addOnCompleteListener {
                        Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }
        }
    }

    fun botonVolver() {
        btnVolver.setOnClickListener { finish() }
    }

    @Deprecated("Desactiva flecha de retroceso")
    override fun onBackPressed() { }
}