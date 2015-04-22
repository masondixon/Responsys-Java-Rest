package rest;


import org.apache.commons.codec.binary.Base64;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.Certificate;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

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
	
	/**
	 * LoginWithCertificate - an alternative way to login, avoiding password auth, but requiring certificates and copious setup steps
	 * @param user_name
	 * @param login_url
	 * @param client_challenge
	 * @param path_to_responsys_cert
	 * @param path_to_keystore
	 * @param keystore_alias
	 * @param keystore_pass
	 * @return boolean
	 * @sets authToken and endPoint for subsequent API calls
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public boolean loginWithCertificate( String user_name, String login_url, String client_challenge, String path_to_responsys_cert, String path_to_keystore, String keystore_alias, String keystore_pass ) throws IOException, URISyntaxException
	{
		boolean is_logged_in = false;
		
        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        
        byte[] clientChallengeStringBytes = client_challenge.getBytes();
        String encodedClientChallenge = Base64.encodeBase64URLSafeString( clientChallengeStringBytes );
        
        HttpUriRequest authenticate_client = RequestBuilder.post()
                .setUri(new URI( login_url + rest_login ))
                .addParameter("user_name", user_name)
                .addParameter("auth_type", "server")
                .addParameter("client_challenge", encodedClientChallenge )
                .build();
        
        CloseableHttpResponse response = httpclient.execute( authenticate_client );

        try 
        {
            if( response.getStatusLine().getStatusCode() == 200 )
            {
	        	HttpEntity entity = response.getEntity();
	            JSONParser parser = new JSONParser();
	            JSONObject json = (JSONObject) parser.parse( EntityUtils.toString( entity ) );
	            
	            if ( json.containsKey("authToken") && json.containsKey("serverChallenge") && json.containsKey("clientChallenge") )
	            {
	            	authToken = json.get("authToken").toString();
	            	
	            	byte[] serverChallengeBytes = Base64.decodeBase64( json.get("serverChallenge").toString().getBytes() );
	            	byte[] clientChallengeBytes = Base64.decodeBase64( json.get("clientChallenge").toString().getBytes() );
	            	
	            	 File certFile = new File( path_to_responsys_cert );
	                 if (!certFile.exists()) {
	                     System.out.println("Server certificate doesn't exist in that location");
	                     return false;
	                 }

	                 try 
	                 {
	                     CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
	                     X509Certificate serverCertificate = (X509Certificate) certFactory.generateCertificate(new FileInputStream(certFile));

	                     Cipher decryptCipher = Cipher.getInstance("RSA");
	                     decryptCipher.init(Cipher.DECRYPT_MODE, serverCertificate.getPublicKey());
	                     byte[] decryptedClientChallenge = decryptCipher.doFinal( clientChallengeBytes );

	                     // Compare the clientChallenge with decryptedClientChallenge.
	                     boolean serverValidated = Arrays.equals( clientChallengeStringBytes, decryptedClientChallenge );

	                     if (serverValidated) 
	                     {
	                         System.out.println("Server validation is success ... proceeding further to login to webservices");
	                         
	                         
	                         KeyStore keyStore = KeyStore.getInstance("JKS");
	                         keyStore.load( new FileInputStream(new File( path_to_keystore )), keystore_pass.toCharArray() );
	                                      
	                         Key key = keyStore.getKey( keystore_alias, keystore_pass.toCharArray());
	                         Certificate certificate = keyStore.getCertificate( keystore_alias );
	                         
	                         PublicKey publicKey = certificate.getPublicKey();
	                         
	                         KeyPair keyPair = new KeyPair(publicKey, (PrivateKey) key);
	                         PrivateKey privateKey = keyPair.getPrivate();
	                                      
	                         Cipher encryptCipher = Cipher.getInstance("RSA");
	                         encryptCipher.init(Cipher.ENCRYPT_MODE, privateKey);
	                         byte[] encryptedServerChallengeBytes = encryptCipher.doFinal( serverChallengeBytes );
	                                      
	                         String encoded_server_challenge = Base64.encodeBase64URLSafeString( encryptedServerChallengeBytes );
	                         
	                         HttpUriRequest login_with_certificate = RequestBuilder.post()
	                                 .setUri(new URI( login_url + rest_login ))
	                                 .addParameter("user_name", user_name)
	                                 .addParameter("auth_type", "client")
	                                 .addParameter("server_challenge", encoded_server_challenge )
	                                 .addHeader("Authorization", authToken)
	                                 .build();
	                         
	                         CloseableHttpResponse login_cert_response = httpclient.execute( login_with_certificate );
	                         
	  
	        
             	        	HttpEntity login_entity = login_cert_response.getEntity();
             	            JSONParser login_parser = new JSONParser();
             	            JSONObject login_json = (JSONObject) login_parser.parse( EntityUtils.toString( login_entity ) );
             	            
             	           if( login_cert_response.getStatusLine().getStatusCode() == 200 )
             	           {
             	        	  // Now we can get the semi-permanenet auth token from login call and set it!
	             	            if ( login_json.containsKey("authToken") && login_json.containsKey("endPoint") )
	             	            {
	             	            	System.out.println(" *** LoginWithCertificate Complete *** ");
	             	            	authToken = login_json.get("authToken").toString();
	             	            	endPoint  = login_json.get("endPoint").toString();
	             	            	return true;
	             	            }
             	           }
             	           else
             	           {
	         	        	   System.out.println(" *** Something went wrong *** ");
	         	        	   System.out.println(" http code : " + login_cert_response.getStatusLine().getStatusCode() );
	         	        	   System.out.println( login_json.toString() );
	         	        	   return false;
             	           }
	
	                     }
	                     else 
	                     {
	                         System.out.println("Server validation failed");
	                         return false;
	                     }
	                 }
	                 catch (CertificateException ex) {
	                     System.out.println("CertificateException : " + ex.getMessage());
	                     return false;
	                 }
	                 catch (NoSuchAlgorithmException ex) {
	                     System.out.println("NoSuchAlgorithmException : " + ex.getMessage());
	                     return false;
	                 }
	                 catch (NoSuchPaddingException ex) {
	                     System.out.println("NoSuchPaddingException : " + ex.getMessage());
	                     return false;
	                 }
	                 catch (InvalidKeyException ex) {
	                     System.out.println("InvalidKeyException : " + ex.getMessage());
	                     return false;
	                 }
	                 catch (BadPaddingException ex) {
	                     System.out.println("BadPaddingException : " + ex.getMessage());
	                     return false;
	                 }
	                 catch (IllegalBlockSizeException ex) {
	                     System.out.println("IllegalBlockSizeException : " + ex.getMessage());
	                     return false;
	                 }
	                 catch (FileNotFoundException ex) {
	                     System.out.println("FileNotFoundException : " + ex.getMessage());
	                     return false;
	                 } catch (KeyStoreException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
						return false;
					} catch (UnrecoverableKeyException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
						return false;
					}
	            	
	            }
	        }
	        else
	        {
	        	System.out.println("Error code : " + response.getStatusLine().getStatusCode() + " - " + response.getStatusLine().getReasonPhrase() );
	        	return false;
	        }
            
        } catch (ParseException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
			return false;
		} catch (org.json.simple.parser.ParseException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
			return false;
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
	        
	        
	        System.out.println( rest_request.getAllHeaders().toString());
	        System.out.println( rest_request.getRequestLine().toString() );
	        
	        
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
				System.out.println( "IO Exception: " + io.getMessage() );
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