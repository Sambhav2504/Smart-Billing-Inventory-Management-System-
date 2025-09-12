package com.smartretail.backend.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.smartretail.backend.models.Bill;
import com.smartretail.backend.models.Product;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;

@Service
public class BillPdfService {
    private final BillService billService;
    private final ProductService productService;

    public BillPdfService(BillService billService, ProductService productService) {
        this.billService = billService;
        this.productService = productService;
    }

    public byte[] generateBillPdf(String billId) throws Exception {
        // Fetch bill
        Bill bill = billService.getBillById(billId);
        if (bill == null) {
            throw new RuntimeException("Bill not found: " + billId);
        }

        // Create PDF in memory
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Set font for premium look
        PdfFont font = PdfFontFactory.createFont("Helvetica");
        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");

        // Add header
        document.add(new Paragraph("SmartRetail")
                .setFont(boldFont)
                .setFontSize(24)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));
        document.add(new Paragraph("Official Receipt")
                .setFont(font)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // Add bill details
        Table detailsTable = new Table(new float[]{1, 2});
        detailsTable.setWidth(UnitValue.createPercentValue(50));
        detailsTable.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setFont(boldFont)
                .setFontSize(10)
                .add(new Paragraph("Bill ID:")));
        detailsTable.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setFont(font)
                .setFontSize(10)
                .add(new Paragraph(bill.getBillId())));
        detailsTable.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setFont(boldFont)
                .setFontSize(10)
                .add(new Paragraph("Customer:")));
        detailsTable.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setFont(font)
                .setFontSize(10)
                .add(new Paragraph(bill.getCustomer().getName())));
        detailsTable.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setFont(boldFont)
                .setFontSize(10)
                .add(new Paragraph("Mobile:")));
        detailsTable.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setFont(font)
                .setFontSize(10)
                .add(new Paragraph(bill.getCustomer().getMobile())));
        detailsTable.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setFont(boldFont)
                .setFontSize(10)
                .add(new Paragraph("Date:")));
        detailsTable.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setFont(font)
                .setFontSize(10)
                .add(new Paragraph(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.getCreatedAt()))));
        document.add(detailsTable.setMarginBottom(20));

        // Add items table
        Table itemsTable = new Table(new float[]{3, 1, 1, 1});
        itemsTable.setWidth(UnitValue.createPercentValue(100));
        itemsTable.addHeaderCell(new Cell().setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setFont(boldFont)
                .setFontSize(10)
                .add(new Paragraph("Product")));
        itemsTable.addHeaderCell(new Cell().setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setFont(boldFont)
                .setFontSize(10)
                .add(new Paragraph("Qty")));
        itemsTable.addHeaderCell(new Cell().setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setFont(boldFont)
                .setFontSize(10)
                .add(new Paragraph("Price")));
        itemsTable.addHeaderCell(new Cell().setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setFont(boldFont)
                .setFontSize(10)
                .add(new Paragraph("Subtotal")));
        for (Bill.BillItem item : bill.getItems()) {
            Product product = productService.getProductById(item.getProductId());
            itemsTable.addCell(new Cell().setFont(font)
                    .setFontSize(10)
                    .add(new Paragraph(product.getName())));
            itemsTable.addCell(new Cell().setFont(font)
                    .setFontSize(10)
                    .add(new Paragraph(String.valueOf(item.getQty()))));
            itemsTable.addCell(new Cell().setFont(font)
                    .setFontSize(10)
                    .add(new Paragraph(String.format("₹%.2f", item.getPrice()))));
            itemsTable.addCell(new Cell().setFont(font)
                    .setFontSize(10)
                    .add(new Paragraph(String.format("₹%.2f", item.getQty() * item.getPrice()))));
        }
        document.add(itemsTable);

        // Add total
        document.add(new Paragraph("Total: ₹" + String.format("%.2f", bill.getTotal()))
                .setFont(boldFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(10));

        // Add footer
        document.add(new Paragraph("Thank you for shopping with SmartRetail!")
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));

        // Close document
        document.close();

        return baos.toByteArray();
    }
}