import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextField;

/*
 * @author Rosheen C.
 * @author David R.
 * @author Julie D.
 */
public class torrentGUI extends javax.swing.JFrame {

    public boolean programStarted = false; 
    
    static int downloadedPieces = 0; 
    
    
    
    /**
     * Creates new form torrentGUI
     */
    public torrentGUI() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        startButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        downloadLabel = new javax.swing.JLabel();
        uploadLabel = new javax.swing.JLabel();
        downloadDisplay = new javax.swing.JTextField();
        uploadDisplay = new javax.swing.JTextField();
        timeLabel = new javax.swing.JLabel();
        timeDisplay = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        startButton.setText("START");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        stopButton.setText("STOP");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        clearButton.setText("CLEAR");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        downloadLabel.setText("Downloaded:");

        uploadLabel.setText("Uploaded:");

        downloadDisplay.setText("0");
        downloadDisplay.setEditable(false);

        uploadDisplay.setText("0");
        uploadDisplay.setEditable(false);

        timeLabel.setText("Time: ");

        timeDisplay.setEditable(false);
        timeDisplay.setText("0.0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(clearButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(stopButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(startButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(41, 41, 41)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(timeLabel)
                    .addComponent(downloadLabel)
                    .addComponent(uploadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(downloadDisplay, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                    .addComponent(uploadDisplay)
                    .addComponent(timeDisplay))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startButton)
                    .addComponent(uploadLabel)
                    .addComponent(uploadDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stopButton)
                    .addComponent(downloadLabel)
                    .addComponent(downloadDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clearButton)
                    .addComponent(timeLabel)
                    .addComponent(timeDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>                        

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        if(!programStarted){
            try {
                RUBTClient.begin();
            } catch (IOException ex) {
                Logger.getLogger(torrentGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NullPointerException ex) {
                Logger.getLogger(torrentGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (BencodingException ex) {
                Logger.getLogger(torrentGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(torrentGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        startButton.setEnabled(false);
        programStarted = true;
       }
    }                                           

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           

          if(programStarted){
        	for(int i = 0; i < RUBTClient.connectedPeers.size(); i++)
        	{
        		RUBTClient.connectedPeers.get(i).th.stop();
        		stopButton.setEnabled(false);
        	}
                updateTime();
        }
    }                                          

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        uploadDisplay.setText("0");
        downloadDisplay.setText("0");
        //timeDisplay.setText("0");
    }                                           

    public static void updateDownload(){
        String display = Integer.toString(RUBTClient.downloaded);
        downloadDisplay.setText(display);
        
    }
    
    public static void updateUpload(){
        String display = Integer.toString(RUBTClient.uploaded);
        uploadDisplay.setText(display);
    }
    
    public static void updateTime(){
        String display = Double.toString(RUBTClient.downloadTime);
        //timeDisplay.setText(display);
    }
    /**
     * @param args the command line arguments
     */
    public static void startGUI() {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(torrentGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(torrentGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(torrentGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(torrentGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new torrentGUI().setVisible(true);
            }
        });
    }//end of main

    // Variables declaration - do not modify                     
    private javax.swing.JButton clearButton;
    public static javax.swing.JTextField downloadDisplay;
    private javax.swing.JLabel downloadLabel;
    private javax.swing.JButton startButton;
    private javax.swing.JButton stopButton;
    public static javax.swing.JTextField timeDisplay;
    private javax.swing.JLabel timeLabel;
    public static javax.swing.JTextField uploadDisplay;
    private javax.swing.JLabel uploadLabel;
    // End of variables declaration                   

}
