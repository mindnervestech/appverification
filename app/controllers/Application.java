package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Random;
import java.util.UUID;

import models.User;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import viewmodel.ResponseVM;

public class Application extends Controller {
  
    public static Result index() {
        //return ok(index.render("Your new application is ready."));
		return ok();
    }
  
    public static Result register() {
    	try{
    		Form<RegisterForm> form = DynamicForm.form(RegisterForm.class).bindFromRequest();
    		RegisterForm rForm = form.get();
    		if(     rForm.userName.isEmpty()||
    				rForm.userName==null||
    				rForm.password.isEmpty()||
    	    		rForm.password==null||
    				rForm.mobileNumber.isEmpty()||
    				rForm.mobileNumber==null||
    				rForm.email==null||
    				rForm.email.isEmpty()||
    				rForm.countryCode==null||
    				rForm.countryCode.isEmpty()) {
    			return ok(Json.toJson(new ErrorResponse(Error.E202.getCode(), Error.E202.getMessage())));
    		} else {
    			try {
    				Long.parseLong(rForm.mobileNumber);
    				if(rForm.mobileNumber.length()<10 || rForm.mobileNumber.length()>10) {
    					return ok(Json.toJson(new ErrorResponse(Error.E206.getCode(), Error.E206.getMessage())));
    				} 
    			} catch(NumberFormatException e) {
    				return ok(Json.toJson(new ErrorResponse(Error.E206.getCode(), Error.E206.getMessage())));
    			}
    			if(User.getUserByMobileNumber(rForm.mobileNumber)!=null) {
    				return ok(Json.toJson(new ErrorResponse(Error.E210.getCode(), Error.E210.getMessage())));
    			} 
    			
    			try {
    				Long.parseLong(rForm.countryCode);
    			} catch(NumberFormatException e) {
    				return ok(Json.toJson(new ErrorResponse(Error.E203.getCode(), Error.E203.getMessage())));
    			}
    			
    			User userObj = new User();
    			userObj.mobileNumber = rForm.mobileNumber;
    			userObj.userName = rForm.userName;
    			userObj.password = User.md5Encryption(rForm.password);
    			userObj.email = rForm.email;
    			userObj.countryCode = rForm.countryCode;
    			userObj.verificationCode = getRandomCode();
    			userObj.status = "pending";
    			userObj.save();
    			
    			/*String action = "sendsms";
    			String user = "shabeebcool@gmail.com";
    			String password = "Shabeeb123!@#";
    			String from = "Test";
    			String to = "00" + userObj.countryCode + userObj.mobileNumber;
    			String text = URLEncoder.encode("Verification Code "+userObj.verificationCode,"UTF-8");
    			*/
    			
    			String toMobileNumber = "00" + userObj.countryCode + userObj.mobileNumber;
    			String text = URLEncoder.encode("Verification Code "+userObj.verificationCode,"UTF-8");
    			
    			/*URL url = new URL("http://www.smsglobal.com/http-api.php"+"?action="+action+"&user="+user+"&password="+password+"&from="+from+"&to="+to+"&text="+text);
    			HttpURLConnection con = (HttpURLConnection) url.openConnection();
    			con.setRequestMethod("GET");
    			int responseCode = con.getResponseCode();
    			String resp = con.getResponseMessage();
    			System.out.println(resp+responseCode);*/
    			
    			ResponseVM responseVM = new ResponseVM();
    			try {
    				sendNotification(toMobileNumber, text);
    				responseVM.code = "200";
        			responseVM.message = "Login Successful!";
    				return ok(Json.toJson(responseVM));
    			} catch (Exception e) {
    				return ok(Json.toJson(new ErrorResponse(Error.E204.getCode(), Error.E204.getMessage())));
    			}
    			
    			//return ok(Json.toJson(new ErrorResponse(Error.E204.getCode(), Error.E204.getMessage())));
    		}
    	} catch(Exception e) {
    		return ok(Json.toJson(new ErrorResponse("500",e.getMessage())));
    	}
    }
    
    public static class RegisterForm {
    	public String mobileNumber;
    	public String userName;
    	public String password;
    	public String email;
    	public String countryCode;
    }
    
    public static class ErrorResponse {
    	public String code;
    	public String message;
    	public ErrorResponse(String code,String message) {
    		this.code = code;
    		this.message = message;
    	}
    }
    
