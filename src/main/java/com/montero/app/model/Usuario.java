package com.montero.app.model;

import jakarta.persistence.*;

/**
 * Entidad Usuario utilizada únicamente para la autenticación y login básico.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String rol;

    @Column(length = 100)
    private String nombre;

    @Column(length = 20)
    private String dni;

    @Column(length = 20)
    private String telefono;

    @Column(length = 50)
    private String preferenciaAsiento;

    @Column(length = 50)
    private String preferenciaServicio;

    @Column(length = 255)
    private String fotoPerfil;

    @Column(nullable = false)
    private Boolean activo = true;

    public Usuario() {
    }

    public Usuario(Long id, String email, String password, String rol) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
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

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
