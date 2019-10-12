package com.example.tcc_marcos_willian.Modelos;

public class Upload_Arquivo {
    public String numero_copias;
    public String url;
    public String nome_usuario;
    public String status;

    public Upload_Arquivo() {
    }

    public String getNome_usuario() {
        return nome_usuario;
    }

    public void setNome_usuario(String nomeAluno) {
        this.nome_usuario = nomeAluno;
    }

    public String getNumero_copias() {
        return numero_copias;
    }

    public void setNumero_copias(String numCopias) {
        this.numero_copias = numCopias;
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
