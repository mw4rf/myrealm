package models.admin;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.Type;

import play.data.validation.MaxSize;
import play.db.jpa.Model;

@Entity
public class AdminMessage extends Model {

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @MaxSize(10000)
    public String message = "";

    public Date dateStart = new Date();
    public Date dateEnd = new Date();

    public AdminMessage(String message, Date start, Date end) {
        super();
        if (start == null)
            start = new Date();
        if (end == null)
            end = new Date();
        this.message = message;
        this.dateStart = start;
        this.dateEnd = end;
        this.save();
    }

    public static AdminMessage getLastMessage() throws Exception {
        return AdminMessage.find("select a from AdminMessage a where now() between a.dateStart and a.dateEnd order by a.id desc", null).first();
    }

    public boolean isActive() {
        Date now = new Date();
        if (now.after(dateStart) && now.before(dateEnd))
            return true;
        else
            return false;
    }

    @Override
    public String toString() {
        return this.message;
    }

}
