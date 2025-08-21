package br.com.prosistema.model.recebiveis;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileTrailerRU {
    private String recordType;                        // "999"
    private String clientDocument;                    // CNPJ do cliente
    private String receivableUnitQuantity;            // Quantidade total de URs
    private String grossAmountTotal;                  // Valor bruto total
    private String grossAmountTotalField;             // Campo formatado
    private String freeNegotiationAmountTotal;        // Total livre negociação
    private String freeNegotiationAmountTotalField;
    private String paidAmountTotal;                   // Total pago
    private String paidAmountTotalField;
    private String allocatedAmountTotal;              // Total alocado
    private String allocatedAmountTotalField;
    private String receivableUnitAmountTotal;         // Total agendado (UR)
    private String receivableUnitAmountTotalField;
    private String recordNumber;                      // Nº de registro no arquivo
}
