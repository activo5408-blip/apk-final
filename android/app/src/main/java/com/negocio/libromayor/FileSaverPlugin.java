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
import com.getcapacitor.PluginMethod;

import java.io.OutputStream;

/**
 * Guardado robusto mediante el selector nativo de Android.
 *
 * El archivo se abre UNA sola vez y permanece abierto mientras JavaScript
 * envía bloques pequeños. Esto evita depender del modo "append" de los
 * distintos DocumentProvider (Descargas, Drive, SD, etc.).
 */
@CapacitorPlugin(name = "FileSaver")
public class FileSaverPlugin extends Plugin {

    private Uri pendingUri;
    private OutputStream pendingOutput;
    private long pendingBytes;

    @PluginMethod
    public void saveFile(PluginCall call) {
        String filename = call.getString("filename");
        if (filename == null || filename.trim().isEmpty()) {
            call.reject("Falta el nombre del archivo");
            return;
        }

        String mimeType = call.getString("mimeType", "application/octet-stream");

        closePendingOutput();

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
            clearPending(false);
            JSObject cancelled = new JSObject();
            cancelled.put("saved", false);
            cancelled.put("cancelled", true);
            call.resolve(cancelled);
            return;
        }

        try {
            // Abrimos el documento una sola vez y mantenemos el stream durante
            // toda la transferencia. "wt" crea/trunca el archivo.
            OutputStream out = getContext().getContentResolver().openOutputStream(uri, "wt");
            if (out == null) {
                throw new Exception("No se pudo abrir el destino elegido");
            }

            pendingUri = uri;
            pendingOutput = out;
            pendingBytes = 0;

            JSObject ok = new JSObject();
            ok.put("saved", true);
            ok.put("cancelled", false);
            ok.put("uri", uri.toString());
            call.resolve(ok);
        } catch (Exception e) {
            clearPending(true);
            call.reject("No se pudo preparar el archivo: " + e.getMessage(), e);
        }
    }

    @PluginMethod
    public void writeFileChunk(PluginCall call) {
        if (pendingOutput == null || pendingUri == null) {
            call.reject("No hay un archivo de respaldo abierto");
            return;
        }

        String data = call.getString("data");
        if (data == null || data.isEmpty()) {
            call.reject("Bloque vacío");
            return;
        }

        try {
            byte[] bytes = Base64.decode(data, Base64.NO_WRAP);
            if (bytes.length == 0) {
                throw new Exception("El bloque decodificado está vacío");
            }

            pendingOutput.write(bytes);
            pendingOutput.flush();
            pendingBytes += bytes.length;

            JSObject ret = new JSObject();
            ret.put("written", bytes.length);
            ret.put("totalWritten", pendingBytes);
            call.resolve(ret);
        } catch (Exception e) {
            clearPending(true);
            call.reject("No se pudo escribir el respaldo: " + e.getMessage(), e);
        }
    }

    @PluginMethod
    public void finishFile(PluginCall call) {
        if (pendingOutput == null || pendingUri == null) {
            call.reject("No hay un archivo de respaldo abierto");
            return;
        }

        try {
            pendingOutput.flush();
            pendingOutput.close();

            long total = pendingBytes;
            pendingOutput = null;
            pendingUri = null;
            pendingBytes = 0;

            JSObject ret = new JSObject();
            ret.put("finished", true);
            ret.put("bytes", total);
            call.resolve(ret);
        } catch (Exception e) {
            clearPending(true);
            call.reject("No se pudo finalizar el respaldo: " + e.getMessage(), e);
        }
    }

    @PluginMethod
    public void cancelFile(PluginCall call) {
        clearPending(true);
        JSObject ret = new JSObject();
        ret.put("cancelled", true);
        call.resolve(ret);
    }

    private void closePendingOutput() {
        if (pendingOutput != null) {
            try { pendingOutput.close(); } catch (Exception ignored) {}
        }
        pendingOutput = null;
        pendingUri = null;
        pendingBytes = 0;
    }

    private void clearPending(boolean deletePartial) {
        Uri uri = pendingUri;
        closePendingOutput();

        if (deletePartial && uri != null) {
            try {
                getContext().getContentResolver().delete(uri, null, null);
            } catch (Exception ignored) {
                // Algunos DocumentProvider no permiten borrar desde aquí.
            }
        }
    }
}
