package com.example.basesDatos.servicios;

import com.example.basesDatos.excepciones.TransferenciaException;
import com.example.basesDatos.excepciones.TransferenciaException.TipoError;
import com.example.basesDatos.modelos.Cuenta;
import org.springframework.stereotype.Service;
import com.example.basesDatos.repositorios.CuentaRepositorio;

import java.math.BigDecimal;

/**
 * Servicio que maneja la lógica de negocio para las operaciones de transferencia entre cuentas.
 * Implementa la validación de reglas de negocio como saldo suficiente, existencia de cuentas, etc.
 */
@Service
public class TransferenciaServicio {

    private final CuentaRepositorio repo;

    /**
     * Constructor que utiliza inyección de dependencias para obtener el repositorio.
     *
     * @param repo Repositorio para acceder a las operaciones de base de datos de las cuentas
     */
    public TransferenciaServicio(CuentaRepositorio repo) {
        this.repo = repo;
    }

    /**
     * Lista todas las cuentas disponibles en la base de datos.
     *
     * @return Iterable con todas las cuentas existentes
     */
    public Iterable<Cuenta> listar() {
        return this.repo.findAll();
    }

    /**
     * Realiza una transferencia de dinero entre dos cuentas, aplicando validaciones.
     *
     * @param origen   ID de la cuenta de origen
     * @param destino  ID de la cuenta de destino
     * @param cantidad Monto a transferir
     * @throws TransferenciaException Si ocurre algún error durante la validación o transferencia
     */
    public void transferir(long origen, long destino, BigDecimal cantidad) {
        // Validación: la cantidad debe ser positiva
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransferenciaException("La cantidad a transferir debe ser mayor que cero",
                    TipoError.CANTIDAD_INVALIDA);
        }

        // Obtener la cuenta de origen, lanzando excepción si no existe
        Cuenta cuentaOrigen = repo.findById(origen)
                .orElseThrow(() -> new TransferenciaException("Cuenta de origen no encontrada",
                        TipoError.CUENTA_NO_ENCONTRADA));

        // Obtener la cuenta de destino, lanzando excepción si no existe
        Cuenta cuentaDestino = repo.findById(destino)
                .orElseThrow(() -> new TransferenciaException("Cuenta de destino no encontrada",
                        TipoError.CUENTA_NO_ENCONTRADA));

        BigDecimal saldoOrigen = cuentaOrigen.getCantidad();

        // Validación: verificar si hay saldo suficiente en la cuenta de origen
        if (saldoOrigen.compareTo(cantidad) < 0) {
            throw new TransferenciaException("Saldo insuficiente en la cuenta de origen",
                    TipoError.SALDO_INSUFICIENTE);
        }

        BigDecimal saldoDestino = cuentaDestino.getCantidad();

        // Realizar la transferencia: restar de la cuenta origen y sumar a la cuenta destino
        cuentaOrigen.setCantidad(saldoOrigen.subtract(cantidad));
        cuentaDestino.setCantidad(saldoDestino.add(cantidad));

        // Persistir los cambios en la base de datos
        repo.save(cuentaOrigen);
        repo.save(cuentaDestino);
    }

    //Método para crear una cuenta desde la aplicación
    public Cuenta crearCuenta(String nombre, BigDecimal cantidadInicial) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new TransferenciaException("El nombre del propietario no puede estar vacío",
                    TipoError.CANTIDAD_INVALIDA);
        }

        if (cantidadInicial == null || cantidadInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new TransferenciaException("La cantidad inicial no puede ser negativa",
                    TipoError.CANTIDAD_INVALIDA);
        }

        Cuenta nuevaCuenta = new Cuenta();
        nuevaCuenta.setNombre(nombre);
        nuevaCuenta.setCantidad(cantidadInicial);

        return repo.save(nuevaCuenta);
    }

}