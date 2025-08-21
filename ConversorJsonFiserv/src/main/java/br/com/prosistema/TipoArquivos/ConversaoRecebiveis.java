package br.com.prosistema.TipoArquivos;

import br.com.prosistema.util.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConversaoRecebiveis {

    private final ObjectMapper mapper = new ObjectMapper();

    public void converter(File arquivoJson, File pastaDestino) {
        try {
            JsonNode raiz = mapper.readTree(arquivoJson);
            StringBuilder csv = new StringBuilder();

            processarFileHeader(raiz, csv);
            processarReceivableUnits(raiz, csv);
            processarReceivableUnitTrailers(raiz, csv);
            processarFileTrailerRU(raiz, csv);

            salvarCsv(pastaDestino, arquivoJson.getName(), csv.toString());

        } catch (Exception e) {
            Logger.erro("Erro ao converter arquivo: " + arquivoJson.getName());
            e.printStackTrace();
        }
    }

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
        csv.append(client.path("name").asText("")).append(",");
        csv.append(client.path("document").asText("")).append(",");

        csv.append(header.path("processingType").asText("")).append(",");
        csv.append(header.path("fileLayoutVersion").asText("")).append(",");
        csv.append(header.path("recordNumber").asText("")).append("\n");
    }

    private void processarReceivableUnits(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarReceivableUnits chamado");

        JsonNode unidades = raiz.path("receivableUnits");
        if (unidades == null || !unidades.isArray() || unidades.size() == 0) {
            csv.append("0\n");
            return;
        }

        for (JsonNode unidade : unidades) {
            // Linha da unidade (001)
            csv.append(unidade.path("recordType").asText("")).append(",");
            csv.append(unidade.path("updateDate").asText("")).append(",");
            csv.append(unidade.path("idUr").asText("")).append(",");
            csv.append(unidade.path("receivableUnitKey").asText("")).append(",");
            csv.append(unidade.path("receivableUnitStatus").asText("")).append(",");
            csv.append(unidade.path("accreditationDocument").asText("")).append(",");
            csv.append(unidade.path("clientDocument").asText("")).append(",");

            JsonNode cardScheme = unidade.path("cardScheme");
            csv.append(cardScheme.path("code").asText("")).append(",");
            csv.append(cardScheme.path("description").asText("")).append(",");

            csv.append(unidade.path("cardSchemeDescription").asText("")).append(",");
            csv.append(unidade.path("paymentDate").asText("")).append(",");

            csv.append(unidade.path("grossAmount").asText("")).append(",");
            csv.append(unidade.path("grossAmountField").asText("")).append(",");
            csv.append(unidade.path("mdrAmount").asText("")).append(",");
            csv.append(unidade.path("mdrAmountField").asText("")).append(",");

            csv.append(unidade.path("updatedTotalAmount").asText("")).append(",");
            csv.append(unidade.path("updatedTotalAmountField").asText("")).append(",");
            csv.append(unidade.path("discountAmount").asText("")).append(",");
            csv.append(unidade.path("discountAmountField").asText("")).append(",");
            csv.append(unidade.path("advanceFeeAmount").asText("")).append(",");
            csv.append(unidade.path("advanceFeeAmountField").asText("")).append(",");

            csv.append(unidade.path("paidAmount").asText("")).append(",");
            csv.append(unidade.path("paidAmountField").asText("")).append(",");

            csv.append(unidade.path("freeNegotiationAmount").asText("")).append(",");
            csv.append(unidade.path("freeNegotiationAmountField").asText("")).append(",");
            csv.append(unidade.path("allocatedAmount").asText("")).append(",");
            csv.append(unidade.path("allocatedAmountField").asText("")).append(",");
            csv.append(unidade.path("recordNumber").asText("")).append("\n");

            // Processa blocos internos de pagamento (004)
            JsonNode pagamentos = unidade.path("paymentRU");
            if (pagamentos != null && pagamentos.isArray()) {
                for (JsonNode pagamento : pagamentos) {
                    processarPaymentRU(pagamento, csv);
                }
            }

            // Gera blocos 002 (contratos)
            processarContractsNegotiatedRU(unidade, csv);

            // Gera blocos 003 (eventos de dedução)
            processarDetailEventsDeductionsRU(unidade, csv);
        }
    }

    private void processarPaymentRU(JsonNode pagamento, StringBuilder csv) {
        csv.append(pagamento.path("recordType").asText("")).append(",");
        csv.append(pagamento.path("idUr").asText("")).append(",");
        csv.append(pagamento.path("receivableUnitKey").asText("")).append(",");
        csv.append(pagamento.path("contractPriorityId").asText("")).append(",");
        csv.append(pagamento.path("domicileDocument").asText("")).append(",");

        JsonNode account = pagamento.path("accountType");
        csv.append(account.path("code").asText("")).append(",");
        csv.append(account.path("description").asText("")).append(",");

        csv.append(pagamento.path("accountTypeDescription").asText("")).append(",");
        csv.append(pagamento.path("ispb").asText("")).append(",");
        csv.append(pagamento.path("compeCode").asText("")).append(",");
        csv.append(pagamento.path("agency").asText("")).append(",");
        csv.append(pagamento.path("paymentAccount").asText("")).append(",");
        csv.append(pagamento.path("payAmount").asText("")).append(",");
        csv.append(pagamento.path("payAmountField").asText("")).append(",");
        csv.append(pagamento.path("cpfCnpjContractBeneficiary").asText("")).append(",");
        csv.append(pagamento.path("effectiveSettlementDate").asText("")).append(",");
        csv.append(pagamento.path("effectiveSettlementAmount").asText("")).append(",");
        csv.append(pagamento.path("effectiveSettlementAmountField").asText("")).append(",");
        csv.append(valueOrZero(pagamento.path("contractIdentifier"))).append(",");
        csv.append(pagamento.path("recordNumber").asText("")).append("\n");
    }

    private void processarContractsNegotiatedRU(JsonNode unidade, StringBuilder csv) {
        Logger.info("-> processarContractsNegotiatedRU chamado");

        JsonNode contratos = unidade.path("contractsNegotiatedRU");

        if (contratos == null || contratos.isMissingNode() || !contratos.isArray() || contratos.size() == 0) {
            csv.append("0\n");
            return;
        }

        for (JsonNode contrato : contratos) {
            csv.append(contrato.path("recordType").asText("")).append(",");
            csv.append(contrato.path("idUr").asText("")).append(",");
            csv.append(contrato.path("receivableUnitKey").asText("")).append(",");
            csv.append(contrato.path("contractIdentifier").asText("")).append(",");
            csv.append(contrato.path("contractEffectIndicator").asText("")).append(",");
            csv.append(contrato.path("contractEffectTypeDescription").asText("")).append(",");
            csv.append(contrato.path("divisionRuleDescription").asText("")).append(",");
            csv.append(contrato.path("commitedAmountField").asText("")).append(",");
            csv.append(contrato.path("effectContractAmountField").asText("")).append(",");
            csv.append(contrato.path("cpfCnpjContractBeneficiary").asText("")).append(",");
            csv.append(contrato.path("recordNumber").asText("")).append("\n");
        }
    }

    private void processarDetailEventsDeductionsRU(JsonNode unidade, StringBuilder csv) {
        Logger.info("-> processarDetailEventsDeductionsRU chamado");

        JsonNode eventos = unidade.path("detailEventsDeductionsRU");

        if (eventos == null || eventos.isMissingNode() || !eventos.isArray() || eventos.size() == 0) {
            csv.append("0\n");
            return;
        }

        for (JsonNode evento : eventos) {
            csv.append(evento.path("recordType").asText("")).append(",");
            csv.append(evento.path("idUr").asText("")).append(",");
            csv.append(evento.path("receivableUnitKey").asText("")).append(",");
            csv.append(evento.path("transactionTypeDescription").asText("")).append(",");
            csv.append(evento.path("eventDate").asText("")).append(",");
            csv.append(evento.path("classCategoryCode").asText("")).append(",");
            csv.append(evento.path("classCategoryDescription").asText("")).append(",");
            csv.append(evento.path("socOperationType").asText("")).append(",");
            csv.append(evento.path("originalTransactionDate").asText("")).append(",");
            csv.append(evento.path("maskedCardNumber").asText("")).append(",");
            csv.append(evento.path("authorizationCode").asText("")).append(",");
            csv.append(evento.path("retrievalReferenceNumber").asText("")).append(",");
            csv.append(evento.path("clientControlNumber").asText("")).append(",");
            csv.append(evento.path("salesReferenceNumber").asText("")).append(",");
            csv.append(evento.path("grossAmount").asText("")).append(",");
            csv.append(evento.path("discountAmount").asText("")).append(",");
            csv.append(evento.path("netAmount").asText("")).append(",");
            csv.append(evento.path("netAmountParcel").asText("")).append(",");
            csv.append(evento.path("netAmountAdjustedDeduction").asText("")).append(",");
            csv.append(evento.path("netAmountRU").asText("")).append(",");
            csv.append(evento.path("installmentNumber").asText("")).append(",");
            csv.append(evento.path("totalInstallments").asText("")).append(",");
            csv.append(evento.path("acquirerReference").asText("")).append(",");
            csv.append(evento.path("recordNumber").asText("")).append("\n");
        }
    }

    private void processarReceivableUnitTrailers(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarReceivableUnitTrailers chamado");

        JsonNode trailers = raiz.path("receivableUnitTrailers");
        if (trailers == null || !trailers.isArray() || trailers.size() == 0) {
            csv.append("0\n");
            return;
        }

        for (JsonNode t : trailers) {
            csv.append(t.path("recordType").asText("")).append(",");
            csv.append(t.path("clientDocument").asText("")).append(",");

            JsonNode cardScheme = t.path("cardScheme");
            csv.append(cardScheme.path("code").asText("")).append(",");
            csv.append(cardScheme.path("description").asText("")).append(",");

            csv.append(t.path("cardSchemeDescription").asText("")).append(",");
            csv.append(t.path("receivableUnitQuantity").asText("")).append(",");

            csv.append(t.path("grossAmountSum").asText("")).append(",");
            csv.append(t.path("grossAmountSumField").asText("")).append(",");
            csv.append(t.path("freeNegotiationAmountSum").asText("")).append(",");
            csv.append(t.path("freeNegotiationAmountSumField").asText("")).append(",");
            csv.append(t.path("paidAmountSum").asText("")).append(",");
            csv.append(t.path("paidAmountSumField").asText("")).append(",");
            csv.append(t.path("allocatedAmountSum").asText("")).append(",");
            csv.append(t.path("allocatedAmountSumField").asText("")).append(",");
            csv.append(t.path("receivableUnitAmountSum").asText("")).append(",");
            csv.append(t.path("receivableUnitAmountSumField").asText("")).append(",");
            csv.append(t.path("recordNumber").asText("")).append("\n");
        }
    }

    private void processarFileTrailerRU(JsonNode raiz, StringBuilder csv) {
        Logger.info("-> processarFileTrailerRU chamado");

        JsonNode trailer = raiz.path("fileTrailerRU");
        if (trailer == null || trailer.isEmpty()) {
            csv.append("0\n");
            return;
        }

        csv.append(trailer.path("recordType").asText("")).append(",");
        csv.append(trailer.path("clientDocument").asText("")).append(",");
        csv.append(trailer.path("receivableUnitQuantity").asText("")).append(",");
        csv.append(trailer.path("grossAmountTotal").asText("")).append(",");
        csv.append(trailer.path("grossAmountTotalField").asText("")).append(",");
        csv.append(trailer.path("freeNegotiationAmountTotal").asText("")).append(",");
        csv.append(trailer.path("freeNegotiationAmountTotalField").asText("")).append(",");
        csv.append(trailer.path("paidAmountTotal").asText("")).append(",");
        csv.append(trailer.path("paidAmountTotalField").asText("")).append(",");
        csv.append(trailer.path("allocatedAmountTotal").asText("")).append(",");
        csv.append(trailer.path("allocatedAmountTotalField").asText("")).append(",");
        csv.append(trailer.path("receivableUnitAmountTotal").asText("")).append(",");
        csv.append(trailer.path("receivableUnitAmountTotalField").asText("")).append(",");
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

    private String valueOrZero(JsonNode node) {
        return node.isNull() || node.isMissingNode() ? "0" : node.asText("");
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
