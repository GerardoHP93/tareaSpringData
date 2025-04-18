# Ejercicio: Conceptos básicos de Spring DATA
- **Nombre:** Gerardo Isidro Herrera Pacheco
- **Matrícula:** ISC 68612
- **Semestre:** 8vo
- **Materia**: Temas selectos de Programación
- **Maestro:** Jose C Aguilar Canepa
- **Institución:** Universidad Autónoma de Campeche, Facultad de Ingeniería

# Aplicación de Transferencias de saldos entre cuentas con Spring Data

## Descripción
Esta aplicación es un sistema sencillo de simulación de transferencias bancarias implementado con Spring Boot y Spring Data. Permite gestionar cuentas bancarias y realizar transferencias entre ellas, ofreciendo endpoints RESTful para consultar, crear cuentas y realizar transferencias con validaciones apropiadas.

## Estructura del Proyecto
```
basesDatos/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.example.basesDatos/
│   │   │       ├── controladores/
│   │   │       │   └── FrontController.java
│   │   │       ├── excepciones/
│   │   │       │   └── TransferenciaException.java
│   │   │       ├── modelos/
│   │   │       │   └── Cuenta.java
│   │   │       ├── repositorios/
│   │   │       │   └── CuentaRepositorio.java
│   │   │       ├── servicios/
│   │   │       │   └── TransferenciaServicio.java
│   │   │       └── BasesDatosApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       ├── templates/
│   │       └── application.properties
│   └── test/
└── pom.xml
```

## Diagrama de Clases

