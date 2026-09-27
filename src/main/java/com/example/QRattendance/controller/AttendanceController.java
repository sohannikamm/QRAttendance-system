package com.example.QRattendance.controller;

import com.example.QRattendance.model.Attendance;
import com.example.QRattendance.model.Employee;
import com.example.QRattendance.repository.AttendanceRepository;
import com.example.QRattendance.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping("/scan")
    public ScanResponse scan(@RequestBody ScanRequest request) {
        Employee employee = employeeRepository.findByQrCode(request.getQrCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        List<Attendance> history = attendanceRepository.findByEmployeeId(employee.getId());
        Attendance lastRecord = history.stream()
                .max(Comparator.comparing(Attendance::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);

        String type = "CHECK_IN";
        if (lastRecord != null && "CHECK_IN".equalsIgnoreCase(lastRecord.getType())) {
            type = "CHECK_OUT";
        }

        Attendance attendance = new Attendance();
        attendance.setEmployeeId(employee.getId());
        attendance.setType(type);
        attendance.setTimestamp(LocalDateTime.now());

        Attendance saved = attendanceRepository.save(attendance);
        return new ScanResponse(saved, employee.getName());
    }

    @GetMapping
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    public static class ScanRequest {
        private String qrCode;

        public String getQrCode() {
            return qrCode;
        }

        public void setQrCode(String qrCode) {
            this.qrCode = qrCode;
        }
    }

    public static class ScanResponse {
        private Long id;
        private Long employeeId;
        private LocalDateTime timestamp;
        private String type;
        private String employeeName;

        public ScanResponse(Attendance attendance, String employeeName) {
            this.id = attendance.getId();
            this.employeeId = attendance.getEmployeeId();
            this.timestamp = attendance.getTimestamp();
            this.type = attendance.getType();
            this.employeeName = employeeName;
        }

        public Long getId() {
            return id;
        }

        public Long getEmployeeId() {
            return employeeId;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public String getType() {
            return type;
        }

        public String getEmployeeName() {
            return employeeName;
        }
    }
}