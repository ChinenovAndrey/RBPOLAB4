package org.example.model;

public enum RequestStatus {
    OPEN,       // открыта, принимает предложения
    ORDERED,    // выбрано предложение, создан заказ
    COMPLETED,  // заказ выполнен
    CANCELLED   // отменена
}