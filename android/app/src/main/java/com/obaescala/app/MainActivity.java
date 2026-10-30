package com.obaescala.app;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

// O app Oba Escala vai inteiro dentro do APK (pasta assets/www) e roda numa WebView, sem internet.
// Os dados ficam guardados no próprio celular (armazenamento da WebView deste app).
public class MainActivity extends Activity {
    private static final int ESCOLHER_ARQUIVO = 41;
    private WebView web;
    private ValueCallback<Uri[]> arquivoCallback;

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setTextZoom(100);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if ("file".equals(u.getScheme())) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, u)); // links externos abrem no navegador
                } catch (Exception e) {
                    // sem app para abrir: ignora
                }
                return true;
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (arquivoCallback != null) arquivoCallback.onReceiveValue(null);
                arquivoCallback = cb;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                try {
                    startActivityForResult(Intent.createChooser(i, "Escolher arquivo"), ESCOLHER_ARQUIVO);
                } catch (Exception e) {
                    arquivoCallback = null;
                    return false;
                }
                return true;
            }
        });

        web.addJavascriptInterface(new Ponte(), "ObaAndroid");
        web.loadUrl("file:///android_asset/www/index.html");
    }

    @Override
    protected void onActivityResult(int pedido, int resultado, Intent dados) {
        super.onActivityResult(pedido, resultado, dados);
        if (pedido != ESCOLHER_ARQUIVO || arquivoCallback == null) return;
        Uri[] escolhido = null;
        if (resultado == RESULT_OK && dados != null && dados.getData() != null) {
            escolhido = new Uri[] { dados.getData() };
        }
        arquivoCallback.onReceiveValue(escolhido);
        arquivoCallback = null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        // Primeiro fecha menu, painel ou tela cheia do app; só sai do app se não houver nada aberto.
        web.evaluateJavascript("(window.__obaVoltar && window.__obaVoltar()) ? '1' : '0'", valor -> {
            if (!"\"1\"".equals(valor)) finish();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        web.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
    }

    // Chamado pelo app (JavaScript) para salvar PDF, CSV e backup.
    public class Ponte {
        @JavascriptInterface
        public String salvarArquivo(String nome, String mime, String base64) {
            try {
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                String limpo = nome.replaceAll("[\\\\/:*?\"<>|]", "_");
                if (Build.VERSION.SDK_INT >= 29) {
                    ContentValues cv = new ContentValues();
                    cv.put(MediaStore.MediaColumns.DISPLAY_NAME, limpo);
                    cv.put(MediaStore.MediaColumns.MIME_TYPE, mime);
                    cv.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Oba Escala");
                    Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                    if (uri == null) return "erro:não foi possível criar o arquivo em Downloads.";
                    try (OutputStream o = getContentResolver().openOutputStream(uri)) {
                        if (o == null) return "erro:não foi possível gravar em Downloads.";
                        o.write(bytes);
                    }
                    return "ok:Salvo em Downloads/Oba Escala: " + limpo;
                }
                // Android 9 ou anterior: abre o menu para salvar ou enviar (Drive, WhatsApp, e-mail).
                File pasta = new File(getCacheDir(), "arquivos");
                pasta.mkdirs();
                File f = new File(pasta, limpo);
                try (FileOutputStream o = new FileOutputStream(f)) {
                    o.write(bytes);
                }
                Uri uri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".arquivos", f);
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType(mime);
                i.putExtra(Intent.EXTRA_STREAM, uri);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                runOnUiThread(() -> startActivity(Intent.createChooser(i, "Salvar ou enviar")));
                return "ok:";
            } catch (Exception e) {
                return "erro:" + e.getMessage();
            }
        }
    }
}
