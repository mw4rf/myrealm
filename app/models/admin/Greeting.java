package models.admin;

import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.Type;

import play.data.validation.MaxSize;
import play.db.jpa.Model;

/**
 * This class represents the ADMIN messages shown on the profile front page.
 * @author mw4rf
 */
@Entity
public class Greeting extends Model {
	
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @MaxSize(10000)
    public String message = "";
    
    public Boolean enable = false;
    
    public Greeting(String message, Boolean enable) {
    	this.message = message;
    	this.enable = enable;
    }
    
    @Override
    public String toString() {
    	return this.message;
    }
    	
}
