
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
import java.net.URLEncoder;
import java.util.*;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import sun.misc.BASE64Encoder;

public class BambooAPI {
	protected BambooHTTPClient httpHandler;
	protected String baseUrl="https://api.bamboohr.com/api/gateway.php/";


	BambooAPI(String companyDomain) {
		httpHandler=new BambooHTTPClient();
		baseUrl+=companyDomain;	
	}

	BambooAPI(String companyDomain, BambooHTTPClient http) {
		httpHandler=http;
		baseUrl+=companyDomain;
	}

	void setSecretKey(String key){
		httpHandler.setBasicAuth(key, "x");
    	}

	private static String implode(String glue, String[] arr) {
    		StringBuilder sb = new StringBuilder();
		if (arr.length > 0) {
			sb.append(arr[0]);
			for (int i=1; i<arr.length; i++) {
				sb.append(glue);
				sb.append(arr[i]);
    			}
		}
		return sb.toString();
	}

	private static String prepareKeyValues(HashMap<String,String> values) {
                StringBuilder xml=new StringBuilder();
		Set<String> keys=values.keySet();
		for(String str : keys) {
			xml.append("<field id=\"").append(str).append("\">").append(values.get(str)).append("</field>");
		}
                return xml.toString();
	}

	private static String encodeHTML(String s) {
    		StringBuffer out = new StringBuffer();
		for(int i=0; i<s.length(); i++) {           
			char c=s.charAt(i);
			switch(c) {
				case '"': out.append("&quot;"); break;
				case '<': out.append("&lt;");break;
				case '>': out.append("&gt;");break;
				default: out.append(c); break;
			}
		}
    		return out.toString();
	}

	BambooHTTPResponse getEmployee(int employeeId, String[] fields) {
		BambooHTTPRequest request=new BambooHTTPRequest();
		request.method="GET";
		request.url=baseUrl + "/v1/employees/" + employeeId + "/?fields=" + implode(",",fields);
		return httpHandler.sendRequest( request );
	}



	BambooHTTPResponse getReport(int reportId, String format) {
                BambooHTTPRequest request=new BambooHTTPRequest();
                request.method="GET";
                request.url=baseUrl+"/v1/reports/"+reportId+"/?format="+format;
                return httpHandler.sendRequest( request );
        }

 	BambooHTTPResponse updateEmployee(int employeeId, HashMap<String,String> fieldValues) {
                BambooHTTPRequest request=new BambooHTTPRequest();
                request.method="POST";
                request.url=baseUrl+"/v1/employees/"+employeeId;
                request.headers.put("Content-type","text/xml");

                String xml=String.format("<employee id=\"%d\">", employeeId);
                xml+=prepareKeyValues(fieldValues);
                xml+="</employee>";
                request.content=xml;
                return httpHandler.sendRequest( request );
        }

        BambooHTTPResponse addEmployee(HashMap<String,String> initialFieldValues) {
                BambooHTTPRequest request=new BambooHTTPRequest();
                request.method="POST";
                request.url=baseUrl+"/v1/employees/";
                request.headers.put("Content-type","text/xml");

                request.content="<employee>"+prepareKeyValues(initialFieldValues)+"</employee>";
                return httpHandler.sendRequest( request );
        }

        BambooHTTPResponse getCustomReport(String format, String[] fields) {
                BambooHTTPRequest request=new BambooHTTPRequest();
                request.method="POST";
                request.url=baseUrl+"/v1/reports/custom/?format="+format;
                request.headers.put("Content-type", "text/xml");

		StringBuilder sb=new StringBuilder();
                sb.append("<report><fields>");
                for(String field : fields) {
			sb.append( String.format("<field id=\"%s\" />", field ) );
                }
		sb.append("</fields></report>");
                request.content=sb.toString();
                return httpHandler.sendRequest( request );
        }

        BambooHTTPResponse getTable(int employeeId, String tableName) {
                BambooHTTPRequest request=new BambooHTTPRequest();
                request.method="GET";
		try {
                	request.url=baseUrl+"/v1/employees/"+employeeId+"/tables/"+URLEncoder.encode(tableName,"utf-8")+"/";
		}
		catch(UnsupportedEncodingException e) {
		}
                return httpHandler.sendRequest( request );
        }




	public static void main(String arguments[]) {

		BambooAPI client=new BambooAPI("" /* Your company subdomain here */);
		client.setSecretKey("" /* Your key here */ );
		BambooHTTPResponse resp=client.getEmployee(27, new String[] {"firstName","lastName"} );
		System.out.println( resp.statusCode );
		System.out.println( resp.content );

		resp=client.getReport(1869,"xml");
		System.out.println( resp.statusCode );
		System.out.println( resp.content );

		resp=client.getCustomReport("xml", new String[] { "firstName","lastName" } );
		System.out.println( resp.statusCode );
		System.out.println( resp.content );

		HashMap<String,String> map=new HashMap<String,String>();
		map.put("firstName","API");
		map.put("lastName","Employee");
		resp=client.addEmployee( map );
		System.out.println( resp.statusCode );
		System.out.println( resp.content );
	}

}