![Diagrama clases Transferencias](https://github.com/user-attachments/assets/279102b0-62d1-4a24-aa46-045a6c1a923c)


## Componentes y su Interacción

### Modelo - Capa de Datos

#### Cuenta
La clase `Cuenta` representa la entidad principal de la aplicación. Contiene un ID, un nombre de propietario, y un saldo

```java

public class Cuenta {
    @Id
    private Long id;
    private String nombre;
    private BigDecimal cantidad;
    // Getters y setters
}
```

La anotación `@Id` identifica el campo que actúa como clave primaria.

### Repositorio - Capa de Acceso a Datos

#### CuentaRepositorio
El repositorio extiende `CrudRepository` de Spring Data, proporcionando métodos CRUD básicos para interactuar con la base de datos.

```java
@Repository
public interface CuentaRepositorio extends CrudRepository<Cuenta, Long> {
    public void findCuentaByNombre(String nombre);
    
    @Modifying
    @Query("UPDATE cuenta SET cantidad = :cantidad WHERE id = :id")
    public void actualizarCantidad(@Param("id") Long id, @Param("cantidad") BigDecimal cantidad);
}
```

Spring Data JDBC implementa automáticamente los métodos CRUD básicos como `findAll()`, `findById()`, `save()`, etc. También permite crear consultas personalizadas mediante el uso de anotaciones como `@Query`.

### Servicio - Capa de Lógica de Negocio

#### TransferenciaServicio
El servicio encapsula la lógica de negocio para las operaciones con cuentas, como listar, crear y realizar transferencias.

```java
@Service
public class TransferenciaServicio {
    private final CuentaRepositorio repo;
    
    // Constructor para inyección de dependencias
    
    // Métodos para listar cuentas, obtener cuenta por ID, crear cuenta y transferir
}
```

Este servicio maneja las validaciones de negocio, como verificar saldo suficiente antes de una transferencia, garantizar que los datos son válidos antes de crear una cuenta, etc.

### Excepciones Personalizadas

#### TransferenciaException
Clase personalizada para manejar excepciones específicas de la aplicación.

```java
public class TransferenciaException extends RuntimeException {
    private final TipoError tipoError;
    
    public enum TipoError {
        SALDO_INSUFICIENTE,
        CUENTA_NO_ENCONTRADA,
        CANTIDAD_INVALIDA,
        ERROR_GENERAL
    }
    
    // Constructor y getters
}
```

Esta clase permite categorizar los errores y proporcionar mensajes específicos que se traducirán a respuestas HTTP apropiadas.

### Controlador - Capa de Presentación REST

#### FrontController
El controlador expone los endpoints REST para interactuar con la aplicación.

```java
@RestController
public class FrontController {
    private final TransferenciaServicio servicio;
    
    // Constructor para inyección de dependencias
    
    @GetMapping("/")
    public Iterable<Cuenta> index() {...}
    
    @PostMapping("/crear")
    public ResponseEntity<?> crearCuenta(...) {...}
    
    @PostMapping("/transferir")
    public ResponseEntity<String> transferir(...) {...}
}
```

Este controlador utiliza anotaciones como `@RestController`, `@GetMapping` y `@PostMapping` para mapear las solicitudes HTTP a métodos específicos. Utiliza `ResponseEntity` para construir respuestas HTTP con códigos de estado apropiados.

## Implementación de Spring Data

En este proyecto se implementan varios conceptos clave de Spring Data:

1. **Entidades y Mapeo Objeto-Relacional**: El uso de modelos marcando un identificador como `@Id` permite mapear objetos Java a registros de la base de datos.

2. **Repositorios**: La extensión de `CrudRepository` proporciona métodos CRUD estándar sin necesidad de implementarlos.

3. **Consultas Personalizadas**: El uso de la anotación `@Query` permite definir consultas SQL personalizadas.

4. **Spring Data JDBC**: El proyecto utiliza Spring Data JDBC para una interacción más simple con la base de datos, evitando la complejidad adicional de JPA completo.

5. **Inyección de Dependencias**: Los componentes utilizan inyección por constructor para obtener sus dependencias, siguiendo las mejores prácticas de Spring.

## Endpoints REST

- `GET /` - Lista todas las cuentas
- `POST /crear` - Crea una nueva cuenta con un nombre y saldo inicial
- `POST /transferir` - Realiza una transferencia entre dos cuentas

### Ejemplos de Uso de Endpoints

A continuación se muestran ejemplos de cómo se usaron los endpoints de la aplicación utilizando Postman, aunque tambien se puede usar otro cualquier cliente HTTP:



https://github.com/user-attachments/assets/2a42d018-3920-4df7-a3bc-f35f537d9776



#### Listar todas las cuentas
```
GET http://localhost:8080/
```

#### Crear una nueva cuenta
```
POST http://localhost:8080/crear?nombre=Gerardo&cantidadInicial=0
```
Este endpoint creará una nueva cuenta con el nombre "Gerardo" y un saldo inicial de 0. La respuesta incluirá los detalles de la cuenta creada, incluyendo su ID generado automáticamente.

#### Realizar una transferencia
```
POST http://localhost:8080/transferir?origen=1&destino=2&cantidad=50
```
Este endpoint transferirá 50 unidades de la cuenta con ID 1 a la cuenta con ID 2. Si la transferencia es exitosa, se mostrará un mensaje detallado confirmando la operación.

#### Manejo de errores
La API maneja diversos escenarios de error y devuelve códigos de estado HTTP apropiados junto con mensajes descriptivos:

1. **Saldo insuficiente**:
   ```
   POST http://localhost:8080/transferir?origen=4&destino=1&cantidad=100
   ```
   Si la cuenta con ID 4 no tiene suficiente saldo para transferir 100 unidades, se devolverá un error HTTP 400 (Bad Request) con un mensaje explicativo.

2. **Cuenta no encontrada**:
   ```
   POST http://localhost:8080/transferir?origen=999&destino=1&cantidad=50
   ```
   Si se intenta transferir desde una cuenta que no existe (ID 999), se devolverá un error HTTP 404 (Not Found).

3. **Cantidad inválida**:
   ```
   POST http://localhost:8080/transferir?origen=1&destino=2&cantidad=-50
   ```
   Si se intenta transferir una cantidad negativa, se devolverá un error HTTP 400 (Bad Request).

## Librerías Utilizadas

El proyecto utiliza las siguientes dependencias principales:

- **Spring Boot Starter Data JDBC**: Proporciona soporte para Spring Data JDBC
- **Spring Boot Starter Web**: Proporciona soporte para aplicaciones web, incluyendo RESTful
- **MySQL Connector/J**: Driver JDBC para MySQL
- **Spring Boot Starter Test**: Herramientas para pruebas

Versiones:
- Spring Boot: 3.4.4
- Java: 17
