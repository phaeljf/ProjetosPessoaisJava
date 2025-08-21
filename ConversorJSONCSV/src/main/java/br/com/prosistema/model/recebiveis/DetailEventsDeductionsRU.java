package br.com.prosistema.model.recebiveis;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailEventsDeductionsRU {

    private String recordType; // 003
    private String idUr;
    private String receivableUnitKey;
    private String transactionTypeDescription;
    private String eventDate;
    private String classCategoryCode;
    private String classCategoryDescription;
    private String socOperationType;
    private String originalTransactionDate;
    private String maskedCardNumber;
    private String authorizationCode;
    private String retrievalReferenceNumber;
    private String clientControlNumber;
    private String salesReferenceNumber;
    private String grossAmount;
    private String discountAmount;
    private String netAmount;
    private String netAmountParcel;
    private String netAmountAdjustedDeduction;
    private String netAmountRU;
    private String installmentNumber;
    private String totalInstallments;
    private String acquirerReference;
    private String recordNumber;
}
