package com.example.demo;

import javafx.scene.paint.Color;

public enum CellState {
    EMPTY(Color.web("#0F172A"), Color.web("#1E293B"), Color.web("#FFFDF5")),
    VIRUS(Color.web("#FF2A6D"), Color.web("#FF85A1"), Color.web("#E62429")),
    TROJAN(Color.web("#A855F7"), Color.web("#E9D5FF"), Color.web("#7E22CE")),
    RANSOMWARE(Color.web("#F97316"), Color.web("#FDE047"), Color.web("#EA580C")),
    WORM(Color.web("#84CC16"), Color.web("#D9F99D"), Color.web("#16A34A")),
    FIREWALL(Color.web("#05D5FA"), Color.web("#BAE6FD"), Color.web("#1D4ED8")),
    DAMAGED_FIREWALL(Color.web("#F59E0B"), Color.web("#FDE68A"), Color.web("#FACC15")),
    TURRET(Color.web("#10B981"), Color.web("#A7F3D0"), Color.web("#059669")),
    DATA_CORE(Color.web("#38BDF8"), Color.web("#E0F2FE"), Color.web("#0284C7")),
    PLASMA_MINE(Color.web("#D946EF"), Color.web("#F5D0FE"), Color.web("#C026D3")),
    PORTAL_A(Color.web("#0EA5E9"), Color.web("#E0F2FE"), Color.web("#2563EB")),
    PORTAL_B(Color.web("#F97316"), Color.web("#FFEDD5"), Color.web("#DC2626")),
    HONEYPOT(Color.web("#F59E0B"), Color.web("#FEF08A"), Color.web("#D97706")),
    ANTIVIRUS(Color.web("#00FF9D"), Color.web("#A7F3D0"), Color.web("#059669"));

    private final Color baseColor;
    private final Color highlightColor;
    private final Color comicColor;

    CellState(Color baseColor, Color highlightColor, Color comicColor) {
        this.baseColor = baseColor;
        this.highlightColor = highlightColor;
        this.comicColor = comicColor;
    }

    public Color getBaseColor() {
        return baseColor;
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public Color getComicColor() {
        return comicColor;
    }

    public boolean isMalware() {
        return this == VIRUS || this == TROJAN || this == RANSOMWARE || this == WORM;
    }

    public boolean isBlockingWall() {
        return this == FIREWALL || this == TURRET || this == HONEYPOT || this == ANTIVIRUS;
    }
}