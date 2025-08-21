package br.com.prosistema.TipoArquivos;

import br.com.prosistema.util.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;



public class ConversaoPagamentos {

    private final ObjectMapper mapper = new ObjectMapper();

    public void converter(File arquivoJson, File pastaDestino) {
        try {
            JsonNode raiz = mapper.readTree(arquivoJson);
            StringBuilder csv = new StringBuilder();

            // Chamada dos métodos de conversão que possuem dados ja registrados
            processarFileHeader(raiz, csv);
            processarClientHeaders(raiz, csv);
            processarDebitFinance(raiz, csv);
            processarCreditFinance(raiz, csv);
            // Chamada dos metodos que ainda nao possuem dados
            processarInstallmentFinanceSummary(raiz, csv);
            processarFinanceSuspendedTransactions(raiz, csv);
            processarIntraCountSummary(raiz, csv);
            processarFinanceAdjustments(raiz, csv);
            processarChargebackReceipt(raiz, csv);
            processarFinanceSummaryAdvancement(raiz, csv);
            processarFinanceAdvancementFileTrailer(raiz, csv);
            // Chamada dos métodos de conversão que possuem dados ja registrados
            processarClientFileTrailers(raiz, csv);
            processarFinanceFileTrailer(raiz, csv);
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
        if (!headers.isArray()) {
            Logger.erro("clientHeaders não é uma lista.");
            return;
        }

        for (JsonNode item : headers) {
            csv.append(item.path("recordType").asText("")).append(",");
            csv.append(item.path("processingDate").asText("")).append(",");
            JsonNode matrix = item.path("matrixClient");
            csv.append(matrix.path("code").asText("")).append(",");
            csv.append(matrix.path("name").asText("")).append(",");
            csv.append(matrix.path("document").asText("")).append(",");

            JsonNode branch = item.path("branchClient");
            csv.append(branch.path("document").asText("")).append(",");
            csv.append(branch.path("code").asText("")).append(",");
            csv.append(branch.path("name").asText("")).append(",");

            JsonNode central = item.path("centralizingClient");
            if (central.isNull() || central.isMissingNode()) {
                csv.append("0,0,0,"); // preenche com 3 campos em branco padronizados
            } else {
                csv.append(central.path("code").asText("")).append(",");
                csv.append(central.path("name").asText("")).append(",");
                csv.append(central.path("document").asText("")).append(",");
            }

            csv.append(item.path("recordNumber").asText("")).append("\n");
        }
    }

    private void processarDebitFinance(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarDebitFinance chamado");

        // Se a lista estiver vazia, escrever linha 0 e sair
        if (verificarListaVazia(raiz, "debitFinanceSummary", csv)) {
            return;
        }
        JsonNode resumos = raiz.path("debitFinanceSummary");

        for (JsonNode resumo : resumos) {
            // LINHA 1: resumo de operação (020)
            csv.append(resumo.path("recordType").asText("")).append(",");
            csv.append(resumo.path("cardScheme").path("code").asText("")).append(",");
            csv.append(resumo.path("cardScheme").path("description").asText("")).append(",");
            csv.append(resumo.path("cardSchemeDescription").asText("")).append(",");
            csv.append(resumo.path("paymentDate").asText("")).append(",");
            csv.append(resumo.path("clientCode").asText("")).append(",");
            csv.append(resumo.path("centralizingClientCode").asText("")).append(",");
            csv.append(resumo.path("paymentInstructionNumber").asText("")).append(",");
            csv.append(resumo.path("paymentAmount").asText("")).append(",");
            csv.append(resumo.path("paymentAmountField").asText("")).append(",");
            csv.append(resumo.path("bank").asText("")).append(",");
            csv.append(resumo.path("agency").asText("")).append(",");
            csv.append(resumo.path("accountType").path("code").asText("")).append(",");
            csv.append(resumo.path("accountType").path("description").asText("")).append(",");
            csv.append(resumo.path("accountTypeDescription").asText("")).append(",");
            csv.append(resumo.path("accountNumber").asText("")).append(",");
            csv.append(resumo.path("transactionGenerationDate").asText("")).append(",");
            csv.append(resumo.path("quantityOfSales").asText("")).append(",");
            csv.append(resumo.path("cashBackAmount").asText("")).append(",");
            csv.append(resumo.path("cashBackAmountField").asText("")).append(",");
            csv.append(resumo.path("reverseInterchange").asText("")).append(",");
            csv.append(resumo.path("reverseInterchangeField").asText("")).append(",");
            csv.append(resumo.path("recordNumber").asText("")).append("\n");

            // LINHAS 2+: detalhes das vendas (021)
            JsonNode recibos = resumo.path("debitFinanceReceipt");
            if (recibos == null || !recibos.isArray() || recibos.size() == 0) {
                csv.append("0\n");
                continue;
            }

            // 🔍 VALIDAÇÃO: compara quantidade esperada com tamanho da lista
            int esperada = resumo.path("quantityOfSales").asInt(0);
            int encontrada = recibos.size();
            if (esperada != encontrada) {
                Logger.alerta("Quantidade de vendas divergente em debitFinanceSummary. Esperado: " + esperada + ", Encontrado: " + encontrada);
            }

            for (JsonNode r : recibos) {
                csv.append(r.path("recordType").asText("")).append(",");
                csv.append(r.path("cardScheme").path("code").asText("")).append(",");
                csv.append(r.path("cardScheme").path("description").asText("")).append(",");
                csv.append(r.path("cardSchemeDescription").asText("")).append(",");
                csv.append(r.path("paymentDate").asText("")).append(",");
                csv.append(r.path("clientCode").asText("")).append(",");
                csv.append(r.path("centralizingClientCode").asText("")).append(",");
                csv.append(r.path("paymentInstructionNumber").asText("")).append(",");
                csv.append(r.path("salesSummaryNumber").asText("")).append(",");
                csv.append(r.path("quantityOfSales").asText("")).append(",");

                csv.append(r.path("paymentType").path("code").asText("")).append(",");
                csv.append(r.path("paymentType").path("description").asText("")).append(",");
                csv.append(r.path("paymentTypeDescription").asText("")).append(",");

                csv.append(r.path("transactionType").path("code").asText("")).append(",");
                csv.append(r.path("transactionType").path("description").asText("")).append(",");
                csv.append(r.path("transactionTypeDescription").asText("")).append(",");

                csv.append(r.path("transactionDescriptionCode").asText("")).append(",");
                csv.append(r.path("transactionDescription").asText("")).append(",");

                csv.append(valueOrZero(r.path("beneficiaryInstituteLockin"))).append(",");
                csv.append(valueOrZero(r.path("lockinType"))).append(",");

                csv.append(r.path("accountType").path("code").asText("")).append(",");
                csv.append(r.path("accountType").path("description").asText("")).append(",");
                csv.append(r.path("accountTypeDescription").asText("")).append(",");

                csv.append(r.path("paymentStatus").asText("")).append(",");

                csv.append(r.path("paymentTransactionType").path("code").asText("")).append(",");
                csv.append(r.path("paymentTransactionType").path("description").asText("")).append(",");
                csv.append(r.path("paymentTransactionTypeDescription").asText("")).append(",");

                csv.append(valueOrZero(r.path("chargeBack"))).append(",");
                csv.append(r.path("salesReceiptNumber").asText("")).append(",");
                csv.append(r.path("transactionDate").asText("")).append(",");
                csv.append(r.path("transactionTime").asText("")).append(",");
                csv.append(r.path("accountNumber").asText("")).append(",");
                csv.append(r.path("recordDate").asText("")).append(",");
                csv.append(r.path("valueDate").asText("")).append(",");
                csv.append(valueOrZero(r.path("clientControlNumber"))).append(",");
                csv.append(r.path("maskedCardNumber").asText("")).append(",");
                csv.append(valueOrZero(r.path("paymentAccountReference"))).append(",");
                csv.append(r.path("authorizationCode").asText("")).append(",");
                csv.append(r.path("retrievalReferenceNumber").asText("")).append(",");

                csv.append(r.path("receiptType").path("code").asText("")).append(",");
                csv.append(r.path("receiptType").path("description").asText("")).append(",");
                csv.append(r.path("receiptTypeDescription").asText("")).append(",");

                csv.append(r.path("grossAmount").asText("")).append(",");
                csv.append(r.path("grossAmountField").asText("")).append(",");
                csv.append(r.path("discountAmount").asText("")).append(",");
                csv.append(r.path("discountAmountField").asText("")).append(",");
                csv.append(r.path("netAmount").asText("")).append(",");
                csv.append(r.path("netAmountField").asText("")).append(",");

                csv.append(r.path("feeModeAmount").asText("")).append(",");
                csv.append(r.path("feeModeAmountField").asText("")).append(",");
                csv.append(r.path("feeModeNetAmount").asText("")).append(",");
                csv.append(r.path("feeModeNetAmountField").asText("")).append(",");

                csv.append(r.path("cashBackAmount").asText("")).append(",");
                csv.append(r.path("cashBackAmountField").asText("")).append(",");
                csv.append(r.path("reverseInterchange").asText("")).append(",");
                csv.append(r.path("reverseInterchangeField").asText("")).append(",");

                csv.append(r.path("acquirerReference").asText("")).append(",");
                csv.append(valueOrZero(r.path("cancelId"))).append(",");
                csv.append(r.path("recordNumber").asText("")).append("\n");
            }
        }
    }

    private void processarCreditFinance(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarCreditFinance chamado");

        if (verificarListaVazia(raiz, "creditFinanceSummary", csv)) {
            return;
        }

        JsonNode resumos = raiz.path("creditFinanceSummary");

        for (JsonNode resumo : resumos) {
            // LINHA 1: resumo de operação (022)
            csv.append(resumo.path("recordType").asText("")).append(",");
            csv.append(resumo.path("cardScheme").path("code").asText("")).append(",");
            csv.append(resumo.path("cardScheme").path("description").asText("")).append(",");
            csv.append(resumo.path("cardSchemeDescription").asText("")).append(",");
            csv.append(resumo.path("paymentDate").asText("")).append(",");
            csv.append(resumo.path("clientCode").asText("")).append(",");
            csv.append(resumo.path("centralizingClientCode").asText("")).append(",");
            csv.append(resumo.path("paymentInstructionNumber").asText("")).append(",");
            csv.append(resumo.path("paymentAmount").asText("")).append(",");
            csv.append(resumo.path("paymentAmountField").asText("")).append(",");
            csv.append(resumo.path("bank").asText("")).append(",");
            csv.append(resumo.path("agency").asText("")).append(",");
            csv.append(resumo.path("accountType").path("code").asText("")).append(",");
            csv.append(resumo.path("accountType").path("description").asText("")).append(",");
            csv.append(resumo.path("accountTypeDescription").asText("")).append(",");
            csv.append(resumo.path("accountNumber").asText("")).append(",");
            csv.append(resumo.path("transactionGenerationDate").asText("")).append(",");
            csv.append(resumo.path("quantityOfSales").asText("")).append(",");
            csv.append(resumo.path("recordNumber").asText("")).append("\n");

            // LINHAS 2+: vendas detalhadas (023)
            JsonNode recibos = resumo.path("creditFinanceReceipt");

            if (recibos == null || !recibos.isArray() || recibos.size() == 0) {
                Logger.info("creditFinanceReceipt está vazio para um resumo. Escrevendo linha 0.");
                csv.append("0\n");
                continue;
            }

            // Validação de quantidade esperada x encontrada
            int esperada = resumo.path("quantityOfSales").asInt(0);
            int encontrada = recibos.size();
            if (esperada != encontrada) {
                Logger.alerta("Quantidade de vendas divergente em creditFinanceSummary. Esperado: " + esperada + ", Encontrado: " + encontrada);
            }

            for (JsonNode r : recibos) {
                csv.append(r.path("recordType").asText("")).append(",");
                csv.append(r.path("cardScheme").path("code").asText("")).append(",");
                csv.append(r.path("cardScheme").path("description").asText("")).append(",");
                csv.append(r.path("cardSchemeDescription").asText("")).append(",");
                csv.append(r.path("paymentDate").asText("")).append(",");
                csv.append(r.path("clientCode").asText("")).append(",");
                csv.append(r.path("centralizingClientCode").asText("")).append(",");
                csv.append(r.path("paymentInstructionNumber").asText("")).append(",");
                csv.append(r.path("salesSummaryNumber").asText("")).append(",");
                csv.append(r.path("quantityOfSales").asText("")).append(",");

                csv.append(r.path("paymentType").path("code").asText("")).append(",");
                csv.append(r.path("paymentType").path("description").asText("")).append(",");
                csv.append(r.path("paymentTypeDescription").asText("")).append(",");

                csv.append(r.path("transactionType").path("code").asText("")).append(",");
                csv.append(r.path("transactionType").path("description").asText("")).append(",");
                csv.append(r.path("transactionTypeDescription").asText("")).append(",");

                csv.append(r.path("transactionDescriptionCode").asText("")).append(",");
                csv.append(r.path("transactionDescription").asText("")).append(",");
                csv.append(r.path("financeSummaryNumber").asText("")).append(",");

                csv.append(valueOrZero(r.path("beneficiaryInstituteLockin"))).append(",");
                csv.append(valueOrZero(r.path("lockinType"))).append(",");

                csv.append(r.path("accountType").path("code").asText("")).append(",");
                csv.append(r.path("accountType").path("description").asText("")).append(",");
                csv.append(r.path("accountTypeDescription").asText("")).append(",");

                csv.append(r.path("paymentStatus").asText("")).append(",");

                csv.append(r.path("paymentTransactionType").path("code").asText("")).append(",");
                csv.append(r.path("paymentTransactionType").path("description").asText("")).append(",");
                csv.append(r.path("paymentTransactionTypeDescription").asText("")).append(",");

                csv.append(valueOrZero(r.path("chargeBack"))).append(",");
                csv.append(r.path("salesReceiptNumber").asText("")).append(",");
                csv.append(r.path("salesTransactionType").asText("")).append(",");
                csv.append(r.path("transactionDate").asText("")).append(",");
                csv.append(r.path("transactionTime").asText("")).append(",");
                csv.append(r.path("accountNumber").asText("")).append(",");
                csv.append(r.path("recordDate").asText("")).append(",");
                csv.append(r.path("valueDate").asText("")).append(",");
                csv.append(valueOrZero(r.path("clientControlNumber"))).append(",");
                csv.append(r.path("maskedCardNumber").asText("")).append(",");
                csv.append(valueOrZero(r.path("paymentAccountReference"))).append(",");
                csv.append(r.path("authorizationCode").asText("")).append(",");
                csv.append(r.path("retrievalReferenceNumber").asText("")).append(",");

                csv.append(r.path("receiptType").path("code").asText("")).append(",");
                csv.append(r.path("receiptType").path("description").asText("")).append(",");
                csv.append(r.path("receiptTypeDescription").asText("")).append(",");

                csv.append(r.path("grossAmount").asText("")).append(",");
                csv.append(r.path("grossAmountField").asText("")).append(",");
                csv.append(r.path("discountAmount").asText("")).append(",");
                csv.append(r.path("discountAmountField").asText("")).append(",");
                csv.append(r.path("netAmount").asText("")).append(",");
                csv.append(r.path("netAmountField").asText("")).append(",");

                csv.append(r.path("feeModeAmount").asText("")).append(",");
                csv.append(r.path("feeModeAmountField").asText("")).append(",");
                csv.append(r.path("feeModeNetAmount").asText("")).append(",");
                csv.append(r.path("feeModeNetAmountField").asText("")).append(",");

                csv.append(r.path("acquirerReference").asText("")).append(",");
                csv.append(valueOrZero(r.path("cancelId"))).append(",");
                csv.append(r.path("recordNumber").asText("")).append("\n");
            }
        }
    }

    private void processarClientFileTrailers(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarClientFileTrailers chamado");
        JsonNode lista = raiz.path("financeClientFileTrailers");

        if (!lista.isArray() || lista.isEmpty()) {
            csv.append("0000\n");
            return;
        }

        for (JsonNode resumo : lista) {
            csv.append(resumo.path("recordType").asText("")).append(",");
            csv.append(resumo.path("matrixClient").path("code").asText("")).append(",");
            csv.append(resumo.path("matrixClient").path("name").asText("")).append(",");
            csv.append(resumo.path("matrixClient").path("document").asText("")).append(",");
            csv.append(resumo.path("branchClient").path("code").asText("")).append(",");
            csv.append(resumo.path("branchClient").path("name").asText("")).append(",");
            csv.append(resumo.path("branchClient").path("document").asText("")).append(",");

            JsonNode central = resumo.path("centralizingClient");
            if (central.isMissingNode() || central.isNull()) {
                csv.append("null").append(",");
            } else {
                csv.append(central.path("code").asText("")).append(",");
            }
            csv.append(resumo.path("centralizingClientCode").asText("")).append(",");
            csv.append(resumo.path("debitSalesSummaryQuantity").asText("")).append(",");
            csv.append(resumo.path("debitSalesAmountMasterCard").asText("")).append(",");
            csv.append(resumo.path("debitSalesAmountMasterCardField").asText("")).append(",");
            csv.append(resumo.path("debitSalesAmountVisa").asText("")).append(",");
            csv.append(resumo.path("debitSalesAmountVisaField").asText("")).append(",");
            csv.append(resumo.path("debitSalesAmountCabal").asText("")).append(",");
            csv.append(resumo.path("debitSalesAmountCabalField").asText("")).append(",");
            csv.append(resumo.path("debitSalesAmountElo").asText("")).append(",");
            csv.append(resumo.path("debitSalesAmountEloField").asText("")).append(",");
            csv.append(resumo.path("debitSalesAmountHiperCard").asText("")).append(",");
            csv.append(resumo.path("debitSalesAmountHiperCardField").asText("")).append(",");
            csv.append(resumo.path("debitSalesNetAmount").asText("")).append(",");
            csv.append(resumo.path("debitSalesNetAmountField").asText("")).append(",");
            csv.append(resumo.path("creditSalesQuantity").asText("")).append(",");
            csv.append(resumo.path("installmentCreditSalesQuantity").asText("")).append(",");
            csv.append(resumo.path("creditSalesAmountMasterCard").asText("")).append(",");
            csv.append(resumo.path("creditSalesAmountMasterCardField").asText("")).append(",");
            csv.append(resumo.path("creditSalesAmountVisa").asText("")).append(",");
            csv.append(resumo.path("creditSalesAmountVisaField").asText("")).append(",");
            csv.append(resumo.path("creditSalesAmountCabal").asText("")).append(",");
            csv.append(resumo.path("creditSalesAmountCabalField").asText("")).append(",");
            csv.append(resumo.path("creditSalesAmountElo").asText("")).append(",");
            csv.append(resumo.path("creditSalesAmountEloField").asText("")).append(",");
            csv.append(resumo.path("creditSalesAmountHiperCard").asText("")).append(",");
            csv.append(resumo.path("creditSalesAmountHiperCardField").asText("")).append(",");
            csv.append(resumo.path("creditSalesAmountAmex").asText("")).append(",");
            csv.append(resumo.path("creditSalesAmountAmexField").asText("")).append(",");
            csv.append(resumo.path("creditSalesNetAmount").asText("")).append(",");
            csv.append(resumo.path("creditSalesNetAmountField").asText("")).append(",");
            csv.append(resumo.path("antecipatedSalesSummaryQuantity").asText("")).append(",");
            csv.append(resumo.path("antecipatedSalesAmount").asText("")).append(",");
            csv.append(resumo.path("antecipatedSalesAmountField").asText("")).append(",");
            csv.append(resumo.path("adjustmentQuantityRealized").asText("")).append(",");
            csv.append(resumo.path("debitOrCredit").asText("")).append(",");
            csv.append(resumo.path("adjustmentAmountRealized").asText("")).append(",");
            csv.append(resumo.path("adjustmentAmountRealizedField").asText("")).append(",");
            csv.append(resumo.path("adjustmentQuantity").asText("")).append(",");
            csv.append(resumo.path("adjustmentAmount").asText("")).append(",");
            csv.append(resumo.path("adjustmentAmountField").asText("")).append(",");
            csv.append(resumo.path("totalNetAmount").asText("")).append(",");
            csv.append(resumo.path("totalNetAmountField").asText("")).append(",");
            csv.append(resumo.path("cashBackAmount").asText("")).append(",");
            csv.append(resumo.path("cashBackAmountField").asText("")).append(",");
            csv.append(resumo.path("reverseInterchange").asText("")).append(",");
            csv.append(resumo.path("reverseInterchangeField").asText("")).append(",");
            csv.append(resumo.path("recordNumber").asText("")).append("\n");
        }
    }

    private void processarFinanceFileTrailer(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarFinanceFileTrailer chamado");
        JsonNode resumo = raiz.path("financeFileTrailer");
        if (resumo.isMissingNode() || resumo.isNull() || resumo.isEmpty()) {
            csv.append("0000\n");
            return;
        }
        csv.append(resumo.path("recordType").asText("")).append(",");
        csv.append(resumo.path("matrixClient").path("code").asText("")).append(",");
        csv.append(resumo.path("matrixClient").path("name").asText("")).append(",");
        csv.append(resumo.path("matrixClient").path("document").asText("")).append(",");
        csv.append(resumo.path("matrixClientCode").asText("")).append(",");

        csv.append(resumo.path("debitSalesSummaryQuantity").asText("")).append(",");
        csv.append(resumo.path("debitSalesAmountMasterCard").asText("")).append(",");
        csv.append(resumo.path("debitSalesAmountMasterCardField").asText("")).append(",");
        csv.append(resumo.path("debitSalesAmountVisa").asText("")).append(",");
        csv.append(resumo.path("debitSalesAmountVisaField").asText("")).append(",");
        csv.append(resumo.path("debitSalesAmountCabal").asText("")).append(",");
        csv.append(resumo.path("debitSalesAmountCabalField").asText("")).append(",");
        csv.append(resumo.path("debitSalesAmountElo").asText("")).append(",");
        csv.append(resumo.path("debitSalesAmountEloField").asText("")).append(",");
        csv.append(resumo.path("debitSalesAmountHiperCard").asText("")).append(",");
        csv.append(resumo.path("debitSalesAmountHiperCardField").asText("")).append(",");
        csv.append(resumo.path("debitSalesNetAmount").asText("")).append(",");
        csv.append(resumo.path("debitSalesNetAmountField").asText("")).append(",");

        csv.append(resumo.path("creditSalesQuantity").asText("")).append(",");
        csv.append(resumo.path("installmentCreditSalesQuantity").asText("")).append(",");
        csv.append(resumo.path("creditSalesAmountMasterCard").asText("")).append(",");
        csv.append(resumo.path("creditSalesAmountMasterCardField").asText("")).append(",");
        csv.append(resumo.path("creditSalesAmountVisa").asText("")).append(",");
        csv.append(resumo.path("creditSalesAmountVisaField").asText("")).append(",");
        csv.append(resumo.path("creditSalesAmountCabal").asText("")).append(",");
        csv.append(resumo.path("creditSalesAmountCabalField").asText("")).append(",");
        csv.append(resumo.path("creditSalesAmountElo").asText("")).append(",");
        csv.append(resumo.path("creditSalesAmountEloField").asText("")).append(",");
        csv.append(resumo.path("creditSalesAmountHiperCard").asText("")).append(",");
        csv.append(resumo.path("creditSalesAmountHiperCardField").asText("")).append(",");
        csv.append(resumo.path("creditSalesAmountAmex").asText("")).append(",");
        csv.append(resumo.path("creditSalesAmountAmexField").asText("")).append(",");
        csv.append(resumo.path("creditSalesNetAmount").asText("")).append(",");
        csv.append(resumo.path("creditSalesNetAmountField").asText("")).append(",");

        csv.append(resumo.path("antecipatedSalesSummaryQuantity").asText("")).append(",");
        csv.append(resumo.path("antecipatedSalesAmount").asText("")).append(",");
        csv.append(resumo.path("antecipatedSalesAmountField").asText("")).append(",");
        csv.append(resumo.path("adjustmentQuantityRealized").asText("")).append(",");
        csv.append(resumo.path("debitOrCredit").asText("")).append(",");
        csv.append(resumo.path("adjustmentAmountRealized").asText("")).append(",");
        csv.append(resumo.path("adjustmentAmountRealizedField").asText("")).append(",");
        csv.append(resumo.path("adjustmentQuantity").asText("")).append(",");
        csv.append(resumo.path("adjustmentAmount").asText("")).append(",");
        csv.append(resumo.path("adjustmentAmountField").asText("")).append(",");
        csv.append(resumo.path("totalNetAmount").asText("")).append(",");
        csv.append(resumo.path("totalNetAmountField").asText("")).append(",");
        csv.append(resumo.path("cashBackAmount").asText("")).append(",");
        csv.append(resumo.path("cashBackAmountField").asText("")).append(",");
        csv.append(resumo.path("reverseInterchange").asText("")).append(",");
        csv.append(resumo.path("reverseInterchangeField").asText("")).append(",");
        csv.append(resumo.path("recordNumber").asText("")).append("\n");
    }

    // ===== Métodos FALTA DADOS PARA  IMPLEMENTAR =====
    private void processarInstallmentFinanceSummary(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarInstallmentFinanceSummary chamado");
        verificarListaOuObjeto(raiz, "installmentFinanceSummary", csv);
    }

    private void processarFinanceSuspendedTransactions(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarFinanceSuspendedTransactions chamado");
        verificarListaOuObjeto(raiz, "financeSuspendedTransactions", csv);
    }

    private void processarIntraCountSummary(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarIntraCountSummary chamado");
        verificarListaOuObjeto(raiz, "intraCountSummary", csv);
    }

    private void processarFinanceAdjustments(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarFinanceAdjustments chamado");
        verificarListaOuObjeto(raiz, "financeAdjustments", csv);
    }

    private void processarChargebackReceipt(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarChargebackReceipt chamado");
        verificarListaOuObjeto(raiz, "chargebackReceipt", csv);
    }

    private void processarFinanceSummaryAdvancement(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarFinanceSummaryAdvancement chamado");
        verificarListaOuObjeto(raiz, "financeSummaryAdvancement", csv);
    }

    private void processarFinanceAdvancementFileTrailer(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarFinanceAdvancementFileTrailer chamado");
        verificarListaOuObjeto(raiz, "financeAdvancementFileTrailer", csv);
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

    private String valueOrZero(JsonNode node) {
        return node.isNull() || node.isMissingNode() ? "0" : node.asText("");
    }

    private boolean verificarListaVazia(JsonNode raiz, String chave, StringBuilder csv) {
        JsonNode elemento = raiz.path(chave);
        if (elemento.isMissingNode() || elemento.isNull() || !elemento.isArray() || elemento.size() == 0) {
            csv.append("0\n"); // escreve linha "0"
            return true;       // indica que está vazio
        }
        Logger.info("Elemento encontrado para chave: " + chave);
        return false;          // indica que tem dados
    }

    private void verificarListaOuObjeto(JsonNode raiz, String chave, StringBuilder csv) {
        JsonNode elemento = raiz.path(chave);

        if (elemento.isMissingNode() || elemento.isNull()) {
            csv.append("0000\n");
            return;
        }

        if (elemento.isArray()) {
            if (elemento.size() == 0) {
                csv.append("0000\n");
            } else {
                Logger.info("⚠ ATENÇÃO: O campo \"" + chave + "\" possui " + elemento.size() + " item(s) no JSON.");
                Logger.info("⚠ Você deve implementar o tratamento no método: processar" + formatarNomeMetodo(chave) + "(...)");
                csv.append("DADOS_ENCONTRADOS_" + chave.toUpperCase()).append("\n");
            }
        } else {
            Logger.info("⚠ ATENÇÃO: O campo \"" + chave + "\" está presente como objeto no JSON.");
            Logger.info("⚠ Você deve implementar o tratamento no método: processar" + formatarNomeMetodo(chave) + "(...)");
            csv.append("DADOS_ENCONTRADOS_" + chave.toUpperCase()).append("\n");
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
