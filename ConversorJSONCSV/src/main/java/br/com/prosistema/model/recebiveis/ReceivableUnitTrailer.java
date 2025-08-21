package br.com.prosistema.model.recebiveis;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivableUnitTrailer {
    private String recordType;                      // "200"
    private String clientDocument;                  // CNPJ do cliente
    private CardScheme cardScheme;                  // Objeto com código e descrição da bandeira
    private String cardSchemeDescription;           // Ex: "VCD - Visa Débito"
    private String receivableUnitQuantity;          // Quantidade de URs
    private String grossAmountSum;
    private String grossAmountSumField;
    private String freeNegotiationAmountSum;
    private String freeNegotiationAmountSumField;
    private String paidAmountSum;
    private String paidAmountSumField;
    private String allocatedAmountSum;
    private String allocatedAmountSumField;
    private String receivableUnitAmountSum;
    private String receivableUnitAmountSumField;
    private String recordNumber;                    // Nº do registro no arquivo
}
