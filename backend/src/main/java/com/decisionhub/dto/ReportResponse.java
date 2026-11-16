package com.decisionhub.dto;

import java.time.LocalDateTime;

public class ReportResponse {

    private Long reportId;
    private String targetType;
    private Long targetId;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
    private String reportedByEmail;

    public ReportResponse() {}

    public ReportResponse(Long reportId, String targetType, Long targetId,
                          String reason, String status, LocalDateTime createdAt,
                          String reportedByEmail) {
        this.reportId = reportId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
        this.reportedByEmail = reportedByEmail;
    }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getReportedByEmail() { return reportedByEmail; }
    public void setReportedByEmail(String reportedByEmail) { this.reportedByEmail = reportedByEmail; }
}
