package br.com.prosistema.TipoArquivos;

import br.com.prosistema.util.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConversaoVendas {

    private final ObjectMapper mapper = new ObjectMapper();

    public void converter(File arquivoJson, File pastaDestino) {
        try {
            JsonNode raiz = mapper.readTree(arquivoJson);
            StringBuilder csv = new StringBuilder();

            Logger.info("🔄 Iniciando conversão do arquivo: " + arquivoJson.getName());

            processarFileHeader(raiz, csv);                      // 000
            processarClientHeaders(raiz, csv);                   // 101
            processarPixTransactions(raiz, csv);                 // 001
            processarDebitSales(raiz, csv);                      // 010 → 011
            processarCreditSales(raiz, csv);                     // 012 → 013
            processarSalesInstallments(raiz, csv);               // 014 → 015
            processarInstallmentAcceleration(raiz, csv);         // 018 → 019
            processarCancelamentos(raiz, csv);                   // 110
            processarChargebacks(raiz, csv);                     // 111
            processarClientFileTrailers(raiz, csv);              // 201
            processarFinalFileTrailer(raiz, csv);                // 999

            salvarCsv(pastaDestino, arquivoJson.getName(), csv.toString());

        } catch (Exception e) {
            Logger.erro("Erro ao converter arquivo: " + arquivoJson.getName());
            e.printStackTrace();
        }
    }

    // ===== Métodos IMPLEMENTADOS =====
    private void processarFileHeader(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarFileHeader chamado");

        JsonNode header = raiz.path("fileHeader");
        if (header.isMissingNode()) {
            Logger.erro("Bloco fileHeader não encontrado.");
            return;
        }

        csv.append(header.path("recordType").asText("")).append(",");
        csv.append(header.path("processingDate").asText("")).append(",");
        csv.append(header.path("acquiringName").asText("")).append(",");
        csv.append(header.path("fileTypeDescription").asText("")).append(",");
        csv.append(header.path("fileNumber").asText("")).append(",");

            JsonNode client = header.path("client");
            csv.append(client.path("code").asText("")).append(",");
            csv.append(client.path("name").asText("")).append(",");
            csv.append(client.path("document").asText("")).append(",");

        csv.append(header.path("processingType").asText("")).append(",");
        csv.append(header.path("fileLayoutVersion").asText("")).append(",");
        csv.append(header.path("recordNumber").asText("")).append("\n");
    }

    private void processarClientHeaders(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarClientHeaders chamado");

        JsonNode headers = raiz.path("clientHeaders");
        if (headers == null || !headers.isArray() || headers.size() == 0) {
            csv.append("0\n");
            return;
        }

        for (JsonNode h : headers) {
            csv.append(h.path("recordType").asText("")).append(",");
            csv.append(h.path("processingDate").asText("")).append(",");

            JsonNode matrix = h.path("matrixClient");
            csv.append(matrix.path("code").asText("")).append(",");
            csv.append(matrix.path("name").asText("")).append(",");
            csv.append(matrix.path("document").asText("")).append(",");

            JsonNode branch = h.path("branchClient");
            csv.append(branch.path("code").asText("")).append(",");
            csv.append(branch.path("name").asText("")).append(",");
            csv.append(branch.path("document").asText("")).append(",");
            csv.append(h.path("recordNumber").asText("")).append("\n");
        }
    }

    private void processarPixTransactions(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarPixTransactions chamado");

        JsonNode lista = raiz.path("pixTransactions");
        if (lista == null || !lista.isArray() || lista.size() == 0) {
            csv.append("0\n");
            return;
        }

        for (JsonNode p : lista) {
            csv.append(p.path("recordType").asText("")).append(",");
            csv.append(p.path("institutionCode").asText("")).append(",");
            csv.append(p.path("serviceContractCode").asText("")).append(",");
            csv.append(p.path("document").asText("")).append(",");
            csv.append(p.path("merchantId").asText("")).append(",");
            csv.append(p.path("terminalRegister").asText("")).append(",");
            csv.append(p.path("pspCode").asText("")).append(",");
            csv.append(p.path("qrCodeStatus").asText("")).append(",");
            csv.append(p.path("dateQRCodeGenerated").asText("")).append(",");
            csv.append(p.path("hourQRCodeGenerated").asText("")).append(",");
            csv.append(p.path("dateQrCodeConfirmationReceived").asText("")).append(",");
            csv.append(p.path("hourQrCodeConfirmationReceived").asText("")).append(",");
            csv.append(p.path("amountTransaction").asText("")).append(",");
            csv.append(p.path("walletAuthorizerName").asText("")).append(",");
            csv.append(p.path("nsuTransaction").asText("")).append(",");
            csv.append(p.path("undoneResponseCode").asText("")).append(",");
            csv.append(p.path("undoneDateTransaction").asText("")).append(",");
            csv.append(p.path("undoneHourTransaction").asText("")).append(",");
            csv.append(p.path("undoneReason").asText("")).append(",");
            csv.append(p.path("referenceNumberFEPAS").asText("")).append(",");
            csv.append(p.path("authorizationCode").asText("")).append(",");
            csv.append(p.path("systemRetrievalReferenceNumber").asText("")).append(",");
            csv.append(p.path("recordNumber").asText("")).append("\n");
        }
    }

    private void processarDebitSales(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarDebitSales chamado");

        JsonNode lista = raiz.path("debitSalesSummary");
        if (lista == null || !lista.isArray() || lista.size() == 0) {
            csv.append("0\n");
            return;
        }

        for (JsonNode resumo : lista) {
            // Linha 010
            csv.append(resumo.path("recordType").asText("")).append(",");
            csv.append(resumo.path("matrixClientCode").asText("")).append(",");
            csv.append(resumo.path("clientCode").asText("")).append(",");
            csv.append(resumo.path("salesDate").asText("")).append(",");
            csv.append(resumo.path("salesSummaryNumber").asText("")).append(",");
            csv.append(resumo.path("quantityOfSales").asText("")).append(",");
            csv.append(resumo.path("cardSchemeDescription").asText("")).append(",");
            csv.append(resumo.path("transactionTypeDescription").asText("")).append(",");
            csv.append(resumo.path("transactionStatusDescription").asText("")).append(",");
            csv.append(resumo.path("grossAmount").asText("")).append(",");
            csv.append(resumo.path("discountAmount").asText("")).append(",");
            csv.append(resumo.path("netAmount").asText("")).append(",");
            csv.append(resumo.path("creditDate").asText("")).append(",");
            csv.append(resumo.path("cashBackAmount").asText("")).append(",");
            csv.append(resumo.path("reverseInterchange").asText("")).append(",");
            csv.append(resumo.path("bank").asText("")).append(",");
            csv.append(resumo.path("agency").asText("")).append(",");
            csv.append(resumo.path("accountTypeDescription").asText("")).append(",");
            csv.append(resumo.path("accountNumber").asText("")).append(",");
            csv.append(resumo.path("recordNumber").asText("")).append("\n");

            // Linhas 011
            JsonNode recibos = resumo.path("debitSalesReceipt");
            if (recibos != null && recibos.isArray()) {
                for (JsonNode r : recibos) {
                    csv.append(r.path("recordType").asText("")).append(",");
                    csv.append(r.path("clientCode").asText("")).append(",");
                    csv.append(r.path("salesDate").asText("")).append(",");
                    csv.append(r.path("salesSummaryNumber").asText("")).append(",");
                    csv.append(r.path("salesReceiptNumber").asText("")).append(",");
                    csv.append(r.path("cardSchemeDescription").asText("")).append(",");
                    csv.append(r.path("transactionTypeDescription").asText("")).append(",");
                    csv.append(r.path("maskedCardNumber").asText("")).append(",");
                    csv.append(r.path("authorizationCode").asText("")).append(",");
                    csv.append(r.path("transactionDateTime").asText("")).append(",");
                    csv.append(r.path("terminalNumber").asText("")).append(",");
                    csv.append(r.path("technologyTypeDescription").asText("")).append(",");
                    csv.append(r.path("entryModeDescription").asText("")).append(",");
                    csv.append(r.path("cardTypeDescription").asText("")).append(",");
                    csv.append(r.path("grossAmount").asText("")).append(",");
                    csv.append(r.path("discountAmount").asText("")).append(",");
                    csv.append(r.path("netAmount").asText("")).append(",");
                    csv.append(r.path("creditDate").asText("")).append(",");
                    csv.append(r.path("feePercent").asText("")).append(",");
                    csv.append(r.path("splitOriginalSlip").asText("")).append(",");
                    csv.append(r.path("splitDiscountAmount").asText("")).append(",");
                    csv.append(r.path("splitGrossAmount").asText("")).append(",");
                    csv.append(r.path("recordNumber").asText("")).append("\n");
                }
            }
        }
    }

    private void processarCreditSales(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarCreditSales chamado");

        JsonNode lista = raiz.path("creditSalesSummary");
        if (lista == null || !lista.isArray() || lista.size() == 0) {
            csv.append("0\n");
            return;
        }

        for (JsonNode resumo : lista) {
            // Linha 012
            csv.append(resumo.path("recordType").asText("")).append(",");
            csv.append(resumo.path("matrixClientCode").asText("")).append(",");
            csv.append(resumo.path("clientCode").asText("")).append(",");
            csv.append(resumo.path("salesDate").asText("")).append(",");
            csv.append(resumo.path("salesSummaryNumber").asText("")).append(",");
            csv.append(resumo.path("quantityOfSales").asText("")).append(",");
            csv.append(resumo.path("cardSchemeDescription").asText("")).append(",");
            csv.append(resumo.path("transactionTypeDescription").asText("")).append(",");
            csv.append(resumo.path("transactionStatusDescription").asText("")).append(",");
            csv.append(resumo.path("grossAmount").asText("")).append(",");
            csv.append(resumo.path("discountAmount").asText("")).append(",");
            csv.append(resumo.path("netAmount").asText("")).append(",");
            csv.append(resumo.path("creditDate").asText("")).append(",");
            csv.append(resumo.path("bank").asText("")).append(",");
            csv.append(resumo.path("agency").asText("")).append(",");
            csv.append(resumo.path("accountTypeDescription").asText("")).append(",");
            csv.append(resumo.path("accountNumber").asText("")).append(",");
            csv.append(resumo.path("recordNumber").asText("")).append("\n");

            // Linhas 013
            JsonNode recibos = resumo.path("creditSalesReceipt");
            if (recibos != null && recibos.isArray()) {
                for (JsonNode r : recibos) {
                    csv.append(r.path("recordType").asText("")).append(",");
                    csv.append(r.path("clientCode").asText("")).append(",");
                    csv.append(r.path("salesDate").asText("")).append(",");
                    csv.append(r.path("salesSummaryNumber").asText("")).append(",");
                    csv.append(r.path("salesReceiptNumber").asText("")).append(",");
                    csv.append(r.path("cardSchemeDescription").asText("")).append(",");
                    csv.append(r.path("transactionTypeDescription").asText("")).append(",");
                    csv.append(r.path("maskedCardNumber").asText("")).append(",");
                    csv.append(r.path("authorizationCode").asText("")).append(",");
                    csv.append(r.path("transactionDateTime").asText("")).append(",");
                    csv.append(r.path("terminalNumber").asText("")).append(",");
                    csv.append(r.path("technologyTypeDescription").asText("")).append(",");
                    csv.append(r.path("entryModeDescription").asText("")).append(",");
                    csv.append(r.path("cardTypeDescription").asText("")).append(",");
                    csv.append(r.path("grossAmount").asText("")).append(",");
                    csv.append(r.path("discountAmount").asText("")).append(",");
                    csv.append(r.path("netAmount").asText("")).append(",");
                    csv.append(r.path("creditDate").asText("")).append(",");
                    csv.append(r.path("feePercent").asText("")).append(",");
                    csv.append(r.path("splitOriginalSlip").asText("")).append(",");
                    csv.append(r.path("splitDiscountAmount").asText("")).append(",");
                    csv.append(r.path("splitGrossAmount").asText("")).append(",");
                    csv.append(r.path("recordNumber").asText("")).append("\n");
                }
            }
        }
    }

    private void processarSalesInstallments(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarSalesInstallments chamado");

        JsonNode lista = raiz.path("salesInstallmentTransaction");
        if (lista == null || !lista.isArray() || lista.size() == 0) {
            csv.append("0\n");
            return;
        }

        for (JsonNode resumo : lista) {
            // Linha 014
            csv.append(resumo.path("recordType").asText("")).append(",");
            csv.append(resumo.path("matrixClientCode").asText("")).append(",");
            csv.append(resumo.path("clientCode").asText("")).append(",");
            csv.append(resumo.path("salesDate").asText("")).append(",");
            csv.append(resumo.path("salesSummaryNumber").asText("")).append(",");
            csv.append(resumo.path("quantityOfSales").asText("")).append(",");
            csv.append(resumo.path("cardSchemeDescription").asText("")).append(",");
            csv.append(resumo.path("cardTypeDescription").asText("")).append(",");
            csv.append(resumo.path("transactionTypeDescription").asText("")).append(",");
            csv.append(resumo.path("transactionStatusDescription").asText("")).append(",");
            csv.append(resumo.path("maskedCardNumber").asText("")).append(",");
            csv.append(resumo.path("authorizationCode").asText("")).append(",");
            csv.append(resumo.path("transactionDateTime").asText("")).append(",");
            csv.append(resumo.path("terminalNumber").asText("")).append(",");
            csv.append(resumo.path("technologyTypeDescription").asText("")).append(",");
            csv.append(resumo.path("entryModeDescription").asText("")).append(",");
            csv.append(resumo.path("grossAmount").asText("")).append(",");
            csv.append(resumo.path("discountAmount").asText("")).append(",");
            csv.append(resumo.path("netAmount").asText("")).append(",");
            csv.append(resumo.path("creditDate").asText("")).append(",");
            csv.append(resumo.path("totalInstallments").asText("")).append(",");
            csv.append(resumo.path("bank").asText("")).append(",");
            csv.append(resumo.path("agency").asText("")).append(",");
            csv.append(resumo.path("accountTypeDescription").asText("")).append(",");
            csv.append(resumo.path("accountNumber").asText("")).append(",");
            csv.append(resumo.path("feePercent").asText("")).append(",");
            csv.append(resumo.path("splitOriginalSlip").asText("")).append(",");
            csv.append(resumo.path("splitDiscountAmount").asText("")).append(",");
            csv.append(resumo.path("splitDiscountFranchiseAmount").asText("")).append(",");
            csv.append(resumo.path("splitGrossAmount").asText("")).append(",");
            csv.append(resumo.path("recordNumber").asText("")).append("\n");

            // Linhas 015
            JsonNode parcelas = resumo.path("salesReceiptInstallmentTransaction");
            if (parcelas != null && parcelas.isArray()) {
                for (JsonNode p : parcelas) {
                    csv.append(p.path("recordType").asText("")).append(",");
                    csv.append(p.path("clientCode").asText("")).append(",");
                    csv.append(p.path("salesDate").asText("")).append(",");
                    csv.append(p.path("salesSummaryNumber").asText("")).append(",");
                    csv.append(p.path("salesReceiptNumber").asText("")).append(",");
                    csv.append(p.path("cardSchemeDescription").asText("")).append(",");
                    csv.append(p.path("transactionTypeDescription").asText("")).append(",");
                    csv.append(p.path("discountAmountField").asText("")).append(",");
                    csv.append(p.path("netAmountParcelField").asText("")).append(",");
                    csv.append(p.path("totalNetAmountFireld").asText("")).append(",");
                    csv.append(p.path("creditDate").asText("")).append(",");
                    csv.append(p.path("salesInstallmentReceiptNumber").asText("")).append(",");
                    csv.append(p.path("installmentNumber").asText("")).append(",");
                    csv.append(p.path("totalInstallments").asText("")).append(",");
                    csv.append(p.path("acquirerReference").asText("")).append(",");
                    csv.append(p.path("idUR").asText("")).append(",");
                    csv.append(p.path("recordNumber").asText("")).append("\n");
                }
            }
        }
    }

    private void processarInstallmentAcceleration(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarInstallmentAcceleration chamado");

        JsonNode lista = raiz.path("salesInstallmentAcceleration");
        if (lista == null || !lista.isArray() || lista.size() == 0) {
            csv.append("0\n");
            return;
        }

        for (JsonNode resumo : lista) {
            // Linha 018
            csv.append(resumo.path("recordType").asText("")).append(",");
            csv.append(resumo.path("matrixClientCode").asText("")).append(",");
            csv.append(resumo.path("clientCode").asText("")).append(",");
            csv.append(resumo.path("salesDate").asText("")).append(",");
            csv.append(resumo.path("salesSummaryNumber").asText("")).append(",");
            csv.append(resumo.path("quantityOfSales").asText("")).append(",");
            csv.append(resumo.path("transactionTypeDescription").asText("")).append(",");
            csv.append(resumo.path("transactionStatusDescription").asText("")).append(",");
            csv.append(resumo.path("grossAmountField").asText("")).append(",");
            csv.append(resumo.path("discountAmountField").asText("")).append(",");
            csv.append(resumo.path("netAmountField").asText("")).append(",");
            csv.append(resumo.path("creditDate").asText("")).append(",");
            csv.append(resumo.path("totalInstallments").asText("")).append(",");
            csv.append(resumo.path("bank").asText("")).append(",");
            csv.append(resumo.path("agency").asText("")).append(",");
            csv.append(resumo.path("accountTypeDescription").asText("")).append(",");
            csv.append(resumo.path("accountNumber").asText("")).append(",");
            csv.append(resumo.path("recordNumber").asText("")).append("\n");

            // Linhas 019
            JsonNode detalhes = resumo.path("salesInstallmentAccelerationDetail");
            if (detalhes != null && detalhes.isArray()) {
                for (JsonNode d : detalhes) {
                    csv.append(d.path("recordType").asText("")).append(",");
                    csv.append(d.path("matrixClientCode").asText("")).append(",");
                    csv.append(d.path("clientCode").asText("")).append(",");
                    csv.append(d.path("salesDate").asText("")).append(",");
                    csv.append(d.path("salesSummaryNumber").asText("")).append(",");
                    csv.append(d.path("salesReceiptNumber").asText("")).append(",");
                    csv.append(d.path("cardSchemeDescription").asText("")).append(",");
                    csv.append(d.path("transactionTypeDescription").asText("")).append(",");
                    csv.append(d.path("entryModeDescription*").asText("")).append(",");
                    csv.append(d.path("cardTypeDescription").asText("")).append(",");
                    csv.append(d.path("grossAmountField").asText("")).append(",");
                    csv.append(d.path("discountAmountField").asText("")).append(",");
                    csv.append(d.path("netAmountField").asText("")).append(",");
                    csv.append(d.path("creditDate").asText("")).append(",");
                    csv.append(d.path("clientControlNumber").asText("")).append(",");
                    csv.append(d.path("salesInstallmentReceiptNumber").asText("")).append(",");
                    csv.append(d.path("installmentNumber").asText("")).append(",");
                    csv.append(d.path("totalInstallments").asText("")).append(",");
                    csv.append(d.path("acquirerReference").asText("")).append(",");
                    csv.append(d.path("recordNumber").asText("")).append("\n");
                }
            }
        }
    }

    private void processarCancelamentos(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarCancelamentos chamado");

        JsonNode lista = raiz.path("salesCancelInfo");
        if (lista == null || !lista.isArray() || lista.size() == 0) {
            csv.append("0\n");
            return;
        }

        for (JsonNode item : lista) {
            csv.append(item.path("recordType").asText("")).append(",");
            csv.append(item.path("cancelTransactionDate").asText("")).append(",");
            csv.append(item.path("clientCode").asText("")).append(",");
            csv.append(item.path("cardScheme").asText("")).append(",");
            csv.append(item.path("cancelTransactionProduct").asText("")).append(",");
            csv.append(item.path("clientName").asText("")).append(",");
            csv.append(item.path("mcc").asText("")).append(",");
            csv.append(item.path("motive").asText("")).append(",");
            csv.append(item.path("originalTransactionDate").asText("")).append(",");
            csv.append(item.path("splitOriginal").asText("")).append(",");
            csv.append(item.path("cardNumber").asText("")).append(",");
            csv.append(item.path("authorizationCode").asText("")).append(",");
            csv.append(item.path("clientControlNumber").asText("")).append(",");
            csv.append(item.path("cancelTransactionArn").asText("")).append(",");
            csv.append(item.path("slipNumber").asText("")).append(",");
            csv.append(item.path("originalSettlementDate").asText("")).append(",");
            csv.append(item.path("cancelTransactionStatus").asText("")).append(",");
            csv.append(item.path("terminalId").asText("")).append(",");
            csv.append(item.path("transactionCaptureMode").asText("")).append(",");
            csv.append(item.path("originalTransactionAmount").asText("")).append(",");
            csv.append(item.path("cancelAmount").asText("")).append(",");
            csv.append(item.path("interchangeAmount").asText("")).append(",");
            csv.append(item.path("mdrAmount").asText("")).append(",");
            csv.append(item.path("netAmount").asText("")).append(",");
            csv.append(item.path("cancelSystemUserId").asText("")).append(",");
            csv.append(item.path("cancelSystemUserName").asText("")).append(",");
            csv.append(item.path("cancelAuthorId").asText("")).append(",");
            csv.append(item.path("cancelAuthorName").asText("")).append(",");
            csv.append(item.path("recordNumber").asText("")).append("\n");
        }
    }

    private void processarChargebacks(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarChargebacks chamado");

        JsonNode lista = raiz.path("salesChargeBackTransactions");
        if (lista == null || !lista.isArray() || lista.size() == 0) {
            csv.append("0\n");
            return;
        }

        for (JsonNode item : lista) {
            csv.append(item.path("recordType").asText("")).append(",");
            csv.append(item.path("processingDate").asText("")).append(",");
            csv.append(item.path("clientCode").asText("")).append(",");
            csv.append(item.path("cardScheme").asText("")).append(",");
            csv.append(item.path("transactionType").asText("")).append(",");
            csv.append(item.path("clientName").asText("")).append(",");
            csv.append(item.path("chargeBackKey").asText("")).append(",");
            csv.append(item.path("clientControlNumber").asText("")).append(",");
            csv.append(item.path("slipChargeback").asText("")).append(",");
            csv.append(item.path("socChargeBackCodeDescription").asText("")).append(",");
            csv.append(item.path("socChargeBackCodeDescription").asText("")).append(","); // mesmo nome duas vezes (confirmar se duplicado)
            csv.append(item.path("chargeBackTypeDescription").asText("")).append(",");
            csv.append(item.path("transactionDate").asText("")).append(",");
            csv.append(item.path("originalSlip").asText("")).append(",");
            csv.append(item.path("tidAmex").asText("")).append(",");
            csv.append(item.path("cardNumber").asText("")).append(",");
            csv.append(item.path("authorizationCode").asText("")).append(",");
            csv.append(item.path("acquirerReference").asText("")).append(",");
            csv.append(item.path("chargeBackAmount").asText("")).append(",");
            csv.append(item.path("netAmount").asText("")).append(",");
            csv.append(item.path("installmentNumber").asText("")).append(",");
            csv.append(item.path("totalInstallments").asText("")).append(",");
            csv.append(item.path("productAccount").asText("")).append(",");
            csv.append(item.path("terminalNumber").asText("")).append(",");
            csv.append(item.path("chargeBackMessage").asText("")).append(",");
            csv.append(item.path("recordNumber").asText("")).append("\n");
        }
    }

    private void processarClientFileTrailers(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarClientFileTrailers chamado");

        JsonNode trailers = raiz.path("clientFileTrailers");
        if (trailers == null || !trailers.isArray() || trailers.size() == 0) {
            csv.append("0\n");
            return;
        }

        for (JsonNode t : trailers) {
            csv.append(t.path("recordType").asText("")).append(",");

            JsonNode matrixClient = t.path("matrixClient");
            csv.append(matrixClient.path("code").asText("")).append(",");
            csv.append(matrixClient.path("name").asText("")).append(",");
            csv.append(matrixClient.path("document").asText("")).append(",");

            csv.append(t.path("matrixClientCode").asText("")).append(",");

            JsonNode branchClient = t.path("branchClient");
            csv.append(branchClient.path("code").asText("")).append(",");
            csv.append(branchClient.path("name").asText("")).append(",");
            csv.append(branchClient.path("document").asText("")).append(",");

            csv.append(t.path("branchClientCode").asText("")).append(",");

            JsonNode debit = t.path("debitSales");
            csv.append(debit.path("quantity").asText("")).append(",");
            csv.append(debit.path("grossAmountField").asText("")).append(",");
            csv.append(debit.path("discountAmountField").asText("")).append(",");
            csv.append(debit.path("netAmountField").asText("")).append(",");

            JsonNode credit = t.path("creditSales");
            csv.append(credit.path("quantity").asText("")).append(",");
            csv.append(credit.path("grossAmountField").asText("")).append(",");
            csv.append(credit.path("discountAmountField").asText("")).append(",");
            csv.append(credit.path("netAmountField").asText("")).append(",");

            JsonNode inst = t.path("installmentSales");
            csv.append(inst.path("quantity").asText("")).append(",");
            csv.append(inst.path("grossAmountField").asText("")).append(",");
            csv.append(inst.path("discountAmountField").asText("")).append(",");
            csv.append(inst.path("netAmountField").asText("")).append(",");

            JsonNode instIssuer = t.path("installmentSalesIssuer");
            csv.append(instIssuer.path("quantity").asText("")).append(",");
            csv.append(instIssuer.path("grossAmountField").asText("")).append(",");
            csv.append(instIssuer.path("discountAmountField").asText("")).append(",");
            csv.append(instIssuer.path("netAmountField").asText("")).append(",");

            JsonNode plan = t.path("installmentPlanSales");
            csv.append(plan.path("quantity").asText("")).append(",");
            csv.append(plan.path("grossAmountField").asText("")).append(",");
            csv.append(plan.path("discountAmountField").asText("")).append(",");
            csv.append(plan.path("netAmountField").asText("")).append(",");

            csv.append(t.path("cashBackAmount").asText("")).append(",");
            csv.append(t.path("cashBackAmountField").asText("")).append(",");
            csv.append(t.path("reverseInterchange").asText("")).append(",");
            csv.append(t.path("reverseInterchangeField").asText("")).append(",");
            csv.append(t.path("quantityOfRecharge").asText("")).append(",");
            csv.append(t.path("recordNumber").asText("")).append("\n");
        }
    }

    private void processarFinalFileTrailer(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarFinalFileTrailer chamado");

        JsonNode trailer = raiz.path("finalFileTrailer");
        if (trailer == null || trailer.isEmpty()) {
            csv.append("0\n");
            return;
        }

        csv.append(trailer.path("recordType").asText("")).append(",");

        JsonNode matrixClient = trailer.path("matrixClient");
        csv.append(matrixClient.path("code").asText("")).append(",");

        JsonNode debit = trailer.path("debitSales");
        csv.append(debit.path("quantity").asText("")).append(",");
        csv.append(debit.path("grossAmountField").asText("")).append(",");
        csv.append(debit.path("discountAmountField").asText("")).append(",");
        csv.append(debit.path("netAmountField").asText("")).append(",");

        JsonNode credit = trailer.path("creditSales");
        csv.append(credit.path("quantity").asText("")).append(",");
        csv.append(credit.path("grossAmountField").asText("")).append(",");
        csv.append(credit.path("discountAmountField").asText("")).append(",");
        csv.append(credit.path("netAmountField").asText("")).append(",");

        JsonNode inst = trailer.path("installmentSales");
        csv.append(inst.path("quantity").asText("")).append(",");
        csv.append(inst.path("grossAmountField").asText("")).append(",");
        csv.append(inst.path("discountAmountField").asText("")).append(",");
        csv.append(inst.path("netAmountField").asText("")).append(",");

        JsonNode instIssuer = trailer.path("installmentSalesIssuer");
        csv.append(instIssuer.path("quantity").asText("")).append(",");
        csv.append(instIssuer.path("grossAmountField").asText("")).append(",");
        csv.append(instIssuer.path("discountAmountField").asText("")).append(",");
        csv.append(instIssuer.path("netAmountField").asText("")).append(",");

        JsonNode plan = trailer.path("installmentPlanSales");
        csv.append(plan.path("quantity").asText("")).append(",");
        csv.append(plan.path("grossAmountField").asText("")).append(",");
        csv.append(plan.path("discountAmountField").asText("")).append(",");
        csv.append(plan.path("netAmountField").asText("")).append(",");

        csv.append(trailer.path("cashBackAmount").asText("")).append(",");
        csv.append(trailer.path("cashBackAmountField").asText("")).append(",");
        csv.append(trailer.path("reverseInterchange").asText("")).append(",");
        csv.append(trailer.path("reverseInterchangeField").asText("")).append(",");
        csv.append(trailer.path("quantityOfRecharge").asText("")).append(",");
        csv.append(trailer.path("recordNumber").asText("")).append("\n");
    }


    //Futuramente uma classe com metodos abaixo
    private void salvarCsv(File pastaDestino, String nomeOriginal, String conteudo) {
        try {
            String nomeArquivo = nomeOriginal.replace(".json", ".csv");
            File destino = new File(pastaDestino, nomeArquivo);

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destino), StandardCharsets.UTF_8))) {
                //System.out.println("TENTANDO ESCREVER:\n" + conteudo);
                //System.out.println("DESTINO ABSOLUTO: " + destino.getAbsolutePath());
                //System.out.println("EXISTE? " + destino.exists());
                //System.out.println("PODE ESCREVER? " + destino.canWrite());
                writer.write(conteudo);
                writer.flush(); // força gravação imediata no disco
                Logger.info("Arquivo salvo com sucesso: " + nomeArquivo);
                Logger.info("Arquivo físico existe? " + destino.exists());
                Logger.info("Tamanho final gravado: " + destino.length() + " bytes");            }


        } catch (IOException e) {
            Logger.erro("Erro ao salvar arquivo CSV");
            e.printStackTrace();
        }
    }

    private String formatarNomeMetodo(String chave) {
        String[] partes = chave.split("(?=[A-Z])");
        StringBuilder nomeMetodo = new StringBuilder();

        for (String parte : partes) {
            if (!parte.isEmpty()) {
                nomeMetodo.append(parte.substring(0, 1).toUpperCase());
                if (parte.length() > 1) {
                    nomeMetodo.append(parte.substring(1).toLowerCase());
                }
            }
        }

        return nomeMetodo.toString();
    }
}
