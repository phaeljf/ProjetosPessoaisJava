package br.com.prosistema.model.recebiveis;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractsNegotiatedRU {
    private String recordType;
    private String idUr;
    private String receivableUnitKey;
    private String contractIdentifier;
    private String contractEffectIndicator;
    private String contractEffectTypeDescription;
    private String divisionRuleDescription;
    private String commitedAmountField;
    private String effectContractAmountField;
    private String cpfCnpjContractBeneficiary;
    private String recordNumber;
}
