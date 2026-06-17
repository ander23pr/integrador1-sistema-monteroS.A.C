package com.montero.app.dto;

public class PerfilUsuarioDTO {

    private Long id;
    private String nombre;
    private String dni;
    private String telefono;
    private String preferenciaAsiento;
    private String preferenciaServicio;
    private String fotoPerfil;
    private long viajesTotales;
    private String destinoFavorito;

    public PerfilUsuarioDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPreferenciaAsiento() {
        return preferenciaAsiento;
    }

    public void setPreferenciaAsiento(String preferenciaAsiento) {
        this.preferenciaAsiento = preferenciaAsiento;
    }

    public String getPreferenciaServicio() {
        return preferenciaServicio;
    }

    public void setPreferenciaServicio(String preferenciaServicio) {
        this.preferenciaServicio = preferenciaServicio;
    }

    public long getViajesTotales() {
        return viajesTotales;
    }

    public void setViajesTotales(long viajesTotales) {
        this.viajesTotales = viajesTotales;
    }

    public String getDestinoFavorito() {
        return destinoFavorito;
    }

    public void setDestinoFavorito(String destinoFavorito) {
        this.destinoFavorito = destinoFavorito;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }
}
