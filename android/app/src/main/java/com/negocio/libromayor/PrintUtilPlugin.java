package com.negocio.libromayor;

import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * Imprime la pantalla actual usando el sistema de impresión nativo de
 * Android (PrintManager). window.print() del navegador NO funciona dentro
 * del WebView de la app empaquetada porque no hay ningún visor de impresión
 * detrás; este plugin sí abre el diálogo real de impresión / "Guardar como
 * PDF" de Android.
 *
 * Desde JavaScript:
 *   Capacitor.Plugins.PrintUtil.imprimir({ titulo: "Panel general" })
 */
@CapacitorPlugin(name = "PrintUtil")
public class PrintUtilPlugin extends Plugin {

    @PluginMethod
    public void imprimir(PluginCall call) {
        try {
            String titulo = call.getString("titulo", "Libro Mayor - Reporte");
            WebView webView = getBridge().getWebView();
            PrintManager printManager = (PrintManager) getContext().getSystemService(Context.PRINT_SERVICE);

            if (webView == null || printManager == null) {
                call.reject("No se pudo acceder al sistema de impresión");
                return;
            }

            PrintDocumentAdapter adapter = webView.createPrintDocumentAdapter(titulo);
            printManager.print(titulo, adapter, new PrintAttributes.Builder().build());
            call.resolve();
        } catch (Exception e) {
            call.reject("No se pudo iniciar la impresión: " + e.getMessage(), e);
        }
    }
}
