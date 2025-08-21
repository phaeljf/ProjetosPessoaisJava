package br.com.prosistema.model.recebiveis;

import java.util.List;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivableUnit {
    // Campos diretos da unidade
    private String recordType;
    private String receivableUnitIdentifier;
    private String receivableUnitType;
    private String receivableUnitSituation;
    private String receivableUnitDate;
    private String documentType;
    private String document;
    private String saleDate;
    private String grossAmount;
    private String netAmount;
    private String installmentNumber;
    private String totalInstallments;
    private String expectedPaymentDate;
    private String paymentDate;
    private String anticipationDate;
    private String anticipationAmount;
    private String anticipationRate;
    private String anticipationIOF;
    private String anticipationDiscount;
    private String anticipationNetAmount;
    private String anticipationStatus;
    private String anticipationRequestDate;
    private String anticipationDeadlineDate;
    private String anticipationPaymentDate;
    private String anticipationIdentifier;
    private String nsu;
    private String authorizationCode;
    private String cardBrand;
    private String cardNumber;
    private String establishmentNumber;
    private String terminalNumber;
    private String transactionType;
    private String mdrAmount;
    private String mdrRate;
    private String anticipationRequestStatus;
    private String anticipationRequestProtocol;
    private String saleType;

    // Filhos que geram novas linhas
    private List<PaymentRU> paymentRU;
    private List<ContractsNegotiatedRU> contractsNegotiatedRU;
    private List<DetailEventsDeductionsRU> detailEventsDeductionsRU;
}
