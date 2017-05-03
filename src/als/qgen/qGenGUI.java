/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package als.qgen;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author leiti_000
 */
public class qGenGUI extends javax.swing.JFrame {

    static funcs fcs;
    String modelFinalPath = "";
    String modelFinalName = "MyModel";
    double scale = 10;
    String refPath = "";
    String materialsFinalPath = "";
    String colPath = "";
    boolean automass = false;
    int mass = 25;
    String surfaceProp = "default";
    boolean nocollision = false;
    boolean defcollision = false;
    boolean staticprop = false;
    String smdFileName = "";
    
    public boolean Validate(){
        boolean result = false;
        if(refPath == "" || smdFileName == ""){
            result = false;
        }
        else{
            result = true;
        }
        return result;
    }
    
    public String getTime(){
    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    Date date = new Date();
    return dateFormat.format(date);
    }
    
    public void checkComp(){
        boolean exs = new File(fcs.getSetting("compDir", null)+"\\studiomdl.exe").isFile();
        System.out.println(exs);
        if(exs){
            jButton6.setText("Compile (Beta)");
            jButton6.setEnabled(true);
        }
        else{
            jButton6.setText("Compile (See Log)");
            jButton6.setEnabled(false);
            log.append(getTime()+" | Studiomdl.exe was not found at "+fcs.getSetting("compDir", "Unset")+"\n");
        }
    }
    
