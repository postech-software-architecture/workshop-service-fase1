package com.postech.workshop_service.domain.valueobjects;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Objeto de Valor imutável que representa um CPF ou CNPJ.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Documento {
    @EqualsAndHashCode.Include
    private final String valor;
    
    private final TipoDocumento tipo;

    public Documento(String documentoOriginal) {
        if (documentoOriginal == null) {
            throw new IllegalArgumentException("Documento não pode ser nulo.");
        }
        
        String cleanDoc = documentoOriginal.replaceAll("[^0-9]", "");
        
        if (cleanDoc.length() == 11) {
            if (!validarCPF(cleanDoc)) {
                throw new IllegalArgumentException("CPF inválido.");
            }
            this.tipo = TipoDocumento.CPF;
            this.valor = cleanDoc;
        } else if (cleanDoc.length() == 14) {
            if (!validarCNPJ(cleanDoc)) {
                throw new IllegalArgumentException("CNPJ inválido.");
            }
            this.tipo = TipoDocumento.CNPJ;
            this.valor = cleanDoc;
        } else {
            throw new IllegalArgumentException("Documento deve ser um CPF (11 dígitos) ou CNPJ (14 dígitos) válido.");
        }
    }

    private boolean validarCPF(String cpf) {
        if (cpf.matches("(\\d)\\1{10}")) return false;

        int d1 = 0, d2 = 0;
        int digit1, digit2, rest;
        int nDigit;

        for (int nCount = 1; nCount < cpf.length() - 1; nCount++) {
            nDigit = Integer.parseInt(cpf.substring(nCount - 1, nCount));
            d1 = d1 + (11 - nCount) * nDigit;
            d2 = d2 + (12 - nCount) * nDigit;
        }

        rest = (d1 % 11);
        if (rest < 2) digit1 = 0;
        else digit1 = 11 - rest;

        d2 = d2 + 2 * digit1;
        rest = (d2 % 11);
        if (rest < 2) digit2 = 0;
        else digit2 = 11 - rest;

        String dvExpected = String.valueOf(digit1) + String.valueOf(digit2);
        return cpf.substring(cpf.length() - 2).equals(dvExpected);
    }

    private boolean validarCNPJ(String cnpj) {
        if (cnpj.matches("(\\d)\\1{13}")) return false;

        int[] pesoCNPJ = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Integer.parseInt(cnpj.substring(i, i + 1)) * pesoCNPJ[i + 1];
        }
        int d1 = 11 - (sum % 11);
        if (d1 >= 10) d1 = 0;

        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += Integer.parseInt(cnpj.substring(i, i + 1)) * pesoCNPJ[i];
        }
        int d2 = 11 - (sum % 11);
        if (d2 >= 10) d2 = 0;

        return cnpj.substring(12).equals("" + d1 + d2);
    }

    public String mascarado() {
        if (tipo == TipoDocumento.CPF) {
            return "***." + valor.substring(3, 6) + "." + valor.substring(6, 9) + "-**";
        } else {
            return "**.*" + valor.substring(2, 4) + ".***/" + valor.substring(8, 12) + "-**";
        }
    }
}
