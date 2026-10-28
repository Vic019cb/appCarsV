package com.example.appcars

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AgregarMantenimientoActivity : ComponentActivity() {

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
                    // Guardamos el vehículo elegido (Coche o Moto)
                    val vehiculoElegido = if (radioMoto.isChecked || vehiculoSpinner.selectedItemPosition == 1) "Moto" else "Coche"
                    val nuevo = Mantenimiento(
                        vehiculo = vehiculoElegido,
                        tipo = tipoSeleccionado,
                        kilometros = kmValor,
                        fecha = campoFecha.text.toString().trim(),
                        coste = costeValor
                    )
                    // Conectamos con Room dentro de lifecycleScope.launch (Regla del apartado 3)
                    lifecycleScope.launch {
                        val dao = AppDatabase.obtener(this@AgregarMantenimientoActivity).mantenimientoDao()
                        dao.insertar(nuevo)
                        Toast.makeText(this@AgregarMantenimientoActivity, "Mantenimiento guardado en la base de datos", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SonidoUtil.liberar()
    }
}