    public enum Error {
    	E201("201","Login Failed!"),
    	E202("202","Required Field Missing!"),
    	E200("200","Login Successful!"),
    	E203("203","Invalid Country Code"),
    	E204("204","User Registered Successfully!"),
    	E205("205","User is not verified yet"),
    	E206("206","Mobile Number is not Valid Number!"),
    	E207("207","Verification Code is Invalid!"),
    	E208("208","Username, Password does'nt matched with our database!"),
    	E209("209","User Validated Successfully!"),
    	E210("210","User Already Exist!"),
    	E211("211","User Does Not Exist");
    	Error(String code,String message) {
    		this.code = code;
    		this.message = message;
    	}
    	
    	private String code;
    	private String message;
		
    	public String getCode() {
			return code;
		}
		public String getMessage() {
			return message;
		}
    	
    }
    
    public static String getRandomCode() {
    	Random ran = new Random();
    	
    	int num=0;
    	for(int i=0;i<4;i++) {
    		num = (num*10)+ran.nextInt(10);
    	}
    	return num+"";
    }
    
    public static class ValidateForm {
    	public String userName;
    	public String password;
    	public String verificationCode;
    }
    
    public static Result validateUser() {
    	try {
    		ValidateForm form = DynamicForm.form(ValidateForm.class).bindFromRequest().get();
    		if(form.userName==null ||
    				form.userName.isEmpty() ||
    				form.password==null ||
    				form.password.isEmpty()||
    				form.verificationCode==null ||
    				form.verificationCode.isEmpty()) {
    			return ok(Json.toJson(new ErrorResponse(Error.E202.getCode(), Error.E202.getMessage())));
    		} else {
    			
    			User user = User.getUserByUserNameAndPassword(form.userName, form.password);
    			if(user == null) {
    				return ok(Json.toJson(new ErrorResponse(Error.E208.getCode(), Error.E208.getMessage())));
    			} else {
    				if(!user.verificationCode.equals(form.verificationCode)) {
    					return ok(Json.toJson(new ErrorResponse(Error.E207.getCode(), Error.E207.getMessage())));
    				} else {
    					user.status = "approved";
    					user.update();
    					return ok(Json.toJson(new ErrorResponse(Error.E209.getCode(), Error.E209.getMessage())));
    				}
    			}
    		}
    	} catch(Exception e) {
    		return ok(Json.toJson(new ErrorResponse("500",e.getMessage())));
    	}
    }
    
    public static class LoginForm {
    	public String userName;
    	public String password;
    }
    
    public static Result login() {
    	ResponseVM responseVM = new ResponseVM();
    	try {
    		Form<LoginForm> form = DynamicForm.form(LoginForm.class).bindFromRequest();
    		String username = form.data().get("userName");
    		String password = form.data().get("password");
    		if(username==null || username.isEmpty() || password==null || password.isEmpty()) {
    			return ok(Json.toJson(new ErrorResponse(Error.E202.getCode(), Error.E202.getMessage())));
    		} else {
    			
    			responseVM.code = "200";
    			responseVM.message = "Login Successful!";
    			User user = User.getUserByUserNameAndPassword(username, password);
    			if(user == null) {
    				return ok(Json.toJson(new ErrorResponse(Error.E201.getCode(),Error.E201.getMessage())));
    			}
    			if(user.status.equals("pending")) {
    				return ok(Json.toJson(new ErrorResponse(Error.E205.getCode(),Error.E205.getMessage())));
    			}
    			
    			return ok(Json.toJson(responseVM));
    		}
    	} catch(Exception e) {
    		return ok(Json.toJson(new ErrorResponse("500",e.getMessage())));
    	} 
    }
    
    public static void sendNotification(String mobileNumber, String textMessage) throws Exception {
    	final String URL = "https://secure.cm.nl/smssgateway/cm/gateway.ashx";
        try {
            final UUID productToken = UUID.fromString("af639359-e0aa-4409-ae62-2ad98faf3e92");
            final String xml = "<?xml version=\"1.0\"?><MESSAGES><AUTHENTICATION><PRODUCTTOKEN>" 
            		+ productToken + "</PRODUCTTOKEN></AUTHENTICATION> <MSG> <FROM>My company</FROM> "
            		+ "<TO>" + mobileNumber + "</TO><BODY>" 
            		+ textMessage + "</BODY> </MSG></MESSAGES>";
            
            final String response = doHttpPost(URL, xml);
        } catch (Exception e) {
            System.err.println(e); // Display the string.
            throw e;
        }
    }
    
    private static String doHttpPost(String urlString, String requestString) {
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(requestString);
            wr.flush();
            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            String response = "";
            while ((line = rd.readLine()) != null) {
                response += line;
            }
            wr.close();
            rd.close();

            return response;
        } catch (IOException ex) {
            System.err.println(ex); return ex.toString();
        }
    }
    
}
