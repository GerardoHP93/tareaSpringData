package com.example.basesDatos.controladores;

import com.example.basesDatos.excepciones.TransferenciaException;
import com.example.basesDatos.excepciones.TransferenciaException.TipoError;
import com.example.basesDatos.modelos.Cuenta;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.basesDatos.servicios.TransferenciaServicio;

import java.math.BigDecimal;

/**
 * Controlador REST que expone endpoints para operaciones relacionadas con cuentas y transferencias.
 * Maneja las solicitudes HTTP y devuelve respuestas con códigos de estado apropiados.
 */
@RestController
public class FrontController {
    private final TransferenciaServicio servicio;

    /**
     * Constructor que utiliza inyección de dependencias para obtener el servicio de transferencias.
     *
     * @param servicio Servicio que implementa la lógica de negocio para transferencias
     */
    public FrontController(TransferenciaServicio servicio) {
        this.servicio = servicio;
    }

    /**
     * Endpoint para listar todas las cuentas disponibles.
     *
     * @return Lista de todas las cuentas en la base de datos
     */
    @GetMapping("/")
    public Iterable<Cuenta> index() {
        return servicio.listar();
    }

    /**
     * Endpoint para realizar una transferencia entre cuentas.
     * Devuelve diferentes códigos HTTP dependiendo del resultado de la operación.
     *
     * @param origen ID de la cuenta de origen
     * @param destino ID de la cuenta de destino
     * @param cantidad Monto a transferir
     * @return ResponseEntity con mensaje de éxito o error y código HTTP apropiado
     */
    @PostMapping("/transferir")
    public ResponseEntity<String> transferir(
            @RequestParam("origen") long origen,
            @RequestParam("destino") long destino,
            @RequestParam("cantidad") BigDecimal cantidad) {

        try {
            // Intentar realizar la transferencia
            servicio.transferir(origen, destino, cantidad);
            // Si es exitosa, devolver código 200 OK
            return ResponseEntity.ok("Transferencia realizada con éxito");
        } catch (TransferenciaException e) {
            // Determinar el código de estado HTTP según el tipo de error
            HttpStatus status;

            switch (e.getTipoError()) {
                case SALDO_INSUFICIENTE:
                    // 400 Bad Request para saldo insuficiente
                case CANTIDAD_INVALIDA:
                    // 400 Bad Request para cantidad inválida
                    status = HttpStatus.BAD_REQUEST;
                    break;
                case CUENTA_NO_ENCONTRADA:
                    // 404 Not Found para cuentas inexistentes
                    status = HttpStatus.NOT_FOUND;
                    break;
                default:
                    // 500 Internal Server Error para otros errores
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
            }

            // Devolver respuesta con el código de estado y mensaje de error
            return ResponseEntity.status(status).body("Error: " + e.getMessage());
        } catch (Exception e) {
            // Capturar cualquier otra excepción no controlada
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado: " + e.getMessage());
        }
    }

    //Endpoint adicional para crear una cuenta usando un método POST
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


}