import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.ScrollPane;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.util.ArrayList;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;

public class Window extends JFrame{

    private JFrame jFrame;
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem item1;
    private JList list;
    private JTextArea logDisplay;
    private JTextField path;
    private String pathDir;
    private JTextField regex;
    private String[] regexList;
    private JTextField updateTime;
    private long updateTimeNum;
    private JTextField modifiedRange;
    private JTextField numLines;
    private int numLinesNum;
    private long modifiedRangeNum;
    private JButton start;
    private JButton stop;
    private DefaultListModel<String> model;
    private Thread thread;
    private boolean runningThread;
    private File[] changingFiles;
    private ArrayList<File> tailedFiles;
    private JButton select;


    
    public Window(){
        init();
    }

    private void init(){
        jFrame = new JFrame("LogWatch");
        jFrame.setPreferredSize(new Dimension(1200, 600));
        jFrame.setMaximumSize(new Dimension(1200, 720));
        jFrame.setMinimumSize(new Dimension(1200, 600));
        jFrame.setResizable(false);
        jFrame.requestFocus();
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BorderLayout layout = new BorderLayout();
        JPanel panel = new JPanel();
        panel.setLayout(layout);
        jFrame.setContentPane(panel);

        panel.add(createMenu(), BorderLayout.PAGE_START);
        panel.add(controlPanel(), BorderLayout.WEST);
        panel.add(filesList(), BorderLayout.CENTER);
        panel.add(watchDisplay(), BorderLayout.EAST);

        actionsSet();
        jFrame.pack();
        jFrame.setVisible(true);
    }

