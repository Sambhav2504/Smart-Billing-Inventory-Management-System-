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
import java.util.Locale;

@Service
public class BillPdfService {
    private final BillService billService;
    private final ProductService productService;

    public BillPdfService(BillService billService, ProductService productService) {
        this.billService = billService;
        this.productService = productService;
    }

    // Optional: Keep this if you still want to support billId directly
    public byte[] generateBillPdf(String billId, Locale locale) {
        Bill bill = billService.getBillById(billId, locale);
        return generateBillPdf(bill, locale);
    }

    // 🔥 Main method (no checked exception now)
    public byte[] generateBillPdf(Bill bill, Locale locale) {
        if (bill == null) {
            throw new RuntimeException("Bill is null, cannot generate PDF");
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            PdfFont font = PdfFontFactory.createFont("Helvetica");
            PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");

            // Header
            document.add(new Paragraph("SmartRetail")
                    .setFont(boldFont).setFontSize(24).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Official Receipt")
                    .setFont(font).setFontSize(14).setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

            // Bill details
            Table detailsTable = new Table(new float[]{1, 2});
            detailsTable.setWidth(UnitValue.createPercentValue(50));
            detailsTable.addCell(new Cell().setBorder(Border.NO_BORDER).setFont(boldFont).add(new Paragraph("Bill ID:")));
            detailsTable.addCell(new Cell().setBorder(Border.NO_BORDER).setFont(font).add(new Paragraph(bill.getBillId())));

            if (bill.getCustomer() != null) {
                detailsTable.addCell(new Cell().setBorder(Border.NO_BORDER).setFont(boldFont).add(new Paragraph("Customer:")));
                detailsTable.addCell(new Cell().setBorder(Border.NO_BORDER).setFont(font).add(new Paragraph(
                        bill.getCustomer().getName() != null ? bill.getCustomer().getName() : "N/A")));

                detailsTable.addCell(new Cell().setBorder(Border.NO_BORDER).setFont(boldFont).add(new Paragraph("Mobile:")));
                detailsTable.addCell(new Cell().setBorder(Border.NO_BORDER).setFont(font).add(new Paragraph(
                        bill.getCustomer().getMobile() != null ? bill.getCustomer().getMobile() : "N/A")));
            }

            document.add(detailsTable.setMarginBottom(20));

            // Items
            Table itemsTable = new Table(new float[]{3, 1, 1, 1});
            itemsTable.setWidth(UnitValue.createPercentValue(100));
            itemsTable.addHeaderCell(new Cell().setBackgroundColor(ColorConstants.LIGHT_GRAY).setFont(boldFont).add(new Paragraph("Product")));
            itemsTable.addHeaderCell(new Cell().setBackgroundColor(ColorConstants.LIGHT_GRAY).setFont(boldFont).add(new Paragraph("Qty")));
            itemsTable.addHeaderCell(new Cell().setBackgroundColor(ColorConstants.LIGHT_GRAY).setFont(boldFont).add(new Paragraph("Price")));
            itemsTable.addHeaderCell(new Cell().setBackgroundColor(ColorConstants.LIGHT_GRAY).setFont(boldFont).add(new Paragraph("Subtotal")));

            if (bill.getItems() != null) {
                for (Bill.BillItem item : bill.getItems()) {
                    Product product = productService.getProductById(item.getProductId(), locale);
                    itemsTable.addCell(new Cell().setFont(font).add(new Paragraph(product != null ? product.getName() : "Unknown Product")));
                    itemsTable.addCell(new Cell().setFont(font).add(new Paragraph(String.valueOf(item.getQty()))));
                    itemsTable.addCell(new Cell().setFont(font).add(new Paragraph(String.format("₹%.2f", item.getPrice()))));
                    itemsTable.addCell(new Cell().setFont(font).add(new Paragraph(String.format("₹%.2f", item.getQty() * item.getPrice()))));
                }
            }
            document.add(itemsTable);

            // Total
            document.add(new Paragraph("Total: ₹" + String.format("%.2f", bill.getTotalAmount()))
                    .setFont(boldFont).setFontSize(12).setTextAlignment(TextAlignment.RIGHT).setMarginTop(10));

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}

