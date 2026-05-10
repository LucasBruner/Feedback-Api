package br.com.fiap.techchallenge.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Avaliacao {

    @JsonProperty("id")
    private String id;

    @NotBlank(message = "Descrição é obrigatória")
    @JsonProperty("descricao")
    private String descricao;

    @NotNull(message = "Nota é obrigatória")
    @Min(value = 0, message = "Nota mínima é 0")
    @Max(value = 10, message = "Nota máxima é 10")
    @JsonProperty("nota")
    private Integer nota;

    @JsonProperty("urgencia")
    private NivelUrgencia urgencia;

    @JsonProperty("dataHora")
    private LocalDateTime dataHora;

    public void inicializar() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.dataHora == null) {
            this.dataHora = LocalDateTime.now();
        }
    }

    public void calcularUrgencia(int threshold) {
        if (nota <= threshold) {
            this.urgencia = NivelUrgencia.CRITICO;
        } else if (nota <= 6) {
            this.urgencia = NivelUrgencia.ALTO;
        } else {
            this.urgencia = NivelUrgencia.NORMAL;
        }
    }

    public enum NivelUrgencia {
        CRITICO,
        ALTO,
        NORMAL
    }
}
