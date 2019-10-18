package com.example.tcc_marcos_willian.Modelos;

public class Upload_Arquivo {
    public String numeroCopias;
    public String url;
    public String nomeUsuario;
    public String status;

    public Upload_Arquivo() {
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeAluno) {
        this.nomeUsuario = nomeAluno;
    }

    public String getNumeroCopias() {
        return numeroCopias;
    }

    public void setNumeroCopias(String numCopias) {
        this.numeroCopias = numCopias;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
