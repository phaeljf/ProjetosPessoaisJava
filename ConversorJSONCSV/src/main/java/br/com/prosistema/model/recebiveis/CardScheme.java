package br.com.prosistema.model.recebiveis;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardScheme {
    private String code;        // Código da bandeira (ex: VCC, VCD)
    private String description; // Nome da bandeira (ex: Visa Crédito)
}
