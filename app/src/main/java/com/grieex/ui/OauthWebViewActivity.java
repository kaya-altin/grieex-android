package com.grieex.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.grieex.R;
import com.grieex.helper.GrieeXSettings;

/**
 * Created by Griee on 18.03.2016.
 */
public class OauthWebViewActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    public static final String AUTHORIZATION_URL = "auth_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);

        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.login_title);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        String auth_url = this.getIntent().getStringExtra(AUTHORIZATION_URL);

        WebView webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress_top);

        webView.setWebViewClient(webViewClient);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        webView.clearCache(true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.animate().alpha(0.0f).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                } else {
                    if (progressBar.getVisibility() == View.GONE) {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setAlpha(0.0f);
                        progressBar.animate().alpha(1.0f);
                    }
                }
            }
        });

        webView.loadUrl(auth_url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final WebViewClient webViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url != null && url.startsWith(GrieeXSettings.TraktCallbackUrl)) {
                Uri uri = Uri.parse(url);

                final String code = uri.getQueryParameter(TraktAuthActivity.QUERY_CODE);
                //Timber.d("We got a code! %s", code);

                Intent result = new Intent();
                result.putExtra(TraktAuthActivity.QUERY_CODE, code);
                setResult(RESULT_OK, result);
                finish();
                return true;
            }

            return false;
        }
    };


}
