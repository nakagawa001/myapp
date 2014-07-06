/**
 * COPYRIGHT LICENSE: This information contains sample code provided in source code form. You may copy, modify, and distribute
 * these sample programs in any form without payment to IBMﾂｮ for the purposes of developing, using, marketing or distributing
 * application programs conforming to the application programming interface for the operating platform for which the sample code is written.
 * Notwithstanding anything to the contrary, IBM PROVIDES THE SAMPLE SOURCE CODE ON AN "AS IS" BASIS AND IBM DISCLAIMS ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, ANY IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE, AND ANY WARRANTY OR CONDITION OF NON-INFRINGEMENT. IBM SHALL NOT BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR OPERATION OF THE SAMPLE SOURCE CODE.
 * IBM HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS OR MODIFICATIONS TO THE SAMPLE SOURCE CODE.
 */
package com.myapp;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class HelloNative extends Activity {

	EditText editText = null;

	public static final int REQCODE_OAUTH = 0;
	public static final String CLIENT_ID = "client_id";
	public static final String CLIENT_SECRET = "client_secret";
	public static final String SCOPE = "scope";
	public static final String ACCESS_TOKEN = "access_token";

	protected String clientId;
	protected String clientSecret;
	protected String scope;

	protected ViewSwitcher vs;
	protected TextView tvProg;
	protected WebView wv;
	
	public String accessToken = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String name = getIntent().getStringExtra("nameParam");

		LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		setContentView(linearLayout);

		TextView textView1 = new TextView(this);
		textView1.setText("Name received from JavaScript :: " + name);

		TextView textView2 = new TextView(this);
		textView2.setText("Enter the phone number");

		editText = new EditText(this);
		editText.setText("1234567890");

		Button submitButton = new Button(this);
		submitButton.setText("Return to the Web App");
		submitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

//				String phoneNumber = editText.getText().toString();
				Intent phoneNumberInfo = new Intent();
				phoneNumberInfo.putExtra("phoneNumber", accessToken);
				Log.v("onClick", accessToken);
				setResult(RESULT_OK, phoneNumberInfo);
				Log.v("onClick", "+++++++++++++++++");
				finish();
			}
		});

		linearLayout.addView(textView1);
		linearLayout.addView(textView2);
		linearLayout.addView(editText);
		linearLayout.addView(submitButton);

	    // Intentからパラメータ取得
	    clientId = "350276189015-6os5esfj3o7msccsnvqs7mis2861nsoj.apps.googleusercontent.com";
	    //clientSecret = intent.getStringExtra(CLIENT_SECRET);
