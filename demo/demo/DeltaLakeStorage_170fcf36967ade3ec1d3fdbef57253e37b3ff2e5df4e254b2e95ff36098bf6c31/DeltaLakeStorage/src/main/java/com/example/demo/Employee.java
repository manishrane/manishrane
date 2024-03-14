package com.example.demo;

import io.delta.standalone.data.RowRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.avro.generic.GenericRecord;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    private Integer id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private Date birthDate;
    private String ssn;
    private Integer salary;

    public static Employee map(RowRecord data) {
        Employee employee = new Employee();
        employee.setId(data.getInt("id"));
        employee.setFirstName(data.getString("firstName"));
        employee.setMiddleName(data.getString("middleName"));
        employee.setLastName(data.getString("lastName"));
        employee.setGender(data.getString("gender"));
        employee.setBirthDate(data.getDate("birthDate"));
        employee.setSsn(data.getString("ssn"));
        employee.setSalary(data.getInt("salary"));
        return employee;
    }

    public static Employee map(GenericRecord data, Map<String, String> partitionValues) {
        Employee employee = new Employee();
        employee.setId(Integer.valueOf(data.get("id").toString()));
        employee.setFirstName(data.get("firstName").toString());
        employee.setMiddleName(data.get("middleName").toString());
        employee.setLastName(data.get("lastName").toString());
        employee.setGender(data.get("gender").toString());
//        employee.setBirthDate(partitionValues.get(("birthDate")));
        employee.setSsn(data.get("ssn").toString());
        employee.setSalary(Integer.valueOf(data.get("salary").toString()));
        return employee;
    }
}
