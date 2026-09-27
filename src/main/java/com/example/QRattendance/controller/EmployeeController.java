package com.example.QRattendance.controller;

import com.example.QRattendance.model.Employee;
import com.example.QRattendance.repository.EmployeeRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @GetMapping("/add-employee")
    public String addEmployeePage() {
        return "add-employee";
    }
    @GetMapping("/employees")
public String employeesPage() {
    return "employees";
}

@GetMapping("/scan")
public String scanPage() {
    return "scan";
}

@GetMapping("/attendance-report")
public String attendanceReportPage() {
    return "attendance-report";
}
    

    @ResponseBody
    @PostMapping("/api/employees")
    public Employee addEmployee(@RequestBody Employee employee) {
        String qrCode;
        do {
            qrCode = UUID.randomUUID().toString();
        } while (employeeRepository.findByQrCode(qrCode).isPresent());

        employee.setQrCode(qrCode);
        return employeeRepository.save(employee);
    }

    @ResponseBody
    @GetMapping("/api/employees")
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @ResponseBody
    @GetMapping(value = "/api/employees/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getEmployeeQrCode(@PathVariable Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        try {
            byte[] png = generateQrCodePng(employee.getQrCode(), 300, 300);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(png);
        } catch (WriterException | IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate QR code");
        }
    }

    private byte[] generateQrCodePng(String content, int width, int height)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }
}