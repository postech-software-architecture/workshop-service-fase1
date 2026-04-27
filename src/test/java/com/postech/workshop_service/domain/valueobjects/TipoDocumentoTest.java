package com.postech.workshop_service.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class TipoDocumentoTest {

    @Test
    void shouldExposeSupportedDocumentTypes() {
        assertArrayEquals(
                new TipoDocumento[]{TipoDocumento.CPF, TipoDocumento.CNPJ},
                TipoDocumento.values()
        );
    }
}
