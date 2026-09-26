package com.negocio.libromayor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.PluginMethod;

import java.io.OutputStream;

/**
 * Guarda un archivo usando el selector nativo de Android (Storage Access
 * Framework, ACTION_CREATE_DOCUMENT). El usuario elige la carpeta y el nombre
 * (Descargas, Documentos, una carpeta propia, Drive, tarjeta SD, etc.).
 *
 * Desde JavaScript:
 *   Capacitor.Plugins.FileSaver.saveFile({ filename, mimeType, data })
 *   donde data es el contenido en base64. Responde:
 *   { saved:true, uri } o { saved:false, cancelled:true }
 */
@CapacitorPlugin(name = "FileSaver")
public class FileSaverPlugin extends Plugin {

    @PluginMethod
    public void saveFile(PluginCall call) {
        String filename = call.getString("filename");
        String data = call.getString("data");
        if (filename == null || filename.isEmpty() || data == null) {
            call.reject("Faltan el nombre del archivo o su contenido");
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
    private void saveFileResult(PluginCall call, ActivityResult result) {
        if (call == null) {
            return;
        }

        Intent resultIntent = result.getData();
        Uri uri = resultIntent != null ? resultIntent.getData() : null;

        if (result.getResultCode() != Activity.RESULT_OK || uri == null) {
            JSObject cancelled = new JSObject();
            cancelled.put("saved", false);
            cancelled.put("cancelled", true);
            call.resolve(cancelled);
            return;
        }

        final String data = call.getString("data");
        final Uri target = uri;

        // Escribir fuera del hilo principal para no congelar la interfaz con respaldos grandes.
        getBridge().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] bytes = Base64.decode(data, Base64.DEFAULT);
                    // "wt" = escribir y truncar, para que un archivo existente no quede con basura al final.
                    OutputStream out = getContext().getContentResolver().openOutputStream(target, "wt");
                    try {
                        if (out == null) {
                            call.reject("No se pudo abrir el destino elegido");
                            return;
                        }
                        out.write(bytes);
                        out.flush();
                    } finally {
                        if (out != null) {
                            out.close();
                        }
                    }
                    JSObject ok = new JSObject();
                    ok.put("saved", true);
                    ok.put("cancelled", false);
                    ok.put("uri", target.toString());
                    ok.put("size", bytes.length);
                    call.resolve(ok);
                } catch (Exception e) {
                    call.reject("No se pudo guardar el archivo: " + e.getMessage(), e);
                }
            }
        });
    }
}
