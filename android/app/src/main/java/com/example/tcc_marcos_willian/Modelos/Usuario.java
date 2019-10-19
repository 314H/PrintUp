package com.example.tcc_marcos_willian.Modelos;

public class Usuario {
    public String nome, email, cpf, nomeLowerCase;

    public Usuario() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNomeLowerCase() {
        return nomeLowerCase;
    }

    public void setNomeLowerCase(String nomeLowerCase) {
        this.nomeLowerCase = nomeLowerCase;
    }
}
