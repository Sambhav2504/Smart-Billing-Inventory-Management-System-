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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

@Service
public class PdfServiceImpl implements PdfService {
    private static final Logger logger = LoggerFactory.getLogger(PdfServiceImpl.class);

    public PdfServiceImpl() {
        // No dependencies needed
    }

    @Override
    public byte[] generateBillPdf(Bill bill, Locale locale) {
        if (bill == null) {
            throw new IllegalArgumentException("Bill cannot be null");
        }

        logger.info("Generating PDF for bill: {}", bill.getBillId());
        logger.debug("Bill total amount: {}", bill.getTotalAmount());
        logger.debug("Bill items count: {}", bill.getItems() != null ? bill.getItems().size() : 0);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            PdfFont font = PdfFontFactory.createFont("Helvetica");
            PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");

            // Header
            addHeader(document, boldFont, font);

            // Bill details
            addBillDetails(document, bill, boldFont, font);

            // Items table - FIXED: Use prices from bill items
            addItemsTable(document, bill, boldFont, font);

            // Total
            addTotal(document, bill, boldFont);

            document.close();
            logger.info("PDF generated successfully for bill: {}", bill.getBillId());
            return outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("Failed to generate PDF for bill {}: {}", bill.getBillId(), e.getMessage());
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addHeader(Document document, PdfFont boldFont, PdfFont font) {
        document.add(new Paragraph("SmartRetail")
                .setFont(boldFont).setFontSize(24).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Official Receipt")
                .setFont(font).setFontSize(14).setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));
    }

    private void addBillDetails(Document document, Bill bill, PdfFont boldFont, PdfFont font) {
        Table detailsTable = new Table(new float[]{1, 2});
        detailsTable.setWidth(UnitValue.createPercentValue(50));

        detailsTable.addCell(createCell(boldFont, "Bill ID:", false));
        detailsTable.addCell(createCell(font, bill.getBillId(), false));

        if (bill.getCustomer() != null) {
            detailsTable.addCell(createCell(boldFont, "Customer:", false));
            detailsTable.addCell(createCell(font,
                    bill.getCustomer().getName() != null ? bill.getCustomer().getName() : "N/A", false));

            detailsTable.addCell(createCell(boldFont, "Mobile:", false));
            detailsTable.addCell(createCell(font,
                    bill.getCustomer().getMobile() != null ? bill.getCustomer().getMobile() : "N/A", false));
        }

        document.add(detailsTable.setMarginBottom(20));
    }

    private void addItemsTable(Document document, Bill bill, PdfFont boldFont, PdfFont font) {
        Table itemsTable = new Table(new float[]{3, 1, 1, 1});
        itemsTable.setWidth(UnitValue.createPercentValue(100));

        // Headers
        itemsTable.addHeaderCell(createCell(boldFont, "Product", true));
        itemsTable.addHeaderCell(createCell(boldFont, "Qty", true));
        itemsTable.addHeaderCell(createCell(boldFont, "Price", true));
        itemsTable.addHeaderCell(createCell(boldFont, "Subtotal", true));

        // Items - FIXED: Use prices from bill items directly
        if (bill.getItems() != null) {
            for (Bill.BillItem item : bill.getItems()) {
                // Use product name from bill item
                String productName = item.getProductName() != null ?
                        item.getProductName() : "Product " + item.getProductId();

                // Use price from the bill item (not from database)
                double itemPrice = item.getPrice();
                double subtotal = item.getQty() * itemPrice;

                logger.debug("Item: {} - Qty: {} - Price: {} - Subtotal: {}",
                        productName, item.getQty(), itemPrice, subtotal);

                itemsTable.addCell(createCell(font, productName, false));
                itemsTable.addCell(createCell(font, String.valueOf(item.getQty()), false));
                itemsTable.addCell(createCell(font, String.format("₹%.2f", itemPrice), false));
                itemsTable.addCell(createCell(font, String.format("₹%.2f", subtotal), false));
            }
        }
        document.add(itemsTable);
    }

    private void addTotal(Document document, Bill bill, PdfFont boldFont) {
        document.add(new Paragraph("Total: ₹" + String.format("%.2f", bill.getTotalAmount()))
                .setFont(boldFont).setFontSize(12)
                .setTextAlignment(TextAlignment.RIGHT).setMarginTop(10));
    }

    private Cell createCell(PdfFont font, String text, boolean isHeader) {
        Cell cell = new Cell().setFont(font).add(new Paragraph(text));
        if (isHeader) {
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        } else {
            cell.setBorder(Border.NO_BORDER);
        }
        return cell;
    }
}