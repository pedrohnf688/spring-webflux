package br.com.udemy.tasks.exception;

public class CepNotFountException extends RuntimeException {

    public CepNotFountException(){
        super("CEP not found");
    }
}
