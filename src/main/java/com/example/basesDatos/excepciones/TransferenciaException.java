package com.example.basesDatos.excepciones;

/**
 * Excepción personalizada para manejar errores específicos durante las operaciones de transferencia.
 * Permite categorizar los errores según su tipo para facilitar el manejo adecuado en el controlador.
 */
public class TransferenciaException extends RuntimeException {
    private final TipoError tipoError;

    /**
     * Enumeración de los tipos de errores que pueden ocurrir en las transferencias.
     * Facilita la clasificación de errores para determinar el código HTTP adecuado.
     */
    public enum TipoError {
        SALDO_INSUFICIENTE,    // Cuando la cuenta origen no tiene fondos suficientes
        CUENTA_NO_ENCONTRADA,  // Cuando alguna de las cuentas especificadas no existe
        CANTIDAD_INVALIDA,     // Cuando el monto a transferir es cero o negativo
        ERROR_GENERAL          // Para cualquier otro error no específico
    }

    /**
     * Constructor que recibe un mensaje descriptivo y el tipo de error.
     *
     * @param mensaje Descripción del error
     * @param tipoError Tipo de error según la enumeración TipoError
     */
    public TransferenciaException(String mensaje, TipoError tipoError) {
        super(mensaje);
        this.tipoError = tipoError;
    }

    /**
     * Obtiene el tipo de error asociado a esta excepción.
     *
     * @return El tipo de error categorizado
     */
    public TipoError getTipoError() {
        return tipoError;
    }
}