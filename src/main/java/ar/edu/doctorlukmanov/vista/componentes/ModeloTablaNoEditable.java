package ar.edu.doctorlukmanov.vista.componentes;

import javax.swing.table.DefaultTableModel;

public class ModeloTablaNoEditable extends DefaultTableModel {

    public ModeloTablaNoEditable(Object[] nombresColumnas) {
        super(nombresColumnas, 0);
    }

    public ModeloTablaNoEditable(Object[][] datos, Object[] nombresColumnas) {
        super(datos, nombresColumnas);
    }

    @Override
    public boolean isCellEditable(int fila, int columna) {
        return false;
    }
}