    private void actionsSet(){

        start.addActionListener(e->{
            pathDir = path.getText();
            regexList = regex.getText().split(";");
            try{
                updateTimeNum = Long.parseLong(updateTime.getText());
                updateTimeNum = updateTimeNum <= 0 ? 1 : updateTimeNum;
            }catch(NumberFormatException ex){
                updateTimeNum = 1;
            }

            try{
                modifiedRangeNum = Long.parseLong(modifiedRange.getText());
                modifiedRangeNum = modifiedRangeNum <= 0 ? 1 : modifiedRangeNum;
            }catch(NumberFormatException ex){
                modifiedRangeNum = 300;
            }

            try{
                numLinesNum = Integer.parseInt(numLines.getText());
                numLinesNum = modifiedRangeNum <= 0 ? 1 : numLinesNum;
            }catch(NumberFormatException ex){
                numLinesNum = 5;
            }

            thread = new Thread(()->{
                runningThread = true;
                try{
                    PathFinder pf= new PathFinder(pathDir, regexList, modifiedRangeNum);
                    while(runningThread){
                        changingFiles = pf.detectChanges();
                        for(File f : changingFiles){
                            if(model.indexOf(f.getAbsolutePath().substring(pathDir.length())) == -1)
                                model.addElement(f.getAbsolutePath().substring(pathDir.length()));
                        }

                        Tailer t = new Tailer(tailedFiles.toArray(new File[tailedFiles.size()]), numLinesNum);
                        String printText = "";
                        for(String s : t.tail()){
                            printText += s;
                            printText += '\n';
                            printText += "------------------------------------------------------------------------------------------------------------------------------";
                            printText += '\n';
                        }
                        logDisplay.setText(printText);
                        Thread.sleep(updateTimeNum*1000);

                        //Update file list
                        new Thread(() ->{
                            pf.updateList();
                        }).start();;
                    }
                }catch(Exception ex){}
            });

            thread.start();

            stop.setEnabled(true);
            start.setEnabled(false);
            path.setEnabled(false);
            updateTime.setEnabled(false);
            modifiedRange.setEnabled(false);
            regex.setEnabled(false);
            numLines.setEnabled(false);
            select.setEnabled(false);
        });

        stop.addActionListener(e->{

            thread.interrupt();
            if(thread.isInterrupted() || runningThread){
                runningThread = false;
                stop.setEnabled(false);
                start.setEnabled(true);
                path.setEnabled(true);
                updateTime.setEnabled(true);
                modifiedRange.setEnabled(true);
                regex.setEnabled(true);
                numLines.setEnabled(true);
                select.setEnabled(true);

                model.clear();
                tailedFiles.clear();
            }
        });

        tailedFiles = new ArrayList<>();

        list.addMouseListener( new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    if(tailedFiles.indexOf(changingFiles[list.getSelectedIndex()]) >= 0){
                        tailedFiles.remove(changingFiles[list.getSelectedIndex()]);
                    }else{
                        tailedFiles.add(changingFiles[list.getSelectedIndex()]);
                    }
                }
            }
        });

        select.addActionListener(e->{
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("."));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
                 path.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
    }

    private JMenuBar createMenu(){
        menuBar = new JMenuBar();

        menu = new JMenu("Files");
        menuBar.add(menu);
        
        item1 = new JMenuItem("Exit");
        item1.addActionListener(e ->{
            System.exit(0);
        });
        menu.add(item1);
        return menuBar;
    }

    private JPanel controlPanel(){
        JPanel control = new JPanel();
        

        JLabel rootPathLabel = new JLabel("Root path:        ");
        path = new JTextField();
        path.setPreferredSize(new Dimension(175,20));
        control.add(rootPathLabel);
        control.add(path);

        select = new JButton(".");
        select.setPreferredSize(new Dimension(25,20));
        control.add(select);



        JLabel regexLabel = new JLabel("File name regex: ");
        regex = new JTextField();
        regex.setText(".+\\.log");
        regex.setPreferredSize(new Dimension(200,20));
        control.add(regexLabel);
        control.add(regex);

        JLabel updateTimeLabel = new JLabel("Refresh per seconds: ");
        updateTime = new JTextField();
        updateTime.setText("1");
        updateTime.setPreferredSize(new Dimension(200,20));
        control.add(updateTimeLabel);
        control.add(updateTime);

        JLabel modifiedRangeLabel = new JLabel("Time form last edit (seconds): ");
        modifiedRange = new JTextField();
        modifiedRange.setText("300");
        modifiedRange.setPreferredSize(new Dimension(200,20));
        control.add(modifiedRangeLabel);
        control.add(modifiedRange);

        
        JLabel numberOfLinesLabel = new JLabel("Number of lines: ");
        numLines = new JTextField();
        numLines.setText("5");
        numLines.setPreferredSize(new Dimension(200,20));
        control.add(numberOfLinesLabel);
        control.add(numLines);

        start = new JButton("Start");
        stop = new JButton("Stop");
        stop.setEnabled(false);;
        control.add(start);
        control.add(stop);


        control.setPreferredSize(new Dimension(250,200));
        control.setMaximumSize(new Dimension(250,200));
        control.setMinimumSize(new Dimension(250,200));
        return control;
    }
    private JPanel filesList(){
        JPanel pane = new JPanel(new BorderLayout());
        model = new DefaultListModel<>();
        list = new JList<>(model);
        pane.add(list);
        pane.setPreferredSize(new Dimension(350,200));
        pane.setMaximumSize(new Dimension(350,200));
        pane.setMinimumSize(new Dimension(350,200));
        return pane;
    }
    private JPanel watchDisplay(){
        JPanel pane = new JPanel(new BorderLayout());

        logDisplay = new JTextArea();
        logDisplay.setLineWrap(true);
        logDisplay.setText("Created by Karol Kosinski \nLogWatch manual: " + 
        "\n1. Set up the parent directory where the files are located. " + 
        "\n2. Use regular expressions to filter out files you interesting. To use more than one expression, use \";\" to separate them." +
        "\n3. Set the refresh time of the file content." + 
        "\n4. Set the time of the last edit a file." + 
        "\n5. Set the number of last lines to show." +
        "\n6. Press start to run logwatch." +
        "\n7. Double click on file, which you want to watch (you can watch more than one file)." +
        "\n8. Double click again to hide watching file." +
                "\n\n\n more information about project: https://github.com/kosinsk2/MultiTail-LogWatch");
        
        JScrollPane sp = new JScrollPane(logDisplay);
        sp.setViewportView(logDisplay);
        pane.add(sp);
        pane.setPreferredSize(new Dimension(600,400));
        pane.setMaximumSize(new Dimension(600,400));
        pane.setMinimumSize(new Dimension(600,400));

        return pane;
    }


}
