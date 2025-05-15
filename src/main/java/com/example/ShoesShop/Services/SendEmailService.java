package com.example.ShoesShop.Services;

import com.example.ShoesShop.Entity.Order;
import com.example.ShoesShop.Entity.OrderDetail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Locale;


@Service
public class SendEmailService {
    private final JavaMailSender mailSender;

    public SendEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmationEmail(Order order) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Load email template from resources/templates
        String templatePath = "templates/order-confirmation.html";
        ClassPathResource resource = new ClassPathResource(templatePath);
        String htmlTemplate = new String(Files.readAllBytes(Paths.get(resource.getURI())));

        // Format currency for VND
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        currencyFormat.setMinimumFractionDigits(0);

        // Generate order details table rows
        StringBuilder orderDetails = new StringBuilder();
        for (OrderDetail item : order.getOrderDetails()) {
            String productDescription = item.getProductVariant().getProduct().getName() + " (" + item.getProductVariant().getName() + ")";
            String unitPrice = currencyFormat.format(item.getUnitPrice()) + " VNĐ";
            String totalPrice = currencyFormat.format(item.getTotalPrice()) + " VNĐ";
            orderDetails.append(String.format(
                    "<tr>" +
                            "<td style=\"color: #555555; font-size: 14px; border-bottom: 1px solid #dddddd;\">%s</td>" +
                            "<td style=\"text-align: right; color: #555555; font-size: 14px; border-bottom: 1px solid #dddddd;\">%d</td>" +
                            "<td style=\"text-align: right; color: #555555; font-size: 14px; border-bottom: 1px solid #dddddd;\">%s</td>" +
                            "<td style=\"text-align: right; color: #555555; font-size: 14px; border-bottom: 1px solid #dddddd;\">%s</td>" +
                            "</tr>",
                    productDescription, item.getQuantity(), unitPrice, totalPrice
            ));
        }

        // Format total amount
        String totalAmount = currencyFormat.format(order.getTotalPrice()) + " VNĐ";

        // Format shipping address
        String shippingAddress = order.getShippingAddress().getAddress();

        // Replace placeholders in the template
        String emailContent = htmlTemplate
                .replace("{{customerName}}", order.getUser().getFullName())
                .replace("{{orderId}}", String.valueOf(order.getId()))
                .replace("{{orderDetails}}", orderDetails.toString())
                .replace("{{totalAmount}}", totalAmount)
                .replace("{{paymentMethod}}", order.getPayment().getPaymentMethod())
                .replace("{{shippingAddress}}", shippingAddress)
                .replace("{{phone}}", order.getShippingAddress().getPhone());

        // Set email properties
        helper.setTo(order.getUser().getUsername());
        helper.setSubject("Xác nhận đơn hàng #" + order.getId() + " từ MonkeyShoes");
        helper.setText(emailContent, true);

        // Send email
        mailSender.send(message);
    }
}
