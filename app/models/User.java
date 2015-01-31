package models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;

import com.avaje.ebean.annotation.EnumMapping;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
public class User extends Model {

	@Id
	public Long id;
	public String mobileNumber;
	public String userName;
	public String password;
	public String email;
	public String countryCode;
	public String verificationCode;
	public String status;
	
	public static Finder<Long,User> find = new Finder<>(Long.class,User.class);
	
	public static User getUserByUserNameAndPassword(String username,String password) throws NoSuchAlgorithmException {
		return find.where().eq("userName", username).eq("password",User.md5Encryption(password)).findUnique();
	}
	
	public static User getUserByUserNameAndPassword(String username) throws NoSuchAlgorithmException {
		return find.where().eq("userName", username).findUnique();
	}

	public static User getUserByMobileNumber(String mobileNumber, String countryCode) {
		return find.where().eq("mobileNumber", mobileNumber).eq("countryCode", countryCode).findUnique();
	}
	
	public static User getUserByMobileNumber(String mobileNumber) {
		return find.where().eq("mobileNumber", mobileNumber).findUnique();
	}
	
	public static String md5Encryption(String password) throws NoSuchAlgorithmException { 
		 MessageDigest md = MessageDigest.getInstance("MD5");
	        md.update(password.getBytes());
	 
	        byte byteData[] = md.digest();
	 
	        //convert the byte to hex format
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < byteData.length; i++) {
	         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	        }
	 
	        
	        return sb.toString();
	}
	
	
}
