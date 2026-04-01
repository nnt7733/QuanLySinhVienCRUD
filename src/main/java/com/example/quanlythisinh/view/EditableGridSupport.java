package com.example.quanlythisinh.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Cấu hình bảng chỉnh sửa: cột chỉ định không sửa được; phím Enter dừng chỉnh sửa ô và gọi callback lưu.
 */
public final class EditableGridSupport {
    private EditableGridSupport() {
    }

    public static DefaultTableModel createModel(String[] columnNames, int... readOnlyColumnIndices) {
        Set<Integer> ro = new HashSet<>();
        for (int i : readOnlyColumnIndices) {
            ro.add(i);
        }
        return new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return !ro.contains(column);
            }
        };
    }

    /**
     * Gắn Enter: kết thúc editor nếu đang sửa, rồi gọi {@code onCommit} (thường là lưu dòng đang chọn).
     */
    public static void installEnterCommits(JTable table, Runnable onCommit) {
        InputMap im = table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = table.getActionMap();
        KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
        im.put(enter, "commitGridEdit");
        am.put("commitGridEdit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table.isEditing()) {
                    TableCellEditor ce = table.getCellEditor();
                    if (ce != null) {
                        ce.stopCellEditing();
                    }
                }
                onCommit.run();
            }
        });
    }
}
