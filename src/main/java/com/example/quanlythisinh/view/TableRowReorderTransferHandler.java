package com.example.quanlythisinh.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Kéo-thả để đổi thứ tự dòng trong {@link JTable} (cùng {@link DefaultTableModel}).
 * Cần: {@code setDragEnabled(true)}, {@code setDropMode(DropMode.INSERT_ROWS)}.
 */
public final class TableRowReorderTransferHandler extends TransferHandler {
    private final JTable table;
    private final Runnable afterMove;

    public TableRowReorderTransferHandler(JTable table, Runnable afterMove) {
        this.table = table;
        this.afterMove = afterMove;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTable t = (JTable) c;
        int row = t.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return new StringSelection(Integer.toString(row));
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDrop() && support.getComponent() == table && support.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        int sourceRow;
        try {
            String s = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
            sourceRow = Integer.parseInt(s.trim());
        } catch (UnsupportedFlavorException | IOException ex) {
            return false;
        }
        JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
        int dropRow = dl.getRow();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int n = model.getRowCount();
        if (sourceRow < 0 || sourceRow >= n || dropRow < 0) {
            return false;
        }
        if (dropRow >= n && n > 0) {
            dropRow = n - 1;
        }
        if (sourceRow == dropRow) {
            return false;
        }
        int to = dropRow > sourceRow ? dropRow - 1 : dropRow;
        model.moveRow(sourceRow, sourceRow, to);
        table.getSelectionModel().setSelectionInterval(to, to);
        if (afterMove != null) {
            afterMove.run();
        }
        return true;
    }
}
