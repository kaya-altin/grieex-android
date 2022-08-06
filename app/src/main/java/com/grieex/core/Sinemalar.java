package com.grieex.core;

import com.grieex.core.listener.OnSinemalarEventListener;
import com.grieex.helper.NLog;
import com.grieex.model.tables.Movie;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import info.movito.themoviedbapi.tools.WebBrowser;

public class Sinemalar {
	private static final String TAG = Sinemalar.class.getName();
	private OnSinemalarEventListener mListener;

    public Sinemalar() {

	}

	public void Parse(String url) {
		try {
			WebBrowser web = new WebBrowser();
			String bodyHtml = web.request(url);

			TextParse(bodyHtml, url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void ParseAsync(final String url) {
		try {
			AsyncHttpClient client = new AsyncHttpClient();
			client.get(url, new AsyncHttpResponseHandler() {
				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
					if (mListener != null)
						mListener.onNotCompleted(e, new String(errorResponse));
				}

				@Override
				public void onSuccess(int statusCode, Header[] headers, final byte[] response) {
					if (response == null)
						return;
					try {
						// AsyncTask<String, Void, String> async = new
						// AsyncTask<String, Void, String>() {
						//
						// @Override
						// protected String doInBackground(String... arg0) {
						TextParse(new String(response), url);

						// return null;
						// }
						//
						// };
						// async.execute();
					} catch (Exception e) {
						NLog.e(TAG, e);
					}
				}
			});
		} catch (Exception e) {
			if (mListener != null) {
				mListener.onNotCompleted(e, "");
			}
		}
	}

	private void TextParse(String response, String url) {
		try {
			Movie m = new Movie();

			String strStart;
			String strEnd;
            int iStartPos = 0;
			ParseResult pr;

			// ********* OtherName ********* //
			strStart = "<title>";
			strEnd = "</title>";
			pr = CoreUtils.GetText(response, strStart, strEnd, iStartPos);
			pr.text = CoreUtils.StripHtml(pr.text);
			pr.text = CoreUtils.StripBlanks(pr.text);
			if (pr.text.contains("(")) {
				pr.text = pr.text.substring(0, pr.text.indexOf("(")).trim();
			}
			m.setOtherName(pr.text);

			// ********* OtherPlot ********* //
			strStart = "<p itemprop=\"description\">";
			strEnd = "</p>";
			pr = CoreUtils.GetText(response, strStart, strEnd, iStartPos);
			pr.text = CoreUtils.StripHtml(pr.text);
			pr.text = CoreUtils.StripBlanks(pr.text);
			m.setOtherPlot(pr.text);

			if (mListener != null)
				mListener.onCompleted(m);
		} catch (Exception e) {
			NLog.e(TAG, e);
		}
	}

	public void Search(String MovieName) {
		final ArrayList<SearchResult> sr = new ArrayList<>();
		final String url = "http://www.sinemalar.com/ara/?type=all&q=" + MovieName;
		AsyncHttpClient client = new AsyncHttpClient();

		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
				if (mListener != null)
					mListener.onCompleted(null);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, final byte[] responsebyte) {
				if (responsebyte == null)
					return;

				String response = new String(responsebyte);
				Pattern pattern = Pattern.compile("<div class=\"grid8 bestof shadow\">");
				Matcher matcher = pattern.matcher(response);

				while (matcher.find()) {
					int iStartIndex = matcher.start();
					int nStartPos;
					int nEndPos;

					nStartPos = response.indexOf("film/", iStartIndex);
					nEndPos = response.indexOf("\"", nStartPos);
					String key = response.substring(nStartPos, nEndPos);

					nStartPos = response.indexOf("title=", iStartIndex);
					nEndPos = response.indexOf("class", nEndPos);
					String title = response.substring(nStartPos + 7, nEndPos - 2);
					title = CoreUtils.StripHtml(title);
					title = CoreUtils.StripBlanks(title);

					nStartPos = response.indexOf("<img src=", nEndPos);
					nEndPos = response.indexOf("alt=", nStartPos + 2);
					String poster = response.substring(nStartPos + 10, nEndPos - 2);

					sr.add(new SearchResult("http://www.sinemalar.com/" + key, key, title, poster, ""));
				}

				if (mListener != null)
					mListener.onCompleted(sr);
			}
		});

	}

	public void setCustomEventListener(OnSinemalarEventListener onBeyazperdeEventListener) {
		mListener = onBeyazperdeEventListener;

	}
}
