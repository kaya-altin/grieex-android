package com.grieex.core;

import com.grieex.helper.NLog;
import com.grieex.core.listener.OnBeyazperdeEventListener;
import com.grieex.model.tables.Movie;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import info.movito.themoviedbapi.tools.WebBrowser;

public class Beyazperde {
	private static final String TAG = Beyazperde.class.getName();
	private OnBeyazperdeEventListener mListener;


	public Beyazperde() {


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
			strStart = "<meta property=\"og:title\" content=\"";
			strEnd = "\" />";
			pr = CoreUtils.GetText(response, strStart, strEnd, iStartPos);
			pr.text = CoreUtils.StripHtml(pr.text);
			pr.text = CoreUtils.StripBlanks(pr.text);
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
		final String url = "http://www.beyazperde.com/ara/?q=" + MovieName;
		AsyncHttpClient client = new AsyncHttpClient();

		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
				if (mListener != null)
					mListener.onCompleted(null);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responsebyte) {
				if (responsebyte == null)
					return;

				String response = new String(responsebyte);
				response = CoreUtils.GetText(response, "Film adları arasında", "</table>", 0).text;

				Pattern pattern = Pattern.compile("<td style=\" vertical-align:top;\">");
				Matcher matcher = pattern.matcher(response);

				while (matcher.find()) {
					int iStartIndex = matcher.start();

					int nStartPos;
					int nEndPos;

					nStartPos = response.indexOf("film-", iStartIndex);
					nEndPos = response.indexOf("/'>", nStartPos);
					String key = response.substring(nStartPos, nEndPos);

					nStartPos = response.indexOf("src='", nEndPos) + 5;
					nEndPos = response.indexOf("alt=", nEndPos) - 2;
					String poster = response.substring(nStartPos, nEndPos);

					nStartPos = response.indexOf("<a href='/filmler/" + key + "/'>", nEndPos);
					nEndPos = response.indexOf("<span class=\"fs11\">", nEndPos);
					String title = response.substring(nStartPos, nEndPos);
					title = CoreUtils.StripHtml(title);
					title = CoreUtils.StripBlanks(title);

					nStartPos = response.indexOf("", nEndPos) + 20;
					nEndPos = response.indexOf("<br />", nEndPos);
					String year = response.substring(nStartPos, nEndPos);

					sr.add(new SearchResult("http://www.beyazperde.com/filmler/" + key, key, title, poster, year));
				}

				if (mListener != null)
					mListener.onCompleted(sr);
			}
		});

	}

	public void setBeyazperdeEventListener(OnBeyazperdeEventListener onBeyazperdeEventListener) {
		mListener = onBeyazperdeEventListener;

	}
}
