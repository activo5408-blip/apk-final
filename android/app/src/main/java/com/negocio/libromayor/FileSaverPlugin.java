package com.negocio.libromayor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.PluginMethod;

import java.io.OutputStream;

/**
 * Guarda archivos mediante el selector nativo de Android.
 *
 * IMPORTANTE:
 * No enviamos el archivo completo en una sola llamada de Capacitor.
 * Los respaldos pueden contener fotos y superar el límite de transacción
 * Binder de Android. Por eso el archivo se selecciona primero y después
 * se escribe en pequeños bloques base64.
 */
@CapacitorPlugin(name = "FileSaver")
public class FileSaverPlugin extends Plugin {

    private Uri pendingUri = null;

    @PluginMethod
    public void saveFile(PluginCall call) {
        String filename = call.getString("filename");
        if (filename == null || filename.isEmpty()) {
            call.reject("Falta el nombre del archivo");
            return;
        }

        String mimeType = call.getString("mimeType", "application/octet-stream");

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, filename);

        startActivityForResult(call, intent, "saveFileResult");
    }

    @ActivityCallback
    private void saveFileResult(PluginCall call, androidx.activity.result.ActivityResult result) {
        if (call == null) return;

        Intent resultIntent = result.getData();
        Uri uri = resultIntent != null ? resultIntent.getData() : null;

        if (result.getResultCode() != Activity.RESULT_OK || uri == null) {
            pendingUri = null;
            JSObject cancelled = new JSObject();
            cancelled.put("saved", false);
            cancelled.put("cancelled", true);
            call.resolve(cancelled);
            return;
        }

        try {
            // Crear/truncar el archivo inmediatamente, pero sin escribir el respaldo todavía.
            OutputStream out = getContext().getContentResolver().openOutputStream(uri, "wt");
            if (out == null) throw new Exception("No se pudo abrir el destino elegido");
            out.close();

            pendingUri = uri;

            JSObject ok = new JSObject();
            ok.put("saved", true);
            ok.put("cancelled", false);
            ok.put("uri", uri.toString());
            call.resolve(ok);
        } catch (Exception e) {
            pendingUri = null;
            call.reject("No se pudo preparar el archivo: " + e.getMessage(), e);
        }
    }

    @PluginMethod
    public void writeFileChunk(PluginCall call) {
        if (pendingUri == null) {
            call.reject("No hay un archivo de respaldo abierto");
            return;
        }

        String data = call.getString("data");
        if (data == null || data.isEmpty()) {
            call.reject("Bloque vacío");
            return;
        }

        try {
            byte[] bytes = Base64.decode(data, Base64.DEFAULT);
            OutputStream out = getContext().getContentResolver().openOutputStream(pendingUri, "wa");
            if (out == null) throw new Exception("No se pudo abrir el archivo");
            try {
                out.write(bytes);
                out.flush();
            } finally {
                out.close();
            }

            JSObject ret = new JSObject();
            ret.put("written", bytes.length);
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("No se pudo escribir el bloque: " + e.getMessage(), e);
        }
    }

    @PluginMethod
    public void finishFile(PluginCall call) {
        pendingUri = null;
        JSObject ret = new JSObject();
        ret.put("finished", true);
        call.resolve(ret);
    }

    @PluginMethod
    public void cancelFile(PluginCall call) {
        pendingUri = null;
        JSObject ret = new JSObject();
        ret.put("cancelled", true);
        call.resolve(ret);
    }
}
