public class Staff extends Person{
    private String Role;
    private double Salary;
    private String HireDate;

    public Staff(int ID, String Firstname, String Lastname, String Email, String PhoneNumber, String Address, String Role, double Salary) {
        super(ID, Firstname, Lastname, Email, PhoneNumber, Address);
        this.Role = Role;
        this.Salary = Salary;
        this.HireDate = HireDate;
    }

    public String getRole() { return Role; }
    public void setRole(String Role) { this.Role = Role; }

    public double getSalary() { return Salary; }
    public void setSalary(double Salary) { this.Salary = Salary; }

    public String getHireDate() { return HireDate; }
    public void setHireDate(String HireDate) { this.HireDate = HireDate; }

    @Override
    public String toString(){
        return super.toString() + ", Role: " + Role + ", Salary: " + Salary + ", HireDate: " + HireDate;
    }
}

