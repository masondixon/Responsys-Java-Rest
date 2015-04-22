import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;

import rest.restClient;

public class samples {

	static String interact2_login_url = "https://login2.responsys.net";
	static String interact5_login_url = "https://login5.responsys.net";
	
	static String api_user = "mason***";
	static String cert_user= "mason***";
	static String password = "*****";
	
	static String ResponsysClientCert  = "/Users/mdixon/Documents/certificatefun/ResponsysServerCertificate.cer";
	static String MasonsKeyStore       = "/Users/mdixon/Documents/certificatefun/mdixon/mason.keystore";
	static String MasonsKeyAlias       = "*****";
	static String MasonsKeyPass        = "*****";
	
	static restClient rest_instance;
	

	public samples() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub	

		samples run = new samples();
		//run.generateTokenAndEndPoint();
		run.loginWithCert();
		//run.run_merge_record();
		//run.run_merge_and_trigger();
		//run.run_trigger_custom_event();
	}

	/**
	 * Example of running a login call to generate token and service endpoint
	 * Tokens are persisted as http headers
	 * endpoint becomes the new service url for service requests, they are dynamic and may change with each login call
	 */
	public void generateTokenAndEndPoint()
	{
		rest_instance = new restClient();
		
		try 
		{
			boolean is_logged_in = rest_instance.generateAuthToken( api_user, password, interact5_login_url );
			System.out.println("auth token : " + rest_instance.authToken );
			System.out.println("end point : " + rest_instance.endPoint );
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Exmaple of loginWithCertificate
	 * @param user_name
	 * @param login_url
	 * @param client_challenge
	 * @param path_to_responsys_cert
	 * @param path_to_keystore
	 * @param keystore_alias
	 * @param keystore_pass
	 */
	public void loginWithCert(){
		rest_instance = new restClient();
		try {
				boolean is_logged_in = rest_instance.loginWithCertificate(cert_user, interact5_login_url, "sillyString", ResponsysClientCert, MasonsKeyStore, MasonsKeyAlias, MasonsKeyPass );
				if( is_logged_in )
			{
				System.out.println("auth token : " + rest_instance.authToken );
				System.out.println("end point : " + rest_instance.endPoint );
			}
			else
			{
				System.out.println( "*** Houston we have a problem ***");
				System.out.println( "*** LoginWithCert Failed ***");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Example of adding a record to a contact list
	 * JSON has been supplied as example
	 * 
	 */
	public void run_merge_record(){
		
		try {
			Object response = rest_instance.executeRequest( getJsonData_mergeRecord(), rest_instance.endPoint + rest_instance.list_service_url + "masonList1" );
			System.out.println("Request Response: " + response.toString() );
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Example of merging a recipient then triggering an email to that recipient in a single operation
	 * JSON has been supplied as example
	 */
	public void run_merge_and_trigger(){
		try {
			Object response = rest_instance.executeRequest( getJsonData_mergeTrigger() , rest_instance.endPoint + rest_instance.campaign_service_url + "masonCampaign1" + "/email" );
			System.out.println("Request Response: " + response.toString() );
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Example of firing a custom event through api
	 * Note that the recipient needs to be added to the contact list or audience list of the program first.
	 * JSON has been supplied as example
	 */
	public void run_trigger_custom_event(){
		try {
			Object response = rest_instance.executeRequest( getJsonData_triggerCustomEvent(), rest_instance.endPoint + rest_instance.event_service_url + "Welcome_Test5" );
			System.out.println("Request Response: " + response.toString() );
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * For brevity i am skipping json building methods to focus API methods
	 * This data will only work on my test account since it refers to string literal values of objects that exist on my account.
	 */
	private static String getJsonData_mergeTrigger() 
	{	
		String json = "{\"mergeRule\": {\"htmlValue\": \"H\",\"matchColumnName1\": \"EMAIL_ADDRESS_\",\"matchColumnName2\": null,\"matchColumnName3\": null,\"optoutValue\": \"O\",\"insertOnNoMatch\": true,\"defaultPermissionStatus\": \"OPTIN\",\"rejectRecordIfChannelEmpty\": \"E\",\"optinValue\": \"I\",\"updateOnMatch\": \"REPLACE_ALL\",\"textValue\": \"T\",\"matchOperator\": \"NONE\"    },    \"recordData\": {\"records\": [    {\"fieldValues\": [    \"kity.daly@oracle.com\",    \"san bruno\"]    },    {\"fieldValues\": [    \"mdixon@gmail.com\",    \"martinez\"]    }],\"fieldNames\": [    \"EMAIL_ADDRESS_\",    \"CITY_\"]    },    \"triggerData\": [{    \"optionalData\": [{    \"name\": \"FIRST_NAME\",    \"value\": \"Mason\"}    ]},{    \"optionalData\": [{    \"name\": \"ORDER_NUMBER\",    \"value\": \"1234567\"}    ]},{    \"optionalData\": [{    \"name\": \"FIRST_NAME\",    \"value\": \"Mike\"}    ]},{    \"optionalData\": [{    \"name\": \"ORDER_NUMBER\",    \"value\": \"32432424\"}    ]}    ]}";
		return json;
	}
	
	private static String getJsonData_mergeRecord()
	{
		String json = "{\"list\": {\"folderName\": \"Mason\"}, \"recordData\": {\"records\": [{\"fieldValues\": [\"mdixon@email.com\", \"san bruno\"]}, {\"fieldValues\": [\"some@email.com\", \"san francisco\"]}], \"fieldNames\": [\"EMAIL_ADDRESS_\", \"CITY_\"]}, \"mergeRule\": {\"htmlValue\": \"H\", \"matchColumnName1\": \"EMAIL_ADDRESS_\", \"matchColumnName2\": null, \"matchColumnName3\": null, \"optoutValue\": \"O\", \"insertOnNoMatch\": true, \"defaultPermissionStatus\": \"OPTOUT\", \"rejectRecordIfChannelEmpty\": \"E\", \"optinValue\": \"I\", \"updateOnMatch\": \"REPLACE_ALL\", \"textValue\": \"T\", \"matchOperator\": \"NONE\"}}";
		return json;
	}
	
	private static String getJsonData_triggerCustomEvent()
	{
		String json = "{\"customEvent\" : {\"eventNumberDataMapping\" : null,\"eventDateDataMapping\" : null,\"eventStringDataMapping\" : null},\"recipientData\" : [{ \"recipient\" : {\"customerId\" : null,\"emailAddress\" : \"mdixon@oracle.com\",\"listName\" : { \"folderName\" : \"Mason\", \"objectName\" : \"masonList1\"},\"recipientId\" : null,\"mobileNumber\" : null,\"emailFormat\" : \"HTML_FORMAT\" }, \"optionalData\" : [{ \"name\" : \"CUSTOM1\", \"value\" : \"value1\"}]}]}";
		return json;
	}
	
}
