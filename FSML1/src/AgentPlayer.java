import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

import javax.swing.*;

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

    private boolean isBorderOnAgent;
    private Sequence sequence;
    private int currentActionIndex = 0;

    private BufferedReader bufferedReader;

    public AgentPlayer(String playfilePath) throws IOException {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel container = new JPanel();
        getContentPane().add(new JScrollPane(container));
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        container.setLayout(gridbag);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JButton button = new JButton("Step");
        button.addActionListener(this);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        gridbag.setConstraints(button, constraints);
        container.add(button);

        button = new JButton("Play");
        button.addActionListener(this);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        gridbag.setConstraints(button, constraints);
        container.add(button);

        this.sequenceToTryLabel = new JLabel();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        gridbag.setConstraints(this.sequenceToTryLabel, constraints);
        container.add(this.sequenceToTryLabel);

        this.sensorLabel = new JLabel();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        gridbag.setConstraints(this.sensorLabel, constraints);
        container.add(this.sensorLabel);

        this.agentImage = new ImageIcon();
        this.agentLabel = new JLabel(this.agentImage);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        gridbag.setConstraints(agentLabel, constraints);
        container.add(agentLabel);

        this.environmentImage = new ImageIcon();
        this.environmentLabel = new JLabel(this.environmentImage);
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        gridbag.setConstraints(this.environmentLabel, constraints);
        container.add(this.environmentLabel);

        File file = new File(playfilePath);
        FileReader fileReader = new FileReader(file);
        bufferedReader = new BufferedReader(fileReader);

        setSize(800, 600);
        this.pack();
    }

    private void updateBorder()
    {
        if (this.isBorderOnAgent)
        {
            this.agentLabel.setBorder(BorderFactory.createLineBorder(Color.red));
            this.environmentLabel.setBorder(null);
        }
        else
        {
            this.agentLabel.setBorder(null);
            this.environmentLabel.setBorder(BorderFactory.createLineBorder(Color.red));

        }
    }

    private void playNext() throws IOException {
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
                this.isBorderOnAgent = true;
                this.updateBorder();
                agentImage.setImage(generateGraph(content));
                this.playNext();
                break;
            case "SEQUENCE":
                this.sequence = new Sequence(content);
                sequenceToTryLabel.setText(this.sequence.toString());
                break;
            case "ENVIRONMENT":
                this.isBorderOnAgent = false;
                this.updateBorder();
                environmentImage.setImage(generateGraph(content));
                this.playNext();
                break;
            case "SENSORS":
                sensorLabel.setText(content);
                sequenceToTryLabel.setText(this.sequence.next());
                break;
        }
    }

    private BufferedImage generateGraph(String dot) throws IOException {
        MutableGraph environment = Parser.read(dot);
        Graphviz viz = Graphviz.fromGraph(environment);
        return viz.render(Format.PNG).toImage();
    }

    private Timer playTimer = new Timer(250, this);

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == "Play")
        {
            if (playTimer.isRunning())
                playTimer.stop();
            else
                playTimer.start();
        }
        else {
            try {
                this.playNext();
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
}
