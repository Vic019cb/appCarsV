package com.example.appcars

// Modificacion: Imports necesarios para permisos, cámara, galería (ActivityResultContracts), archivos y ubicación GPS
import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AgregarMantenimientoActivity : ComponentActivity() {

    // Modificacion: Propiedades de la clase (fuera de onCreate) para mostrar la foto, la ubicación y gestionar el GPS
    private lateinit var ivFoto: ImageView
    private lateinit var tvUbicacion: TextView
    private lateinit var clienteUbicacion: FusedLocationProviderClient

    // Modificacion: Variables para almacenar la ruta de la foto y el archivo temporal de la cámara
    private var fotoPath: String? = null
    private var archivoFotoActual: File? = null

    // Modificacion: Variables para almacenar la latitud y longitud obtenidas por el GPS
    private var latitud: Double? = null
    private var longitud: Double? = null

    // Modificacion: (1) Lanzador registrado como propiedad de la clase para pedir el permiso de cámara
    private val pedirPermisoCamara = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) lanzarCamara()
        else Toast.makeText(this, "Sin permiso no puedo hacer la foto", Toast.LENGTH_SHORT).show()
    }

    // Modificacion: (2) Lanzador registrado como propiedad de la clase para abrir la cámara y recibir si se hizo la foto
    private val hacerFoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito: Boolean ->
        val archivo = archivoFotoActual
        if (exito && archivo != null) {
            fotoPath = archivo.absolutePath
            ivFoto.setImageBitmap(Fotos.cargar(fotoPath))
        } else {
            archivo?.delete()
        }
    }

    // Modificacion: (3) Lanzador registrado como propiedad de la clase para elegir una imagen de la galería sin pedir permisos
    private val elegirDeGaleria = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val ruta = Fotos.copiarDesdeUri(this, uri)
            if (ruta != null) {
                fotoPath = ruta
                ivFoto.setImageBitmap(Fotos.cargar(ruta))
            }
        }
    }

    // Modificacion: (4) Lanzador registrado como propiedad de la clase para pedir los dos permisos de ubicación (precisa y aproximada)
    private val pedirPermisosUbicacion = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        val fina = resultado[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val aproximada = resultado[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fina || aproximada) obtenerUbicacion()
        else Toast.makeText(this, "Sin permiso no puedo saber dónde estás", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_mantenimiento)

        val radioCoche = findViewById<RadioButton>(R.id.radioCoche)
        val radioMoto = findViewById<RadioButton>(R.id.radioMoto)
        val imagenVehiculo = findViewById<ImageView>(R.id.imagenVehiculoFormulario)
        val vehiculoSpinner = findViewById<Spinner>(R.id.spinnerVehiculo)
        val tipoSpinner = findViewById<Spinner>(R.id.spinnerTipo)
        val campoOtroTipo = findViewById<EditText>(R.id.editOtroTipo)
        val campoKm = findViewById<EditText>(R.id.editKilometros)
        val campoFecha = findViewById<EditText>(R.id.editFecha)
        val campoCoste = findViewById<EditText>(R.id.editCoste)
        val botonGuardar = findViewById<Button>(R.id.btnGuardar)
        val botonCancelar = findViewById<Button>(R.id.btnCancelar)

        // Modificacion: Enlazamos el ImageView de la foto, el TextView de ubicación y el cliente de ubicación de Google
        ivFoto = findViewById(R.id.ivFoto)
        tvUbicacion = findViewById(R.id.tvUbicacion)
        clienteUbicacion = LocationServices.getFusedLocationProviderClient(this)

        // Nuevo Proyecto V2: Mejora propia: si venimos desde el detalle de un modelo de la API, mostramos el modelo elegido
        val modeloDesdeApi = intent.getStringExtra("EXTRA_MODELO_API")
        val tvModeloApi = findViewById<TextView>(R.id.tvModeloSeleccionadoApi)
        if (!modeloDesdeApi.isNullOrBlank()) {
            tvModeloApi.text = "🚗 Modelo elegido del catálogo online: $modeloDesdeApi"
            tvModeloApi.visibility = View.VISIBLE
        }

        // Modificacion: Al pulsar el botón de cámara, comprobamos permiso y abrimos la cámara
        findViewById<Button>(R.id.btnFoto).setOnClickListener {
            pulsarBotonFoto()
        }

        // Modificacion: Al pulsar el botón de galería, lanzamos el selector oficial de Android solo para imágenes
        findViewById<Button>(R.id.btnGaleria).setOnClickListener {
            elegirDeGaleria.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        // Modificacion: Al pulsar el botón de ubicación, comprobamos permisos GPS y obtenemos las coordenadas
        findViewById<Button>(R.id.btnUbicacion).setOnClickListener {
            pulsarBotonUbicacion()
        }

        // Opciones del desplegable para vehículo y tipo de mantenimiento
        vehiculoSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Coche", "Moto"))
        val tipos = arrayOf("Mantenimiento anual", "Cambio de piezas", "Cambio de ruedas", "Cambio luces", "Otras reparaciones")
        tipoSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tipos)

        // Control para evitar que suene automáticamente al entrar a la pantalla
        var usuarioTocoSpinner = false
        vehiculoSpinner.setOnTouchListener { _, _ ->
            usuarioTocoSpinner = true
            false
        }

        // Al pulsar en Coche: cambiamos la foto al coche deportivo y suena su motor
        radioCoche.setOnClickListener {
            usuarioTocoSpinner = false
            vehiculoSpinner.setSelection(0)
            imagenVehiculo.setImageResource(R.drawable.coche_deportivo)
            SonidoUtil.reproducirCoche(this)
        }

        // Al pulsar en Moto: cambiamos la foto a la moto deportiva y suena su motor
        radioMoto.setOnClickListener {
            usuarioTocoSpinner = false
            vehiculoSpinner.setSelection(1)
            imagenVehiculo.setImageResource(R.drawable.moto_deportiva)
            SonidoUtil.reproducirMoto(this)
        }

        // Si el usuario usa el desplegable, mantenemos sincronizados los botones, la foto y el sonido
        vehiculoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    radioCoche.isChecked = true
                    imagenVehiculo.setImageResource(R.drawable.coche_deportivo)
                    if (usuarioTocoSpinner) {
                        SonidoUtil.reproducirCoche(this@AgregarMantenimientoActivity)
                    }
                } else {
                    radioMoto.isChecked = true
                    imagenVehiculo.setImageResource(R.drawable.moto_deportiva)
                    if (usuarioTocoSpinner) {
                        SonidoUtil.reproducirMoto(this@AgregarMantenimientoActivity)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Ponemos la fecha de hoy por defecto y abrimos el selector de calendario al pulsar
        campoFecha.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time))
        campoFecha.setOnClickListener {
            val hoy = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                campoFecha.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year))
            }, hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH)).show()
        }

        botonCancelar.setOnClickListener { finish() }

        // Validamos el formulario y guardamos en la base de datos Room
        botonGuardar.setOnClickListener {
            val kmValor = campoKm.text.toString().trim().toIntOrNull()
            val costeValor = campoCoste.text.toString().trim().replace(',', '.').toDoubleOrNull()
            val tipoSeleccionado = if (tipos[tipoSpinner.selectedItemPosition] == "Otras reparaciones") {
                campoOtroTipo.text.toString().trim()
            } else {
                tipos[tipoSpinner.selectedItemPosition]
            }

            // Comprobamos formato válido de fecha
            val fechaValida = try {
                val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }
                formato.parse(campoFecha.text.toString().trim()) != null
            } catch (_: Exception) { false }

            when {
                tipoSeleccionado.isBlank() -> Toast.makeText(this, "Indica el tipo de reparación.", Toast.LENGTH_SHORT).show()
                kmValor == null || kmValor < 0 -> Toast.makeText(this, "Introduce kilómetros válidos.", Toast.LENGTH_SHORT).show()
                !fechaValida -> Toast.makeText(this, "Selecciona una fecha válida.", Toast.LENGTH_SHORT).show()
                costeValor == null || !costeValor.isFinite() || costeValor < 0 -> Toast.makeText(this, "Introduce un coste válido.", Toast.LENGTH_SHORT).show()
                else -> {
                    // Guardamos el vehículo elegido (Coche o Moto).
                    // Nuevo Proyecto V2: Si el usuario vino desde un modelo del catálogo online y elige Coche, incluimos el modelo
                    val vehiculoElegido = if (radioMoto.isChecked || vehiculoSpinner.selectedItemPosition == 1) {
                        "Moto"
                    } else if (!modeloDesdeApi.isNullOrBlank()) {
                        "Coche ($modeloDesdeApi)"
                    } else {
                        "Coche"
                    }
                    // Modificacion: Pasamos también latitud, longitud y fotoPath al crear el objeto Mantenimiento para Room
                    val nuevo = Mantenimiento(
                        vehiculo = vehiculoElegido,
                        tipo = tipoSeleccionado,
                        kilometros = kmValor,
                        fecha = campoFecha.text.toString().trim(),
                        coste = costeValor,
                        latitud = latitud,
                        longitud = longitud,
                        fotoPath = fotoPath
                    )
                    // Conectamos con Room dentro de lifecycleScope.launch (Regla del apartado 3)
                    lifecycleScope.launch {
                        val dao = AppDatabase.obtener(this@AgregarMantenimientoActivity).mantenimientoDao()
                        dao.insertar(nuevo)
                        // Modificacion: Limpiamos las variables de foto y ubicación y vaciamos el ImageView tras guardar (Apartado 7 del PDF)
                        fotoPath = null
                        latitud = null
                        longitud = null
                        ivFoto.setImageDrawable(null)
                        Toast.makeText(this@AgregarMantenimientoActivity, "Mantenimiento guardado en la base de datos", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    // Modificacion: Función que comprueba si ya tenemos permiso de cámara; si lo tenemos abre la cámara y si no lo pide
    private fun pulsarBotonFoto() {
        val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permiso == PackageManager.PERMISSION_GRANTED) lanzarCamara()
        else pedirPermisoCamara.launch(Manifest.permission.CAMERA)
    }

    // Modificacion: Función que prepara el archivo vacío, guarda su referencia y lanza la cámara pasándole su Uri
    private fun lanzarCamara() {
        val archivo = Fotos.crearArchivo(this)
        archivoFotoActual = archivo
        hacerFoto.launch(Fotos.uriDe(this, archivo))
    }

    // Modificacion: Función que comprueba si tenemos concedido alguno de los dos permisos de ubicación (precisa o aproximada)
    private fun tienePermisoUbicacion(): Boolean {
        val fina = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val aprox = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fina == PackageManager.PERMISSION_GRANTED ||
                aprox == PackageManager.PERMISSION_GRANTED
    }

    // Modificacion: Función que pide la ubicación si ya hay permiso o lanza el cuadro de diálogo con los dos permisos GPS
    private fun pulsarBotonUbicacion() {
        if (tienePermisoUbicacion()) obtenerUbicacion()
        else pedirPermisosUbicacion.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Modificacion: Función que solicita la ubicación actual de alta precisión a Google Play Services y muestra latitud y longitud con 4 decimales
    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion() {
        clienteUbicacion
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { ubicacion: Location? ->
                if (ubicacion != null) {
                    latitud = ubicacion.latitude
                    longitud = ubicacion.longitude
                    tvUbicacion.text = "Ubicación: " + "%.4f".format(ubicacion.latitude) +
                            ", " + "%.4f".format(ubicacion.longitude)
                } else {
                    Toast.makeText(this, "No se ha podido obtener la ubicación", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        SonidoUtil.liberar()
    }
}
