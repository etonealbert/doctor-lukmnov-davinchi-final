package ar.edu.doctorlukmanov.vista;

import ar.edu.doctorlukmanov.estrategia.TipoReporte;
import ar.edu.doctorlukmanov.vista.cliente.PanelClientes;
import ar.edu.doctorlukmanov.vista.componentes.Dialogos;
import ar.edu.doctorlukmanov.vista.gato.PanelGatos;
import ar.edu.doctorlukmanov.vista.reporte.PanelReportes;
import ar.edu.doctorlukmanov.vista.tratamiento.PanelTratamientos;
import ar.edu.doctorlukmanov.vista.turno.PanelTurnos;
import ar.edu.doctorlukmanov.vista.veterinario.PanelVeterinarios;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public final class VentanaPrincipal extends JFrame {

    public static final String INICIO = "inicio";
    public static final String CLIENTES = "clientes";
    public static final String GATOS = "gatos";
    public static final String TURNOS = "turnos";
    public static final String VETERINARIOS = "veterinarios";
    public static final String TRATAMIENTOS = "tratamientos";
    public static final String REPORTES = "reportes";

    private static final Color VERDE_PROFUNDO = new Color(25, 74, 74);
    private static final Color CREMA = new Color(246, 242, 232);

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final JLabel estado = new JLabel("Base de datos conectada | Inicio");
    private final PanelClientes panelClientes;
    private final PanelGatos panelGatos;
    private final PanelTurnos panelTurnos;
    private final PanelVeterinarios panelVeterinarios;
    private final PanelTratamientos panelTratamientos;
    private final PanelReportes panelReportes;
    private final Runnable respaldarBaseDatos;

    public VentanaPrincipal(
            PanelClientes panelClientes,
            PanelGatos panelGatos,
            PanelTurnos panelTurnos,
            PanelVeterinarios panelVeterinarios,
            PanelTratamientos panelTratamientos,
            PanelReportes panelReportes,
            Runnable respaldarBaseDatos) {
        super("Clínica Veterinaria Doctor Lukmanov - Gestión Felina");
        this.panelClientes = panelClientes;
        this.panelGatos = panelGatos;
        this.panelTurnos = panelTurnos;
        this.panelVeterinarios = panelVeterinarios;
        this.panelTratamientos = panelTratamientos;
        this.panelReportes = panelReportes;
        this.respaldarBaseDatos = respaldarBaseDatos;
        configurarVentana();
        conectarFlujos();
    }

    public void mostrarPanel(String nombre) {
        tarjetas.show(contenido, nombre);
        estado.setText("Base de datos conectada | " + nombreVisible(nombre));
        refrescar(nombre);
    }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    private void configurarVentana() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(crearCabecera(), BorderLayout.NORTH);
        add(crearNavegacion(), BorderLayout.WEST);
        contenido.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        contenido.add(crearInicio(), INICIO);
        contenido.add(panelClientes, CLIENTES);
        contenido.add(panelGatos, GATOS);
        contenido.add(panelTurnos, TURNOS);
        contenido.add(panelVeterinarios, VETERINARIOS);
        contenido.add(panelTratamientos, TRATAMIENTOS);
        contenido.add(panelReportes, REPORTES);
        add(contenido, BorderLayout.CENTER);
        estado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 210, 210)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        add(estado, BorderLayout.SOUTH);
        setJMenuBar(crearMenu());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                salir();
            }
        });
        mostrarPanel(INICIO);
    }

    private JPanel crearCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(VERDE_PROFUNDO);
        cabecera.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        JLabel nombre = new JLabel("DOCTOR LUKMANOV  |  CLÍNICA VETERINARIA FELINA");
        nombre.setForeground(Color.WHITE);
        nombre.setFont(nombre.getFont().deriveFont(Font.BOLD, 19f));
        JLabel fecha = new JLabel(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale.forLanguageTag("es-AR"))));
        fecha.setForeground(new Color(220, 235, 232));
        cabecera.add(nombre, BorderLayout.WEST);
        cabecera.add(fecha, BorderLayout.EAST);
        return cabecera;
    }

    private JPanel crearNavegacion() {
        JPanel navegacion = new JPanel(new GridLayout(0, 1, 0, 7));
        navegacion.setBackground(CREMA);
        navegacion.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        navegacion.setPreferredSize(new Dimension(180, 0));
        navegacion.add(botonNavegacion("Inicio", INICIO));
        navegacion.add(botonNavegacion("Clientes", CLIENTES));
        navegacion.add(botonNavegacion("Gatos", GATOS));
        navegacion.add(botonNavegacion("Turnos", TURNOS));
        navegacion.add(botonNavegacion("Veterinarios", VETERINARIOS));
        navegacion.add(botonNavegacion("Tratamientos", TRATAMIENTOS));
        navegacion.add(botonNavegacion("Reportes", REPORTES));
        return navegacion;
    }

    private JButton botonNavegacion(String texto, String tarjeta) {
        JButton boton = new JButton(texto);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setFocusPainted(false);
        boton.addActionListener(evento -> mostrarPanel(tarjeta));
        return boton;
    }

    private JPanel crearInicio() {
        JPanel inicio = new JPanel(new BorderLayout(20, 20));
        inicio.setBackground(Color.WHITE);
        inicio.setBorder(BorderFactory.createEmptyBorder(45, 55, 45, 55));
        JLabel titulo = new JLabel("Gestión clínica felina, de punta a punta");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 30f));
        titulo.setForeground(VERDE_PROFUNDO);
        inicio.add(titulo, BorderLayout.NORTH);

        JPanel flujo = new JPanel(new GridLayout(1, 4, 14, 14));
        flujo.setOpaque(false);
        flujo.add(tarjetaInicio("1", "Cliente", "Registrar al responsable del paciente."));
        flujo.add(tarjetaInicio("2", "Gato", "Crear la ficha clínica felina."));
        flujo.add(tarjetaInicio("3", "Turno", "Programar y confirmar la consulta."));
        flujo.add(tarjetaInicio("4", "Atención", "Diagnosticar, tratar y cerrar."));
        inicio.add(flujo, BorderLayout.CENTER);

        JLabel ayuda = new JLabel(
                "Use el menú lateral para gestionar datos. Los reportes se generan sin bloquear la interfaz.");
        ayuda.setFont(ayuda.getFont().deriveFont(15f));
        inicio.add(ayuda, BorderLayout.SOUTH);
        return inicio;
    }

    private JPanel tarjetaInicio(String numero, String titulo, String texto) {
        JPanel tarjeta = new JPanel(new BorderLayout(4, 8));
        tarjeta.setBackground(CREMA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, VERDE_PROFUNDO),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        JLabel encabezado = new JLabel(numero + "  " + titulo);
        encabezado.setFont(encabezado.getFont().deriveFont(Font.BOLD, 18f));
        tarjeta.add(encabezado, BorderLayout.NORTH);
        tarjeta.add(new JLabel("<html>" + texto + "</html>"), BorderLayout.CENTER);
        return tarjeta;
    }

    private JMenuBar crearMenu() {
        JMenuBar barra = new JMenuBar();
        JMenu archivo = menu("Archivo", 'A');
        archivo.add(item("Inicio", () -> mostrarPanel(INICIO)));
        archivo.add(item("Respaldar base de datos", respaldarBaseDatos));
        archivo.addSeparator();
        archivo.add(item("Salir", this::salir));

        JMenu clientes = menu("Clientes", 'C');
        clientes.add(item("Gestionar clientes", () -> mostrarPanel(CLIENTES)));
        clientes.add(item("Nuevo cliente", () -> {
            mostrarPanel(CLIENTES);
            panelClientes.nuevo();
        }));

        JMenu gatos = menu("Gatos", 'G');
        gatos.add(item("Gestionar gatos", () -> mostrarPanel(GATOS)));
        gatos.add(item("Registrar gato", () -> {
            mostrarPanel(GATOS);
            panelGatos.nuevo();
        }));
        gatos.add(item("Historia clínica", () -> mostrarPanel(GATOS)));

        JMenu turnos = menu("Turnos", 'T');
        turnos.add(item("Agenda diaria", () -> mostrarPanel(TURNOS)));
        turnos.add(item("Programar turno", () -> {
            mostrarPanel(TURNOS);
            panelTurnos.nuevo();
        }));
        turnos.add(item("Confirmar turno", () -> mostrarPanel(TURNOS)));
        turnos.add(item("Completar turno", () -> mostrarPanel(TURNOS)));
        turnos.add(item("Cancelar turno", () -> mostrarPanel(TURNOS)));

        JMenu veterinarios = menu("Veterinarios", 'V');
        veterinarios.add(item("Gestionar veterinarios", () -> mostrarPanel(VETERINARIOS)));
        JMenu tratamientos = menu("Tratamientos", 'R');
        tratamientos.add(item("Gestionar tratamientos", () -> mostrarPanel(TRATAMIENTOS)));

        JMenu reportes = menu("Reportes", 'P');
        for (TipoReporte tipo : TipoReporte.values()) {
            reportes.add(item(tipo.getDescripcion(), () -> {
                mostrarPanel(REPORTES);
                panelReportes.seleccionarTipo(tipo);
            }));
        }

        JMenu ayuda = menu("Ayuda", 'Y');
        ayuda.add(item("Acerca de", () -> Dialogos.informar(this,
                "Clínica Veterinaria Doctor Lukmanov\nGestión exclusiva de pacientes felinos\nVersión 1.0.0")));
        barra.add(archivo);
        barra.add(clientes);
        barra.add(gatos);
        barra.add(turnos);
        barra.add(veterinarios);
        barra.add(tratamientos);
        barra.add(reportes);
        barra.add(ayuda);
        return barra;
    }

    private JMenu menu(String texto, char mnemonic) {
        JMenu menu = new JMenu(texto);
        menu.setMnemonic(mnemonic);
        return menu;
    }

    private JMenuItem item(String texto, Runnable accion) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(evento -> accion.run());
        return item;
    }

    private void conectarFlujos() {
        panelClientes.setAlVerGatos(cliente -> {
            mostrarPanel(GATOS);
            panelGatos.filtrarPorCliente(cliente);
        });
        panelGatos.setAlProgramarTurno(gato -> {
            mostrarPanel(TURNOS);
            panelTurnos.nuevoParaGato(gato);
        });
    }

    private void refrescar(String nombre) {
        switch (nombre) {
            case CLIENTES -> panelClientes.refrescar();
            case GATOS -> panelGatos.refrescar();
            case TURNOS -> panelTurnos.refrescar();
            case VETERINARIOS -> panelVeterinarios.refrescar();
            case TRATAMIENTOS -> panelTratamientos.refrescar();
            default -> {
            }
        }
    }

    private String nombreVisible(String nombre) {
        return switch (nombre) {
            case CLIENTES -> "Clientes";
            case GATOS -> "Gatos";
            case TURNOS -> "Agenda";
            case VETERINARIOS -> "Veterinarios";
            case TRATAMIENTOS -> "Tratamientos";
            case REPORTES -> "Reportes";
            default -> "Inicio";
        };
    }

    private void salir() {
        if (Dialogos.confirmar(this, "¿Desea cerrar la aplicación?")) {
            dispose();
        }
    }
}
