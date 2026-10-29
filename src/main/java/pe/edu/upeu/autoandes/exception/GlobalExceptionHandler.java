package pe.edu.upeu.autoandes.exception;

import jakarta.servlet.http.HttpServletRequest;
import pe.edu.upeu.autoandes.exception.dto.ErrorResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(
            RecursoNoEncontradoException ex,
            HttpServletRequest request
    ) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationFailure(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        log.warn("Inicio de sesión rechazado en {}", request.getRequestURI());
        return respuesta(HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Credenciales incorrectas o cuenta inactiva",
                request,
                null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessRule(
            ReglaNegocioException ex,
            HttpServletRequest request
    ) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidRequest(
            SolicitudInvalidaException ex,
            HttpServletRequest request
    ) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                campos.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        log.warn("Solicitud rechazada por validación en {}: {}", request.getRequestURI(), campos);
        return respuesta(HttpStatus.BAD_REQUEST, "Datos inválidos", request, campos);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleMalformedRequest(
            Exception ex,
            HttpServletRequest request
    ) {
        return respuesta(HttpStatus.BAD_REQUEST, "La solicitud contiene datos inválidos", request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataConflict(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        return respuesta(HttpStatus.CONFLICT, "La operación entra en conflicto con los datos existentes", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Error inesperado en ruta {}", request.getRequestURI(), ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", request, null);
    }

    private ResponseEntity<ErrorResponseDTO> respuesta(
            HttpStatus status,
            String mensaje,
            HttpServletRequest request,
            Map<String, String> campos
    ) {
        return respuesta(status, status.getReasonPhrase(), mensaje, request, campos);
    }

    private ResponseEntity<ErrorResponseDTO> respuesta(
            HttpStatus status,
            String error,
            String mensaje,
            HttpServletRequest request,
            Map<String, String> campos
    ) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                error,
                mensaje,
                request.getRequestURI(),
                campos
        );
        return ResponseEntity.status(status).body(body);
    }
}
