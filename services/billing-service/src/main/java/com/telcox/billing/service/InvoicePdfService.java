package com.telcox.billing.service;

import com.telcox.billing.domain.Invoice;
import com.telcox.billing.domain.InvoiceItem;
import com.telcox.billing.repository.InvoiceItemRepository;
import com.telcox.billing.repository.InvoiceRepository;
import com.telcox.common.exception.BusinessException;
import com.telcox.common.exception.ErrorCode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;

/**
 * BILL-03 / FR-23: Fatura PDF'ini olusturur. Uretilen PDF bir dosya sistemine/
 * object storage'a yazilmiyor; on-demand olarak byte[] doner
 * (InvoiceController.downloadPdf uzerinden). Kalici saklama gerekiyorsa
 * (ör. S3/MinIO) ayri bir takip isi olarak eklenmeli.
 */
@Service
public class InvoicePdfService {

    private static final float MARGIN = 50f;
    private static final float LINE_HEIGHT = 16f;

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    public InvoicePdfService(InvoiceRepository invoiceRepository, InvoiceItemRepository invoiceItemRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
    }

    public byte[] generatePdf(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Invoice not found: " + invoiceId));
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoiceId);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            var titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            var bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - MARGIN;

                y = writeLine(content, titleFont, 16, MARGIN, y, "TelcoX - Fatura");
                y -= LINE_HEIGHT / 2;
                y = writeLine(content, bodyFont, 11, MARGIN, y, "Fatura No: " + invoice.getInvoiceNumber());
                y = writeLine(content, bodyFont, 11, MARGIN, y,
                        "Donem: " + invoice.getPeriodStart() + " - " + invoice.getPeriodEnd());
                y = writeLine(content, bodyFont, 11, MARGIN, y, "Durum: " + invoice.getStatus());
                y -= LINE_HEIGHT;

                y = writeLine(content, titleFont, 12, MARGIN, y, "Kalemler");
                y -= LINE_HEIGHT / 2;
                for (InvoiceItem item : items) {
                    String line = String.format("%-20s x%-8s %10s %s",
                            truncate(item.getDescription(), 20), item.getQuantity(), item.getUnitPrice(), invoice.getCurrency());
                    y = writeLine(content, bodyFont, 10, MARGIN, y, line);
                }

                y -= LINE_HEIGHT;
                y = writeLine(content, bodyFont, 11, MARGIN, y, "Ara toplam: " + invoice.getSubtotal() + " " + invoice.getCurrency());
                y = writeLine(content, bodyFont, 11, MARGIN, y, "Vergi: " + invoice.getTaxAmount() + " " + invoice.getCurrency());
                writeLine(content, titleFont, 12, MARGIN, y, "Genel toplam: " + invoice.getTotalAmount() + " " + invoice.getCurrency());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate invoice PDF for " + invoiceId, e);
        }
    }

    private float writeLine(PDPageContentStream content, PDType1Font font, float fontSize, float x, float y, String text) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
        return y - LINE_HEIGHT;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1) + "\u2026";
    }
}
