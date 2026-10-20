package cl.inacap.appiot

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class Principal : AppCompatActivity() {
    private lateinit var txtRut: EditText
    private lateinit var txtCon: EditText
    private lateinit var btnIniciar: Button
    private lateinit var btnCrear: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)
        txtRut = findViewById(R.id.txtRut)
        txtCon = findViewById(R.id.txtCon)
        btnIniciar = findViewById(R.id.btnIniciar)
        btnCrear = findViewById(R.id.btnCrear)
        botonLogin()
        botonCrearCuenta()
    }

    fun botonLogin() {
        btnIniciar.setOnClickListener {
            ocultarTeclado()
            val rut = txtRut.text.toString().trim()
            val con = txtCon.text.toString().trim()
            if (rut.isEmpty() || con.isEmpty()) {
                Toast.makeText(this, "Complete los campos faltantes", Toast.LENGTH_LONG).show()
            } else {
                val conHash = hashSha256(con)
                var estado = false
                val dbref = FirebaseDatabase.getInstance().getReference("Usuario")
                dbref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (x in snapshot.children) {
                            val rutBd = x.child("rut").value.toString()
                            val conBd = x.child("contrasena").value.toString()
                            if (rut.equals(rutBd, ignoreCase = true) && conHash == conBd) {
                                estado = true
                                val usu = Usuario(
                                    rutBd,
                                    x.child("nombre").value.toString(),
                                    x.child("apellido").value.toString(),
                                    x.child("email").value.toString(),
                                    x.child("cargo").value.toString(),
                                    conBd
                                )
                                val i = Intent(this@Principal, Menu::class.java)
                                i.putExtra("usu", usu)
                                startActivity(i)
                                finish()
                            }
                        }
                        if (!estado) {
                            txtRut.setText("")
                            txtCon.setText("")
                            Toast.makeText(this@Principal, "Error de usuario y/o contraseña", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }
    }

    fun botonCrearCuenta() {
        btnCrear.setOnClickListener {
            startActivity(Intent(this, CrearCuenta::class.java))
        }
    }

    fun ocultarTeclado() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    @Deprecated("Desactiva flecha de retroceso")
    override fun onBackPressed() { }
}