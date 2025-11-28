package org.example;


import org.example.view.BaristaView;
import org.example.view.CamareroView;

import javax.swing.*;

public class Main extends JFrame {
    private JTabbedPane tabbedPane;

    public Main() {
        setTitle("Cafeteria Examen - Sistema de Pedidos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Crear pestañas
        tabbedPane = new JTabbedPane();

        // Vista camarero
        CamareroView camareroView = new CamareroView();
        tabbedPane.addTab("👨‍💼 Camarero", camareroView);

        // Vista barista
        BaristaView baristaView = new BaristaView();
        tabbedPane.addTab("👨‍🍳 Barista", baristaView);

        add(tabbedPane);
    }

    public static void main(String[] args) {
        // ✅ SOLUCIÓN SIMPLE - Eliminar el LookAndFeel problemático
        try {
            // Esto funciona en la mayoría de sistemas
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("No se pudo cargar el LookAndFeel, usando el por defecto");
            // No hagas nada, usa el look and feel por defecto
        }

        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}