package bank;

/**
 * Created by Kevin on 4/30/20
 */

public class Customers {
    private String CFirstName;
    private String CLastName;
    private String CEmailAdd;
    private String CPhoneNo;
    private String UEmail;

    public String getUEmail() {
        return UEmail;
    }

    public void setUEmail(String UEmail) {
        this.UEmail = UEmail;
    }

    public String getCFirstName() {
        return CFirstName;
    }

    public void setCFirstName(String CFirstName) {
        this.CFirstName = CFirstName;
    }

    public String getCLastName() {
        return CLastName;
    }

    public void setCLastName(String CLastName) {
        this.CLastName = CLastName;
    }

    public String getCEmailAdd() {
        return CEmailAdd;
    }

    public void setCEmailAdd(String CEmailAdd) {
        this.CEmailAdd = CEmailAdd;
    }

    public String getCPhoneNo() {
        return CPhoneNo;
    }

    public void setCPhoneNo(String CPhoneNo) {
        this.CPhoneNo = CPhoneNo;
    }
}
