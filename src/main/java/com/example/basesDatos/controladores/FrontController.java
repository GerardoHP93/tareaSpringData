package com.example.basesDatos.controladores;

import com.example.basesDatos.excepciones.TransferenciaException;
import com.example.basesDatos.excepciones.TransferenciaException.TipoError;
import com.example.basesDatos.modelos.Cuenta;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.basesDatos.servicios.TransferenciaServicio;

import java.math.BigDecimal;

/**
 * Controlador REST que maneja las operaciones relacionadas con cuentas y transferencias.
 * Proporciona endpoints para listar cuentas, crear cuentas nuevas y realizar transferencias.
 */
@RestController
public class FrontController {
    private final TransferenciaServicio servicio;

    /**
     * Constructor que inyecta el servicio de transferencias.
     *
     * @param servicio Servicio que maneja la lógica de negocio para las operaciones con cuentas
     */
    public FrontController(TransferenciaServicio servicio) {
        this.servicio = servicio;
    }

    /**
     * Endpoint para listar todas las cuentas existentes.
     *
     * @return Listado de todas las cuentas almacenadas en la base de datos
     */
    @GetMapping("/")
    public Iterable<Cuenta> index() {
        return servicio.listar();
    }

    /**
     * Endpoint para crear una nueva cuenta bancaria.
     *
     * @param nombre Nombre del propietario de la cuenta
     * @param cantidadInicial Saldo inicial con el que se creará la cuenta
     * @return Respuesta HTTP con la cuenta creada o mensaje de error
     */
    @PostMapping("/crear")
    public ResponseEntity<?> crearCuenta(
            @RequestParam("nombre") String nombre,
            @RequestParam("cantidadInicial") BigDecimal cantidadInicial) {

        try {
            Cuenta cuentaCreada = servicio.crearCuenta(nombre, cantidadInicial);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(cuentaCreada);
        } catch (TransferenciaException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al crear la cuenta: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Endpoint para realizar transferencias entre cuentas.
     * Valida que ambas cuentas existan y que la cuenta origen tenga saldo suficiente.
     *
     * @param origen ID de la cuenta origen
     * @param destino ID de la cuenta destino
     * @param cantidad Monto a transferir
     * @return Respuesta HTTP con resultado de la operación y mensaje descriptivo
     */
    @PostMapping("/transferir")
    public ResponseEntity<String> transferir(
            @RequestParam("origen") long origen,
            @RequestParam("destino") long destino,
            @RequestParam("cantidad") BigDecimal cantidad) {

        try {
            // Obtenemos los detalles de las cuentas antes de la transferencia
            Cuenta cuentaOrigen = servicio.obtenerCuentaPorId(origen);
            Cuenta cuentaDestino = servicio.obtenerCuentaPorId(destino);

            // Realizamos la transferencia
            servicio.transferir(origen, destino, cantidad);

            // Mensaje de éxito detallado
            String mensaje = String.format(
                    "Transferencia realizada con éxito. Se han transferido %s de la cuenta de %s a la cuenta de %s.",
                    cantidad.toString(),
                    cuentaOrigen.getNombre(),
                    cuentaDestino.getNombre()
            );

            return ResponseEntity.ok(mensaje);

        } catch (TransferenciaException e) {
            HttpStatus status;

            switch (e.getTipoError()) {
                case SALDO_INSUFICIENTE:
                case CANTIDAD_INVALIDA:
                    status = HttpStatus.BAD_REQUEST;
                    break;
                case CUENTA_NO_ENCONTRADA:
                    status = HttpStatus.NOT_FOUND;
                    break;
                default:
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
            }

            return ResponseEntity.status(status).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado: " + e.getMessage());
        }
    }
}