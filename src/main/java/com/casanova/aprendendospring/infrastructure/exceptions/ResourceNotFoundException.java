package com.casanova.aprendendospring.infrastructure.exceptions;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String mensage){
        super(mensage);
    }
    public ResourceNotFoundException(String mensage,Throwable Throwable){
        super(mensage, Throwable);
    }

}
