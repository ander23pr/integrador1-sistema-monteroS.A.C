package com.montero.app.service;

import com.montero.app.model.SesionUsuario;
import com.montero.app.model.Usuario;
import com.montero.app.repository.SesionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SesionService {

    private static final Logger logger = LoggerFactory.getLogger(SesionService.class);

    private final SesionRepository sesionRepository;

    public SesionService(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    @Transactional
    public void registrarSesion(Usuario usuario, String sessionId, String userAgent, String ip) {
        SesionUsuario sesion = new SesionUsuario();
        sesion.setUsuario(usuario);
        sesion.setSessionId(sessionId);
        sesion.setNombreDispositivo(parsearDispositivo(userAgent));
        sesion.setIp(ip);
        sesion.setActiva(usuario != null);
        sesion.setFechaInicio(LocalDateTime.now());

        sesionRepository.save(sesion);
        if (usuario != null) {
            logger.info("Sesión iniciada para {} desde {}", usuario.getEmail(), sesion.getNombreDispositivo());
        } else {
            logger.warn("Intento de inicio de sesión fallido desde {}", sesion.getNombreDispositivo());
        }
    }

    @Transactional(readOnly = true)
    public List<SesionUsuario> obtenerSesionesActivas(Long usuarioId) {
        return sesionRepository.findByUsuarioIdAndActivaTrue(usuarioId);
    }

    @Transactional(readOnly = true)
    public SesionUsuario obtenerSesionActual(String sessionId) {
        return sesionRepository.findBySessionIdAndActivaTrue(sessionId).orElse(null);
    }

    @Transactional
    public void cerrarSesion(Long sesionId, Long usuarioId) {
        SesionUsuario sesion = sesionRepository.findById(sesionId).orElse(null);
        if (sesion == null || !sesion.getUsuario().getId().equals(usuarioId)) {
            logger.warn("Intento de cerrar sesión inexistente o de otro usuario");
            return;
        }
        sesion.setActiva(false);
        sesion.setFechaCierre(LocalDateTime.now());
        sesionRepository.save(sesion);
        logger.info("Sesión {} cerrada para usuario {}", sesionId, sesion.getUsuario().getEmail());
    }

    @Transactional
    public void cerrarSesionPorToken(String sessionId, Long usuarioId) {
        SesionUsuario sesion = sesionRepository.findBySessionIdAndActivaTrue(sessionId).orElse(null);
        if (sesion == null || !sesion.getUsuario().getId().equals(usuarioId)) {
            return;
        }
        sesion.setActiva(false);
        sesion.setFechaCierre(LocalDateTime.now());
        sesionRepository.save(sesion);
        logger.info("Sesión por token {} cerrada para usuario {}", sessionId, sesion.getUsuario().getEmail());
    }

    @Transactional
    public void cerrarTodasExcepto(String sessionIdActual, Long usuarioId) {
        List<SesionUsuario> activas = sesionRepository.findByUsuarioIdAndActivaTrue(usuarioId);
        for (SesionUsuario sesion : activas) {
            if (!sesion.getSessionId().equals(sessionIdActual)) {
                sesion.setActiva(false);
                sesion.setFechaCierre(LocalDateTime.now());
            }
        }
        sesionRepository.saveAll(activas);
        logger.info("Sesiones cerradas para usuario {} excepto la actual", usuarioId);
    }

    @Transactional(readOnly = true)
    public List<SesionUsuario> obtenerActividadReciente(Long usuarioId) {
        return sesionRepository.findTop100ByUsuarioIdOrUsuarioIdIsNullOrderByFechaInicioDesc(usuarioId);
    }

    private String parsearDispositivo(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Navegador web";
        }
        String ua = userAgent.toLowerCase();
        String os = "Navegador web";
        String navegador = "";

        if (ua.contains("windows nt")) os = "Windows";
        else if (ua.contains("macintosh") || ua.contains("mac os x")) os = "Mac";
        else if (ua.contains("linux") && !ua.contains("android")) os = "Linux";
        else if (ua.contains("android")) os = "Android";
        else if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ipod")) os = "iOS";

        if (ua.contains("edg") || ua.contains("edge")) navegador = "Edge";
        else if (ua.contains("chrome") && !ua.contains("edg")) navegador = "Chrome";
        else if (ua.contains("firefox")) navegador = "Firefox";
        else if (ua.contains("safari") && !ua.contains("chrome")) navegador = "Safari";

        boolean esMovil = ua.contains("mobile") || ua.contains("android") || ua.contains("iphone");

        if (esMovil && navegador.isEmpty()) {
            if (ua.contains("instagram")) return "App iOS" + (os.equals("Android") ? "Android" : "");
            return os + " · Móvil";
        }
        if (navegador.isEmpty()) return os;
        return os + " · " + navegador;
    }
}