package com.example.demo.service;

import com.example.demo.entity.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void exportEmployeesToCSV(String filePath) {
        List<Employee> employees = employeeRepository.findAll();
        
        // Query the table dynamically to get column names
        String query = "SELECT * FROM Employee LIMIT 1";

        try (FileWriter writer = new FileWriter(filePath)) {

            // Get the column names dynamically
            jdbcTemplate.query(query, (ResultSet rs) -> {
                try {
                    // Fetch column names from metadata
                    int columnCount = rs.getMetaData().getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        writer.append(rs.getMetaData().getColumnName(i));
                        if (i < columnCount) {
                            writer.append(",");
                        }
                    }
                    writer.append("\n");  // Move to next line after headers
                } catch (SQLException | IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });

            // Write the data rows
            for (Employee employee : employees) {
                writer.append(employee.getId().toString())
                      .append(',')
                      .append(employee.getName())
                      .append(',')
                      .append(employee.getEmail())
                      .append('\n');
            }

            writer.flush();
            System.out.println("CSV file written successfully!");

        } catch (IOException e) {
            System.out.println("Error while writing CSV file: " + e.getMessage());
        }
    }
}
