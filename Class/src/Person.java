public class Person {
    private int ID;
    private String FirstName;
    private String LastName;
    private String Firstname;
    private String Lastname;
    private String Email;
    private String PhoneNumber;
    private String Address;

    public Person(Integer ID, String Firstname, String Lastname, String Email, String PhoneNumber, String Address) {
            this.ID = ID;
            this.Firstname = Firstname;
            this.Lastname = Lastname;
            this.Email = Email;
            this.PhoneNumber = PhoneNumber;
            this.Address = Address;
        }

    public int getID() { return ID; }
    public void setID(int ID) { this.ID = ID; }

    public String getFirstname() { return Firstname; }
    public void setFirstname(String firstname) { this.Firstname = firstname; }

    public String getLastname() {
        return Lastname;
    }
    public void setLastname(String lastname) { this.Lastname = lastname; }

    public String getEmail() {
        return Email;
    }
    public void setEmail(String email) { this.Email = email; }

    public String getPhoneNumber() {
        return PhoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) { this.PhoneNumber = phoneNumber; }

    public String getAddress() {
        return Address;
    }
    public void setAddress(String address) { this.Address = address; }

    @Override
    public String toString(){
        return "ID: " + ID + ", Firstname: " + Firstname + ", Lastname: " + Lastname + ", Email: " + Email  + ", PhoneNumber: " + PhoneNumber + ", Address: " + Address;
    }
}

