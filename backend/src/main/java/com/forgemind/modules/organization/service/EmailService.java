package com.forgemind.modules.organization.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Stub email service for the invitation workflow.
 *
 * <p>In production, replace this with an SMTP / SendGrid / AWS SES implementation.
 * The invitation link is logged at INFO level so developers can test the flow locally.
 */
@Slf4j
@Service
public class EmailService {

  /**
   * Sends an organization invitation email.
   *
   * @param toEmail       recipient email address
   * @param orgName       name of the organization
   * @param rawToken      the plaintext invitation token (embedded in the accept URL)
   */
  public void sendInvitation(String toEmail, String orgName, String rawToken) {
    String acceptUrl = "http://localhost:5173/invitations/accept?token=" + rawToken;
    log.info("[EMAIL STUB] To: {} | Subject: You're invited to join {} on ForgeMind | URL: {}",
        toEmail, orgName, acceptUrl);
    // TODO: integrate real email provider (SMTP / SendGrid / SES)
  }

  /**
   * Sends a generic notification email.
   *
   * @param toEmail  recipient address
   * @param subject  email subject
   * @param body     plain-text body
   */
  public void sendNotification(String toEmail, String subject, String body) {
    log.info("[EMAIL STUB] To: {} | Subject: {} | Body: {}", toEmail, subject, body);
  }
}
