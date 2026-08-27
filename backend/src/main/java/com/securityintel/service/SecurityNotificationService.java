package com.securityintel.service;

import com.securityintel.model.RemediationItem;
import com.securityintel.model.ScanExecution;
import com.securityintel.model.SecurityFinding;
import com.securityintel.model.Service;
import com.securityintel.repository.RemediationItemRepository;
import com.securityintel.repository.ScanExecutionRepository;
import com.securityintel.repository.ScanReportRepository;
import com.securityintel.repository.SecurityFindingRepository;
import com.securityintel.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.regex.Pattern;
import jakarta.mail.MessagingException;

@Component
public class SecurityNotificationService {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final ObjectProvider<JavaMailSender> mailSender;
    private final ServiceRepository serviceRepository;
    private final SecurityFindingRepository findingRepository;
    private final RemediationItemRepository remediationRepository;
    private final ScanExecutionRepository scanRepository;
    private final ScanReportRepository reportRepository;
    @Value("${mail.enabled:false}") private boolean enabled;
    @Value("${mail.from:security-platform@localhost}") private String from;

    public SecurityNotificationService(ObjectProvider<JavaMailSender> mailSender, ServiceRepository serviceRepository,
            SecurityFindingRepository findingRepository, RemediationItemRepository remediationRepository,
            ScanExecutionRepository scanRepository, ScanReportRepository reportRepository) {
        this.mailSender = mailSender; this.serviceRepository = serviceRepository;
        this.findingRepository = findingRepository; this.remediationRepository = remediationRepository;
        this.scanRepository = scanRepository; this.reportRepository = reportRepository;
    }

    public boolean isEnabled() { return enabled; }

    public void sendServiceReport(String serviceName) {
        Service service = serviceRepository.findByServiceName(serviceName)
            .orElseThrow(() -> new com.securityintel.exception.ResourceNotFoundException("Service not found: " + serviceName));
        requireRecipient(service.getOwner());
        List<SecurityFinding> findings = findingRepository.findByServiceName(serviceName);
        List<RemediationItem> remediation = remediationRepository.findByServiceName(serviceName);
        List<ScanExecution> scans = scanRepository.findByServiceNameOrderByCreatedAtDesc(serviceName);
        List<com.securityintel.model.ScanReport> sourceReports = reportRepository.findByServiceName(serviceName);
        StringBuilder body = new StringBuilder("Security report for ").append(serviceName).append("\n\n")
            .append("Owner: ").append(service.getOwner()).append("\n")
            .append("Environment: ").append(service.getEnvironment()).append("\n")
            .append("Deduplicated findings: ").append(findings.size()).append("\n")
            .append("Remediation items: ").append(remediation.size()).append("\n")
            .append("Recorded scans: ").append(scans.size()).append("\n\nImmediate action:\n");
        findings.stream().filter(f -> f.getStatus() == com.securityintel.model.Status.OPEN)
            .filter(f -> f.getPriority() == com.securityintel.model.Priority.P0 || f.getPriority() == com.securityintel.model.Priority.P1)
            .limit(10).forEach(f -> body.append("- ").append(f.getPriority()).append(" ").append(f.getCve()).append(" ").append(f.getTitle()).append("\n"));
        sendWithAttachment(service.getOwner(), "Security report: " + serviceName, body.toString(),
            buildCsv(serviceName, findings, remediation, scans, sourceReports), serviceName + "-security-report.csv");
    }

    public int sendDailyBrief() {
        int sent = 0;
        for (Service service : serviceRepository.findAll()) {
            if (service.getOwner() != null && EMAIL.matcher(service.getOwner()).matches()) {
                List<SecurityFinding> findings = findingRepository.findByServiceName(service.getServiceName());
                long p0 = findings.stream().filter(f -> f.getStatus() == com.securityintel.model.Status.OPEN && f.getPriority() == com.securityintel.model.Priority.P0).count();
                long p1 = findings.stream().filter(f -> f.getStatus() == com.securityintel.model.Status.OPEN && f.getPriority() == com.securityintel.model.Priority.P1).count();
                send(service.getOwner(), "Today's security brief: " + service.getServiceName(),
                    "Service: " + service.getServiceName() + "\nEnvironment: " + service.getEnvironment() +
                    "\nOpen P0: " + p0 + "\nOpen P1: " + p1 + "\n\nReview the Security Intelligence Platform for the consolidated remediation queue.");
                sent++;
            }
        }
        return sent;
    }

