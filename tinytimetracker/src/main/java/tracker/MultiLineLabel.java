// SPDX-License-Identifier: GPL-2.0-only
/*
 * Created on Apr 10, 2003
 *
 * Multi-Line Label - A label that can display multiple lines of text.
 */
package tracker;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

/**
 * @author rblack
 *
 * I got tired of using multiple labels when I wanted a label that spanned more than one line.  
 * This class allows multi-line labels.
 */
public class MultiLineLabel extends JPanel {
    
    private String text;
    private int maxWidth;

    /**
     * Constructs a MultiLineLabel where newlines are indicated by a \n
     * @param text The text of the label
     */
    public MultiLineLabel(String text) {
        this(text, Integer.MAX_VALUE);
    }
    /**
     * Constructs a MultiLineLabel where newlines are indicated by a \n.  
     * In addition, lines will not extend beyond maxWidth
     * @param text The text of the label
     * @param maxWidth The maximum length of a line
     */
    public MultiLineLabel(String text, int maxWidth) {
        super(new GridBagLayout());
        setOpaque(false);
        this.text = text;
        this.maxWidth = maxWidth;
        // same default font as labels
        setFont(new JLabel().getFont());
    }
    
    /**
     * @param i
     * @return
     */
    private void computeLines() {

        Graphics graphics = getGraphics();
        if (graphics == null || text == null) return;

        removeAll();

        Font f = getFont();
        Color fg = getForeground();
        Color bg = getBackground();

        FontMetrics metrics = graphics.getFontMetrics(f);
        List<String> l = wrapText(0, new ArrayList<String>(), metrics);
        String[] lines = (String[]) l.toArray(new String[l.size()]);
        
        for (int i = 0; i < lines.length; i++) {
            JLabel label =new JLabel(lines[i]);
            label.setFont(f);
            label.setForeground(fg);
            label.setBackground(bg);
            add (label, new GridBagConstraints(0,i,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
        }
        add(new JPanel(){
            /* (non-Javadoc)
             * @see javax.swing.JComponent#getPreferredSize()
             */
            public Dimension getPreferredSize() {
                return new Dimension(0,0);
            }
            
            public Dimension getMinimumSize() {
                return new Dimension(0,0);
            }

        }, new GridBagConstraints(1,lines.length,1,1,1,1,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0)); // spacer
    }
    
    public void setText(String text) {
        this.text = text;
        computeLines();
        validate();
    }
    
    public String getText() {
        return text;
    }
    
    /**
     * @param i
     * @return
     */
    private List<String> wrapText(int i, List<String> l, FontMetrics metrics) {
        int index = i;

        int lastSpaceIndex = -1;
        char[] chars = text.toCharArray();
        if (chars.length == 0) return l;
        
        boolean newline = false;
        for (;;index++) {
            char c = chars[index];
            if (Character.isWhitespace(c)) {
                lastSpaceIndex = index;
                if (c == '\n') newline = true;
            }
            if (index == chars.length - 1) {
                lastSpaceIndex = index + 1;
            }
            int stringWidth = metrics.stringWidth(new String(chars, i, index - i));
            if (stringWidth > maxWidth && lastSpaceIndex != -1 || index == chars.length - 1 || newline) {

                // add to the the last space
                String line = new String(chars, i, lastSpaceIndex -i); 
                //if (width > widestWord) widestWord = width;
                
                l.add(line);

                 // recurse if necessary
                 if (lastSpaceIndex + 1< chars.length)
                    wrapText(lastSpaceIndex + 1, l, metrics);
                
                 break;
            }

        }
        
        return l;
    }
    
    
    /* (non-Javadoc)
     * @see java.awt.Component#addNotify()
     */
    public void addNotify() {
        super.addNotify();
        computeLines();
    }

    public static void main(String[] args) {
        JFrame f=new JFrame();
        MultiLineLabel l = new MultiLineLabel("Helloooooooooooooooo World The QUick brown fox jumps over the lazy\n   dog", 100); //$NON-NLS-1$
        //l.setFont(new Font("Arial", Font.BOLD, 30));
        f.getContentPane().add(l);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
    }
}
