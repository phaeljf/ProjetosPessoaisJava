package br.com.prosistema.model.recebiveis;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileHeaderRecebiveis {
    private String recordType;
    private String processingDate;
    private String acquiringName;
    private String fileTypeDescription;
    private String fileNumber;
    private String clientName;
    private String clientDocument;
    private String processingType;
    private String fileLayoutVersion;
    private String recordNumber;
}
