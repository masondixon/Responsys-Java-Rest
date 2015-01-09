package rest;


import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;

public class restClient 
{
	public static String rest_login           = "/rest/api/v1/auth/token";
	public static String campaign_service_url = "/rest/api/v1/campaigns/";
	public static String list_service_url     = "/rest/api/v1/lists/";
	public static String event_service_url    = "/rest/api/v1/events/";
	
	public String authToken = null;
	public String endPoint  = null;

	public boolean generateAuthToken( String user_name, String password, String login_url ) throws IOException, URISyntaxException
	{
		boolean is_logged_in = false;
		
        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        
        HttpUriRequest login = RequestBuilder.post()
                .setUri(new URI( login_url + rest_login ))
                .addParameter("user_name", user_name)
                .addParameter("password",  password)
                .addParameter("auth_type", "password")
                .build();
        
        CloseableHttpResponse response = httpclient.execute(login);
        
        try 
        {
            if( response.getStatusLine().getStatusCode() == 200 )
            {
	        	HttpEntity entity = response.getEntity();
	            JSONParser parser = new JSONParser();
	            JSONObject json = (JSONObject) parser.parse( EntityUtils.toString( entity ) );
	            if ( json.containsKey("authToken") && json.containsKey("endPoint") )
	            {
	            	authToken = json.get("authToken").toString();
	            	endPoint  = json.get("endPoint").toString();
	            	is_logged_in = true;
	            }
	        }
	        else
	        {
	        	System.out.println("Error code : " + response.getStatusLine().getStatusCode() + " - " + response.getStatusLine().getReasonPhrase() );
	        }
            
        } catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            response.close();
        }
        
        return is_logged_in;
	}
	
	public Object executeRequest( String request_json, String service_url ) throws URISyntaxException, ClientProtocolException, IOException
	{
		CloseableHttpResponse response = null;
		Object service_response = null;
		
        try 
        {
	        BasicCookieStore cookieStore = new BasicCookieStore();
	        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
	        
	        StringEntity json_entity = new StringEntity( request_json.trim() );
	        
	        HttpUriRequest rest_request = RequestBuilder.post()
	                .setUri(new URI( service_url ))
	                .addHeader("Content-Type", "application/json;charset=UTF-8")
	                .addHeader("Authorization", authToken)
	                .setEntity( json_entity )
	                .build();
	        
	        
	        response = httpclient.execute( rest_request );
	        
	        if( response.getStatusLine().getStatusCode() == 200 && response != null )
	        {
	            HttpEntity entity = response.getEntity();
	            service_response = parseResponse(entity);  
	            //System.out.println("Request Response: " + service_response.toString() );   
	        }
	        else
	        {
	        	System.out.println("Error code : " + response.getStatusLine().getStatusCode() + " - " + response.getStatusLine().getReasonPhrase() );
	        }
            
        } catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally 
		{
			try
			{
				response.close();
			}
			catch( IOException io )
			{
				io.printStackTrace();
			}
        }
        
        return service_response;
	}
	
	
	private Object parseResponse( HttpEntity entity ) throws ParseException, org.json.simple.parser.ParseException, IOException
	{
		Object json = null;
		JSONParser parser = new JSONParser();
		json = parser.parse( EntityUtils.toString(entity) );
		//System.out.println(" Response Class Type : " + json.getClass() ); 
		
		return json;
	}
	
}