    private void send(String recipient, String subject, String body) {
        if (!enabled) throw new IllegalStateException("Email delivery is not configured. Set MAIL_ENABLED=true and spring.mail.* settings.");
        JavaMailSender sender = requireMailSender();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from); message.setTo(recipient); message.setSubject(subject); message.setText(body);
        sender.send(message);
    }

    private void sendWithAttachment(String recipient, String subject, String body, String csv, String fileName) {
        if (!enabled) throw new IllegalStateException("Email delivery is not configured. Set MAIL_ENABLED=true and spring.mail.* settings.");
        try {
            JavaMailSender sender = requireMailSender();
            var message = sender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true);
            helper.setFrom(from); helper.setTo(recipient); helper.setSubject(subject); helper.setText(body);
            helper.addAttachment(fileName, new org.springframework.core.io.ByteArrayResource(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            sender.send(message);
        } catch (MessagingException exception) {
            throw new IllegalStateException("Unable to build service report email", exception);
        }
    }

    private String buildCsv(String serviceName, List<SecurityFinding> findings, List<RemediationItem> remediation, List<ScanExecution> scans, List<com.securityintel.model.ScanReport> sourceReports) {
        StringBuilder csv = new StringBuilder("Record Type,Service,Owner/Team,Environment,Priority,Severity,CVE,Title,Package,Installed Version,Fixed Version,Scanner,Scan Type,Status,Detection State,Risk Score,Scan Date\n");
        findings.forEach(f -> csv.append(row("Finding", serviceName, "", f.getEnvironment(), f.getPriority(), f.getSeverity(), f.getCve(), f.getTitle(), f.getPackageName(), f.getInstalledVersion(), f.getFixedVersion(), f.getTool(), f.getScanType(), f.getStatus(), f.getDetectionState(), f.getRiskScore(), f.getLastDetectedAt(), f.getSourceFindings())));
        remediation.forEach(r -> csv.append(row("Remediation", serviceName, r.getTeamName(), "", r.getPriority(), "", r.getFindingId(), r.getRecommendedAction(), "", "", "", "", "", r.getRemediationStatus(), "", r.getRiskScore(), r.getUpdatedAt())));
        scans.forEach(s -> csv.append(row("Scan", serviceName, "", s.getEnvironment(), "", "", "", s.getNewFindings() + " new / " + s.getResolvedFindings() + " resolved", "", "", "", s.getTool(), s.getScanType(), s.getStatus(), "", s.getTotalUniqueFindings(), s.getCompletedAt() != null ? s.getCompletedAt() : s.getReceivedAt())));
        sourceReports.forEach(r -> csv.append(row("Source Report", serviceName, "", r.getEnvironment(), "", "", r.getId(), r.getUploadedFileName(), "", "", "", r.getTool(), r.getScanType(), r.getStatus(), "", r.getTotalFindings(), r.getCreatedAt())));
        return csv.toString();
    }

    private String row(Object... values) {
        return java.util.Arrays.stream(values).map(value -> "\"" + String.valueOf(value == null ? "" : value).replace("\"", "\"\"") + "\"").collect(java.util.stream.Collectors.joining(",")) + "\n";
    }

    private void requireRecipient(String recipient) {
        if (recipient == null || !EMAIL.matcher(recipient).matches()) throw new IllegalArgumentException("Service owner email is missing or invalid");
    }

    private JavaMailSender requireMailSender() {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) throw new IllegalStateException("SMTP is not configured. Set spring.mail.host and MAIL_ENABLED=true.");
        return sender;
    }
}