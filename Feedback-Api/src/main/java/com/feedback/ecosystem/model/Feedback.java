package com.feedback.ecosystem.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Feedback {

    private String id;
    private String usuario;
    private String comentario;
    private int nota;
    private long timestamp;
    private boolean ehCritico;

    public Feedback() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public int getNota() {
        return nota;
    }

    public void setNota(int nota) {
        this.nota = nota;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isEhCritico() {
        return ehCritico;
    }

    public void setEhCritico(boolean ehCritico) {
        this.ehCritico = ehCritico;
    }
}
