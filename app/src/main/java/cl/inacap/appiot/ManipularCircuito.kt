package cl.inacap.appiot

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ManipularCircuito : AppCompatActivity() {
    private lateinit var usu: Usuario
    private lateinit var tvDist: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvUltCmd: TextView
    private lateinit var etUmbral: EditText
    private lateinit var cm: ConnectivityManager
    private lateinit var netCb: ConnectivityManager.NetworkCallback
    private var lDatos: ValueEventListener? = null
    private var lCmd: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manipular_circuito)
        usu = intent.getSerializableExtra("usu", Usuario::class.java)!!
        findViewById<TextView>(R.id.tvUsuMan).text = "${usu.nombre} ${usu.apellido}"
        tvDist = findViewById(R.id.tvDist); tvEstado = findViewById(R.id.tvEstado)
        tvUltCmd = findViewById(R.id.tvUltCmd); etUmbral = findViewById(R.id.etUmbral)
        findViewById<Button>(R.id.btnActivar).setOnClickListener { enviarComando("led", "on") }
        findViewById<Button>(R.id.btnDesactivar).setOnClickListener { enviarComando("led", "off") }
        findViewById<Button>(R.id.btnUmbral).setOnClickListener {
            val u = etUmbral.text.toString().trim()
            if (u.isEmpty()) Toast.makeText(this, "Indique umbral en cm", Toast.LENGTH_SHORT).show()
            else enviarComando("umbral", u)
        }
        findViewById<Button>(R.id.btnVolver).setOnClickListener {
            val i = Intent(this, Menu::class.java); i.putExtra("usu", usu); startActivity(i)
        }
        cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        netCb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { runOnUiThread { vaciarCola() } }
        }
    }

    // ---- APP -> MCU (3.1.2.5) con cola offline (2.7 / 4.13) ----
    fun enviarComando(tipo: String, valor: String) {
        val cmd = mapOf("tipo" to tipo, "valor" to valor, "estado" to "pendiente",
            "fecha" to fechaHoy(), "hora" to horaAhora(), "usuario" to usu.rut)
        if (hayRed()) {
            FirebaseDatabase.getInstance().getReference("Comandos").push().setValue(cmd)
            tvUltCmd.text = "Comando enviado: $tipo=$valor (pendiente)"
        } else {
            val p = getPreferences(MODE_PRIVATE)
            p.edit().putString("cola", p.getString("cola", "") + "$tipo=$valor\n").apply()
            tvUltCmd.text = "SIN CONEXIÓN: $tipo=$valor encolado en el teléfono"
        }
    }

    fun vaciarCola() {
        val p = getPreferences(MODE_PRIVATE)
        val cola = p.getString("cola", "")!!
        if (cola.isEmpty()) return
        val ref = FirebaseDatabase.getInstance().getReference("Comandos")
        cola.split("\n").filter { it.isNotEmpty() }.forEach { l ->
            val partes = l.split("=")
            ref.push().setValue(mapOf("tipo" to partes[0], "valor" to partes[1],
                "estado" to "pendiente", "fecha" to fechaHoy(), "hora" to horaAhora(),
                "usuario" to usu.rut, "origen" to "colaOffline"))
        }
        p.edit().putString("cola", "").apply()
        Toast.makeText(this, "Cola offline sincronizada con Firebase", Toast.LENGTH_SHORT).show()
    }

    fun hayRed(): Boolean {
        val net = cm.activeNetwork
        return net != null && cm.getNetworkCapabilities(net)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    // ---- MCU -> APP en tiempo real (3.1.2.6) + acuse de comandos ----
    fun escuchar() {
        val db = FirebaseDatabase.getInstance().getReference()
        lDatos = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val u = s.children.lastOrNull() ?: return
                tvDist.text = "${u.child("distancia").value} cm"
                tvEstado.text = "${u.child("descripcion").value} (${u.child("fecha").value} ${u.child("hora").value})"
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        db.child("Datos").limitToLast(1).addValueEventListener(lDatos!!)
        lCmd = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val u = s.children.lastOrNull() ?: return
                tvUltCmd.text = "Cmd: ${u.child("tipo").value}=${u.child("valor").value} → ${u.child("estado").value}"
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        db.child("Comandos").limitToLast(1).addValueEventListener(lCmd!!)
    }

    override fun onStart() {
        super.onStart()
        cm.registerNetworkCallback(NetworkRequest.Builder().build(), netCb)
        escuchar()
        if (hayRed()) vaciarCola()
    }

    override fun onStop() {
        super.onStop()
        cm.unregisterNetworkCallback(netCb)
        val db = FirebaseDatabase.getInstance().getReference()
        lDatos?.let { db.child("Datos").removeEventListener(it) }
        lCmd?.let { db.child("Comandos").removeEventListener(it) }
    }

    @Deprecated("Desactiva flecha de retroceso")
    override fun onBackPressed() { }
}