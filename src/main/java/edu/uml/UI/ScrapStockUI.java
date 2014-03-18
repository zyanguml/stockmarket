package edu.uml.UI;

import edu.uml.business.HistoryReportForm;
import edu.uml.business.ScrapStockData;
import edu.uml.persistence.PersistSymbol;
import edu.uml.persistence.PersistSymbolXML;
import edu.uml.business.SymbolSearchForm;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Set;

/**
 * Created by US315746 on 3/1/14.
 */
public class ScrapStockUI extends JFrame {

    public JFrame thisFrame; // parent frame
    private JLabel labelStockSymbol = new JLabel();  // this is the label for the stock symbol text box
    private JTextField textFieldStockSymbol = new JTextField("",5); // this is the text box for entering the stock symbol
    private DefaultComboBoxModel cbModel = new DefaultComboBoxModel(); // this contains all the items in teh combo box
    private JComboBox comboBoxStockSymbol = new JComboBox(cbModel); // this is a list of stocks that are saved
    private JButton buttonLookUp = new JButton("Look Up");  //  this is a button to look up the data on the stock in the text box
    private JButton buttonAdd =  new JButton("Add");    // this button adds the symbol in the text box to the combo box / list to be saved
    private JButton buttonRemove = new JButton("Remove");// this button removes the symbol in the text box to the combo box and from list to be saved
    private JButton buttonSearch = new JButton("Symbol Search");// this button brings up a dialog to search for a stock symbol
    private JScrollPane scrollPaneTable; // this is the scroll panel where the table goes
    private JTable stockInfoTable;  // this is the table where data on the stock is displayed
    private String[] columnNames = {"Symbol", "Price", "Change", "Percent Change"};  // the items we will get about the stock
    private String[][] data = new String[30][4];  // the data obtained about the stock
    private DefaultTableModel model;  // the model to put the table in
    private int currentRow = 0;  // the current row of the table to write text data to
    private PersistSymbol persistence ; // a persistence object to save stock symbols to
    public final String xmlFile = "C:\\Temp\\StockSymbolData.xml"; // the name of the file to save the stock symbols to

    public ScrapStockUI() {
        try {
            // load all the stock symbols that were persisted
            persistence = new PersistSymbolXML();
            persistence.readFromDB(xmlFile);
            Set<String> symbols = persistence.getSymbols();

            model = new DefaultTableModel(data, columnNames);
            stockInfoTable = new JTable(model);
            scrollPaneTable = new JScrollPane(stockInfoTable);
            comboBoxStockSymbol.setPrototypeDisplayValue("xbcdef");


            for (String s : symbols)
            {
                comboBoxStockSymbol.addItem(s);
            }

            JPanel northPanel = new JPanel();
            labelStockSymbol.setText("Stock Symbol");

            northPanel.add(labelStockSymbol);
            northPanel.add(textFieldStockSymbol);
            northPanel.add(buttonLookUp);
            northPanel.add(buttonAdd);
            northPanel.add(buttonRemove);
            northPanel.add(buttonSearch);
            add(northPanel, BorderLayout.NORTH);

            JPanel southPanel = new JPanel();

            southPanel.add(scrollPaneTable);
            southPanel.add(comboBoxStockSymbol);
            add(southPanel, BorderLayout.SOUTH);

            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
            this.pack();
            this.setLocationRelativeTo(null);
            this.setVisible(true);

            // this is the listener for the search button which helps the user find the symbol of a company name
            buttonSearch.addActionListener(new ActionListener()
            {


                @Override
                public void actionPerformed(ActionEvent e)
                {
                    SymbolSearchForm searchFrame = new SymbolSearchForm();
                    searchFrame.setVisible(true);
                }
            });

            // listener for add button - this is called when the user presses the "Add" button
            buttonAdd.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    String stockSymbol = textFieldStockSymbol.getText();
                    if (!stockSymbol.equals(""))
                    {
                        // make sure the symbol isn't already here
                        comboBoxStockSymbol.setSelectedItem(stockSymbol);
                        int theIndex = comboBoxStockSymbol.getSelectedIndex();

                        if (cbModel.getIndexOf(stockSymbol) == -1)
                        {
                            cbModel.addElement(stockSymbol);
                            //comboBoxStockSymbol.addItem(stockSymbol);
                            Map<String,String> obj = persistence.createSymbol(stockSymbol);
                            persistence.saveSymbol(stockSymbol);
                            persistence.writeDB(xmlFile);
                        }
                    }
                }
            });

            // this is the listener for the remove button which will remove the item from the list
            buttonRemove.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    String currentSymbol = comboBoxStockSymbol.getSelectedItem().toString();
                    comboBoxStockSymbol.removeItem(currentSymbol);
                    persistence.deleteSymbol(currentSymbol);
                    persistence.writeDB(xmlFile);
                }
            });
            /*
            *  Get the selected item and put it in the text box for the stock symbol
             */
            comboBoxStockSymbol.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    String stockSymbol = (String )comboBoxStockSymbol.getSelectedItem();
                    textFieldStockSymbol.setText(stockSymbol);
                }
            });
            /*
            * Get data on the stock symbol in the text box when this button is pushed
             */
            buttonLookUp.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    // get the stock symbol from the text box
                    String stockSymbol = textFieldStockSymbol.getText();
                    // create a new ScrapStockData object
                    ScrapStockData stockInfo = new ScrapStockData();
                    // TODO what happens if the stock Symbol is unknown?
                    // TODO add code to make sure the table doesn't run out (wrap around?)
                    // Get the info on the stock
                    // see https://code.google.com/p/yahoo-finance-managed/wiki/enumQuoteProperty for meaning
                    // of the string to obtain info on stocks
                    String info = stockInfo.getStockInfoOn(stockSymbol,"s0p0c1p2");
                    String currentPrice = info.split(",")[1].replaceAll("^\"|\"$", "");
                    // determine if the symbol is a valid one
                    if (currentPrice.equals("N/A")) {
                        JOptionPane.showMessageDialog(thisFrame,"Symbol is not recognized.");
                    }
                    else {
                         for (int j = 0; j < 4; j++) {
                            model.setValueAt(info.split(",")[j].replaceAll("^\"|\"$", ""), currentRow, j);
                         }
                        currentRow++;
                    }
                }
            });

            /**
             * History table - it pops up when a row on the table is clicked
             */
            stockInfoTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                  //  if (e.getClickCount() == 2) {
                    JTable target = (JTable)e.getSource();
                    int row = target.getSelectedRow();
                    String stockSymbol = (String)stockInfoTable.getValueAt(row, 0);
                    if (stockSymbol != null){
                        HistoryReportForm historyReportForm = new HistoryReportForm(stockSymbol);
                    }
                  //  }
                }
            });
        } //try
        catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args) {
        ScrapStockUI frame = new ScrapStockUI();
        frame.thisFrame = frame;
    }
}


