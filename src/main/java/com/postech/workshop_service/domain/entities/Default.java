package com.postech.workshop_service.domain.entities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indica ao MapStruct qual construtor deve ser utilizado quando ha ambiguidade entre
 * multiplos construtores em entidades de dominio. MapStruct reconhece qualquer anotacao
 * com nome simples "Default".
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.CLASS)
public @interface Default {

}
