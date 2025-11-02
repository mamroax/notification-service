package com.example.notification.dto;

public class UserOperationMessage {
    private String operation; // "CREATED" or "DELETED"
    private String email;

    public UserOperationMessage() {}

    public UserOperationMessage(String operation, String email) {
        this.operation = operation;
        this.email = email;
    }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "UserOperationMessage{operation='" + operation + "', email='" + email + "'}";
    }
}
