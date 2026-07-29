package ar.edu.doctorlukmanov.vista.componentes;

import java.awt.FlowLayout;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

public final class SelectorFechaHora extends JPanel {

    private final JSpinner fecha;
    private final JSpinner hora;

    public SelectorFechaHora() {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        Date inicial = Date.from(LocalDateTime.now().plusHours(1)
                .atZone(ZoneId.systemDefault()).toInstant());
        fecha = new JSpinner(new SpinnerDateModel(inicial, null, null, java.util.Calendar.DAY_OF_MONTH));
        hora = new JSpinner(new SpinnerDateModel(inicial, null, null, java.util.Calendar.MINUTE));
        fecha.setEditor(new JSpinner.DateEditor(fecha, "dd/MM/yyyy"));
        hora.setEditor(new JSpinner.DateEditor(hora, "HH:mm"));
        add(fecha);
        add(hora);
    }

    public LocalDateTime getValor() {
        LocalDateTime valorFecha = LocalDateTime.ofInstant(
                ((Date) fecha.getValue()).toInstant(), ZoneId.systemDefault());
        LocalDateTime valorHora = LocalDateTime.ofInstant(
                ((Date) hora.getValue()).toInstant(), ZoneId.systemDefault());
        return valorFecha.toLocalDate().atTime(valorHora.toLocalTime()).withSecond(0).withNano(0);
    }

    public void setValor(LocalDateTime valor) {
        Date fechaJava = Date.from(valor.atZone(ZoneId.systemDefault()).toInstant());
        fecha.setValue(fechaJava);
        hora.setValue(fechaJava);
    }
}
