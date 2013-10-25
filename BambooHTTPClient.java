
/*

Copyright (c) 2011, Bamboo HR LLC
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of Bamboo HR nor the names of its contributors may be used
  to endorse or promote products derived from this software without specific
  prior written permission.


THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

import java.io.*;
import javax.net.ssl.*;
import java.net.URL;
import java.util.*;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import sun.misc.BASE64Encoder;

public class BambooHTTPClient {
	private String basicAuthUsername="";
	private String basicAuthPassword;
	private TrustManager[] trustManager;
   	private	SSLContext sslContext = null;

	public static String slurp (InputStream in) throws IOException {
    		StringBuffer out = new StringBuffer();
    		byte[] b = new byte[4096];
    		for (int n; (n = in.read(b)) != -1;) {
    		    out.append(new String(b, 0, n));
    		}
		return out.toString();
	}

	public BambooHTTPClient() {
		trustManager = new TrustManager[] {new TrustEverythingTrustManager()};

    		// Let us create the factory where we can set some parameters for the connection
    		try {
    		    sslContext = SSLContext.getInstance("SSL");
    		    sslContext.init(null, trustManager, new java.security.SecureRandom());
    		} catch (NoSuchAlgorithmException e) {
    		    // do nothing
    		}catch (KeyManagementException e) {
    		    // do nothing
    		}

	}



	public void setBasicAuth(String username, String password) {
		basicAuthUsername=username;
		basicAuthPassword=password;
	}

	public BambooHTTPResponse  sendRequest(BambooHTTPRequest req) {
		BambooHTTPResponse ret=new BambooHTTPResponse();
    		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		try {
			URL url=new URL(req.url);
			HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
			connection.setHostnameVerifier(new VerifyEverythingHostnameVerifier());


			Set<String> headerKeys=req.headers.keySet();
			for(String key: headerKeys) {
				connection.setRequestProperty(key, req.headers.get( key ) );
			}

			if(!basicAuthUsername.equals("")) {
    				BASE64Encoder enc = new sun.misc.BASE64Encoder();
      				String userpassword = basicAuthUsername+":"+basicAuthPassword;
      				String encodedAuthorization = enc.encode( userpassword.getBytes() );
      				connection.setRequestProperty("Authorization", "Basic "+ encodedAuthorization);
			}

       
			if(req.content.length()>0) {
				connection.setDoOutput(true);
				OutputStream out=connection.getOutputStream();
				out.write( req.content.getBytes("utf-8") );
			}

			connection.connect();
			ret.statusCode=connection.getResponseCode();
			Map<String, List<String>> fields=connection.getHeaderFields();
			for (String header : fields.keySet()) {
				ret.headers.put(header, connection.getHeaderField(header));
			}
			ret.content="";
			try {
				ret.content=slurp(connection.getInputStream() );
			}
			catch(java.io.IOException e) {
			}
		}
		catch(java.net.MalformedURLException e) {
			ret.statusCode=0;
			ret.content="Connection error";
		}
		catch(java.io.IOException e) {
			ret.statusCode=0;
			ret.content="Connection error";
		}
		return ret;
	}


}

class TrustEverythingTrustManager implements X509TrustManager {
       	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
       	    return null;
       	}

      	public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {   }
       	public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {   }
}

class VerifyEverythingHostnameVerifier implements HostnameVerifier {
	public boolean verify(String string, SSLSession sslSession) {
       	    return true;
       	}
}	

