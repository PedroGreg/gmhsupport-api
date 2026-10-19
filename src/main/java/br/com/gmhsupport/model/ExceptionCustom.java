package br.com.gmhsupport.model;

import java.util.Date;

public record ExceptionCustom(Date timestamp, String mensagem, String details) {
}