//	    scope = "https://www.googleapis.com/auth/userinfo.profile";
	    scope = "https://www.google.com/m8/feeds";

	    // 各種Viewを取得
	    vs = new ViewSwitcher(this);
	    tvProg = new TextView(this);
	    wv = new WebView(this);

	    // WebView設定
	    wv.setWebViewClient(new WebViewClient() { // これをしないとアドレスバーなどが出る


	      @Override
	      public void onPageFinished(WebView view, String url) { // ページ読み込み完了時

	        // ページタイトルからコードを取得
	        String title = view.getTitle();
	        String code = getCode(title);

	        // コード取得成功ページ以外
	        if (code == null) {
	          Log.v("onPageFinished", "コード取得成功ページ以外 url=" + url);
	          if (!(vs.getCurrentView() instanceof WebView)) { // WebViewが表示されてなかったら
	            vs.showNext(); // Web認証画面表示
	          }
	        }

	        // コード取得成功
	        else {
	          Log.v("onPageFinished", "コード取得成功 code=" + code);
	          vs.showPrevious(); // プログレス画面に戻る
	          new TaskGetAccessToken().execute(code); // アクセストークン取得開始
	        }

	      }
	    });

	    // 認証ページURL
	    String url = "https://accounts.google.com/o/oauth2/auth" // ここに投げることになってる
	        + "?client_id=" + clientId // アプリケーション登録してもらった
	        + "&response_type=code" // InstalledAppだとこの値で固定
	        + "&redirect_uri=urn:ietf:wg:oauth:2.0:oob" // タイトルにcodeを表示する場合は固定
	        + "&scope=" + URLEncoder.encode(scope) // 許可を得たいサービス
	    
	        + "&access_type=offline"
	        + "&approval_prompt=auto";

	    
	    Log.v("onCreate", "clientId=" + clientId + " clientSecret=" + clientSecret + " scope=" + scope + " url=" + url);
        Log.v("onCreate", "url+++++++++++++++" + url);
	    // 認証ページロード開始
	    wv.getSettings().setJavaScriptEnabled(true);
	    wv.loadUrl(url);
	    linearLayout.addView(wv);
	}

	  /**
	   * 認証成功ページのタイトルは「Success code=XXXXXXX」という風になっているので、
	   * このタイトルから「code=」以下の部分を切り出してOAuth2アクセスコードとして返す
	   *
	   * @param title
	   *          ページタイトル
	   * @return OAuth2アクセスコード
	   */
	  protected String getCode(String title) {
	    String code = null;
	    String codeKey = "code=";
	    int idx = title.indexOf(codeKey);
	    if (idx != -1) { // 認証成功ページだった
	      code = title.substring(idx + codeKey.length()); // 「code」を切り出し
	    }
	    return code;
	  }


	  // アクセストークン取得タスク
	  protected class TaskGetAccessToken extends AsyncTask<String, Void, String> {


	    @Override
	    protected void onPreExecute() {
	      Log.v("onPostExecute", "アクセストークン取得開始");
	      tvProg.setText("アクセストークンを取得中...");
	    }


	    @Override
	    protected String doInBackground(String... codes) {
	      String token = null;
	      DefaultHttpClient client = new DefaultHttpClient();
	      try {

	        // パラメータ構築
	        ArrayList<NameValuePair> formParams = new ArrayList<NameValuePair>();
	        formParams.add(new BasicNameValuePair("code", codes[0]));
	        formParams.add(new BasicNameValuePair("client_id", clientId));
	        formParams.add(new BasicNameValuePair("client_secret", clientSecret));
	        formParams.add(new BasicNameValuePair("redirect_uri", "urn:ietf:wg:oauth:2.0:oob"));
	        formParams.add(new BasicNameValuePair("grant_type", "authorization_code"));

	        // トークンの取得はPOSTで行うことになっている
	        HttpPost httpPost = new HttpPost("https://accounts.google.com/o/oauth2/token");
	        httpPost.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8")); // パラメータセット
	        HttpResponse res = client.execute(httpPost);
	        HttpEntity entity = res.getEntity();
	        String result = EntityUtils.toString(entity);

	        // JSONObject取得
	        JSONObject json = new JSONObject(result);
	        if (json.has("access_token")) {
	          token = json.getString("access_token");
	        } else {
	          if (json.has("error")) {
	            String error = json.getString("error");
	            Log.d("getAccessToken", error);
	          }
	        }

	      } catch (ClientProtocolException e) {
	        e.printStackTrace();
	      } catch (IOException e) {
	        e.printStackTrace();
	      } catch (JSONException e) {
	        e.printStackTrace();
	      } finally {
	        client.getConnectionManager().shutdown();
	      }
	      return token;
	    }


	    @Override
	    protected void onPostExecute(String token) {
	      if (token == null) {
	        Log.v("onPostExecute", "アクセストークン取得失敗");
	      } else {
	        Log.v("onPostExecute", "アクセストークン取得成功 token=" + token);
	        accessToken = token;
	        Intent intent = new Intent();
//	        intent.putExtra(ACCESS_TOKEN, token);
//	        setResult(Activity.RESULT_OK, intent);
	      }
	      //finish();
	    }

	  } // END class TaskGetAccessToken

}
