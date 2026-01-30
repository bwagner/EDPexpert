import java.lang.invoke.MethodHandles;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * TODO:
 * - incorporate into EDP-structure
 * - provide displaying of current state
 * - provide editing
 * - global parameters?
 */

public class Presets {
	
	private static final boolean DEBUG = true;
	
	public Presets(){
		buildGui();
	}
	private static void buildGui() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
        JTable table = new JTable(new MyTableModel());
        table.setShowGrid(true);
        table.setGridColor(Color.GRAY);
        table.setPreferredScrollableViewportSize(new Dimension(1000, 260));
        setupCellEditors(table);
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
		frame.add(scrollPane, BorderLayout.NORTH);
		JPanel buttons = new JPanel(new GridLayout(1, 0));
		JButton download = new JButton("download");
		JButton upload = new JButton("upload");
		JButton load = new JButton("load");
		JButton save = new JButton("save");
		buttons.add(download);
		buttons.add(upload);
		buttons.add(load);
		buttons.add(save);
		frame.add(buttons, BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);
	}
	private static void setupCellEditors(JTable table) {
         for(int i = 1 ; i < table.getColumnCount() ; ++i) {
        	TableColumn column = table.getColumnModel().getColumn(i);
            JComboBox<String> comboBox = new JComboBox<String>();
            for(int j = 1 ; j < EDPParameterStrings.fgLocalParameters[i-1].length ; ++j){
            	comboBox.addItem(EDPParameterStrings.fgLocalParameters[i-1][j]);
            }
            // the last column is the tempo column:
            comboBox.addItem("Off");
            TempoParameterParser valueGenerator = new TempoParameterParser(new String[][]{});
            for(int j = 1 ; j < 128 ; ++j){
            	comboBox.addItem(""+ valueGenerator.doDetermineValue(0, j));
            }
            column.setCellEditor(new DefaultCellEditor(comboBox));
            DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();
	        renderer.setToolTipText("Click for combo box");
	        column.setCellRenderer(renderer);
        }
	}
	public static void main(String[] args){
		buildGui();
	}
	
    static class MyTableModel extends AbstractTableModel {
    	
		private static final long serialVersionUID = 1L;

		private static List<String> fgColumnNames = new ArrayList<String>();
    	
    	static {
    		fgColumnNames.add("Preset");
    		for(int i = 0 ; i < EDPParameterStrings.fgLocalParameters.length ; ++i){
        		fgColumnNames.add(EDPParameterStrings.fgLocalParameters[i][0]);
    		}
    	}
        private Object[][] data = {
        		{ "0" , ".", "", "", "", "", "", "", "",  "", "", "", "", "", "", "", "", "", "."},
        		{ "1" ,  "", "", "", "", "", "", "", "",  "", "", "", "", "", "", "", "", "",  ""},
        		{ "2" ,  "", "", "", "", "", "", "", "",  "", "", "", "", "", "", "", "", "",  ""},
        		{ "3" ,  "", "", "", "", "", "", "", "",  "", "", "", "", "", "", "", "", "",  ""},
        		{ "4" ,  "", "", "", "", "", "", "", "",  "", "", "", "", "", "", "", "", "",  ""},
        		{ "5" ,  "", "", "", "", "", "", "", "",  "", "", "", "", "", "", "", "", "",  ""},
        		{ "6" ,  "", "", "", "", "", "", "", "",  "", "", "", "", "", "", "", "", "",  ""},
        		{ "7" ,  "", "", "", "", "", "", "", "",  "", "", "", "", "", "", "", "", "",  ""},
        		{ "8" ,  "", "", "", "", "", "", "", "", ".", "", "", "", "", "", "", "", "",  ""},
        		{ "9" ,  "", "", "", "", "", "", "", "",  "", "", "", "", "", "", "", "", "",  ""},
        		{"10" ,  "", "", "", "", "", "", "", "",  "", "", "", "", "", "", "", "", "",  ""},
        		{"11" ,  "", "", "", "", "", "", "", "",  "", "", "", "", "", "", "", "", "",  ""},
        		{"12" ,  "", "", "", "", "", "", "", "",  "", "", "", "", "", "", "", "", "",  ""},
        		{"13" ,  "", "", "", "", "", "", "", "",  "", "", "", "", "", "", "", "", "",  ""},
        		{"14" ,  "", "", "", "", "", "", "", "",  "", "", "", "", "", "", "", "", "",  ""},
        		{"15" , ".", "", "", "", "", "", "", "",  "", "", "", "", "", "", "", "", "", "."},
        };

        public int getColumnCount() {
            return fgColumnNames.size();
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return fgColumnNames.get(col);
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class<?> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
        	return col > 0; // col 0 is preset number which is constant.
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
        	// row equals preset number
            if (DEBUG) {
                LOGGER.debug("Setting value at " + row + "," + col
                                   + " to " + value
                                   + " (an instance of "
                                   + value.getClass() + ")");
            }

            data[row][col] = value;
            fireTableCellUpdated(row, col);

            if (DEBUG) {
                LOGGER.debug("New value of data:");
                printDebugData();
            }
        }

        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();

            final StringBuffer sb = new StringBuffer();
            for (int i=0; i < numRows; i++) {
                sb.append("    row " + i + ":" + "\n");
                for (int j=0; j < numCols; j++) {
                    sb.append("  " + data[i][j]);
                }
                sb.append("\n");
            }
            sb.append("--------------------------\n");
            LOGGER.info(sb);
        }
    }
    
	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

}
