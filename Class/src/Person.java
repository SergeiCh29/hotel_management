abstract public class Person {
    private final int id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;

    public Person(int id, String firstName, String lastName, String email, String phone, String address) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
            this.address = address;
        }

    public int getId() { return id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName(){ return firstName + " " + lastName; }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        if (email != null && email.contains("@") && email.contains(".")) {
            this.email = email;
        } else {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }


    @Override
    public String toString(){
        return "id: " + id + ", FirstName: " + firstName + ", Lastname: " + lastName + ", Email: " + email + ", Phone: " + phone + ", Address: " + address;
    }
}

