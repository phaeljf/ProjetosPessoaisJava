package br.com.prosistema.service;

import br.com.prosistema.model.recebiveis.*;
import br.com.prosistema.util.LoggerUtil;
import com.fasterxml.jackson.databind.JsonNode;
import br.com.prosistema.model.recebiveis.PaymentRU;
import br.com.prosistema.model.recebiveis.ReceivableUnit;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ConversaoRecebiveis {

    public void processarFileHeader(JsonNode node, BufferedWriter writer) throws IOException {
        FileHeaderRecebiveis cabecalho = FileHeaderRecebiveis.builder()
                .recordType(node.path("recordType").asText())
                .processingDate(node.path("processingDate").asText())
                .acquiringName(node.path("acquiringName").asText())
                .fileTypeDescription(node.path("fileTypeDescription").asText())
                .fileNumber(node.path("fileNumber").asText())
                .clientName(node.path("client").path("name").asText())
                .clientDocument(node.path("client").path("document").asText())
                .processingType(node.path("processingType").asText())
                .fileLayoutVersion(node.path("fileLayoutVersion").asText())
                .recordNumber(node.path("recordNumber").asText())
                .build();

        String linha = String.join(",",
                cabecalho.getRecordType(),
                cabecalho.getProcessingDate(),
                cabecalho.getAcquiringName(),
                cabecalho.getFileTypeDescription(),
                cabecalho.getFileNumber(),
                cabecalho.getClientName(),
                cabecalho.getClientDocument(),
                cabecalho.getProcessingType(),
                cabecalho.getFileLayoutVersion(),
                cabecalho.getRecordNumber()
        );

        writer.write(linha);
        writer.newLine();
    }

    public void processarReceivableUnits(JsonNode node, BufferedWriter writer) throws IOException {
        JsonNode unidades = node.path("receivableUnits");
        if (!unidades.isArray()) return;

        for (JsonNode unitNode : unidades) {
            ReceivableUnit unidade = ReceivableUnit.builder()
                    .recordType(unitNode.path("recordType").asText())
                    .receivableUnitIdentifier(unitNode.path("receivableUnitIdentifier").asText())
                    .receivableUnitType(unitNode.path("receivableUnitType").asText())
                    .receivableUnitSituation(unitNode.path("receivableUnitSituation").asText())
                    .receivableUnitDate(unitNode.path("receivableUnitDate").asText())
                    .documentType(unitNode.path("documentType").asText())
                    .document(unitNode.path("document").asText())
                    .saleDate(unitNode.path("saleDate").asText())
                    .grossAmount(unitNode.path("grossAmount").asText())
                    .netAmount(unitNode.path("netAmount").asText())
                    .installmentNumber(unitNode.path("installmentNumber").asText())
                    .totalInstallments(unitNode.path("totalInstallments").asText())
                    .expectedPaymentDate(unitNode.path("expectedPaymentDate").asText())
                    .paymentDate(unitNode.path("paymentDate").asText())
                    .anticipationDate(unitNode.path("anticipationDate").asText())
                    .anticipationAmount(unitNode.path("anticipationAmount").asText())
                    .anticipationRate(unitNode.path("anticipationRate").asText())
                    .anticipationIOF(unitNode.path("anticipationIOF").asText())
                    .anticipationDiscount(unitNode.path("anticipationDiscount").asText())
                    .anticipationNetAmount(unitNode.path("anticipationNetAmount").asText())
                    .anticipationStatus(unitNode.path("anticipationStatus").asText())
                    .anticipationRequestDate(unitNode.path("anticipationRequestDate").asText())
                    .anticipationDeadlineDate(unitNode.path("anticipationDeadlineDate").asText())
                    .anticipationPaymentDate(unitNode.path("anticipationPaymentDate").asText())
                    .anticipationIdentifier(unitNode.path("anticipationIdentifier").asText())
                    .nsu(unitNode.path("nsu").asText())
                    .authorizationCode(unitNode.path("authorizationCode").asText())
                    .cardBrand(unitNode.path("cardBrand").asText())
                    .cardNumber(unitNode.path("cardNumber").asText())
                    .establishmentNumber(unitNode.path("establishmentNumber").asText())
                    .terminalNumber(unitNode.path("terminalNumber").asText())
                    .transactionType(unitNode.path("transactionType").asText())
                    .mdrAmount(unitNode.path("mdrAmount").asText())
                    .mdrRate(unitNode.path("mdrRate").asText())
                    .anticipationRequestStatus(unitNode.path("anticipationRequestStatus").asText())
                    .anticipationRequestProtocol(unitNode.path("anticipationRequestProtocol").asText())
                    .saleType(unitNode.path("saleType").asText())
                    .build();

            JsonNode pagamentos = unitNode.path("paymentRU");

            if (!pagamentos.isArray() || !pagamentos.elements().hasNext()) {
                // Sem pagamentos -> linha com PaymentRU zerado
                String linha = juntarCampos(getReceivableUnitFields(unidade), getPaymentRUFields(null));
                writer.write(linha);
                writer.newLine();
            } else {
                for (JsonNode payNode : pagamentos) {
                    PaymentRU p = PaymentRU.builder()
                            .recordType(payNode.path("recordType").asText())
                            .paymentDate(payNode.path("paymentDate").asText())
                            .paymentGrossAmount(payNode.path("paymentGrossAmount").asText())
                            .paymentNetAmount(payNode.path("paymentNetAmount").asText())
                            .documentType(payNode.path("documentType").asText())
                            .document(payNode.path("document").asText())
                            .anticipationGrossAmount(payNode.path("anticipationGrossAmount").asText())
                            .anticipationNetAmount(payNode.path("anticipationNetAmount").asText())
                            .anticipationDiscount(payNode.path("anticipationDiscount").asText())
                            .anticipationRate(payNode.path("anticipationRate").asText())
                            .anticipationIOF(payNode.path("anticipationIOF").asText())
                            .anticipationDate(payNode.path("anticipationDate").asText())
                            .anticipationStatus(payNode.path("anticipationStatus").asText())
                            .anticipationIdentifier(payNode.path("anticipationIdentifier").asText())
                            .anticipationProtocol(payNode.path("anticipationProtocol").asText())
                            .receivableUnitIdentifier(payNode.path("receivableUnitIdentifier").asText())
                            .build();

                    String linha = juntarCampos(getReceivableUnitFields(unidade), getPaymentRUFields(p));
                    writer.write(linha);
                    writer.newLine();
                }
            }
        }
    }

    private List<String> getReceivableUnitFields(ReceivableUnit u) {
        if (u == null) return Collections.nCopies(38, "0");
        return List.of(
                u.getRecordType(), u.getReceivableUnitIdentifier(), u.getReceivableUnitType(),
                u.getReceivableUnitSituation(), u.getReceivableUnitDate(), u.getDocumentType(),
                u.getDocument(), u.getSaleDate(), u.getGrossAmount(), u.getNetAmount(),
                u.getInstallmentNumber(), u.getTotalInstallments(), u.getExpectedPaymentDate(),
                u.getPaymentDate(), u.getAnticipationDate(), u.getAnticipationAmount(),
                u.getAnticipationRate(), u.getAnticipationIOF(), u.getAnticipationDiscount(),
                u.getAnticipationNetAmount(), u.getAnticipationStatus(), u.getAnticipationRequestDate(),
                u.getAnticipationDeadlineDate(), u.getAnticipationPaymentDate(), u.getAnticipationIdentifier(),
                u.getNsu(), u.getAuthorizationCode(), u.getCardBrand(), u.getCardNumber(),
                u.getEstablishmentNumber(), u.getTerminalNumber(), u.getTransactionType(),
                u.getMdrAmount(), u.getMdrRate(), u.getAnticipationRequestStatus(),
                u.getAnticipationRequestProtocol(), u.getSaleType()
        );
    }

    private List<String> getPaymentRUFields(PaymentRU p) {
        if (p == null) return Collections.nCopies(16, "0");
        return List.of(
                p.getRecordType(), p.getPaymentDate(), p.getPaymentGrossAmount(),
                p.getPaymentNetAmount(), p.getDocumentType(), p.getDocument(),
                p.getAnticipationGrossAmount(), p.getAnticipationNetAmount(),
                p.getAnticipationDiscount(), p.getAnticipationRate(),
                p.getAnticipationIOF(), p.getAnticipationDate(),
                p.getAnticipationStatus(), p.getAnticipationIdentifier(),
                p.getAnticipationProtocol(), p.getReceivableUnitIdentifier()
        );
    }

    private String juntarCampos(List<String>... listas) {
        return Stream.of(listas)
                .flatMap(List::stream)
                .collect(Collectors.joining(","));
    }
}

}

