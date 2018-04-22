import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;

public class AgentPlayer extends JFrame implements ActionListener {

    private JLabel environmentLabel;
    private ImageIcon environmentImage;
    private JLabel agentLabel;
    private ImageIcon agentImage;
    private JLabel sequenceToTryLabel;
    private JLabel sensorLabel;
    private JTextPane messageLabel;
    private JButton playButton;
    private JCheckBox skipToGoalCheckbox;
    private BufferedImage currentAgentImage;
    private BufferedImage currentEnvironmentImage;
    private JCheckBox renderEnvironmentCheckbox;

    private Sequence sequence;
    private int currentActionIndex = 0;

    private BufferedReader bufferedReader;

    private BorderTarget currentBorder;

    public AgentPlayer(String playfilePath) throws IOException {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container pane = getContentPane();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        pane.add(buttonPanel, BorderLayout.PAGE_START);

        JButton exportImageButton = new JButton("Save Agent Image");
        exportImageButton.addActionListener(this);
        buttonPanel.add(exportImageButton);

        exportImageButton = new JButton("Save Environment Image");
        exportImageButton.addActionListener(this);
        buttonPanel.add(exportImageButton);

        JButton button = new JButton("Step");
        button.addActionListener(this);
        buttonPanel.add(button);

        this.playButton = new JButton("Play");
        this.playButton.addActionListener(this);
        buttonPanel.add(this.playButton);

        this.skipToGoalCheckbox = new JCheckBox("Skip to goal");
        buttonPanel.add(this.skipToGoalCheckbox);

        this.renderEnvironmentCheckbox = new JCheckBox("Render environment");
        this.renderEnvironmentCheckbox.setSelected(true);
        buttonPanel.add(this.renderEnvironmentCheckbox);

        this.messageLabel = new JTextPane();
        JScrollPane scrollPane = new JScrollPane(this.messageLabel);
        pane.add(scrollPane, BorderLayout.LINE_START);

        this.agentImage = new ImageIcon();
        this.agentLabel = new JLabel(this.agentImage);
        JScrollPane scrollPaneForAgent = new JScrollPane(this.agentLabel);

        this.environmentImage = new ImageIcon();
        this.environmentLabel = new JLabel(this.environmentImage);
        JScrollPane scrollPaneForEnvironment = new JScrollPane(this.environmentLabel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPaneForAgent, scrollPaneForEnvironment);
        pane.add(splitPane, BorderLayout.CENTER);

        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new FlowLayout());
        pane.add(dataPanel, BorderLayout.PAGE_END);

        this.sequenceToTryLabel = new JLabel();
        dataPanel.add(this.sequenceToTryLabel);

        this.sensorLabel = new JLabel();
        dataPanel.add(this.sensorLabel);

        File file = new File(playfilePath);
        FileReader fileReader = new FileReader(file);
        bufferedReader = new BufferedReader(fileReader);

        setSize(800, 600);
        this.pack();
    }

    private void updateBorder()
    {
        switch (this.currentBorder) {
            case Agent:
                this.agentLabel.setBorder(BorderFactory.createLineBorder(Color.red));
                this.environmentLabel.setBorder(null);
                this.messageLabel.setBorder(null);
                break;
            case Environment:
                this.agentLabel.setBorder(null);
                this.environmentLabel.setBorder(BorderFactory.createLineBorder(Color.red));
                this.messageLabel.setBorder(null);
                break;
            case Message:
                this.agentLabel.setBorder(null);
                this.environmentLabel.setBorder(null);
                this.messageLabel.setBorder(BorderFactory.createLineBorder(Color.red));
                break;
        }
    }

    private void playNext(boolean doSkip) throws IOException {
        String currentLine = bufferedReader.readLine();

        System.out.println(currentLine);
        if (currentLine == null) {
            if (playTimer.isRunning())
                playTimer.stop();
            return;
        }

        int separator = currentLine.indexOf(':');
        String item = currentLine.substring(0, separator);
        String content = currentLine.substring(separator + 1);
        switch (item)
        {
            case "AGENT":
                this.currentBorder = BorderTarget.Agent;
                this.updateBorder();
                this.currentAgentImage = generateGraph(content);
                agentImage.setImage(this.currentAgentImage);
                this.playNext(doSkip);
                break;
            case "SEQUENCE":
                this.sequence = new Sequence(content);
                sequenceToTryLabel.setText(this.sequence.toString());
                break;
            case "ENVIRONMENT":
                if (doSkip)
                {
                    while (currentLine != null && !currentLine.contains("Goal [fillcolor = gray, style = filled];"))
                    {
                        currentLine = bufferedReader.readLine();
                        System.out.println(currentLine);
                    }
                    separator = currentLine.indexOf(':');
                    content = currentLine.substring(separator + 1);
                }
                this.currentBorder = BorderTarget.Environment;
                this.updateBorder();
                if (this.renderEnvironmentCheckbox.isSelected()) {
                    this.currentEnvironmentImage = generateGraph(content);
                    this.environmentImage.setImage(this.currentEnvironmentImage);
                }
                this.playNext(doSkip);
                break;
            case "SENSORS":
                sensorLabel.setText(content);
                sequenceToTryLabel.setText(this.sequence.next());
                break;
            case "MESSAGE":
                this.currentBorder = BorderTarget.Message;
                this.messageLabel.setText(this.messageLabel.getText() + "\n" + content);
                this.updateBorder();
                this.playNext(doSkip);
                break;
        }
    }

    private BufferedImage generateGraph(String dot) throws IOException {
        MutableGraph environment = Parser.read(dot);
        Graphviz viz = Graphviz.fromGraph(environment);
        return viz.render(Format.PNG).toImage();
    }

    private Timer playTimer = new Timer(100, this);

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == "Play")
        {
            this.playTimer.start();
            this.playButton.setText("Stop");
        }
        else if (e.getActionCommand() == "Stop")
        {
            this.playTimer.stop();
            this.playButton.setText("Play");
        }
        else if (e.getActionCommand() == "Save Agent Image")
        {
           this.saveImage(this.currentAgentImage);
        }
        else if (e.getActionCommand() == "Save Environment Image")
        {
            this.saveImage(this.currentEnvironmentImage);
        }
        else {
            try {
                this.playNext(this.skipToGoalCheckbox.isSelected());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void saveImage(BufferedImage image)
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG", "png"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            File file = fileChooser.getSelectedFile();
            String fileName = file.getName();
            if (!fileName.toUpperCase().endsWith("PNG")) {
                file = new File(file.getParent() + "\\" + fileName + ".png");
            }
            try {
                ImageIO.write(image, "png", file);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        try {
            AgentPlayer player = new AgentPlayer("agent.playfile");
            player.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class Sequence {
        private String sequence;
        private int currentIndex = 0;
        public Sequence(String sequenceToTrack)
        {
            sequence = sequenceToTrack.trim();
        }

        public String next()
        {
            currentIndex++;
            return this.toString();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(this.sequence);
            builder.insert(this.currentIndex, "    ");
            return builder.toString();
        }
    }
    private enum BorderTarget {
        Agent,
        Environment,
        Message,
    }
}
