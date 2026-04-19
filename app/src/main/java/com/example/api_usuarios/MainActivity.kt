package com.example.api_usuarios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// Modelo de datos que representa un usuario de la API
data class Persona(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null
)

// Interfaz de Retrofit que define los endpoints de la API
interface UsuarioApi {
    @GET("usuarios")
    suspend fun fetchPersonas(): List<Persona>
}

// Cliente HTTP singleton que configura Retrofit
object HttpClient {
    private const val URL_BASE = "https://69de37ca410caa3d47bace81.mockapi.io/"

    val service: UsuarioApi by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsuarioApi::class.java)
    }
}

// Actividad principal, punto de entrada de la app
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    PantallaUsuarios(modifier = Modifier.padding(padding))
                }
            }
        }
    }
}

// Pantalla principal que muestra la lista de usuarios
@Composable
fun PantallaUsuarios(modifier: Modifier = Modifier) {
    // Estados de la pantalla
    var lista by remember { mutableStateOf<List<Persona>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Se ejecuta una sola vez al cargar la pantalla
    LaunchedEffect(Unit) {
        try {
            // Llamada a la API en hilo de fondo
            lista = withContext(Dispatchers.IO) {
                HttpClient.service.fetchPersonas()
            }
        } catch (e: Exception) {
            error = "No se pudo cargar la lista"
        } finally {
            cargando = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título de la pantalla
        Text(
            text = "Lista de Usuarios",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        when {
            // Mientras carga muestra un indicador
            cargando -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            // Si hubo error muestra el mensaje
            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            // Si todo OK muestra la lista
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(lista) { persona ->
                        TarjetaUsuario(persona)
                    }
                }
            }
        }
    }
}

// Card simple que muestra los datos de un usuario
@Composable
fun TarjetaUsuario(persona: Persona) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = persona.name ?: "Sin nombre",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = persona.email ?: "Sin email",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "ID: ${persona.id ?: "-"}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}