    public void genQC(int save){
        String qGens = "";
        if(Validate() == true){
        try{
        StringBuilder writer = new StringBuilder();   
        //PrintWriter writer = new PrintWriter(selectedPath, "UTF-8");
        writer.append("$modelname \""+modelFinalPath+modelFinalName+".mdl\"");
        writer.append("\n$scale "+ (scale / 10));
        if(staticprop == true){
            writer.append("\n$staticprop");
        }
        writer.append("\n$model \"Body\" \""+refPath+"\"");
        writer.append("\n$cdmaterials \""+materialsFinalPath+"\"");
        writer.append("\n$surfaceprop \""+surfaceProp+"\"");
        writer.append("\n$sequence idle \""+refPath+"\" loop fps 15");
        if(nocollision == false){
            if(defcollision == true || colPath == ""){
                writer.append("\n$collisionmodel \""+refPath+"\"");
            }
            else if(defcollision == false){
                writer.append("\n$collisionmodel \""+colPath+"\"");
            }
            writer.append("\n{");
            if(automass == false){
            writer.append("\n\t$mass "+mass);
            }
            else{
            writer.append("\n\t$automass");    
            }
            writer.append("\n\t$concave");
            writer.append("\n}");
            
        }
        else{
            //Nothing
        }
        qGens = writer.toString();
            }
            catch(Exception e){
                e.printStackTrace();
            }    
        
        if(save == 1){
        File file = new File(fcs.getSetting("workingDir", "Not Set")+"\\"+smdFileName+".qc");
        JFileChooser f = new JFileChooser(fcs.getSetting("workingDir", "C:"));
        f.setSelectedFile(file);
        f.showSaveDialog(null);
        String selectedPath = String.valueOf(f.getSelectedFile());
        try{
        PrintWriter writer = new PrintWriter(selectedPath, "UTF-8");
        writer.write(qGens);
        writer.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        }
        else if(save == 2){
            StringSelection stringSelection = new StringSelection (qGens);
            Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
            clpbrd.setContents (stringSelection, null);
        }
        else if(save == 3){
           try{
                  
                    String[] lel = modelFinalPath.split("common");
                    for(int a = 0; a < lel.length; a++){
                        System.out.println(lel[a]);
                    }
                    String FilePth = fcs.getSetting("gameDir", "C:")+"\\qGenTemp.qc";
                    File tmp = new File(FilePth);
                    tmp.createNewFile();
                    PrintWriter out = new PrintWriter(tmp);
                    out.write(qGens);
                    out.close();
                    
                    Runtime rt = Runtime.getRuntime();
                    String[] commands = {
                        fcs.getSetting("compDir", null)+"\\studiomdl.exe","-get t", 
                        "-nop4",
                        "-game",
                        fcs.getSetting("gameDir", "Unset"),
                        FilePth
                    
                    
                    };
                    Process proc = rt.exec(commands);

                    BufferedReader stdInput = new BufferedReader(new 
                    InputStreamReader(proc.getInputStream()));

                    BufferedReader stdError = new BufferedReader(new 
                    InputStreamReader(proc.getErrorStream()));
                    String s = null;
                    while ((s = stdInput.readLine()) != null) {
                    log.append(s+"\n");
                    }
                    
                    // read any errors from the attempted command
                    System.out.println("Here is the standard error of the command (if any):\n");
                    while ((s = stdError.readLine()) != null) {
                    log.append(s);
                    }
           }
           catch(Exception e){e.printStackTrace();}
        }
        }
        else{
            error.setText("You need to select at least SMD reference.");
        }
   
    }
    
    
    public qGenGUI() {
        initComponents();
        fcs = new funcs();
        if(fcs.getSetting("firstVisit", "true").equals("true")){
        jFrame1.pack();
        jFrame1.setLocationRelativeTo(null);
        jFrame1.setVisible(true);
        workingDirLabel.setText(fcs.getSetting("workingDir", "Unset"));
        gameDirLabel.setText(fcs.getSetting("gameDir", "Unset"));
        gameDirLabel1.setText(fcs.getSetting("compDir", "Unset"));
        fcs.setSetting("firstVisit", "false");
        }
        
        
        
        try{
        Image i = ImageIO.read(getClass().getResource("logo.png"));
            setIconImage(i);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        checkComp();
        
        jFrame1.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
        checkComp();
        }
});
        String workingDir = fcs.getSetting("workingDir", "Not Set");
        workingDirLabel.setText(workingDir);
        String gameDir = fcs.getSetting("gameDir", "Not Set");
        gameDirLabel.setText(gameDir);
        output_name_field.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                upText();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                upText();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                upText();
            }
            public void upText(){
                modelFinalName = output_name_field.getText();
            }
        });
    
        jSlider1.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                scale = jSlider1.getValue();
                scaleField.setText(String.valueOf((scale * 10) / 100));
            }
        });
        
        jSlider2.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                mass = jSlider2.getValue();
                massField.setText(String.valueOf(mass));
            }
        });
        
        //Scale Auto a.k.a - 1
        jCheckBox2.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if(jCheckBox2.isSelected()){
                jSlider1.setValue(10);
                jSlider1.setEnabled(false);
                scaleField.setEnabled(false);
                }
                else{
                jSlider1.setEnabled(true);
                scaleField.setEnabled(true);
                }
            }
        });
        //Mass Auto.
        jCheckBox1.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if(jCheckBox1.isSelected()){
                massField.setText("Auto");
                jSlider2.setEnabled(false);
                massField.setEnabled(false);
                automass = true;
                }
                else{
                massField.setText(String.valueOf(jSlider2.getValue()));
                jSlider2.setEnabled(true);
                massField.setEnabled(true);
                automass = false;
                }
            }
        });
        
        
        surface.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    surfaceProp = String.valueOf(surface.getSelectedItem());
                    System.out.println(surfaceProp);
                }
            }
        });
        
        //Autogen collision.
        default_check.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if(default_check.isSelected()){
                defcollision = true;
                collision_check.setEnabled(false);
                collision_field.setEnabled(false);
                collision_browse.setEnabled(false);
                }
                else{
                collision_check.setEnabled(true);
                defcollision = false;
                collision_field.setEnabled(true);
                collision_browse.setEnabled(true);
                }
            }
        });
        
        //No collisions.
        collision_check.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                
                if(collision_check.isSelected()){
                nocollision = true;
                default_check.setEnabled(false);
                collision_field.setEnabled(false);
                collision_browse.setEnabled(false);
                }
                else{
                nocollision = false;
                default_check.setEnabled(true);
                collision_field.setEnabled(true);
                collision_browse.setEnabled(true);
                }
            }
        });
        //static prop
        collision_check1.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                
                if(collision_check1.isSelected()){
                staticprop = true;
                }
                else{
                staticprop = false;
                }
            }
        });
    }
        
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrame1 = new javax.swing.JFrame();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        workingDirLabel = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        gameDirLabel = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        gameDirLabel1 = new javax.swing.JTextField();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        options_logo = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        output_location_field = new javax.swing.JTextField();
        output_browse = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        output_name_field = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        ref_field = new javax.swing.JTextField();
        browse_ref = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        materials_field = new javax.swing.JTextField();
        materials_browse = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        surface = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        collision_field = new javax.swing.JTextField();
        collision_browse = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jSlider1 = new javax.swing.JSlider();
        scaleField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jSlider2 = new javax.swing.JSlider();
        massField = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        default_check = new javax.swing.JCheckBox();
        collision_check = new javax.swing.JCheckBox();
        collision_check1 = new javax.swing.JCheckBox();
        jButton20 = new javax.swing.JButton();
        jButton21 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        log = new javax.swing.JTextArea();
        jButton23 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        error = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();

        jFrame1.setTitle("Settings");
        jFrame1.setResizable(false);

        jButton3.setText("Browse");
        jButton3.setPreferredSize(new java.awt.Dimension(73, 27));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Browse");
        jButton4.setPreferredSize(new java.awt.Dimension(73, 27));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("Browse");
        jButton5.setPreferredSize(new java.awt.Dimension(73, 27));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        workingDirLabel.setEditable(false);
        workingDirLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        workingDirLabel.setForeground(new java.awt.Color(102, 102, 102));
        workingDirLabel.setText("This should be where your smd files are.");
        workingDirLabel.setPreferredSize(new java.awt.Dimension(250, 27));

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel10.setText("Working Dir: ");
        jLabel10.setPreferredSize(new java.awt.Dimension(73, 27));

        gameDirLabel.setEditable(false);
        gameDirLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        gameDirLabel.setForeground(new java.awt.Color(102, 102, 102));
        gameDirLabel.setText("This should be Common\\Game\\Game\\");
            gameDirLabel.setPreferredSize(new java.awt.Dimension(250, 27));

            jLabel11.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jLabel11.setText("Game Dir: ");
            jLabel11.setPreferredSize(new java.awt.Dimension(73, 27));

            jLabel12.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jLabel12.setText("Compiler Dir:");
            jLabel12.setPreferredSize(new java.awt.Dimension(73, 27));

            gameDirLabel1.setEditable(false);
            gameDirLabel1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            gameDirLabel1.setForeground(new java.awt.Color(102, 102, 102));
            gameDirLabel1.setText("This should be Common\\Game\\Bin");
            gameDirLabel1.setPreferredSize(new java.awt.Dimension(250, 27));

            jButton8.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jButton8.setText("?");
            jButton8.setBorder(null);
            jButton8.setContentAreaFilled(false);
            jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
            jButton8.setPreferredSize(new java.awt.Dimension(24, 27));
            jButton8.setRolloverEnabled(false);
            jButton8.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton8ActionPerformed(evt);
                }
            });

            jButton9.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jButton9.setText("?");
            jButton9.setBorder(null);
            jButton9.setContentAreaFilled(false);
            jButton9.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
            jButton9.setPreferredSize(new java.awt.Dimension(24, 27));
            jButton9.setRolloverEnabled(false);
            jButton9.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton9ActionPerformed(evt);
                }
            });

            jButton10.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jButton10.setText("?");
            jButton10.setBorder(null);
            jButton10.setContentAreaFilled(false);
            jButton10.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
            jButton10.setPreferredSize(new java.awt.Dimension(24, 27));
            jButton10.setRolloverEnabled(false);
            jButton10.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton10ActionPerformed(evt);
                }
            });

            jButton11.setText("Save");
            jButton11.setPreferredSize(new java.awt.Dimension(73, 27));
            jButton11.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton11ActionPerformed(evt);
                }
            });

            jLabel16.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jLabel16.setForeground(new java.awt.Color(153, 153, 153));
            jLabel16.setText("Version 0.2");

            jLabel17.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jLabel17.setForeground(new java.awt.Color(153, 153, 153));
            jLabel17.setText("Created by arleitiss");

            options_logo.setPreferredSize(new java.awt.Dimension(128, 128));

            jLabel15.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
            jLabel15.setText("Settings");

            jLabel18.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jLabel18.setForeground(new java.awt.Color(153, 153, 153));
            jLabel18.setText("(Click on Question Marks ? to Show More Info.)");

            javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
            jFrame1.getContentPane().setLayout(jFrame1Layout);
            jFrame1Layout.setHorizontalGroup(
                jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jFrame1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jFrame1Layout.createSequentialGroup()
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jFrame1Layout.createSequentialGroup()
                                    .addComponent(gameDirLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                        .addGroup(jFrame1Layout.createSequentialGroup()
                            .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jFrame1Layout.createSequentialGroup()
                                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(workingDirLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jFrame1Layout.createSequentialGroup()
                                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(gameDirLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jFrame1Layout.createSequentialGroup()
                                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(jFrame1Layout.createSequentialGroup()
                                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGroup(jFrame1Layout.createSequentialGroup()
                            .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFrame1Layout.createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel16)
                                        .addComponent(jLabel17)))
                                .addGroup(jFrame1Layout.createSequentialGroup()
                                    .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel18)
                                        .addComponent(jLabel15))
                                    .addGap(0, 0, Short.MAX_VALUE)))
                            .addGap(18, 18, 18)
                            .addComponent(options_logo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap())
            );
            jFrame1Layout.setVerticalGroup(
                jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jFrame1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jFrame1Layout.createSequentialGroup()
                            .addComponent(jLabel15)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel18)
                            .addGap(18, 18, 18)
                            .addComponent(jLabel16)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel17))
                        .addComponent(options_logo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(workingDirLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(gameDirLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(gameDirLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(27, 27, 27)
                    .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(163, Short.MAX_VALUE))
            );

            setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
            setTitle("QCGen");
            setLocationByPlatform(true);
            setResizable(false);

            jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Main Settings"));

            output_location_field.setEditable(false);
            output_location_field.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            output_location_field.setForeground(new java.awt.Color(51, 51, 51));
            output_location_field.setText("Should be Game\\Game\\Models");
            output_location_field.setToolTipText("");
            output_location_field.setPreferredSize(new java.awt.Dimension(300, 27));

            output_browse.setText("Browse");
            output_browse.setPreferredSize(new java.awt.Dimension(73, 27));
            output_browse.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    output_browseActionPerformed(evt);
                }
            });

            jLabel1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jLabel1.setText("Output Location:");
            jLabel1.setPreferredSize(new java.awt.Dimension(85, 27));

            jLabel2.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jLabel2.setText("Output Name:");
            jLabel2.setPreferredSize(new java.awt.Dimension(85, 27));

            output_name_field.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            output_name_field.setText("MyModel");
            output_name_field.setPreferredSize(new java.awt.Dimension(100, 27));
            output_name_field.addInputMethodListener(new java.awt.event.InputMethodListener() {
                public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                }
                public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                    output_name_fieldInputMethodTextChanged(evt);
                }
            });
            output_name_field.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    output_name_fieldActionPerformed(evt);
                }
            });

            jLabel3.setText(".mdl");
            jLabel3.setPreferredSize(new java.awt.Dimension(73, 27));

            jLabel4.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jLabel4.setText("Reference SMD:");
            jLabel4.setPreferredSize(new java.awt.Dimension(85, 27));

            ref_field.setEditable(false);
            ref_field.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            ref_field.setForeground(new java.awt.Color(51, 51, 51));
            ref_field.setText("Should be the exported .smd file.");
            ref_field.setPreferredSize(new java.awt.Dimension(300, 27));

            browse_ref.setText("Browse");
            browse_ref.setPreferredSize(new java.awt.Dimension(73, 27));
            browse_ref.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    browse_refActionPerformed(evt);
                }
            });

            jLabel5.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jLabel5.setText("Materials Location:");
            jLabel5.setPreferredSize(new java.awt.Dimension(85, 27));

            materials_field.setEditable(false);
            materials_field.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            materials_field.setForeground(new java.awt.Color(51, 51, 51));
            materials_field.setText("Should be Game\\Game\\Materials");
            materials_field.setPreferredSize(new java.awt.Dimension(300, 27));

            materials_browse.setText("Browse");
            materials_browse.setPreferredSize(new java.awt.Dimension(73, 27));
            materials_browse.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    materials_browseActionPerformed(evt);
                }
            });

            jLabel6.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jLabel6.setText("Surface Type:");
            jLabel6.setPreferredSize(new java.awt.Dimension(85, 27));

            surface.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default", "default_silent", "floatingstandable", "item", "ladder", "no_decal", "player", "player_control_clip", "brick", "concrete", "concrete_block", "gravel", "rock", "canister", "chain", "chainlink", "combine_metal", "crowbar", "floating_metal_barrel", "grenade", "gunship", "metal", "metal_barrel", "metal_bouncy", "Metal_Box", "metal_seafloorcar", "metalgrate", "metalpanel", "metalvent", "metalvehicle", "paintcan", "popcan", "roller", "slipperymetal", "solidmetal", "strider", "weapon", "wood", "Wood_Box", "Wood_Furniture", "Wood_Plank", "Wood_Panel", "Wood_Solid", "dirt", "grass", "gravel", "mud", "quicksand", "sand", "slipperyslime", "antlionsand", "slime", "water", "wade", "ice", "snow", "alienflesh", "antlion", "armorflesh", "bloodyflesh", "flesh", "foliage", "watermelon", "zombieflesh", "glass", "glassbottle", "combine_glass", "tile", "paper", "papercup", "cardboard", "plaster", "plastic_barrel", "plastic_barrel_buoyant", "Plastic_Box", "plastic", "rubber", "rubbertire", "slidingrubbertire", "slidingrubbertire_front", "slidingrubbertire_rear", "jeeptire", "brakingrubbertire", "carpet", "ceiling_tile", "computer", "pottery" }));
            surface.setMinimumSize(new java.awt.Dimension(80, 20));
            surface.setPreferredSize(new java.awt.Dimension(150, 27));

            jLabel7.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jLabel7.setText("Collision SMD:");
            jLabel7.setPreferredSize(new java.awt.Dimension(85, 27));

            collision_field.setEditable(false);
            collision_field.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            collision_field.setForeground(new java.awt.Color(51, 51, 51));
            collision_field.setText("Should be the exported .smd collision file.");
            collision_field.setPreferredSize(new java.awt.Dimension(300, 27));

            collision_browse.setText("Browse");
            collision_browse.setPreferredSize(new java.awt.Dimension(73, 27));
            collision_browse.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    collision_browseActionPerformed(evt);
                }
            });

            jLabel8.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jLabel8.setText("Scale:");
            jLabel8.setPreferredSize(new java.awt.Dimension(85, 27));

            jSlider1.setMinimum(1);
            jSlider1.setPaintLabels(true);
            jSlider1.setValue(10);
            jSlider1.setPreferredSize(new java.awt.Dimension(300, 27));

            scaleField.setEditable(false);
            scaleField.setForeground(new java.awt.Color(51, 51, 51));
            scaleField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
            scaleField.setText("1");
            scaleField.setPreferredSize(new java.awt.Dimension(73, 27));

            jLabel9.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jLabel9.setText("Mass:");
            jLabel9.setPreferredSize(new java.awt.Dimension(85, 27));

            jSlider2.setMaximum(300);
            jSlider2.setMinimum(1);
            jSlider2.setPaintLabels(true);
            jSlider2.setValue(25);
            jSlider2.setPreferredSize(new java.awt.Dimension(300, 27));

            massField.setEditable(false);
            massField.setForeground(new java.awt.Color(51, 51, 51));
            massField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
            massField.setText("25");
            massField.setPreferredSize(new java.awt.Dimension(73, 27));

            jCheckBox1.setText("Auto");
            jCheckBox1.setRolloverEnabled(false);

            jCheckBox2.setText("Auto");
            jCheckBox2.setRolloverEnabled(false);
            jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jCheckBox2ActionPerformed(evt);
                }
            });

            jButton12.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jButton12.setText("?");
            jButton12.setBorder(null);
            jButton12.setContentAreaFilled(false);
            jButton12.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
            jButton12.setPreferredSize(new java.awt.Dimension(15, 27));
            jButton12.setRolloverEnabled(false);
            jButton12.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton12ActionPerformed(evt);
                }
            });

            jButton13.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jButton13.setText("?");
            jButton13.setBorder(null);
            jButton13.setContentAreaFilled(false);
            jButton13.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
            jButton13.setPreferredSize(new java.awt.Dimension(15, 27));
            jButton13.setRolloverEnabled(false);
            jButton13.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton13ActionPerformed(evt);
                }
            });

            jButton14.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jButton14.setText("?");
            jButton14.setBorder(null);
            jButton14.setContentAreaFilled(false);
            jButton14.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
            jButton14.setPreferredSize(new java.awt.Dimension(15, 27));
            jButton14.setRolloverEnabled(false);
            jButton14.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton14ActionPerformed(evt);
                }
            });

            jButton15.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jButton15.setText("?");
            jButton15.setBorder(null);
            jButton15.setContentAreaFilled(false);
            jButton15.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
            jButton15.setPreferredSize(new java.awt.Dimension(15, 27));
            jButton15.setRolloverEnabled(false);
            jButton15.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton15ActionPerformed(evt);
                }
            });

            jButton16.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jButton16.setText("?");
            jButton16.setBorder(null);
            jButton16.setContentAreaFilled(false);
            jButton16.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
            jButton16.setPreferredSize(new java.awt.Dimension(15, 27));
            jButton16.setRolloverEnabled(false);
            jButton16.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton16ActionPerformed(evt);
                }
            });

            jButton17.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jButton17.setText("?");
            jButton17.setBorder(null);
            jButton17.setContentAreaFilled(false);
            jButton17.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
            jButton17.setPreferredSize(new java.awt.Dimension(15, 27));
            jButton17.setRolloverEnabled(false);
            jButton17.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton17ActionPerformed(evt);
                }
            });

            jButton18.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jButton18.setText("?");
            jButton18.setBorder(null);
            jButton18.setContentAreaFilled(false);
            jButton18.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
            jButton18.setPreferredSize(new java.awt.Dimension(15, 27));
            jButton18.setRolloverEnabled(false);
            jButton18.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton18ActionPerformed(evt);
                }
            });

            jButton19.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jButton19.setText("?");
            jButton19.setBorder(null);
            jButton19.setContentAreaFilled(false);
            jButton19.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
            jButton19.setPreferredSize(new java.awt.Dimension(15, 27));
            jButton19.setRolloverEnabled(false);
            jButton19.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton19ActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(ref_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(browse_ref, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(materials_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(materials_browse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(surface, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(scaleField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(output_location_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(output_browse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(output_name_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jCheckBox2))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(collision_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(collision_browse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(massField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jButton18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jCheckBox1))
                                .addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addContainerGap(23, Short.MAX_VALUE))
            );
            jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(output_location_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(output_browse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(output_name_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(scaleField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBox2)
                            .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(ref_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(browse_ref, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(materials_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(materials_browse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(collision_field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(collision_browse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(massField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBox1)
                            .addComponent(jButton18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(surface, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("(Optional) Extra"));

            default_check.setText("Default Collision");
            default_check.setPreferredSize(new java.awt.Dimension(105, 27));

            collision_check.setText("No Collisions");
            collision_check.setPreferredSize(new java.awt.Dimension(105, 27));

            collision_check1.setText("Static Prop");
            collision_check1.setPreferredSize(new java.awt.Dimension(105, 27));

            jButton20.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jButton20.setText("?");
            jButton20.setBorder(null);
            jButton20.setContentAreaFilled(false);
            jButton20.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
            jButton20.setPreferredSize(new java.awt.Dimension(15, 27));
            jButton20.setRolloverEnabled(false);
            jButton20.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton20ActionPerformed(evt);
                }
            });

            jButton21.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jButton21.setText("?");
            jButton21.setBorder(null);
            jButton21.setContentAreaFilled(false);
            jButton21.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
            jButton21.setPreferredSize(new java.awt.Dimension(15, 27));
            jButton21.setRolloverEnabled(false);
            jButton21.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton21ActionPerformed(evt);
                }
            });

            jButton22.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jButton22.setText("?");
            jButton22.setBorder(null);
            jButton22.setContentAreaFilled(false);
            jButton22.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
            jButton22.setPreferredSize(new java.awt.Dimension(15, 27));
            jButton22.setRolloverEnabled(false);
            jButton22.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton22ActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
            jPanel2.setLayout(jPanel2Layout);
            jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(default_check, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(collision_check, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(collision_check1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(jButton22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(default_check, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(collision_check, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(collision_check1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Log"));

            log.setEditable(false);
            log.setBackground(new java.awt.Color(240, 240, 240));
            log.setColumns(20);
            log.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
            log.setLineWrap(true);
            log.setRows(5);
            log.setBorder(null);
            log.setVerifyInputWhenFocusTarget(false);
            jScrollPane1.setViewportView(log);

            jButton23.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jButton23.setText("x");
            jButton23.setBorder(null);
            jButton23.setContentAreaFilled(false);
            jButton23.setFocusPainted(false);
            jButton23.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
            jButton23.setPreferredSize(new java.awt.Dimension(15, 27));
            jButton23.setRolloverEnabled(false);
            jButton23.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            jButton23.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
            jButton23.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton23ActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
            jPanel3.setLayout(jPanel3Layout);
            jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            );
            jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addComponent(jButton23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE))
            );

            jButton1.setText("Save to File");
            jButton1.setPreferredSize(new java.awt.Dimension(120, 30));
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton1ActionPerformed(evt);
                }
            });

            jButton2.setText("Copy to Clipboard");
            jButton2.setPreferredSize(new java.awt.Dimension(120, 30));
            jButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton2ActionPerformed(evt);
                }
            });

            error.setForeground(new java.awt.Color(255, 0, 51));
            error.setPreferredSize(new java.awt.Dimension(253, 14));

            jButton6.setForeground(new java.awt.Color(51, 51, 51));
            jButton6.setText("Compile (Beta)");
            jButton6.setPreferredSize(new java.awt.Dimension(120, 30));
            jButton6.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton6ActionPerformed(evt);
                }
            });

            jButton7.setText("Settings");
            jButton7.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton7ActionPerformed(evt);
                }
            });

            jLabel13.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
            jLabel13.setText("QCGen");

            jLabel14.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
            jLabel14.setForeground(new java.awt.Color(153, 153, 153));
            jLabel14.setText("(Click on Question Marks ? to Show More Info.)");

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel13)
                                        .addComponent(jLabel14))
                                    .addGap(0, 0, Short.MAX_VALUE)))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(18, 18, 18)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(error, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addComponent(jButton7)))
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(77, 77, 77)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGap(7, 7, 7)))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButton7)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel13)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel14)))
                    .addGap(26, 26, 26)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(error, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap())
            );

            pack();
        }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
       JFileChooser f = new JFileChooser("C:");
        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
        f.showDialog(jFrame1, "Select");
        String selectedPath = String.valueOf(f.getSelectedFile());
        if(!selectedPath.equals("null")){
            fcs.setSetting("workingDir", selectedPath);
            workingDirLabel.setText(selectedPath);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        JFileChooser f = new JFileChooser("C:");
        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
        f.showDialog(jFrame1, "Select");
        String selectedPath = String.valueOf(f.getSelectedFile());
        if(!selectedPath.equals("null")){
            fcs.setSetting("gameDir", selectedPath);
            gameDirLabel.setText(selectedPath);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void output_browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_output_browseActionPerformed
        JFileChooser f = new JFileChooser(fcs.getSetting("gameDir", "C:"));
        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
        f.showDialog(jLabel1, "Set Destination");
        String selectedPath = String.valueOf(f.getSelectedFile());
        if(!selectedPath.equals("null")){
            boolean status = selectedPath.matches("(?i)[^;]*[\\\\|\\/]\\b(steamapps)\\b[\\\\|\\/]\\b(common)[\\\\|\\/][^;]*[\\\\|\\/]\\b(models)\\b[^;]*");
            if(status == true){
            error.setText("");
            String[] newPath = selectedPath.split("\\\\models\\\\", 2);
            if(newPath.length == 2){
            output_location_field.setText("[GameDirectory]\\models\\"+newPath[1]+"\\");
            modelFinalPath = newPath[1]+"\\";
            }
            else if(newPath.length == 1){
            output_location_field.setText("[GameDirectory]\\models\\");
            modelFinalPath = "";
            }
            }
            else{
                error.setText("Invalid output path. Must be game/game/models/");
            }
        }
        //output_location_field
    }//GEN-LAST:event_output_browseActionPerformed

    private void output_name_fieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_output_name_fieldActionPerformed

    }//GEN-LAST:event_output_name_fieldActionPerformed

    private void output_name_fieldInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_output_name_fieldInputMethodTextChanged
       
    }//GEN-LAST:event_output_name_fieldInputMethodTextChanged

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void browse_refActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browse_refActionPerformed
        error.setText("");
        JFileChooser f = new JFileChooser(fcs.getSetting("workingDir", "C:"));
        f.showOpenDialog(null);
        String selectedPath = String.valueOf(f.getSelectedFile());
        if(!selectedPath.equals("null")){
        String ex = selectedPath.substring(selectedPath.lastIndexOf("."), selectedPath.length());
        if(ex.toLowerCase().equals(".smd")){
            refPath = selectedPath;
            ref_field.setText(refPath);
            smdFileName = selectedPath.substring(selectedPath.lastIndexOf("\\")+1, selectedPath.length()-4);
        }
        else{
            error.setText("Wrong File Type. Must be smd/SMD.");
        }
        }
    }//GEN-LAST:event_browse_refActionPerformed

    private void materials_browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_materials_browseActionPerformed
