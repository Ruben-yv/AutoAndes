package pe.edu.upeu.autoandes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrdenServicioRequestDTO {
    @NotNull(message = "El vehículo es obligatorio")
    @Positive(message = "El identificador del vehículo debe ser válido")
    private Long vehiculoId;

    @NotNull(message = "El kilometraje es obligatorio")
    @Min(value = 0, message = "El kilometraje no puede ser negativo")
    @Max(value = 9_999_999, message = "El kilometraje excede el máximo permitido")
    private Integer kilometraje;

    @NotEmpty(message = "La orden debe contener al menos un detalle")
    @Size(max = 10, message = "La orden admite como máximo 10 detalles")
    @Valid
    private List<DetalleOrdenRequestDTO> detalles;
}
