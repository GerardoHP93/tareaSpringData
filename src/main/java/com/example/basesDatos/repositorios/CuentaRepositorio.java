package com.example.basesDatos.repositorios;

import com.example.basesDatos.modelos.Cuenta;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface CuentaRepositorio extends CrudRepository<Cuenta, Long> {

    public void findCuentaByNombre(String nombre);

    @Modifying
    @Query("UPDATE cuenta SET cantidad = :cantidad WHERE id = :id")
    public void actualizarCantidad(@Param("id") Long id, @Param("cantidad") BigDecimal cantidad);
}
