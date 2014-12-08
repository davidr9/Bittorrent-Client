import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

/*
 * @author Rosheen C.
 * @author David R.
 * @author Julie D.
 */
public class torrentGUI extends javax.swing.JFrame {

    	
    private javax.swing.JButton clearButton; //used to create clear button
    
    public static javax.swing.JTextField dlSpeedDisplay;//creates textfield to display download speed
    
    private javax.swing.JLabel dlSpeedLabel;//labels textfield for download speed
    
    public static javax.swing.JTextField downloadDisplay;//displays the bytes downloaded
    
    private javax.swing.JLabel downloadLabel;//labels textfield for bytes downloaded
    
    private javax.swing.JLabel peerConnectionsLabel;//labels textfield for peer window
    
    public static javax.swing.JTextField peerConnectionsWindow;//shows how many peers we are connected to
    
    public static javax.swing.JProgressBar progressBar;//shows how many pieces we have downloaded
    
    private javax.swing.JButton startButton;//starts the program when presses
    
    private javax.swing.JButton stopButton;//stops the download when presses
    
    public static javax.swing.JTextField timeDisplay;//displays the execution time of the program
    
    private javax.swing.JLabel timeLabel;//label for timeDisplay

    public boolean programStarted = false;//tracks if start button was clicled

    static int downloadedPieces = 0;//number of pieces we currently have downloaded
    
    public torrentGUI() {
        initComponents();
    }

    /*initializes all the components of the jFrame*/
    @SuppressWarnings("unchecked")
    private void initComponents() {

        startButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        downloadLabel = new javax.swing.JLabel();
        downloadDisplay = new javax.swing.JTextField();
        dlSpeedLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        dlSpeedDisplay = new javax.swing.JTextField();
        timeLabel = new javax.swing.JLabel();
        timeDisplay = new javax.swing.JTextField();
        peerConnectionsLabel = new javax.swing.JLabel();
        peerConnectionsWindow = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        startButton.setFont(new java.awt.Font("Ubuntu Medium", 1, 15));
        startButton.setText("START");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        stopButton.setFont(new java.awt.Font("Ubuntu Medium", 1, 15));
        stopButton.setText("STOP");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        clearButton.setFont(new java.awt.Font("Ubuntu Medium", 1, 15));
        clearButton.setText("CLEAR");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        downloadLabel.setFont(new java.awt.Font("Ubuntu", 1, 15));
        downloadLabel.setText("Downloaded:");

        downloadDisplay.setText("0");
        downloadDisplay.setEditable(false);

        dlSpeedLabel.setFont(new java.awt.Font("Ubuntu", 1, 15)); 
        dlSpeedLabel.setText("Download Speed:");

        progressBar.setBackground(new java.awt.Color(24, 12, 12));
        progressBar.setFont(new java.awt.Font("Ubuntu Medium", 1, 15)); 
        progressBar.setForeground(java.awt.Color.black);
        progressBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        progressBar.setOpaque(false);

        dlSpeedDisplay.setEditable(false);
        dlSpeedDisplay.setText("0");

        timeLabel.setFont(new java.awt.Font("Ubuntu", 1, 15)); 
        timeLabel.setText("Time:");

        timeDisplay.setEditable(false);
        timeDisplay.setText("0.0");

        peerConnectionsLabel.setFont(new java.awt.Font("Ubuntu", 1, 15));
        peerConnectionsLabel.setText("Peer Connections:");

        peerConnectionsWindow.setEditable(false);
        peerConnectionsWindow.setText("0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(clearButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(stopButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(startButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(41, 41, 41)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(downloadLabel)
                            .addComponent(dlSpeedLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(timeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(peerConnectionsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(53, 53, 53)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(timeDisplay, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(downloadDisplay)
                            .addComponent(dlSpeedDisplay)
                            .addComponent(peerConnectionsWindow))))
                .addContainerGap(47, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeLabel)
                    .addComponent(timeDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startButton))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(24, 24, 24)
                            .addComponent(stopButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                            .addComponent(downloadDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(20, 20, 20)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(downloadLabel)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(peerConnectionsWindow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(peerConnectionsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(clearButton))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(dlSpeedDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(dlSpeedLabel))))))
                .addGap(18, 18, 18)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        pack();
    }

    /*Listens for the event of the start button being clicked.
     *After the button is clicked, it initializes the textfields and 
     *progress bar to 0.*/
    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        ColorUIResource purple = new ColorUIResource(new Color(160, 0, 240));
        UIManager.put("nimbusOrange", purple);//sets progress bar to purple
        progressBar.setMinimum(0);
        if (!programStarted) {
            try {
                RUBTClient.begin();//begins the client thread
            } catch (IOException ex) {
                Logger.getLogger(torrentGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NullPointerException ex) {
                Logger.getLogger(torrentGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (BencodingException ex) {
                Logger.getLogger(torrentGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(torrentGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            programStarted = true;
            startButton.setEnabled(false);//once the start button is pressed, you cannot press it again until you exit the program
        }
    }                                           

    /*Listens for the event of the stop button being pressed.
     *Once pressed, the method stops all treachs and publishes to the tracker
     */
    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
        if (programStarted) {
            for (int i = 0; i < RUBTClient.getConnectedPeers().size(); i++) {//stops all threads 
                RUBTClient.getConnectedPeers().get(i).updateStopProgram();
                stopButton.setEnabled(false);//cannot press button again after it is clicked
            }
            System.out.println("Total time of download: " + RUBTClient.getDownloadTime() + " ms");
        }
        try {
                RUBTClient.publishToTracker("stopped");
                RUBTClient.writeToDisk(RUBTClient.getfName());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }                                          

    /*Waits for the event of the clear button being pressed.
    * Sets all the textFields and progress bars to the initial value.
    */
    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        downloadDisplay.setText("0");
        timeDisplay.setText("0");
        progressBar.setValue(0);
        peerConnectionsWindow.setText("0");
        RUBTClient.downloadRate = 0;
        RUBTClient.clientTimer.cancel();
        dlSpeedDisplay.setText("0");
    }                                           

    /*updates the download display*/
    public static void updateDownload() {
        String display = Integer.toString(RUBTClient.getDownloaded());
        downloadDisplay.setText(display + " B");
    }

    /*updates the time display*/
    public static void updateTime(double downloadTime) {
        String display = Double.toString(downloadTime);
        timeDisplay.setText(display + " s");
    }

    /*updates the progress bar based on the number of pieces downloaded*/
    public static void updateProgressBar() {
        progressBar.setMaximum(RUBTClient.getNumPieces());
        progressBar.setValue(RUBTClient.getNumPiecesVerified());
    }

    /*updates the number of connected peers into the display*/
    public static void updateConnectedPeers() {
        int peerNum = RUBTClient.getConnectedPeers().size();
        String output = Integer.toString(peerNum);
        peerConnectionsWindow.setText(output);
    }

    /*starts the instance of the GUI*/
    public static void startGUI() {
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

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new torrentGUI().setVisible(true);
            }
        });
    }
}
