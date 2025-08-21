package br.com.prosistema.model.recebiveis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArquivoRecebiveis {
    private FileHeaderRecebiveis fileHeader;
    private List<ReceivableUnit> receivableUnits;
    private List<ReceivableUnitTrailer> receivableUnitTrailers;
    private FileTrailerRU fileTrailerRU;
}
