package com.montero.app.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Genera códigos QR directamente en el servidor (con ZXing) y los entrega
 * como una imagen PNG en Base64, lista para incrustar en un <img> de
 * Thymeleaf. Al generarse en el backend, el QR siempre aparece en la vista
 * sin depender de que el navegador descargue ninguna librería externa.
 */
public final class QrCodeGenerator {

    private QrCodeGenerator() {
    }

    /**
     * Genera un QR a partir del texto/URL indicado.
     *
     * @param contenido texto o URL que codificará el QR
     * @param tamanoPx  ancho y alto de la imagen resultante, en píxeles
     * @return el PNG codificado en Base64, o {@code null} si ocurre un error
     *         (por ejemplo, contenido vacío), para que la vista pueda mostrar
     *         un estado alternativo en vez de romper la página.
     */
    public static String generarComoBase64(String contenido, int tamanoPx) {
        if (contenido == null || contenido.isBlank()) {
            return null;
        }
        try {
            Map<EncodeHintType, Object> opciones = new EnumMap<>(EncodeHintType.class);
            opciones.put(EncodeHintType.MARGIN, 1);
            opciones.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            //Crea una matriz de puntos QR con el contenido de la URL
            BitMatrix matriz = new QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, tamanoPx, tamanoPx, opciones);
//          Convierte a imagen PNG            
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matriz, "PNG", salida);
//          Codificarla a Base64
            return Base64.getEncoder().encodeToString(salida.toByteArray());
        } catch (WriterException | IOException excepcionGeneracionQr) {
            return null;
        }
    }
}
