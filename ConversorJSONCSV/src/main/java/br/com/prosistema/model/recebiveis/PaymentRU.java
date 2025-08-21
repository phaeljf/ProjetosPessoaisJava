package br.com.prosistema.model.recebiveis;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRU {
    private String recordType;
    private String paymentDate;
    private String paymentGrossAmount;
    private String paymentNetAmount;
    private String documentType;
    private String document;
    private String anticipationGrossAmount;
    private String anticipationNetAmount;
    private String anticipationDiscount;
    private String anticipationRate;
    private String anticipationIOF;
    private String anticipationDate;
    private String anticipationStatus;
    private String anticipationIdentifier;
    private String anticipationProtocol;
    private String receivableUnitIdentifier;
}
