package com.html.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {
    private WebView webView;
    private String preferredBrowserPkg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (!preferredBrowserPkg.isEmpty() && (url.startsWith("http://") || url.startsWith("https://"))) {
                    openInExternal(url, preferredBrowserPkg);
                    return true;
                }
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!preferredBrowserPkg.isEmpty() && (url.startsWith("http://") || url.startsWith("https://"))) {
                    openInExternal(url, preferredBrowserPkg);
                    return true;
                }
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                    .setMessage(message)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface d, int w) { result.confirm(); }
                    })
                    .setCancelable(false).show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                    .setMessage(message)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface d, int w) { result.confirm(); }
                    })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface d, int w) { result.cancel(); }
                    })
                    .setCancelable(false).show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
                final EditText input = new EditText(MainActivity.this);
                input.setText(defaultValue);
                new AlertDialog.Builder(MainActivity.this)
                    .setMessage(message)
                    .setView(input)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface d, int w) { result.confirm(input.getText().toString()); }
                    })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface d, int w) { result.cancel(); }
                    })
                    .setCancelable(false).show();
                return true;
            }
        });

        webView.loadUrl("file:///android_asset/www/index.html");
    }

    private void openInExternal(String url, String pkg) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setPackage(pkg);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Не удалось открыть в выбранном браузере", Toast.LENGTH_SHORT).show();
        }
    }

    public class AndroidBridge {
        @JavascriptInterface
        public String getInstalledBrowsers() {
            PackageManager pm = getPackageManager();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com"));
            List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL);
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            for (ResolveInfo info : list) {
                String pkg = info.activityInfo.packageName;
                String label = info.loadLabel(pm).toString().replace("\"", "'");
                if (!first) json.append(",");
                json.append("{\"name\":\"").append(label).append("\",\"pkg\":\"").append(pkg).append("\"}");
                first = false;
            }
            json.append("]");
            return json.toString();
        }

        @JavascriptInterface
        public String getCurrentWebViewInfo() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    android.content.pm.PackageInfo info = WebView.getCurrentWebViewPackage();
                    if (info != null) {
                        return info.packageName + " (" + info.versionName + ")";
                    }
                } catch (Exception ignored) {}
            }
            return "System WebView";
        }

        @JavascriptInterface
        public void setPreferredBrowser(String pkg) {
            preferredBrowserPkg = (pkg == null) ? "" : pkg;
        }

        @JavascriptInterface
        public void openUrlInBrowser(final String url, final String pkg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    openInExternal(url, pkg);
                }
            });
        }

        @JavascriptInterface
        public void openSystemWebViewPicker() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent intent = new Intent("android.settings.WEBVIEW_SETTINGS");
                        startActivity(intent);
                    } catch (Exception e) {
                        try {
                            startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                        } catch (Exception ex) {
                            Toast.makeText(MainActivity.this, "Откройте Настройки -> Для разработчиков -> Реализация WebView", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }

        @JavascriptInterface
        public void setUserAgent(final String ua) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.getSettings().setUserAgentString(ua);
                    webView.reload();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
