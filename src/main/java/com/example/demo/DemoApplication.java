package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DevOps Dashboard Application
 *
 * <p>Provides comprehensive visibility into GitHub Actions workflows and deployments.
 *
 * <p>Features: - Workflow monitoring with real-time status - Deployment tracking across
 * environments - Build metrics and analytics - Commit diff analysis - Deployment history and
 * comparison
 *
 * <p>Configuration is done via application.properties
 *
 * <p>The application uses multiple CommandLineRunner components: - WorkflowDashboardRunner:
 * Displays workflow status and metrics - DeploymentTrackingRunner: Shows deployment tracking across
 * environments - DashboardCompletionRunner: Prints completion message
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
