package cl.inacap.appiot

import java.io.Serializable

data class Usuario(
    val rut: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val cargo: String = "",
    val contrasena: String = ""
) : Serializable

data class Accion(
    val fecha: String = "",
    val hora: String = "",
    val usuario: String = "",
    val accion: String = "",
    val distancia: Int = 0
) : Serializable