JFileChooser f = new JFileChooser(fcs.getSetting("gameDir", "C:"));
        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
        f.showDialog(jLabel1, "Set as Materials Dir");
        String selectedPath = String.valueOf(f.getSelectedFile());
        if(!selectedPath.equals("null")){
            boolean status = selectedPath.matches("(?i)[^;]*[\\\\|\\/]\\b(steamapps)\\b[\\\\|\\/]\\b(common)[\\\\|\\/][^;]*[\\\\|\\/]\\b(materials)\\b[^;]*");
            if(status == true){
            error.setText("");
            String[] newPath = selectedPath.split("\\\\materials\\\\", 2);
            if(newPath.length == 2){
            materials_field.setText("[GameDirectory]\\materials\\"+newPath[1]+"\\");
            materialsFinalPath = newPath[1];
            }
            else if(newPath.length == 1){
            materials_field.setText("[GameDirectory]\\materials\\");
            materialsFinalPath = "";
            }
            }
            else{
                error.setText("Invalid materials path. Must be game\\game\\materials.");
            }
        }
    }//GEN-LAST:event_materials_browseActionPerformed

    private void collision_browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_collision_browseActionPerformed
        error.setText("");
        JFileChooser f = new JFileChooser(fcs.getSetting("workingDir", "C:"));
        f.showOpenDialog(null);
        String selectedPath = String.valueOf(f.getSelectedFile());
        if(!selectedPath.equals("null")){
        String ex = selectedPath.substring(selectedPath.lastIndexOf("."), selectedPath.length());
        if(ex.toLowerCase().equals(".smd")){
            colPath = selectedPath;
            collision_field.setText(colPath);
        }
        else{
            error.setText("Wrong File Type. Must be smd/SMD.");
        }
        }
    }//GEN-LAST:event_collision_browseActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        genQC(1);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        genQC(3);
        
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    genQC(2);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        JFileChooser f = new JFileChooser(fcs.getSetting("gameDir", "C:"));
        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
        f.showDialog(jFrame1, "Set as Comp. Path");
        String selectedPath = String.valueOf(f.getSelectedFile());
        if(!selectedPath.equals("null")){
            fcs.setSetting("compDir", selectedPath);
            gameDirLabel1.setText(selectedPath);
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        jFrame1.pack();
        jFrame1.setLocationRelativeTo(null);
        jFrame1.setVisible(true);
        workingDirLabel.setText(fcs.getSetting("workingDir", "Unset"));
        gameDirLabel.setText(fcs.getSetting("gameDir", "Unset"));
        gameDirLabel1.setText(fcs.getSetting("compDir", "Unset"));
        try{
        Image is = ImageIO.read(getClass().getResource("logo.png"));
        ImageIcon icon = new ImageIcon(is); 
            options_logo.setIcon(icon);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        JOptionPane.showMessageDialog(jFrame1, "This is your working dir, it does not affect QC in any way, this is merely for quicker SMD file browsing.");
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        JOptionPane.showMessageDialog(jFrame1, "This is your game directory (Garry's Mod, Ep2, TF2, CSS etc..) this does not affect QC in any way, this is merely for quicker materials/models browsing. If you wish to use compiler - you need to set this to be same folder as gameinfo.txt is located in.");

    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        JOptionPane.showMessageDialog(jFrame1, "This should be set to where ever studiomdl.exe is located.");

    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        jFrame1.setVisible(false);
        checkComp();
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
        log.setText("");
    }//GEN-LAST:event_jButton23ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        JOptionPane.showMessageDialog(this, "This is where .mdl file will be compiled. (Your final model location)");
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        JOptionPane.showMessageDialog(this, "This will be your final models name. It will be referenced by game as such.)");
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        JOptionPane.showMessageDialog(this, "Scale of model in game. 1 is original model size. >1 will make it bigger <1 will make it smaller. Auto will set it to 1.");
    }//GEN-LAST:event_jButton14ActionPerformed

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        JOptionPane.showMessageDialog(this, "This is body/reference SMD file. The main model file. Ideally this should NOT contain spaces.");
    }//GEN-LAST:event_jButton15ActionPerformed

    private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
        JOptionPane.showMessageDialog(this, "This is where vmt files for your model are located. Should be game\\game\\materials\\");
    }//GEN-LAST:event_jButton16ActionPerformed

    private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
        JOptionPane.showMessageDialog(this, "This is your collision model SMD. If you haven't made one or prefer to auto generate it - tick \"Default Collision\" or leave blank.");
    }//GEN-LAST:event_jButton17ActionPerformed

    private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
        JOptionPane.showMessageDialog(this, "This is mass of the object/model in Kg. Tick auto for compiler to automatically calculate it based on size and surface prop.");
    }//GEN-LAST:event_jButton18ActionPerformed

    private void jButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton19ActionPerformed
        JOptionPane.showMessageDialog(this, "This is material/behavior of model in game. e.g - if it's wood -> It will float in water etc..");
    }//GEN-LAST:event_jButton19ActionPerformed

    private void jButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton20ActionPerformed
        JOptionPane.showMessageDialog(this, "This will skip any collision models referenced by you and let engine automatically generate one. Good for convex props, wont work well with concave types.");
    }//GEN-LAST:event_jButton20ActionPerformed

    private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
        JOptionPane.showMessageDialog(this, "No collisions, model will be visible but passable-through. e.g - effects.");
    }//GEN-LAST:event_jButton21ActionPerformed

    private void jButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton22ActionPerformed
        JOptionPane.showMessageDialog(this, "Make prop static, good if you are modelling furniture for your map. In game static props can still be moved but not so affected by physics.");
    }//GEN-LAST:event_jButton22ActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(qGenGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(qGenGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(qGenGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(qGenGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new qGenGUI().setVisible(true);
        
            }
        });
              
        
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browse_ref;
    private javax.swing.JButton collision_browse;
    private javax.swing.JCheckBox collision_check;
    private javax.swing.JCheckBox collision_check1;
    private javax.swing.JTextField collision_field;
    private javax.swing.JCheckBox default_check;
    private javax.swing.JLabel error;
    private javax.swing.JTextField gameDirLabel;
    private javax.swing.JTextField gameDirLabel1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JSlider jSlider2;
    private javax.swing.JTextArea log;
    private javax.swing.JTextField massField;
    private javax.swing.JButton materials_browse;
    private javax.swing.JTextField materials_field;
    private javax.swing.JLabel options_logo;
    private javax.swing.JButton output_browse;
    private javax.swing.JTextField output_location_field;
    private javax.swing.JTextField output_name_field;
    private javax.swing.JTextField ref_field;
    private javax.swing.JTextField scaleField;
    private javax.swing.JComboBox surface;
    private javax.swing.JTextField workingDirLabel;
    // End of variables declaration//GEN-END:variables
}
