package cl.inacap.appiot

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun hashSha256(t: String): String =
    MessageDigest.getInstance("SHA-256").digest(t.toByteArray()).joinToString("") { "%02x".format(it) }

fun fechaHoy(): String = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

fun horaAhora(): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())