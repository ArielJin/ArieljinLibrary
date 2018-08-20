package com.arieljin.library.manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.arieljin.library.abs.AbsApplication;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class HttpManger {
	public static final int GET = 0, POST = 1;

	// public static CookieStore cookieStore;

	// private static HttpParams params;
	// private static ClientConnectionManager conMgr;
	private static volatile DefaultHttpClient httpClient;
	private static volatile ConcurrentHashMap<Integer, HttpRequestBase> requests;

	private static synchronized void initHttpClient() {
		if (httpClient == null) {

			BasicHttpParams params = new BasicHttpParams();
			params.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			HttpProtocolParams.setUseExpectContinue(params, true);
//			HttpProtocolParams.setUserAgent(params, "Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) " + "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
			HttpProtocolParams.setUserAgent(params, AbsApplication.getInstance().getPackageName() + "/" + AbsApplication.getInstance().VERSION+" " + "/" + AbsApplication.getInstance().getAppChannelMetaDataForTask() + "/ " +"Mozilla/5.0(Linux;U;Android "+ Build.VERSION.RELEASE +";"+ Locale.getDefault().getLanguage() + "-"+ Locale.getDefault().getCountry() +";"+Build.BRAND + " "+Build.MODEL+ "/"+Build.DISPLAY + ")");
			//修改org.apache.http的主机名验证
			try {
				KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
				trustStore.load(null, null);
				SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
				sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			} catch (KeyStoreException e) {
				e.printStackTrace();
			} catch (CertificateException e) {
				e.printStackTrace();
			} catch (UnrecoverableKeyException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (KeyManagementException e) {
				e.printStackTrace();
			}

			SSLSocketFactory.getSocketFactory().setHostnameVerifier(new AllowAllHostnameVerifier());
			// 超时设置

			/* 从连接池中取连接的超时时间 */
			ConnManagerParams.setTimeout(params, 5000);
			/* 连接超时 */
			HttpConnectionParams.setConnectionTimeout(params, 10000);
			/* 请求超时 */
			HttpConnectionParams.setSoTimeout(params, 15000);
			// 设置我们的HttpClient支持HTTP和HTTPS两种模式
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			// 使用线程安全的连接管理来创建HttpClient
			ThreadSafeClientConnManager conMgr = new ThreadSafeClientConnManager(params, schReg);

			// if (httpClient != null) {
			// client.setCookieStore(httpClient.getCookieStore());
			// } else {
			// // client.setCookieStore(loadCookie());
			// }

			httpClient = new DefaultHttpClient(conMgr, params);
		}

		if (requests == null) {
			requests = new ConcurrentHashMap<Integer, HttpRequestBase>();
		}
	}

	private static class MySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException,
				KeyManagementException, KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType)
						throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType)
						throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
				throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}

	public static boolean isNetworkAvailable() {
		ConnectivityManager connectivity = (ConnectivityManager) AbsApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			// Log.i("NetWorkState", "Unavailabel");
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].isAvailable() && info[i].isConnected()) {
						// Log.i("NetWorkState", "Availabel");
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isNetworkOnWifi() {
		ConnectivityManager connectivity = (ConnectivityManager) AbsApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getTypeName().equals("WIFI") && info[i].isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static HttpResponse getResponse(int id, String url, List<NameValuePair> params, int type) throws Exception {
		return getResponse(id, url, null, params, type);
	}

	public static HttpResponse getResponse(int id, String url, List<NameValuePair> params) throws Exception {
		return getResponse(id, url, null, params, POST);
	}

	public static HttpResponse getResponse(int id, String url, HashMap<String, String> headers, List<NameValuePair> params) throws Exception {
		return getResponse(id, url, headers, params, POST);
	}

	public static HttpResponse getResponse(int id, String url, HashMap<String, String> headers, List<NameValuePair> params, int type) throws Exception {
		// if (cookieStore == null) {
		// loadCookie();
		// }
		if (!isNetworkAvailable()) {
			throw new Exception("网络异常，请确认是否联网");
		}

		switch (type) {
		case POST:
			return getResponseByPost(id, url, headers, params != null ? new UrlEncodedFormEntity(params, HTTP.UTF_8) : null);
		case GET:
			return getResponseByGet(id, url, headers, params != null ? params : null);
		}
		return null;
	}

	public static HttpResponse getResponse(int id, String url, HashMap<String, String> headers, HttpEntity mpEntity) throws Exception {
		// if (cookieStore == null) {
		// loadCookie();
		// }
		return getResponseByPost(id, url, headers, mpEntity);
	}

	public static HttpResponse getResponseByGet(int id, String url, HashMap<String, String> headers, List<NameValuePair> params) throws Exception {
		// if (cookieStore != null) {
		// httpClient.setCookieStore(loadCookie());
		// }

		initHttpClient();

		StringBuilder builder = new StringBuilder(url);
		if (params != null) {
			builder.append("?");

			for (NameValuePair nameValuePair : params) {
				builder.append(nameValuePair.getName());
				builder.append("=");
				builder.append(nameValuePair.getValue());
				builder.append("&");
			}
		}

		HttpGet request = new HttpGet(builder.toString());
		requests.put(id, request);

		if (headers != null) {
			addHeaders(headers, request);
		}

		HttpResponse response = null;
		try {
			response = httpClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("arieljin", "Error Response: " + e.getMessage());
			throw new Exception("网络异常");
		} finally {
			requests.remove(id);
		}

		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			// saveCookie(httpClient.getCookieStore());
		} else {
			Log.i("arieljin", "Error Response: " + response.getStatusLine().toString() + ": " + url);
		}

		return response;
	}

	public static HttpResponse getResponseByPost(int id, String url, HashMap<String, String> headers, HttpEntity entity) throws Exception {
		// if (cookieStore != null) {
		// httpClient.setCookieStore(loadCookie());
		// }

		initHttpClient();

		HttpPost request = new HttpPost(url);
		requests.put(id, request);

		if (entity != null) {
			request.setEntity(entity);
		}

		if (headers != null) {
			addHeaders(headers, request);
		}

		ConnectivityManager connectivityManager = (ConnectivityManager) AbsApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

		HttpResponse response = null;
		try {
			if (activeNetInfo != null && activeNetInfo.getExtraInfo() != null && activeNetInfo.getExtraInfo().equals("cmwap")) {
				HttpHost proxy = new HttpHost("10.0.0.172", 80, "http");
				HttpHost target = new HttpHost(url.split("/")[2], 80, "http");
				httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

				response = httpClient.execute(target, request);
			} else {
				response = httpClient.execute(request);
			}
		} catch (Exception e) {
			if (!request.isAborted()) {
				e.printStackTrace();
				Log.i("arieljin", "Error Response: " + url + ":" + e.getMessage());
				throw new Exception("网络异常");
			}
		} finally {
			requests.remove(id);
		}

		if (response == null) {
			return null;
		}

		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			// saveCookie(httpClient.getCookieStore());
		} else {
			// String s = EntityUtils.toString(response.getEntity(), "UTF-8");
			// Log.e("json", s);
			Log.i("arieljin --" + url, "    --- Error Response: " + response.getStatusLine().toString());
			throw new Exception("网络异常:" + response.getStatusLine().getStatusCode());
		}

		return response;
	}

	private static void addHeaders(HashMap<String, String> headers, HttpRequestBase request) {
		for (String key : headers.keySet()) {
//			if(key.equals("token")){
//				String tokenValue = headers.get("token");
//				if(!TextUtils.isEmpty(tokenValue)){
//					headers.remove(tokenValue);
//					request.addHeader("Authorization", "Bearer " + tokenValue);
//				}
//			}else {
				request.addHeader(key, headers.get(key));
//			}
		}
	}

	public static HttpResponse sendRequest(String url, List<NameValuePair> params) {
		try {
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpPost request = new HttpPost(url);
			request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			return httpClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unused")
	private String getStringFromGZIP(HttpResponse response) {
		String string = null;
		InputStream is = null;
		BufferedInputStream bis = null;
		InputStreamReader reader = null;
		try {
			is = response.getEntity().getContent();
			bis = new BufferedInputStream(is);
			bis.mark(2);

			byte[] header = new byte[2];
			int result = bis.read(header);

			bis.reset();

			int headerData = getShort(header);

			if (result != -1 && headerData == 0x1f8b) {
				is = new GZIPInputStream(bis);
			} else {
				is = bis;
			}

			reader = new InputStreamReader(is, "UTF-8");
			char[] data = new char[100];
			int readSize;
			StringBuilder sb = new StringBuilder();
			while ((readSize = reader.read(data)) > 0) {
				sb.append(data, 0, readSize);
			}
			string = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return string;
	}

	private int getShort(byte[] data) {
		return (int) ((data[0] << 8 | data[1] & 0xFF));
	}

	public static void cancel(int id) {
		if (requests != null) {
			final HttpRequestBase request = requests.get(id);
			if (request != null) {
				requests.remove(id);

				new Thread() {

					@Override
					public void run() {
						request.abort();
					}
				}.start();
			}
		}
	}
}