package com.smartretail.backend.controller;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.service.BillPdfService;
import com.smartretail.backend.service.BillService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
public class BillController {
    private final BillService billService;
    private final BillPdfService billPdfService;

    public BillController(BillService billService, BillPdfService billPdfService) {
        this.billService = billService;
        this.billPdfService = billPdfService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CASHIER')")
    public ResponseEntity<Bill> createBill(@RequestBody Bill bill) {
        Bill createdBill = billService.createBill(bill);
        return new ResponseEntity<>(createdBill, HttpStatus.CREATED);
    }

    @GetMapping("/{billId}")
    @PreAuthorize("hasRole('CASHIER')")
    public ResponseEntity<Bill> getBillById(@PathVariable String billId) {
        Bill bill = billService.getBillById(billId);
        return new ResponseEntity<>(bill, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasRole('CASHIER')")
    public ResponseEntity<List<Bill>> getAllBills() {
        List<Bill> bills = billService.getAllBills();
        return new ResponseEntity<>(bills, HttpStatus.OK);
    }

    @GetMapping("/{billId}/pdf")
    @PreAuthorize("hasAnyRole('CASHIER', 'MANAGER')")
    public ResponseEntity<byte[]> getBillPdf(@PathVariable String billId) throws Exception {
        byte[] pdfBytes = billPdfService.generateBillPdf(billId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "bill_" + billId + ".pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}