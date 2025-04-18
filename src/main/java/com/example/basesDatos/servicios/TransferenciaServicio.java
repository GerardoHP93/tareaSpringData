package com.example.basesDatos.servicios;

import com.example.basesDatos.excepciones.TransferenciaException;
import com.example.basesDatos.excepciones.TransferenciaException.TipoError;
import com.example.basesDatos.modelos.Cuenta;
import org.springframework.stereotype.Service;
import com.example.basesDatos.repositorios.CuentaRepositorio;

import java.math.BigDecimal;

/**
 * Servicio que implementa la lógica de negocio para operaciones con cuentas.
 * Maneja la creación de cuentas, consultas y transferencias entre cuentas.
 */
@Service
public class TransferenciaServicio {

    private final CuentaRepositorio repo;

    /**
     * Constructor que inyecta el repositorio de cuentas.
     *
     * @param repo Repositorio que maneja las operaciones de persistencia
     */
    public TransferenciaServicio(CuentaRepositorio repo) {
        this.repo = repo;
    }

    /**
     * Lista todas las cuentas disponibles en la base de datos.
     *
     * @return Colección de todas las cuentas
     */
    public Iterable<Cuenta> listar() {
        return this.repo.findAll();
    }

    /**
     * Obtiene una cuenta específica por su ID.
     *
     * @param id Identificador único de la cuenta
     * @return La cuenta encontrada
     * @throws TransferenciaException Si la cuenta no existe
     */
    public Cuenta obtenerCuentaPorId(long id) {
        return repo.findById(id)
                .orElseThrow(() -> new TransferenciaException(
                        "Cuenta con ID " + id + " no encontrada",
                        TipoError.CUENTA_NO_ENCONTRADA));
    }

    /**
     * Crea una nueva cuenta con los datos proporcionados.
     *
     * @param nombre Nombre del propietario de la cuenta
     * @param cantidadInicial Saldo inicial de la cuenta
     * @return La cuenta creada con su ID generado
     * @throws TransferenciaException Si los datos son inválidos
     */
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

    /**
     * Realiza una transferencia de fondos entre dos cuentas.
     *
     * @param origen ID de la cuenta origen
     * @param destino ID de la cuenta destino
     * @param cantidad Monto a transferir
     * @throws TransferenciaException Si la transferencia no puede completarse por algún motivo
     */
    public void transferir(long origen, long destino, BigDecimal cantidad) {
        // Validar que la cantidad sea positiva
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransferenciaException("La cantidad a transferir debe ser mayor que cero",
                    TipoError.CANTIDAD_INVALIDA);
        }

        // Validar que no sea la misma cuenta
        if (origen == destino) {
            throw new TransferenciaException("No se puede transferir a la misma cuenta",
                    TipoError.CANTIDAD_INVALIDA);
        }

        // Obtener las cuentas
        Cuenta cuentaOrigen = repo.findById(origen)
                .orElseThrow(() -> new TransferenciaException("Cuenta de origen no encontrada",
                        TipoError.CUENTA_NO_ENCONTRADA));

        Cuenta cuentaDestino = repo.findById(destino)
                .orElseThrow(() -> new TransferenciaException("Cuenta de destino no encontrada",
                        TipoError.CUENTA_NO_ENCONTRADA));

        BigDecimal saldoOrigen = cuentaOrigen.getCantidad();

        // Verificar si hay saldo suficiente
        if (saldoOrigen.compareTo(cantidad) < 0) {
            throw new TransferenciaException("Saldo insuficiente en la cuenta de origen",
                    TipoError.SALDO_INSUFICIENTE);
        }

        BigDecimal saldoDestino = cuentaDestino.getCantidad();

        // Actualizar cantidades
        cuentaOrigen.setCantidad(saldoOrigen.subtract(cantidad));
        cuentaDestino.setCantidad(saldoDestino.add(cantidad));

        // Guardar cambios en la base de datos
        repo.save(cuentaOrigen);
        repo.save(cuentaDestino);
    }
}